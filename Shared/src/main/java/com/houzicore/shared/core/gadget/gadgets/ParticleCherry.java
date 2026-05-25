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

public class ParticleCherry extends ParticleGadget {
	public ParticleCherry(GadgetManager manager) {
		super(manager, "Cherry Blossom",
				new String[] { C.cWhite + "Beautiful cherry blossoms", C.cWhite + "gently fall around you." },
				-2, Material.CHERRY_LEAVES, (byte) 0);
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
				UtilParticle.PlayParticle(ParticleType.CHERRY_LEAVES, player.getLocation().add(0, 1, 0), 0.4f, 0.4f,
						0.4f, 0.05f, 3, ViewDist.NORMAL, UtilServer.getPlayers());
			} else {
				UtilParticle.PlayParticle(ParticleType.CHERRY_LEAVES, player.getLocation().add(0, 2, 0), 0.5f, 0f,
						0.5f, -0.05f, 2, ViewDist.NORMAL, UtilServer.getPlayers());
			}
		}
	}
}
