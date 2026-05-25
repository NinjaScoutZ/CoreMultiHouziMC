package com.houzicore.shared.core.quest;

import org.bukkit.entity.Player;

import com.houzicore.shared.common.Rank;
import com.houzicore.shared.core.command.CommandBase;

public class QuestCommand extends CommandBase<QuestManager> {

	public QuestCommand(QuestManager plugin) {
		super(plugin, Rank.ALL, "quests", "quest");
	}

	@Override
	public void Execute(Player caller, String[] args) {
		Plugin.openShop(caller);
	}
}
