package com.houzicore.shared.core.gadget.gadgets;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.util.Vector;

import com.houzicore.shared.common.util.C;
import com.houzicore.shared.common.util.UtilBlock;
import com.houzicore.shared.common.util.UtilEnt;
import com.houzicore.shared.common.util.UtilMath;
import com.houzicore.shared.common.util.UtilParticle;
import com.houzicore.shared.common.util.UtilServer;
import com.houzicore.shared.common.util.UtilTime;
import com.houzicore.shared.common.util.UtilParticle.ParticleType;
import com.houzicore.shared.common.util.UtilParticle.ViewDist;
import com.houzicore.shared.core.gadget.types.ParticleGadget;
import com.houzicore.shared.updater.UpdateType;
import com.houzicore.shared.updater.event.UpdateEvent;
import com.houzicore.shared.core.gadget.GadgetManager;

public class ParticleFoot extends ParticleGadget {
	private boolean _foot = false;

	private final HashMap<Location, Long> _steps = new HashMap<>();

	public ParticleFoot(GadgetManager manager) {
		super(manager, "Shadow Walk",
				new String[] { C.cWhite + "In a world where footprints", C.cWhite + "do not exist, leaving your",
						C.cWhite + "shadow behind is the next", C.cWhite + "best thing.", },
				-2, Material.LEATHER_BOOTS, (byte) 0);
	}

	public void cleanSteps() {
		if (_steps.isEmpty())
			return;

		final Iterator<Entry<Location, Long>> stepIterator = _steps.entrySet().iterator();

		while (stepIterator.hasNext()) {
			final Entry<Location, Long> entry = stepIterator.next();

			if (UtilTime.elapsed(entry.getValue(), 10000)) {
				stepIterator.remove();
			}
		}
	}

	public boolean nearStep(Location loc) {
		for (final Location other : _steps.keySet()) {
			if (UtilMath.offset(loc, other) < 0.3)
				return true;
		}

		return false;
	}

	@EventHandler
	public void playParticle(UpdateEvent event) {
		if (event.getType() != UpdateType.FASTEST)
			return;

		_foot = !_foot;

		cleanSteps();

		for (final Player player : GetActive()) {
			if (!shouldDisplay(player)) {
				continue;
			}

			if (!Manager.isMoving(player)) {
				continue;
			}

			if (!UtilEnt.isGrounded(player)) {
				continue;
			}

			Vector offset;

			final Vector dir = player.getLocation().getDirection();
			dir.setY(0);
			dir.normalize();

			if (_foot) {
				offset = new Vector(dir.getZ() * -1, 0.1, dir.getX());
			} else {
				offset = new Vector(dir.getZ(), 0.1, dir.getX() * -1);
			}

			final Location loc = player.getLocation().add(offset.multiply(0.2));

			if (nearStep(loc)) {
				continue;
			}

			if (!UtilBlock.solid(loc.getBlock().getRelative(BlockFace.DOWN))) {
				continue;
			}

			_steps.put(loc, System.currentTimeMillis());

			UtilParticle.PlayParticle(ParticleType.FOOTSTEP, loc, 0f, 0f, 0f, 0, 1, ViewDist.NORMAL,
					UtilServer.getPlayers());

			UtilParticle.PlayParticle(ParticleType.LARGE_SMOKE, loc.clone().add(0, 0.1, 0), 0f, 0f, 0f, 0, 1,
					ViewDist.NORMAL, UtilServer.getPlayers());
		}
	}
}
