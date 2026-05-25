package com.houzicore.shared.core.message.commands;

import org.bukkit.entity.Player;

import com.houzicore.shared.core.command.CommandBase;
import com.houzicore.shared.common.Rank;
import com.houzicore.shared.common.util.F;
import com.houzicore.shared.common.util.UtilPlayer;
import com.houzicore.shared.core.message.MessageManager;

public class ResendCommand extends CommandBase<MessageManager> {
	public ResendCommand(MessageManager plugin) {
		super(plugin, Rank.ALL, "r");
	}

	@Override
	public void Execute(Player caller, String[] args) {
		if (args == null) {
			Plugin.Help(caller);
		} else {
			final String lastTo = Plugin.Get(caller).LastTo;

			// Get To
			if (lastTo == null) {
				UtilPlayer.message(caller, F.main(Plugin.getName(), com.houzicore.shared.core.lang.LangManager.get().get(caller, "message.no_reply")));
				return;
			}

			// Parse Message
			String message = "Beep!";
			if (args.length > 0) {
				message = F.combine(args, 0, null, false);
			} else {
				message = Plugin.GetRandomMessage();
			}

			Plugin.sendMessage(caller, lastTo, message, true, false);
		}
	}
}
