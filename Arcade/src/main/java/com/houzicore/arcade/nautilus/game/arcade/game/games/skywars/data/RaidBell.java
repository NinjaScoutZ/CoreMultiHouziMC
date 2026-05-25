package com.houzicore.arcade.nautilus.game.arcade.game.games.skywars.data;

import com.houzicore.shared.common.util.C;
import com.houzicore.shared.common.util.UtilMath;
import com.houzicore.shared.common.util.UtilParticle;
import com.houzicore.shared.common.util.UtilParticle.ParticleType;
import com.houzicore.shared.common.util.UtilParticle.ViewDist;
import com.houzicore.shared.common.util.UtilServer;
import com.houzicore.shared.common.util.UtilTime;
import com.houzicore.shared.core.loot.ChestLoot;
import com.houzicore.shared.core.loot.RandomItem;
import com.houzicore.arcade.nautilus.game.arcade.game.games.skywars.Skywars;
import com.houzicore.shared.core.hologram.Hologram;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.Particle;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.HashMap;

import com.houzicore.arcade.nautilus.game.arcade.game.modules.objective.GameObjective;

public class RaidBell extends GameObjective
{
	private Skywars Host;
	
	private Location _loc;
	private double _radius = 8.0;
	private double _heightLimit = 5.0;
	private long _itemInterval = 5000; // 5 seconds
	
	private HashMap<Player, Long> _lastBlessingTime = new HashMap<>();
	
	private ChestLoot _blessings = new ChestLoot();
	
	private int _playersInside = 0;
	
	// Particle rotation angle
	private double _particleAngle = 0;
	
	public RaidBell(Skywars host, Location loc)
	{
		super(host, "Raid Bell", loc);
		Host = host;
		_loc = loc;
		
		// Place a bell block at the location
		_loc.getBlock().setType(Material.BELL);
		
		setupBlessingLoot();
	}
	
	@Override
	protected String[] getHologramText()
	{
		if (_playersInside > 0)
			return new String[] { "§6🔔 Blessed Bell §c[" + _playersInside + " Players!]" };
		else
			return new String[] { "§e🔔 Blessed Bell §a[Ready]" };
	}
	
	private void setupBlessingLoot()
	{
		// Blocks for bridging (weight 35)
		_blessings.addLoot(new RandomItem(Material.COBBLESTONE, 35, 8, 16));
		_blessings.addLoot(new RandomItem(Material.OAK_PLANKS, 35, 8, 16));
		
		// Arrows (weight 25)
		_blessings.addLoot(new RandomItem(Material.ARROW, 25, 4, 8));
		
		// Iron Ingots (weight 20)
		_blessings.addLoot(new RandomItem(Material.IRON_INGOT, 20, 1, 2));
		
		// Snowballs (weight 15)
		_blessings.addLoot(new RandomItem(Material.SNOWBALL, 15, 8, 16));
		
		// Golden Apple - rare (weight 5)
		_blessings.addLoot(new RandomItem(Material.GOLDEN_APPLE, 5, 1, 1));
	}
	
