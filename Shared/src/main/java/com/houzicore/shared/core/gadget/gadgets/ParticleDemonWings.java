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

public class ParticleDemonWings extends ParticleGadget {

	public ParticleDemonWings(GadgetManager manager) {
		super(manager, "Demon Wings",
				new String[] { C.cWhite + "Spread darker wings with", C.cWhite + "embers and smoke behind you." }, -2,
				Material.BAT_SPAWN_EGG, (byte) 0);
	}

	@EventHandler
	public void playParticle(UpdateEvent event) {
		if (event.getType() != UpdateType.TICK) {
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

			double timeOffset = (player.getUniqueId().hashCode() & 0xFFFF) / 1000.0;
			double time = (System.currentTimeMillis() / 1000.0) + timeOffset;
			double flapSpeed = player.isSprinting() ? 10.0 : (player.isSneaking() ? 3.0 : 6.0);
			double flapAngle = Math.toRadians(25) * Math.sin(time * flapSpeed);
			double bob = Math.sin(time * flapSpeed * 0.5) * 0.05;

			var base = player.getLocation().clone().add(0, 1.35 + bob, 0).subtract(forward.clone().multiply(0.15));

			for (int wing = -1; wing <= 1; wing += 2) {
				double currentFlapAngle = flapAngle * wing;
				for (int i = 0; i < 8; i++) {
					double progress = i / 7.0;
					double spread = 0.3 + (progress * 1.3);
					double rise = 0.08 + Math.sin(progress * Math.PI) * 0.65;
					double back = 0.04 + (progress * 0.34);
					double claw = Math.cos(progress * Math.PI) * 0.12;

					Vector offset = side.clone().multiply((spread + claw) * wing)
							.add(new Vector(0, rise, 0))
							.subtract(forward.clone().multiply(back));

					offset.rotateAroundAxis(forward, currentFlapAngle);

					var point = base.clone().add(offset);
					point.getWorld().spawnParticle(Particle.SMOKE, point, 1, 0.005, 0.005, 0.005, 0.0);
					point.getWorld().spawnParticle(Particle.DUST, point, 1, 0.0, 0.0, 0.0, 0.0,
							new Particle.DustOptions(Color.fromRGB(180, 32, 32), 1.0f));
				}
			}
		}
	}
}
