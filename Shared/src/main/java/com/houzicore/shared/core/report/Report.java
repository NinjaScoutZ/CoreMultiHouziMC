package com.houzicore.shared.core.report;

import java.util.HashSet;
import java.util.Set;

import com.houzicore.shared.serverdata.data.Data;

public class Report implements Data {

	private final int _reportId;

	private final String _serverName;

	private final String _playerName;

	// Set of account ids of players who contributed to reporting this player
	private final Set<String> _reporters;

	/**
	 * Class constructor
	 * 
	 * @param reportId
	 * @param playerName
	 * @param serverName
	 */
	public Report(int reportId, String playerName, String serverName) {
		_reportId = reportId;
		_playerName = playerName;
		_serverName = serverName;
		_reporters = new HashSet<>();
	}

	public void addReporter(String reporter) {
		_reporters.add(reporter);
	}

	@Override
	public String getDataId() {
		return String.valueOf(_reportId);
	}

	public String getPlayerName() {
		return _playerName;
	}

	public Set<String> getReporters() {
		return _reporters;
	}

	public int getReportId() {
		return _reportId;
	}

	public String getServerName() {
		return _serverName;
	}
}
