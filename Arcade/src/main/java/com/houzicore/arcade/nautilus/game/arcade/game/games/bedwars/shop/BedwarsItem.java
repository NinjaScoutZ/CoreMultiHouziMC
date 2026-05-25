package com.houzicore.arcade.nautilus.game.arcade.game.games.bedwars.shop;

import org.bukkit.inventory.ItemStack;

public interface BedwarsItem
{

	BedwarsShopItemType getItemType();

	ItemStack getItemStack();

	int getCost();

}
