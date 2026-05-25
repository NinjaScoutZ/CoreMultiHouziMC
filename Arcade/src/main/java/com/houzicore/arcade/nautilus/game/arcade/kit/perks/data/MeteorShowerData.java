package com.houzicore.arcade.nautilus.game.arcade.kit.perks.data;


import com.houzicore.shared.common.util.UtilTime;
// NMS imports removed

import org.bukkit.Location;
import org.bukkit.entity.LargeFireball;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

public class MeteorShowerData
{
	public Player Shooter;
	public Location Target;
	public long Time;

	public MeteorShowerData(Player shooter, Location target)
	{
		Shooter = shooter;
		Target = target;
		Time = System.currentTimeMillis();
	}	

	public boolean update()
	{
		if (UtilTime.elapsed(Time, 12000))
			return true;

		LargeFireball ball = Target.getWorld().spawn(Target.clone().add(Math.random() * 24 - 12, 32 + Math.random() * 16, Math.random() * 24 - 12), LargeFireball.class);

		ball.setDirection(new Vector((Math.random()-0.5)*0.02, -0.2 - 0.05 * Math.random(), (Math.random()-0.5)*0.02));

		
		ball.setShooter(Shooter);
		ball.setYield(2.2f);

		return false;
	}
}
