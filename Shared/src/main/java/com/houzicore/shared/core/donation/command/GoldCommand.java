package com.houzicore.shared.core.donation.command;

import com.houzicore.shared.account.CoreClient;
import com.houzicore.shared.core.command.CommandBase;
import com.houzicore.shared.common.Rank;
import com.houzicore.shared.common.util.Callback;
import com.houzicore.shared.common.util.F;
import com.houzicore.shared.common.util.UtilPlayer;
import com.houzicore.shared.core.donation.DonationManager;

import org.bukkit.entity.Player;

public class GoldCommand extends CommandBase<DonationManager> {
	public GoldCommand(DonationManager plugin) {
		super(plugin, Rank.ADMIN, "givegold");
	}

	@Override
	public void Execute(final Player caller, String[] args) {
		if (args == null || args.length == 0) {
			UtilPlayer.message(caller, F.main("Gold", "Your Gold: " + F.elem("" + Plugin.Get(caller).getGold())));
		} else if (args.length < 2) {
			UtilPlayer.message(caller, F.main("Gold", "Missing Args: " + F.elem("/gold <player> <amount>")));
			return;
		}

		final String targetName = args[0];
		final String goldString = args[1];
		final Player target = UtilPlayer.searchExact(targetName);

		if (target == null) {
			Plugin.getClientManager().loadClientByName(targetName, new Runnable() {
				@Override
				public void run() {
					final CoreClient client = Plugin.getClientManager().Get(targetName);

					if (client != null) {
						rewardGold(caller, null, targetName, client.getAccountId(), goldString);
					} else {
						UtilPlayer.message(caller, F.main("Gold", "Could not find player " + F.name(targetName)));
					}
				}
			});
		} else {
			rewardGold(caller, target, target.getName(), Plugin.getClientManager().Get(target).getAccountId(),
					goldString);
		}
	}

	private void rewardGold(final Player caller, final Player target, final String targetName, final int accountId,
			final int gold) {
		Plugin.RewardGold(new Callback<Boolean>() {
			@Override
			public void run(Boolean completed) {
				UtilPlayer.message(caller,
						F.main("Gold", "You gave " + F.elem(gold + " Gold") + " to " + F.name(targetName) + "."));

				if (target != null) {
					UtilPlayer.message(target,
							F.main("Gold", F.name(caller.getName()) + " gave you " + F.elem(gold + " Gold") + "."));
				}
			}
		}, caller.getName(), targetName, accountId, gold);
	}

	private void rewardGold(final Player caller, final Player target, final String targetName, final int accountId,
			String goldString) {
		try {
			final int gold = Integer.parseInt(goldString);
			rewardGold(caller, target, targetName, accountId, gold);
		} catch (final Exception e) {
			UtilPlayer.message(caller, F.main("Gold", "Invalid Gold Amount"));
		}
	}
}
