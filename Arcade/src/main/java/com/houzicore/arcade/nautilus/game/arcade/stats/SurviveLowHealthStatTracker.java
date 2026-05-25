package com.houzicore.arcade.nautilus.game.arcade.stats;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;

import com.houzicore.arcade.nautilus.game.arcade.events.GameStateChangeEvent;
import com.houzicore.arcade.nautilus.game.arcade.game.Game;
import com.houzicore.arcade.nautilus.game.arcade.game.GameTeam;

public class SurviveLowHealthStatTracker extends StatTracker<Game>
{
	public SurviveLowHealthStatTracker(Game game)
	{
		super(game);
	}

	@EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
	public void onGameStateChange(GameStateChangeEvent event)
	{
		if (event.GetState() != Game.GameState.Dead)
			return;

		java.util.List<Player> winners = getGame().getWinners();
		if (winners == null)
			return;

		for (Player player : winners)
		{
			GameTeam team = getGame().GetTeam(player);
			if (team != null && team.GetName().equals("Hiders"))
			{
				if (player.getHealth() <= 1.0D)
				{
					addStat(player, "CloseCall", 1, true, false);
				}
			}
		}
	}
}
