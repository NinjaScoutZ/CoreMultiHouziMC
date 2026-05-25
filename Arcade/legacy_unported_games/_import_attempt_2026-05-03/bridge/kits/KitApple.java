package com.houzicore.arcade.nautilus.game.arcade.game.games.bridge.kits;

import org.bukkit.entity.Player;

import com.houzicore.arcade.nautilus.game.arcade.kit.GameKit;

import com.houzicore.arcade.ArcadeManager;
import com.houzicore.arcade.nautilus.game.arcade.kit.Kit;
import com.houzicore.arcade.nautilus.game.arcade.kit.Perk;
import com.houzicore.arcade.nautilus.game.arcade.kit.perks.PerkApple;

public class KitApple extends Kit
{

	private static final Perk[] PERKS =
			{
					new PerkApple(7000)
			};

	public KitApple(ArcadeManager manager)
	{
		super(manager, GameKit.BRIDGES_APPLER, PERKS);
	}

	@Override
	public void GiveItems(Player player)
	{

	}
}
