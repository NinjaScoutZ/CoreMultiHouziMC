package com.houzicore.shared.core.scoreboard;

import java.util.ArrayList;
import java.util.List;
import org.bukkit.entity.Player;
import net.kyori.adventure.text.Component;

/**
 * Default data provider that implements fallback to the legacy ScoreboardData system.
 */
public class DefaultScoreboardDataProvider implements ScoreboardDataProvider {
	private final ScoreboardManager _manager;
	private final PlayerScoreboard _playerScoreboard;

	public DefaultScoreboardDataProvider(ScoreboardManager manager, PlayerScoreboard playerScoreboard) {
		_manager = manager;
		_playerScoreboard = playerScoreboard;
	}

	@Override
	public Component getTitle(Player player) {
		String titleStr = _playerScoreboard.getLastTitleStr();
		if (titleStr == null || titleStr.isEmpty()) {
			if (org.bukkit.Bukkit.getPluginManager().isPluginEnabled("Arcade") || org.bukkit.Bukkit.getPluginManager().isPluginEnabled("HouziCoreArcade")) {
				titleStr = com.houzicore.shared.common.util.C.Bold + "   ARCADE   ";
			} else {
				titleStr = com.houzicore.shared.common.util.C.Bold + "   " + com.houzicore.shared.core.common.BrandConfig.mainServerName().toUpperCase(java.util.Locale.ROOT) + "   ";
			}
		}
		return _playerScoreboard.parseLine(titleStr);
	}

	@Override
	public List<Component> getLines(Player player) {
		List<Component> lines = new ArrayList<>();
		ScoreboardData data = _manager.getData(_playerScoreboard.getScoreboardDataName(), false);
		if (data == null) {
			data = _manager.getData("default", false);
		}
		if (data != null) {
			List<String> rawLines = data.getLines(_manager, player);
			for (String raw : rawLines) {
				lines.add(_playerScoreboard.parseLine(raw));
			}
		}
		return lines;
	}
}
