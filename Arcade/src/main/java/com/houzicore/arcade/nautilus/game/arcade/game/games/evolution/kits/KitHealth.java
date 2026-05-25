package com.houzicore.arcade.nautilus.game.arcade.game.games.evolution.kits;

import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;

import com.houzicore.arcade.ArcadeManager;
import com.houzicore.arcade.nautilus.game.arcade.kit.Kit;
import com.houzicore.arcade.nautilus.game.arcade.kit.KitAvailability;
import com.houzicore.arcade.nautilus.game.arcade.kit.Perk;
import com.houzicore.arcade.nautilus.game.arcade.kit.perks.PerkRegeneration;
import com.houzicore.arcade.nautilus.game.arcade.kit.perks.PerkVampire;

public class KitHealth extends Kit
{
	public KitHealth(ArcadeManager manager)
	{
		super(manager, "Vitality", KitAvailability.Free, 

				// EN
				new String[] 
						{
				"You have improved survivability."
						}, 
				// TH
				new String[] 
						{
				"[TH] You have improved survivability."
						}, 
				new Perk[] 
								{
				new PerkRegeneration(0),
				new PerkVampire(6),
								}, 
								EntityType.ZOMBIE,
								null);
	}

	@Override
	public void GiveItems(Player player) 
	{
		
	}
}
