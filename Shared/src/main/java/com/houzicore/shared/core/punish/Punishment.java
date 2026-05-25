package com.houzicore.shared.core.punish;

import com.houzicore.shared.common.util.TimeSpan;

public class Punishment {
	private final int _id;
	private final PunishmentSentence _punishmentType;
	private final Category _category;
	private final String _reason;
	private final String _admin;
	private final double _hours;
	private final int _severity;
	private final long _time;
	private final boolean _active;
	private boolean _removed;
	private String _removeAdmin;
	private String _removeReason;

	public Punishment(int id, PunishmentSentence punishmentType, Category category, String reason, String admin,
			double hours, int severity, long time, boolean active, boolean removed, String removeAdmin,
			String removeReason) {
		_id = id;
		_punishmentType = punishmentType;
		_category = category;
		_reason = reason;
		_admin = admin;
		_hours = hours;
		_severity = severity;
		_time = time;
		_active = active;
		_removed = removed;
		_removeAdmin = removeAdmin;
		_removeReason = removeReason;
	}

	public boolean GetActive() {
		return _active;
	}

	public String GetAdmin() {
		return _admin;
	}

	public Category GetCategory() {
		return _category;
	}

	public double GetHours() {
		return _hours;
	}

	public int GetPunishmentId() {
		return _id;
	}

	public PunishmentSentence GetPunishmentType() {
		return _punishmentType;
	}

	public String GetReason() {
		return _reason;
	}

	public long GetRemaining() {
		return _hours < 0 ? -1 : (long) (_time + TimeSpan.HOUR * _hours - System.currentTimeMillis());
	}

	public String GetRemoveAdmin() {
		return _removeAdmin;
	}

	public boolean GetRemoved() {
		return _removed;
	}

	public String GetRemoveReason() {
		return _removeReason;
	}

	public int GetSeverity() {
		return _severity;
	}

	public long GetTime() {
		return _time;
	}

	public boolean IsBanned() {
		return _punishmentType == PunishmentSentence.Ban && (GetRemaining() > 0 || _hours < 0) && _active;
	}

	public boolean IsMuted() {
		return _punishmentType == PunishmentSentence.Mute && (GetRemaining() > 0 || _hours < 0) && _active;
	}

	public void Remove(String admin, String reason) {
		_removed = true;
		_removeAdmin = admin;
		_removeReason = reason;
	}
}
