package com.houzicore.shared.core.gadget.gadgets;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.util.Vector;

import com.houzicore.shared.common.util.C;
import com.houzicore.shared.core.gadget.GadgetManager;
import com.houzicore.shared.core.gadget.types.ParticleGadget;
import com.houzicore.shared.updater.UpdateType;
import com.houzicore.shared.updater.event.UpdateEvent;

public class ParticleJetpack extends ParticleGadget {
	public ParticleJetpack(GadgetManager manager) {
		super(manager, "Jetpack Trail",
				new String[] { C.cWhite + "Leave a rocket wash behind you", C.cWhite + "every time you push forward." },
				-2, Material.FIRE_CHARGE, (byte) 0);
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

			Vector back = player.getLocation().getDirection().normalize().multiply(-0.35);
			var leftThruster = player.getLocation().add(back).add(0.18, 0.8, 0);
			var rightThruster = player.getLocation().add(back).add(-0.18, 0.8, 0);

			player.getWorld().spawnParticle(org.bukkit.Particle.FLAME, leftThruster, 2, 0.04, 0.04, 0.04, 0.01);
			player.getWorld().spawnParticle(org.bukkit.Particle.FLAME, rightThruster, 2, 0.04, 0.04, 0.04, 0.01);
			player.getWorld().spawnParticle(org.bukkit.Particle.SMOKE, leftThruster, 2, 0.05, 0.05, 0.05, 0.01);
			player.getWorld().spawnParticle(org.bukkit.Particle.SMOKE, rightThruster, 2, 0.05, 0.05, 0.05, 0.01);
		}
	}
}
