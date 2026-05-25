package com.houzicore.shared.core.spawn.command;

import org.bukkit.entity.Player;

import com.houzicore.shared.core.command.CommandBase;
import com.houzicore.shared.common.Rank;
import com.houzicore.shared.core.spawn.Spawn;

public class AddCommand extends CommandBase<Spawn> {
	public AddCommand(Spawn plugin) {
		super(plugin, Rank.ADMIN, "add", "a");
	}

	@Override
	public void Execute(Player caller, String[] args) {
		Plugin.AddSpawn(caller);
	}
}
