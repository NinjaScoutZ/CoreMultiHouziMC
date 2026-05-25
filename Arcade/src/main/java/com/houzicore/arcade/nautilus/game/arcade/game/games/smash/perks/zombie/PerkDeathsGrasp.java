package com.houzicore.arcade.nautilus.game.arcade.game.games.smash.perks.zombie;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerInteractEvent;

import com.houzicore.shared.common.util.C;
import com.houzicore.shared.common.util.F;
import com.houzicore.shared.common.util.UtilAction;
import com.houzicore.shared.common.util.UtilAlg;
import com.houzicore.shared.common.util.UtilBlock;
import com.houzicore.shared.common.util.UtilEnt;
import com.houzicore.shared.common.util.UtilEvent;
import com.houzicore.shared.common.util.UtilEvent.ActionType;
import com.houzicore.shared.common.util.UtilMath;
import com.houzicore.shared.common.util.UtilParticle;
import com.houzicore.shared.common.util.UtilParticle.ParticleType;
import com.houzicore.shared.common.util.UtilParticle.ViewDist;
import com.houzicore.shared.common.util.UtilPlayer;
import com.houzicore.shared.common.util.UtilServer;
import com.houzicore.shared.common.util.UtilTime;
import com.houzicore.shared.recharge.Recharge;
import com.houzicore.shared.updater.UpdateType;
import com.houzicore.shared.updater.event.UpdateEvent;
import com.houzicore.shared.core.damage.CustomDamageEvent;
import com.houzicore.arcade.nautilus.game.arcade.kit.Perk;

public class PerkDeathsGrasp extends Perk
{

	private static final int COOLDOWN = 12000;

	private Map<LivingEntity, Long> _live = new HashMap<>();
	private Map<LivingEntity, Long> _weakness = new HashMap<>();

	public PerkDeathsGrasp()
	{
		super("Deaths Grasp", new String[] { C.cYellow + "Left-Click" + C.cGray + " with Bow to use " + C.cGreen + "Deaths Grasp", C.cGray + "+100% Arrow Damage to enemies thrown by Deaths Grasp" });
	}

	@EventHandler
	public void leap(PlayerInteractEvent event)
	{
		if (event.isCancelled())
		{
			return;
		}

		if (!UtilEvent.isAction(event, ActionType.L))
		{
			return;
		}

		if (UtilBlock.usable(event.getClickedBlock()))
		{
			return;
		}

		Player player = event.getPlayer();

		if (player.getInventory().getItemInMainHand() == null || player.getInventory().getItemInMainHand().getType() != Material.BOW)
		{
			return;
		}

		if (!hasPerk(player))
		{
			return;
		}

		if (!Recharge.Instance.use(player, GetName(), COOLDOWN, true, true))
		{
			return;
		}

		UtilAction.velocity(player, player.getLocation().getDirection(), 1.4, false, 0, 0.2, 1.2, true);

		// Record
		_live.put(player, System.currentTimeMillis());

		// Inform
		UtilPlayer.message(player, F.main("Game", "You used " + F.skill(GetName()) + "."));

		// Effect
		player.getWorld().playSound(player.getLocation(), Sound.ENTITY_ZOMBIE_HURT, 1f, 1.4f);
	}

	@EventHandler
	public void end(UpdateEvent event)
	{
		if (event.getType() != UpdateType.TICK)
		{
			return;
		}

		List<Player> alivePlayers = Manager.GetGame().GetPlayers(true);

		// Collide
		for (Player player : alivePlayers)
		{
			if (!_live.containsKey(player))
			{
				continue;
			}

			for (Player other : alivePlayers)
			{
				if (Manager.isSpectator(other))
				{
					continue;
				}

				if (other.equals(player))
				{
					continue;
				}

				if (UtilMath.offsetSquared(player, other) < 4)
				{
					collide(player, other);
					_live.remove(player);
					return;
				}
			}
		}

		// Leap End
		Iterator<LivingEntity> leapIter = _live.keySet().iterator();

		while (leapIter.hasNext())
		{
			LivingEntity ent = leapIter.next();

			if (!UtilEnt.isGrounded(ent))
			{
				continue;
			}

			if (!UtilTime.elapsed(_live.get(ent), 1000))
			{
				continue;
			}
			
			leapIter.remove();
		}

		// Weakness End
		Iterator<LivingEntity> weaknessIter = _weakness.keySet().iterator();

		while (weaknessIter.hasNext())
		{
			LivingEntity ent = weaknessIter.next();

			if (!UtilEnt.isGrounded(ent))
			{
				continue;
			}
			
			if (!UtilTime.elapsed(_weakness.get(ent), 1000))
			{
				continue;
			}
			
			weaknessIter.remove();
		}
	}

	public void collide(Player damager, LivingEntity damagee)
	{
		// Damage Event
		Manager.GetDamage().NewDamageEvent(damagee, damager, null, DamageCause.CUSTOM, 6, false, true, false, damager.getName(), GetName());

		UtilAction.velocity(damagee, UtilAlg.getTrajectory2d(damagee, damager), 1.6, false, 0, 1.2, 1.8, true);

		damager.setVelocity(new org.bukkit.util.Vector(0, 0, 0));

		damager.getWorld().playSound(damager.getLocation(), Sound.ENTITY_ZOMBIE_HURT, 1f, 0.7f);

		_weakness.put(damagee, System.currentTimeMillis());

		// Inform
		UtilPlayer.message(damager, F.main("Game", "You hit " + F.name(UtilEnt.getName(damagee)) + " with " + F.skill(GetName()) + "."));
		UtilPlayer.message(damagee, F.main("Game", F.name(damager.getName()) + " hit you with " + F.skill(GetName()) + "."));

		Recharge.Instance.recharge(damager, GetName());
		Recharge.Instance.use(damager, GetName(), 2000, true, true);
	}

	@EventHandler(priority = EventPriority.HIGH)
	public void arrowDamage(CustomDamageEvent event)
	{
		if (event.GetProjectile() == null)
		{
			return;
		}
		
		if (!(event.GetProjectile() instanceof Arrow))
		{
			return;
		}
		
		if (!_weakness.containsKey(event.GetDamageeEntity()))
		{
			return;
		}
		
		Player damager = event.GetDamagerPlayer(true);
		
		if (damager == null)
		{
			return;
		}
		
		if (!hasPerk(damager))
		{
			return;
		}
		
		if (Manager.isSpectator(damager))
		{
			return;
		}
		
		event.AddMult(GetName(), GetName() + " Combo", 2, true);

		UtilParticle.PlayParticle(ParticleType.RED_DUST, event.GetDamageeEntity().getLocation(), 0.5f, 0.5f, 0.5f, 0, 20, ViewDist.MAX, UtilServer.getPlayers());
		UtilParticle.PlayParticle(ParticleType.LARGE_EXPLODE, event.GetDamageeEntity().getLocation(), 0, 0, 0, 0, 1, ViewDist.MAX, UtilServer.getPlayers());

		damager.getWorld().playSound(damager.getLocation(), Sound.ENTITY_ZOMBIE_HURT, 1f, 2f);
	}

	@EventHandler
	public void playerDeath(PlayerDeathEvent event)
	{
		_live.remove(event.getEntity());
	}
}
