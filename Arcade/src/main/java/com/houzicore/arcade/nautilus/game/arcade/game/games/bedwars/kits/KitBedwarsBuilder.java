package com.houzicore.arcade.nautilus.game.arcade.game.games.bedwars.kits;

import org.bukkit.entity.Player;

import com.houzicore.shared.core.game.kit.GameKit;

import com.houzicore.arcade.ArcadeManager;
import com.houzicore.arcade.nautilus.game.arcade.game.games.bedwars.kits.perk.PerkPassiveWoolGain;
import com.houzicore.arcade.nautilus.game.arcade.kit.Kit;
import com.houzicore.arcade.nautilus.game.arcade.kit.Perk;

public class KitBedwarsBuilder extends Kit
{

	private static final Perk[] PERKS =
			{
					new PerkPassiveWoolGain()
			};

	public KitBedwarsBuilder(ArcadeManager manager)
	{
		super(manager, GameKit.BED_WARS_BUILDER, PERKS);
	}

	@Override
	public void GiveItems(Player player)
	{
	}

}
