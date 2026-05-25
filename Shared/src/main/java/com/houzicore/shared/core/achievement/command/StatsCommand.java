package com.houzicore.shared.core.achievement.command;

import org.bukkit.entity.Player;

import com.houzicore.shared.core.achievement.AchievementManager;
import com.houzicore.shared.core.command.CommandBase;
import com.houzicore.shared.common.Rank;
import com.houzicore.shared.common.util.UtilPlayer;
import com.houzicore.shared.command.CommandMeta;

@CommandMeta(
	description = "View your personal statistics and achievements.",
	usage = "/stats",
	aliases = {"stats"},
	permission = Rank.ALL
)
public class StatsCommand extends CommandBase<AchievementManager> {
	public StatsCommand(AchievementManager plugin) {
		super(plugin, Rank.ALL);
	}

	@Override
	public void Execute(Player caller, String[] args) {
		if (args == null || args.length == 0) {
			Plugin.openShop(caller);
		} else {
			final Player target = UtilPlayer.searchOnline(caller, args[0], true);

			if (target == null)
				return;

			Plugin.openShop(caller, target);
		}
	}
}
