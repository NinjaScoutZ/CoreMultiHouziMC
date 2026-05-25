package com.houzicore.shared.core.party.redis;

import java.util.UUID;
import com.houzicore.shared.core.party.Party;
import com.houzicore.shared.serverdata.commands.ServerCommand;

public class RedisPartyData extends ServerCommand {
	private final String[] _players;
	private final UUID[] _playerUuids;
	private final String _leader;
	private final UUID _leaderUuid;
	private final String _previousServer;
	private final String _password;

	public RedisPartyData(Party party, String previousServer) {
		_players = party.GetPlayers().toArray(new String[0]);
		_playerUuids = party.getPlayerUuids().toArray(new UUID[0]);
		_leader = party.getLeaderName();
		_leaderUuid = party.getLeaderUuid();
		_previousServer = previousServer;
		_password = party.getPassword();
	}

	public String getPassword() {
		return _password;
	}

	public String getLeaderName() {
		return _leader;
	}

	public UUID getLeaderUuid() {
		return _leaderUuid;
	}

	public String[] getPlayers() {
		return _players;
	}

	public UUID[] getPlayerUuids() {
		return _playerUuids;
	}

	public String getPreviousServer() {
		return _previousServer;
	}
}
