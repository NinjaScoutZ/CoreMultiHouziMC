package com.houzicore.shared.core.donation.command;

import com.houzicore.shared.serverdata.commands.ServerCommand;

import org.bukkit.Bukkit;

public class CurrencyUpdateCommand extends ServerCommand {
	private String _playerName;
	private int _gemsAdded;
	private int _coinsAdded;
	private int _serverPort;

	public CurrencyUpdateCommand(String playerName, int gemsAdded, int coinsAdded) {
		super();
		_playerName = playerName;
		_gemsAdded = gemsAdded;
		_coinsAdded = coinsAdded;
		_serverPort = Bukkit.getPort();
	}

	public String getPlayerName() {
		return _playerName;
	}

	public int getGemsAdded() {
		return _gemsAdded;
	}

	public int getCoinsAdded() {
		return _coinsAdded;
	}

	public int getServerPort() {
		return _serverPort;
	}
}
