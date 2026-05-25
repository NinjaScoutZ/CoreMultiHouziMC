package com.houzicore.shared.core.scoreboard.elements;

import java.util.ArrayList;

import com.houzicore.shared.core.scoreboard.ScoreboardManager;

import org.bukkit.entity.Player;

public class ScoreboardElementText extends ScoreboardElement {
	private final String _line;

	public ScoreboardElementText(String line) {
		_line = line;
	}

	@Override
	public ArrayList<String> GetLines(ScoreboardManager manager, Player player) {
		final ArrayList<String> orderedScores = new ArrayList<>();

		orderedScores.add(_line);

		return orderedScores;
	}

}
