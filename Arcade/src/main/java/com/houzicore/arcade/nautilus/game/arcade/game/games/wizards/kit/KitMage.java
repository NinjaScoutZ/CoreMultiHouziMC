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

public class KitMage extends Kit
{
	public KitMage(ArcadeManager manager)
	{
		super(manager, "Mage", KitAvailability.Free, new String[]
			{
				"Start with two extra spells"
			}, new Perk[0], EntityType.WITCH, new ItemStack(Material.BLAZE_ROD));
	}

	@Override
	public void GiveItems(Player player)
	{
		((Wizards) this.Manager.GetGame()).setupWizard(player);
	}
}
