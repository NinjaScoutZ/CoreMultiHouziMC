package com.houzicore.shared.core.message.redis;

import com.houzicore.shared.common.util.C;
import com.houzicore.shared.common.util.F;
import com.houzicore.shared.common.util.UtilPlayer;
import com.houzicore.shared.common.util.UtilTextMiddle;
import com.houzicore.shared.serverdata.commands.AnnouncementCommand;
import com.houzicore.shared.serverdata.commands.CommandCallback;
import com.houzicore.shared.serverdata.commands.ServerCommand;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public class AnnouncementHandler implements CommandCallback {
	@Override
	public void run(ServerCommand command) {
		if (command instanceof AnnouncementCommand) {
			final AnnouncementCommand announcementCommand = (AnnouncementCommand) command;

			final String message = announcementCommand.getMessage();

			if (announcementCommand.getDisplayTitle()) {
				UtilTextMiddle.display(C.cYellow + "Announcement", message, 10, 120, 10);
			}

			for (final Player player : Bukkit.getOnlinePlayers()) {
				UtilPlayer.message(player, F.main("Announcement", C.cAqua + message));
			}
		}
	}
}
