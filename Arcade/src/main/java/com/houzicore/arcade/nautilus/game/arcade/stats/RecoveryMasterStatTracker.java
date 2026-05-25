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
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import com.houzicore.arcade.nautilus.game.arcade.game.Game;

public class RecoveryMasterStatTracker extends StatTracker<Game>
{
	private final Map<UUID, Double> _damageTaken = new HashMap<>();

	public RecoveryMasterStatTracker(Game game)
	{
		super(game);
	}

	@EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
	public void onCombatDeath(PlayerDeathEvent event)
	{
		if (getGame().GetState() != Game.GameState.Live)
			return;

		if (event.getEntity() == null)
			return;

		// getEntity is natively Player

		Player player = UtilPlayer.searchExact(event.getEntity().getName());
		if (player == null)
			return;

		_damageTaken.remove(player.getUniqueId());
	}

	@EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
	public void onCustomDamage(EntityDamageByEntityEvent event)
	{
		if (getGame().GetState() != Game.GameState.Live)
			return;

		if (!(event.getEntity() instanceof Player)) return;
		Player damagee = ((Player) event.getEntity());
		if (damagee == null)
			return;

		Double damage = _damageTaken.get(damagee.getUniqueId());

		damage = (damage == null ? 0 : damage) + event.getDamage();

		_damageTaken.put(damagee.getUniqueId(), damage);

		if (damage >= 200)
			addStat(damagee, "RecoveryMaster", 1, true, false);
	}
}
