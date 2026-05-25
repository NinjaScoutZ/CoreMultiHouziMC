package com.houzicore.shared.core.quest;

public enum QuestType {
	DAILY(86400000L), // Milliseconds in a day
	WEEKLY(604800000L); // Milliseconds in a week

	private final long _duration;

	private QuestType(long duration) {
		_duration = duration;
	}

	public long getDuration() {
		return _duration;
	}

	public long getCurrentPeriodId() {
		// Offset slightly so week/day resets logically at a specific time (optional, UTC for now)
		return System.currentTimeMillis() / _duration;
	}
	
	public long getPeriodExpiration(long periodId) {
		return (periodId + 1) * _duration;
	}
}
