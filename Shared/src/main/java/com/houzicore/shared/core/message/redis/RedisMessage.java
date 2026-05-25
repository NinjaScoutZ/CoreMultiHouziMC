package com.houzicore.shared.core.message.redis;

import java.util.UUID;

import com.houzicore.shared.serverdata.commands.ServerCommand;

/**
 * Used to send a admin or normal message between servers
 */
public class RedisMessage extends ServerCommand {
	private final String _message;
	private final String _sender;
	private final String _sendingServer;
	private final String _target;
	private final String _rank;
	private final UUID _uuid = UUID.randomUUID();

	public RedisMessage(String sendingServer, String sender, String targetServer, String target, String message,
			String rank) {
		_sender = sender;
		_target = target;
		_message = message;
		_sendingServer = sendingServer;
		_rank = rank;

		if (targetServer != null) {
			setTargetServers(targetServer);
		}
	}

	public String getMessage() {
		return _message;
	}

	public String getRank() {
		return _rank;
	}

	public String getSender() {
		return _sender;
	}

	public String getSendingServer() {
		return _sendingServer;
	}

	public String getTarget() {
		return _target;
	}

	public UUID getUUID() {
		return _uuid;
	}

	public boolean isStaffMessage() {
		return getTargetServers().length == 0;
	}

	@Override
	public void run() {
		// Utilitizes a callback functionality to seperate dependencies
	}
}
