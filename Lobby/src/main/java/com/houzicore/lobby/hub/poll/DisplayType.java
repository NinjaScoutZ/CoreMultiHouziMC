package com.houzicore.lobby.hub.poll;

import com.houzicore.shared.common.Rank;

public enum DisplayType
{
	ALL,
	RANKED,
	PREMIUM,
	NORMAL;

	public boolean shouldDisplay(Rank rank)
	{
		switch (this)
		{
			case PREMIUM:
				return rank.Has(Rank.WARRIOR);
			case NORMAL:
				return !rank.Has(Rank.WARRIOR);
			default:
				return true;
		}
	}
}
