package com.houzicore.shared.core.chat.command;

import org.bukkit.entity.Player;

import com.houzicore.shared.core.chat.Chat;
import com.houzicore.shared.core.command.CommandBase;
import com.houzicore.shared.common.Rank;
import com.houzicore.shared.common.util.F;
import com.houzicore.shared.common.util.UtilPlayer;

public class ChatSlowCommand extends CommandBase<Chat> {
	public ChatSlowCommand(Chat plugin) {
		super(plugin, Rank.SNR_MODERATOR, "chatslow");
	}

	@Override
	public void Execute(Player caller, String[] args) {
		if (args != null && args.length == 1) {
			try {
				final int seconds = Integer.parseInt(args[0]);

				if (seconds < 0) {
					UtilPlayer.message(caller, F.main("Chat", "Seconds must be a positive integer"));
					return;
				}

				Plugin.setChatSlow(seconds, true);
				UtilPlayer.message(caller, F.main("Chat", "Set chat slow to " + F.time(seconds + " seconds")));
			} catch (final Exception e) {
				showUsage(caller);
			}
		} else {
			showUsage(caller);
		}
	}

	private void showUsage(Player caller) {
		UtilPlayer.message(caller, F.main("Chat", "Usage: /chatslow <seconds>"));
	}
}
