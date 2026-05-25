package com.houzicore.shared.core.gadget.gadgets;

import org.bukkit.Material;
import org.bukkit.Sound;
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

public class ParticleFireRings extends ParticleGadget {

	public ParticleFireRings(GadgetManager manager) {
		super(manager, "Flame Rings", new String[] { C.cWhite + "Forged from the burning ashes",
				C.cWhite + "of 1000 Blazes by the infamous", C.cWhite + "Flame King of the Nether realm.", }, -2,
				Material.BLAZE_POWDER, (byte) 0);
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
				UtilParticle.PlayParticle(ParticleType.FLAME, player.getLocation().add(0, 1f, 0), 0.2f, 0.2f, 0.2f, 0,
						1, ViewDist.NORMAL, UtilServer.getPlayers());
			} else {
				for (int i = 0; i < 1; i++) {
					final double lead = i * (2d * Math.PI / 2);

					final float x = (float) (Math.sin(player.getTicksLived() / 5d + lead) * 1f);
					final float z = (float) (Math.cos(player.getTicksLived() / 5d + lead) * 1f);

					final float y = (float) (Math.sin(player.getTicksLived() / 5d + lead) + 1f);

					UtilParticle.PlayParticle(ParticleType.FLAME, player.getLocation().add(x, y, z), 0f, 0f, 0f, 0, 1,
							ViewDist.NORMAL, UtilServer.getPlayers());
				}

				for (int i = 0; i < 1; i++) {
					final double lead = i * (2d * Math.PI / 2);

					final float x = (float) -(Math.sin(player.getTicksLived() / 5d + lead) * 1f);
					final float z = (float) (Math.cos(player.getTicksLived() / 5d + lead) * 1f);

					final float y = (float) (Math.sin(player.getTicksLived() / 5d + lead) + 1f);

					UtilParticle.PlayParticle(ParticleType.FLAME, player.getLocation().add(x, y, z), 0f, 0f, 0f, 0, 1,
							ViewDist.NORMAL, UtilServer.getPlayers());
				}

				// Sound
				player.getWorld().playSound(player.getLocation(), Sound.BLOCK_FIRE_AMBIENT, 0.2f, 1f);
			}
		}
	}
}
