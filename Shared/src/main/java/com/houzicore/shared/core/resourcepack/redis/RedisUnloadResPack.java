package com.houzicore.shared.core.resourcepack.redis;

import com.houzicore.shared.serverdata.commands.ServerCommand;

public class RedisUnloadResPack extends ServerCommand {
	private final String _player;

	public RedisUnloadResPack(String player) {

		_player = player;
	}

	public String getPlayer() {
		return _player;
	}
}
