package com.houzicore.shared.core.report.command;

import com.houzicore.shared.core.command.CommandBase;
import com.houzicore.shared.common.Rank;
import com.houzicore.shared.common.util.C;
import com.houzicore.shared.common.util.F;
import com.houzicore.shared.common.util.UtilPlayer;
import com.houzicore.shared.core.report.ReportManager;
import com.houzicore.shared.core.report.ReportPlugin;

import org.bukkit.entity.Player;

public class ReportCommand extends CommandBase<ReportPlugin> {

	public ReportCommand(ReportPlugin plugin) {
		super(plugin, Rank.ALL, "report");
	}

	@Override
	public void Execute(final Player player, final String[] args) {
		if (args == null || args.length < 2) {
			UtilPlayer.message(player,
					F.main(Plugin.getName(), C.cRed + "Your arguments are inappropriate for this command!"));
			return;
		} else {
			final String playerName = args[0];
			final Player reportedPlayer = UtilPlayer.searchOnline(player, playerName, false);
			final String reason = F.combine(args, 1, null, false);

			if (reportedPlayer != null) {
				ReportManager.getInstance().reportPlayer(player, reportedPlayer, reason);
			} else {
				UtilPlayer.message(player,
						F.main(Plugin.getName(), C.cRed + "Unable to find player '" + playerName + "'!"));
			}
		}
	}
}
