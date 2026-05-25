package com.houzicore.shared.core.friend.redis;

import com.houzicore.shared.serverdata.commands.ServerCommand;

public class DeleteFriend extends ServerCommand {
	private final String _deleter;
	private final String _deleted;

	public DeleteFriend(String deleter, String deleted) {
		_deleter = deleter;
		_deleted = deleted;
	}

	public String getDeleted() {
		return _deleted;
	}

	public String getDeleter() {
		return _deleter;
	}

	@Override
	public void run() {
		// Utilitizes a callback functionality to seperate dependencies
	}
}
