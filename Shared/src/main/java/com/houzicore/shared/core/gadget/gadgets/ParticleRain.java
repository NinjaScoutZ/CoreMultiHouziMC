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

public class ParticleRain extends ParticleGadget {

	public ParticleRain(GadgetManager manager) {
		super(manager, "Rain Cloud", new String[] { C.cWhite + "Your very own rain cloud!",
				C.cWhite + "Now you never have to worry", C.cWhite + "about not being wet. Woo...", }, -2,
				Material.INK_SAC, (byte) 4);
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
				UtilParticle.PlayParticle(ParticleType.SPLASH, player.getLocation().add(0, 1, 0), 0.2f, 0.2f, 0.2f, 0,
						4, ViewDist.NORMAL, UtilServer.getPlayers());
			} else {
				UtilParticle.PlayParticle(ParticleType.EXPLODE, player.getLocation().add(0, 3.5, 0), 0.6f, 0f, 0.6f, 0,
						8, ViewDist.NORMAL, player);

				for (final Player other : UtilServer.getPlayers())
					if (!player.equals(other)) {
						UtilParticle.PlayParticle(ParticleType.CLOUD, player.getLocation().add(0, 3.5, 0), 0.6f, 0.1f,
								0.6f, 0, 8, ViewDist.NORMAL, other);
					}

				UtilParticle.PlayParticle(ParticleType.DRIP_WATER, player.getLocation().add(0, 3.5, 0), 0.4f, 0.1f,
						0.4f, 0, 2, ViewDist.NORMAL, UtilServer.getPlayers());

				// Sound
				player.getWorld().playSound(player.getLocation(), Sound.WEATHER_RAIN, 0.1f, 1f);
			}
		}
	}
}
