package com.houzicore.shared.core.friend.data;

import java.util.ArrayList;

public class FriendData {
	private ArrayList<FriendStatus> _friends = new ArrayList<>();

	public ArrayList<FriendStatus> getFriends() {
		return _friends;
	}

	public void setFriends(ArrayList<FriendStatus> newFriends) {
		_friends = newFriends;
	}
}
