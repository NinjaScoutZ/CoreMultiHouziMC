package com.houzicore.shared.core.punish;

import com.houzicore.shared.common.util.Callback;
import com.houzicore.shared.common.util.UtilPlayer;
import com.houzicore.shared.core.punish.Tokens.PunishClientToken;
import com.houzicore.shared.serverdata.commands.CommandCallback;
import com.houzicore.shared.serverdata.commands.PunishCommand;
import com.houzicore.shared.serverdata.commands.ServerCommand;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public class PunishmentHandler implements CommandCallback {
	private final Punish _punishManager;

	public PunishmentHandler(Punish punishManager) {
		_punishManager = punishManager;
	}

	@Override
	public void run(ServerCommand command) {
		if (command instanceof PunishCommand) {
			final PunishCommand punishCommand = (PunishCommand) command;

			final String playerName = punishCommand.getPlayerName();
			final boolean ban = punishCommand.getBan();
			final String reason = punishCommand.getMessage();
			final Player player = Bukkit.getPlayer(playerName);

			if (player != null && player.isOnline()) {
				if (ban) {
					Bukkit.getServer().getScheduler().runTask(_punishManager.getPlugin(), new Runnable() {
						@Override
						public void run() {
							player.kickPlayer(reason);
						}
					});
				} else {
					_punishManager.GetRepository().LoadPunishClient(playerName, new Callback<PunishClientToken>() {
						@Override
						public void run(PunishClientToken token) {
							_punishManager.LoadClient(token);
							UtilPlayer.message(player, reason);
						}
					});
				}
			}
		}
	}
}
