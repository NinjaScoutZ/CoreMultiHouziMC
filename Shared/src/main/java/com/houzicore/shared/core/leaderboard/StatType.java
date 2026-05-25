package com.houzicore.shared.core.leaderboard;

/**
 * An enumeration delegating the various types of statistics to be dynamically
 * tracked.
 * 
 * @author MrTwiggy
 *
 */
public enum StatType {
	WIN(1), LOSS(2), KILL(3), DEATH(4);

	/**
	 * @param stat
	 *            - the display name for the stat type
	 * @return the {@link StatType} corresponding to the passed in {@code stat}, if
	 *         one exists, null otherwise.
	 */
	public static StatType getType(String stat) {
		switch (stat.toUpperCase().trim()) {
		case "WINS":
			return WIN;
		case "LOSSES":
			return LOSS;
		case "KILLS":
			return KILL;
		case "DEATHS":
			return DEATH;
		default:
			return null;
		}
	}

	private int _typeId; // Unique id for stat type

	/**
	 * Private class constructor
	 * 
	 * @param typeId
	 *            - the unique identifying id for this {@link StatType}
	 */
	private StatType(int typeId) {
		_typeId = typeId;
	}

	public int getTypeId() {
		return _typeId;
	}
}
