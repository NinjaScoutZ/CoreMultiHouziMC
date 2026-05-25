package com.houzicore.shared.core.friend;

import java.util.Comparator;

import com.houzicore.shared.core.friend.data.FriendStatus;

public class FriendSorter implements Comparator<FriendStatus> {
	@Override
	public int compare(FriendStatus a, FriendStatus b) {
		if (a.Favorite && !b.Favorite)
			return -1;
		if (b.Favorite && !a.Favorite)
			return 1;

		if (a.Online && !b.Online)
			return -1;
		if (b.Online && !a.Online)
			return 1;

		// If online we sort by mutual
		if (a.Online && b.Online) {
			if (a.Status == FriendStatusType.Accepted && b.Status != FriendStatusType.Accepted)
				return -1;
			else if (b.Status == FriendStatusType.Accepted && a.Status != FriendStatusType.Accepted)
				return 1;

			return a.Name.compareToIgnoreCase(b.Name);
		}

		if (a.LastSeenOnline < b.LastSeenOnline)
			return -1;

		if (b.LastSeenOnline < a.LastSeenOnline)
			return 1;

		return 0;
	}
}
