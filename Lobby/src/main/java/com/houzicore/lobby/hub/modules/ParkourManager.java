package com.houzicore.lobby.hub.modules;

import java.util.HashSet;
import java.util.Iterator;
import java.util.WeakHashMap;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerVelocityEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.util.Vector;

import com.houzicore.shared.MiniPlugin;
import com.houzicore.shared.common.util.C;
import com.houzicore.shared.common.util.Callback;
import com.houzicore.shared.common.util.F;
import com.houzicore.shared.common.util.UtilBlock;
import com.houzicore.shared.common.util.UtilEnt;
import com.houzicore.shared.common.util.UtilEvent;
import com.houzicore.shared.common.util.UtilMath;
import com.houzicore.shared.common.util.UtilPlayer;
import com.houzicore.shared.common.util.UtilServer;
import com.houzicore.shared.common.util.UtilTime;
import com.houzicore.shared.common.util.UtilEvent.ActionType;
import com.houzicore.shared.core.donation.DonationManager;
import com.houzicore.shared.core.gadget.event.GadgetActivateEvent;
import com.houzicore.shared.core.gadget.event.GadgetBlockEvent;
import com.houzicore.shared.core.gadget.types.MusicGadget;
import com.houzicore.shared.core.mount.event.MountActivateEvent;
import com.houzicore.shared.recharge.Recharge;
import com.houzicore.shared.core.task.TaskManager;
import com.houzicore.shared.core.treasure.event.TreasureStartEvent;
import com.houzicore.shared.updater.UpdateType;
import com.houzicore.shared.updater.event.UpdateEvent;
import com.houzicore.lobby.hub.HubManager;
import com.houzicore.lobby.hub.modules.parkour.ParkourData;

public class ParkourManager extends MiniPlugin
{
	public HubManager Manager;

	private HashSet<Player> _active = new HashSet<Player>();

	private HashSet<ParkourData> _parkour = new HashSet<ParkourData>();

	//private Location _snakeParkourReturn;
	private Location _lavaParkourReturn;
	private WeakHashMap<Player, Location> _lavaLocation = new WeakHashMap<Player, Location>();
	private WeakHashMap<Player, Long> _lavaTimer = new WeakHashMap<Player, Long>();

	//Modules
	protected DonationManager _donationManager;
	protected TaskManager _taskManager;
 
	public ParkourManager(HubManager manager, DonationManager donation, TaskManager task)
	{
		super("Parkour", manager.getPlugin());

		Manager = manager;

		_taskManager = task;
		_donationManager = donation;

		_parkour.add(new ParkourData("Ruins Parkour", new String[]
				{
				"This is an extremely difficult parkour.",
				"You will need to find the correct way through",
				"the ruins, overcoming many challenging jumps.",
				},
				6000, new Location(Manager.GetSpawn().getWorld(), 110,66,-44), 
				new Location(Manager.GetSpawn().getWorld(), 103,100,-60),  new Location(Manager.GetSpawn().getWorld(), 150,50,26)));


		_parkour.add(new ParkourData("Lava Parkour", new String[]
				{
				"This parkour is HOT! It's so hot that you",
				"must keep sprinting for the entire course,",
				"or you will die in flames!"
				}, 4000, new Location(Manager.GetSpawn().getWorld(), -93,67,38),  
				new Location(Manager.GetSpawn().getWorld(), -86,100,42),  new Location(Manager.GetSpawn().getWorld(), -120,50,-17)));
		
//		_parkour.add(new ParkourSnake("Snake Parkour", new String[]
//				{
//				"This parkour requires incredible timing",
//				"and great agility! Some say it was created",
//				"by the devil as a cruel joke!"
//				}, 8000, new Location(Manager.GetSpawn().getWorld(), 22,70,-54),  
//				new Location(Manager.GetSpawn().getWorld(), 35,-200,-90),  new Location(Manager.GetSpawn().getWorld(), -30,250,-46)));

		_lavaParkourReturn = new Location(Manager.GetSpawn().getWorld(), -89.5,68,36.5);
		_lavaParkourReturn.setYaw(90);
		
		//_snakeParkourReturn = new Location(Manager.GetSpawn().getWorld(), 16.5,72,-52.5);
		//_snakeParkourReturn.setYaw(180);
	}

