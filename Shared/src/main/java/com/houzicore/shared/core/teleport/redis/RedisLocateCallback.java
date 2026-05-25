package com.houzicore.shared.core.teleport.redis;

import java.util.UUID;

import com.houzicore.shared.serverdata.commands.ServerCommand;

public class RedisLocateCallback extends ServerCommand {
	private final String _locatedPlayer;
	private final String _server;
	private final String _receivingPlayer;
	private final UUID _uuid;

	public RedisLocateCallback(RedisLocate command, String server, String targetName) {
		_uuid = command.getUUID();
		_receivingPlayer = command.getSender();
		_locatedPlayer = targetName;
		_server = server;

		setTargetServers(command.getServer());
	}

	public String getLocatedPlayer() {
		return _locatedPlayer;
	}

	public String getReceivingPlayer() {
		return _receivingPlayer;
	}

	public String getServer() {
		return _server;
	}

	public UUID getUUID() {
		return _uuid;
	}
}
