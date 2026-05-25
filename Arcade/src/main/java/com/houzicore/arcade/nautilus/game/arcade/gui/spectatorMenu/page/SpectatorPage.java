package com.houzicore.arcade.nautilus.game.arcade.gui.spectatorMenu.page;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;

import com.houzicore.shared.account.CoreClientManager;
import com.houzicore.shared.common.util.C;
import com.houzicore.shared.common.util.UtilColor;
import com.houzicore.shared.common.util.UtilMath;
import com.houzicore.shared.common.util.UtilText;
import com.houzicore.shared.core.donation.DonationManager;
import com.houzicore.shared.core.shop.item.ShopItem;
import com.houzicore.shared.core.shop.page.ShopPageBase;
import com.houzicore.arcade.ArcadeManager;
import com.houzicore.arcade.nautilus.game.arcade.addons.CompassAddon;
import com.houzicore.arcade.nautilus.game.arcade.game.GameTeam;
import com.houzicore.arcade.nautilus.game.arcade.gui.spectatorMenu.SpectatorShop;
import com.houzicore.arcade.nautilus.game.arcade.gui.spectatorMenu.button.SpectatorButton;

/**
 * Created by shaun on 14-09-24.
 */

public class SpectatorPage extends ShopPageBase<CompassAddon, SpectatorShop>
{
	private ArcadeManager _arcadeManager;
	private int _page = 0;

	public SpectatorPage(CompassAddon plugin, ArcadeManager arcadeManager, SpectatorShop shop, CoreClientManager clientManager,
			DonationManager donationManager, Player player)
	{
		super(plugin, shop, clientManager, donationManager, UtilText.toSmallCaps("Spectator Menu"), player);
		_arcadeManager = arcadeManager;
		buildPage();
	}

	@Override
	protected void buildPage()
	{
		int playerCount = _arcadeManager.GetGame().GetPlayers(true).size();
		List<GameTeam> teamList = _arcadeManager.GetGame().GetTeamList();

		if (teamList.size() == 1 && playerCount < 28)
			buildSingleTeam(teamList.get(0), playerCount);
		else
			buildMultipleTeams(teamList, playerCount);

	}

	private void buildSingleTeam(GameTeam team, int playerCount)
	{
		ArrayList<Player> players = team.GetPlayers(true);

		Collections.sort(players, new Comparator<Player>()
		{
			@Override
			public int compare(Player o1, Player o2)
			{
				return o1.getName().compareToIgnoreCase(o2.getName());
			}
		});

		int itemsPerPage = 36;
		int startIndex = _page * itemsPerPage;
		int endIndex = Math.min(startIndex + itemsPerPage, players.size());
		
		int slot = 9;
		
		if (_page == 0)
			setItem(4, getTeamItem(team, playerCount));

		for (int i = startIndex; i < endIndex; i++)
		{
			Player other = players.get(i);
			addPlayerItem(slot, team, other);
			slot++;
		}
		
		buildNavigation(players.size(), itemsPerPage);
	}

	private void buildMultipleTeams(List<GameTeam> teamList, int playerCount)
	{
		int startRow = _page * 5;
		int endRow = startRow + 5;
		
		int totalRows = 0;

		for (GameTeam team : teamList)
		{
			ArrayList<Player> teamPlayers = team.GetPlayers(true);
			int rowsNeeded = (int) Math.ceil(teamPlayers.size() / 8.0);

			Collections.sort(teamPlayers, new Comparator<Player>()
			{
				@Override
				public int compare(Player o1, Player o2)
				{
					return o1.getName().compareToIgnoreCase(o2.getName());
				}
			});

			for (int row = 0; row < rowsNeeded; row++)
			{
				if (totalRows >= startRow && totalRows < endRow)
				{
					int renderRow = totalRows - startRow;
					int woolSlot = renderRow * 9;

					setItem(woolSlot, getTeamItem(team, teamPlayers.size()));

					int playerIndex = row * 8;
					for (int i = 0; i < 8 && playerIndex < teamPlayers.size(); i++, playerIndex++)
					{
						Player other = teamPlayers.get(playerIndex);
						int slot = woolSlot + 1 + i;
						addPlayerItem(slot, team, other);
					}
				}
				totalRows++;
			}
			
			// Optional padding
			if (rowsNeeded == 1 && teamList.size() < 4 && playerCount <= 26 && totalRows < endRow)
				totalRows += 1;
		}
		
		buildNavigation(totalRows, 5);
	}
	
	private void buildNavigation(int totalItems, int itemsPerPage)
	{
		int totalPages = (int) Math.ceil((double) totalItems / itemsPerPage);
		if (totalPages <= 1) return;
		
		if (_page > 0)
		{
			ItemStack prev = new ItemStack(Material.ARROW);
			ItemMeta meta = prev.getItemMeta();
			meta.setDisplayName(ChatColor.RED + "← Previous Page");
			prev.setItemMeta(meta);
			
			addButton(45, new ShopItem(prev, "Previous Page", "Previous Page", 1, false, false), new com.houzicore.shared.core.shop.item.IButton() {
				@Override
				public void onClick(Player player, org.bukkit.event.inventory.ClickType clickType) {
					_page--;
					refresh();
				}
			});
		}
		
		ItemStack map = new ItemStack(Material.MAP);
		ItemMeta mapMeta = map.getItemMeta();
		mapMeta.setDisplayName(ChatColor.GRAY + "Page " + ChatColor.YELLOW + (_page + 1) + ChatColor.GRAY + "/" + ChatColor.YELLOW + totalPages);
		map.setItemMeta(mapMeta);
		setItem(49, map);
		
		if (_page < totalPages - 1)
		{
			ItemStack next = new ItemStack(Material.ARROW);
			ItemMeta meta = next.getItemMeta();
			meta.setDisplayName(ChatColor.GREEN + "Next Page →");
			next.setItemMeta(meta);
			
			addButton(53, new ShopItem(next, "Next Page", "Next Page", 1, false, false), new com.houzicore.shared.core.shop.item.IButton() {
				@Override
				public void onClick(Player player, org.bukkit.event.inventory.ClickType clickType) {
					_page++;
					refresh();
				}
			});
		}
	}

