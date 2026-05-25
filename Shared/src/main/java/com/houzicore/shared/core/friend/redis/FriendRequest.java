package com.houzicore.shared.core.friend.redis;

import com.houzicore.shared.serverdata.commands.ServerCommand;

public class FriendRequest extends ServerCommand {
	private final String _requester;
	private final String _requested;

	public FriendRequest(String requester, String requested) {
		_requester = requester;
		_requested = requested;
	}

	public String getRequested() {
		return _requested;
	}

	public String getRequester() {
		return _requester;
	}

	@Override
	public void run() {
		// Utilitizes a callback functionality to seperate dependencies
	}
}
