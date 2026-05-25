package com.houzicore.shared.core.gadget.gadgets;

import org.bukkit.Color;
import org.bukkit.FireworkEffect;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import com.houzicore.shared.common.util.C;
import com.houzicore.shared.common.util.UtilFirework;
import com.houzicore.shared.core.gadget.CosmeticRarity;
import com.houzicore.shared.core.gadget.GadgetManager;
import com.houzicore.shared.core.gadget.types.WinEffectGadget;

public class WinEffectPartyAnimal extends WinEffectGadget {
	public WinEffectPartyAnimal(GadgetManager manager) {
		super(manager, "Party Animal",
				new String[] { C.cWhite + "Turn the winner scene into", C.cWhite + "a loud confetti celebration." }, -2,
				Material.CAKE, (byte) 0, CosmeticRarity.RARE);
	}

	@Override
	public void playEffect(Player player) {
		org.bukkit.Location base = getEffectLocation(player);
		new BukkitRunnable() {
			int bursts = 0;

			@Override
			public void run() {
				if (bursts >= 10 || !player.isOnline()) {
					cancel();
					return;
				}

				var center = base.clone().add(0, 1.4, 0);
				base.getWorld().spawnParticle(org.bukkit.Particle.FIREWORK, center, 20, 0.65, 0.45, 0.65, 0.02);
				base.getWorld().spawnParticle(org.bukkit.Particle.NOTE, center, 12, 0.7, 0.4, 0.7, 1);
				base.getWorld().playSound(center, Sound.ENTITY_FIREWORK_ROCKET_TWINKLE, 1f, 1.4f);

				UtilFirework.playFirework(center,
						FireworkEffect.builder().with(FireworkEffect.Type.BALL_LARGE).trail(true).flicker(true)
								.withColor(Color.YELLOW, Color.AQUA, Color.FUCHSIA).build());
				bursts++;
			}
		}.runTaskTimer(Manager.getPlugin(), 0L, 6L);
	}
}
