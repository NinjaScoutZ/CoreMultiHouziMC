package com.houzicore.shared.core.clan.command;

import org.bukkit.entity.Player;

import com.houzicore.shared.core.command.CommandBase;
import com.houzicore.shared.common.Rank;
import com.houzicore.shared.common.util.F;
import com.houzicore.shared.common.util.UtilPlayer;
import com.houzicore.shared.core.clan.ClanManager;

public class ClanCommand extends CommandBase<ClanManager> {

	public ClanCommand(ClanManager plugin) {
		super(plugin, Rank.ALL, new String[] { "clan", "guild", "c" });
	}

	@Override
	public void Execute(Player caller, String[] args) {
		if (args == null || args.length == 0) {
			UtilPlayer.message(caller, F.main("Clan", "§6Clan Commands:"));
			UtilPlayer.message(caller, F.value(0, "/clan create <Name>", "Create a new clan (Requires DIVINE)"));
			UtilPlayer.message(caller, F.value(0, "/clan join <Name>", "Join an existing clan"));
			UtilPlayer.message(caller, F.value(0, "/clan leave", "Leave your current clan"));
			return;
		}

		if (args[0].equalsIgnoreCase("create")) {
			if (args.length < 2) {
				UtilPlayer.message(caller, F.main("Clan", "§cUsage: /clan create <Name>"));
				return;
			}
			Plugin.createClan(caller, args[1]);
		} else if (args[0].equalsIgnoreCase("join")) {
			if (args.length < 2) {
				UtilPlayer.message(caller, F.main("Clan", "§cUsage: /clan join <Name>"));
				return;
			}
			Plugin.joinClan(caller, args[1]);
		} else if (args[0].equalsIgnoreCase("leave") || args[0].equalsIgnoreCase("disband")) {
			Plugin.leaveClan(caller);
		} else {
			// If not a specific subcommand, treat it as a chat message!
			StringBuilder message = new StringBuilder();
			for (String arg : args) {
				message.append(arg).append(" ");
			}
			Plugin.sendClanChat(caller, message.toString().trim());
		}
	}
}
