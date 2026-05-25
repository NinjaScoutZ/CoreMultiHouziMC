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

public class KitMystic extends Kit
{
	public KitMystic(ArcadeManager manager)
	{
		super(manager, "Mystic", KitAvailability.Gem, new String[]
			{
				"Mana regeneration increased by 10%"
			}, new Perk[0], EntityType.WITCH, new ItemStack(Material.WOODEN_HOE));
	}

	@Override
	public void GiveItems(Player player)
	{
		((Wizards) this.Manager.GetGame()).setupWizard(player);
	}
}
