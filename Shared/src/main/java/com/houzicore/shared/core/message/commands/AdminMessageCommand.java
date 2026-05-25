package com.houzicore.shared.core.message.commands;

import org.bukkit.entity.Player;

import com.houzicore.shared.core.command.CommandBase;
import com.houzicore.shared.common.Rank;
import com.houzicore.shared.common.util.F;
import com.houzicore.shared.common.util.UtilPlayer;
import com.houzicore.shared.core.message.MessageManager;

public class AdminMessageCommand extends CommandBase<MessageManager> {
	public AdminMessageCommand(MessageManager plugin) {
		super(plugin, Rank.SNR_MODERATOR, "am");
	}

	@Override
	public void Execute(Player caller, String[] args) {
		if (args == null) {
			Plugin.Help(caller);
		} else {
			if (args.length == 0) {
				UtilPlayer.message(caller, F.main(Plugin.getName(), com.houzicore.shared.core.lang.LangManager.get().get(caller, "message.missing_arg")));
				return;
			}

			// Parse Message
			String message = "Beep!";
			if (args.length > 1) {
				message = F.combine(args, 1, null, false);
			} else {
				message = Plugin.GetRandomMessage();
			}

			Plugin.sendMessage(caller, args[0], message, false, true);
		}
	}
}
