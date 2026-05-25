package com.houzicore.shared.core.scoreboard.elements;

import java.util.ArrayList;

import com.houzicore.shared.core.scoreboard.ScoreboardFormatUtil;
import com.houzicore.shared.core.scoreboard.ScoreboardManager;

import org.bukkit.entity.Player;

public class ScoreboardElementDate extends ScoreboardElement {
	@Override
	public ArrayList<String> GetLines(ScoreboardManager manager, Player player) {
		final ArrayList<String> output = new ArrayList<>();
		output.add(com.houzicore.shared.common.util.C.cGray + ScoreboardFormatUtil.currentDate());
		return output;
	}
}
