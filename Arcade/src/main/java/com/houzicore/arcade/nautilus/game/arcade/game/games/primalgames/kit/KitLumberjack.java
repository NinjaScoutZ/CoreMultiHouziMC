package com.houzicore.arcade.nautilus.game.arcade.game.games.primalgames.kit;

import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import com.houzicore.arcade.ArcadeManager;
import com.houzicore.arcade.nautilus.game.arcade.kit.Kit;
import com.houzicore.arcade.nautilus.game.arcade.kit.KitAvailability;
import com.houzicore.arcade.nautilus.game.arcade.kit.Perk;
import com.houzicore.shared.core.itemstack.ItemBuilder;

public class KitLumberjack extends Kit
{
	public KitLumberjack(ArcadeManager manager)
	{
		super(manager, "Lumberjack", KitAvailability.Free, 0,

		new String[]
			{
					"Masters of the forest.",
					"Break a log block to instantly chop down the entire tree!"
			},

		new Perk[]
			{
				new PerkLumberjack(),
			}, EntityType.ZOMBIE, new ItemStack(Material.IRON_AXE));
	}

	@Override
	public void GiveItems(Player player)
	{
		player.getInventory().addItem(new ItemBuilder(Material.IRON_AXE).setUnbreakable(true).build());
	}
}
