package com.houzicore.arcade.nautilus.game.arcade.game.games.build.kits;

import org.bukkit.entity.Player;

import com.houzicore.shared.core.game.kit.GameKit;

import com.houzicore.arcade.ArcadeManager;
import com.houzicore.arcade.nautilus.game.arcade.kit.Kit;

public class KitBuilder extends Kit
{

	public KitBuilder(ArcadeManager manager)
	{
		super(manager, GameKit.BUILD_BUILDER);
	}
	
	@Override
	public void GiveItems(Player player)
	{
	}

}
