package com.houzicore.shared.common.util;

import org.apache.commons.lang3.RandomUtils;
import org.bukkit.Color;
import org.bukkit.FireworkEffect;
import org.bukkit.FireworkEffect.Builder;
import org.bukkit.Location;
import org.bukkit.entity.Firework;
import org.bukkit.inventory.meta.FireworkMeta;

public class FireworkUtil
{
	public static Firework LaunchRandomFirework(Location location)
	{
		Builder builder = FireworkEffect.builder();
		
		if (RandomUtils.nextInt(0, 3) == 0)
		{
			builder.withTrail();
		}
		else if (RandomUtils.nextInt(0, 2) == 0)
		{
			builder.withFlicker();
		}
		
		builder.with(FireworkEffect.Type.values()[RandomUtils.nextInt(0, FireworkEffect.Type.values().length)]);
	
		int colorCount = 17;
		
		builder.withColor(Color.fromRGB(RandomUtils.nextInt(0, 255), RandomUtils.nextInt(0, 255), RandomUtils.nextInt(0, 255)));
		
		while (RandomUtils.nextInt(0, colorCount) != 0)
		{
			builder.withColor(Color.fromRGB(RandomUtils.nextInt(0, 255), RandomUtils.nextInt(0, 255), RandomUtils.nextInt(0, 255)));
			colorCount--;
		}
		
		Firework firework = location.getWorld().spawn(location, Firework.class);
        FireworkMeta data = (FireworkMeta) firework.getFireworkMeta();
        data.addEffects(builder.build());
        data.setPower(RandomUtils.nextInt(0, 3));
        firework.setFireworkMeta(data);

		return firework;
	}
}
