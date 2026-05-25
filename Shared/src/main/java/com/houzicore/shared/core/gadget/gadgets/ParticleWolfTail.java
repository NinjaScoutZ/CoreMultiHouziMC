package com.houzicore.shared.core.gadget.gadgets;

import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.util.Vector;

import com.houzicore.shared.common.util.C;
import com.houzicore.shared.core.gadget.GadgetManager;
import com.houzicore.shared.core.gadget.types.ParticleGadget;
import com.houzicore.shared.updater.UpdateType;
import com.houzicore.shared.updater.event.UpdateEvent;

public class ParticleWolfTail extends ParticleGadget {

	public ParticleWolfTail(GadgetManager manager) {
		super(manager, "Wolf Tail",
				new String[] { C.cWhite + "Trail a softer silver tail", C.cWhite + "that follows your movement." }, -2,
				Material.GRAY_DYE, (byte) 0);
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

			Vector forward = player.getLocation().getDirection().setY(0).normalize();
			if (forward.lengthSquared() == 0) {
				forward = new Vector(0, 0, 1);
			}
			Vector side = new Vector(-forward.getZ(), 0, forward.getX()).normalize();
			double sway = Math.sin(player.getTicksLived() * 0.16) * 0.18;
			var base = player.getLocation().clone().add(0, 0.68, 0).subtract(forward.clone().multiply(0.34));

			for (int i = 0; i < 5; i++) {
				double progress = i / 4.0;
				var point = base.clone()
						.add(side.clone().multiply(sway * progress))
						.subtract(forward.clone().multiply(progress * 0.15))
						.add(0, progress * 0.16, 0);
				point.getWorld().spawnParticle(Particle.DUST, point, 1, 0, 0, 0, 0,
						new Particle.DustOptions(i == 4 ? Color.WHITE : Color.fromRGB(180, 180, 180), 1.0f));
			}
		}
	}
}
