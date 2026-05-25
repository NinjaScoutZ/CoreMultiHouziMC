package com.houzicore.shared.common.util;

import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Particle.DustOptions;
import org.bukkit.entity.Player;

public class UtilParticle
{
	public enum ViewDist
	{
		SHORT(8),
		NORMAL(24),
		LONG(48),
		LONGER(96),
		MAX(256);
		
		private int _dist;
		
		ViewDist(int dist)
		{
			_dist = dist;
		}
		
		public int getDist()
		{
			return _dist;
		}
	}
	
	public enum ParticleType
	{
		ANGRY_VILLAGER(Particle.ANGRY_VILLAGER),
		BLOCK_CRACK(Particle.BLOCK),
		BLOCK_DUST(Particle.BLOCK),
		BUBBLE(Particle.BUBBLE),
		CLOUD(Particle.CLOUD),
		CAMPFIRE_COSY_SMOKE(Particle.CAMPFIRE_COSY_SMOKE),
		CRIT(Particle.CRIT),
		DEPTH_SUSPEND(Particle.DRIPPING_WATER), // Suspend is gone, using drip water as placeholder
		DRIP_LAVA(Particle.DRIPPING_LAVA),
		DRIP_WATER(Particle.DRIPPING_WATER),
		DROPLET(Particle.SPLASH),
		ENCHANTMENT_TABLE(Particle.ENCHANT),
		EXPLODE(Particle.EXPLOSION),
		FIREWORKS_SPARK(Particle.FIREWORK),
		FLAME(Particle.FLAME),
		FOOTSTEP(Particle.CLOUD), // Footstep is gone
		HAPPY_VILLAGER(Particle.HAPPY_VILLAGER),
		HEART(Particle.HEART),
		HUGE_EXPLOSION(Particle.EXPLOSION_EMITTER),
		ICON_CRACK(Particle.ITEM),
		INSTANT_SPELL(Particle.WITCH),
		LARGE_EXPLODE(Particle.EXPLOSION),
		LARGE_SMOKE(Particle.LARGE_SMOKE),
		LAVA(Particle.LAVA),
		MAGIC_CRIT(Particle.ENCHANTED_HIT),
		MOB_SPELL(Particle.WITCH),
		MOB_SPELL_AMBIENT(Particle.WITCH),
		NOTE(Particle.NOTE),
		PORTAL(Particle.PORTAL),
		RED_DUST(Particle.DUST),
		SLIME(Particle.ITEM_SLIME),
		SNOW_SHOVEL(Particle.SNOWFLAKE),
		SNOWBALL_POOF(Particle.ITEM_SNOWBALL),
		SPELL(Particle.WITCH),
		SPLASH(Particle.SPLASH),
		SUSPEND(Particle.DRIPPING_WATER),
		TOWN_AURA(Particle.MYCELIUM),
		WITCH_MAGIC(Particle.WITCH),
		CHERRY_LEAVES(Particle.CHERRY_LEAVES),
		DRAGON_BREATH(Particle.DRAGON_BREATH),
		SCULK_SOUL(Particle.SCULK_SOUL),
		END_ROD(Particle.END_ROD),
		GLOW(Particle.GLOW);

		public Particle particle;

		ParticleType(Particle particle)
		{
			this.particle = particle;
		}
	}

	public static void PlayParticle(ParticleType type, Location location, float offsetX, float offsetY, float offsetZ,
			float speed, int count, ViewDist dist, Player... players)
	{
		for (Player player : players)
		{
			if (UtilMath.offset(player.getLocation(), location) > dist.getDist())
				continue;
			
			if (type.particle == Particle.DUST)
			{
				// DUST requires DustOptions(Color, size) in Paper 1.21+
				DustOptions opts = new DustOptions(Color.RED, 1.0f);
				player.spawnParticle(Particle.DUST, location, count, offsetX, offsetY, offsetZ, speed, opts);
			}
			else
			{
				player.spawnParticle(type.particle, location, count, offsetX, offsetY, offsetZ, speed);
			}
		}
	}

	public static void PlayParticleToAll(ParticleType type, Location location, float offsetX, float offsetY, float offsetZ,
			float speed, int count, ViewDist dist)
	{
		PlayParticle(type, location, offsetX, offsetY, offsetZ, speed, count, dist, UtilServer.getPlayers());
	}

	public static void PlayParticle(Particle type, Location location, float offsetX, float offsetY, float offsetZ, 
			float speed, int count, ViewDist dist, Player... players) 
	{
		for (Player player : players)
		{
			if (UtilMath.offset(player.getLocation(), location) > dist.getDist())
				continue;
			
			if (type == Particle.DUST)
			{
				DustOptions opts = new DustOptions(Color.RED, 1.0f);
				player.spawnParticle(Particle.DUST, location, count, offsetX, offsetY, offsetZ, speed, opts);
			}
			else
			{
				player.spawnParticle(type, location, count, offsetX, offsetY, offsetZ, speed);
			}
		}
	}

