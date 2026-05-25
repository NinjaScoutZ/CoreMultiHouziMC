package com.houzicore.shared.core.npc.command;

import java.sql.SQLException;

import org.bukkit.entity.Player;

import com.houzicore.shared.core.command.CommandBase;
import com.houzicore.shared.common.Rank;
import com.houzicore.shared.common.util.F;
import com.houzicore.shared.common.util.UtilPlayer;
import com.houzicore.shared.core.npc.NpcManager;

public class ClearCommand extends CommandBase<NpcManager> {
	public ClearCommand(NpcManager plugin) {
		super(plugin, Rank.DEVELOPER, "clear");
	}

	@Override
	public void Execute(Player caller, String[] args) {
		if (args != null) {
			Plugin.help(caller);
		} else {
			try {
				Plugin.clearNpcs(true);

				UtilPlayer.message(caller, F.main(Plugin.getName(), com.houzicore.shared.core.lang.LangManager.get().get(caller, "npc.cleared")));
			} catch (final SQLException e) {
				Plugin.help(caller, "Database error.");
			}
		}
	}
}
