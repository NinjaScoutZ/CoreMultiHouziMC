package com.houzicore.shared.core.punish.Command;

import java.util.Iterator;
import java.util.List;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.houzicore.shared.core.command.CommandBase;
import com.houzicore.shared.common.Rank;
import com.houzicore.shared.common.util.Callback;
import com.houzicore.shared.common.util.UtilPlayer;
import com.houzicore.shared.core.punish.Punish;
import com.houzicore.shared.core.punish.Tokens.PunishClientToken;
import com.houzicore.shared.core.punish.UI.PunishPage;

public class PunishCommand extends CommandBase<Punish> {
	public PunishCommand(Punish plugin) {
		super(plugin, Rank.HELPER, "punish", "p");
	}

	@Override
	public void Execute(final Player caller, String[] args) {
		if (args == null || args.length < 2) {
			Plugin.Help(caller);
		} else {
			final String playerName = args[0];
			String reason = args[1];

			for (int i = 2; i < args.length; i++) {
				reason += " " + args[i];
			}

			final String finalReason = reason;

			// Match exact online first
			final Player target = UtilPlayer.searchExact(playerName);
			if (target != null) {
				Plugin.GetRepository().LoadPunishClient(playerName, new Callback<PunishClientToken>() {
					@Override
					public void run(PunishClientToken clientToken) {
						Plugin.LoadClient(clientToken);
						new PunishPage(Plugin, caller, playerName, finalReason);
					}
				});

				return;
			}

			// Check repo
			Plugin.GetRepository().MatchPlayerName(new Callback<List<String>>() {
				@Override
				public void run(List<String> matches) {
					boolean matchedExact = false;

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

					UtilPlayer.searchOffline(matches, new Callback<String>() {
						@Override
						public void run(final String target) {
							if (target == null)
								return;

							Plugin.GetRepository().LoadPunishClient(target, new Callback<PunishClientToken>() {
								@Override
								public void run(PunishClientToken clientToken) {
									Plugin.LoadClient(clientToken);
									new PunishPage(Plugin, caller, target, finalReason);
								}
							});

						}
					}, caller, playerName, true);
				}
			}, playerName);
		}
	}

	@Override
	public List<String> onTabComplete(CommandSender sender, String commandLabel, String[] args) {
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
		}
		return null;
	}
}
