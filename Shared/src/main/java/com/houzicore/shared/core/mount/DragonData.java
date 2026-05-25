package com.houzicore.shared.core.mount;

import com.houzicore.shared.common.util.UtilAlg;
import com.houzicore.shared.common.util.UtilEnt;
import com.houzicore.shared.api.disguise.DisguiseArchetype;
import com.houzicore.shared.api.disguise.DisguiseRequest;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Sound;
////
import org.bukkit.entity.EnderDragon;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

public class DragonData {
	DragonMount Host;

	public EnderDragon DragonBase;
	public Player Rider;

	public Entity TargetEntity = null;

	public Location Location = null;

	public float Pitch = 0;
	public Vector Velocity = new Vector(0, 0, 0);

	public DragonData(DragonMount dragonMount, Player rider) {
		Host = dragonMount;

		Rider = rider;

		Velocity = rider.getLocation().getDirection().setY(0).normalize();
		Pitch = UtilAlg.GetPitch(rider.getLocation().getDirection());

		Location = rider.getLocation();

		// Spawn EnderDragon directly using PetDragonAPI
		try {
			Class<?> apiClass = Class.forName("com.ericdebouwer.petdragon.api.PetDragonAPI");
			Object apiInstance = apiClass.getMethod("getInstance").invoke(null);
			DragonBase = (EnderDragon) apiClass.getMethod("spawnDragon", Location.class, java.util.UUID.class)
					.invoke(apiInstance, rider.getLocation(), rider.getUniqueId());
		} catch (Exception e) {
			System.out.println("[Mounts] PetDragon not found, falling back to vanilla EnderDragon.");
			DragonBase = rider.getWorld().spawn(rider.getLocation(), EnderDragon.class);
		}
		
		rider.getWorld().playSound(rider.getLocation(), Sound.ENTITY_ENDER_DRAGON_GROWL, 20f, 1f);

		DragonBase.addPassenger(Rider);

		Bukkit.getServer().getScheduler().runTaskLater(Host.Manager.getPlugin(), new Runnable() {
			@Override
			public void run() {
				if (!DragonBase.getPassengers().contains(Rider)) {
					DragonBase.addPassenger(Rider);
				}
			}
		}, 10L);
	}

	public Location GetTarget() {
		return Rider.getLocation().add(Rider.getLocation().getDirection().multiply(40));
	}

	public void Move() {
		if (Rider != null && Rider.isValid()) {
			Vector dir = Rider.getLocation().getDirection();
			//DragonBase.setVelocity(dir.multiply(1.2));
			//DragonBase.setRotation(Rider.getLocation().getYaw(), Rider.getLocation().getPitch());
		}
	}
}
