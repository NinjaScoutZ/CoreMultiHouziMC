package com.houzicore.arcade.nautilus.game.arcade.game.games.holeinwall;

import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;

import com.houzicore.shared.core.achievement.Achievement;
import com.houzicore.arcade.ArcadeManager;
import com.houzicore.arcade.nautilus.game.arcade.kit.Kit;
import com.houzicore.arcade.nautilus.game.arcade.kit.KitAvailability;
import com.houzicore.arcade.nautilus.game.arcade.kit.Perk;

public class KitNormal extends Kit
{
	public KitNormal(ArcadeManager manager)
	{
		super(manager, "Default Kit", KitAvailability.Free,

		new String[]
			{
				"Default kit"
			},

		new Perk[]
			{

			}, EntityType.SKELETON, null);
	}

	@Override
	public void GiveItems(Player player)
	{

	}
}
