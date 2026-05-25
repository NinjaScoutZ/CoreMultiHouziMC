package com.houzicore.arcade.nautilus.game.arcade.game.games.bedwars.kits;

import org.bukkit.entity.Player;

import com.houzicore.shared.core.game.kit.GameKit;

import com.houzicore.arcade.ArcadeManager;
import com.houzicore.arcade.nautilus.game.arcade.game.games.bedwars.kits.perk.PerkLifeSteal;
import com.houzicore.arcade.nautilus.game.arcade.kit.Kit;
import com.houzicore.arcade.nautilus.game.arcade.kit.Perk;

public class KitBedwarsWarrior extends Kit
{

	private static final Perk[] PERKS =
			{
				new PerkLifeSteal(6)
			};

	public KitBedwarsWarrior(ArcadeManager manager)
	{
		super(manager, GameKit.BED_WARS_WARRIOR, PERKS);
	}

	@Override
	public void GiveItems(Player player)
	{
	}
}
