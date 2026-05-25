package com.houzicore.shared.core.gadget.gadgets;

import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import com.houzicore.shared.common.util.C;
import com.houzicore.shared.core.gadget.CosmeticRarity;
import com.houzicore.shared.core.gadget.GadgetManager;
import com.houzicore.shared.core.gadget.types.WinEffectGadget;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;

public class WinEffectPodium extends WinEffectGadget {

	public WinEffectPodium(GadgetManager manager) {
		super(manager, "Podium",
				new String[] { C.cWhite + "Summon a victory crown", C.cWhite + "above the winner platform." }, -2,
				Material.GOLD_BLOCK, (byte) 0, CosmeticRarity.LEGENDARY);
	}

	@Override
	public void playEffect(Player player) {
		pasteSchematic(player, "WinRoomPodium");
		var base = getEffectLocation(player);
		ArmorStand badge = base.getWorld().spawn(base.clone().add(0, 2.7, 0), ArmorStand.class);
		badge.setVisible(false);
		badge.setGravity(false);
		badge.setMarker(true);
		badge.setSmall(true);
		badge.customName(Component.text("MVP", NamedTextColor.GOLD, TextDecoration.BOLD));
		badge.setCustomNameVisible(true);

		base.getWorld().playSound(base, Sound.UI_TOAST_CHALLENGE_COMPLETE, 1f, 1.0f);
		base.getWorld().spawnParticle(org.bukkit.Particle.FIREWORK, base.clone().add(0, 1.6, 0), 20, 0.4, 0.3, 0.4, 0.12);

		new BukkitRunnable() {
			int ticks = 0;

			@Override
			public void run() {
				if (ticks >= 50 || !player.isOnline()) {
					if (badge.isValid()) {
						badge.remove();
					}
					cancel();
					return;
				}

				double angle = ticks * 0.24;
				for (int i = 0; i < 3; i++) {
					double offset = angle + (i * (Math.PI * 2.0 / 3.0));
					var point = base.clone().add(Math.cos(offset) * 0.9, 1.9 + (Math.sin(ticks * 0.12) * 0.08), Math.sin(offset) * 0.9);
					base.getWorld().spawnParticle(org.bukkit.Particle.END_ROD, point, 1, 0.01, 0.01, 0.01, 0.0);
					base.getWorld().spawnParticle(org.bukkit.Particle.HAPPY_VILLAGER, point, 1, 0.02, 0.02, 0.02, 0.01);
				}

				if (ticks % 12 == 0) {
					base.getWorld().playSound(base, Sound.BLOCK_AMETHYST_BLOCK_CHIME, 0.7f, 1.4f);
				}
				ticks++;
			}
		}.runTaskTimer(Manager.getPlugin(), 0L, 2L);
	}
}