	public boolean isParkourMode(Player player)
	{
		return _active.contains(player);
	}
	
	public void setParkourMode(Player player, boolean enabled)
	{
		boolean isThai = com.houzicore.shared.core.lang.LangManager.get().isThai(player);
		if (enabled)
		{
			_active.add(player);
			UtilPlayer.message(player, F.main("Parkour", isThai ? "§7คุณเข้าสู่ " + F.elem("โหมด Parkour") : "§7You have entered " + F.elem("Parkour Mode")));
			
			Manager.GetGadget().DisableAll(player);
			
			player.setVelocity(new Vector(0,-1,0));
		}
		else
		{
			_active.remove(player);
			UtilPlayer.message(player, F.main("Parkour", isThai ? "§7คุณออกจาก " + F.elem("โหมด Parkour") : "§7You have left " + F.elem("Parkour Mode")));
		}
	}
	
	@EventHandler
	public void playerVelocity(PlayerVelocityEvent event)
	{
		if (isParkourMode(event.getPlayer()))
			event.setCancelled(true);
	}
	
	@EventHandler
	public void disableGadgets(GadgetActivateEvent event)
	{
		if (isParkourMode(event.getPlayer()))
			event.setCancelled(true);
	}
	
	@EventHandler
	public void disableMounts(MountActivateEvent event)
	{
		if (isParkourMode(event.getPlayer()))
			event.setCancelled(true);
	}
	
	@EventHandler
	public void playerEnterParkour(UpdateEvent event)
	{
		if (event.getType() != UpdateType.FAST)
			return;
		
		for (Player player : UtilServer.getPlayers())
		{
			if (isParkourMode(player))
				continue;
			
			for (ParkourData data : _parkour)
				if (UtilMath.offset(player.getLocation(), data.NPC) < 6)
					if (Recharge.Instance.use(player, data.Name+" Info", 300000, false, false))
						data.Inform(player);
		}
	}
	
	@EventHandler
	public void parkourUpdate(UpdateEvent event)
	{
		if (event.getType() != UpdateType.TICK)
			return;
		
		Iterator<Player> playerIterator = _active.iterator();
		
		while (playerIterator.hasNext())
		{
			Player player = playerIterator.next();
			
			player.leaveVehicle();
			player.eject();
			
			if (!InsideParkour(player.getLocation()))
			{
				playerIterator.remove();
				boolean isThai = com.houzicore.shared.core.lang.LangManager.get().isThai(player);
				UtilPlayer.message(player, F.main("Parkour", isThai ? "§7คุณออกจาก " + F.elem("โหมด Parkour") : "§7You have left " + F.elem("Parkour Mode")));
			}
			else
			{
				
				for (Iterator<PotionEffect> iterator = player.getActivePotionEffects().iterator(); iterator.hasNext();)
				{
					player.removePotionEffect(iterator.next().getType());
				}
			}
		}
	}

	@EventHandler
	public void playerQuit(PlayerQuitEvent event)
	{
		_active.remove(event.getPlayer());
	}

	@EventHandler
	public void disallowBlockBreak(BlockBreakEvent event)
	{
		if (isParkourMode(event.getPlayer()))
		{
			event.getPlayer().teleport(Manager.GetSpawn());
			boolean isThai = com.houzicore.shared.core.lang.LangManager.get().isThai(event.getPlayer());
			UtilPlayer.message(event.getPlayer(), F.main("Parkour", isThai ? "§cคุณไม่สามารถทุบบล็อกในโหมด Parkour ได้!" : "§cYou cannot break blocks in Parkour mode!"));
		}
	}
	
	@EventHandler(priority = EventPriority.LOWEST)
	public void disallowBlockPlace(PlayerInteractEvent event)
	{
		if (event.getPlayer().getItemInHand() == null)
			return;
		
		if (!event.getPlayer().getItemInHand().getType().isBlock())
			return;
	
		if (!UtilEvent.isAction(event, ActionType.R_BLOCK))
			return;
		
		if (isParkourMode(event.getPlayer()))
		{
			event.getPlayer().teleport(Manager.GetSpawn());
			boolean isThai = com.houzicore.shared.core.lang.LangManager.get().isThai(event.getPlayer());
			UtilPlayer.message(event.getPlayer(), F.main("Parkour", isThai ? "§cคุณไม่สามารถวางบล็อกในโหมด Parkour ได้!" : "§cYou cannot place blocks in Parkour mode!"));
		}
	}

