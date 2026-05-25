package com.houzicore.shared.core.teleport.command;

import org.bukkit.entity.Player;

import com.houzicore.shared.core.command.CommandBase;
import com.houzicore.shared.common.Rank;
import com.houzicore.shared.core.teleport.Teleport;

public class AllCommand extends CommandBase<Teleport> {
	public AllCommand(Teleport plugin) {
		super(plugin, Rank.OWNER, "all");
	}

	@Override
	public void Execute(Player caller, String[] args) {
		Plugin.playerToPlayer(caller, "%ALL%", caller.getName());
	}
}
