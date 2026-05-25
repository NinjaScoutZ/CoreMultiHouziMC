package com.houzicore.arcade.nautilus.game.arcade.kit.perks;

import com.houzicore.shared.common.util.C;
import com.houzicore.arcade.nautilus.game.arcade.kit.Perk;

public class PerkNotFinished extends Perk
{
	public PerkNotFinished() 
	{
		super("Not Completed", new String[] 
				{ 
				C.cRed + C.Bold + "KIT IS NOT FINISHED",
				});
	}
}
