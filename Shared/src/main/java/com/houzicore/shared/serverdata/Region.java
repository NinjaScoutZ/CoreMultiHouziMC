package com.houzicore.shared.serverdata;

import java.io.File;

/**
 * Region enumerates the various geographical regions where HouziCore servers are
 * hosted.
 * @author Ty
 *
 */
public enum Region
{
	ASIA,
	TH,
	ALL;
	
	/**
	 * @return the geographical {@link Region} of the current running process.
	 */
	public static Region currentRegion()
	{
		return !new File("th.dat").exists() ? Region.ASIA : Region.TH;
	}
}
