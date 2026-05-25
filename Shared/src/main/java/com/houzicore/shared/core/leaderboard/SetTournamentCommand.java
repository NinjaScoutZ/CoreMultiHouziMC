package com.houzicore.shared.core.leaderboard;

import com.houzicore.shared.core.command.CommandBase;
import com.houzicore.shared.common.Rank;

import org.bukkit.entity.Player;

public class SetTournamentCommand extends CommandBase<LeaderboardManager> {
	public SetTournamentCommand(LeaderboardManager plugin) {
		super(plugin, Rank.ADMIN, "settournament", "set-tournament");
	}

	@Override
	public void Execute(Player caller, String[] args) {
		if (args.length != 3) {
			com.houzicore.shared.common.util.UtilPlayer.message(caller, com.houzicore.shared.common.util.F.main("Command", "Usage: /settournament <StatType> <GameModeID> <Value>"));
			return;
		}

		try {
			String statType = args[0];
			int gamemode = Integer.parseInt(args[1]);
			int value = Integer.parseInt(args[2]);
			
			boolean success = LeaderboardManager.getInstance().attemptStatEvent(caller, statType, gamemode, value);
			if (success) {
				com.houzicore.shared.common.util.UtilPlayer.message(caller, com.houzicore.shared.common.util.F.main("Leaderboard", "Recorded tournament stat event."));
			} else {
				com.houzicore.shared.common.util.UtilPlayer.message(caller, com.houzicore.shared.common.util.F.main("Leaderboard", "Failed to record stat event. Invalid StatType?"));
			}
		} catch (NumberFormatException e) {
			com.houzicore.shared.common.util.UtilPlayer.message(caller, com.houzicore.shared.common.util.F.main("Command", "Invalid number format for GameModeID or Value."));
		}
	}
}
