package com.houzicore.shared.core.chat.command;

import org.bukkit.entity.Player;

import com.houzicore.shared.core.chat.Chat;
import com.houzicore.shared.core.command.CommandBase;
import com.houzicore.shared.common.Rank;
import com.houzicore.shared.common.util.F;
import com.houzicore.shared.common.util.UtilPlayer;

public class SilenceCommand extends CommandBase<Chat> {
	public SilenceCommand(Chat plugin) {
		super(plugin, Rank.ADMIN, "silence");
	}

	@Override
	public void Execute(Player caller, String[] args) {
		try {
			// Toggle
			if (args.length == 0) {
				// Disable
				if (Plugin.Silenced() != 0) {
					Plugin.Silence(0, true);
				}
				// Enable
				else {
					Plugin.Silence(-1, true);
				}
			}
			// Timer
			else {
				final long time = (long) (Double.valueOf(args[0]) * 3600000);

				Plugin.Silence(time, true);
			}
		} catch (final Exception e) {
			UtilPlayer.message(caller, F.main("Chat", "Invalid Time Parameter."));
		}
	}
}
