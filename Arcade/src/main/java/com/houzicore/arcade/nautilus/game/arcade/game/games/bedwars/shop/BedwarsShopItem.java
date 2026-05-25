package com.houzicore.arcade.nautilus.game.arcade.game.games.bedwars.shop;

import org.bukkit.inventory.ItemStack;

public class BedwarsShopItem implements BedwarsItem
{

	private final BedwarsShopItemType _itemType;
	private final ItemStack _itemStack;
	private final int _cost;

	public BedwarsShopItem(BedwarsShopItemType itemType, ItemStack itemStack, int cost)
	{
		_itemType = itemType;
		_itemStack = itemStack;
		_cost = cost;
	}

	@Override
	public BedwarsShopItemType getItemType()
	{
		return _itemType;
	}

	@Override
	public ItemStack getItemStack()
	{
		return _itemStack;
	}

	@Override
	public int getCost()
	{
		return _cost;
	}
}
