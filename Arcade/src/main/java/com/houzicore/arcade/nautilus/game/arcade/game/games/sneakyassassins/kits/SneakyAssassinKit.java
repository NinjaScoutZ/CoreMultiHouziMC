package com.houzicore.arcade.nautilus.game.arcade.game.games.sneakyassassins.kits;

import com.houzicore.shared.common.util.*;
import com.houzicore.shared.core.disguise.*;
import com.houzicore.shared.core.disguise.disguises.*;
import com.houzicore.shared.core.itemstack.*;
import com.houzicore.arcade.*;
import com.houzicore.arcade.nautilus.game.arcade.kit.*;
import org.bukkit.*;
import org.bukkit.entity.*;
import org.bukkit.inventory.*;

public abstract class SneakyAssassinKit extends Kit
{
	public static final ItemStack SMOKE_BOMB = ItemStackFactory.Instance.CreateStack(Material.INK_SAC, (byte) 0, 1,
			C.cYellow + C.Bold + "Right-Click" + C.cWhite + C.Bold + " - " + C.cGreen + C.Bold + "Smoke Bomb",
			new String[]
					{
							ChatColor.RESET + "Throw a Smoke Bomb.",
							ChatColor.RESET + "Everyone within 6 blocks",
							ChatColor.RESET + "gets Blindness for 6 seconds.",

					});

	public SneakyAssassinKit(ArcadeManager manager, String name, KitAvailability kitAvailability, String[] kitDesc, Perk[] kitPerks, ItemStack itemInHand, EntityType disguiseType)
	{
		super(manager, name, kitAvailability, kitDesc, kitPerks, disguiseType, itemInHand);
	}

	public SneakyAssassinKit(ArcadeManager manager, String name, KitAvailability kitAvailability, int cost, String[] kitDesc, Perk[] kitPerks, ItemStack itemInHand, EntityType disguiseType)
	{
		super(manager, name, kitAvailability, cost, kitDesc, kitPerks, disguiseType, itemInHand);
	}

	public SneakyAssassinKit(ArcadeManager manager, String name, KitAvailability kitAvailability, String[] kitDescEn, String[] kitDescTh, Perk[] kitPerks, ItemStack itemInHand, EntityType disguiseType)
	{
		super(manager, name, kitAvailability, kitDescEn, kitDescTh, kitPerks, disguiseType, itemInHand);
	}

	public SneakyAssassinKit(ArcadeManager manager, String name, KitAvailability kitAvailability, int cost, String[] kitDescEn, String[] kitDescTh, Perk[] kitPerks, ItemStack itemInHand, EntityType disguiseType)
	{
		super(manager, name, kitAvailability, cost, kitDescEn, kitDescTh, kitPerks, disguiseType, itemInHand);
	}

	@Override
	public void GiveItems(Player player)
	{
		com.houzicore.shared.api.disguise.DisguiseRequest request = new com.houzicore.shared.api.disguise.DisguiseRequest(
			player.getUniqueId(),
			com.houzicore.shared.api.disguise.DisguiseArchetype.MOB,
			_entityType.name(),
			true,
			false,
			false
		);
		Manager.GetDisguise().getService().apply(player, request);
 
		player.getInventory().addItem(new ItemStack(Material.WOODEN_SWORD));
		player.getInventory().setArmorContents(new ItemStack[]{
				new ItemStack(Material.LEATHER_BOOTS),
				new ItemStack(Material.LEATHER_LEGGINGS),
				new ItemStack(Material.LEATHER_CHESTPLATE),
				new ItemStack(Material.LEATHER_HELMET)
		});
		player.getInventory().addItem(SMOKE_BOMB.clone());
	}
}
