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

public class ParticleSculk extends ParticleGadget {
	public ParticleSculk(GadgetManager manager) {
		super(manager, "Sculk Soul",
				new String[] { C.cWhite + "A deep, dark resonance", C.cWhite + "emanates from your presence." },
				-2, Material.SCULK_SHRIEKER, (byte) 0);
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
				UtilParticle.PlayParticle(ParticleType.SCULK_SOUL, player.getLocation().add(0, 0.5, 0), 0.2f, 0.2f,
						0.2f, 0.05f, 2, ViewDist.NORMAL, UtilServer.getPlayers());
			} else {
				UtilParticle.PlayParticle(ParticleType.SCULK_SOUL, player.getLocation().add(0, 0.2, 0), 0.4f, 0.4f,
						0.4f, 0.05f, 4, ViewDist.NORMAL, UtilServer.getPlayers());
			}
		}
	}
}
