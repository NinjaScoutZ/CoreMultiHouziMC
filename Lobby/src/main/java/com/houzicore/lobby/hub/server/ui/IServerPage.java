package com.houzicore.lobby.hub.server.ui;

import com.houzicore.lobby.hub.server.ServerInfo;

import org.bukkit.entity.Player;

public interface IServerPage
{
	void SelectServer(Player player, ServerInfo _serverInfo);
}
