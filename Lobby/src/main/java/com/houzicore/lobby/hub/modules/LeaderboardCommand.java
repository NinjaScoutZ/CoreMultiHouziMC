package com.houzicore.lobby.hub.modules;

import org.bukkit.entity.Player;

import com.houzicore.shared.core.command.CommandBase;
import com.houzicore.shared.common.Rank;
import com.houzicore.shared.common.util.F;
import com.houzicore.shared.common.util.UtilPlayer;

public class LeaderboardCommand extends CommandBase<LeaderboardManager>
{
	public LeaderboardCommand(LeaderboardManager plugin)
	{
		super(plugin, Rank.ADMIN, "leaderboard", "lb");
	}

	@Override
	public void Execute(Player caller, String[] args)
	{
		if (args == null || args.length == 0)
		{
			UtilPlayer.message(caller, F.main("Leaderboard", "Commands List:"));
			UtilPlayer.message(caller, F.help("/lb create <StatName>", "Spawn a leaderboard", Rank.ADMIN));
			UtilPlayer.message(caller, F.help("/lb delete", "Delete nearest leaderboard", Rank.ADMIN));
			return;
		}

		if (args[0].equalsIgnoreCase("create"))
		{
			if (args.length < 2)
			{
				UtilPlayer.message(caller, F.main("Leaderboard", "Missing StatName. Example: /lb create Skywars.Wins"));
				return;
			}
			
			String statName = args[1];
			if (Plugin.addLeaderboard(caller.getLocation(), statName))
			{
				UtilPlayer.message(caller, F.main("Leaderboard", "Successfully spawned leaderboard for " + F.elem(statName) + "."));
			}
			else
			{
				UtilPlayer.message(caller, F.main("Leaderboard", "A leaderboard already exists at this exact block."));
			}
		}
		else if (args[0].equalsIgnoreCase("delete") || args[0].equalsIgnoreCase("remove"))
		{
			if (Plugin.removeNearestLeaderboard(caller.getLocation()))
			{
				UtilPlayer.message(caller, F.main("Leaderboard", "Successfully deleted nearest leaderboard."));
			}
			else
			{
				UtilPlayer.message(caller, F.main("Leaderboard", "No leaderboard found within 5 blocks."));
			}
		}
		else
		{
			UtilPlayer.message(caller, F.main("Leaderboard", "Unknown argument."));
		}
	}
}
