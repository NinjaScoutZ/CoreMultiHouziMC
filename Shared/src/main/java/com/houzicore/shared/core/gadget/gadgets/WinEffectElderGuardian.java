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

public class WinEffectElderGuardian extends WinEffectGadget {

	public WinEffectElderGuardian(GadgetManager manager) {
		super(manager, "Elder Guardian",
				new String[] { C.cWhite + "Flood the stage with an", C.cWhite + "ancient deep-sea warning." }, -2,
				Material.PRISMARINE_CRYSTALS, (byte) 0, CosmeticRarity.MYTHIC);
	}

	@Override
	public void playEffect(Player player) {
		var base = getEffectLocation(player);
		new BukkitRunnable() {
			int ticks = 0;

			@Override
			public void run() {
				if (ticks >= 44 || !player.isOnline()) {
					cancel();
					return;
				}

				var center = base.clone().add(0, 0.4, 0);
				double radius = 1.0 + (ticks * 0.03);
				UtilParticle.playParticleRing(UtilParticle.ParticleType.BUBBLE, center, radius, 20, UtilParticle.ViewDist.NORMAL);
				UtilParticle.playParticleRing(UtilParticle.ParticleType.GLOW, center.clone().add(0, 0.65, 0), radius * 0.75, 14, UtilParticle.ViewDist.NORMAL);
				base.getWorld().spawnParticle(org.bukkit.Particle.SPLASH, center, 8, 0.45, 0.35, 0.45, 0.04);
				if (ticks == 0) {
					base.getWorld().playSound(base, Sound.ENTITY_ELDER_GUARDIAN_CURSE, 1f, 1.0f);
				}
				if (ticks % 10 == 0) {
					base.getWorld().playSound(base, Sound.BLOCK_BUBBLE_COLUMN_WHIRLPOOL_INSIDE, 0.7f, 1.1f);
				}
				ticks++;
			}
		}.runTaskTimer(Manager.getPlugin(), 0L, 2L);
	}
}
