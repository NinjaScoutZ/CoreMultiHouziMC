package com.houzicore.arcade.nautilus.game.arcade.stats;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityDamageEvent;

import com.houzicore.shared.common.util.UtilPlayer;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.entity.Player;
import com.houzicore.arcade.nautilus.game.arcade.game.Game;

public class DeathBomberStatTracker extends StatTracker<Game>
{
	private int _required;
	private final Map<UUID, Integer> _killCount = new HashMap<>();

	public DeathBomberStatTracker(Game game, int requiredKills)
	{
		super(game);
		
		_required = requiredKills;
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

// 		if (event.getEntity().getKiller() != null && event.getEntity().getKiller().GetReason().contains("Throwing TNT"))
		{
			Integer count = _killCount.get(killer.getUniqueId());

			count = (count == null ? 0 : count) + 1;

			
			_killCount.put(killer.getUniqueId(), count);

			if (count >= _required)
				addStat(killer, "DeathBomber", 1, true, false);
		}
	}
}
