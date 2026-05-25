package com.houzicore.arcade.nautilus.game.arcade.game.games.bedwars.kits;

import org.bukkit.entity.Player;

import com.houzicore.shared.core.game.kit.GameKit;

import com.houzicore.arcade.ArcadeManager;
import com.houzicore.arcade.nautilus.game.arcade.game.games.bedwars.kits.perk.PerkSlowSnowball;
import com.houzicore.arcade.nautilus.game.arcade.kit.Kit;
import com.houzicore.arcade.nautilus.game.arcade.kit.Perk;

public class KitBedwarsFrosting extends Kit
{

	private static final Perk[] PERKS =
			{
					new PerkSlowSnowball()
			};

	public KitBedwarsFrosting(ArcadeManager manager)
	{
		super(manager, GameKit.BED_WARS_FROSTING, PERKS);
	}

	@Override
	public void GiveItems(Player player)
	{
	}
}
