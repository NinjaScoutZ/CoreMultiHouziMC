package com.houzicore.shared.core.party.redis;

import com.houzicore.shared.core.party.Party;
import com.houzicore.shared.core.party.PartyManager;
import com.houzicore.shared.serverdata.commands.CommandCallback;
import com.houzicore.shared.serverdata.commands.ServerCommand;

public class RedisPartyHandler implements CommandCallback {
	private final PartyManager _partyManager;

	public RedisPartyHandler(PartyManager partyManager) {
		_partyManager = partyManager;
	}

	@Override
	public void run(ServerCommand command) {
		final RedisPartyData data = (RedisPartyData) command;

		_partyManager.getPlugin().getServer().getScheduler().runTask(_partyManager.getPlugin(), new Runnable() {
			@Override
			public void run() {
				_partyManager.addParty(new Party(_partyManager, data));
			}
		});
	}
}
