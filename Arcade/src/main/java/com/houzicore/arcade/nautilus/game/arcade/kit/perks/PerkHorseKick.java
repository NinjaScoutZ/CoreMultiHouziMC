package com.houzicore.arcade.nautilus.game.arcade.kit.perks;

import java.util.HashMap;
import java.util.Iterator;

import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.player.PlayerInteractEvent;

import com.houzicore.shared.api.disguise.DisguiseRequest;
import com.houzicore.shared.common.util.C;
import com.houzicore.shared.common.util.F;
import com.houzicore.shared.common.util.UtilBlock;
import com.houzicore.shared.common.util.UtilMath;
import com.houzicore.shared.common.util.UtilParticle;
import com.houzicore.shared.common.util.UtilServer;
import com.houzicore.shared.common.util.UtilParticle.ParticleType;
import com.houzicore.shared.common.util.UtilParticle.ViewDist;
import com.houzicore.shared.common.util.UtilPlayer;
import com.houzicore.shared.common.util.UtilTime;
import com.houzicore.shared.recharge.Recharge;
import com.houzicore.shared.updater.UpdateType;
import com.houzicore.shared.updater.event.UpdateEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import com.houzicore.arcade.nautilus.game.arcade.kit.SmashPerk;

public class PerkHorseKick extends SmashPerk
{
	private HashMap<Player, Long> _active = new HashMap<Player, Long>();

	public PerkHorseKick() 
	{
		super("Bone Kick", new String[] 
				{ 
				C.cYellow + "Right-Click" + C.cGray + " with Axe to use " + C.cGreen + "Bone Kick"
				});
	}
	
	@EventHandler
	public void Activate(PlayerInteractEvent event)
	{
		if (event.isCancelled())
			return;

		if (event.getAction() != Action.RIGHT_CLICK_AIR && event.getAction() != Action.RIGHT_CLICK_BLOCK)
			return;

		if (UtilBlock.usable(event.getClickedBlock()))
			return;

		if (!event.getPlayer().getItemInHand().getType().toString().contains("_AXE"))
			return;

		Player player = event.getPlayer();
		
		if (isSuperActive(player))
			return;

		if (!Kit.HasKit(player))
			return;

		if (!Recharge.Instance.use(player, GetName(), 6000, true, true))
			return;

		//Horse Animation
		setHorseRearing(player, true);

		//Animation
		_active.put(player, System.currentTimeMillis());


		//AoE Area
		Location loc = player.getLocation();
		loc.add(player.getLocation().getDirection().setY(0).normalize().multiply(1.5));
		loc.add(0, 0.8, 0);

		for (Entity other : player.getWorld().getEntities())
		{
			if (!(other instanceof LivingEntity))
				continue;

			if (other instanceof Player)
				if (!Manager.GetGame().IsAlive((Player)other))
					continue;
			
			if (other.equals(player))
				continue;

			if (UtilMath.offset(loc, other.getLocation()) > 2.5)
				continue;

			//Damage Event
			Manager.GetDamage().NewDamageEvent((LivingEntity)other, player, null,
					DamageCause.CUSTOM, 7, true, true, false,
					player.getName(), GetName());
			
			//Sound
			player.getWorld().playSound(player.getLocation(), Sound.ENTITY_SKELETON_HURT, 4f, 0.6f);
			player.getWorld().playSound(player.getLocation(), Sound.ENTITY_SKELETON_HURT, 4f, 0.6f);

			//Inform
			UtilPlayer.message(other, F.main("Skill", F.name(player.getName()) + " hit you with " + F.skill(GetName()) + "."));				
		}

		//Inform
		UtilPlayer.message(player, F.main("Skill", "You used " + F.skill(GetName()) + "."));
		
		//Slow
		Manager.GetCondition().Factory().Slow(GetName(), player, player, 0.8, 3, false, false, true, false);
	}

	@EventHandler
	public void Update(UpdateEvent event)  
	{
		if (event.getType() != UpdateType.TICK)
			return;

		//Player
		Iterator<Player> playerIterator = _active.keySet().iterator();
		
		while (playerIterator.hasNext())
		{
			Player player = playerIterator.next();
			
			if (!player.isValid() || player.getHealth() <= 0 || UtilTime.elapsed(_active.get(player), 1000))
			{
				playerIterator.remove();
				
				//Horse Animation
				setHorseRearing(player, false);
				
				Manager.GetCondition().EndCondition(player, null, GetName());
			}
			else
			{
				Location loc = player.getLocation();
				loc.add(player.getLocation().getDirection().setY(0).normalize().multiply(1.5));
				loc.add(0, 0.8, 0);
				
				UtilParticle.PlayParticle(ParticleType.LARGE_SMOKE, loc, 0.3f, 0.3f, 0.3f, 0, 2,
						ViewDist.LONG, UtilServer.getPlayers());
			}
		}
	}

	private void setHorseRearing(Player player, boolean rearing)
	{
		Manager.GetDisguise().getService().getActiveSession(player.getUniqueId())
				.map(session -> session.request())
				.filter(request -> "HORSE".equalsIgnoreCase(request.variantKey()))
				.filter(request -> !String.valueOf(rearing).equals(request.attributes().get("horseRearing")))
				.map(request -> request.withAttribute("horseRearing", String.valueOf(rearing)))
				.ifPresent(request -> applyUpdatedDisguise(player, request));
	}

	private void applyUpdatedDisguise(Player player, DisguiseRequest request)
	{
		Manager.GetDisguise().getService().apply(player, request);
	}

	@EventHandler
	public void Knockback(EntityDamageByEntityEvent event)
	{
		if (event.getCause().name() == null || (!event.getCause().name().contains(GetName()) && !event.getCause().name().contains("Flame Kick")))
			return;

  // /* event.AddKnockback(...) */, 4);
	}
}
