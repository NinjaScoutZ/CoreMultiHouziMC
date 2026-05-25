package com.houzicore.arcade.nautilus.game.arcade.stats;

import java.util.List;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;

import com.houzicore.arcade.nautilus.game.arcade.events.GameStateChangeEvent;
import com.houzicore.arcade.nautilus.game.arcade.game.Game;

public class WinStatTracker extends StatTracker<Game>
{
	public WinStatTracker(Game game)
	{
		super(game);
	}

	@EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
	public void onGameStateChange(GameStateChangeEvent event)
	{
		if (event.GetState() == Game.GameState.End)
		{
			List<Player> winners = getGame().getWinners();

			if (winners != null)
			{
				for (Player winner : winners)
				{
					addStat(winner, "Wins", 1, false, false);

//					if (getGame().GetKit(winner) != null)
//						addStat(winner, getGame().GetKit(winner).getName() + " Wins", 1, false, false);
				}
			}
		}
	}
}
