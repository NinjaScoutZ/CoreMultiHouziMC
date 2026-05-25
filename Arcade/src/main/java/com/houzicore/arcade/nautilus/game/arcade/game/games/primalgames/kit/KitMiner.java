package com.houzicore.arcade.nautilus.game.arcade.game.games.primalgames.kit;

import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.enchantments.Enchantment;

import com.houzicore.arcade.ArcadeManager;
import com.houzicore.arcade.nautilus.game.arcade.kit.Kit;
import com.houzicore.arcade.nautilus.game.arcade.kit.KitAvailability;
import com.houzicore.arcade.nautilus.game.arcade.kit.Perk;
import com.houzicore.shared.core.itemstack.ItemBuilder;

public class KitMiner extends Kit
{
	public KitMiner(ArcadeManager manager)
	{
		super(manager, "Miner", KitAvailability.Free, 0,

		new String[]
			{
					"Start with a strong pickaxe and fast mining speed to gather ores quickly!"
			},

		new Perk[]
			{
				new PerkMiner(),
			}, EntityType.ZOMBIE, new ItemStack(Material.IRON_PICKAXE));
	}

	@Override
	public void GiveItems(Player player)
	{
		player.getInventory().addItem(new ItemBuilder(Material.IRON_PICKAXE)
				.addEnchantment(Enchantment.EFFICIENCY, 2)
				.addEnchantment(Enchantment.UNBREAKING, 1)
				.setUnbreakable(true)
				.build());
	}
}
