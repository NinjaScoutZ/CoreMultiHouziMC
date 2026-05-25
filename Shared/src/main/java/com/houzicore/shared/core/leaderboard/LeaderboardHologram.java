package com.houzicore.shared.core.leaderboard;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;

import org.bukkit.Location;

import com.houzicore.shared.common.util.C;
import com.houzicore.shared.common.util.Callback;
import com.houzicore.shared.core.hologram.Hologram;
import com.houzicore.shared.core.hologram.HologramManager;
import com.houzicore.shared.core.lang.LangManager;
import com.houzicore.shared.core.stats.StatsManager;
import com.houzicore.shared.updater.UpdateType;
import com.houzicore.shared.updater.event.UpdateEvent;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.Bukkit;

public class LeaderboardHologram implements Listener {

	private final HologramManager _hologramManager;
	private final StatsManager _statsManager;
	private final Hologram _hologram;
	
	private final String _title;
	private final String _statName;
	private final int _limit;

	public LeaderboardHologram(HologramManager hologramManager, StatsManager statsManager, Location location, String title, String statName, int limit) {
		_hologramManager = hologramManager;
		_statsManager = statsManager;
		_title = title;
		_statName = statName;
		_limit = limit;
		
		String loadingLine = LangManager.get() == null
				? C.cGray + "Loading leaderboard..."
				: C.cGray + LangManager.get().get("loading.data", "Loading data...");
		_hologram = new Hologram(_hologramManager, location, title, loadingLine);
		_hologram.start();
		
		Bukkit.getPluginManager().registerEvents(this, _statsManager.getPlugin());
		
		updateLeaderboard();
	}
	
	// Update every minute
	@EventHandler
	public void onUpdate(UpdateEvent event) {
		if (event.getType() == UpdateType.MIN_05) {
			updateLeaderboard();
		}
	}
	
	public void updateLeaderboard() {
		com.houzicore.shared.common.util.Callback<LinkedHashMap<String, Long>> callback = new Callback<LinkedHashMap<String, Long>>() {
			@Override
			public void run(LinkedHashMap<String, Long> data) {
				ArrayList<String> lines = new ArrayList<>();
				lines.add(_title);
				
				if (!data.isEmpty()) {
					lines.add(""); // Add gap only if there is stat data
				}
				
				int rank = 1;
				for (Map.Entry<String, Long> entry : data.entrySet()) {
					String color = C.cYellow;
					if (rank == 1) color = C.cGold + C.Bold;
					else if (rank == 2) color = C.cGray + C.Bold; // Silver
					else if (rank == 3) color = C.cGold; // Bronze (Gold is closest)
					
					String valStr = String.valueOf(entry.getValue());
					if (_statName.contains("Parkour")) {
						long ms = entry.getValue();
						valStr = String.format("%d:%05.2f", ms / 60000, (ms % 60000) / 1000.0);
					}
					
					lines.add(color + "#" + rank + " " + C.cWhite + entry.getKey() + " " + C.cGray + "- " + C.cGreen + valStr);
					rank++;
				}
				
				if (data.isEmpty()) {
					lines.add(C.cGray + (LangManager.get() == null
							? "No data available yet."
							: LangManager.get().get("leaderboard.no_data", "No data available yet.")));
				}
				
				_hologram.setText(lines.toArray(new String[0]));
			}
		};
		
		if (_statName.contains("Parkour")) {
			_statsManager.getLowestStatsAsync(_statName, _limit, callback);
		} else {
			_statsManager.getTopStatsAsync(_statName, _limit, callback);
		}
	}
	
	public void destroy() {
		_hologram.stop();
		UpdateEvent.getHandlerList().unregister(this);
	}
}
