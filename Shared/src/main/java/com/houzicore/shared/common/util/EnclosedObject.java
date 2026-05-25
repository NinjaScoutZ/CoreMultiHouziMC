package com.houzicore.shared.common.util;

public class EnclosedObject<T> {
	private T _value;

	public EnclosedObject() {
		this((T) null);
	}

	public EnclosedObject(T t) {
		_value = t;
	}

	public T Get() {
		return _value;
	}

	public T Set(T value) {
		return _value = value;
	}

	@Override
	public String toString() {
		return _value == null ? "null" : _value.toString();
	}
}
