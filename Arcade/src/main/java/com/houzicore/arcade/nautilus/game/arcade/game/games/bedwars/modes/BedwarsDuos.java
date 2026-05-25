package com.houzicore.arcade.nautilus.game.arcade.game.games.bedwars.modes;

import com.houzicore.arcade.ArcadeManager;
import com.houzicore.arcade.GameType;
import com.houzicore.arcade.nautilus.game.arcade.game.games.bedwars.Bedwars;
import com.houzicore.arcade.nautilus.game.arcade.game.team.TeamRequestsModule;
import com.houzicore.arcade.nautilus.game.arcade.game.team.selectors.FillToSelector;

public class BedwarsDuos extends Bedwars
{

	public BedwarsDuos(ArcadeManager manager)
	{
		super(manager, GameType.Bedwars);

		HideTeamSheep = true;
		_teamSelector = new FillToSelector(this, 2);

		new TeamRequestsModule(this)
				.register();
	}

	@Override
	public String GetMode()
	{
		return "Duos";
	}
}
