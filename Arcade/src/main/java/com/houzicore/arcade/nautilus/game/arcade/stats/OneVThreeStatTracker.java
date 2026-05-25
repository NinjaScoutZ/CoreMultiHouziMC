package com.houzicore.arcade.nautilus.game.arcade.stats;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;

import com.houzicore.shared.common.util.UtilPlayer;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.entity.Player;
import com.houzicore.arcade.nautilus.game.arcade.events.GameStateChangeEvent;
import com.houzicore.arcade.nautilus.game.arcade.game.Game;

public class OneVThreeStatTracker extends StatTracker<Game>
{
	private final Map<UUID, Integer> _killCount = new HashMap<>();

	public OneVThreeStatTracker(Game game)
	{
		super(game);
	}

	@EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
	public void onCombatDeath(PlayerDeathEvent event)
	{
		if (getGame().GetState() != Game.GameState.Live)
			return;

		if (event.getEntity().getKiller() == null)
			return;

		// getKiller is natively Player

		Player killer = UtilPlayer.searchExact(event.getEntity().getKiller().getName());
		if (killer == null)
			return;

		if (event.getEntity() == null)
			return;

		// getEntity is natively Player

		Player player = UtilPlayer.searchExact(event.getEntity().getName());
		if (player == null)
			return;

		Integer killCount = _killCount.get(killer.getUniqueId());
		if (killCount == null)
			killCount = 0;

		killCount++;

		_killCount.put(killer.getUniqueId(), killCount);
	}

	@EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
	public void onGameStateChange(GameStateChangeEvent event)
	{
		if (event.GetState() == Game.GameState.End)
		{
			for (Player player : getGame().GetPlayers(false))
			{
				Integer killCount = _killCount.get(player.getUniqueId());

				if (killCount != null && killCount >= 10)
					addStat(player, "1v3", 1, true, false);
			}
		}
	}
}
