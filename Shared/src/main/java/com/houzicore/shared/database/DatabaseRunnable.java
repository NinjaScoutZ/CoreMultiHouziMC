package com.houzicore.shared.core.database;

public class DatabaseRunnable {
	private final Runnable _runnable;
	private int _failedAttempts = 0;

	public DatabaseRunnable(Runnable runnable) {
		_runnable = runnable;
	}

	public int getFailedCounts() {
		return _failedAttempts;
	}

	public void incrementFailCount() {
		_failedAttempts++;
	}

	public void run() {
		_runnable.run();
	}
}
