package com.houzicore.shared.core.teleport.redis;

import java.util.UUID;

import com.houzicore.shared.serverdata.commands.ServerCommand;

public class RedisLocate extends ServerCommand {
	private final String _sender;
	private final String _sendingServer;
	private final String _target;
	private final UUID _uuid = UUID.randomUUID();

	public RedisLocate(String sendingServer, String sender, String target) {
		_sender = sender;
		_target = target;
		_sendingServer = sendingServer;
	}

	public String getSender() {
		return _sender;
	}

	public String getServer() {
		return _sendingServer;
	}

	public String getTarget() {
		return _target;
	}

	public UUID getUUID() {
		return _uuid;
	}
}