	@EventHandler
	public void lavaReturn(EntityDamageEvent event)
	{
		if (event.getCause() != DamageCause.LAVA)
			return;

		if (!(event.getEntity() instanceof Player))
		{
			event.getEntity().remove();
			return;
		}

		Player player = (Player)event.getEntity();

		if (!isParkourMode(player))
			return;

		event.getEntity().eject();
		event.getEntity().leaveVehicle();
		event.getEntity().teleport(_lavaParkourReturn);
	}
	
//	@EventHandler(priority = EventPriority.HIGHEST)
//	public void snakeReturn(EntityDamageEvent event)
//	{
//		if (event.getCause() != DamageCause.VOID)
//			return;
//		
//		if (!(event.getEntity() instanceof Player))
//			return;
//		
//		Player player = (Player)event.getEntity();
//		
//		if (!isParkourMode(player))
//			return;
//					
//		event.getEntity().eject();
//		event.getEntity().leaveVehicle();
//		event.getEntity().teleport(_snakeParkourReturn);
//	}

	@EventHandler
	public void lavaBlockReturn(UpdateEvent event)
	{
		if (event.getType() != UpdateType.TICK)
			return;

		for (Player player : UtilServer.getPlayers())
		{
			if (!UtilEnt.isGrounded(player))
				continue;
			
			if (!isParkourMode(player))
				continue;

			Block under = player.getLocation().getBlock().getRelative(BlockFace.DOWN);
			int id = com.houzicore.shared.common.util.IdUtil.getTypeId(under);
			int data = com.houzicore.shared.common.util.IdUtil.getData(under);
			if (id != 0 && id != 112 && id != 114 && !(id == 43 && data == 6)  && !(id == 44 && data == 6))
				continue;

			if (!_lavaLocation.containsKey(player) || UtilMath.offset(player.getLocation(), _lavaLocation.get(player)) > 1.5)
			{
				_lavaLocation.put(player, player.getLocation());
				_lavaTimer.put(player, System.currentTimeMillis());
				continue;
			}

			if (UtilTime.elapsed(_lavaTimer.get(player), 500))
			{
				boolean inCourse = false;
				for (Block block : UtilBlock.getInRadius(player.getLocation(), 1.5).keySet())
				{
					if (block.getType() == Material.NETHER_BRICK || block.getType() == Material.NETHER_BRICK_STAIRS)
					{
						inCourse = true;
						break;
					}
				}

				if (!inCourse)
					continue;

				_lavaLocation.remove(player);
				_lavaTimer.remove(player);

				player.eject();
				player.leaveVehicle();
				player.teleport(_lavaParkourReturn);
				player.setFireTicks(0);

				boolean isThai = com.houzicore.shared.core.lang.LangManager.get().isThai(player);
				UtilPlayer.message(player, F.main("Parkour", isThai ? "§cคุณห้ามหยุดวิ่งระหว่างเล่น Lava Parkour!" : "§cYou must keep running while playing Lava Parkour!"));
			}
		}
	}

