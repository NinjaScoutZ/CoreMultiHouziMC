package com.houzicore.arcade.nautilus.game.arcade.game.games.primalgames.kit;

import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import com.houzicore.shared.recharge.Recharge;
import com.houzicore.arcade.ArcadeManager;
import com.houzicore.arcade.nautilus.game.arcade.kit.Kit;
import com.houzicore.arcade.nautilus.game.arcade.kit.KitAvailability;
import com.houzicore.arcade.nautilus.game.arcade.kit.Perk;
import com.houzicore.arcade.nautilus.game.arcade.kit.perks.*;

public class KitBarbarian extends Kit
{
	public KitBarbarian(ArcadeManager manager)
	{
		super(manager, "Barbarian", KitAvailability.Gem, 6000, 

				new String[] 
						{
				"Skilled at taking out teams!",
				"Abilities disabled for first 30 seconds."
						}, 

						new Perk[] 
								{ 
				
				new PerkCleave(0.75, false),
				new PerkBladeVortex()
								}, 
								EntityType.ZOMBIE,
								new ItemStack(Material.DIAMOND_SWORD));

	}

	@Override
	public void GiveItems(Player player) 
	{
		Recharge.Instance.useForce(player, GetName(), 45000);
	}
}
