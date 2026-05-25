package com.houzicore.shared.core.command;

import java.util.Collection;
import java.util.List;

import com.houzicore.shared.common.Rank;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public interface ICommand {
	Collection<String> Aliases();

	void Execute(Player caller, String[] args);

	Rank GetRequiredRank();

	Rank[] GetSpecificRanks();

	String GetDescription();

	String GetUsage();

	boolean AllowConsole();

	List<String> onTabComplete(CommandSender sender, String commandLabel, String[] args);

	void SetAliasUsed(String name);

	void SetCommandCenter(CommandCenter commandCenter);
}