	@EventHandler
	public void finishParkour(PlayerInteractEntityEvent event)
	{
		if (event.getRightClicked() == null)
			return;

		if (!(event.getRightClicked() instanceof LivingEntity))
			return;

		LivingEntity ent = (LivingEntity)event.getRightClicked();

		if (ent.getCustomName() == null)
			return;
		
		//Start Message
		if (ent.getCustomName().contains("Start"))
		{
			Player player = event.getPlayer();

			for (ParkourData data : _parkour)
			{
				if (!ent.getCustomName().contains(data.Name))
					continue;
				
				if (isParkourMode(player))
					setParkourMode(player, false);
				else
					setParkourMode(player, true);
			}
		}

		//Finish Message
		if (ent.getCustomName().contains("Finish"))
		{
			final Player player = event.getPlayer();
			
			if (!isParkourMode(player))
			{
				boolean isThai = com.houzicore.shared.core.lang.LangManager.get().isThai(player);
				//Inform
				UtilPlayer.message(player, F.main("Parkour", isThai ? "§cคุณต้องอยู่ใน " + F.elem("โหมด Parkour") + " §cก่อนถึงจะจบได้" : "§cYou must be in " + F.elem("Parkour Mode") + " §cto finish!"));
				UtilPlayer.message(player, F.main("Parkour", isThai ? "§7คุยกับ " + F.elem("NPC จุดเริ่มต้น") + " §7เพื่อเข้าสู่โหมด Parkour" : "§7Talk to the " + F.elem("Start NPC") + " §7to enter Parkour Mode"));
				return;
			}

			if (!Recharge.Instance.use(player, "Finish Parkour", 30000, false, false))
				return;

			for (ParkourData data : _parkour)
			{
				if (!ent.getCustomName().contains(data.Name))
					continue;

				//Inform
				boolean isThai = com.houzicore.shared.core.lang.LangManager.get().isThai(player);
				UtilPlayer.message(player, F.main("Parkour", isThai ? "§7คุณผ่านด่าน " + F.elem(data.Name) + " §7แล้ว!" : "§7You have completed " + F.elem(data.Name) + "!"));

				//Gems
				if (!_taskManager.hasCompletedTask(player, data.Name))		
				{
					final ParkourData fData = data;

					_taskManager.completedTask(new Callback<Boolean>() 
					{
						public void run(Boolean completed)
						{
							_donationManager.RewardEssence(new Callback<Boolean>() 
							{
								public void run(Boolean completed)
								{
									if (completed)
									{
										boolean isThai = com.houzicore.shared.core.lang.LangManager.get().isThai(player);
										UtilPlayer.message(player, F.main("Parkour", isThai ? "§7คุณได้รับ " + F.elem(C.cGreen + fData.Gems + " Essence") : "§7You received " + F.elem(C.cGreen + fData.Gems + " Essence")));
	
										//Sound
										player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 2f, 1.5f);
									}
									else
									{
										boolean isThai = com.houzicore.shared.core.lang.LangManager.get().isThai(player);
										_taskManager.Get(player).TasksCompleted.remove(_taskManager.getTaskId(fData.Name));
										UtilPlayer.message(player, F.main("Parkour", isThai ? "§cเกิดข้อผิดพลาดในการมอบ " + F.elem(C.cGreen + fData.Gems + " Essence") + " §cโปรดคลิก NPC อีกครั้ง" : "§cFailed to reward " + F.elem(C.cGreen + fData.Gems + " Essence") + " §cPlease click NPC again"));
									}
								}
							}, "Parkour " + fData.Name, player.getName(), player.getUniqueId(), fData.Gems);							

							//Sound
							player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 2f, 1.5f);
						}
					}, player, fData.Name);
				}
			}
		}
	}
	
	@EventHandler
	public void gadgetBlockChange(GadgetBlockEvent event)
	{
		for (Iterator<Block> iterator = event.getBlocks().iterator(); iterator.hasNext();)
		{
			Block block = iterator.next();
			
			for (ParkourData data : _parkour)
			{
				if (data.InBoundary(block.getLocation()))
				{
					iterator.remove();
					break;
				}
			}	
		}
	}

	public boolean InsideParkour(Location loc)
	{
		for (ParkourData data : _parkour)
			if (data.InBoundary(loc))
				return true;
		
		return false;
	}

	@EventHandler
	public void preventTreasureNearParkour(TreasureStartEvent event)
	{
		if (InsideParkour(event.getPlayer().getLocation()))
		{
			event.setCancelled(true);
			boolean isThai = com.houzicore.shared.core.lang.LangManager.get().isThai(event.getPlayer());
			UtilPlayer.message(event.getPlayer(), F.main("Parkour", isThai ? "§cคุณไม่สามารถเปิดกล่องใกล้โซน Parkour ได้" : "§cYou cannot open chests near the Parkour zone!"));
		}
	}
	
	@EventHandler
	public void musicDisable(GadgetActivateEvent event)
	{
		if (event.getGadget() instanceof MusicGadget)
		{
			if (InsideParkour(event.getPlayer().getLocation()))
			{
				event.setCancelled(true);
			}
		}
	}
	
//	@EventHandler
//	public void snakeUpdate(UpdateEvent event)
//	{
//		if (event.getType() != UpdateType.FASTER)
//			return;
//		
//		for (ParkourData parkour : _parkour)
//			if (parkour instanceof ParkourSnake)
//				((ParkourSnake)parkour).Update();
//	}
}
