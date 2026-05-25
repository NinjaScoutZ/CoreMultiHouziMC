package com.houzicore.arcade.nautilus.game.arcade.stats;

import java.util.HashSet;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

import com.houzicore.arcade.nautilus.game.arcade.events.GameStateChangeEvent;
import com.houzicore.arcade.nautilus.game.arcade.game.Game;

public class WinWithoutTakingPlayerDamageStatTracker extends StatTracker<Game>
{
	private HashSet<Player> _damaged = new HashSet<>();

	public WinWithoutTakingPlayerDamageStatTracker(Game game)
	{
		super(game);
	}

	@EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
	public void onDamage(EntityDamageByEntityEvent event)
	{
		if (getGame().GetState() != Game.GameState.Live)
			return;

		if (event.getEntity() instanceof Player && event.getDamager() instanceof Player)
		{
			_damaged.add((Player) event.getEntity());
		}
	}

	@EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
	public void onGameStateChange(GameStateChangeEvent event)
	{
		if (event.GetState() != Game.GameState.Dead)
			return;

		for (Player player : getGame().getWinners())
		{
			if (!_damaged.contains(player))
			{
				addStat(player, "Survivor", 1, true, false);
			}
		}
	}
}
