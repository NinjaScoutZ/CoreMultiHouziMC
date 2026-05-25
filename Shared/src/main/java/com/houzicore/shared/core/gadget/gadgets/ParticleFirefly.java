package com.houzicore.shared.core.gadget.gadgets;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;

import com.houzicore.shared.common.util.C;
import com.houzicore.shared.common.util.UtilParticle;
import com.houzicore.shared.common.util.UtilServer;
import com.houzicore.shared.common.util.UtilParticle.ParticleType;
import com.houzicore.shared.common.util.UtilParticle.ViewDist;
import com.houzicore.shared.core.gadget.types.ParticleGadget;
import com.houzicore.shared.updater.UpdateType;
import com.houzicore.shared.updater.event.UpdateEvent;
import com.houzicore.shared.core.gadget.GadgetManager;

public class ParticleFirefly extends ParticleGadget {
	public ParticleFirefly(GadgetManager manager) {
		super(manager, "Fireflies",
				new String[] { C.cWhite + "Tiny sources of light", C.cWhite + "dancing merrily around you." },
				-2, Material.LANTERN, (byte) 0);
	}

	@EventHandler
	public void playParticle(UpdateEvent event) {
		if (event.getType() != UpdateType.TICK)
			return;

		for (final Player player : GetActive()) {
			if (!shouldDisplay(player)) {
				continue;
			}

			if (Manager.isMoving(player)) {
				UtilParticle.PlayParticle(ParticleType.GLOW, player.getLocation().add(0, 1, 0), 0.5f, 0.5f,
						0.5f, 0, 2, ViewDist.NORMAL, UtilServer.getPlayers());
			} else {
				UtilParticle.PlayParticle(ParticleType.GLOW, player.getLocation().add(0, 1, 0), 0.8f, 0.5f,
						0.8f, 0.05f, 5, ViewDist.NORMAL, UtilServer.getPlayers());
			}
		}
	}
}
