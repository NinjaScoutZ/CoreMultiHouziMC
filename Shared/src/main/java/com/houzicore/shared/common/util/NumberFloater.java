package com.houzicore.shared.common.util;

public class NumberFloater {
	private double _min;
	private double _max;
	private double _modifyPerCall;

	private double _cur;
	private boolean _up;

	public NumberFloater(double min, double max, double modify) {
		_min = min;
		_max = max;
		_modifyPerCall = modify;
	}

	public double pulse() {
		if (_up && (_cur = Math.max(_min, Math.min(_max, _cur += _modifyPerCall))) >= _max)
			_up = false;
		else if ((_cur = Math.max(_min, Math.min(_max, _cur -= _modifyPerCall))) <= _min)
			_up = true;

		return _cur;
	}
}
