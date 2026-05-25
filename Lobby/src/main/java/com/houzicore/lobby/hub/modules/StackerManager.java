package com.houzicore.lobby.hub.modules;

import java.time.Duration;
import java.util.HashSet;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.entity.EnderDragon;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Horse;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Wither;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.util.Vector;

import com.houzicore.shared.MiniPlugin;
import com.houzicore.shared.common.util.F;
import com.houzicore.shared.common.util.UtilAction;
import com.houzicore.shared.common.util.UtilAlg;
import com.houzicore.shared.common.util.UtilEnt;
import com.houzicore.shared.common.util.UtilEvent;
import com.houzicore.shared.common.util.UtilGear;
import com.houzicore.shared.common.util.UtilPlayer;
import com.houzicore.shared.common.util.UtilEvent.ActionType;
import com.houzicore.shared.core.lang.LangManager;
import com.houzicore.shared.core.event.StackerEvent;
import com.houzicore.shared.core.gadget.gadgets.MorphBlock;
import com.houzicore.shared.core.gadget.types.GadgetType;
import com.houzicore.shared.core.projectile.IThrown;
import com.houzicore.shared.core.projectile.ProjectileManager;
import com.houzicore.shared.core.projectile.ProjectileUser;
import com.houzicore.shared.recharge.Recharge;
import com.houzicore.lobby.hub.HubManager;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.kyori.adventure.title.Title;

public class StackerManager extends MiniPlugin implements IThrown
{
	private static final Title.Times STACKER_SUBTITLE_TIMES = Title.Times.times(
			Duration.ofMillis(100),
			Duration.ofMillis(1400),
			Duration.ofMillis(250));

	public HubManager Manager;

	private ProjectileManager _projectileManager;

	private HashSet<Entity> _tempStackShift = new HashSet<Entity>();
	
	public StackerManager(HubManager manager)
	{
		super("Stacker", manager.getPlugin());

		Manager = manager;

		_projectileManager = new ProjectileManager(manager.getPlugin());
	} 
	
