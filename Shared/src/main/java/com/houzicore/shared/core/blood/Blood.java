package com.houzicore.shared.core.blood;

import java.util.HashMap;
import java.util.HashSet;

import com.houzicore.shared.MiniPlugin;
import com.houzicore.shared.common.util.UtilServer;
import com.houzicore.shared.core.itemstack.ItemBuilder;
import com.houzicore.shared.updater.event.UpdateEvent;
import com.houzicore.shared.updater.UpdateType;

	import org.bukkit.Particle;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.inventory.InventoryPickupItemEvent;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.util.Vector;

public class Blood extends MiniPlugin {

	public Blood(JavaPlugin plugin) {
		super("Blood", plugin);
	}

	@EventHandler
	public void Death(PlayerDeathEvent event) {
		Effects(event.getEntity(), event.getEntity().getEyeLocation(), 10, 0.5, Sound.ENTITY_PLAYER_HURT, 1f, 1f,
				Material.RED_DYE, (byte) 0, true);
	}

	@EventHandler(priority = EventPriority.MONITOR)
	public void display(BloodEvent event) {
		
		// Optimization: Replaced extreme TPS-draining Item Drop spawners with native Particle API
		// Particle.ITEM uses the physical velocity physics out of the box in modern clients
		try {
			org.bukkit.inventory.ItemStack itemData = new org.bukkit.inventory.ItemStack(event.getMaterial());
			event.getLocation().getWorld().spawnParticle(
					Particle.ITEM,
					event.getLocation(),
					event.getParticles(), // count
					0.3, 0.3, 0.3, // offset
					event.getVelocityMult(), // speed
					itemData);
		} catch (Exception ex) {
			// Ignore if material is invalid for ITEM particle
		}

		if (event.getBloodStep()) {
			event.getLocation().getWorld().spawnParticle(
					Particle.BLOCK,
					event.getLocation(),
					5, 0.1, 0.1, 0.1, 0.1,
					Material.RED_WOOL.createBlockData());
		}

		if (event.getSound() != null) {
			event.getLocation().getWorld().playSound(event.getLocation(), event.getSound(), event.getSoundVolume(),
					event.getSoundPitch());
		}
	}

	public void Effects(Player player, Location loc, int particles, double velMult, Sound sound, float soundVol,
			float soundPitch, Material type, byte data, boolean bloodStep) {
		Effects(player, loc, particles, velMult, sound, soundVol, soundPitch, type, data, 10, bloodStep);
	}

	public void Effects(Player player, Location loc, int particles, double velMult, Sound sound, float soundVol,
			float soundPitch, Material type, byte data, int ticks, boolean bloodStep) {
		final BloodEvent event = new BloodEvent(player, loc, particles, velMult, sound, soundVol, soundPitch, type,
				data, ticks, bloodStep);
		UtilServer.getServer().getPluginManager().callEvent(event);
	}
}
