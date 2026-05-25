package com.houzicore.shared.core.spawn.command;

import org.bukkit.entity.Player;

import com.houzicore.shared.core.command.MultiCommandBase;
import com.houzicore.shared.common.Rank;
import com.houzicore.shared.common.util.F;
import com.houzicore.shared.common.util.UtilPlayer;
import com.houzicore.shared.core.spawn.Spawn;

public class SpawnCommand extends MultiCommandBase<Spawn> {
	public SpawnCommand(Spawn plugin) {
		super(plugin, Rank.ADMIN, "spawn");

		AddCommand(new AddCommand(plugin));
		AddCommand(new ClearCommand(plugin));
	}

	@Override
	public void Help(Player caller, String[] args) {
		UtilPlayer.message(caller, F.main("Spawn", "Commands List:"));
		UtilPlayer.message(caller, F.help("/spawn add", com.houzicore.shared.core.lang.LangManager.get().get(caller, "spawn.add_help"), Rank.ADMIN));
		UtilPlayer.message(caller, F.help("/spawn clear", com.houzicore.shared.core.lang.LangManager.get().get(caller, "spawn.clear_help"), Rank.ADMIN));
	}
}
