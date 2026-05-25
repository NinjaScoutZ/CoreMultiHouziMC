package com.houzicore.arcade.nautilus.game.arcade.game.games.turfforts.kits;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import com.houzicore.arcade.nautilus.game.arcade.kit.GameKit;
import com.houzicore.shared.core.itemstack.ItemStackFactory;

import com.houzicore.arcade.ArcadeManager;
import com.houzicore.arcade.nautilus.game.arcade.kit.Kit;
import com.houzicore.arcade.nautilus.game.arcade.kit.Perk;
import com.houzicore.arcade.nautilus.game.arcade.kit.perks.PerkConstructor;
import com.houzicore.arcade.nautilus.game.arcade.kit.perks.PerkFletcher;

public class KitInfiltrator extends Kit
{

	private static final Perk[] PERKS =
			{
					new PerkConstructor("Constructor", 4, 4, Material.WOOL, "Wool", false),
					new PerkFletcher(8, 1, false),
			};

	private static final ItemStack[] PLAYER_ITEMS =
			{
					ItemStackFactory.Instance.CreateStack(Material.IRON_SWORD),
					ItemStackFactory.Instance.CreateStack(Material.BOW),
			};

	public KitInfiltrator(ArcadeManager manager)
	{
		super(manager, GameKit.TURF_WARS_INFILTRATOR, PERKS);
	}

	@Override
	public void GiveItems(Player player)
	{
		player.getInventory().addItem(PLAYER_ITEMS);

		player.getInventory().addItem(ItemStackFactory.Instance.CreateStack(Material.WOOL, Manager.GetGame().GetTeam(player).GetColorData(), Manager.GetGame().IsLive() ? 6 : 64));

		Bukkit.getScheduler().scheduleSyncDelayedTask(Manager.getPlugin(), player::updateInventory, 10);
	}
}

