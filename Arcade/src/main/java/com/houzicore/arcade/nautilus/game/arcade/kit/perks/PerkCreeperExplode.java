package com.houzicore.arcade.nautilus.game.arcade.kit.perks;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;

import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.util.Vector;

import com.houzicore.shared.api.disguise.DisguiseRequest;
import com.houzicore.shared.common.util.C;
import com.houzicore.shared.common.util.F;
import com.houzicore.shared.common.util.UtilAction;
import com.houzicore.shared.common.util.UtilBlock;
import com.houzicore.shared.common.util.UtilMath;
import com.houzicore.shared.common.util.UtilParticle;
import com.houzicore.shared.common.util.UtilPlayer;
import com.houzicore.shared.common.util.UtilServer;
import com.houzicore.shared.common.util.UtilParticle.ParticleType;
import com.houzicore.shared.common.util.UtilParticle.ViewDist;
import com.houzicore.shared.recharge.Recharge;
import com.houzicore.shared.updater.UpdateType;
import com.houzicore.shared.updater.event.UpdateEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import com.houzicore.arcade.nautilus.game.arcade.kit.SmashPerk;

public class PerkCreeperExplode extends SmashPerk
{
	private HashMap<Player, Long> _active = new HashMap<Player, Long>();
	private HashSet<Player> _super = new HashSet<Player>();
	
	public PerkCreeperExplode() 
	{
		super("Explode", new String[] 
				{ 
				C.cYellow + "Right-Click" + C.cGray + " with Shovel use " + C.cGreen + "Explosive Leap"
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

		if (!event.getPlayer().getItemInHand().getType().toString().contains("_SHOVEL"))
			return;

		Player player = event.getPlayer();
		
		if (isSuperActive(player))
			return;

		if (!Kit.HasKit(player))
			return;

		if (!Recharge.Instance.use(player, GetName(), 8000, true, true))
			return;

		_active.put(player, System.currentTimeMillis());

		IncreaseSize(player);

		UtilPlayer.message(player, F.main("Skill", "You are charging " + F.skill(GetName()) + "."));
	}

	@Override
	public void addSuperCustom(Player player)
	{
		_active.put(player, System.currentTimeMillis());
		_super.add(player);

		IncreaseSize(player);
	}
	
	@EventHandler
	public void Update(UpdateEvent event)  
	{
		if (event.getType() != UpdateType.TICK)
			return;

		Iterator<Player> chargeIterator = _active.keySet().iterator();

		while (chargeIterator.hasNext())
		{
			Player player = chargeIterator.next();

			double elapsed = (System.currentTimeMillis() - _active.get(player))/1000d;

			//Idle in Air
			player.setVelocity(new Vector(0,0,0));
			
			//Sound
			player.getWorld().playSound(player.getLocation(), Sound.ENTITY_CREEPER_PRIMED, (float)(0.5 + elapsed), (float)(0.5 + elapsed));

			IncreaseSize(player);
			
			player.setExp(Math.min(0.99f, (float)(elapsed/1.5)));

			//Not Detonated
			if (elapsed < 1.5)
				continue;

			chargeIterator.remove();

			//Unpower
			DecreaseSize(player);

			boolean isSuper = _super.remove(player);
			
			//Explode
			if (!isSuper)
			{
				//Effect
				UtilParticle.PlayParticle(ParticleType.HUGE_EXPLOSION, player.getLocation(), 0, 0, 0, 0, 1,
						ViewDist.MAX, UtilServer.getPlayers());
				player.getWorld().playSound(player.getLocation(), Sound.ENTITY_GENERIC_EXPLODE, 2f, 1f);
			}
			else
			{
				//Particles
				UtilParticle.PlayParticle(ParticleType.HUGE_EXPLOSION, player.getLocation(), 5f, 5f, 5f, 0, 20,
						ViewDist.MAX, UtilServer.getPlayers());
				
				//Sound
				for (int i=0 ; i<4 ; i++)
					player.getWorld().playSound(player.getLocation(), Sound.ENTITY_GENERIC_EXPLODE, (float)(2 + Math.random()*4), (float)(Math.random() + 0.2));
				
				//Blocks
				Manager.GetExplosion().BlockExplosion(UtilBlock.getInRadius(player.getLocation(), 12).keySet(), player.getLocation(), false);
				
				//Remove Spawns
				Iterator<Location> spawnIterator = Manager.GetGame().GetTeam(player).GetSpawns().iterator();
				while (spawnIterator.hasNext())
				{
					Location spawn = spawnIterator.next();
					
					if (UtilMath.offset(player.getLocation(), spawn) < 14)
						spawnIterator.remove();
				}
			}
			
			//Damage
			for (Entity ent : player.getWorld().getEntities())
			{
				if (!(ent instanceof LivingEntity))
					continue;
				
				if (ent.equals(player))
					continue;

				double dist = UtilMath.offset(player.getLocation(), ent.getLocation());
				
				double maxRange = 8;
				if (isSuper)
					maxRange = 24;
				
				double damage = 20;
				if (isSuper)
					damage = 30;
				
				if (dist > maxRange)
					continue;

				if (ent instanceof Player)
					if (!Manager.GetGame().IsAlive((Player)ent))
						continue;

				LivingEntity livingEnt = (LivingEntity)ent;
				
				double scale = 0.1 + 0.9 * ((maxRange-dist)/maxRange);
				
				//Damage Event
				Manager.GetDamage().NewDamageEvent(livingEnt, player, null,
						DamageCause.CUSTOM, damage * scale, true, true, false,
						player.getName(), isSuper ? "Atomic Blast" : GetName());
			}
			
			//Velocity
			UtilAction.velocity(player, 1.8, 0.2, 1.4, true);
			
			//Inform
			if (!isSuper)
				UtilPlayer.message(player, F.main("Skill", "You used " + F.skill(GetName()) + "."));
		}
	}

	public void DecreaseSize(Player player)
	{
		setCreeperIgnited(player, false);
	}
	
	public void IncreaseSize(Player player)
	{
		setCreeperIgnited(player, true);
	}

	private void setCreeperIgnited(Player player, boolean ignited)
	{
		Manager.GetDisguise().getService().getActiveSession(player.getUniqueId())
				.map(session -> session.request())
				.filter(request -> "CREEPER".equalsIgnoreCase(request.variantKey()))
				.filter(request -> !String.valueOf(ignited).equals(request.attributes().get("creeperIgnited")))
				.map(request -> request.withAttribute("creeperIgnited", String.valueOf(ignited)))
				.ifPresent(request -> applyUpdatedDisguise(player, request));
	}

	private void applyUpdatedDisguise(Player player, DisguiseRequest request)
	{
		Manager.GetDisguise().getService().apply(player, request);
	}
	
	@EventHandler
	public void Knockback(EntityDamageByEntityEvent event)
	{
		if (event.getCause().name() == null || !event.getCause().name().contains(GetName()))
			return;
		
  // /* event.AddKnockback(...) */, 2.5);
	}
	
	@EventHandler
	public void Death(PlayerDeathEvent event)
	{
		if (!Kit.HasKit(event.getEntity()))
			return;
		
		_active.remove(event.getEntity());
		_super.remove(event.getEntity());
		
		DecreaseSize(event.getEntity());
	}
}
