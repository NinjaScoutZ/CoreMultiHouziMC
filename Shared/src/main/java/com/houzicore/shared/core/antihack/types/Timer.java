package com.houzicore.shared.core.antihack.types;

import java.util.HashMap;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.PlayerMoveEvent;

import com.houzicore.shared.MiniPlugin;
import com.houzicore.shared.core.antihack.AntiHack;
import com.houzicore.shared.core.antihack.Detector;
import com.houzicore.shared.updater.UpdateType;
import com.houzicore.shared.updater.event.UpdateEvent;

public class Timer extends MiniPlugin implements Detector {
	private final AntiHack Host;
	private final HashMap<Player, Integer> _packets = new HashMap<>();

	public Timer(AntiHack host) {
		super("Timer Detector", host.getPlugin());
		Host = host;
	}

	@EventHandler(priority = EventPriority.LOWEST)
	public void onMove(PlayerMoveEvent event) {
		if (!Host.isEnabled()) return;
		final Player player = event.getPlayer();
		
		_packets.put(player, _packets.getOrDefault(player, 0) + 1);
	}

	@EventHandler
	public void checkTimer(UpdateEvent event) {
		if (event.getType() != UpdateType.SEC) return;
		
		for (Player player : _packets.keySet()) {
			int count = _packets.get(player);
			// Vanilla Minecraft client sends 20 ticks/sec. Minor lag compensation = 22-25.
			// Anything above 30 is almost certainly Timer/FastMath
			if (count > 30) {
				Host.addSuspicion(player, "Timer (Speed Hack)");
			}
			_packets.put(player, 0); // Reset for the next second
		}
	}

	@Override
	public void Reset(Player player) {
		_packets.remove(player);
	}
}
