package com.houzicore.shared.core.battlepass;

import org.bukkit.entity.Player;

import com.houzicore.shared.common.Rank;
import com.houzicore.shared.core.command.CommandBase;

public class BattlePassCommand extends CommandBase<BattlePassManager> {

	public BattlePassCommand(BattlePassManager plugin) {
		super(plugin, Rank.ALL, "battlepass", "season");
	}

	@Override
	public void Execute(Player caller, String[] args) {
		Plugin.openShop(caller);
	}
}
