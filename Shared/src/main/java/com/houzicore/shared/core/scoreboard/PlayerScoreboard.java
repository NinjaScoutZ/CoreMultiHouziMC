package com.houzicore.shared.core.scoreboard;

import java.util.ArrayList;
import java.util.List;

import com.houzicore.shared.common.util.C;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.entity.Player;

/**
 * Per-player packet sidebar renderer backed by scoreboard-library.
 */
public class PlayerScoreboard {
	private final ScoreboardManager _manager;

	private String _scoreboardData = "default";
	private ScoreboardDataProvider _provider;

	public void setScoreboardData(String dataName) {
		this._scoreboardData = dataName;
	}

	public String getScoreboardDataName() {
		return _scoreboardData;
	}

	public void setProvider(ScoreboardDataProvider provider) {
		this._provider = provider;
	}

	public ScoreboardDataProvider getProvider() {
		return _provider;
	}

	public String getLastTitleStr() {
		return _lastTitleStr;
	}

	private ScoreboardSidebar _sidebar;

	private String _lastTitleStr = "";
	private int _lastLineCount = 0;
	private boolean _initialized = false;
	private boolean _hidden = false;

	public PlayerScoreboard(final ScoreboardManager manager, final Player player) {
		_manager = manager;
		_provider = new DefaultScoreboardDataProvider(manager, this);
	}

	public Component parseLine(String lineStr) {
		Component comp = Component.empty();
		java.util.regex.Matcher m = java.util.regex.Pattern.compile("<(texture|sprite):([a-zA-Z0-9_:./]+)>\\s*").matcher(lineStr);
		int lastAppend = 0;
		while (m.find()) {
			String textBefore = lineStr.substring(lastAppend, m.start());
			if (!textBefore.isEmpty()) {
				comp = comp.append(LegacyComponentSerializer.legacySection().deserialize(textBefore));
			}
			String type = m.group(1);
			String key = m.group(2);
			if (type.equals("texture")) {
				Component wide = com.houzicore.shared.core.chat.Chat.buildWideTagComponent(key);
				if (wide != null) {
					comp = comp.append(wide);
				}
			}
			lastAppend = m.end();
		}
		String textAfter = lineStr.substring(lastAppend);
		if (!textAfter.isEmpty()) {
			comp = comp.append(LegacyComponentSerializer.legacySection().deserialize(textAfter));
		}
		return comp;
	}

	public void assignScoreboard(final Player player, final ScoreboardData data) {
		assignScoreboard(player);
	}

	public void assignScoreboard(final Player player) {
		if (_sidebar == null || _sidebar.closed()) {
			_sidebar = _manager.createSidebar();
			_sidebar.addPlayer(player);

			String titleStr;
			if (org.bukkit.Bukkit.getPluginManager().isPluginEnabled("Arcade") || org.bukkit.Bukkit.getPluginManager().isPluginEnabled("HouziCoreArcade")) {
				titleStr = C.Bold + "   ARCADE   ";
			} else {
				titleStr = C.Bold + "   " + com.houzicore.shared.core.common.BrandConfig.mainServerName().toUpperCase(java.util.Locale.ROOT) + "   ";
			}
			_lastTitleStr = titleStr;
			_sidebar.title(parseLine(titleStr));
		}

		_initialized = true;
	}

	public void draw(final ScoreboardManager manager, final Player player) {
		if (!_initialized || _sidebar == null || _sidebar.closed()) {
			assignScoreboard(player);
		}

		if (_hidden) {
			_sidebar.clearLines();
			_lastLineCount = 0;
			return;
		}

		if (_provider != null) {
			Component titleComp = _provider.getTitle(player);
			if (titleComp != null) {
				_sidebar.title(titleComp);
			}

			List<Component> linesToDraw = _provider.getLines(player);
			if (linesToDraw != null) {
				int lineCount = Math.min(linesToDraw.size(), ScoreboardSidebar.MAX_LINES);
				for (int i = 0; i < lineCount; i++) {
					_sidebar.lineWithoutScore(i, linesToDraw.get(i));
				}
				for (int i = lineCount; i < _lastLineCount; i++) {
					_sidebar.line(i, null);
				}
				_lastLineCount = lineCount;
			}
		}
	}

	private ScoreboardData getData() {
		final ScoreboardData data = _manager.getData(_scoreboardData, false);
		if (data != null)
			return data;

		// Revert to default
		_scoreboardData = "default";
		return _manager.getData(_scoreboardData, false);
	}

	public void setTitle(final String out) {
		if (out.equals(_lastTitleStr)) return;
		_lastTitleStr = out;
		if (_sidebar != null && !_sidebar.closed()) {
			if (_provider instanceof DefaultScoreboardDataProvider) {
				_sidebar.title(parseLine(out));
			}
		}
	}

	public void clear() {
		if (_sidebar != null && !_sidebar.closed()) {
			_sidebar.close();
			_sidebar = null;
		}
		_lastLineCount = 0;
		_initialized = false;
	}

	public void setHidden(boolean hidden) {
		this._hidden = hidden;
	}
}
