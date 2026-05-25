package com.houzicore.arcade.nautilus.game.arcade.game.games.bedwars.kits.perk;

import java.util.concurrent.TimeUnit;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.inventory.ItemStack;

import com.houzicore.shared.common.util.C;
import com.houzicore.shared.common.util.F;
import com.houzicore.shared.common.util.UtilInv;
import com.houzicore.shared.common.util.UtilPlayer;
import com.houzicore.shared.core.itemstack.ItemBuilder;
import com.houzicore.shared.recharge.Recharge;
import com.houzicore.shared.updater.UpdateType;
import com.houzicore.shared.updater.event.UpdateEvent;

import com.houzicore.arcade.nautilus.game.arcade.game.GameTeam;
import com.houzicore.arcade.nautilus.game.arcade.game.games.bedwars.item.items.BedwarsDeployPlatform;
import com.houzicore.arcade.nautilus.game.arcade.kit.Perk;

public class PerkPassiveWoolGain extends Perk
{

	private static final long WOOL_RECHARGE = TimeUnit.SECONDS.toMillis(4);
	private static final long PLATFORM_RECHARGE = TimeUnit.SECONDS.toMillis(10);
	private static final int MAX_WOOL = 32;
	private static final int MAX_PLATFORMS = 5;
	private static final String WOOL_NAME = "Knitted Wool";
	private static final String PLATFORM_NAME = "Knitted Platform";
	private static final ItemStack PLATFORM_ITEM = new ItemBuilder(BedwarsDeployPlatform.ITEM_STACK)
			.setTitle(C.cYellow + C.Bold + PLATFORM_NAME)
			.build();

	public PerkPassiveWoolGain()
	{
		super("Knitter", new String[] { "Passively gain wool and platforms" });
	}

	@EventHandler
	public void updateGain(UpdateEvent event)
	{
		if (event.getType() != UpdateType.SEC || !Manager.GetGame().IsLive())
		{
			return;
		}

		for (GameTeam team: Manager.GetGame().GetTeamList())
		{
			for (Player player : team.GetPlayers(true))
			{
				if (UtilPlayer.isSpectator(player) || !Kit.HasKit(player))
				{
					continue;
				}

				Material woolMaterial = getWoolMaterial(team.GetColor());
				if (!player.getInventory().contains(woolMaterial, MAX_WOOL) && Recharge.Instance.use(player, WOOL_NAME, WOOL_RECHARGE, false, false))
				{
					ItemStack itemStack = new ItemStack(woolMaterial, 1);

					player.getInventory().addItem(itemStack);
				}

				if (!UtilInv.contains(player, PLATFORM_NAME, PLATFORM_ITEM.getType(), (byte) -1, MAX_PLATFORMS) && Recharge.Instance.use(player, PLATFORM_NAME, PLATFORM_RECHARGE, false, false))
				{
					ItemStack itemStack = new ItemBuilder(PLATFORM_ITEM)
							.build();

					player.getInventory().addItem(itemStack);
				}
			}
		}
	}

	@EventHandler
	public void inventoryClick(InventoryClickEvent event)
	{
		Player player = (Player) event.getWhoClicked();
		GameTeam team = Manager.GetGame().GetTeam(player);

		if (!Kit.HasKit(player) || team == null)
		{
			return;
		}

		UtilInv.DisallowMovementOf(event, PLATFORM_NAME, PLATFORM_ITEM.getType(), (byte) -1, true);
	}

	@EventHandler
	public void playerDrop(PlayerDropItemEvent event)
	{
		Player player = event.getPlayer();
		ItemStack itemStack = event.getItemDrop().getItemStack();
		GameTeam team = Manager.GetGame().GetTeam(player);

		if (Kit.HasKit(player) && team != null && UtilInv.IsItem(itemStack, PLATFORM_NAME, PLATFORM_ITEM.getType(), (byte) -1))
		{
			event.setCancelled(true);
			event.getPlayer().sendMessage(F.main("Game", "You cannot drop " + F.item(PLATFORM_NAME) + "."));
		}
	}

	private Material getWoolMaterial(org.bukkit.ChatColor chatColor)
	{
		try
		{
			String colorName = chatColor.name();
			if (chatColor == org.bukkit.ChatColor.LIGHT_PURPLE) colorName = "PINK";
			else if (chatColor == org.bukkit.ChatColor.DARK_GREEN) colorName = "GREEN";
			else if (chatColor == org.bukkit.ChatColor.DARK_BLUE || chatColor == org.bukkit.ChatColor.BLUE) colorName = "BLUE";
			else if (chatColor == org.bukkit.ChatColor.GOLD) colorName = "ORANGE";
			else if (chatColor == org.bukkit.ChatColor.GREEN) colorName = "LIME";
			else if (chatColor == org.bukkit.ChatColor.DARK_GRAY) colorName = "GRAY";
			else if (chatColor == org.bukkit.ChatColor.GRAY) colorName = "LIGHT_GRAY";
			else if (chatColor == org.bukkit.ChatColor.DARK_AQUA) colorName = "CYAN";
			else if (chatColor == org.bukkit.ChatColor.DARK_PURPLE) colorName = "PURPLE";
			return Material.valueOf(colorName + "_WOOL");
		}
		catch (Exception e)
		{
			return Material.WHITE_WOOL;
		}
	}
}