	@EventHandler
	public void GrabEntity(PlayerInteractEntityEvent event)
	{
		if (event.isCancelled())
			return;

		Entity stackee = event.getRightClicked();
		if (stackee == null)
			return;

		if (!(stackee instanceof LivingEntity))
			return;
		
		if (stackee instanceof Horse)
			return;
		
		if (stackee instanceof EnderDragon)
			return;

		if (stackee instanceof Player && ((Player)stackee).getGameMode() != GameMode.SURVIVAL)
			return;
		
		Player stacker = event.getPlayer();

		if (stacker.getGameMode() != GameMode.SURVIVAL)
			return;
		
		if (UtilGear.isMat(stacker.getItemInHand(), Material.SNOWBALL))
			return;
		
		if (Manager.getArenaManager() != null && (Manager.getArenaManager().isPlayerInMatch(stacker) || (stackee instanceof Player && Manager.getArenaManager().isPlayerInMatch((Player)stackee)))) return;
		StackerEvent stackerEvent = new StackerEvent(stacker);		
		Bukkit.getServer().getPluginManager().callEvent(stackerEvent);
		if (stackerEvent.isCancelled())
			return;

		//Parkour Disable
		if (Manager.GetParkour().InsideParkour(stacker.getLocation()))
		{
			UtilPlayer.message(stacker, F.main("Parkour", com.houzicore.shared.core.lang.LangManager.get().isThai(stacker) ? "§cคุณไม่สามารถโยนหรือขี่คอใกล้โซน Parkour ได้" : "§cYou cannot stack or throw near the Parkour zone."));
			return;
		}

		if (!Manager.CanBump(stacker))
		{
			showStackerSubtitle(stacker, isThai(stacker) ? "§cคุณไม่ได้เปิดโหมด Stacker" : "§cStacker mode is disabled.");
			return;
		}

		if (stacker.getVehicle() != null || _tempStackShift.contains(stacker))
		{
			showStackerSubtitle(stacker, isThai(stacker) ? "§cคุณไม่สามารถขี่คอซ้อนกันได้" : "§cYou cannot stack while already stacked.");
			return;
		}

		if (Manager.GetGadget().getActive(stacker, GadgetType.Morph) instanceof MorphBlock)
		{
			showStackerSubtitle(stacker, isThai(stacker) ? "§cแปลงร่างเป็นบล็อกอยู่ ยกไม่ได้" : "§cYou cannot stack while morphed as a block.");
			return;
		}

		if (Manager.GetTreasure().isOpening(stacker))
			return;

		stackerEvent = new StackerEvent(stackee);		
		Bukkit.getServer().getPluginManager().callEvent(stackerEvent);
		if (stackerEvent.isCancelled())
			return;

		if (stackee instanceof Player)
		{
			if (!Manager.CanBump(((Player)stackee)))
			{
				showStackerSubtitle(stacker, "§e" + UtilEnt.getName(stackee) + (isThai(stacker) ? " §cไม่ได้เปิดโหมด Stacker" : " §chas Stacker disabled."));
				return;
			}

			if (Manager.GetTreasure().isOpening((Player) stackee))
			{
				showStackerSubtitle(stacker, "§e" + UtilEnt.getName(stackee) + (isThai(stacker) ? " §cกำลังเปิดกล่องสมบัติอยู่" : " §cis opening treasure."));
				return;
			}
		}		
		
		if (stackee instanceof LivingEntity)
		{
			if (LobbyNpcManager.isLobbyNpc(stackee)) {
				return;
			}
			if (Manager.getPetManager().getPets().contains(stackee) || stackee instanceof Wither || stackee instanceof EnderDragon || ((LivingEntity)stackee).isCustomNameVisible())
			{
				showStackerSubtitle(stacker, isThai(stacker) ? "§cยกสิ่งมีชีวิตนี้ไม่ได้" : "§cYou cannot stack this entity.");
				return;
			}
		}

		while (stackee.getVehicle() != null)
			stackee = stackee.getVehicle();

		if (stackee.equals(stacker))
			return;

		Entity top = stacker;
		while (!top.getPassengers().isEmpty())
			top = top.getPassengers().get(0);

		if (!Recharge.Instance.use(stacker, "Stacker", 500, true, false))
			return;

		top.addPassenger(stackee);

		if (stackee instanceof Player) {
			Player pStackee = (Player) stackee;
			showStackerSubtitle(stacker, isThai(stacker) ? "§6กำลังยก §e" + pStackee.getName() : "§6Carrying §e" + pStackee.getName());
			showStackerSubtitle(pStackee, isThai(pStackee) ? "§e" + stacker.getName() + " §7กำลังยกคุณ • §fย่อเพื่อลง" : "§e" + stacker.getName() + " §7is carrying you • §fCrouch to dismount");
		}

		//Portal Delay
		Manager.SetPortalDelay(stacker);
		Manager.SetPortalDelay(stackee);

		event.setCancelled(true);
	}

