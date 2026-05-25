package com.houzicore.arcade.nautilus.game.arcade.game.games.dragonriders;

import com.houzicore.arcade.ArcadeManager;
import com.houzicore.arcade.GameType;
import com.houzicore.arcade.nautilus.game.arcade.game.SoloGame;
import com.houzicore.arcade.nautilus.game.arcade.game.games.dragonriders.kits.KitRider;
import com.houzicore.arcade.nautilus.game.arcade.kit.Kit;

public class DragonRiders extends SoloGame
{
	public DragonRiders(ArcadeManager manager) 
	{
		super(manager, GameType.DragonRiders,

				new Kit[]
						{
				new KitRider(manager)
						},

						// EN
				new String[]
								{
				
								}, 
				// TH
				new String[]
								{
				
								});
		
		this.Damage = false;
		this.HungerSet = 20;
	}
}
