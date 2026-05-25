package com.houzicore.shared.core.gadget.gadgets;

import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;

import com.houzicore.shared.common.util.C;
import com.houzicore.shared.core.gadget.GadgetManager;
import com.houzicore.shared.core.gadget.types.ParticleGadget;
import com.houzicore.shared.updater.UpdateType;
import com.houzicore.shared.updater.event.UpdateEvent;

public class ParticleYinYang extends ParticleGadget {

	public ParticleYinYang(GadgetManager manager) {
		super(manager, "Yin Yang",
				new String[] { C.cWhite + "Orbit two opposite lights", C.cWhite + "in a balanced symbol around you." },
				-2, Material.BLACK_DYE, (byte) 0);
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

			double angle = player.getTicksLived() * 0.16;
			var center = player.getLocation().clone().add(0, 1.05, 0);
			var first = center.clone().add(Math.cos(angle) * 0.7, 0.0, Math.sin(angle) * 0.7);
			var second = center.clone().add(Math.cos(angle + Math.PI) * 0.7, 0.0, Math.sin(angle + Math.PI) * 0.7);
			first.getWorld().spawnParticle(Particle.DUST, first, 1, 0, 0, 0, 0, new Particle.DustOptions(Color.WHITE, 1.1f));
			second.getWorld().spawnParticle(Particle.DUST, second, 1, 0, 0, 0, 0, new Particle.DustOptions(Color.BLACK, 1.1f));
			center.getWorld().spawnParticle(Particle.END_ROD, center.clone().add(0, 0.18, 0), 1, 0.02, 0.02, 0.02, 0.0);
		}
	}
}
