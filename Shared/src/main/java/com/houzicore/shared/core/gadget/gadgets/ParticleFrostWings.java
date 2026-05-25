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

public class ParticleFrostWings extends ParticleGadget {

	private static final String[] PATTERN = {
			"----xxx---",
			"---xxxxx--",
			"--xxxxxxx-",
			"-xxxxxxxx-",
			"xxxxxxxxxx",
			"xxxxxxxxxx",
			"xxxxxxxxxx",
			"xxxxxxxxxx",
			"--xxxxxxxx",
			"---xxxxxxx",
			"---xxxxxxx",
			"----xxxxxx",
			"----xxxxxx",
			"-----xxxx-",
			"-----xxxx-",
			"------xxx-",
			"------xxx-",
			"-------xx-",
			"--------x-"
	};

	public ParticleFrostWings(GadgetManager manager) {
		super(manager, "Frost Wings",
				new String[] { C.cWhite + "Equip beautiful wings", C.cWhite + "made of icy frost." }, -2,
				Material.DIAMOND, (byte) 0);
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

			var base = player.getLocation().clone().add(0, 1.6 + bob, 0).subtract(forward.clone().multiply(0.2));
			double scale = 0.12; // distance between particles

			for (int wing = -1; wing <= 1; wing += 2) {
				double currentFlapAngle = flapAngle * wing;
				for (int row = 0; row < PATTERN.length; row++) {
					String line = PATTERN[row];
					for (int col = 0; col < line.length(); col++) {
						if (line.charAt(col) == 'x') {
							// col is horizontal distance, row is vertical (downwards)
							double spread = 0.15 + (col * scale);
							double rise = -(row * scale);
							double back = 0.02 + (col * 0.03); // slightly pushed back as it goes outwards

							Vector offset = side.clone().multiply(spread * wing)
									.add(new Vector(0, rise, 0))
									.subtract(forward.clone().multiply(back));

							offset.rotateAroundAxis(forward, currentFlapAngle);

							var point = base.clone().add(offset);
							point.getWorld().spawnParticle(Particle.DUST, point, 1, 0.0, 0.0, 0.0, 0.0,
									new Particle.DustOptions(Color.fromRGB(3, 252, 244), 0.8f));
						}
					}
				}
			}
		}
	}
}
