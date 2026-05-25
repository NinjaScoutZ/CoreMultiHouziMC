package com.houzicore.arcade.nautilus.game.arcade.stats;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.houzicore.shared.common.util.UtilPlayer;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.entity.Player;
import com.houzicore.arcade.nautilus.game.arcade.events.GameStateChangeEvent;
import com.houzicore.arcade.nautilus.game.arcade.game.Game;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;

public class WinWithoutDyingStatTracker extends StatTracker<Game>
{
	private final Set<String> _hasDied = new HashSet<String>();
	private final String _stat;

	public WinWithoutDyingStatTracker(Game game, String stat)
	{
		super(game);

		_stat = stat;
	}

	@EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
	public void onCombatDeath(PlayerDeathEvent event)
	{
		if (!getGame().IsLive())
			return;

		if (event.getEntity() == null)
			return;

		// getEntity is natively Player

		Player player = UtilPlayer.searchExact(event.getEntity().getName());
		if (player == null || !player.isOnline())
		{
			return;
		}
		_hasDied.add(player.getUniqueId().toString());
	}

	@EventHandler(priority = EventPriority.MONITOR)
	public void onGameStateChange(GameStateChangeEvent event)
	{
		if (event.GetState() == Game.GameState.End)
		{
			List<Player> winners = getGame().getWinners();

			if (winners != null)
			{
				for (Player winner : winners)
				{
					if (!_hasDied.contains(winner.getUniqueId().toString()))
					{
						addStat(winner, _stat, 1, true, false);
					}
				}
			}
		}
	}

	public String getStat()
	{
		return _stat;
	}
}
