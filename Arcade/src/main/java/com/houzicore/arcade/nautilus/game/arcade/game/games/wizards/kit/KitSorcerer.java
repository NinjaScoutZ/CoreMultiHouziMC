package com.houzicore.arcade.nautilus.game.arcade.game.games.wizards.kit;

import com.houzicore.arcade.ArcadeManager;
import com.houzicore.arcade.nautilus.game.arcade.game.games.wizards.Wizards;
import com.houzicore.arcade.nautilus.game.arcade.kit.Kit;
import com.houzicore.arcade.nautilus.game.arcade.kit.KitAvailability;
import com.houzicore.arcade.nautilus.game.arcade.kit.Perk;

import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class KitSorcerer extends Kit
{
	public KitSorcerer(ArcadeManager manager)
	{
		super(manager, "Sorcerer", KitAvailability.Gem, new String[]
			{
				"Start out with an extra wand"
			}, new Perk[0], EntityType.WITCH, new ItemStack(Material.STONE_HOE));
	}

	@Override
	public void GiveItems(Player player)
	{
		((Wizards) this.Manager.GetGame()).setupWizard(player);
	}
}
