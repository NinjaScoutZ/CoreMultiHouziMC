package com.houzicore.shared.core.message.commands;

import com.houzicore.shared.core.command.CommandBase;
import com.houzicore.shared.common.Rank;
import com.houzicore.shared.common.util.F;
import com.houzicore.shared.core.message.MessageManager;
import com.houzicore.shared.serverdata.commands.AnnouncementCommand;

import org.bukkit.entity.Player;

public class GlobalCommand extends CommandBase<MessageManager> {
	public GlobalCommand(MessageManager plugin) {
		super(plugin, Rank.ADMIN, "global");
	}

	@Override
	public void Execute(Player caller, String[] args) {
		if (args == null) {
			Plugin.Help(caller);
		} else {
			new AnnouncementCommand(false, F.combine(args, 0, null, false), 0L).publish();
		}
	}
}
