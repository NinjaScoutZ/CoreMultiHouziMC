package com.houzicore.shared.core.npc.command;

import org.bukkit.entity.Player;

import com.houzicore.shared.core.command.CommandBase;
import com.houzicore.shared.common.Rank;
import com.houzicore.shared.common.util.C;
import com.houzicore.shared.common.util.F;
import com.houzicore.shared.common.util.UtilInv;
import com.houzicore.shared.common.util.UtilPlayer;
import com.houzicore.shared.core.npc.NpcManager;

public class AdminBuilderCommand extends CommandBase<NpcManager> {
	public AdminBuilderCommand(NpcManager plugin) {
		super(plugin, Rank.ADMIN, "adminbuilder", "builder");
	}

	@Override
	public void Execute(Player caller, String[] args) {
		boolean state = Plugin.toggleAdminBuilder(caller);
		if (state) {
			// Clear lobby items so they don't block creative inventory
			UtilInv.Clear(caller);
			caller.setGameMode(org.bukkit.GameMode.CREATIVE);
			caller.setOp(true);
			UtilPlayer.message(caller, F.main(Plugin.getName(), com.houzicore.shared.core.lang.LangManager.get().get(caller, "npc.builder_enable")));
			UtilPlayer.message(caller, F.main(Plugin.getName(), com.houzicore.shared.core.lang.LangManager.get().get(caller, "npc.builder_op")));
		} else {
			caller.setGameMode(org.bukkit.GameMode.SURVIVAL);
			caller.setOp(false);
			// Clear any creative items from builder session
			UtilInv.Clear(caller);
			UtilPlayer.message(caller, F.main(Plugin.getName(), com.houzicore.shared.core.lang.LangManager.get().get(caller, "npc.builder_disable")));
			UtilPlayer.message(caller, F.main(Plugin.getName(), com.houzicore.shared.core.lang.LangManager.get().get(caller, "npc.builder_revoke")));
			UtilPlayer.message(caller, F.main(Plugin.getName(), com.houzicore.shared.core.lang.LangManager.get().get(caller, "npc.builder_rejoin")));
		}
	}
}
