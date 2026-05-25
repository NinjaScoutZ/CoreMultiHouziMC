package com.houzicore.shared.core.teleport.command;

import org.bukkit.entity.Player;

import com.houzicore.shared.core.command.CommandBase;
import com.houzicore.shared.common.Rank;
import com.houzicore.shared.common.util.F;
import com.houzicore.shared.common.util.UtilPlayer;
import com.houzicore.shared.core.teleport.Teleport;

public class LocateCommand extends CommandBase<Teleport> {
	public LocateCommand(Teleport plugin) {
		super(plugin, Rank.MODERATOR, "locate", "where", "find");
	}

	@Override
	public void Execute(Player caller, String[] args) {
		if (args == null || args.length == 0) {
			UtilPlayer.message(caller, F.main("Locate", com.houzicore.shared.core.lang.LangManager.get().get(caller, "locate.missing_arg")));
			return;
		}

		Plugin.locatePlayer(caller, args[0]);
	}
}
