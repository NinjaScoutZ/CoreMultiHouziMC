package com.houzicore.shared.core.teleport;

import com.houzicore.shared.common.util.UtilPlayer;

import org.bukkit.Location;
import org.bukkit.entity.Player;

public class Teleporter {
	private final Player _pA;
	private final Location _loc;
	private final String _mA;

	private final Player _pB;
	private final String _mB;

	private final Teleport _tp;

	public Teleporter(Teleport teleport, Player pA, Player pB, String mA, String mB, Location loc, boolean record,
			String log) {
		_tp = teleport;
		_pA = pA;
		_pB = pB;
		_mA = mA;
		_mB = mB;
		_loc = loc;
	}

	public void doTeleport() {
		if (_loc == null)
			return;

		// Different Worlds
		/*
		 * if (!_pA.getWorld().getName().equals(_loc.getWorld().getName())) { if (_pB ==
		 * null) return;
		 * 
		 * _tp.UtilPlayer.message(_pB, F.main("Teleport", F.elem(_pA.getName()) +
		 * " is not in teleport destinations world.")); return; }
		 */

		// Player A
		if (_pA != null) {

			// Teleport
			_tp.TP(_pA, _loc);

			// Inform
			if (_mA != null) {
				UtilPlayer.message(_pA, _mA);
			}
		}

		// Player B
		if (_pB != null && _mB != null) {
			UtilPlayer.message(_pB, _mB);
		}
	}
}
