package com.houzicore.shared.core.message.redis;

import java.util.UUID;

import com.houzicore.shared.serverdata.commands.ServerCommand;

/**
 * Used as a response in return to a admin or normal message between servers.
 */
public class RedisMessageCallback extends ServerCommand {
	private final String _message;
	private final String _setLastMessage;
	private final String _target;
	private final boolean _staffMessage;
	private final UUID _uuid;

	public RedisMessageCallback(RedisMessage globalMessage, boolean staffMessage, String receivedPlayer,
			String message) {
		_target = globalMessage.getSender();
		_message = message;
		_setLastMessage = receivedPlayer;
		_uuid = globalMessage.getUUID();
		_staffMessage = staffMessage;

		if (globalMessage.getSendingServer() != null) {
			setTargetServers(globalMessage.getSendingServer());
		}
	}

	public String getLastReplied() {
		return _setLastMessage;
	}

	public String getMessage() {
		return _message;
	}

	public String getTarget() {
		return _target;
	}

	public UUID getUUID() {
		return _uuid;
	}

	public boolean isStaffMessage() {
		return _staffMessage;
	}

	@Override
	public void run() {
		// Utilitizes a callback functionality to seperate dependencies
	}
}
