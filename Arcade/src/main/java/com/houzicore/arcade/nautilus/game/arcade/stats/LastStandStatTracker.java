package com.houzicore.arcade.nautilus.game.arcade.stats;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import com.houzicore.shared.common.util.UtilPlayer;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.entity.Player;
import com.houzicore.arcade.nautilus.game.arcade.game.Game;
import com.houzicore.arcade.nautilus.game.arcade.game.TeamGame;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;

public class LastStandStatTracker extends StatTracker<TeamGame>
{
	private final Map<UUID, Integer> _kills = new HashMap<>();

	public LastStandStatTracker(TeamGame game)
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

		if (getGame().GetTeam(killer).GetPlayers(true).size() == 1)
		{
			Integer kills = _kills.get(killer.getUniqueId());

			kills = (kills == null ? 0 : kills) + 1;

			_kills.put(killer.getUniqueId(), kills);

			if (kills >= 3)
				addStat(killer, "LastStand", 1, true, false);
		}
	}
}
