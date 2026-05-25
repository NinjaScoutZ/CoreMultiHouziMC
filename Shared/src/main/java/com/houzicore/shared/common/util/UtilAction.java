package com.houzicore.shared.common.util;


import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;
import com.houzicore.shared.core.combat.CombatManager;

public class UtilAction
{
	public static void velocity(Entity ent, double str, double yAdd, double yMax, boolean groundBoost)
	{
		velocity(ent, ent.getLocation().getDirection(), str, false, 0, yAdd, yMax, groundBoost);
	}

	public static void velocity(Entity ent, double str, double yAdd, double yMax, boolean groundBoost, LivingEntity attacker, String source)
	{
		velocity(ent, ent.getLocation().getDirection(), str, false, 0, yAdd, yMax, groundBoost, attacker, source);
	}

	public static void velocity(Entity ent, Vector vec, double str, boolean ySet, double yBase, double yAdd, double yMax, boolean groundBoost, LivingEntity attacker, String source)
	{
		if (ent instanceof Player && attacker != null && source != null)
		{
			if (CombatManager.get() != null)
			{
				CombatManager.get().logKnockback((Player) ent, attacker, source);
			}
		}
		velocity(ent, vec, str, ySet, yBase, yAdd, yMax, groundBoost);
	}

	public static void velocity(Entity ent, Vector vec, double str, boolean ySet, double yBase, double yAdd, double yMax, boolean groundBoost)
	{
		if (ent instanceof org.bukkit.entity.Display || ent instanceof org.bukkit.entity.Interaction)
			return;
		if (ent instanceof org.bukkit.entity.ArmorStand && ((org.bukkit.entity.ArmorStand) ent).isMarker())
			return;
			
		if (Double.isNaN(vec.getX()) || Double.isNaN(vec.getY()) || Double.isNaN(vec.getZ()) || vec.length() == 0)
			return;
		
		//YSet
		if (ySet)
			vec.setY(yBase);

		//Modify
		vec.normalize();
		vec.multiply(str);
		
		//YAdd
		vec.setY(vec.getY() + yAdd);
		
		//Limit
		if (vec.getY() > yMax)
			vec.setY(yMax);
		
		if (groundBoost)
			if (UtilEnt.isGrounded(ent))
				vec.setY(vec.getY() + 0.2); 
		
		//Velocity
		ent.setFallDistance(0);
		
		
		ent.setVelocity(vec);	
	}
}
