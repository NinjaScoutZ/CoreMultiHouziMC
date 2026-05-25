package com.houzicore.shared.core.chat;

public class MessageData {
	private final String _message;
	private final long _timeSent;

	public MessageData(String message) {
		this(message, System.currentTimeMillis());
	}

	public MessageData(String message, long timeSent) {
		_message = message;
		_timeSent = timeSent;
	}

	public String getMessage() {
		return _message;
	}

	public long getTimeSent() {
		return _timeSent;
	}
}
