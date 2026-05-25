package com.houzicore.arcade.nautilus.game.arcade.game.games.smash.perks.skeletalhorse;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;

import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.entity.Item;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

import com.houzicore.shared.common.util.C;
import com.houzicore.shared.common.util.F;
import com.houzicore.shared.common.util.UtilAction;
import com.houzicore.shared.common.util.UtilBlock;
import com.houzicore.shared.common.util.UtilEnt;
import com.houzicore.shared.common.util.UtilEvent;
import com.houzicore.shared.common.util.UtilEvent.ActionType;
import com.houzicore.shared.common.util.UtilItem;
import com.houzicore.shared.common.util.UtilPlayer;
import com.houzicore.shared.common.util.UtilTime;
import com.houzicore.shared.core.projectile.IThrown;
import com.houzicore.shared.core.projectile.ProjectileUser;
import com.houzicore.shared.recharge.Recharge;
import com.houzicore.shared.updater.UpdateType;
import com.houzicore.shared.updater.event.UpdateEvent;
import com.houzicore.shared.core.damage.CustomDamageEvent;
import com.houzicore.arcade.nautilus.game.arcade.game.games.smash.perks.SmashPerk;

public class PerkBoneRush extends SmashPerk implements IThrown
{

	private int _cooldown;
	private float _damageNormal;
	private int _damageSmash;
	private int _knockbackNormal;
	private int _knockbackSmash;
	private int _expireTime;
	private float _yLimit;

	private Map<UUID, Long> _active = new HashMap<>();

	public PerkBoneRush()
	{
		super("Bone Rush", new String[] { C.cYellow + "Right-Click" + C.cGray + " with Spade to use " + C.cGreen + "Bone Rush", C.cGray + "Crouch to avoid movement with " + C.cGreen + "Bone Rush" });
	}

	@Override
	public void setupValues()
	{
		_cooldown = getPerkTime("Cooldown");
		_damageNormal = getPerkFloat("Damage Normal");
		_damageSmash = getPerkInt("Damage Smash");
		_knockbackNormal = getPerkInt("Knockback Normal");
		_knockbackSmash = getPerkInt("Knockback Smash");
		_expireTime = getPerkTime("Expire Time");
		_yLimit = getPerkFloat("Y Limit");
	}

	@EventHandler
	public void Skill(PlayerInteractEvent event)
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

		if (isSuperActive(player))
		{
			return;
		}

		if (!Recharge.Instance.use(player, GetName(), _cooldown, true, true))
		{
			return;
		}

		activate(player);

		UtilPlayer.message(player, F.main("Skill", "You used " + F.skill(GetName()) + "."));
	}

	public void activate(Player player)
	{
		_active.put(player.getUniqueId(), System.currentTimeMillis());
	}
	
	public void deactivate(Player player)
	{
		_active.remove(player.getUniqueId());
	}

	@EventHandler
	public void Update(UpdateEvent event)
	{
		if (event.getType() != UpdateType.TICK)
		{
			return;
		}

		Iterator<UUID> playerIterator = _active.keySet().iterator();

		while (playerIterator.hasNext())
		{
			UUID key = playerIterator.next();
			Player player = UtilPlayer.searchExact(key);

			if (player == null)
			{
				playerIterator.remove();
				continue;
			}

			if (!player.isValid() || (UtilTime.elapsed(_active.get(key), 1500) && !isSuperActive(player)))
			{
				playerIterator.remove();
				continue;
			}

			// Sound
			player.getWorld().playSound(player.getLocation(), Sound.ENTITY_SKELETON_HURT, 0.4f, (float) (Math.random() + 1));

			// Velocity
			Vector dir = player.getLocation().getDirection();
			double limit = isSuperActive(player) ? _yLimit + 0.1 : _yLimit;
			// Player
			if (!player.isSneaking())
			{
				UtilAction.velocity(player, dir, 0.6, false, 0, 0, limit, false);
			}

			// Bones
			for (int i = 0; i < 6; i++)
			{
				Item bone = player.getWorld().dropItem(player.getLocation().add(Math.random() * 5 - 2.5, Math.random() * 3, Math.random() * 5 - 2.5), new ItemStack(Material.BONE));
				UtilAction.velocity(bone, dir, 0.6 + 0.3 * Math.random(), false, 0, 0, 0.3, false);
				Manager.GetProjectile().AddThrow(bone, player, this, _expireTime, true, true, true, true, 0.5f);
			}
		}
	}

	@EventHandler
	public void Knockback(CustomDamageEvent event)
	{
		if (event.GetReason() == null)
		{
			return;
		}

		if (event.GetReason().contains(GetName()))
		{
			event.AddKnockback(GetName(), _knockbackNormal);
		}

		if (event.GetReason().contains("Bone Storm"))
		{
			event.AddKnockback(GetName(), _knockbackSmash);
		}
	}

	@Override
	public void Collide(LivingEntity target, Block block, ProjectileUser data)
	{
		data.GetThrown().remove();

		if (target == null)
		{
			return;
		}

		if (!(target instanceof Player) || !(data.GetThrower() instanceof Player))
		{
			return;
		}
		
		Player damager = (Player) data.GetThrower();
		String reason = GetName();

		if (damager instanceof Player)
		{
			if (isTeamDamage((Player) target, damager))
			{
				return;
			}
		}

		if (isSuperActive(damager))
		{
			reason = "Bone Storm";
		}

		// Damage Event
		Manager.GetDamage().NewDamageEvent(target, data.GetThrower(), null, DamageCause.CUSTOM, isSuperActive(damager) ? _damageSmash : _damageNormal, false, true, false, UtilEnt.getName(data.GetThrower()), reason);

		target.setVelocity(data.GetThrown().getVelocity());
	}

	@Override
	public void Idle(ProjectileUser data)
	{
		data.GetThrown().remove();
	}

	@Override
	public void Expire(ProjectileUser data)
	{
		data.GetThrown().remove();
	}
	@EventHandler
	public void Clean(PlayerDeathEvent event)
	{
		_active.remove(event.getEntity().getUniqueId());
	}
}