	public static void drawTornadoFrame(Location loc, ParticleType type, double maxRadius, double height, double timeOffset) {
		for (double y = 0; y < height; y += 0.2) {
			double radius = (y / height) * maxRadius;
			double angle = (y * java.lang.Math.PI) + timeOffset;
			double x = radius * java.lang.Math.cos(angle);
			double z = radius * java.lang.Math.sin(angle);
			PlayParticle(type, loc.clone().add(x, y, z), 0f, 0f, 0f, 0f, 1, ViewDist.NORMAL, UtilServer.getPlayers());
		}
	}

	public static void drawDNAFrame(Location loc, ParticleType type1, ParticleType type2, double height, double timeOffset) {
		for (double y = 0; y < height; y += 0.2) {
			double angle = (y * 2) + timeOffset;
			double x1 = 1.0 * java.lang.Math.cos(angle);
			double z1 = 1.0 * java.lang.Math.sin(angle);
			PlayParticle(type1, loc.clone().add(x1, y, z1), 0f, 0f, 0f, 0f, 1, ViewDist.NORMAL, UtilServer.getPlayers());
			
			double x2 = 1.0 * java.lang.Math.cos(angle + java.lang.Math.PI);
			double z2 = 1.0 * java.lang.Math.sin(angle + java.lang.Math.PI);
			PlayParticle(type2, loc.clone().add(x2, y, z2), 0f, 0f, 0f, 0f, 1, ViewDist.NORMAL, UtilServer.getPlayers());
		}
	}

	public static void playParticleLine(ParticleType type, Location start, Location end, int points, ViewDist dist) {
		org.bukkit.util.Vector vector = end.toVector().subtract(start.toVector());
		double length = vector.length();
		vector.normalize().multiply(length / points);
		Location current = start.clone();
		for (int i = 0; i < points; i++) {
			PlayParticle(type, current, 0, 0, 0, 0, 1, dist, UtilServer.getPlayers());
			current.add(vector);
		}
	}

	public static void playParticleRing(ParticleType type, Location center, double radius, int points, ViewDist dist) {
		double angleIncrement = (2 * Math.PI) / points;
		for (int i = 0; i < points; i++) {
			double angle = i * angleIncrement;
			double x = radius * Math.cos(angle);
			double z = radius * Math.sin(angle);
			PlayParticle(type, center.clone().add(x, 0, z), 0, 0, 0, 0, 1, dist, UtilServer.getPlayers());
		}
	}

	public static void drawBlackHoleFrame(Location loc, ParticleType type, double radius, int particles) {
		for (int i = 0; i < particles; i++) {
			double u = java.lang.Math.random();
			double v = java.lang.Math.random();
			double theta = 2 * java.lang.Math.PI * u;
			double phi = java.lang.Math.acos(2 * v - 1);
			double x = (radius * java.lang.Math.sin(phi) * java.lang.Math.cos(theta));
			double y = (radius * java.lang.Math.sin(phi) * java.lang.Math.sin(theta));
			double z = (radius * java.lang.Math.cos(phi));
			
			PlayParticle(type, loc.clone().add(x,y,z), (float)-x, (float)-y, (float)-z, 0.1f, 0, ViewDist.NORMAL, UtilServer.getPlayers());
		}
	}

	public static void playRing(Location center, ParticleType type, double radius, int particles) {
		double increment = (2 * Math.PI) / particles;
		for (int i = 0; i < particles; i++) {
			double angle = i * increment;
			double x = center.getX() + (radius * Math.cos(angle));
			double z = center.getZ() + (radius * Math.sin(angle));
			PlayParticle(type, new Location(center.getWorld(), x, center.getY(), z), 0f, 0f, 0f, 0f, 1, ViewDist.NORMAL, UtilServer.getPlayers());
		}
	}

	public static void playSpiral(Location center, ParticleType type, double maxRadius, double height, int particles) {
		double increment = (2 * Math.PI) / (particles / 2.0); 
		double yIncrement = height / particles;
		for (int i = 0; i < particles; i++) {
			double angle = i * increment;
			double radius = (i / (double) particles) * maxRadius;
			double x = center.getX() + (radius * Math.cos(angle));
			double z = center.getZ() + (radius * Math.sin(angle));
			double y = center.getY() + (i * yIncrement);
			PlayParticle(type, new Location(center.getWorld(), x, y, z), 0f, 0f, 0f, 0f, 1, ViewDist.NORMAL, UtilServer.getPlayers());
		}
	}
}

