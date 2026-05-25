package com.houzicore.arcade.nautilus.game.arcade.managers;

import java.util.Iterator;

import com.houzicore.shared.common.Rank;
import com.houzicore.shared.common.util.C;
import com.houzicore.shared.common.util.F;
import com.houzicore.shared.core.chat.ChatBadgeFormatter;
import com.houzicore.shared.core.party.Party;
import com.houzicore.shared.TablistFix;
import com.houzicore.arcade.ArcadeManager;
import com.houzicore.arcade.GameType;
import com.houzicore.arcade.nautilus.game.arcade.game.GameTeam;
import com.houzicore.arcade.nautilus.game.arcade.game.Game.GameState;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;

import io.papermc.paper.event.player.AsyncChatEvent;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;

public class GameChatManager implements Listener
{
	ArcadeManager Manager;

	public GameChatManager(ArcadeManager manager)
	{
		Manager = manager; 

		Manager.getPluginManager().registerEvents(this, Manager.getPlugin());
	}  

	@EventHandler
	public void MeCancel(PlayerCommandPreprocessEvent event)
	{
		if (event.getMessage().startsWith("/me "))
		{
			event.getPlayer().sendMessage(F.main("Mirror", "You can't see /me messages, are you a vampire?"));
			event.setCancelled(true);
		}
	}

	@EventHandler(priority = EventPriority.HIGH)
	public void HandleChat(AsyncChatEvent event) 
	{
		if (event.isCancelled())
			return;

		Player sender = event.getPlayer();
		String senderName = sender.getName();

		boolean ownsUltra = false;
		if (Manager.GetGame() != null)
			ownsUltra = Manager.GetDonation().Get(sender.getName()).OwnsUnknownPackage(Manager.GetServerConfig().ServerType + " ULTRA");

		// Fetch the modern Shared prefix Component
		Component modernPrefix = com.houzicore.shared.core.chat.Chat.getChatPrefixComponent(sender);

		// MPS/Event Host Badge Overrides
		if (Manager.GetGameHostManager().isHost(sender))
		{
			String rankBadge = Manager.GetGameHostManager().isEventServer() ? 
				ChatBadgeFormatter.buildSpecialBadge("event host", "#86efac,#22c55e") : 
				ChatBadgeFormatter.buildSpecialBadge("mps host", "#67e8f9,#06b6d4");
			modernPrefix = LegacyComponentSerializer.legacySection().deserialize(rankBadge);
		}
		else if (Manager.GetGameHostManager().isAdmin(sender, false))
		{
			String rankBadge = Manager.GetGameHostManager().isEventServer() ? 
				ChatBadgeFormatter.buildSpecialBadge("event admin", "#fcd34d,#f59e0b") : 
				ChatBadgeFormatter.buildSpecialBadge("mps admin", "#f9a8d4,#ec4899");
			modernPrefix = LegacyComponentSerializer.legacySection().deserialize(rankBadge);
		}

		boolean isDead = false;
		if (Manager.GetGame() != null && Manager.GetGame().GetTeam(sender) != null && !Manager.GetGame().IsAlive(sender)) {
			isDead = true;
		}

		Component deadComponent = isDead ? Component.text("Dead ", NamedTextColor.GRAY) : Component.empty();

		String rawMessage = PlainTextComponentSerializer.plainText().serialize(event.message());

		if (rawMessage.length() > 0 && rawMessage.charAt(0) == '@')
		{
			//Party Chat
			Party party = Manager.getPartyManager().getPartyByPlayer(sender);

			if (party != null)
			{
				event.viewers().clear();
				for (Player p : party.GetPlayersOnline()) {
					event.viewers().add(p);
				}

				// Trim the '@'
				final Component finalMsg = rawMessage.startsWith("@") ? Component.text(rawMessage.substring(1)) : event.message();

				Component prefixComp = modernPrefix
						.append(Component.text("Party ", NamedTextColor.DARK_PURPLE, TextDecoration.BOLD))
						.append(Component.text(senderName, NamedTextColor.WHITE, TextDecoration.BOLD));

				event.renderer((s, sDisplayName, msg, viewer) -> 
					prefixComp.append(Component.space()).append(finalMsg.color(NamedTextColor.LIGHT_PURPLE))
				);
				return;
			}
		}

		// Public/Private/Team
		if (Manager.GetGame() != null && Manager.GetGame().GetState() == GameState.Live)
		{
			// Anti-Ghosting Chat Filter
			if (!Manager.GetGame().IsAlive(sender) && !Manager.GetClients().Get(sender).GetRank().Has(Rank.ADMIN)) {
				event.viewers().removeIf(viewer -> {
					if (viewer instanceof Player) {
						return Manager.GetGame().IsAlive((Player) viewer);
					}
					return false;
				});
			}

			boolean globalMessage = false;

			GameTeam team = Manager.GetGame().GetTeam(sender);

			if (team != null) 
			{
				NamedTextColor teamColor = TablistFix.chatColorToAdventure(team.GetColor());

				if (rawMessage.length() > 0 && rawMessage.charAt(0) == '@')
				{
					// Team Chat
					final Component finalMsg = rawMessage.startsWith("@") ? Component.text(rawMessage.substring(1)) : event.message();

					Component prefixComp = Component.text("Team ", NamedTextColor.WHITE, TextDecoration.BOLD)
							.append(deadComponent)
							.append(modernPrefix)
							.append(Component.text(senderName, teamColor));

					event.renderer((s, sDisplayName, msg, viewer) -> 
						prefixComp.append(Component.space()).append(finalMsg.color(NamedTextColor.WHITE))
					);
				}
				else
				{
					// All Chat
					globalMessage = true;
					Component prefixComp = deadComponent
							.append(modernPrefix)
							.append(Component.text(senderName, teamColor));

					event.renderer((s, sDisplayName, msg, viewer) -> 
						prefixComp.append(Component.space()).append(msg)
					);
				}
			}

			if (globalMessage)
				return;

			// Team Message Remove Recipient
			Iterator<Audience> recipientIterator = event.viewers().iterator();

			while (recipientIterator.hasNext())
			{
				Audience viewer = recipientIterator.next();
				if (!(viewer instanceof Player)) continue;
				Player receiver = (Player) viewer;

				if (!Manager.GetServerConfig().Tournament && Manager.GetClients().Get(receiver).GetRank().Has(Rank.MODERATOR))
					continue;
				
				GameTeam recTeam = Manager.GetGame().GetTeam(receiver);
				GameTeam sendTeam = Manager.GetGame().GetTeam(sender);
				
				if (recTeam == null || sendTeam == null) continue;

				if (!recTeam.equals(sendTeam))
					recipientIterator.remove();
			}
			return;
		}

		// Base Format (Outside of game or fallback)
		NamedTextColor nameColor = TablistFix.chatColorToAdventure(Manager.GetColor(sender));
		Component prefixComp = deadComponent
				.append(modernPrefix)
				.append(Component.text(senderName, nameColor));

		event.renderer((s, sDisplayName, msg, viewer) -> 
			prefixComp.append(Component.space()).append(msg)
		);
	}
}
