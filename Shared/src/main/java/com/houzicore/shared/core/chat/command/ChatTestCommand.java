package com.houzicore.shared.core.chat.command;

import org.bukkit.entity.Player;

import com.houzicore.shared.common.Rank;
import com.houzicore.shared.common.util.HouziColorParser;
import com.houzicore.shared.common.util.UtilServer;
import com.houzicore.shared.core.chat.Chat;
import com.houzicore.shared.core.command.CommandBase;

public class ChatTestCommand extends CommandBase<Chat> {
	public ChatTestCommand(Chat plugin) {
		super(plugin, Rank.ADMIN, "chattest");
	}

	@Override
	public void Execute(Player caller, String[] args) {
		if (args.length == 0) {
			caller.sendMessage("§cUsage: /chattest <message...>");
			return;
		}

		String testMessage = String.join(" ", args);
		String parsed = HouziColorParser.parse(testMessage);

		caller.sendMessage("§6§lFormat Test:\n§r" + parsed);
	}
}
