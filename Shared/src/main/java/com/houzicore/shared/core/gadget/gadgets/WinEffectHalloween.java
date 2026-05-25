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

public class WinEffectHalloween extends WinEffectGadget {

	public WinEffectHalloween(GadgetManager manager) {
		super(manager, "Pumpkin King",
				new String[] { C.cWhite + "Rise of the Pumpkin King", C.cWhite + "in a spooky Halloween room." }, -2,
				Material.JACK_O_LANTERN, (byte) 0, CosmeticRarity.LEGENDARY);
	}

	@Override
	public void playEffect(Player player) {
		pasteSchematic(player, "HalloweenRoom");
		var base = getEffectLocation(player);

		base.getWorld().playSound(base, Sound.ENTITY_WITHER_SPAWN, 1.0f, 0.8f);

		new BukkitRunnable() {
			int ticks = 0;

			@Override
			public void run() {
				if (ticks >= 40 || !player.isOnline()) {
					cancel();
					return;
				}

				double radius = 1.2;
				var center = base.clone().add(0, 1.5, 0);
				UtilParticle.playParticleRing(UtilParticle.ParticleType.FLAME, center, radius, 12, UtilParticle.ViewDist.NORMAL);
				if (ticks % 10 == 0) {
					base.getWorld().playSound(base, Sound.ENTITY_SKELETON_AMBIENT, 0.8f, 0.9f);
				}
				ticks++;
			}
		}.runTaskTimer(Manager.getPlugin(), 0L, 2L);
	}
}
