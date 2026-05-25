package com.houzicore.shared.core.punish;

import java.util.HashMap;

import com.houzicore.shared.MiniClientPlugin;
import com.houzicore.shared.account.CoreClientManager;
import com.houzicore.shared.account.event.ClientWebResponseEvent;
import com.houzicore.shared.common.Rank;
import com.houzicore.shared.common.util.C;
import com.houzicore.shared.common.util.Callback;
import com.houzicore.shared.common.util.F;
import com.houzicore.shared.common.util.UtilPlayer;
import com.houzicore.shared.common.util.UtilServer;
import com.houzicore.shared.common.util.UtilTime;
import com.houzicore.shared.common.util.UtilTime.TimeUnit;
import com.houzicore.shared.core.punish.Command.PunishCommand;
import com.houzicore.shared.core.punish.Tokens.PunishClientToken;
import com.houzicore.shared.core.punish.Tokens.PunishmentToken;
import com.houzicore.shared.serverdata.commands.ServerCommandManager;

import org.bukkit.Bukkit;
import org.bukkit.Sound;
import com.google.gson.Gson;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent.Result;
import org.bukkit.plugin.java.JavaPlugin;

public class Punish extends MiniClientPlugin<PunishClient> {
	private final PunishRepository _repository;
	private final CoreClientManager _clientManager;

	public Punish(JavaPlugin plugin, String webServerAddress, CoreClientManager clientManager) {
		super("Punish", plugin);

		_clientManager = clientManager;
		_repository = new PunishRepository();

		ServerCommandManager.getInstance().registerCommandType("PunishCommand",
				com.houzicore.shared.serverdata.commands.PunishCommand.class, new PunishmentHandler(this));
	}

	@Override
	protected PunishClient AddPlayer(String player) {
		return new PunishClient();
	}

	@Override
	public void addCommands() {
		addCommand(new PunishCommand(this));
	}

	public void AddPunishment(final String playerName, final Category category, final String reason,
			final Player caller, final int severity, boolean ban, long duration) {
		
		// Ensure client tracking
		Get(playerName);

		final PunishmentSentence sentence = !ban ? PunishmentSentence.Mute : PunishmentSentence.Ban;

		final long finalDuration = duration;

		_repository.Punish(new Callback<String>() {
			@Override
			public void run(String result) {
				final PunishmentResponse banResult = PunishmentResponse.valueOf(result);

				if (banResult == PunishmentResponse.AccountDoesNotExist) {
					if (caller != null) {
						caller.sendMessage(
								F.main(getName(), "Account with name " + F.elem(playerName) + " does not exist."));
					} else {
					}
				} else if (banResult == PunishmentResponse.InsufficientPrivileges) {
					if (caller != null) {
						caller.sendMessage(F.main(getName(),
								"You have insufficient rights to punish " + F.elem(playerName) + "."));
					} else {
					}
				} else if (banResult == PunishmentResponse.Punished) {
					final String durationString = UtilTime
							.convertString(finalDuration < 0 ? -1 : (long) (finalDuration * 3600000), 1, TimeUnit.FIT);

					if (sentence == PunishmentSentence.Ban) {
						if (caller == null) {
						}

						Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(getPlugin(), new Runnable() {
							@Override
							public void run() {
								final String kickReason = C.cRed + C.Bold + "You were banned for " + durationString
										+ " by " + (caller == null ? com.houzicore.shared.core.common.BrandConfig.mainServerName() + " Anti-Cheat" : caller.getName()) + "\n"
										+ C.cWhite + reason + "\n" + C.cDGreen + "Unfairly banned? Appeal at "
										+ C.cGreen + "an administrator";

								final Player target = UtilPlayer.searchOnline(null, playerName, false);
								if (target != null) {
									target.kickPlayer(kickReason);
								} else {
									new com.houzicore.shared.serverdata.commands.PunishCommand(playerName, true, false, kickReason)
											.publish();
								}
							}
						});

						informOfPunish(playerName, F.main(getName(), caller == null ? com.houzicore.shared.core.common.BrandConfig.mainServerName() + " Anti-Cheat"
								: caller.getName() + " banned " + playerName + " for " + durationString + "."));
					} else {
						if (caller == null) {
						}

						// Warning
						if (finalDuration == 0) {
							informOfPunish(playerName, F.main(getName(), caller == null ? com.houzicore.shared.core.common.BrandConfig.mainServerName() + " Anti-Cheat"
									: caller.getName() + " issued a friendly warning to " + playerName + "."));
						} else {
							informOfPunish(playerName, F.main(getName(), caller == null ? com.houzicore.shared.core.common.BrandConfig.mainServerName() + " Anti-Cheat"
									: caller.getName() + " muted " + playerName + " for " + durationString + "."));
						}

						// Inform
						final Player target = UtilPlayer.searchExact(playerName);
						if (target != null) {
							UtilPlayer.message(target,
									F.main("Punish", F.elem(C.cGray + C.Bold + "Reason: ") + reason));
							target.playSound(target.getLocation(), Sound.ENTITY_CAT_AMBIENT, 1f, 1f);
						} else {
							new com.houzicore.shared.serverdata.commands.PunishCommand(playerName, false, finalDuration != 0,
									F.main("Punish", F.elem(
											C.cGray + C.Bold + (finalDuration != 0 ? "Mute" : "Warning") + " Reason: ")
											+ reason)).publish();
						}

						_repository.LoadPunishClient(playerName, new Callback<PunishClientToken>() {
							@Override
							public void run(PunishClientToken token) {
								LoadClient(token);
							}
						});
					}
				}
			}

		}, playerName, category.toString(), sentence, reason, duration,
				caller == null ? com.houzicore.shared.core.common.BrandConfig.mainServerName() + " Anti-Cheat" : caller.getName(), severity);
	}

