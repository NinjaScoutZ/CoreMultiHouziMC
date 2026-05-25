package com.houzicore.arcade.nautilus.game.arcade.stats;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;

import com.houzicore.arcade.nautilus.game.arcade.events.GameStateChangeEvent;
import com.houzicore.arcade.nautilus.game.arcade.game.Game;

public class WinWithoutKillsStatTracker extends StatTracker<Game>
{
	public WinWithoutKillsStatTracker(Game game)
	{
		super(game);
	}

	@EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
	public void onGameStateChange(GameStateChangeEvent event)
	{
		if (event.GetState() != Game.GameState.Dead)
			return;

		for (Player player : getGame().getWinners())
		{
			if (getGame().GetStats().get(player) == null || !getGame().GetStats().get(player).containsKey("Kills"))
			{
				addStat(player, "Pacifist", 1, true, false);
			}
			else if (getGame().GetStats().get(player).get("Kills") == 0)
			{
				addStat(player, "Pacifist", 1, true, false);
			}
		}
	}
}
