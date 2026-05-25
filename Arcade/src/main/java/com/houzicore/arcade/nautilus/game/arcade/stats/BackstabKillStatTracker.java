package com.houzicore.arcade.nautilus.game.arcade.stats;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.util.Vector;

import com.houzicore.arcade.nautilus.game.arcade.game.Game;

public class BackstabKillStatTracker extends StatTracker<Game>
{
	public BackstabKillStatTracker(Game game)
	{
		super(game);
	}

	@EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
	public void onPlayerDeath(PlayerDeathEvent event)
	{
		if (getGame().GetState() != Game.GameState.Live)
			return;

		Player killer = event.getEntity().getKiller();
		Player victim = event.getEntity();

		if (killer != null && victim != null)
		{
			Vector killerDir = killer.getLocation().getDirection().setY(0).normalize();
			Vector victimDir = victim.getLocation().getDirection().setY(0).normalize();
			
			if (killerDir.dot(victimDir) > 0.5)
			{
				addStat(killer, "Backstabber", 1, false, false);
			}
		}
	}
}
