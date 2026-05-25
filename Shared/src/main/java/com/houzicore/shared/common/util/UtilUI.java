package com.houzicore.shared.common.util;

public class UtilUI {
	
	/**
	 * Calculates the centered slots for a given number of items in a standard 9-slot row.
	 * 
	 * @param count The number of items to center (max 9).
	 * @return An array of slot indexes perfectly centered on the first row.
	 */
	public static int[] getCenteredSlots(int count) {
		return getCenteredSlots(count, 0);
	}

	/**
	 * Calculates the centered slots for a given number of items on a specific row.
	 * 
	 * @param count The number of items to center (max 9).
	 * @param rowOffset The row index (0-based) to center the items on.
	 * @return An array of slot absolute indexes properly centered.
	 */
	public static int[] getCenteredSlots(int count, int rowOffset) {
		if (count <= 0) return new int[0];
		if (count > 9) count = 9;
		
		int[] slots = new int[count];
		// (9 - count) / 2 provides the perfect starting slot for centering
		int startIndex = (9 - count) / 2;
		
		for (int i = 0; i < count; i++) {
			slots[i] = (rowOffset * 9) + startIndex + i;
		}
		
		return slots;
	}
}
