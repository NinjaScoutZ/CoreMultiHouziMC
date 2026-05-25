package com.houzicore.shared.core.gadget.gadgets;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;

import com.houzicore.shared.common.util.C;
import com.houzicore.shared.core.gadget.GadgetManager;
import com.houzicore.shared.core.gadget.types.ParticleGadget;
import com.houzicore.shared.updater.UpdateType;
import com.houzicore.shared.updater.event.UpdateEvent;

public class ParticleDeepSeaSwirl extends ParticleGadget {

	public ParticleDeepSeaSwirl(GadgetManager manager) {
		super(manager, "Deep Sea Swirl",
				new String[] { C.cWhite + "Carry a cool underwater spiral", C.cWhite + "around your movement path." },
				-2, Material.HEART_OF_THE_SEA, (byte) 0);
	}

	@EventHandler
	public void playParticle(UpdateEvent event) {
		if (event.getType() != UpdateType.FAST) {
			return;
		}

		for (Player player : GetActive()) {
			if (!shouldDisplay(player)) {
				continue;
			}

			double baseAngle = player.getTicksLived() * 0.22;
			for (int i = 0; i < 3; i++) {
				double angle = baseAngle + (i * Math.PI * 2 / 3);
				double radius = 0.45 + (i * 0.12);
				double y = 0.2 + (i * 0.28);
				var point = player.getLocation().clone().add(Math.cos(angle) * radius, y, Math.sin(angle) * radius);
				point.getWorld().spawnParticle(org.bukkit.Particle.BUBBLE, point, 1, 0.02, 0.02, 0.02, 0.0);
				point.getWorld().spawnParticle(org.bukkit.Particle.SPLASH, point, 1, 0.02, 0.02, 0.02, 0.01);
			}
		}
	}
}
