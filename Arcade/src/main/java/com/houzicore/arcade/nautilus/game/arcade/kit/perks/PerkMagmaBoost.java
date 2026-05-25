package com.houzicore.arcade.nautilus.game.arcade.kit.perks;

import java.util.HashMap;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;

import com.houzicore.shared.api.disguise.DisguiseRequest;
import com.houzicore.shared.common.util.C;
import com.houzicore.shared.common.util.UtilParticle;
import com.houzicore.shared.common.util.UtilPlayer;
import com.houzicore.shared.common.util.UtilServer;
import com.houzicore.shared.common.util.UtilParticle.ParticleType;
import com.houzicore.shared.common.util.UtilParticle.ViewDist;
import com.houzicore.shared.updater.UpdateType;
import com.houzicore.shared.updater.event.UpdateEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import com.houzicore.arcade.nautilus.game.arcade.kit.Perk;

public class PerkMagmaBoost extends Perk
{
	private HashMap<Player, Integer> _kills = new HashMap<Player, Integer>();
	
	public PerkMagmaBoost() 
	{
		super("Fuel the Fire", new String[] 
				{ 
				C.cGray + "Kills give +1 Damage, -15% Knockback Taken and +1 Size.",
				C.cGray + "Kill bonuses can stack 3 times, and reset on death.",
				});
	}

	@EventHandler
	public void Kill(PlayerDeathEvent event)
	{
		if (!(event.getEntity() instanceof Player)) return;
		Player killed = (Player)event.getEntity();
		
		_kills.remove(killed);

		if (event.getEntity().getKiller() == null)
			return;
		
		Player killer = UtilPlayer.searchExact(event.getEntity().getKiller().getName());

		if (killer == null || killer.equals(killed) || !Kit.HasKit(killer))
			return;

		int size = 1;
		if (_kills.containsKey(killer))
			size += _kills.get(killer);

		size = Math.min(3, size);
		
		_kills.put(killer, size);
		
		// Re-apply disguise with updated slimeSize attribute via the bridge.
		final int finalSize = size;
		Manager.GetDisguise().getService().getActiveSession(killer.getUniqueId()).ifPresent(session -> {
			DisguiseRequest updated = session.request().withAttribute("slimeSize", String.valueOf(finalSize + 1));
			Manager.GetDisguise().getService().apply(killer, updated);
		});
		
		killer.setExp(0.99f * (size/3f));
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	public void SizeDamage(EntityDamageByEntityEvent event)
	{	
		if (event.isCancelled())
			return;
		
		if (event.getCause() != DamageCause.ENTITY_ATTACK)
			return;

		if (!(event.getDamager() instanceof Player)) return;
		Player damager = ((Player) event.getDamager());
		if (damager == null)			return;

		if (!Kit.HasKit(damager))
			return;

		if (!_kills.containsKey(damager))
			return;
		
		int bonus = _kills.get(damager);

  // /* event.AddMod(...) */, GetName(), bonus, false);
	}
	
	@EventHandler(priority = EventPriority.HIGHEST)
	public void SizeKnockback(EntityDamageByEntityEvent event)
	{	
		if (event.isCancelled())
			return;
		
		if (event.getCause() != DamageCause.ENTITY_ATTACK)
			return;

		if (!(event.getEntity() instanceof Player)) return;
		Player damagee = ((Player) event.getEntity());
		if (damagee == null)			return;

		if (!Kit.HasKit(damagee))
			return;

		if (!_kills.containsKey(damagee))
			return;
		
		int bonus = _kills.get(damagee);

  // /* event.AddKnockback(...) */, bonus*0.15d);
	}
	
	@EventHandler
	public void EnergyUpdate(UpdateEvent event)
	{
		if (event.getType() != UpdateType.SEC && event.getType() != UpdateType.FAST && event.getType() != UpdateType.FASTER && event.getType() != UpdateType.FASTEST)
			return;
		
		for (Player player : UtilServer.getPlayers())
		{
			if (!Kit.HasKit(player))
				continue;
			
			float size = 0;
			if (_kills.containsKey(player))
				size += _kills.get(player);
			
			if (size == 0 && event.getType() == UpdateType.SEC)
				UtilParticle.PlayParticle(ParticleType.LAVA, player.getLocation().add(0,0.4,0), 0.15f + 0.15f * size, 0.15f + 0.15f * size, 0.15f + 0.15f * size, 0, 1,
						ViewDist.LONG, UtilServer.getPlayers());
			else if (size == 1 && event.getType() == UpdateType.FAST)
				UtilParticle.PlayParticle(ParticleType.LAVA, player.getLocation().add(0,0.4,0), 0.15f + 0.15f * size, 0.15f + 0.15f * size, 0.15f + 0.15f * size, 0, 1,
						ViewDist.LONG, UtilServer.getPlayers());
			else if (size == 2 && event.getType() == UpdateType.FASTER)
				UtilParticle.PlayParticle(ParticleType.LAVA, player.getLocation().add(0,0.4,0), 0.15f + 0.15f * size, 0.15f + 0.15f * size, 0.15f + 0.15f * size, 0, 1,
						ViewDist.LONG, UtilServer.getPlayers());
			else if (size == 3 && event.getType() == UpdateType.FASTEST)
				UtilParticle.PlayParticle(ParticleType.LAVA, player.getLocation().add(0,0.4,0), 0.15f + 0.15f * size, 0.15f + 0.15f * size, 0.15f + 0.15f * size, 0, 1,
						ViewDist.LONG, UtilServer.getPlayers());
		}
	}
}
