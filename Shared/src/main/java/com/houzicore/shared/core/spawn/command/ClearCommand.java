package com.houzicore.shared.core.spawn.command;

import org.bukkit.entity.Player;

import com.houzicore.shared.core.command.CommandBase;
import com.houzicore.shared.common.Rank;
import com.houzicore.shared.core.spawn.Spawn;

public class ClearCommand extends CommandBase<Spawn> {
	public ClearCommand(Spawn plugin) {
		super(plugin, Rank.ADMIN, "clear");
	}

	@Override
	public void Execute(Player caller, String[] args) {
		Plugin.ClearSpawn(caller);
	}
}
