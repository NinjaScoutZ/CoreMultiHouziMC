package com.houzicore.shared.core.simpleStats;

import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.plugin.java.JavaPlugin;

import com.houzicore.shared.MiniPlugin;
import com.houzicore.shared.common.util.NautHashMap;
import com.houzicore.shared.updater.UpdateType;
import com.houzicore.shared.updater.event.UpdateEvent;

public class SimpleStats extends MiniPlugin {
	private static Object _transferLock = new Object();

	private final SimpleStatsRepository _repository = new SimpleStatsRepository();
	private NautHashMap<String, String> _entries = new NautHashMap<>();

	public SimpleStats(JavaPlugin plugin) {
		super("SimpleStats", plugin);

		_repository.initialize();
	}

	/*
	 * public NautHashMap<String, String> getEntries() { synchronized
	 * (_transferLock) { return _entries; } }
	 */

	public NautHashMap<String, String> getStat(String statName) {
		final String statNameFinal = statName;

		Bukkit.getScheduler().runTaskAsynchronously(getPlugin(), new Runnable() {
			@Override
			public void run() {
				synchronized (_transferLock) {
					_entries = _repository.retrieveStat(statNameFinal);
				}
			}
		});

		return _entries;
	}

	public void store(String statName, String statValue) {
		final String statNameFinal = statName;
		final String statValueFinal = statValue;

		Bukkit.getScheduler().runTaskAsynchronously(getPlugin(), new Runnable() {
			@Override
			public void run() {
				synchronized (_transferLock) {
					_repository.storeStatValue(statNameFinal, statValueFinal);
				}
			}
		});
	}

	@EventHandler
	public void storeStatsUpdate(final UpdateEvent updateEvent) {
		if (updateEvent.getType() != UpdateType.SLOW)
			return;

		Bukkit.getScheduler().runTaskAsynchronously(getPlugin(), new Runnable() {
			@Override
			public void run() {
				synchronized (_transferLock) {
					_entries = _repository.retrieveStatRecords();
				}
			}
		});
	}
}
