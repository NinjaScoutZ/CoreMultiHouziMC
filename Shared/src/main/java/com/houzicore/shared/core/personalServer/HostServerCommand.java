package com.houzicore.shared.core.personalServer;

import org.bukkit.entity.Player;

import com.houzicore.shared.core.command.CommandBase;
import com.houzicore.shared.common.Rank;
import com.houzicore.shared.recharge.Recharge;

public class HostServerCommand extends CommandBase<PersonalServerManager> {
	public HostServerCommand(PersonalServerManager plugin) {
		super(plugin, Rank.DIVINE, "hostserver");
	}

	@Override
	public void Execute(Player caller, String[] args) {
		if (!Recharge.Instance.use(caller, "Host Server", 30000, false, false))
			return;

		Plugin.hostServer(caller, caller.getName(), false);
	}
}
