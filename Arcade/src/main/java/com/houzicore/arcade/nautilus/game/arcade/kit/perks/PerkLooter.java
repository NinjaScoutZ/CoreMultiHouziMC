package com.houzicore.arcade.nautilus.game.arcade.kit.perks;

import com.houzicore.shared.common.util.C;
import com.houzicore.arcade.nautilus.game.arcade.kit.Perk;

public class PerkLooter extends Perk
{
	public PerkLooter() 
	{
		super("Looter", new String[] 
				{ 
				C.cGray + "You find extra loot in chests.",
				});
	}
}
