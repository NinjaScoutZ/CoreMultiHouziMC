package com.houzicore.arcade.nautilus.game.arcade.game.games.primalgames.kit;

import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import com.houzicore.arcade.ArcadeManager;
import com.houzicore.arcade.nautilus.game.arcade.kit.Kit;
import com.houzicore.arcade.nautilus.game.arcade.kit.KitAvailability;
import com.houzicore.arcade.nautilus.game.arcade.kit.Perk;
import com.houzicore.arcade.nautilus.game.arcade.kit.perks.*;

public class KitArcher extends Kit
{
	public KitArcher(ArcadeManager manager)
	{
		super(manager, "Archer", KitAvailability.Gem, 

				new String[] 
						{
				"Passively crafts arrows from surrounding terrain."
						}, 

						new Perk[] 
								{
				new PerkFletcher(20, 3, true),
				new PerkBarrage(5, 250, true, false),
								}, 
								EntityType.ZOMBIE,
								new ItemStack(Material.BOW));

	}

	@Override
	public void GiveItems(Player player) 
	{
		
	}
}
