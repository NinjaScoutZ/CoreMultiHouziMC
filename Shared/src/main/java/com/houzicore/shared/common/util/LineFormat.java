package com.houzicore.shared.common.util;

public enum LineFormat {
	LORE(40),
	CHAT(55);

	private final int _length;

	LineFormat(int length) {
		_length = length;
	}

	public int getLength() {
		return _length;
	}
}
