package com.houzicore.shared.core.npc.command;

import org.bukkit.entity.Player;

import com.houzicore.shared.core.command.CommandBase;
import com.houzicore.shared.common.Rank;
import com.houzicore.shared.common.util.F;
import com.houzicore.shared.common.util.UtilPlayer;
import com.houzicore.shared.core.npc.NpcManager;

public class DeleteCommand extends CommandBase<NpcManager> {
	public DeleteCommand(NpcManager plugin) {
		super(plugin, Rank.DEVELOPER, "del");
	}

	@Override
	public void Execute(Player caller, String[] args) {
		if (args != null) {
			Plugin.help(caller);
		} else {
			Plugin.prepDeleteNpc(caller);

			UtilPlayer.message(caller, F.main(Plugin.getName(), com.houzicore.shared.core.lang.LangManager.get().get(caller, "npc.right_click_del")));
		}
	}
}
