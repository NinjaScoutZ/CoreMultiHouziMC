package com.houzicore.shared.core.scoreboard.elements;

import java.util.ArrayList;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import com.houzicore.shared.common.util.C;
import com.houzicore.shared.core.scoreboard.ScoreboardManager;

public class ScoreboardElementOnline extends ScoreboardElement {
	@Override
	public ArrayList<String> GetLines(ScoreboardManager manager, Player player) {
		final ArrayList<String> output = new ArrayList<>();
		output.add(C.cWhite + String.valueOf(Bukkit.getOnlinePlayers().size()));
		return output;
	}
}
