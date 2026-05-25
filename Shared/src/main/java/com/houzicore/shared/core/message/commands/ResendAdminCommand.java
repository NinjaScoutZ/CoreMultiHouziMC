package com.houzicore.shared.core.message.commands;

import org.bukkit.entity.Player;

import com.houzicore.shared.core.command.CommandBase;
import com.houzicore.shared.common.Rank;
import com.houzicore.shared.common.util.F;
import com.houzicore.shared.common.util.UtilPlayer;
import com.houzicore.shared.core.message.MessageManager;

public class ResendAdminCommand extends CommandBase<MessageManager> {
	public ResendAdminCommand(MessageManager plugin) {
		super(plugin, Rank.ALL, "ra");
	}

	@Override
	public void Execute(Player caller, String[] args) {
		if (args == null) {
			Plugin.Help(caller);
		} else {
			if (!Plugin.GetClientManager().Get(caller).GetRank().Has(caller, Rank.HELPER, true))
				return;

			final String lastTo = Plugin.Get(caller).LastAdminTo;

			// Get To
			if (lastTo == null) {
				UtilPlayer.message(caller, F.main(Plugin.getName(), com.houzicore.shared.core.lang.LangManager.get().get(caller, "message.no_admin_reply")));
				return;
			}

			// Parse Message
			String message = "Beep!";
			if (args.length > 0) {
				message = F.combine(args, 0, null, false);
			} else {
				message = Plugin.GetRandomMessage();
			}

			Plugin.sendMessage(caller, lastTo, message, true, true);
		}
	}
}
