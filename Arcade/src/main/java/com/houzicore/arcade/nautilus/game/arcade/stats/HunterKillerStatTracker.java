package com.houzicore.arcade.nautilus.game.arcade.stats;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;

import com.houzicore.shared.common.util.UtilPlayer;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.entity.Player;
import com.houzicore.arcade.nautilus.game.arcade.game.Game;
import com.houzicore.arcade.nautilus.game.arcade.game.games.hideseek.kits.KitTracker;

public class HunterKillerStatTracker extends StatTracker<Game>
{
	private final Map<UUID, Integer> _huntersKilled = new HashMap<>();

	public HunterKillerStatTracker(Game game)
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

		if (getGame().GetKit(player) instanceof KitTracker)
		{
			Integer huntersKilled = _huntersKilled.get(killer.getUniqueId());

			huntersKilled = (huntersKilled == null ? 0 : huntersKilled) + 1;

			_huntersKilled.put(killer.getUniqueId(), huntersKilled);

			if (huntersKilled >= 10)
				addStat(killer, "HunterKiller", 1, true, false);
		}
	}
}
