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

public class ParticleHalo extends ParticleGadget {

	public ParticleHalo(GadgetManager manager) {
		super(manager, "Angelic Halo",
				new String[] { C.cWhite + "A divine golden ring", C.cWhite + "floats above your head.", },
				-2, Material.GOLD_NUGGET, (byte) 0);
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
				// Simplified effect while moving
				UtilParticle.PlayParticle(ParticleType.FIREWORKS_SPARK, player.getLocation().add(0, 2.2, 0), 0.2f, 0.2f, 0.2f, 0.05f,
						1, ViewDist.NORMAL, UtilServer.getPlayers());
			} else {
				// Circle Trigonometry
				double time = player.getTicksLived() * 0.15;
				double radius = 0.4;
				double bob = Math.sin(player.getTicksLived() * 0.1) * 0.1;
				
				for (int i = 0; i < 360; i += 30) {
					// Add time offset for spinning effect
					double angle = Math.toRadians(i + (time * 50));
					double x = Math.cos(angle) * radius;
					double z = Math.sin(angle) * radius;
					
					UtilParticle.PlayParticle(ParticleType.FLAME,
							player.getLocation().add(x, 2.1 + bob, z), 0f, 0f, 0f, 0, 1,
							ViewDist.NORMAL, UtilServer.getPlayers());
				}
				
				// A center sparkle
				if (player.getTicksLived() % 5 == 0) {
				    UtilParticle.PlayParticle(ParticleType.FIREWORKS_SPARK,
							player.getLocation().add(0, 2.1 + bob, 0), 0f, 0f, 0f, 0.02f, 1,
							ViewDist.NORMAL, UtilServer.getPlayers());
                }
			}
		}
	}
}
