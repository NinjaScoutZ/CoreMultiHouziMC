package com.houzicore.arcade.nautilus.game.arcade.game.games.speedbuilders.kits;

import org.bukkit.entity.Player;

import com.houzicore.shared.core.game.kit.GameKit;

import com.houzicore.arcade.ArcadeManager;
import com.houzicore.arcade.nautilus.game.arcade.kit.Kit;

public class DefaultKit extends Kit
{

	public DefaultKit(ArcadeManager manager)
	{
		super(manager, GameKit.NULL_PLAYER, new com.houzicore.arcade.nautilus.game.arcade.kit.Perk[0]);
	}

	@Override
	public void GiveItems(Player player)
	{
		
	}

}
