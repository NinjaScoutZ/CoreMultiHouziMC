package com.houzicore.lobby.hub.modules;

import java.util.HashSet;
import java.util.Iterator;

import com.houzicore.shared.MiniPlugin;
import com.houzicore.shared.common.util.C;
import com.houzicore.shared.common.util.UtilBlock;
import com.houzicore.shared.common.util.UtilMath;
import com.houzicore.shared.common.util.UtilServer;
import com.houzicore.shared.common.util.UtilWorld;
import com.houzicore.shared.core.itemstack.ItemStackFactory;
import com.houzicore.shared.updater.UpdateType;
import com.houzicore.shared.updater.event.UpdateEvent;
import com.houzicore.lobby.hub.HubManager;
import com.houzicore.lobby.hub.HubType;
import com.houzicore.shared.core.damage.CustomDamageEvent;

import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Boat;
import org.bukkit.entity.Chicken;
import org.bukkit.entity.Cow;
import org.bukkit.entity.Creature;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Pig;
import org.bukkit.entity.Player;
import org.bukkit.entity.Skeleton;
import org.bukkit.entity.Snowman;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockFormEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.block.BlockSpreadEvent;
import org.bukkit.event.block.LeavesDecayEvent;
import org.bukkit.event.entity.EntityCombustEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.EntityTargetEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerPickupItemEvent;
import org.bukkit.event.weather.WeatherChangeEvent;

public class WorldManager extends MiniPlugin
{
	public HubManager Manager;

	private HashSet<LivingEntity> _mobs = new HashSet<LivingEntity>();
	
	private boolean _christmasSnow = false;
	private long _christSnowTime = 0;

	public WorldManager(HubManager manager)
	{
		super("World Manager", manager.getPlugin());

		Manager = manager;
		
		//Added by TheMineBench, to stop day cycles instead of setting the time on update event.
		World world = UtilWorld.getWorld("world");

		// Clear residual entities from the previous map loads (holograms, mobs, old NPCs) without deleting paintings/item frames
		for (org.bukkit.entity.Entity ent : world.getEntities()) {
			if (ent instanceof org.bukkit.entity.Player) continue;

			if (ent instanceof org.bukkit.entity.LivingEntity || ent instanceof org.bukkit.entity.ArmorStand || ent instanceof org.bukkit.entity.TextDisplay) {
				// Force-remove invulnerable entities (e.g. old Lobby NPCs stuck from previous sessions)
				if (ent instanceof org.bukkit.entity.LivingEntity) {
					((org.bukkit.entity.LivingEntity) ent).setInvulnerable(false);
					((org.bukkit.entity.LivingEntity) ent).setAI(true);
				}
				ent.remove();
			}
		}
		
		world.setGameRuleValue("doDaylightCycle", "false");
		world.setGameRuleValue("doMobSpawning", "false");
		
		if (Manager.Type == HubType.Halloween)	
			world.setTime(16000);
		else									
			world.setTime(2000); // Keep Lobby at a bright morning look
		world.setStorm(false);
		world.setThundering(false);
	}

	@EventHandler
	public void SpawnAnimals(UpdateEvent event)
	{
		if (event.getType() != UpdateType.SLOW)
			return;

		Iterator<LivingEntity> entIterator = _mobs.iterator();

		while (entIterator.hasNext())
		{
			LivingEntity ent = entIterator.next();

			if (!ent.isValid())
			{
				ent.remove();
				entIterator.remove();
			}
		}
	}

	@EventHandler
	public void BlockBreak(BlockBreakEvent event)
	{
		if (event.getPlayer().getGameMode() == GameMode.CREATIVE)
			return; 

		if (Manager.getArenaManager() != null && Manager.getArenaManager().isPlayerInMatch(event.getPlayer())) return;
		event.setCancelled(true);
	}

	@EventHandler(priority = EventPriority.LOWEST)
	public void Explosion(EntityExplodeEvent event)
	{
		event.blockList().clear();
	}

	@EventHandler
	public void VineGrow(BlockSpreadEvent event)
	{
		event.setCancelled(true);
	}

	@EventHandler
	public void LeaveDecay(LeavesDecayEvent event)
	{
		event.setCancelled(true);
	}

	@EventHandler
	public void BlockPlace(BlockPlaceEvent event)
	{
		if (event.getPlayer().getGameMode() == GameMode.CREATIVE)
			return;

		if (Manager.getArenaManager() != null && Manager.getArenaManager().isPlayerInMatch(event.getPlayer())) return;
		event.setCancelled(true);
	}

	@EventHandler
	public void BorderUpdate(UpdateEvent event)
	{
		if (event.getType() != UpdateType.FASTEST)
			return;

		for (Player player : UtilServer.getPlayers())
		{
			if (UtilMath.offset(player.getLocation(), Manager.GetSpawn()) > 200)
			{
				player.eject();
				player.leaveVehicle();
				player.teleport(Manager.GetSpawn());
			}
		}
	}

