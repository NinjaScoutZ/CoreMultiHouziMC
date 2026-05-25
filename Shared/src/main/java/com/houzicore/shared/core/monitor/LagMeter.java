package com.houzicore.shared.core.monitor;

import java.util.HashSet;

import com.houzicore.shared.MiniPlugin;
import com.houzicore.shared.account.CoreClientManager;
import com.houzicore.shared.common.Rank;
import com.houzicore.shared.common.util.F;
import com.houzicore.shared.updater.UpdateType;
import com.houzicore.shared.updater.event.UpdateEvent;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.java.JavaPlugin;

public class LagMeter extends MiniPlugin {
	private final CoreClientManager _clientManager;
	private long _lastRun = -1;
	private int _count;
	private double _ticksPerSecond;
	private double _ticksPerSecondAverage;
	private long _lastAverage;

	private final HashSet<Player> _monitoring = new HashSet<>();

	public LagMeter(JavaPlugin plugin, CoreClientManager clientManager) {
		super("LagMeter", plugin);

		_clientManager = clientManager;
		_lastRun = System.currentTimeMillis();
		_lastAverage = System.currentTimeMillis();
	}

	public double getTicksPerSecond() {
		return _ticksPerSecond;
	}

	@EventHandler
	public void onPlayerCommandPreProcess(PlayerCommandPreprocessEvent event) {
		if (_clientManager.Get(event.getPlayer()).GetRank().Has(Rank.MODERATOR)) {
			if (event.getMessage().trim().equalsIgnoreCase("/lag")) {
				sendUpdate(event.getPlayer());
				event.setCancelled(true);
			} else if (event.getMessage().trim().equalsIgnoreCase("/monitor")) {
				if (_monitoring.contains(event.getPlayer())) {
					_monitoring.remove(event.getPlayer());
				} else {
					_monitoring.add(event.getPlayer());
				}

				event.setCancelled(true);
			}
		}
	}

	@EventHandler
	public void playerQuit(PlayerQuitEvent event) {
		_monitoring.remove(event.getPlayer());
	}

	private void sendUpdate(Player player) {
		player.sendMessage(" ");
		player.sendMessage(" ");
		player.sendMessage(" ");
		player.sendMessage(" ");
		player.sendMessage(" ");
		player.sendMessage(F.main(getName(),
				ChatColor.GRAY + "Live-------" + ChatColor.YELLOW + String.format("%.00f", _ticksPerSecond)));
		player.sendMessage(F.main(getName(), ChatColor.GRAY + "Avg--------" + ChatColor.YELLOW
				+ String.format("%.00f", _ticksPerSecondAverage * 20)));
		player.sendMessage(F.main(getName(), ChatColor.YELLOW + "MEM"));
		player.sendMessage(F.main(getName(), ChatColor.GRAY + "Free-------" + ChatColor.YELLOW
				+ Runtime.getRuntime().freeMemory() / 1048576 + "MB"));
		player.sendMessage(F.main(getName(),
				ChatColor.GRAY + "Max--------" + ChatColor.YELLOW + Runtime.getRuntime().maxMemory() / 1048576) + "MB");
	}

	private void sendUpdates() {
		for (final Player player : _monitoring) {
			sendUpdate(player);
		}
	}

	@EventHandler
	public void update(UpdateEvent event) {
		if (event.getType() != UpdateType.SEC)
			return;

		final long now = System.currentTimeMillis();
		_ticksPerSecond = 1000D / (now - _lastRun) * 20D;

		sendUpdates();

		if (_count % 30 == 0) {
			_ticksPerSecondAverage = 30000D / (now - _lastAverage) * 20D;
			_lastAverage = now;
		}

		_lastRun = now;

		_count++;
	}
}
