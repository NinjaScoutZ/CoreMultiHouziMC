package com.houzicore.shared.core.gadget.gadgets;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Chicken;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import com.houzicore.shared.common.util.C;
import com.houzicore.shared.core.gadget.GadgetManager;
import com.houzicore.shared.core.gadget.types.ItemGadget;

public class ItemMobBomb extends ItemGadget {

	public ItemMobBomb(GadgetManager manager) {
		super(manager, "Mob Bomb",
				new String[] { C.cWhite + "Pop open a critter crate", C.cWhite + "and let chaos scatter everywhere." },
				-1, Material.EGG, (byte) 0, 6000,
				new Ammo("Mob Bomb", "12 Mob Bombs", Material.EGG, (byte) 0,
						new String[] { C.cWhite + "A fresh batch of critter crates." }, 1200, 12));
	}

	@Override
	public void ActivateCustom(Player player) {
		var center = player.getLocation().clone().add(0, 0.2, 0);
		var world = player.getWorld();
		List<Chicken> chickens = new ArrayList<>();

		world.spawnParticle(org.bukkit.Particle.CLOUD, center, 18, 0.35, 0.2, 0.35, 0.03);
		world.spawnParticle(org.bukkit.Particle.HAPPY_VILLAGER, center, 12, 0.4, 0.3, 0.4, 0.02);
		world.playSound(center, Sound.ENTITY_CHICKEN_EGG, 1f, 0.8f);

		for (int i = 0; i < 4; i++) {
			Chicken chicken = (Chicken) world.spawnEntity(center.clone().add((Math.random() - 0.5) * 0.8, 0.1, (Math.random() - 0.5) * 0.8), EntityType.CHICKEN);
			chicken.setAdult();
			chicken.setAI(false);
			chicken.setInvulnerable(true);
			chicken.setCollidable(false);
			chicken.setSilent(false);
			chicken.setVelocity(new org.bukkit.util.Vector((Math.random() - 0.5) * 0.6, 0.35 + (Math.random() * 0.15), (Math.random() - 0.5) * 0.6));
			chickens.add(chicken);
		}

		new BukkitRunnable() {
			int ticks = 0;

			@Override
			public void run() {
				if (ticks >= 40) {
					for (Chicken chicken : chickens) {
						if (chicken != null && chicken.isValid()) {
							chicken.remove();
						}
					}
					cancel();
					return;
				}

				for (Chicken chicken : chickens) {
					if (chicken == null || !chicken.isValid()) {
						continue;
					}

					chicken.getWorld().spawnParticle(org.bukkit.Particle.CLOUD, chicken.getLocation().add(0, 0.2, 0), 1, 0.05, 0.02, 0.05, 0.0);
					if (ticks % 10 == 0) {
						chicken.getWorld().playSound(chicken.getLocation(), Sound.ENTITY_CHICKEN_AMBIENT, 0.6f, 1.2f);
					}
				}

				ticks++;
			}
		}.runTaskTimer(Manager.getPlugin(), 0L, 2L);
	}
}