	@EventHandler
	public void ThrowEntity(PlayerInteractEvent event)
	{
		if (!UtilEvent.isAction(event, ActionType.L))
			return;

		Player thrower = event.getPlayer();

		if (thrower.getVehicle() != null)
			return;

		if (thrower.getPassengers().isEmpty())
			return;
		Entity throwee = thrower.getPassengers().get(0);
		
		if (Manager.getArenaManager() != null && Manager.getArenaManager().isPlayerInMatch(thrower)) return;
		StackerEvent stackerEvent = new StackerEvent(thrower);		
		Bukkit.getServer().getPluginManager().callEvent(stackerEvent);
		if (stackerEvent.isCancelled())
			return;

		thrower.eject();

		Entity throweeStack = throwee.getPassengers().isEmpty() ? null : throwee.getPassengers().get(0);
		if (throweeStack != null)
		{
			throwee.eject();
			throweeStack.leaveVehicle();

			final Entity fThrower = thrower;
			final Entity fThroweeStack = throweeStack;

			_tempStackShift.add(throweeStack);

			getPlugin().getServer().getScheduler().scheduleSyncDelayedTask(getPlugin(), new Runnable()
			{
				public void run()
				{
					fThrower.addPassenger(fThroweeStack);
					_tempStackShift.remove(fThroweeStack);
				}
			}, 2);
		}

		//Parkour Disable
		if (Manager.GetParkour().InsideParkour(thrower.getLocation()))
		{
			UtilPlayer.message(thrower, F.main("Parkour", com.houzicore.shared.core.lang.LangManager.get().isThai(thrower) ? "§cคุณไม่สามารถโยนหรือขี่คอใกล้โซน Parkour ได้" : "§cYou cannot stack or throw near the Parkour zone."));
			return;
		}

		if (throwee instanceof Player) {
			Player pThrowee = (Player) throwee;
			showStackerSubtitle(thrower, isThai(thrower) ? "§6โยน §e" + pThrowee.getName() + " §6แล้ว" : "§6Launched §e" + pThrowee.getName());
			showStackerSubtitle(pThrowee, isThai(pThrowee) ? "§7ถูกโยนโดย §e" + thrower.getName() : "§7Launched by §e" + thrower.getName());
		}

		UtilAction.velocity(throwee, thrower.getLocation().getDirection(), 1.8, false, 0, 0.3, 2, false);

		_projectileManager.AddThrow(throwee, thrower, this, -1, true, false, true, false, 0.5f);

		//Portal Delay
		Manager.SetPortalDelay(thrower);
		Manager.SetPortalDelay(throwee);
	}

	@Override
	public void Collide(LivingEntity target, Block block, ProjectileUser data) 
	{
		if (target == null)
			return;
		
		Entity topRider = target.getPassengers().isEmpty() ? null : target.getPassengers().get(0);
		if (target.getCustomName() != null || (topRider instanceof LivingEntity && ((LivingEntity)topRider).getCustomName() != null))
			return;
		
		if (!Manager.CanBump(target))
			return;

		//Velocity
		UtilAction.velocity(target, UtilAlg.getTrajectory2d(data.GetThrown(), target), 1, true, 0.8, 0, 10, true);

		for (Entity rider : new java.util.ArrayList<>(target.getPassengers()))
		{
			//Portal Delay
			Manager.SetPortalDelay(rider);
			
			rider.leaveVehicle();
			rider.setVelocity(new Vector(0.25 - Math.random()/2, Math.random()/2, 0.25 - Math.random()/2));
		}

		if (target instanceof Player) {
			Player pTarget = (Player) target;
			showStackerSubtitle(pTarget,
				"§e" + UtilEnt.getName(data.GetThrower()) +
				(isThai(pTarget) ? " §7ปา §e" : " §7threw §e") +
				UtilEnt.getName(data.GetThrown()) +
				(isThai(pTarget) ? " §7ใส่คุณ" : " §7at you"));
		}

		//Effect
		data.GetThrown().getWorld().playSound(data.GetThrown().getLocation(), Sound.ENTITY_PLAYER_HURT, 1f, 1f);
		
		//Portal Delay
		Manager.SetPortalDelay(target);
	}

	@Override
	public void Idle(ProjectileUser data) 
	{

	}

	@Override
	public void Expire(ProjectileUser data) 
	{

	}

	private void showStackerSubtitle(Player player, String legacySubtitle)
	{
		if (player == null || !player.isOnline())
			return;

		player.showTitle(Title.title(
				Component.empty(),
				LegacyComponentSerializer.legacySection().deserialize(legacySubtitle),
				STACKER_SUBTITLE_TIMES));
	}

	private boolean isThai(Player player)
	{
		return LangManager.get() != null && LangManager.get().isThai(player);
	}
}
