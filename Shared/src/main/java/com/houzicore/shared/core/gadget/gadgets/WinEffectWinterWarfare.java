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

public class WinEffectWinterWarfare extends WinEffectGadget {

	public WinEffectWinterWarfare(GadgetManager manager) {
		super(manager, "Winter Warfare",
				new String[] { C.cWhite + "Santa isn't only packing coal", C.cWhite + "for the bad girls and boys this year!" }, -2,
				Material.TNT, (byte) 0, CosmeticRarity.LEGENDARY);
	}

	@Override
	public void playEffect(Player player) {
		pasteSchematic(player, "WinterWarfareSleigh");
		var base = getEffectLocation(player);

		base.getWorld().playSound(base, Sound.ENTITY_GENERIC_EXPLODE, 1.0f, 1.0f);

		new BukkitRunnable() {
			int ticks = 0;

			@Override
			public void run() {
				if (ticks >= 40 || !player.isOnline()) {
					cancel();
					return;
				}

				double radius = 1.0;
				var center = base.clone().add(0, 1.5, 0);
				UtilParticle.playParticleRing(UtilParticle.ParticleType.FIREWORKS_SPARK, center, radius, 8, UtilParticle.ViewDist.NORMAL);
				if (ticks % 8 == 0) {
					base.getWorld().playSound(base, Sound.ENTITY_TNT_PRIMED, 0.7f, 1.3f);
				}
				ticks++;
			}
		}.runTaskTimer(Manager.getPlugin(), 0L, 2L);
	}
}
