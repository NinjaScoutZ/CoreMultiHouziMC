package com.houzicore.shared.core.clan.redis;

import com.houzicore.shared.serverdata.commands.ServerCommand;

public class ClanChatCommand extends ServerCommand {
	private final int _clanId;
	private final String _senderName;
	private final String _message;

	public ClanChatCommand(int clanId, String senderName, String message) {
		_clanId = clanId;
		_senderName = senderName;
		_message = message;
	}

	public int getClanId() {
		return _clanId;
	}

	public String getSenderName() {
		return _senderName;
	}

	public String getMessage() {
		return _message;
	}
}
