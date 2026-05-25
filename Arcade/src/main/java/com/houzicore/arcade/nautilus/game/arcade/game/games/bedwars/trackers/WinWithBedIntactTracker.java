package com.houzicore.arcade.nautilus.game.arcade.game.games.bedwars.trackers;

import org.bukkit.event.EventHandler;

import com.houzicore.arcade.nautilus.game.arcade.events.GameStateChangeEvent;
import com.houzicore.arcade.nautilus.game.arcade.game.Game.GameState;
import com.houzicore.arcade.nautilus.game.arcade.game.GameTeam;
import com.houzicore.arcade.nautilus.game.arcade.game.games.bedwars.Bedwars;
import com.houzicore.arcade.nautilus.game.arcade.stats.StatTracker;

public class WinWithBedIntactTracker extends StatTracker<Bedwars>
{

	private static final byte ONE_BITE_BED = 6;

	public WinWithBedIntactTracker(Bedwars game)
	{
		super(game);
	}

	@EventHandler
	public void gameEnd(GameStateChangeEvent event)
	{
		if (event.GetState() != GameState.End)
		{
			return;
		}

		GameTeam winners = getGame().WinnerTeam;

		if (winners == null || getGame().getBedwarsTeamModule().getBedwarsTeam(winners).getBed().getBlock().getData() != ONE_BITE_BED)
		{
			return;
		}

		winners.GetPlayers(false).forEach(player -> addStat(player, "WinWithOneBite", 1, true, false));
	}
}
