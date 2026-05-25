package com.houzicore.shared.core.gadget.gadgets;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;

import com.houzicore.shared.common.util.C;
import com.houzicore.shared.common.util.UtilParticle;
import com.houzicore.shared.common.util.UtilServer;
import com.houzicore.shared.common.util.UtilParticle.ParticleType;
import com.houzicore.shared.common.util.UtilParticle.ViewDist;
import com.houzicore.shared.core.gadget.GadgetManager;
import com.houzicore.shared.core.gadget.types.ParticleGadget;
import com.houzicore.shared.updater.UpdateType;
import com.houzicore.shared.updater.event.UpdateEvent;

public class ParticleMusicNotes extends ParticleGadget {
	public ParticleMusicNotes(GadgetManager manager) {
		super(manager, "Music Notes",
				new String[] { C.cWhite + "Let bright notes drift around you", C.cWhite + "while you wander the lobby." },
				-2, Material.JUKEBOX, (byte) 0);
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

			UtilParticle.PlayParticle(ParticleType.NOTE, player.getLocation().add(0, 1.6, 0), 0.35f, 0.35f, 0.35f, 1f,
					Manager.isMoving(player) ? 3 : 5, ViewDist.NORMAL, UtilServer.getPlayers());
		}
	}
}
