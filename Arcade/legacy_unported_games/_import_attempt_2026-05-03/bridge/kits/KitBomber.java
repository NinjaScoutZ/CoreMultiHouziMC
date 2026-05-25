package com.houzicore.arcade.nautilus.game.arcade.game.games.bridge.kits;

import org.bukkit.entity.Player;

import com.houzicore.arcade.nautilus.game.arcade.kit.GameKit;

import com.houzicore.arcade.ArcadeManager;
import com.houzicore.arcade.nautilus.game.arcade.kit.Kit;
import com.houzicore.arcade.nautilus.game.arcade.kit.Perk;
import com.houzicore.arcade.nautilus.game.arcade.kit.perks.PerkBomber;

public class KitBomber extends Kit
{

	private static final Perk[] PERKS =
			{
					new PerkBomber(25, 2, -1)
			};

	public KitBomber(ArcadeManager manager)
	{
		super(manager, GameKit.BRIDGES_BOMBER, PERKS);
	}

	@Override
	public void GiveItems(Player player)
	{

	}
}

