package com.houzicore.shared.core.friend.data;

import com.houzicore.shared.core.friend.FriendStatusType;

public class FriendStatus {
	public String Name;
	public java.util.UUID Uuid;
	public String ServerName;
	public boolean Online;
	public boolean Favorite;
	/**
	 * This seems like it should be unmodified without current time subtracted when
	 * set
	 */
	public long LastSeenOnline;
	public FriendStatusType Status;
}
