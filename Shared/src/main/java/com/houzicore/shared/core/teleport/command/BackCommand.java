package com.houzicore.shared.core.teleport.command;

import org.bukkit.Location;
import org.bukkit.entity.Player;

import com.houzicore.shared.core.command.CommandBase;
import com.houzicore.shared.common.Rank;
import com.houzicore.shared.common.util.F;
import com.houzicore.shared.common.util.UtilPlayer;
import com.houzicore.shared.core.teleport.Teleport;

public class BackCommand extends CommandBase<Teleport> {
	public BackCommand(Teleport plugin) {
		super(plugin, Rank.MODERATOR, "back", "b");
	}

	private void Back(Player caller, String target, String amountString) {
		int amount = 1;
		try {
			amount = Integer.parseInt(amountString);
		} catch (final Exception e) {
			UtilPlayer.message(caller, F.main("Teleport", com.houzicore.shared.core.lang.LangManager.get().get(caller, "teleport.invalid_amt").replace("{0}", amountString)));
		}

		final Player player = UtilPlayer.searchOnline(caller, target, true);

		if (player == null)
			return;

		Location loc = null;
		int back = 0;
		for (int i = 0; i < amount; i++) {
			if (Plugin.GetTPHistory(player).isEmpty()) {
				break;
			}

			loc = Plugin.GetTPHistory(player).removeFirst();
			back++;
		}

		if (loc == null) {
			UtilPlayer.message(caller, F.main("Teleport", com.houzicore.shared.core.lang.LangManager.get().get(caller, "teleport.no_history").replace("{0}", player.getName())));
			return;
		}

		// Register
		final String mA = F.main("Teleport",
				F.elem(caller.getName()) + " undid your last " + F.count("" + back) + " teleport(s).");
		final String mB = F.main("Teleport",
				"You undid the last " + F.count("" + back) + " teleport(s) for " + F.elem(player.getName()) + ".");
		Plugin.Add(player, loc, mA, false, caller, mB,
				"Undid last " + back + " teleports for " + player.getName() + " via " + caller.getName());
	}

	@Override
	public void Execute(Player caller, String[] args) {
		if (args.length == 0) {
			Back(caller, caller.getName(), "1");
		} else if (args.length == 1) {
			Back(caller, args[0], "1");
		} else {
			Back(caller, args[0], args[1]);
		}
	}
}
