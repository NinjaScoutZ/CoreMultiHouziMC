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

public class WinEffectTornado extends WinEffectGadget {
	public WinEffectTornado(GadgetManager manager) {
		super(manager, "Victory Tornado",
				new String[] { C.cWhite + "Surround the winner with", C.cWhite + "a twisting storm column." }, -2,
				Material.FEATHER, (byte) 0, CosmeticRarity.EPIC);
	}

	@Override
	public void playEffect(Player player) {
		org.bukkit.Location base = getEffectLocation(player);
		new BukkitRunnable() {
			double offset = 0;
			int ticks = 0;

			@Override
			public void run() {
				if (ticks >= 50 || !player.isOnline()) {
					cancel();
					return;
				}

				var origin = base.clone().add(0, 0.1, 0);
				UtilParticle.drawTornadoFrame(origin, UtilParticle.ParticleType.CLOUD, 1.45, 3.5, offset);
				UtilParticle.drawTornadoFrame(origin, UtilParticle.ParticleType.FIREWORKS_SPARK, 0.9, 2.4, -offset);

				if (ticks % 10 == 0) {
					base.getWorld().playSound(origin, Sound.ENTITY_WIND_CHARGE_WIND_BURST, 0.9f, 1.15f);
				}

				offset += 0.45;
				ticks++;
			}
		}.runTaskTimer(Manager.getPlugin(), 0L, 2L);
	}
}
