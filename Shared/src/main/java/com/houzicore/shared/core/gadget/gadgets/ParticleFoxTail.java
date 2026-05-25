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

public class ParticleFoxTail extends ParticleGadget {

	public ParticleFoxTail(GadgetManager manager) {
		super(manager, "Fox Tail",
				new String[] { C.cWhite + "Sway a bright orange tail", C.cWhite + "behind you with every step." }, -2,
				Material.ORANGE_DYE, (byte) 0);
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
			double sway = Math.sin(player.getTicksLived() * 0.18) * 0.22;
			var base = player.getLocation().clone().add(0, 0.65, 0).subtract(forward.clone().multiply(0.38));

			for (int i = 0; i < 5; i++) {
				double progress = i / 4.0;
				var point = base.clone()
						.add(side.clone().multiply(sway * progress))
						.subtract(forward.clone().multiply(progress * 0.18))
						.add(0, progress * 0.18, 0);
				point.getWorld().spawnParticle(Particle.DUST, point, 1, 0, 0, 0, 0,
						new Particle.DustOptions(i == 4 ? Color.WHITE : Color.fromRGB(255, 140, 48), 1.0f));
			}
		}
	}
}
