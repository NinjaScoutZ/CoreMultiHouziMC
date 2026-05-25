package com.houzicore.shared.core.portal;

import com.houzicore.shared.serverdata.commands.CommandCallback;
import com.houzicore.shared.serverdata.commands.ServerCommand;
import com.houzicore.shared.serverdata.commands.ServerTransfer;
import com.houzicore.shared.serverdata.commands.TransferCommand;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public class TransferHandler implements CommandCallback {
	@Override
	public void run(ServerCommand command) {
		if (command instanceof TransferCommand) {
			final TransferCommand transferCommand = (TransferCommand) command;
			final ServerTransfer transfer = transferCommand.getTransfer();

			final Player player = Bukkit.getPlayerExact(transfer.getPlayerName());

			if (player != null && player.isOnline()) {
				Portal.getInstance().sendPlayerToServer(player, transfer.getServerName());
			}
		}
	}
}
