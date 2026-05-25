package com.houzicore.shared.core.teleport.command;

import org.bukkit.entity.Player;

import com.houzicore.shared.core.command.MultiCommandBase;
import com.houzicore.shared.common.Rank;
import com.houzicore.shared.common.util.F;
import com.houzicore.shared.common.util.UtilPlayer;
import com.houzicore.shared.core.teleport.Teleport;

public class TeleportCommand extends MultiCommandBase<Teleport> {
	public TeleportCommand(Teleport plugin) {
		super(plugin, Rank.MODERATOR, "tp", "teleport");

		AddCommand(new AllCommand(plugin));
		AddCommand(new BackCommand(plugin));
		AddCommand(new HereCommand(plugin));
	}

	@Override
	protected void Help(Player caller, String[] args) {
		// Caller to Player
		if (args.length == 1
				&& CommandCenter.GetClientManager().Get(caller).GetRank().Has(caller, Rank.MODERATOR, true)) {
			Plugin.playerToPlayer(caller, caller.getName(), args[0]);
		} else if (args.length == 2
				&& CommandCenter.GetClientManager().Get(caller).GetRank().Has(caller, Rank.ADMIN, true)) {
			Plugin.playerToPlayer(caller, args[0], args[1]);
		} else if (args.length == 3
				&& CommandCenter.GetClientManager().Get(caller).GetRank().Has(caller, Rank.ADMIN, true)) {
			Plugin.playerToLoc(caller, caller.getName(), args[0], args[1], args[2]);
		} else if (args.length == 5) {
			Plugin.playerToLoc(caller, args[0], args[1], args[2], args[3], args[4]);
		} else if (args.length == 4
				&& CommandCenter.GetClientManager().Get(caller).GetRank().Has(caller, Rank.ADMIN, true)) {
			Plugin.playerToLoc(caller, args[0], args[1], args[2], args[3]);
		} else {
			UtilPlayer.message(caller, F.main(Plugin.getName(), "Commands List:"));
			UtilPlayer.message(caller, F.help("/tp <target>", com.houzicore.shared.core.lang.LangManager.get().get(caller, "teleport.tp_to_player"), Rank.MODERATOR));
			UtilPlayer.message(caller, F.help("/tp b(ack) (amount) (player)", com.houzicore.shared.core.lang.LangManager.get().get(caller, "teleport.undo_tp"), Rank.MODERATOR));
			UtilPlayer.message(caller, F.help("/tp here <player>", com.houzicore.shared.core.lang.LangManager.get().get(caller, "teleport.tp_to_self"), Rank.ADMIN));
			UtilPlayer.message(caller, F.help("/tp <player> <target>", com.houzicore.shared.core.lang.LangManager.get().get(caller, "teleport.tp_player_to_player"), Rank.ADMIN));
			UtilPlayer.message(caller, F.help("/tp <X> <Y> <Z>", com.houzicore.shared.core.lang.LangManager.get().get(caller, "teleport.tp_to_loc"), Rank.ADMIN));
			UtilPlayer.message(caller, F.help("/tp all", com.houzicore.shared.core.lang.LangManager.get().get(caller, "teleport.tp_all"), Rank.OWNER));
		}
	}
}
