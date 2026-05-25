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

public class WinEffectSolarFlare extends WinEffectGadget {
	public WinEffectSolarFlare(GadgetManager manager) {
		super(manager, "Solar Flare",
				new String[] { C.cWhite + "Burst bright rings of light", C.cWhite + "out from the winner platform." },
				-2, Material.BLAZE_POWDER, (byte) 0, CosmeticRarity.LEGENDARY);
	}

	@Override
	public void playEffect(Player player) {
		org.bukkit.Location base = getEffectLocation(player);
		new BukkitRunnable() {
			int wave = 0;

			@Override
			public void run() {
				if (wave >= 8 || !player.isOnline()) {
					cancel();
					return;
				}

				var center = base.clone().add(0, 0.2, 0);
				double radius = 0.8 + wave * 0.35;

				UtilParticle.playParticleRing(UtilParticle.ParticleType.FLAME, center, radius, 22,
						UtilParticle.ViewDist.NORMAL);
				UtilParticle.playParticleRing(UtilParticle.ParticleType.END_ROD, center.clone().add(0, 0.3, 0), radius,
						18, UtilParticle.ViewDist.NORMAL);
				base.getWorld().playSound(center, Sound.BLOCK_BEACON_POWER_SELECT, 0.7f, 1.2f + (wave * 0.03f));

				wave++;
			}
		}.runTaskTimer(Manager.getPlugin(), 0L, 5L);
	}
}
