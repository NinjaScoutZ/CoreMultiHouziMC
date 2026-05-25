package com.houzicore.arcade.nautilus.game.arcade.kit.perks;

import java.util.HashMap;
import java.util.Iterator;

import org.bukkit.DyeColor;
import org.bukkit.Effect;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Horse;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.util.Vector;

import com.houzicore.shared.api.disguise.DisguiseRequest;
import com.houzicore.shared.common.util.C;
import com.houzicore.shared.common.util.F;
import com.houzicore.shared.common.util.UtilAction;
import com.houzicore.shared.common.util.UtilEnt;
import com.houzicore.shared.common.util.UtilMath;
import com.houzicore.shared.common.util.UtilParticle;
import com.houzicore.shared.common.util.UtilPlayer;
import com.houzicore.shared.common.util.UtilServer;
import com.houzicore.shared.common.util.UtilTime;
import com.houzicore.shared.common.util.UtilParticle.ParticleType;
import com.houzicore.shared.common.util.UtilParticle.ViewDist;
import com.houzicore.shared.recharge.Recharge;
import com.houzicore.shared.updater.UpdateType;
import com.houzicore.shared.updater.event.UpdateEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import com.houzicore.arcade.nautilus.game.arcade.kit.Perk;

public class PerkWoolCloud extends Perk
{
	private HashMap<Player, Long> _active = new HashMap<Player, Long>();
	
	public PerkWoolCloud() 
	{
		super("Wooly Rocket", new String[]  
				{
				C.cYellow + "Right-Click" + C.cGray + " with Spade to " + C.cGreen + "Wooly Rocket"
				});
	}
	
	@EventHandler
	public void Leap(PlayerInteractEvent event)
	{
		if (event.isCancelled())
			return;
		
		if (event.getAction() != Action.RIGHT_CLICK_AIR && event.getAction() != Action.RIGHT_CLICK_BLOCK)
			return;

		if (event.getPlayer().getItemInHand() == null)
			return;
		
		if (!event.getPlayer().getItemInHand().getType().toString().contains("_SHOVEL"))
			return;
		
		Player player = event.getPlayer();
		
		if (!Kit.HasKit(player))
			return;
	
		//Recharge
		if (!Recharge.Instance.use(player, GetName(), 10000, true, true))
			return;
		
		UtilAction.velocity(player, new Vector(0,1,0), 1, false, 0, 0, 2, true);
		
		UtilPlayer.message(player, F.main("Skill", "You used " + F.skill(GetName()) + "."));
		
		player.getWorld().playEffect(player.getLocation(), Effect.BLAZE_SHOOT, 0);
		
		//Allow double jump
		player.setAllowFlight(true);
		
		setWoolColor(player, DyeColor.RED);
		
		_active.put(player, System.currentTimeMillis());
	}
	
	@EventHandler
	public void update(UpdateEvent event)
	{
		if (event.getType() != UpdateType.TICK)
			return;
		
		if (Manager.GetGame() == null)
			return;
		
		Iterator<Player> playerIterator = _active.keySet().iterator();
		while (playerIterator.hasNext())
		{
			Player player = playerIterator.next();
			
			UtilParticle.PlayParticle(ParticleType.FLAME, player.getLocation(), 0.2f, 0.2f, 0.2f, 0, 4,
					ViewDist.LONGER, UtilServer.getPlayers());
			
			if (!UtilTime.elapsed(_active.get(player), 200))
				continue;
			
			for (Player other : Manager.GetGame().GetPlayers(true))
			{
				if (player.equals(other))
					continue;
				
				if (UtilMath.offset(player, other) < 2)
				{
					//Damage Event
					Manager.GetDamage().NewDamageEvent(other, player, null,
							DamageCause.CUSTOM, 8, true, false, false,
							player.getName(), GetName());
					
					UtilParticle.PlayParticle(ParticleType.EXPLODE, other.getLocation(), 0f, 0f, 0f, 0, 1,
							ViewDist.MAX, UtilServer.getPlayers());
					UtilParticle.PlayParticle(ParticleType.LAVA, player.getLocation(), 0.2f, 0.2f, 0.2f, 0, 10,
							ViewDist.MAX, UtilServer.getPlayers());
				}
			}
			
			if (UtilEnt.isGrounded(player) || UtilTime.elapsed(_active.get(player), 1200))
			{
				playerIterator.remove();
				setWoolColor(player, DyeColor.WHITE);
			}
		}
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
	
	@EventHandler
	public void knockback(EntityDamageByEntityEvent event)
	{
		if (event.getCause().name() == null || !event.getCause().name().contains(GetName()))
			return;

  // /* event.AddKnockback(...) */, 2.5);
	}
}