	@EventHandler(priority = EventPriority.LOW)
	public void ItemPickup(PlayerPickupItemEvent event)
	{
		if (event.getPlayer().getGameMode() == GameMode.CREATIVE)
			return;

		event.setCancelled(true);
	}

	@EventHandler(priority = EventPriority.LOW)
	public void ItemDrop(PlayerDropItemEvent event)
	{
		if (event.getPlayer().getGameMode() == GameMode.CREATIVE)
			return;

		event.setCancelled(true);
	}
	
	@EventHandler(priority = EventPriority.LOW)
	public void ItemDespawn(UpdateEvent event)
	{
		if (event.getType() != UpdateType.SEC)
			return;
		
		for (Entity ent : UtilWorld.getWorld("world").getEntities())
		{
			if (!(ent instanceof Item))
				continue;
			
			if (((Item)ent).getItemStack().getType() == Material.BAT_SPAWN_EGG)
				continue;
			
			if (ent.getTicksLived() > 1200)
				ent.remove();
		}
	}
//Removed by TheMineBench, time is now stopped when the WorldManager is created
/*
	@EventHandler
	public void UpdateWeather(UpdateEvent event)
	{
		if (event.getType() != UpdateType.SEC)
			return;

		World world = UtilWorld.getWorld("world");

		if (Manager.Type == HubType.Halloween)	
			world.setTime(16000);
		else										
			world.setTime(6000);
										
		world.setStorm(false);
	}
*/
	@EventHandler
	public void SlowSunriseUpdate(UpdateEvent event)
	{
		if (event.getType() != UpdateType.SLOW)
			return; // Every 10 seconds

		World world = UtilWorld.getWorld("world");
		if (world == null) return;
		world.setStorm(false);
		world.setThundering(false);
		world.setTime(Manager.Type == HubType.Halloween ? 16000 : 2000);
	}

	@EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
	public void CreatureSpawn(CreatureSpawnEvent event)
	{
		if (event.getSpawnReason() == CreatureSpawnEvent.SpawnReason.CUSTOM
				|| event.getSpawnReason() == CreatureSpawnEvent.SpawnReason.SPAWNER_EGG
				|| event.getSpawnReason() == CreatureSpawnEvent.SpawnReason.COMMAND)
			return;

		if (!(event.getEntity() instanceof Player) && event.getEntity() instanceof LivingEntity)
			event.setCancelled(true);
	}

	//Added by TheMineBench.  Stops weather from changing.
	@EventHandler
	public void OnWeather(WeatherChangeEvent event) {
		
		if (!event.getWorld().getName().equals("world"))
			return;
		
		event.setCancelled(true);
	}
	
	@EventHandler
	public void HalloweenUpdates(UpdateEvent event)
	{
		if (event.getType() != UpdateType.FAST)
			return;

		if (Manager.Type != HubType.Halloween)
			return;

		//Block Lightup
		for (Player player : UtilServer.getPlayers())
		{
			for (Block block : UtilBlock.getInRadius(player.getLocation(), 3d).keySet())
			{
				if (block.getType() == Material.PUMPKIN)
						Manager.GetBlockRestore().Add(block, 91,
								com.houzicore.shared.common.util.IdUtil.getData(block), 2000);
			}
		}

		//Mob Helmets
		for (LivingEntity ent : _mobs)
		{
			if (!(ent instanceof Creature))
				continue;

			Creature skel = (Creature)ent;

			if (skel.getTarget() != null && skel.getTarget() instanceof Player && UtilMath.offset(skel, skel.getTarget()) < 6)
			{
				skel.getEquipment().setHelmet(ItemStackFactory.Instance.CreateStack(Material.JACK_O_LANTERN));
			}
			else
			{
				skel.getEquipment().setHelmet(ItemStackFactory.Instance.CreateStack(Material.PUMPKIN));
			}
		}
	}
	
	@EventHandler
	public void SoundUpdate(UpdateEvent event)
	{
		if (Manager.Type != HubType.Halloween)
			return;

		if (event.getType() != UpdateType.SLOW)
			return;

		if (Math.random() > 0.1)
			return;

		for (Player player : UtilServer.getPlayers())
			player.playSound(player.getLocation(), Sound.AMBIENT_CAVE, 3f, 1f);
	}
	
	@EventHandler
	public void BlockForm(BlockFormEvent event)
	{
		event.setCancelled(true);
	}
	
	@EventHandler
	public void CreatureTarget(EntityTargetEvent event)
	{
		if (Manager.Type == HubType.Christmas)
		{
			event.setCancelled(true);
		}
	}
	
	@EventHandler
	public void BoatBreak(CustomDamageEvent event)
	{
		if (event.GetDamageeEntity() instanceof Boat)
			event.SetCancelled("Boat Break");
	}
	
	@EventHandler
	public void combustPrevent(EntityCombustEvent event)
	{
		if (event.getEntity() instanceof Player)
		{
			event.setCancelled(true);
		}
	}
}

