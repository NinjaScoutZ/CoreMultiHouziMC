package com.houzicore.lobby.hub.commands;

import org.bukkit.Sound;
import org.bukkit.entity.Player;

import com.houzicore.lobby.hub.server.ServerManager;
import com.houzicore.shared.common.Rank;
import com.houzicore.shared.core.command.CommandBase;

public class LobbyMenuCommand extends CommandBase<ServerManager> {
	public LobbyMenuCommand(ServerManager plugin) {
		super(plugin, Rank.ALL, "lobbies", "servers", "servermenu");
	}

	@Override
	public void Execute(Player caller, String[] args) {
		caller.playSound(caller.getLocation(), Sound.UI_BUTTON_CLICK, 0.8f, 0.95f);
		Plugin.getLobbyShop().attemptShopOpen(caller);
	}
}
