package com.houzicore.arcade.nautilus.game.arcade.game.games.smash.perks.cow;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.player.PlayerInteractEvent;

import com.houzicore.shared.common.util.C;
import com.houzicore.shared.common.util.F;
import com.houzicore.shared.common.util.UtilBlock;
import com.houzicore.shared.common.util.UtilEvent;
import com.houzicore.shared.common.util.UtilEvent.ActionType;
import com.houzicore.shared.common.util.UtilItem;
import com.houzicore.shared.common.util.UtilMath;
import com.houzicore.shared.common.util.UtilParticle;
import com.houzicore.shared.common.util.UtilParticle.ParticleType;
import com.houzicore.shared.common.util.UtilParticle.ViewDist;
import com.houzicore.shared.common.util.UtilPlayer;
import com.houzicore.shared.recharge.Recharge;
import com.houzicore.shared.updater.UpdateType;
import com.houzicore.shared.updater.event.UpdateEvent;

import com.houzicore.arcade.nautilus.game.arcade.game.games.smash.perks.SmashPerk;

public class PerkCowMilkSpiral extends SmashPerk
{

	private int _cooldownNormal;
	private int _cooldownSmash;
	private int _hitBoxRadius;
	private int _damage;
	private int _hitFrequency;

	private final Set<DataCowMilkSpiral> _active = new HashSet<>();

	public PerkCowMilkSpiral()
	{
		super("Milk Spiral", new String[] { C.cYellow + "Right Click" + C.cGray + " with Spade to " + C.cGreen + "Milk Spiral", C.cGray + "Crouch to cancel movement for Milk Spiral" });
	}

	@Override
	public void setupValues()
	{
		_cooldownNormal = getPerkTime("Cooldown Normal");
		_cooldownSmash = getPerkTime("Cooldown Smash");
		_hitBoxRadius = getPerkInt("Hit Box Radius");
		_damage = getPerkInt("Damage");
		_hitFrequency = getPerkInt("Hit Frequency (ms)");
	}

	@EventHandler
	public void activate(PlayerInteractEvent event)
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

		if (!player.getInventory().getItemInMainHand().getType().name().endsWith("_SHOVEL"))
		{
			return;
		}

		if (!hasPerk(player))
		{
			return;
		}

		if (!Recharge.Instance.use(player, GetName(), isSuperActive(player) ? _cooldownSmash : _cooldownNormal, true, true))
		{
			return;
		}

		_active.add(new DataCowMilkSpiral(player, isSuperActive(player)));

		// Inform
		UtilPlayer.message(player, F.main("Skill", "You used " + F.skill(GetName()) + "."));
	}

	@EventHandler
	public void update(UpdateEvent event)
	{
		if (event.getType() != UpdateType.TICK)
		{
			return;
		}

		Iterator<DataCowMilkSpiral> activeIter = _active.iterator();

		while (activeIter.hasNext())
		{
			DataCowMilkSpiral data = activeIter.next();

			if (data.update())
			{
				data.HitPlayers.clear();
				activeIter.remove();
			}

			for (Player player : Manager.GetGame().GetPlayers(true))
			{
				if (player.equals(data.Player) || !Recharge.Instance.use(player, GetName() + " Rate", _hitFrequency, false, false))
				{
					continue;
				}

				int timesHit = data.HitPlayers.getOrDefault(player, 0);

				if (UtilMath.offset(player.getLocation().add(0, 1.5, 0), data.Spiral) < _hitBoxRadius && timesHit < 2)
				{
					data.HitPlayers.put(player, ++timesHit);
					Manager.GetDamage().NewDamageEvent(player, data.Player, null, DamageCause.CUSTOM, _damage, true, true, false, player.getName(), GetName());

					ParticleType pType = isSuperActive(data.Player) ? ParticleType.RED_DUST : ParticleType.FIREWORKS_SPARK;
					UtilParticle.PlayParticleToAll(pType, player.getLocation().add(0, 1, 0), 0.2f, 0.2f, 0.2f, 0.3f, 30, ViewDist.LONG);
					player.getWorld().playSound(player.getLocation(), Sound.ENTITY_GENERIC_SPLASH, 0.25f, 2f);
				}
			}
		}
	}
}
