package com.houzicore.arcade.nautilus.game.arcade.game.games.bedwars.shop;

import org.bukkit.inventory.ItemStack;

public enum BedwarsShopItemType
{

	// Weapons
	SWORD(ItemCategory.SWORD, false, false, true),
	BOW(false, false, true),

	// Tools
	AXE(ItemCategory.AXE, false, false, true),
	PICKAXE(ItemCategory.PICKAXE, false, false, true),
	SHEARS(false, false, true),

	// Armour
	HELMET(false, false, true),
	CHESTPLATE(false, false, true),
	LEGGINGS(false, false, true),
	BOOTS(false, false, true),

	// Blocks
	BLOCK(true, false, true),

	// Special
	TEAM_UPGRADE(false, false, false),
	TRAP(false, true, false),

	// Other
	OTHER(true, false, true);

	private final ItemCategory _removeOnPurchase;
	private final boolean _multiBuy;
	private final boolean _onePerTeam;
	private final boolean _isItem;

	BedwarsShopItemType(boolean multiBuy, boolean onePerTeam, boolean isItem)
	{
		this(null, multiBuy, onePerTeam, isItem);
	}

	BedwarsShopItemType(ItemCategory removeOnPurchase, boolean multiBuy, boolean onePerTeam, boolean isItem)
	{
		_removeOnPurchase = removeOnPurchase;
		_multiBuy = multiBuy;
		_onePerTeam = onePerTeam;
		_isItem = isItem;
	}

	public ItemCategory getRemoveOnPurchase()
	{
		return _removeOnPurchase;
	}

	public boolean isMultiBuy()
	{
		return _multiBuy;
	}

	public boolean isOnePerTeam()
	{
		return _onePerTeam;
	}

	public boolean isItem()
	{
		return _isItem;
	}

	public enum ItemCategory
	{
		SWORD,
		AXE,
		PICKAXE;

		public boolean matches(ItemStack stack)
		{
			if (stack == null) return false;
			String name = stack.getType().name();
			switch (this)
			{
				case SWORD:
					return name.endsWith("_SWORD");
				case AXE:
					return name.endsWith("_AXE");
				case PICKAXE:
					return name.endsWith("_PICKAXE");
				default:
					return false;
			}
		}
	}
}
