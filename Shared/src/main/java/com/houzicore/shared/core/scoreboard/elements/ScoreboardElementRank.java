package com.houzicore.shared.core.scoreboard.elements;

import java.util.ArrayList;

import org.bukkit.entity.Player;

import com.houzicore.shared.common.Rank;
import com.houzicore.shared.core.scoreboard.ScoreboardManager;

public class ScoreboardElementRank extends ScoreboardElement {
	@Override
	public ArrayList<String> GetLines(ScoreboardManager manager, Player player) {
		final ArrayList<String> output = new ArrayList<>();

		if (manager.getClients().Get(player).GetRank().Has(Rank.WARRIOR)) {
			output.add(manager.getClients().Get(player).GetRank().Name);
		} else if (manager.getDonation().Get(player.getName()).OwnsUnknownPackage("SuperSmashMobs ULTRA")
				|| manager.getDonation().Get(player.getName()).OwnsUnknownPackage("Survival Primal Game ULTRA")
				|| manager.getDonation().Get(player.getName()).OwnsUnknownPackage("Minigames ULTRA")
				|| manager.getDonation().Get(player.getName()).OwnsUnknownPackage("CastleSiege ULTRA")
				|| manager.getDonation().Get(player.getName()).OwnsUnknownPackage("Champions ULTRA")) {
			output.add("จอมยุทธ (เฉพาะเกม)");
		} else {
			output.add("ผู้มาใหม่");
		}

		return output;
	}

}
