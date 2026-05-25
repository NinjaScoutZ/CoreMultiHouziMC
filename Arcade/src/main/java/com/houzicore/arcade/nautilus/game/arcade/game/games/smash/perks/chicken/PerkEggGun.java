package com.houzicore.arcade.nautilus.game.arcade.game.games.smash.perks.chicken;

import com.houzicore.shared.common.util.C;
import com.houzicore.shared.common.util.F;
import com.houzicore.shared.common.util.UtilAction;
import com.houzicore.shared.common.util.UtilBlock;
import com.houzicore.shared.common.util.UtilEvent;
import com.houzicore.shared.common.util.UtilEvent.ActionType;
import com.houzicore.shared.common.util.UtilItem;
import com.houzicore.shared.common.util.UtilPlayer;
import com.houzicore.shared.common.util.UtilServer;
import com.houzicore.shared.common.util.UtilTime;
import com.houzicore.shared.recharge.Recharge;
import com.houzicore.shared.updater.UpdateType;
import com.houzicore.shared.updater.event.UpdateEvent;
import com.houzicore.shared.core.damage.CustomDamageEvent;
import com.houzicore.arcade.nautilus.game.arcade.game.games.smash.perks.SmashPerk;
import org.bukkit.Sound;
import org.bukkit.entity.Egg;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.util.Vector;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class PerkEggGun extends SmashPerk
{

	private int _cooldown;
	private int _duration;
	private int _damage;

	private Map<UUID, Long> _active = new HashMap<>();

	public PerkEggGun()
	{
		super("Egg Blaster", new String[] { C.cYellow + "Hold Block" + C.cGray + " to use " + C.cGreen + "Egg Blaster" });
	}

	@Override
	public void setupValues()
	{
		_cooldown = getPerkInt("Cooldown (ms)");
		_duration = getPerkInt("Duration (ms)");
		_damage = getPerkInt("Damage");
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
		
		if (isSuperActive(event.getPlayer()))
		{
			return;
		}

		if (!hasPerk(player))
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
		if (event.getType() != UpdateType.TICK || !Manager.GetGame().IsLive())
		{
			return;
		}

		for (Player cur : UtilServer.getPlayers())
		{
			UUID key = cur.getUniqueId();
			
			if (!isSuperActive(cur))
			{
				if (!_active.containsKey(key))
				{
					continue;
				}

				if (!cur.isBlocking())
				{
					_active.remove(key);
					continue;
				}

				if (UtilTime.elapsed(_active.get(key), _duration))
				{
					_active.remove(key);
					continue;
				}
			}

			Vector offset = cur.getLocation().getDirection();
			
			if (offset.getY() < 0)
			{
				offset.setY(0);
			}
			
			Egg egg = cur.getWorld().spawn(cur.getLocation().add(0, 0.5, 0).add(offset), Egg.class);
			egg.setVelocity(cur.getLocation().getDirection().add(new Vector(0, 0.2, 0)));
			egg.setShooter(cur);

			// Effect
			cur.getWorld().playSound(cur.getLocation(), Sound.ENTITY_CHICKEN_EGG, 0.5f, 1f);
		}
	}

	@EventHandler(priority = EventPriority.LOWEST)
	public void EggHit(CustomDamageEvent event)
	{
		if (event.GetProjectile() == null || !(event.GetProjectile() instanceof Egg))
		{
			return;
		}

		Player damagee = event.GetDamageePlayer();
		Player damager = event.GetDamagerPlayer(true);

		if (damager == null || !hasPerk(damager))
		{
			return;
		}

		event.AddMod(damager.getName(), "Negate", -event.GetDamage(), false);
		event.AddMod(damager.getName(), "Egg Blaster", _damage, true);
		event.SetIgnoreRate(true);
		event.SetKnockback(false);

		if (damagee == null || !isTeamDamage(damagee, damager))
		{
			event.GetDamageeEntity().setVelocity(new Vector(0, 0, 0));
		}
	}
}
