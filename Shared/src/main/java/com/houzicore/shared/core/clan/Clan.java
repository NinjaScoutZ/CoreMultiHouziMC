package com.houzicore.shared.core.clan;

public class Clan {
	private int _id;
	private String _name;
	private String _description;
	private int _leaderId;

	public Clan(int id, String name, String description, int leaderId) {
		_id = id;
		_name = name;
		_description = description;
		_leaderId = leaderId;
	}

	public int getId() {
		return _id;
	}

	public String getName() {
		return _name;
	}

	public String getDescription() {
		return _description;
	}

	public int getLeaderId() {
		return _leaderId;
	}
}
