package com.houzicore.arcade.nautilus.game.arcade.game.games.bedwars.ui;

import org.bukkit.Material;

public enum BedwarsShopCategory
{
	QUICK_BUY("Quick Buy", Material.NETHER_STAR),
	BLOCKS("Blocks", Material.ORANGE_TERRACOTTA),
	MELEE("Melee", Material.GOLDEN_SWORD),
	ARMOR("Armor", Material.CHAINMAIL_CHESTPLATE),
	TOOLS("Tools", Material.STONE_PICKAXE),
	RANGED("Ranged", Material.BOW),
	POTIONS("Potions", Material.BREWING_STAND),
	UTILITY("Utility", Material.TNT);

	private final String _name;
	private final Material _material;

	BedwarsShopCategory(String name, Material material)
	{
		_name = name;
		_material = material;
	}

	public String getName()
	{
		return _name;
	}

	public Material getMaterial()
	{
		return _material;
	}
}
