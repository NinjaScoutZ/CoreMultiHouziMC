package com.houzicore.shared.core.scoreboard.elements;

import java.util.ArrayList;
import java.util.HashMap;

import com.houzicore.shared.core.scoreboard.ScoreboardManager;

import org.bukkit.entity.Player;

public class ScoreboardElementScores extends ScoreboardElement {
	private final String _key;

	private final HashMap<String, Integer> _scores;

	private final boolean _higherIsBetter;

	public ScoreboardElementScores(String key, String line, int value, boolean higherIsBetter) {
		_scores = new HashMap<>();

		_key = key;

		AddScore(line, value);

		_higherIsBetter = higherIsBetter;
	}

	public void AddScore(String line, int value) {
		_scores.put(line, value);
	}

	@Override
	public ArrayList<String> GetLines(ScoreboardManager manager, Player player) {
		final ArrayList<String> orderedScores = new ArrayList<>();

		// Order Scores
		while (orderedScores.size() < _scores.size()) {
			String bestKey = null;
			int bestScore = 0;

			for (final String key : _scores.keySet()) {
				if (orderedScores.contains(key)) {
					continue;
				}

				if (bestKey == null || _higherIsBetter && _scores.get(key) >= bestScore
						|| !_higherIsBetter && _scores.get(key) <= bestScore) {
					bestKey = key;
					bestScore = _scores.get(key);
				}
			}

			orderedScores.add(bestKey);
		}

		return orderedScores;
	}

	public boolean IsKey(String key) {
		return _key.equals(key);
	}
}
