package com.houzicore.shared.core.scoreboard;

import java.util.ArrayList;

import com.houzicore.shared.core.scoreboard.elements.*;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

public class ScoreboardData {
	private final ArrayList<ScoreboardElement> _elements = new ArrayList<>();

	public ScoreboardData() {

	}

	public ArrayList<String> getLines(ScoreboardManager manager, Player player) {
		final ArrayList<String> output = new ArrayList<>();

		for (final ScoreboardElement elem : _elements) {
			output.addAll(elem.GetLines(manager, player));
		}

		return output;
	}

	public String prepareLine(String line) {
		if (line.length() > 28) {
			// Due to the scoreboard using teams, You can use prefix and suffix for a total
			// length of 32.
			// this means that the total length of the string can't extend 32.
			// Reason for the fancy logic is that the beginning of the suffix needs to use
			// colors from line1 else the line is pure
			// white. And line2 can't have its length extend 16..

			final String line1 = line.substring(0, 16);
			final String color = ChatColor.getLastColors(line1);
			final String line2 = line.substring(16);

			final int length = 16 - (color + line2).length();

			if (length > 0)
				return line1 + line2.substring(0, line2.length() - length);
		}

		return line;
	}

	public void reset() {
		_elements.clear();
	}

	public void write(String line) {
		line = prepareLine(line);
		_elements.add(new ScoreboardElementText(line));
	}
	
	public void addElement(ScoreboardElement elem) {
		_elements.add(elem);
	}

	public void writeEmpty() {
		_elements.add(new ScoreboardElementText(" "));
	}

	public void writeOrdered(String key, String line, int value, boolean prependScore) {
		if (prependScore) {
			line = value + " " + line;
		}

		line = prepareLine(line);

		for (final ScoreboardElement elem : _elements) {
			if (elem instanceof ScoreboardElementScores) {
				final ScoreboardElementScores scores = (ScoreboardElementScores) elem;

				if (scores.IsKey(key)) {
					scores.AddScore(line, value);
					return;
				}
			}
		}

		_elements.add(new ScoreboardElementScores(key, line, value, true));
	}

	public void writePlayerCoins() {
		_elements.add(new ScoreboardElementCoinCount());
	}

	public void writePlayerGems() {
		_elements.add(new ScoreboardElementEssenceCount());
	}

	public void writePlayerRank() {
		_elements.add(new ScoreboardElementRank());
	}

	public void writePlayerLevel() {
		_elements.add(new ScoreboardElementLevel());
	}

	public void writeOnlinePlayers() {
		_elements.add(new ScoreboardElementOnline());
	}

	public void writeDate() {
		_elements.add(new ScoreboardElementDate());
	}

	public void writePlayerName() {
		_elements.add(new ScoreboardElementPlayerName());
	}

	public void writeAnnouncement() {
		_elements.add(new ScoreboardElementAnnounce(com.houzicore.shared.core.announce.AnnounceManager.getInstance()));
	}
}
