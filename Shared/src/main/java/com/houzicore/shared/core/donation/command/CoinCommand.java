package com.houzicore.shared.core.donation.command;

import com.houzicore.shared.account.CoreClient;
import com.houzicore.shared.core.command.CommandBase;
import com.houzicore.shared.common.Rank;
import com.houzicore.shared.common.util.Callback;
import com.houzicore.shared.common.util.F;
import com.houzicore.shared.common.util.UtilPlayer;
import com.houzicore.shared.core.donation.DonationManager;

import org.bukkit.entity.Player;

public class CoinCommand extends CommandBase<DonationManager> {
	public CoinCommand(DonationManager plugin) {
		super(plugin, Rank.ADMIN, "coin");
	}

	@Override
	public java.util.List<String> onTabComplete(org.bukkit.command.CommandSender sender, String alias, String[] args) {
		if (args.length == 1) {
			java.util.List<String> matches;
			if (sender instanceof Player) {
				matches = getPlayerMatches((Player) sender, args[0]);
			} else {
				matches = new java.util.ArrayList<>();
				for (final Player player : com.houzicore.shared.common.util.UtilServer.getPlayers()) {
					if (player.getName().toLowerCase().startsWith(args[0].toLowerCase())) {
						matches.add(player.getName());
					}
				}
			}
			if ("add".startsWith(args[0].toLowerCase())) matches.add("add");
			return matches;
		} else if (args.length == 2 && args[0].equalsIgnoreCase("add")) {
			if (sender instanceof Player) {
				return getPlayerMatches((Player) sender, args[1]);
			} else {
				final java.util.List<String> matches = new java.util.ArrayList<>();
				for (final Player player : com.houzicore.shared.common.util.UtilServer.getPlayers()) {
					if (player.getName().toLowerCase().startsWith(args[1].toLowerCase())) {
						matches.add(player.getName());
					}
				}
				return matches;
			}
		}
		return null;
	}

	@Override
	public void Execute(final Player caller, String[] args) {
		if (args.length < 2) {
			UtilPlayer.message(caller, F.main("Coin", "Missing Args: " + F.elem("/coin <player> <amount>")));
			return;
		}
		int offset = 0;
		if (args[0].equalsIgnoreCase("add")) {
			if (args.length < 3) {
				UtilPlayer.message(caller, F.main("Coin", "Missing Args: " + F.elem("/coin [add] <player> <amount>")));
				return;
			}
			offset = 1;
		}

		final String targetName = args[offset];
		final String coinsString = args[offset + 1];
		final Player target = UtilPlayer.searchExact(targetName);

		if (target == null) {
			Plugin.getClientManager().loadClientByName(targetName, new Runnable() {
				@Override
				public void run() {
					final CoreClient client = Plugin.getClientManager().Get(targetName);

					if (client != null) {
						rewardCoins(caller, null, targetName, client.getAccountId(), coinsString);
					} else {
						UtilPlayer.message(caller, F.main("Coin", "Could not find player " + F.name(targetName)));
					}
				}
			});
		} else {
			rewardCoins(caller, target, target.getName(), Plugin.getClientManager().Get(target).getAccountId(),
					coinsString);
		}
	}

	private void rewardCoins(final Player caller, final Player target, final String targetName, final int accountId,
			final int coins) {
		Plugin.RewardCoins(new Callback<Boolean>() {
			@Override
			public void run(Boolean completed) {
				if (completed) {
					UtilPlayer.message(caller,
							F.main("Coin", "You gave " + F.elem(coins + " Coins") + " to " + F.name(targetName) + "."));

					if (target != null) {
						UtilPlayer.message(target, F.main("Coin",
								F.name(caller.getName()) + " gave you " + F.elem(coins + " Coins") + "."));
					}
				} else {
					UtilPlayer.message(caller, F.main("Coin", "There was an error giving " + F.elem(coins + "Coins")
							+ " to " + F.name(targetName) + "."));
				}
			}
		}, caller.getName(), targetName, accountId, coins);
	}

	private void rewardCoins(final Player caller, final Player target, final String targetName, final int accountId,
			String coinsString) {
		try {
			final int coins = Integer.parseInt(coinsString);
			rewardCoins(caller, target, targetName, accountId, coins);
		} catch (final Exception e) {
			UtilPlayer.message(caller, F.main("Coin", "Invalid Coins Amount"));
		}
	}
}
