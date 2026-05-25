package com.houzicore.arcade.nautilus.game.arcade.stats;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;

import com.houzicore.shared.common.util.UtilPlayer;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.entity.Player;
import com.houzicore.arcade.nautilus.game.arcade.game.Game;
import com.houzicore.arcade.nautilus.game.arcade.game.games.sneakyassassins.SneakyAssassins;

public class TheMastersMasterStatTracker extends StatTracker<SneakyAssassins>
{
	public TheMastersMasterStatTracker(SneakyAssassins game)
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

		if (getGame().getPowerUpManager().getPowerUpCount(killer) == 0 && getGame().getPowerUpManager().getPowerUpCount(player) >= 4)
			addStat(killer, "TheMastersMaster", 1, true, false);
	}
}
