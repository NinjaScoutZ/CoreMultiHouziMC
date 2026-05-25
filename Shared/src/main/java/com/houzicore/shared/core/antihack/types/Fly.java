package com.houzicore.shared.core.antihack.types;

import java.util.AbstractMap;
import java.util.HashMap;
import java.util.Map.Entry;

import com.houzicore.shared.MiniPlugin;
import com.houzicore.shared.core.antihack.AntiHack;
import com.houzicore.shared.core.antihack.Detector;
import com.houzicore.shared.common.util.UtilBlock;
import com.houzicore.shared.common.util.UtilMath;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.PlayerMoveEvent;

public class Fly extends MiniPlugin implements Detector {
	private final AntiHack Host;

	private final HashMap<Player, Entry<Integer, Double>> _floatTicks = new HashMap<>(); // Ticks,
																														// PrevY
	private final HashMap<Player, Entry<Integer, Double>> _hoverTicks = new HashMap<>(); // Ticks,
																														// PrevY
	private final HashMap<Player, Entry<Integer, Double>> _riseTicks = new HashMap<>(); // Ticks,
																														// PrevY

	public Fly(AntiHack host) {
		super("Fly Detector", host.getPlugin());
		Host = host;
	}

	@Override
	public void Reset(Player player) {
		_floatTicks.remove(player);
		_hoverTicks.remove(player);
		_riseTicks.remove(player);
	}

	private void updateFloat(Player player) {
		int count = 0;

		if (_floatTicks.containsKey(player)) {
			if (player.getLocation().getY() == _floatTicks.get(player).getValue()) {
				count = _floatTicks.get(player).getKey() + 1;
			} else {
				count = 0;
			}
		}

		if (count > Host.FloatHackTicks) {
			Host.addSuspicion(player, "Fly (Float)");
			count -= 2;
		}

		_floatTicks.put(player, new AbstractMap.SimpleEntry<>(count, player.getLocation().getY()));
	}

	@EventHandler(priority = EventPriority.MONITOR)
	public void updateFlyhack(PlayerMoveEvent event) {
		if (!Host.isEnabled())
			return;

		final Player player = event.getPlayer();

		// 100% Valid
		if (Host.isValid(player, true)) {
			Reset(player);
		}

		// Hasn't moved, just looking around
		if (UtilMath.offset(event.getFrom(), event.getTo()) <= 0) {
			updateFloat(player);
			return;
		} else {
			_floatTicks.remove(player);
		}

		updateHover(player);
		updateRise(player);
	}

	private void updateHover(Player player) {
		int count = 0;

		if (_hoverTicks.containsKey(player)) {
			if (player.getLocation().getY() == _hoverTicks.get(player).getValue()) {
				count = _hoverTicks.get(player).getKey() + 1;
			} else {
				count = 0;
			}

			// player.sendMessage(count + " - " + player.getLocation().getY() + " vs " +
			// _hoverTicks.get(player).getValue());
		}

		if (count > Host.HoverHackTicks) {
			Host.addSuspicion(player, "Fly (Hover)");
			count -= 2;
		}

		_hoverTicks.put(player, new AbstractMap.SimpleEntry<>(count, player.getLocation().getY()));
	}

	private void updateRise(Player player) {
		int count = 0;

		if (_riseTicks.containsKey(player)) {
			if (player.getLocation().getY() >= _riseTicks.get(player).getValue()) {
				boolean nearBlocks = false;
				for (final Block block : UtilBlock.getSurrounding(player.getLocation().getBlock(), true)) {
					if (block.getType() != Material.AIR) {
						nearBlocks = true;
						break;
					}
				}

				if (nearBlocks) {
					count = 0;
				} else {
					count = _riseTicks.get(player).getKey() + 1;
				}

			} else {
				count = 0;
			}
		}

		int limit = Host.RiseHackTicks;
		for (org.bukkit.potion.PotionEffect effect : player.getActivePotionEffects()) {
			if (effect.getType().equals(org.bukkit.potion.PotionEffectType.JUMP_BOOST)) {
				limit += (effect.getAmplifier() + 1) * 3;
			}
		}

		if (count > limit) {
			// Only give Offense if actually rising - initial ticks can be trigged via
			// Hover.
			if (player.getLocation().getY() > _riseTicks.get(player).getValue()) {
				Host.addSuspicion(player, "Fly (Rise)");
			}

			count -= 2;
		}

		_riseTicks.put(player, new AbstractMap.SimpleEntry<>(count, player.getLocation().getY()));
	}
}