	private void addPlayerItem(int slot, GameTeam team, Player other)
	{
		ItemStack playerItem = getPlayerItem(team, other);
		ShopItem shopItem = new ShopItem(playerItem, other.getName(), other.getName(), 1, false, false);
		addButton(slot, shopItem, new SpectatorButton(_arcadeManager, getPlayer(), other));
	}

	private ItemStack getTeamItem(GameTeam team, int playerCount)
	{
		ItemStack item = new ItemStack(Material.WHITE_WOOL, 1, (short) 0, UtilColor.chatColorToWoolData(team.GetColor()));

		ItemMeta meta = item.getItemMeta();
		String teamName = UtilText.toSmallCaps(team.GetFormattedName());
		String emoji = "🛡";
		if (_arcadeManager.GetGame().GetTeamList().size() == 1) emoji = "⚔";
		
		meta.setDisplayName(team.GetColor() + emoji + " " + teamName);
		meta.setLore(Arrays.asList(
				"§8──────────────────────",
				" ",
				"§7เมื่อสังเวียนดำเนินไป... ย่อมมีผู้หลงเหลือ",
				"§7ทีม §e" + team.GetFormattedName() + " §7มีผู้เล่น §a" + playerCount + " §7คน",
				" ",
				"§8──────────────────────"
		));
		item.setItemMeta(meta);

		return item;
	}

	private String getDirectionArrow(org.bukkit.Location from, org.bukkit.Location to)
	{
		if (from == null || to == null) return "•";
		org.bukkit.util.Vector direction = to.toVector().subtract(from.toVector());
		if (direction.lengthSquared() == 0) return "•";
		
		double angle = Math.atan2(direction.getZ(), direction.getX());
		double yaw = (angle * 180 / Math.PI) - 90;
		double relativeYaw = yaw - from.getYaw();
		relativeYaw = (relativeYaw % 360 + 360) % 360;
		
		if (relativeYaw >= 337.5 || relativeYaw < 22.5) return "↑";
		if (relativeYaw >= 22.5 && relativeYaw < 67.5) return "↗";
		if (relativeYaw >= 67.5 && relativeYaw < 112.5) return "→";
		if (relativeYaw >= 112.5 && relativeYaw < 157.5) return "↘";
		if (relativeYaw >= 157.5 && relativeYaw < 202.5) return "↓";
		if (relativeYaw >= 202.5 && relativeYaw < 247.5) return "↙";
		if (relativeYaw >= 247.5 && relativeYaw < 292.5) return "←";
		if (relativeYaw >= 292.5 && relativeYaw < 337.5) return "↖";
		
		return "•";
	}

	private ItemStack getPlayerItem(GameTeam team, Player other)
	{
		ItemStack item = new ItemStack(Material.PLAYER_HEAD, 1);

		double distance = UtilMath.offset(getPlayer(), other);
		double heightDifference = other.getLocation().getY() - getPlayer().getLocation().getY();
		
		String hFlag = heightDifference > 0 ? "§a+" : "§c";
		String arrow = getDirectionArrow(getPlayer().getLocation(), other.getLocation());
		String kitName = _arcadeManager.GetGame().GetKit(other) != null ? _arcadeManager.GetGame().GetKit(other).GetName() : "None";

		java.util.HashMap<String, Integer> stats = _arcadeManager.GetGame().GetStats().containsKey(other) ? _arcadeManager.GetGame().GetStats().get(other) : null;
		int kills = stats != null && stats.containsKey("Kills") ? stats.get("Kills") : 0;
		double hp = Math.ceil(other.getHealth() / 2.0);
		double maxHp = other.getMaxHealth() / 2.0;

		ArrayList<String> lore = new ArrayList<String>();
		lore.add("§8──────────────────────");
		lore.add(" ");
		lore.add("§7📍 §eKit: §f" + kitName);
		lore.add("§7⚔ §eKills: §c" + kills);
		lore.add("§7♥ §eHealth: §c" + (int)hp + "§7/§c" + (int)maxHp);
		lore.add("§7🧭 §eDistance: §f" + UtilMath.trim(1, distance) + "m §7" + arrow);
		lore.add("§7📐 §eHeight: " + hFlag + UtilMath.trim(1, heightDifference) + "m");
		lore.add(" ");
		lore.add("§a✦ คลิกเพื่อจับตาดู");
		lore.add("§8──────────────────────");
		
		SkullMeta skullMeta = ((SkullMeta) item.getItemMeta());
		skullMeta.setDisplayName(team.GetColor() + "✦ " + UtilText.toSmallCaps(other.getName()));
		skullMeta.setLore(lore);
		item.setItemMeta(skullMeta);

		return item;
	}

}
