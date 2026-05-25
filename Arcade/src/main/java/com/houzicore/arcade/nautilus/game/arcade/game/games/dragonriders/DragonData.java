package com.houzicore.arcade.nautilus.game.arcade.game.games.dragonriders;

import com.houzicore.shared.common.util.UtilAlg;
import com.houzicore.shared.common.util.UtilBlock;
import com.houzicore.shared.common.util.UtilEnt;
import com.houzicore.arcade.ArcadeManager;

import org.bukkit.Location;
import org.bukkit.Sound;
//import org.bukkit.craftbukkit.v1_7_R4.entity.CraftEnderDragon;
import org.bukkit.entity.EnderDragon;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

public class DragonData 
{
	ArcadeManager Manager;
	
	public EnderDragon Dragon; 
	public Player Rider;
	
	public Entity TargetEntity = null;
  
	public Location Location = null;

	public float Pitch = 0;
	public Vector Velocity = new Vector(0,0,0);
	
	public DragonData(ArcadeManager manager, Player rider) 
	{
		Manager = manager;
		
		Rider = rider; 

		Velocity = rider.getLocation().getDirection().setY(0).normalize();
		Pitch = UtilAlg.GetPitch(rider.getLocation().getDirection());

		Location = rider.getLocation();
		
		//Spawn Dragon
		manager.GetGame().CreatureAllowOverride = true;
		try {
			Class<?> apiClass = Class.forName("com.ericdebouwer.petdragon.api.PetDragonAPI");
			Object apiInstance = apiClass.getMethod("getInstance").invoke(null);
			Dragon = (EnderDragon) apiClass.getMethod("spawnDragon", Location.class, java.util.UUID.class)
					.invoke(apiInstance, rider.getLocation(), rider.getUniqueId());
		} catch (Exception e) {
			System.out.println("[DragonRiders] PetDragon not found, falling back to vanilla EnderDragon.");
			Dragon = rider.getWorld().spawn(rider.getLocation(), EnderDragon.class);
		}
		UtilEnt.Vegetate(Dragon);
		manager.GetGame().CreatureAllowOverride = false;
		
		rider.getWorld().playSound(rider.getLocation(), Sound.ENTITY_ENDER_DRAGON_GROWL, 20f, 1f);
		
		Dragon.setPassenger(Rider);
	}
	
	public void Move()
	{
		// ((CraftEnderDragon)Dragon).getHandle().setTargetBlock(GetTarget().getBlockX(), GetTarget().getBlockY(), GetTarget().getBlockZ()); // NMS removed
		
		Manager.GetExplosion().BlockExplosion(UtilBlock.getInRadius(Dragon.getLocation(), 10d).keySet(), Dragon.getLocation(), false);
	}
	
	public Location GetTarget()
	{
		return Rider.getLocation().add(Rider.getLocation().getDirection().multiply(40));
	}
}
