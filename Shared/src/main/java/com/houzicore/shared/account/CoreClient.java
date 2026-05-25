package com.houzicore.shared.account;

import com.houzicore.shared.common.Rank;

import org.bukkit.entity.Player;

public class CoreClient {
	private int _accountId = -1;
	private String _name;
	private Player _player;
	private Rank _rank = Rank.ALL;

	public CoreClient(Player player) {
		_player = player;
		_name = player.getName();
	}

	public CoreClient(String name) {
		_name = name;
	}

	public void Delete() {
		_name = null;
		_player = null;
	}

	public int getAccountId() {
		return _accountId;
	}

	public Player GetPlayer() {
		return _player;
	}

	public String GetPlayerName() {
		return _name;
	}

	public Rank GetRank() {
		return _rank;
	}

	public void setAccountId(int accountId) {
		_accountId = accountId;
	}

	public void SetPlayer(Player player) {
		_player = player;
	}

	public void SetRank(Rank rank) {
		_rank = rank;
	}
}
