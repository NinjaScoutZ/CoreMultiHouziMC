package com.houzicore.shared.core.scoreboard.elements;

import java.util.ArrayList;

import com.houzicore.shared.common.CurrencyType;
import com.houzicore.shared.core.scoreboard.ScoreboardManager;

import org.bukkit.entity.Player;

import com.houzicore.shared.core.scoreboard.ScoreboardFormatUtil;

public class ScoreboardElementEssenceCount extends ScoreboardElement {
	@Override
	public ArrayList<String> GetLines(ScoreboardManager manager, Player player) {
		final ArrayList<String> output = new ArrayList<>();
		output.add(ScoreboardFormatUtil.formatCurrency(manager.getDonation().Get(player).GetBalance(CurrencyType.Essence)));
		return output;
	}
}
