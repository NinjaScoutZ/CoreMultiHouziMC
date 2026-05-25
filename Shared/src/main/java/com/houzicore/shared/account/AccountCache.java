package com.houzicore.shared.account;

import java.util.UUID;

import com.houzicore.shared.serverdata.data.Data;

public class AccountCache implements Data {
	private final UUID _uuid;
	private final Integer _id;

	public AccountCache(UUID uuid, int id) {
		_uuid = uuid;
		_id = id;
	}

	@Override
	public String getDataId() {
		return _uuid.toString();
	}

	public int getId() {
		return _id;
	}

	public UUID getUUID() {
		return _uuid;
	}
}
