package com.houzicore.shared.core.message.commands;

import org.bukkit.Sound;
import org.bukkit.entity.Player;

import com.houzicore.shared.core.command.CommandBase;
import com.houzicore.shared.common.Rank;
import com.houzicore.shared.common.util.C;
import com.houzicore.shared.common.util.F;
import com.houzicore.shared.common.util.UtilPlayer;
import com.houzicore.shared.common.util.UtilServer;
import com.houzicore.shared.core.message.MessageManager;

public class AdminCommand extends CommandBase<MessageManager> {
	public AdminCommand(MessageManager plugin) {
		super(plugin, Rank.ALL, "a", "admin");
	}

	@Override
	public void Execute(Player caller, String[] args) {
		if (args == null) {
			Plugin.Help(caller);
		} else {
			if (args.length == 0) {
				UtilPlayer.message(caller, F.main(Plugin.getName(), com.houzicore.shared.core.lang.LangManager.get().get(caller, "message.missing_msg_arg")));
				return;
			}

			if (Plugin.isMuted(caller))
				return;

			// Parse Message
			final String message = F.combine(args, 0, null, false);

			// Inform
			UtilPlayer.message(caller, F.rank(Plugin.GetClientManager().Get(caller).GetRank()) + " " + caller.getName()
					+ " " + C.cPurple + message);

			// Send
			boolean staff = false;
			for (final Player to : UtilServer.getPlayers()) {
				if (Plugin.GetClientManager().Get(to).GetRank().Has(Rank.HELPER)) {
					if (!to.equals(caller)) {
						UtilPlayer.message(to, F.rank(Plugin.GetClientManager().Get(caller).GetRank()) + " "
								+ caller.getName() + " " + C.cPurple + message);
					}

					staff = true;

					// Sound
					to.playSound(to.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 0.5f, 2f);
				}
			}

			if (!staff) {
				UtilPlayer.message(caller, F.main(Plugin.getName(), com.houzicore.shared.core.lang.LangManager.get().get(caller, "message.no_staff")));
			}

			// Log XXX
			// Logger().logChat("Staff Chat", from, staff, message);
		}
	}
}
