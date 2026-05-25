package com.houzicore.shared.core.scoreboard.elements;

import java.util.ArrayList;
import org.bukkit.entity.Player;
import com.houzicore.shared.core.scoreboard.ScoreboardManager;
import com.houzicore.shared.common.util.C;

public class ScoreboardElementPlayerName extends ScoreboardElement {
	@Override
	public ArrayList<String> GetLines(ScoreboardManager manager, Player player) {
		ArrayList<String> output = new ArrayList<>();
		output.add(C.cGray + player.getName());
		return output;
	}
}