	public int factorial(int n) {
		if (n == 0)
			return 1;

		return n * factorial(n - 1);
	}

	public PunishClient GetClient(String name) {
		return Get(name);
	}

	public CoreClientManager GetClients() {
		return _clientManager;
	}

	public PunishRepository GetRepository() {
		return _repository;
	}

	public void Help(Player caller) {
		UtilPlayer.message(caller, F.main(_moduleName, "Commands List:"));
		UtilPlayer.message(caller, F.help("/punish", "<player> <reason>", Rank.MODERATOR));
	}

	private void informOfPunish(String punishee, String msg) {
		for (final Player player : UtilServer.getPlayers()) {
			if (_clientManager.Get(player).GetRank().Has(Rank.HELPER) || player.getName().equals(punishee)) {
				player.sendMessage(msg);
			}
		}
	}

	public void LoadClient(PunishClientToken token) {
		final PunishClient client = new PunishClient();

		final long timeDifference = System.currentTimeMillis() - token.Time;

		for (final PunishmentToken punishment : token.Punishments) {
			client.AddPunishment(Category.valueOf(punishment.Category),
					new Punishment(punishment.PunishmentId, PunishmentSentence.valueOf(punishment.Sentence),
							Category.valueOf(punishment.Category), punishment.Reason, punishment.Admin,
							punishment.Duration, punishment.Severity, punishment.Time + timeDifference,
							punishment.Active, punishment.Removed, punishment.RemoveAdmin, punishment.RemoveReason));
		}

		Set(token.Name, client);
	}

	@EventHandler
	public void OnClientWebResponse(ClientWebResponseEvent event) {
		final PunishClientToken token = new Gson().fromJson(event.GetResponse(), PunishClientToken.class);
		LoadClient(token);
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	public void PlayerLogin(AsyncPlayerPreLoginEvent event) {
		final PunishClient client = GetClient(event.getName());

		if (client != null && client.IsBanned()) {
			final Punishment punishment = client.GetPunishment(PunishmentSentence.Ban);
				String time = UtilTime.convertString(punishment.GetRemaining(), 0, TimeUnit.FIT);

				if (punishment.GetHours() == -1) {
					time = "Permanent";
				}

				final String reason = C.cRed + C.Bold + "You are banned for " + time + "\n" + C.cWhite
						+ punishment.GetReason() + "\n" + C.cDGreen + "Unfairly banned? Appeal at " + C.cGreen
						+ "an administrator";

				event.disallow(Result.KICK_BANNED, reason);
			}
	}

	@EventHandler(priority = EventPriority.LOWEST)
	public void PunishChatEvent(AsyncPlayerChatEvent event) {
		final PunishClient client = GetClient(event.getPlayer().getName());

		if (client != null && client.IsMuted()) {
			event.getPlayer().sendMessage(F.main(getName(),
					"Shh, you're muted because " + client.GetPunishment(PunishmentSentence.Mute).GetReason() + " by "
							+ client.GetPunishment(PunishmentSentence.Mute).GetAdmin() + " for " + C.cGreen
							+ UtilTime.convertString(client.GetPunishment(PunishmentSentence.Mute).GetRemaining(), 1,
									TimeUnit.FIT)
							+ "."));
			event.setCancelled(true);
		}
	}

	public void RemoveBan(String name, String reason) {
		_repository.RemoveBan(name, reason);
	}

	public void RemovePunishment(int punishmentId, String target, final Player admin, String reason,
			Callback<String> callback) {
		_repository.RemovePunishment(callback, punishmentId, target, reason, admin.getName());
	}
}
