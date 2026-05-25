package com.houzicore.arcade.nautilus.game.arcade.stats;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;

import com.houzicore.arcade.nautilus.game.arcade.events.GameStateChangeEvent;
import com.houzicore.arcade.nautilus.game.arcade.game.Game;
import com.houzicore.arcade.nautilus.game.arcade.game.GameTeam;
import com.houzicore.shared.common.util.UtilTime;

import org.bukkit.event.entity.PlayerDeathEvent;

public class FirstBloodStatTracker extends StatTracker<Game>
{
	private boolean _firstBloodTaken = false;

	public FirstBloodStatTracker(Game game)
	{
		super(game);
	}

	@EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
	public void onState(GameStateChangeEvent event)
	{
		if (event.GetState() == Game.GameState.Live)
			_firstBloodTaken = false;
	}

	@EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
	public void onPlayerDeath(PlayerDeathEvent event)
	{
		if (getGame().GetState() != Game.GameState.Live)
			return;

		if (_firstBloodTaken)
			return;

		Player killer = event.getEntity().getKiller();
		if (killer != null)
		{
			GameTeam team = getGame().GetTeam(killer);
			if (team != null && team.GetName().equals("Hunters"))
			{
				if (UtilTime.elapsed(getGame().GetStateTime(), 30000))
					return;

				_firstBloodTaken = true;
				addStat(killer, "FirstBlood", 1, true, false);
			}
		}
	}
}
