package com.houzicore.arcade.nautilus.game.arcade.kit.perks;

import java.util.HashSet;
import java.util.Iterator;

import org.bukkit.Color;
import org.bukkit.DyeColor;
import org.bukkit.FireworkEffect;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.FireworkEffect.Type;
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
import com.houzicore.shared.common.util.UtilEnt;
import com.houzicore.shared.common.util.UtilFirework;
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
import com.houzicore.arcade.nautilus.game.arcade.kit.Perk;

public class PerkLazer extends Perk
{
	private double _range;
	private long _recharge;
	
	private HashSet<Player> _active = new HashSet<Player>();

	public PerkLazer()
	{
		this(12, 10000);
	}

	public PerkLazer(double range, long recharge) 
	{
		super("Static Lazer", new String[] 
				{
				C.cYellow + "Hold Block" + C.cGray + " with Sword to use " + C.cGreen + "Static Lazer"
				});

		_range = range;
		_recharge = recharge;
	}

	@EventHandler
	public void skill(PlayerInteractEvent event)
	{
		if (event.isCancelled())
			return;

		if (event.getAction() != Action.RIGHT_CLICK_AIR && event.getAction() != Action.RIGHT_CLICK_BLOCK)
			return;

		if (UtilBlock.usable(event.getClickedBlock()))
			return;

		if (event.getPlayer().getItemInHand() == null)
			return;

		if (!event.getPlayer().getItemInHand().getType().toString().contains("_SWORD"))
			return;

		Player player = event.getPlayer();

		if (!Kit.HasKit(player))
			return;
		
		if (!Recharge.Instance.use(player, GetName(), _recharge, true, true))
			return;
		
		_active.add(player);
	}
	
	@EventHandler
	public void chargeFire(UpdateEvent event)
	{
		if (event.getType() != UpdateType.TICK)
			return;
		
		Iterator<Player> playerIterator = _active.iterator();
		
		while (playerIterator.hasNext())
		{
			Player player = playerIterator.next();
			
			if (player.isBlocking())
			{
				player.setExp(Math.min(0.999f, player.getExp() + 0.035f));
				
				player.getWorld().playSound(player.getLocation(), Sound.BLOCK_FIRE_EXTINGUISH, 0.25f + player.getExp(), 0.75f + player.getExp());
				
				//Wool
				setWoolColor(player, Math.random() > 0.5 ? DyeColor.YELLOW : DyeColor.BLACK);
			}
				
			if (player.getExp() >= 0.999f)
				{
					playerIterator.remove();
					fire(player);
				}
			// }
			// else
			// {
				playerIterator.remove();
				fire(player);
			// }
		}
	}
	
	public void fire(Player player)
	{	
		if (player.getExp() <= 0.2f)
		{
			setWoolColor(player, DyeColor.WHITE);
			player.setExp(0f);
			return;
		}
		
		double curRange = 0;
		while (curRange <= _range * player.getExp())
		{
			Location newTarget = player.getEyeLocation().add(player.getLocation().getDirection().multiply(curRange));

			//Hit Player
			boolean hitPlayer = false;
			for (Player other : Manager.GetGame().GetPlayers(true))
			{
				if (other.equals(player))
					continue;
				
				if (UtilMath.offset(newTarget, other.getLocation().add(0, 1, 0)) < 3)
				{
					hitPlayer = true;
					break;
				}
			}
			if (hitPlayer)
				break;
			
			//Hit Block
			if (!UtilBlock.airFoliage(newTarget.getBlock()))
			{
				break;
			}
				
			//Progress Forwards
			curRange += 0.2;

			//Smoke Trail
			UtilParticle.PlayParticle(ParticleType.FIREWORKS_SPARK, newTarget, 0, 0, 0, 0, 1,
					ViewDist.MAX, UtilServer.getPlayers());
		}

		//Destination
		Location target = player.getLocation().add(player.getLocation().getDirection().multiply(curRange));
		
		UtilParticle.PlayParticle(ParticleType.EXPLODE, target, 0, 0, 0, 0, 1,
				ViewDist.MAX, UtilServer.getPlayers());
		
		//Firework 
		UtilFirework.playFirework(player.getLocation().add(player.getLocation().getDirection().multiply(Math.max(0, curRange - 0.6))), Type.BURST, Color.YELLOW, false, false);
		
		for (LivingEntity other : UtilEnt.getInRadius(target, 5).keySet())
		{
			if (other.equals(player))
				continue;

			//Do from center
			if (UtilMath.offset(target, other.getLocation().add(0, 1, 0)) < 3.5)
			{
				//Damage Event
				Manager.GetDamage().NewDamageEvent(other, player, null,
						DamageCause.CUSTOM, player.getExp() * 7, true, true, false,
						player.getName(), GetName());
			}
		}
			
		//Inform
		UtilPlayer.message(player, F.main("Game", "You fired " + F.skill(GetName()) + "."));
		
		//Sound
		player.getWorld().playSound(player.getEyeLocation(), Sound.ENTITY_ZOMBIE_VILLAGER_CURE, 0.5f + player.getExp(), 1.75f - player.getExp());
		player.getWorld().playSound(player.getLocation(), Sound.ENTITY_SHEEP_AMBIENT, 2f, 1.5f);
		
		//Wool
		setWoolColor(player, DyeColor.WHITE);
		player.setExp(0f);
	}
	
	@EventHandler
	public void knockback(EntityDamageByEntityEvent event)
	{
		if (event.getCause().name() == null || !event.getCause().name().contains(GetName()))
			return;

  // /* event.AddKnockback(...) */, 3);
	}
	
	
	public void setWoolColor(Player player, DyeColor color)
	{
		Manager.GetDisguise().getService().getActiveSession(player.getUniqueId())
				.map(session -> session.request())
				.filter(request -> "SHEEP".equalsIgnoreCase(request.variantKey()))
				.map(request -> request.withAttribute("sheared", "false").withAttribute("sheepColor", color.name()))
				.ifPresent(request -> applyUpdatedDisguise(player, request));
	}

	private void applyUpdatedDisguise(Player player, DisguiseRequest request)
	{
		Manager.GetDisguise().getService().apply(player, request);
	}
}
