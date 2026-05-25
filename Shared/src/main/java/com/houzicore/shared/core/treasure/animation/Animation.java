package com.houzicore.shared.core.treasure.animation;

import com.houzicore.shared.core.treasure.Treasure;

/**
 * Created by Shaun on 8/29/2014.
 */
public abstract class Animation {
	private final Treasure _treasure;
	private boolean _running;
	private int _ticks;

	public Animation(Treasure treasure) {
		_treasure = treasure;
		_running = true;
	}

	public void finish() {
		if (_running) {
			_running = false;
			onFinish();
		}
	}

	public int getTicks() {
		return _ticks;
	}

	public Treasure getTreasure() {
		return _treasure;
	}

	public boolean isRunning() {
		return _running;
	}

	protected abstract void onFinish();

	public void run() {
		tick();
		_ticks++;
	}

	protected abstract void tick();

}
