package com.houzicore.shared.account.command;

import java.util.Iterator;
import java.util.List;
import java.util.UUID;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import com.houzicore.shared.account.CoreClientManager;
import com.houzicore.shared.core.command.CommandBase;
import com.houzicore.shared.common.Rank;
import com.houzicore.shared.common.util.Callback;
import com.houzicore.shared.common.util.F;
import com.houzicore.shared.common.util.UUIDFetcher;
import com.houzicore.shared.common.util.UtilPlayer;

public class UpdateRank extends CommandBase<CoreClientManager> {
	public UpdateRank(CoreClientManager plugin) {
		super(plugin, Rank.ADMIN, new Rank[] { Rank.JNR_DEV /* On test servers only */ }, "updaterank", "rank");
	}

	@Override
	public List<String> onTabComplete(org.bukkit.command.CommandSender sender, String alias, String[] args) {
		if (args.length == 1) {
			if (sender instanceof Player) {
				return getPlayerMatches((Player) sender, args[0]);
			}
			final List<String> matches = new java.util.ArrayList<>();
			for (final Player player : com.houzicore.shared.common.util.UtilServer.getPlayers()) {
				if (player.getName().toLowerCase().startsWith(args[0].toLowerCase())) {
					matches.add(player.getName());
				}
			}
			return matches;
		} else if (args.length == 2) {
			return getMatches(args[1], Rank.values());
		}
		return null;
	}

	@Override
	public void Execute(final Player caller, String[] args) {
		final boolean testServer = Plugin.getPlugin().getConfig().getString("serverstatus.group")
				.equalsIgnoreCase("Testing");

		if (Plugin.Get(caller).GetRank() == Rank.JNR_DEV && !testServer) {
			F.main(Plugin.getName(),
					F.elem(Rank.JNR_DEV.GetTag(true, true)) + "s are only permitted to set ranks on test servers!");
			return;
		}

		if (args == null || args.length < 2) {
			UtilPlayer.message(caller, F.main(Plugin.getName(), "/" + AliasUsed + " joeschmo MODERATOR"));
		} else {
			if (args.length == 0) {
				UtilPlayer.message(caller, F.main(Plugin.getName(), "Player argument missing."));
				return;
			}

			final String playerName = args[0];
			Rank tempRank = null;

			try {
				tempRank = Rank.valueOf(args[1].toUpperCase());
			} catch (final Exception ex) {
				UtilPlayer.message(caller,
						F.main(Plugin.getName(), ChatColor.RED + "" + ChatColor.BOLD + "Invalid rank!"));
				return;
			}

			final Rank rank = tempRank;

			if (rank != null) {
				if (!testServer && rank.Has(Rank.ADMIN) && !Plugin.hasRank(caller, Rank.ADMIN)) {
					UtilPlayer.message(caller,
							F.main(Plugin.getName(), ChatColor.RED + "" + ChatColor.BOLD + "Insufficient privileges!"));
					return;
				}

				Plugin.getRepository().matchPlayerName(new Callback<List<String>>() {
					@Override
					public void run(List<String> matches) {
						boolean matchedExact = false;

						if (matches != null) {
							for (final String match : matches) {
								if (match.equalsIgnoreCase(playerName)) {
									matchedExact = true;
								}
							}

							if (matchedExact) {
								for (final Iterator<String> matchIterator = matches.iterator(); matchIterator.hasNext();) {
									if (!matchIterator.next().equalsIgnoreCase(playerName)) {
										matchIterator.remove();
									}
								}
							}
						}

						UtilPlayer.searchOffline(matches, new Callback<String>() {
							@Override
							public void run(final String target) {
								if (target == null)
									return;

								UUID uuid = Plugin.loadUUIDFromDB(playerName);

								if (uuid == null) {
									uuid = UUIDFetcher.getUUIDOf(playerName);
								}

								Plugin.getRepository().saveRank(new Callback<Rank>() {
									@Override
									public void run(Rank rank) {
										caller.sendMessage(F.main(Plugin.getName(),
												target + "'s rank has been updated to " + rank.Name + "!"));
										Player targetPlayer = org.bukkit.Bukkit.getPlayerExact(target);
										if (targetPlayer != null) {
											Plugin.Get(targetPlayer).SetRank(rank);
										}
									}
								}, target, uuid, rank, true);

							}
						}, caller, playerName, true);
					}
				}, playerName);
			}
		}
	}
}
