package com.houzicore.shared.common.util;

import org.bukkit.Color;
import org.bukkit.FireworkEffect;
import org.bukkit.Location;
import org.bukkit.entity.Firework;
import org.bukkit.inventory.meta.FireworkMeta;

public class UtilFirework {
	/**
	 * Item 80: Preventing Entity Bloat Using Detonate Framework.
	 * Instantly consumes the Firework entity on the same tick it spawns,
	 * ensuring 0 physics or collision lag while maintaining the high-fidelity native particle display.
	 */
	public static void playFirework(Location loc, FireworkEffect fe) {
		Firework firework = loc.getWorld().spawn(loc, Firework.class);
		FireworkMeta data = firework.getFireworkMeta();
		data.clearEffects();
		data.setPower(1);
		data.addEffect(fe);
		firework.setFireworkMeta(data);
		
		// Detonate safely explodes it to render particles natively without NMS or memory leaks
		firework.detonate();
	}

	public static void spawnRandomFirework(Location location) {
		FireworkEffect.Type type = FireworkEffect.Type.values()[(int) (Math.random()
				* FireworkEffect.Type.values().length)];
		Color color = Color.fromRGB((int) (Math.random() * 256), (int) (Math.random() * 256),
				(int) (Math.random() * 256));
		playFirework(location,
				FireworkEffect.builder().flicker(Math.random() > 0.5).withColor(color).with(type)
						.trail(Math.random() > 0.5).build());
	}

	public static Firework launchFirework(Location loc, FireworkEffect fe, org.bukkit.util.Vector dir, int power) {
		Firework fw = loc.getWorld().spawn(loc, Firework.class);
		FireworkMeta data = fw.getFireworkMeta();
		data.clearEffects();
		data.setPower(power);
		data.addEffect(fe);
		fw.setFireworkMeta(data);
		if (dir != null) fw.setVelocity(dir);
		return fw;
	}

	public static void playFirework(Location loc, FireworkEffect.Type type, Color color, boolean flicker, boolean trail) {
		playFirework(loc, FireworkEffect.builder().flicker(flicker).withColor(color).with(type).trail(trail).build());
	}

	public static Firework launchFirework(Location loc, FireworkEffect.Type type, Color color, boolean flicker, boolean trail, org.bukkit.util.Vector dir, int power) {
		return launchFirework(loc, FireworkEffect.builder().flicker(flicker).withColor(color).with(type).trail(trail).build(), dir, power);
	}
}
