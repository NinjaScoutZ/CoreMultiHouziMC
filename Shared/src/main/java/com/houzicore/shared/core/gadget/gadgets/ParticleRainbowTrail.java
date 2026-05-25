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

public class ParticleRainbowTrail extends ParticleGadget {
	private static final Color[] COLORS = new Color[] { Color.RED, Color.ORANGE, Color.YELLOW, Color.LIME, Color.AQUA,
			Color.BLUE, Color.FUCHSIA };

	public ParticleRainbowTrail(GadgetManager manager) {
		super(manager, "Rainbow Trail",
				new String[] { C.cWhite + "Paint your movement path", C.cWhite + "with a bright rainbow wake." }, -2,
				Material.RED_DYE, (byte) 0);
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

			if (!Manager.isMoving(player)) {
				continue;
			}

			Vector direction = player.getLocation().getDirection().normalize().multiply(-0.45);
			var base = player.getLocation().add(direction).add(0, 0.15, 0);
			int startIndex = Math.floorMod(player.getTicksLived() / 2, COLORS.length);

			for (int i = 0; i < 3; i++) {
				Color color = COLORS[(startIndex + i) % COLORS.length];
				base.getWorld().spawnParticle(Particle.DUST, base.clone().add(0, i * 0.08, 0), 3, 0.08, 0.03, 0.08, 0,
						new Particle.DustOptions(color, 1.1f));
			}
		}
	}
}
