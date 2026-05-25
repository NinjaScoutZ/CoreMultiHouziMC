package com.houzicore.shared.core.antihack.types;

import java.util.HashMap;

import org.bukkit.Location;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.player.PlayerMoveEvent;

import com.houzicore.shared.MiniPlugin;
import com.houzicore.shared.common.util.UtilEvent;
import com.houzicore.shared.core.antihack.AntiHack;
import com.houzicore.shared.core.antihack.Detector;

public class KillAura extends MiniPlugin implements Detector {
	private final AntiHack Host;
	private final HashMap<Player, Float> _lastYaw = new HashMap<>();

	public KillAura(AntiHack host) {
		super("KillAura Detector", host.getPlugin());
		Host = host;
	}

	@EventHandler(priority = EventPriority.LOWEST)
	public void onMove(PlayerMoveEvent event) {
		if (!Host.isEnabled()) return;
		_lastYaw.put(event.getPlayer(), event.getFrom().getYaw());
	}

	@EventHandler(priority = EventPriority.LOWEST)
	public void onDamage(EntityDamageEvent event) {
		if (event.getCause() != DamageCause.ENTITY_ATTACK) return;
		
		final LivingEntity damagerEntity = UtilEvent.GetDamagerEntity(event, false);
		if (!(damagerEntity instanceof Player)) return;

		final Player damager = (Player) damagerEntity;
		if (!_lastYaw.containsKey(damager)) return;

		float oldYaw = _lastYaw.get(damager);
		float curYaw = damager.getLocation().getYaw();
		
		float diff = Math.abs(curYaw - oldYaw);
		if (diff > 180) diff = 360 - diff; // Wrap-around handling

		// If a player snaps more than 60 degrees in a single tick and lands a hit immediately
		if (diff > 60.0f) {
			Host.addSuspicion(damager, "KillAura (Snap)");
		}
	}

	@Override
	public void Reset(Player player) {
		_lastYaw.remove(player);
	}
}
