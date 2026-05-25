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
import com.houzicore.arcade.nautilus.game.arcade.game.games.hideseek.kits.KitChameleon;
import com.houzicore.arcade.nautilus.game.arcade.game.games.hideseek.kits.KitTracker;

public class HunterOfTheYearStatTracker extends StatTracker<Game>
{
	private final Map<UUID, Integer> _hidersKilled = new HashMap<>();

	public HunterOfTheYearStatTracker(Game game)
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

		if (getGame().GetKit(player) instanceof KitChameleon)
		{
			Integer hidersKilled = _hidersKilled.get(killer.getUniqueId());

			hidersKilled = (hidersKilled == null ? 0 : hidersKilled) + 1;

			_hidersKilled.put(killer.getUniqueId(), hidersKilled);

			if (hidersKilled >= 7)
				addStat(killer, "HunterOfTheYear", 1, true, false);
		}
	}
}
