package com.houzicore.lobby.hub.commands;

import org.bukkit.entity.Player;

import com.houzicore.shared.core.command.CommandBase;
import com.houzicore.shared.common.Rank;
import com.houzicore.lobby.hub.modules.AdminPunchManager;

public class AdminPunchCommand extends CommandBase<AdminPunchManager> {

	public AdminPunchCommand(AdminPunchManager plugin) {
		super(plugin, Rank.ADMIN, new String[] { "mystery", "rocketpunch" });
	}

	@Override
	public void Execute(Player caller, String[] args) {
		Plugin.togglePunch(caller);
	}
}
