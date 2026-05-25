package com.houzicore.shared.core.scoreboard.elements;

import java.util.ArrayList;

import org.bukkit.entity.Player;

import com.houzicore.shared.common.util.C;
import com.houzicore.shared.core.scoreboard.ScoreboardManager;

public class ScoreboardElementLevel extends ScoreboardElement {
	@Override
	public ArrayList<String> GetLines(ScoreboardManager manager, Player player) {
		final ArrayList<String> output = new ArrayList<>();

		int level = player.getLevel();
		float exp = player.getExp();
		int percent = (int) (exp * 100);

		StringBuilder bar = new StringBuilder(C.cGreen);
		int totalBars = 10;
		int filledBars = Math.round(exp * totalBars);

		for (int i = 0; i < totalBars; i++) {
			if (i == filledBars) {
				bar.append(C.cGray);
			}
			bar.append(i < filledBars ? "■" : "□");
		}

		output.add(level + " " + bar.toString() + " " + C.cWhite + percent + "%");
		return output;
	}
}
