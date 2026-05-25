package com.houzicore.shared.timing;

import java.util.Map.Entry;

import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;

import com.houzicore.shared.common.util.NautHashMap;

public class TimingManager implements Listener {
	private static TimingManager _instance;

	private static NautHashMap<String, Long> _timingList = new NautHashMap<>();
	private static NautHashMap<String, TimeData> _totalList = new NautHashMap<>();
	private static Object _timingLock = new Object();

	private static Object _totalLock = new Object();
	public static boolean Debug = true;

	public static void endTotal(String title, boolean print) {
		if (!Debug)
			return;

		synchronized (_totalLock) {
			final TimeData data = _totalList.remove(title);

			if (data != null && print) {
				data.printInfo();
			}
		}
	}

	public static TimingManager Initialize(JavaPlugin plugin) {
		if (_instance == null) {
			_instance = new TimingManager(plugin);
		}

		return _instance;
	}

	public static TimingManager instance() {
		return _instance;
	}

	public static void printTotal(String title) {
		if (!Debug)
			return;

		synchronized (_totalLock) {
			_totalList.get(title).printInfo();
		}
	}

	public static void printTotals() {
		if (!Debug)
			return;

		synchronized (_totalLock) {
			for (final Entry<String, TimeData> entry : _totalList.entrySet()) {
				entry.getValue().printInfo();
			}
		}
	}

	public static void start(String title) {
		if (!Debug)
			return;

		synchronized (_timingLock) {
			_timingList.put(title, System.currentTimeMillis());
		}
	}

	public static void startTotal(String title) {
		if (!Debug)
			return;

		synchronized (_totalLock) {
			if (_totalList.containsKey(title)) {
				final TimeData data = _totalList.get(title);
				data.LastMarker = System.currentTimeMillis();

				_totalList.put(title, data);
			} else {
				final TimeData data = new TimeData(title, System.currentTimeMillis());
				_totalList.put(title, data);
			}
		}
	}

	public static void stop(String title) {
		if (!Debug)
			return;

		synchronized (_timingLock) {
			_timingList.remove(title);
		}
	}

	public static void stopTotal(String title) {
		if (!Debug)
			return;

		synchronized (_totalLock) {
			if (_totalList.containsKey(title)) {
				_totalList.get(title).addTime();
			}
		}
	}

	private final JavaPlugin _plugin;

	protected TimingManager(JavaPlugin plugin) {
		_instance = this;

		_plugin = plugin;

		_plugin.getServer().getPluginManager().registerEvents(this, _plugin);
	}
}
