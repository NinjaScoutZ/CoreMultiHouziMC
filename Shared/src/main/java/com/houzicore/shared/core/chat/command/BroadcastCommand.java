package com.houzicore.shared.core.chat.command;

import org.bukkit.entity.Player;

import com.houzicore.shared.core.chat.Chat;
import com.houzicore.shared.core.command.CommandBase;
import com.houzicore.shared.common.Rank;
import com.houzicore.shared.common.util.UtilServer;

public class BroadcastCommand extends CommandBase<Chat> {
	public BroadcastCommand(Chat plugin) {
		super(plugin, Rank.MODERATOR, "s");
	}

	@Override
	public void Execute(Player caller, String[] args) {
		String announcement = "";

		for (final String arg : args) {
			announcement += arg + " ";
		}

		if (announcement.length() > 0) {
			announcement = announcement.substring(0, announcement.length() - 1);
		}

		UtilServer.broadcast(caller.getName(), announcement);
	}
}