	@Override
	public void onTick()
	{
		// Rotating particle effect around the bell (aura ring)
		_particleAngle += 0.1;
		if (_particleAngle > Math.PI * 2)
			_particleAngle -= Math.PI * 2;
		
		for (int i = 0; i < 4; i++) {
			double currentAngle = _particleAngle + (Math.PI / 2 * i);
			double px = _loc.getX() + 0.5 + Math.cos(currentAngle) * _radius;
			double pz = _loc.getZ() + 0.5 + Math.sin(currentAngle) * _radius;
			
			UtilParticle.PlayParticle(ParticleType.END_ROD,
					new Location(_loc.getWorld(), px, _loc.getY() + 0.2, pz),
					0.0f, 0.0f, 0.0f, 0.0f, 1,
					ViewDist.LONG, UtilServer.getPlayers());
		}
		
		// Beacon-like particle beam upward
		if (UtilMath.r(3) == 0)
		{
			UtilParticle.PlayParticle(ParticleType.END_ROD,
					_loc.clone().add(0.5, 2.0 + UtilMath.r(15), 0.5),
					0.1f, 0.5f, 0.1f, 0f, 1,
					ViewDist.MAX, UtilServer.getPlayers());
		}
		
		// Check players in radius (Cylinder: horizontal 8 blocks, vertical 5 blocks)
		_playersInside = 0;
		
		for (Player player : Host.GetPlayers(true))
		{
			if (!Host.IsAlive(player))
				continue;
			
			double horizDist = Math.sqrt(
					Math.pow(player.getLocation().getX() - (_loc.getX() + 0.5), 2) +
					Math.pow(player.getLocation().getZ() - (_loc.getZ() + 0.5), 2));
			
			double vertDist = player.getLocation().getY() - _loc.getY();
			
			if (horizDist <= _radius && vertDist >= -1 && vertDist <= _heightLimit)
			{
				_playersInside++;
				
				// Show ActionBar to player
				com.houzicore.shared.common.actionbar.ActionBarService.display(player, com.houzicore.shared.common.actionbar.ActionBarChannel.GAME_STATUS,
					net.kyori.adventure.text.Component.text("🔔 ").color(net.kyori.adventure.text.format.NamedTextColor.GOLD)
						.append(net.kyori.adventure.text.Component.text("Raid Bell ").color(net.kyori.adventure.text.format.NamedTextColor.AQUA))
						.append(net.kyori.adventure.text.Component.text("[ ").color(net.kyori.adventure.text.format.NamedTextColor.WHITE))
						.append(net.kyori.adventure.text.Component.text("▼").color(net.kyori.adventure.text.format.NamedTextColor.YELLOW))
						.append(net.kyori.adventure.text.Component.text(" ] ").color(net.kyori.adventure.text.format.NamedTextColor.WHITE))
						.append(net.kyori.adventure.text.Component.text("+1 Item").color(net.kyori.adventure.text.format.NamedTextColor.GRAY)),
					1500);
				
				// Apply Glowing effect (40 ticks = 2 seconds, refreshed each tick)
				player.addPotionEffect(new PotionEffect(PotionEffectType.GLOWING, 40, 0, true, false), true);
				
				// Check blessing interval
				long lastTime = _lastBlessingTime.getOrDefault(player, 0L);
				
				if (UtilTime.elapsed(lastTime, _itemInterval))
				{
					// Give blessing item
					ItemStack blessing = _blessings.getLoot();
					player.getInventory().addItem(blessing);
					
					// Sound + particle feedback
					player.playSound(player.getLocation(), Sound.BLOCK_BELL_USE, 1.5f, 1.2f);
					player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 0.8f, 1.5f);
					
					// Heavenly Descent effect
					UtilParticle.PlayParticle(Particle.TOTEM_OF_UNDYING,
							player.getLocation().add(0, 3.0, 0),
							0.5f, 1.5f, 0.5f, 0.1f, 30,
							ViewDist.SHORT, UtilServer.getPlayers());
					
					// Golden shimmer instead of lightning (less disruptive)
					UtilParticle.PlayParticle(ParticleType.END_ROD,
							player.getLocation().add(0, 2.5, 0),
							0.3f, 1.0f, 0.3f, 0.05f, 15,
							ViewDist.SHORT, UtilServer.getPlayers());
					
					_lastBlessingTime.put(player, System.currentTimeMillis());
				}
			}
		}
		
		// Ambient bell sound when players are inside (every ~3 seconds)
		if (_playersInside > 0 && UtilMath.r(60) == 0)
		{
			_loc.getWorld().playSound(_loc, Sound.BLOCK_BELL_RESONATE, 2.0f, 0.8f);
		}
		
		// Update Hologram text
		updateHologram();
	}
	
	public String getScoreboardInfo()
	{
		int count = 0;
		for (Player player : Host.GetPlayers(true))
		{
			if (!Host.IsAlive(player))
				continue;
			
			double horizDist = Math.sqrt(
					Math.pow(player.getLocation().getX() - (_loc.getX() + 0.5), 2) +
					Math.pow(player.getLocation().getZ() - (_loc.getZ() + 0.5), 2));
			double vertDist = player.getLocation().getY() - _loc.getY();
			
			if (horizDist <= _radius && vertDist >= -1 && vertDist <= _heightLimit)
				count++;
		}
		
		if (count > 0)
			return C.cGreen + count + " Inside";
		else
			return C.cGray + "Empty";
	}
}
	

