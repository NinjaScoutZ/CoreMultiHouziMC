package com.houzicore.shared.core.antihack.types;

import java.util.ArrayList;
import java.util.HashMap;

import com.houzicore.shared.MiniPlugin;
import com.houzicore.shared.core.antihack.AntiHack;
import com.houzicore.shared.core.antihack.Detector;
import com.houzicore.shared.common.util.UtilEvent;
import com.houzicore.shared.common.util.UtilMath;
import com.houzicore.shared.common.util.UtilPlayer;
import com.houzicore.shared.common.util.UtilServer;
import com.houzicore.shared.updater.UpdateType;
import com.houzicore.shared.updater.event.UpdateEvent;

import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;

public class Reach extends MiniPlugin implements Detector {
	private final AntiHack Host;

	private final HashMap<Player, ArrayList<Location>> _history = new HashMap<>();

	public Reach(AntiHack host) {
		super("Reach Detector", host.getPlugin());
		Host = host;
	}

	private boolean isOutOfRange(Location a, Location b) {
		// 2d Range
		final double distFlat = UtilMath.offset2d(a, b); // Limit is 3.40
		if (distFlat > 3.4)
			return true;

		// 3d Range
		final double dist = UtilMath.offset(a, b); // Limit is 6 (highest i saw was 5.67)
		if (dist > 6.0)
			return true;

		return false;
	}

	@EventHandler(priority = EventPriority.LOWEST)
	public void reachDetect(EntityDamageEvent event) {
		if (event.getCause() != DamageCause.ENTITY_ATTACK)
			return;

		if (!(event.getEntity() instanceof Player))
			return;

		final LivingEntity damagerEntity = UtilEvent.GetDamagerEntity(event, false);

		if (!(damagerEntity instanceof Player))
			return;

		final Player damager = (Player) damagerEntity;
		final Player damagee = (Player) event.getEntity();

		if (isOutOfRange(damager.getLocation(), damagee.getLocation())) {
			final ArrayList<Location> damageeHistory = _history.get(damagee);

			if (damageeHistory != null) {
				for (final Location historyLoc : damageeHistory) {
					if (!isOutOfRange(damager.getLocation(), historyLoc))
						return; // Hit was valid due to ping/lag compensation (they were close recently)
				}
			}

			Host.addSuspicion(damager, "Reach");
		}
	}

	@EventHandler(priority = EventPriority.LOWEST)
	public void recordMove(UpdateEvent event) {
		if (!Host.isEnabled())
			return;

		if (event.getType() != UpdateType.TICK)
			return;

		for (final Player player : UtilServer.getPlayers()) {
			if (player.getGameMode() != GameMode.SURVIVAL || UtilPlayer.isSpectator(player)) {
				continue;
			}

			if (!_history.containsKey(player)) {
				_history.put(player, new ArrayList<Location>());
			}

			_history.get(player).add(0, player.getLocation());

			while (_history.get(player).size() > 40) {
				_history.get(player).remove(_history.get(player).size() - 1);
			}
		}
	}

	@Override
	public void Reset(Player player) {
		_history.remove(player);
	}
}
