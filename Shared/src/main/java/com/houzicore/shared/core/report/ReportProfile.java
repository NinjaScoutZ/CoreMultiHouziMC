package com.houzicore.shared.core.report;

import com.houzicore.shared.serverdata.data.Data;

public class ReportProfile implements Data {

	private final int _playerId;

	private int _reputation;
	private boolean _banned;

	public ReportProfile(String playerName, int playerId) {
		_playerId = playerId;
		_reputation = 0;
		_banned = false;
	}

	public boolean canReport() {
		return !_banned;
	}

	@Override
	public String getDataId() {
		return String.valueOf(_playerId);
	}

	public int getReputation() {
		return _reputation;
	}

	/**
	 * Called when a report made by this player is closed.
	 * 
	 * @param result
	 *            - the result of the closed report.
	 */
	public void onReportClose(ReportResult result) {
		if (result == ReportResult.MUTED || result == ReportResult.BANNED) {
			_reputation++;
		} else if (result == ReportResult.ABUSE) {
			_reputation = -1;
			_banned = true;
		}
	}
}
