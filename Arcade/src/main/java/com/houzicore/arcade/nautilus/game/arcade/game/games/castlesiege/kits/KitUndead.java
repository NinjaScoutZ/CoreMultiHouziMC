package com.houzicore.arcade.nautilus.game.arcade.game.games.castlesiege.kits;

import org.bukkit.entity.EntityType;
import org.bukkit.inventory.ItemStack;

import com.houzicore.arcade.ArcadeManager;
import com.houzicore.arcade.nautilus.game.arcade.kit.Kit;
import com.houzicore.arcade.nautilus.game.arcade.kit.KitAvailability;
import com.houzicore.arcade.nautilus.game.arcade.kit.Perk;

public abstract class KitUndead extends Kit
{
	public KitUndead(ArcadeManager manager, String name, KitAvailability kitAvailability, String[] kitDesc, Perk[] kitPerks, EntityType entityType, ItemStack itemInHand)
	{
		super(manager, name, kitAvailability, kitDesc, kitPerks, entityType, itemInHand);
	}

	public KitUndead(ArcadeManager manager, String name, KitAvailability kitAvailability, int cost, String[] kitDesc, Perk[] kitPerks, EntityType entityType, ItemStack itemInHand)
	{
		super(manager, name, kitAvailability, cost, kitDesc, kitPerks, entityType, itemInHand);
	}

	public KitUndead(ArcadeManager manager, String name, KitAvailability kitAvailability, String[] kitDescEn, String[] kitDescTh, Perk[] kitPerks, EntityType entityType, ItemStack itemInHand)
	{
		super(manager, name, kitAvailability, kitDescEn, kitDescTh, kitPerks, entityType, itemInHand);
	}

	public KitUndead(ArcadeManager manager, String name, KitAvailability kitAvailability, int cost, String[] kitDescEn, String[] kitDescTh, Perk[] kitPerks, EntityType entityType, ItemStack itemInHand)
	{
		super(manager, name, kitAvailability, cost, kitDescEn, kitDescTh, kitPerks, entityType, itemInHand);
	}
}
