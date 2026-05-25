package com.houzicore.shared.common.util;

import org.bukkit.Bukkit;

public class UtilTasks {
	
	/**
	 * Item 76: Ensure the executing thread is the primary Bukkit Server Thread.
	 * If not, throw an immediate IllegalStateException to prevent cascading asynchronous access errors.
	 */
	public static void ensureMainThread() {
		if (!Bukkit.getServer().isPrimaryThread()) {
			throw new IllegalStateException("This method must be executed on the main server thread!");
		}
	}
}
