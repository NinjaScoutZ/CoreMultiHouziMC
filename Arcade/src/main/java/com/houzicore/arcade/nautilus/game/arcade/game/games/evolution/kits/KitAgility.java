package com.houzicore.arcade.nautilus.game.arcade.game.games.evolution.kits;

import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;

import com.houzicore.arcade.ArcadeManager;
import com.houzicore.arcade.nautilus.game.arcade.kit.Kit;
import com.houzicore.arcade.nautilus.game.arcade.kit.KitAvailability;
import com.houzicore.arcade.nautilus.game.arcade.kit.Perk;
import com.houzicore.arcade.nautilus.game.arcade.kit.perks.PerkDoubleJump;
import com.houzicore.arcade.nautilus.game.arcade.kit.perks.PerkSpeed;

public class KitAgility extends Kit
{
	public KitAgility(ArcadeManager manager)
	{
		super(manager, "Agility", KitAvailability.Free, 

				// EN
				new String[] 
						{
				"You are extremely agile and can double jump!"
						}, 
				// TH
				new String[] 
						{
				"[TH] You are extremely agile and can double jump!"
						}, 
				new Perk[] 
								{
				new PerkDoubleJump("Double Jump", 0.8, 0.8, false),
				new PerkSpeed(0),
								}, 
								EntityType.ZOMBIE,
								null);
	}

	@Override
	public void GiveItems(Player player) 
	{
		
	}
}
