package com.houzicore.shared.core.antihack.types;

import java.util.AbstractMap;
import java.util.HashMap;
import java.util.Map.Entry;

import com.houzicore.shared.MiniPlugin;
import com.houzicore.shared.core.antihack.AntiHack;
import com.houzicore.shared.core.antihack.Detector;
import com.houzicore.shared.common.util.UtilEnt;
import com.houzicore.shared.common.util.UtilMath;
import com.houzicore.shared.common.util.UtilTime;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class Speed extends MiniPlugin implements Detector {
	private final AntiHack Host;

	private final HashMap<Player, Entry<Integer, Long>> _speedTicks = new HashMap<>(); // Ticks,
																													// PrevY

	public Speed(AntiHack host) {
		super("Speed Detector", host.getPlugin());
		Host = host;
	}

	@Override
	public void Reset(Player player) {
		_speedTicks.remove(player);
	}

	private void UpdateSpeed(Player player, PlayerMoveEvent event) {
		int count = 0;

		if (_speedTicks.containsKey(player)) {
			double offset;
			if (event.getFrom().getY() > event.getTo().getY()) {
				offset = UtilMath.offset2d(event.getFrom(), event.getTo());
			} else {
				offset = UtilMath.offset(event.getFrom(), event.getTo());
			}

			// Limit
			double limit = 0.74;
			if (UtilEnt.isGrounded(player)) {
				limit = 0.32;
			}

			for (final PotionEffect effect : player.getActivePotionEffects()) {
				if (effect.getType().equals(PotionEffectType.SPEED)) {
					if (UtilEnt.isGrounded(player)) {
						limit += 0.08 * (effect.getAmplifier() + 1);
					} else {
						limit += 0.04 * (effect.getAmplifier() + 1);
					}
				}
			}

			// Check
			if (offset > limit && !UtilTime.elapsed(_speedTicks.get(player).getValue(), 100))// Counters Lag
			{
				count = _speedTicks.get(player).getKey() + 1;
			} else {
				count = 0;
			}
		}

		if (count > Host.SpeedHackTicks) {
			Host.addSuspicion(player, "Speed (Fly/Move)");
			count -= 2;
		}

		_speedTicks.put(player, new AbstractMap.SimpleEntry<>(count, System.currentTimeMillis()));
	}

	@EventHandler(priority = EventPriority.MONITOR)
	public void updateSpeedhack(PlayerMoveEvent event) {
		if (!Host.isEnabled())
			return;

		final Player player = event.getPlayer();

		// 100% Valid
		if (Host.isValid(player, false))
			return;

		UpdateSpeed(player, event);
	}
}
