package com.houzicore.shared.core.gadget.gadgets;

import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import com.houzicore.shared.common.util.C;
import com.houzicore.shared.core.gadget.CosmeticRarity;
import com.houzicore.shared.core.gadget.GadgetManager;
import com.houzicore.shared.core.gadget.types.WinEffectGadget;

public class WinEffectLavaTrap extends WinEffectGadget {

	public WinEffectLavaTrap(GadgetManager manager) {
		super(manager, "Lava Trap",
				new String[] { C.cWhite + "Raise a hot lava cage", C.cWhite + "around the winner platform." }, -2,
				Material.LAVA_BUCKET, (byte) 0, CosmeticRarity.LEGENDARY);
	}

	@Override
	public void playEffect(Player player) {
		var base = getEffectLocation(player);
		new BukkitRunnable() {
			int ticks = 0;

			@Override
			public void run() {
				if (ticks >= 32 || !player.isOnline()) {
					cancel();
					return;
				}

				double progress = ticks / 31.0;
				double height = 0.4 + (progress * 2.2);
				double radius = 1.3;
				for (int i = 0; i < 4; i++) {
					double angle = Math.PI / 2.0 * i;
					var point = base.clone().add(Math.cos(angle) * radius, height, Math.sin(angle) * radius);
					base.getWorld().spawnParticle(org.bukkit.Particle.FLAME, point, 4, 0.08, 0.15, 0.08, 0.02);
					base.getWorld().spawnParticle(org.bukkit.Particle.LAVA, point, 1, 0.03, 0.05, 0.03, 0.0);
					base.getWorld().spawnParticle(org.bukkit.Particle.SMOKE, point, 1, 0.06, 0.08, 0.06, 0.01);
				}

				if (ticks % 8 == 0) {
					base.getWorld().playSound(base, Sound.BLOCK_LAVA_POP, 0.8f, 1.1f);
				}
				ticks++;
			}
		}.runTaskTimer(Manager.getPlugin(), 0L, 2L);
	}
}
