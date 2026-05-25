package com.houzicore.arcade.nautilus.game.arcade.stats;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;

import com.houzicore.shared.common.util.UtilPlayer;
//import com.houzicore.shared.combat.CombatComponent;
//import com.houzicore.shared.combat.CombatDamage;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
//import com.houzicore.shared.damage.DamageChange;
import com.houzicore.arcade.nautilus.game.arcade.game.Game;

public class KillReasonStatTracker extends StatTracker<Game>
{
	private final String _reason;
	private final String _statName;
	private final boolean _canBeDamagedByKilledPlayer;
	private final Map<UUID, Set<UUID>> _damaged = new HashMap<>();

	public KillReasonStatTracker(Game game, String reason, String statName, boolean canBeDamagedByKilledPlayer)
	{
		super(game);

		_reason = reason;
		_statName = statName;
		_canBeDamagedByKilledPlayer = canBeDamagedByKilledPlayer;
	}

	@EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
	public void onCustomDamage(EntityDamageByEntityEvent event)
	{
		if (canBeDamagedByKilledPlayer())
			return;

		if (getGame().GetState() != Game.GameState.Live)
			return;

		if (!(event.getDamager() instanceof Player)) return;
		Player damager = ((Player) event.getDamager());
		if (damager == null)
			return;

		if (!(event.getEntity() instanceof Player)) return;
		Player damagee = ((Player) event.getEntity());
		if (damagee == null)
			return;

		Set<UUID> set = _damaged.get(damagee.getUniqueId());
		if (set == null)
		{
			set = new HashSet<>();
			_damaged.put(damagee.getUniqueId(), set);
		}
		set.add(damager.getUniqueId());
	}

	@EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
	public void onCombatDeath(PlayerDeathEvent event)
	{
		if (getGame().GetState() != Game.GameState.Live)
			return;

		if (event.getEntity().getKiller() == null)
			return;

		if (!(event.getEntity().getKiller() instanceof Player)) 
			return;

		Player killer = UtilPlayer.searchExact(event.getEntity().getKiller().getName());
		if (killer == null)
			return;

		if (event.getEntity() == null)
			return;

		if (!(event.getEntity() instanceof Player)) 
			return;

		Player player = UtilPlayer.searchExact(event.getEntity().getName());
		if (player == null)
			return;

		if (!canBeDamagedByKilledPlayer())
		{
			Set<UUID> set = _damaged.remove(killer.getUniqueId());
			if (set != null && set.contains(player.getUniqueId()))
				return;
		}

		// Legacy code ignored via proxy
		addStat(killer, getStatName(), 1, false, false);
	}

	public String getStatName()
	{
		return _statName;
	}

	public String getReason()
	{
		return _reason;
	}

	public boolean canBeDamagedByKilledPlayer()
	{
		return _canBeDamagedByKilledPlayer;
	}
}
