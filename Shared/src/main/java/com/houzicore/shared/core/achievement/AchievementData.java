package com.houzicore.shared.core.achievement;

public class AchievementData {
	private final int _level;
	private final long _expRemainder;
	private final long _expNextLevel;

	public AchievementData(int level, long expRemainder, long expNextLevel) {
		_level = level;
		_expRemainder = expRemainder;
		_expNextLevel = expNextLevel;
	}

	public long getExpNextLevel() {
		return _expNextLevel;
	}

	public long getExpRemainder() {
		return _expRemainder;
	}

	public int getLevel() {
		return _level;
	}
}
