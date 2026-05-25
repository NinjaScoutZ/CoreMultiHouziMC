package com.houzicore.shared.core.personalServer;

import org.bukkit.entity.Player;

import com.houzicore.shared.core.command.CommandBase;
import com.houzicore.shared.common.Rank;
import com.houzicore.shared.recharge.Recharge;

public class HostEventServerCommand extends CommandBase<PersonalServerManager> {
	public HostEventServerCommand(PersonalServerManager plugin) {
		super(plugin, Rank.ADMIN, "hostevent");
	}

	@Override
	public void Execute(Player caller, String[] args) {
		if (!Recharge.Instance.use(caller, "Host Event", 30000, false, false))
			return;

		Plugin.hostServer(caller, caller.getName(), true);
	}
}
