package com.houzicore.arcade.nautilus.game.arcade.game.games.bedwars.shop;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import com.houzicore.shared.common.Pair;
import com.houzicore.shared.common.util.C;
import com.houzicore.shared.common.util.LineFormat;
import com.houzicore.shared.common.util.UtilItem;
import com.houzicore.shared.common.util.UtilMath;
import com.houzicore.shared.common.util.UtilText;
import com.houzicore.shared.core.itemstack.ItemBuilder;

public enum BedwarsNetherItem implements BedwarsTeamItem
{

	PROTECTION(
			"Protection",
			new ItemStack(Material.DIAMOND_CHESTPLATE),
			"Gives your entire team %s.",
			Pair.create("Protection I", 4),
			Pair.create("Protection II", 10)
	)
			{
				@Override
				public void apply(Player player, int level, Location bed)
				{
					for (ItemStack itemStack : player.getInventory().getArmorContents())
					{
						if (itemStack != null && (itemStack.getType().name().contains("HELMET") || itemStack.getType().name().contains("CHESTPLATE") || itemStack.getType().name().contains("LEGGINGS") || itemStack.getType().name().contains("BOOTS")) && itemStack.getEnchantmentLevel(Enchantment.PROTECTION) < level)
						{
							itemStack.addUnsafeEnchantment(Enchantment.PROTECTION, level);
						}
					}
				}
			},
	HASTE(
			"Haste",
			new ItemStack(Material.GOLDEN_PICKAXE),
			"Gives your entire team %s.",
			Pair.create("Haste I", 4),
			Pair.create("Haste II", 10)
	)
			{
				@Override
				public void apply(Player player, int level, Location bed)
				{
					player.addPotionEffect(new PotionEffect(PotionEffectType.HASTE, Integer.MAX_VALUE, level - 1, true, false), true);
				}
			},
	SHARPNESS(
			"Sharpness",
			new ItemStack(Material.DIAMOND_SWORD),
			"Gives your entire team %s.",
			Pair.create("Sharpness I", 8),
			Pair.create("Sharpness II", 12)
	)
			{
				@Override
				public void apply(Player player, int level, Location bed)
				{
					for (ItemStack itemStack : player.getInventory().getContents())
					{
						if (itemStack != null && itemStack.getType().name().contains("SWORD") && itemStack.getEnchantmentLevel(Enchantment.SHARPNESS) < level)
						{
							itemStack.addUnsafeEnchantment(Enchantment.SHARPNESS, level);
						}
					}
				}
			},
	POWER(
			"Power",
			new ItemStack(Material.BOW),
			"Gives your entire team %s.",
			Pair.create("Power I", 8),
			Pair.create("Power II", 12)
	)
			{
				@Override
				public void apply(Player player, int level, Location bed)
				{
					for (ItemStack itemStack : player.getInventory().getContents())
					{
						if (itemStack != null && itemStack.getType() == Material.BOW && itemStack.getEnchantmentLevel(Enchantment.POWER) < level)
						{
							itemStack.addUnsafeEnchantment(Enchantment.POWER, level);
						}
					}
				}
			},
	RESOURCE(
			"Resource Generator",
			new ItemStack(Material.DIAMOND),
			"When an item generates in your generator it will also generate %s extra.",
			Pair.create("1", 10),
			Pair.create("2", 20)
	),
	REGENERATION(
			"Healing Station",
			new ItemStack(Material.GOLDEN_APPLE),
			"Receive %s when within %s of your bed.",
			Pair.create("Regeneration I;" + BedwarsShopModule.getHealingStationRadius(1) + " blocks", 8),
			Pair.create("Regeneration I;" + BedwarsShopModule.getHealingStationRadius(2) + " blocks", 12)
	)
			{
				@Override
				public void apply(Player player, int level, Location bed)
				{
					int maxDist = BedwarsShopModule.getHealingStationRadius(level);

					if (UtilMath.offset2d(player.getLocation(), bed) < maxDist)
					{
						player.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, 60, 0, true, false), true);
					}
				}
			},
	;

	private final String _name;
	private final ItemStack _itemStack;
	private final String _description;
	private final Pair<String, Integer>[] _levels;

	@SafeVarargs
	BedwarsNetherItem(String name, ItemStack itemStack, String description, Pair<String, Integer>... levels)
	{
		_name = name;
		_itemStack = new ItemBuilder(itemStack)
				.setTitle(name)
				.setGlow(true)
				.build();
		_description = description;
		_levels = levels;
	}

	@Override
	public void apply(Player player, int level, Location bed)
	{
	}

	@Override
	public BedwarsShopItemType getItemType()
	{
		return BedwarsShopItemType.TEAM_UPGRADE;
	}

	@Override
	public String getName()
	{
		return _name;
	}

	@Override
	public ItemStack getItemStack()
	{
		return _itemStack;
	}

	@Override
	public String[] getDescription(int level)
	{
		String description = C.mBody + _description;
		String[] vars = _levels[level].getLeft().split(";");

		for (String var : vars)
		{
			description = description.replaceFirst("%s", C.cGreen + var + C.mBody);
		}

		return UtilText.smartWordWrap(description, 30).toArray(new String[0]);
	}

	@Override
	public Pair<String, Integer>[] getLevels()
	{
		return _levels;
	}

	@Override
	public int getCost()
	{
		return 0;
	}
}
