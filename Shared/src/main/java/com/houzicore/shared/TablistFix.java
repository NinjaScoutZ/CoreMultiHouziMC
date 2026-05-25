package com.houzicore.shared;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.java.JavaPlugin;

import com.houzicore.shared.account.CoreClient;
import com.houzicore.shared.account.CoreClientManager;
import com.houzicore.shared.common.Rank;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import com.houzicore.shared.core.chat.PlayerHeadUtil;

public class TablistFix extends MiniPlugin {

	private final CoreClientManager _clientManager;

	public TablistFix(JavaPlugin plugin, CoreClientManager clientManager) {
		super("Tablist Fix", plugin);
		_clientManager = clientManager;
		
		// Repeating task stays disabled; callers explicitly refresh tablist
		// context when rank/team/map data changes.
	}

	@EventHandler(priority = EventPriority.MONITOR)
	public void onJoin(PlayerJoinEvent event) {
		_suffixes.remove(event.getPlayer().getUniqueId());
		_colors.remove(event.getPlayer().getUniqueId());
		updateTablist(event.getPlayer(), _clientManager, null);
	}

	/**
	 * Refreshes the tab list name for a specific player.
	 * Called by UpdateRank to immediately update after rank changes.
	 */
	public void refreshPlayer(Player player) {
		updateTablist(player, _clientManager, null);
	}

	/**
	 * Allows external modules to update the tablist with a specific name color (e.g., Team Colors)
	 */
	public static void updateTablist(Player player, CoreClientManager clientManager, ChatColor nameColor) {
		updateTablist(player, clientManager, nameColor, null, null, null);
	}

	public static void updateTablist(Player player, CoreClientManager clientManager, ChatColor nameColor, Component suffixStr) {
		updateTablist(player, clientManager, nameColor, suffixStr, null, null);
	}

	private static java.util.Map<java.util.UUID, Component> _suffixes = new java.util.concurrent.ConcurrentHashMap<>();
	private static java.util.Map<java.util.UUID, ChatColor> _colors = new java.util.concurrent.ConcurrentHashMap<>();

	public static void setSuffix(Player player, Component suffix) {
		if (suffix == null) _suffixes.remove(player.getUniqueId());
		else _suffixes.put(player.getUniqueId(), suffix);
	}

	public static void updateTablist(Player player, CoreClientManager clientManager, ChatColor nameColor, Component suffixStr, String gameName, String mapName) {
		if (nameColor != null) _colors.put(player.getUniqueId(), nameColor);
		else nameColor = _colors.getOrDefault(player.getUniqueId(), ChatColor.WHITE);

		CoreClient client = clientManager.Get(player);
		Rank rank = (client != null) ? client.GetRank() : Rank.ALL;
		NamedTextColor adventureNameColor = chatColorToAdventure(nameColor);
		
		Component nameComp = Component.text(player.getName(), adventureNameColor);

		Component cachedSuffix = _suffixes.get(player.getUniqueId());
		if (suffixStr != null) {
			nameComp = nameComp.append(suffixStr);
		} else if (cachedSuffix != null) {
			nameComp = nameComp.append(cachedSuffix);
		}
		
		com.houzicore.shared.core.clan.ClanManager clanManager = com.houzicore.shared.core.clan.ClanManager.getInstance();
		if (clanManager != null) {
			com.houzicore.shared.core.clan.Clan clan = clanManager.getClan(player);
			if (clan != null) {
				nameComp = nameComp.append(Component.text(" §b[" + clan.getName() + "]"));
			}
		}
		Component headComp = PlayerHeadUtil.buildInlineHead(player);
		
		// Build wide tag icon for rank
		Component wideTag = com.houzicore.shared.core.chat.Chat.buildWideTagComponent(rank.name());
		
		if (rank == Rank.ALL) {
			if (wideTag != null) {
				player.playerListName(headComp.append(wideTag).append(Component.space()).append(nameComp));
			} else {
				player.playerListName(headComp.append(nameComp));
			}
		} else {
			NamedTextColor rankColor = chatColorToAdventure(rank.GetColor());
			Component prefix = Component.text(rank.Name + " ", rankColor, TextDecoration.BOLD);
			if (wideTag != null) {
				player.playerListName(headComp.append(wideTag).append(Component.space()).append(nameComp));
			} else {
				player.playerListName(headComp.append(prefix).append(nameComp));
			}
		}

		// Update Header and Footer
		net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer leg = net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer.legacySection();

		String headerStr = "§8§m──────────────────────────\n";
		headerStr += com.houzicore.shared.core.common.BrandConfig.tabHeader() + "\n";
		
		boolean isThai = com.houzicore.shared.core.lang.LangManager.get().isThai(player);

		if (isThai) {
			headerStr += "§7เซิร์ฟเวอร์ Minecraft สุดพรีเมียม";
		} else {
			headerStr += "§7The Premium Minecraft Server";
		}
		
		if (gameName == null) gameName = "Waiting...";
		if (mapName == null) mapName = "Unknown";
		
		String footerStr = "§7🎮 " + (isThai ? "เกม: " : "Game: ") + "§e§l" + gameName + "  §8|  §7🗺 " + (isThai ? "แมพ: " : "Map: ") + "§a" + mapName + "\n";
		footerStr += "§fplay." + com.houzicore.shared.core.common.BrandConfig.website() + "\n";
		footerStr += "§8§m──────────────────────────";
		
		net.kyori.adventure.text.Component header = leg.deserialize(headerStr);
		net.kyori.adventure.text.Component footer = leg.deserialize(footerStr);
		player.sendPlayerListHeaderAndFooter(header, footer);
	}

	/**
	 * Map legacy ChatColor to Adventure NamedTextColor
	 */
	public static NamedTextColor chatColorToAdventure(ChatColor color) {
		switch (color) {
			case BLACK: return NamedTextColor.BLACK;
			case DARK_BLUE: return NamedTextColor.DARK_BLUE;
			case DARK_GREEN: return NamedTextColor.DARK_GREEN;
			case DARK_AQUA: return NamedTextColor.DARK_AQUA;
			case DARK_RED: return NamedTextColor.DARK_RED;
			case DARK_PURPLE: return NamedTextColor.DARK_PURPLE;
			case GOLD: return NamedTextColor.GOLD;
			case GRAY: return NamedTextColor.GRAY;
			case DARK_GRAY: return NamedTextColor.DARK_GRAY;
			case BLUE: return NamedTextColor.BLUE;
			case GREEN: return NamedTextColor.GREEN;
			case AQUA: return NamedTextColor.AQUA;
			case RED: return NamedTextColor.RED;
			case LIGHT_PURPLE: return NamedTextColor.LIGHT_PURPLE;
			case YELLOW: return NamedTextColor.YELLOW;
			case WHITE: default: return NamedTextColor.WHITE;
		}
	}
}
