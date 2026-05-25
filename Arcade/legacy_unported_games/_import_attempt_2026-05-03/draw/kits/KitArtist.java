package com.houzicore.arcade.nautilus.game.arcade.game.games.draw.kits;

import org.bukkit.entity.Player;

import com.houzicore.arcade.nautilus.game.arcade.kit.GameKit;

import com.houzicore.arcade.ArcadeManager;
import com.houzicore.arcade.nautilus.game.arcade.kit.Kit;

public class KitArtist extends Kit
{

	public KitArtist(ArcadeManager manager)
	{
		super(manager, GameKit.DRAW_ARTIST);
	}

	@Override
	public void GiveItems(Player player)
	{
	}
}

