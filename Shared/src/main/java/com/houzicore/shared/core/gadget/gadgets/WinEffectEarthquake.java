package com.houzicore.shared.core.gadget.gadgets;

import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import com.houzicore.shared.common.util.C;
import com.houzicore.shared.common.util.UtilParticle;
import com.houzicore.shared.core.gadget.CosmeticRarity;
import com.houzicore.shared.core.gadget.GadgetManager;
import com.houzicore.shared.core.gadget.types.WinEffectGadget;

public class WinEffectEarthquake extends WinEffectGadget {

	public WinEffectEarthquake(GadgetManager manager) {
		super(manager, "Earthquake",
				new String[] { C.cWhite + "Shake the winner platform", C.cWhite + "with heavy ground shockwaves." }, -2,
				Material.COARSE_DIRT, (byte) 0, CosmeticRarity.RARE);
	}

	@Override
	public void playEffect(Player player) {
		var base = getEffectLocation(player);
		new BukkitRunnable() {
			int pulse = 0;

			@Override
			public void run() {
				if (pulse >= 7 || !player.isOnline()) {
					cancel();
					return;
				}

				double radius = 1.0 + (pulse * 0.55);
				var center = base.clone().add(0, 0.1, 0);
				UtilParticle.playParticleRing(UtilParticle.ParticleType.CLOUD, center, radius, 20, UtilParticle.ViewDist.NORMAL);
				UtilParticle.playParticleRing(UtilParticle.ParticleType.LARGE_SMOKE, center.clone().add(0, 0.05, 0), radius, 16, UtilParticle.ViewDist.NORMAL);
				base.getWorld().playSound(center, Sound.BLOCK_ANVIL_LAND, 0.65f, 0.8f + (pulse * 0.03f));
				pulse++;
			}
		}.runTaskTimer(Manager.getPlugin(), 0L, 5L);
	}
}
