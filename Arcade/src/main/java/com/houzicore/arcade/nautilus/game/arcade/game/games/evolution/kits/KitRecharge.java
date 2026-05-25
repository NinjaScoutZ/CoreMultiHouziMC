package com.houzicore.arcade.nautilus.game.arcade.game.games.evolution.kits;

import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;

import com.houzicore.arcade.ArcadeManager;
import com.houzicore.arcade.nautilus.game.arcade.kit.Kit;
import com.houzicore.arcade.nautilus.game.arcade.kit.KitAvailability;
import com.houzicore.arcade.nautilus.game.arcade.kit.Perk;
import com.houzicore.arcade.nautilus.game.arcade.kit.perks.PerkRecharge;

public class KitRecharge extends Kit
{
	public KitRecharge(ArcadeManager manager)
	{
		super(manager, "Stamina", KitAvailability.Free, 

				// EN
				new String[] 
						{
				"You are able to use your abilities more often!"
						}, 
				// TH
				new String[] 
						{
				"[TH] You are able to use your abilities more often!"
						}, 
				new Perk[] 
								{
				new PerkRecharge(0.5),
								}, 
								EntityType.ZOMBIE,
								null);

	}

	@Override
	public void GiveItems(Player player) 
	{
		
	}
}
