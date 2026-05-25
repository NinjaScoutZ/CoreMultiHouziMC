package com.houzicore.shared.common.util;

import org.bukkit.util.Vector;

public interface VelocityReceiver {
	/**
	 * Allows custom entities to receive dynamic velocity inputs correctly.
	 * @param velocity The velocity vector applied to this entity.
	 */
	void receiveVelocity(Vector velocity);
}
