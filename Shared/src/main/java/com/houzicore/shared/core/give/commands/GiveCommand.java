package com.houzicore.shared.core.give.commands;

import org.bukkit.entity.Player;

import com.houzicore.shared.core.command.CommandBase;
import com.houzicore.shared.common.Rank;
import com.houzicore.shared.core.give.Give;

public class GiveCommand extends CommandBase<Give> {
	public GiveCommand(Give plugin) {
		super(plugin, Rank.ADMIN, "give", "g", "item", "i");
	}

	@Override
	public void Execute(final Player caller, final String[] args) {
		Plugin.parseInput(caller, args);
	}
}
