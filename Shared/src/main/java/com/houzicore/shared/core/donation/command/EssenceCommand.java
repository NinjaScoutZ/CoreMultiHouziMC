package com.houzicore.shared.core.donation.command;

import java.util.UUID;

import org.bukkit.entity.Player;

import com.houzicore.shared.core.command.CommandBase;
import com.houzicore.shared.common.Rank;
import com.houzicore.shared.common.util.Callback;
import com.houzicore.shared.common.util.F;
import com.houzicore.shared.common.util.UUIDFetcher;
import com.houzicore.shared.common.util.UtilPlayer;
import com.houzicore.shared.core.donation.DonationManager;

public class EssenceCommand extends CommandBase<DonationManager> {
	public EssenceCommand(DonationManager plugin) {
		super(plugin, Rank.ADMIN, "essence");
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
			UtilPlayer.message(caller, F.main("essence", "Missing Args: " + F.elem("/essence <player> <amount>")));
			return;
		}
		int offset = 0;
		if (args[0].equalsIgnoreCase("add")) {
			if (args.length < 3) {
				UtilPlayer.message(caller, F.main("essence", "Missing Args: " + F.elem("/essence [add] <player> <amount>")));
				return;
			}
			offset = 1;
		}

		final String targetName = args[offset];
		final String gemsString = args[offset + 1];
		final Player target = UtilPlayer.searchExact(targetName);

		if (target == null) {
			final UUID uuid = UUIDFetcher.getUUIDOf(targetName);
			if (uuid != null) {
				rewardGems(caller, null, targetName, uuid, gemsString);
			} else {
				UtilPlayer.message(caller, F.main("Essence", "Could not find player " + F.name(targetName)));
			}
		} else {
			rewardGems(caller, target, target.getName(), target.getUniqueId(), gemsString);
		}
	}

	private void rewardGems(final Player caller, final Player target, final String targetName, final UUID uuid,
			final int gems) {
		Plugin.RewardEssence(new Callback<Boolean>() {
			@Override
			public void run(Boolean completed) {
				if (completed) {
					UtilPlayer.message(caller,
							F.main("essence", "You gave " + F.elem(gems + " essence") + " to " + F.name(targetName) + "."));

					if (target != null) {
						UtilPlayer.message(target,
								F.main("essence", F.name(caller.getName()) + " gave you " + F.elem(gems + " essence") + "."));
					}
				} else {
					UtilPlayer.message(caller, F.main("Essence", "There was an error giving " + F.elem(gems + " essence") + " to " + F.name(targetName) + "."));
				}
			}
		}, caller.getName(), targetName, uuid, gems);
	}

	private void rewardGems(final Player caller, final Player target, final String targetName, final UUID uuid,
			String gemsString) {
		try {
			final int gems = Integer.parseInt(gemsString);
			rewardGems(caller, target, targetName, uuid, gems);
		} catch (final Exception e) {
			UtilPlayer.message(caller, F.main("essence", "Invalid essence Amount"));
		}
	}
}
