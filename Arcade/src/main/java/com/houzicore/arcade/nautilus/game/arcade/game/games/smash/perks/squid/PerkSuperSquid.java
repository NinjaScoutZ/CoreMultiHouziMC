package com.houzicore.arcade.nautilus.game.arcade.game.games.smash.perks.squid;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.bukkit.Sound;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.player.PlayerInteractEvent;

import com.houzicore.shared.common.util.C;
import com.houzicore.shared.common.util.F;
import com.houzicore.shared.common.util.UtilAction;
import com.houzicore.shared.common.util.UtilBlock;
import com.houzicore.shared.common.util.UtilEvent;
import com.houzicore.shared.common.util.UtilEvent.ActionType;
import com.houzicore.shared.common.util.UtilItem;
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
import com.houzicore.arcade.nautilus.game.arcade.game.games.smash.perks.SmashPerk;

public class PerkSuperSquid extends SmashPerk
{
	
	private int _cooldown;
	private int _velocityTime;
	
	private Map<UUID, Long> _active = new HashMap<>();

	public PerkSuperSquid()
	{
		super("Super Squid", new String[] { C.cYellow + "Hold Block" + C.cGray + " to use " + C.cGreen + "Super Squid", });
	}

	@Override
	public void setupValues()
	{
		_cooldown = getPerkTime("Cooldown");
		_velocityTime = getPerkInt("Velocity Time (ms)");
	}

	@EventHandler
	public void Activate(PlayerInteractEvent event)
	{
		if (event.isCancelled())
		{
			return;
		}

		if (!UtilEvent.isAction(event, ActionType.R))
		{
			return;
		}

		if (UtilBlock.usable(event.getClickedBlock()))
		{
			return;
		}

		Player player = event.getPlayer();

		if (!player.getInventory().getItemInMainHand().getType().name().endsWith("_SWORD"))
		{
			return;
		}

		if (!hasPerk(player))
		{
			return;
		}
		
		if (isSuperActive(player))
		{
			return;
		}

		if (!Recharge.Instance.use(player, GetName(), _cooldown, true, true))
		{
			return;
		}
		
		_active.put(player.getUniqueId(), System.currentTimeMillis());

		UtilPlayer.message(player, F.main("Skill", "You used " + F.skill(GetName()) + "."));
	}

	@EventHandler
	public void Update(UpdateEvent event)
	{
		if (event.getType() != UpdateType.TICK)
		{
			return;
		}
		
		for (Player cur : UtilServer.getPlayers())
		{
			if (!_active.containsKey(cur.getUniqueId()))
			{
				continue;
			}
			
			if (isSuperActive(cur))
			{
				continue;
			}
			
			if (!cur.isBlocking())
			{
				_active.remove(cur.getUniqueId());
				continue;
			}

			if (UtilTime.elapsed(_active.get(cur.getUniqueId()), _velocityTime))
			{
				_active.remove(cur.getUniqueId());
				continue;
			}

			UtilAction.velocity(cur, 0.6, 0.1, 1, true);

			cur.getWorld().playSound(cur.getLocation(), Sound.ENTITY_PLAYER_SPLASH, 0.5f, 1f);
			UtilParticle.PlayParticle(ParticleType.SPLASH, cur.getLocation().add(0, 0.5, 0), 0.3f, 0.3f, 0.3f, 0, 60, ViewDist.LONG, UtilServer.getPlayers());
		}
	}

	@EventHandler
	public void DamageCancel(CustomDamageEvent event)
	{
		LivingEntity damagee = event.GetDamageeEntity();
		LivingEntity damager = event.GetDamagerEntity(false);

		if (_active.containsKey(damagee.getUniqueId()) || damager != null && event.GetCause() == DamageCause.ENTITY_ATTACK && _active.containsKey(damager.getUniqueId()))
		{
			event.SetCancelled(GetName());
		}
	}
}
