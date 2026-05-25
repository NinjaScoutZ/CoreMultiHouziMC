package com.houzicore.shared.core.quest;

public class PlayerQuest {
	private final int _questId;
	private long _periodId;
	private int _progress;
	private boolean _completed;

	public PlayerQuest(int questId, long periodId, int progress, boolean completed) {
		_questId = questId;
		_periodId = periodId;
		_progress = progress;
		_completed = completed;
	}

	public int getQuestId() {
		return _questId;
	}

	public long getPeriodId() {
		return _periodId;
	}

	public void setPeriodId(long periodId) {
		_periodId = periodId;
	}

	public int getProgress() {
		return _progress;
	}

	public void setProgress(int progress) {
		_progress = progress;
	}

	public void addProgress(int amount) {
		_progress += amount;
	}

	public boolean isCompleted() {
		return _completed;
	}

	public void setCompleted(boolean completed) {
		_completed = completed;
	}
	
	public Quest getQuest() {
		return Quest.getById(_questId);
	}
}
