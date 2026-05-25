package com.houzicore.shared.core.creature.command;

import org.bukkit.entity.Player;

import com.houzicore.shared.core.command.CommandBase;
import com.houzicore.shared.common.Rank;
import com.houzicore.shared.common.util.F;
import com.houzicore.shared.common.util.UtilPlayer;
import com.houzicore.shared.core.creature.Creature;

public class HelpCommand extends CommandBase<Creature> {
	public HelpCommand(Creature plugin) {
		super(plugin, Rank.ADMIN, "help");
	}

	@Override
	public void Execute(Player caller, String[] args) {
		UtilPlayer.message(caller, F.main(Plugin.getName(), "Commands List;"));
		UtilPlayer.message(caller, F.help("/mob", "List Entities", Rank.MODERATOR));
		UtilPlayer.message(caller, F.help("/mob kill <Type>", "Remove Entities of Type", Rank.ADMIN));
		UtilPlayer.message(caller, F.help("/mob <Type> (# baby lock angry s# <Prof>)", "Create", Rank.ADMIN));
		UtilPlayer.message(caller, F.desc("Professions", "Butcher, Blacksmith, Farmer, Librarian, Priest"));
	}
}
