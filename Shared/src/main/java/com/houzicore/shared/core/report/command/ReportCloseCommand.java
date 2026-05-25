package com.houzicore.shared.core.report.command;

import com.houzicore.shared.core.command.CommandBase;
import com.houzicore.shared.common.Rank;
import com.houzicore.shared.common.util.C;
import com.houzicore.shared.common.util.F;
import com.houzicore.shared.common.util.UtilPlayer;
import com.houzicore.shared.core.report.ReportManager;
import com.houzicore.shared.core.report.ReportPlugin;

import org.bukkit.entity.Player;

public class ReportCloseCommand extends CommandBase<ReportPlugin> {

	public ReportCloseCommand(ReportPlugin plugin) {
		super(plugin, Rank.ADMIN, "reportclose", "rc");
	}

	@Override
	public void Execute(final Player player, final String[] args) {
		if (args == null || args.length < 2) {
			UtilPlayer.message(player,
					F.main(Plugin.getName(), C.cRed + "Your arguments are inappropriate for this command!"));
			return;
		} else {
			final int reportId = Integer.parseInt(args[0]);
			final String reason = F.combine(args, 1, null, false);

			ReportManager.getInstance().closeReport(reportId, player, reason);
		}
	}
}
