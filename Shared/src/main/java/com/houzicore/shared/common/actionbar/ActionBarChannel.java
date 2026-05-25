package com.houzicore.shared.common.actionbar;

public enum ActionBarChannel
{
	SYSTEM_ALERT(100, 2500),
	GAME_EVENT(80, 1500),
	REWARD(70, 1500),
	GAME_STATUS(60, 800),
	LEGACY(50, 500),
	TOOL_HINT(30, 350);

	private final int priority;
	private final long defaultTtlMs;

	ActionBarChannel(int priority, long defaultTtlMs)
	{
		this.priority = priority;
		this.defaultTtlMs = defaultTtlMs;
	}

	public int getPriority()
	{
		return priority;
	}

	public long getDefaultTtlMs()
	{
		return defaultTtlMs;
	}
}
