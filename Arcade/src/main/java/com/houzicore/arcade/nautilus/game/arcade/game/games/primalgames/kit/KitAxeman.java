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

public class KitAxeman extends Kit
{
	public KitAxeman(ArcadeManager manager)
	{
		super(manager, "Axeman", KitAvailability.Gem, 5000,

		new String[]
			{
					"A brutal combatant.",
					"Right-Click with an Axe to Leap Slam, dealing area damage!"
			},

		new Perk[]
			{
				new PerkLeapSlam(),
			}, EntityType.ZOMBIE, new ItemStack(Material.DIAMOND_AXE));
	}

	@Override
	public void GiveItems(Player player)
	{

	}
}
