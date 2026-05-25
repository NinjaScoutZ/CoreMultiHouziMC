package com.houzicore.arcade.nautilus.game.arcade.game.games.smash.perks.zombie;

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

public class PerkZombieBile extends SmashPerk implements IThrown
{

	private int _cooldown;
	private int _items;
	private int _damage;
	private int _knockbackMagnitude;
	
	private Map<UUID, Long> _active = new HashMap<>();

	public PerkZombieBile()
	{
		super("Spew Bile", new String[] { C.cYellow + "Hold Block" + C.cGray + " to use " + C.cGreen + "Spew Bile" });
	}

	@Override
	public void setupValues()
	{
		_cooldown = getPerkTime("Cooldown");
		_items = getPerkInt("Items");
		_damage = getPerkInt("Damage");
		_knockbackMagnitude = getPerkInt("Knockback Magnitude");
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

		if (!player.getInventory().getItemInMainHand().getType().name().endsWith("_AXE"))
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
	public void deactivateDeath(PlayerDeathEvent event)
	{
		if (!hasPerk(event.getEntity()))
		{
			return;
		}

		_active.remove(event.getEntity().getUniqueId());
	}

	@EventHandler
	public void update(UpdateEvent event)
	{
		if (event.getType() != UpdateType.TICK)
		{
			return;
		}

		Iterator<UUID> activeIter = _active.keySet().iterator();

		while (activeIter.hasNext())
		{
			UUID uuid = activeIter.next();
			Player player = UtilPlayer.searchExact(uuid);

			// Expire
			if (UtilTime.elapsed(_active.get(player.getUniqueId()), 2000))
			{
				activeIter.remove();
				continue;
			}

			// Sound
			if (Math.random() > 0.85)
			{
				player.getWorld().playSound(player.getLocation(), Sound.ENTITY_PLAYER_BURP, 1f, (float) (Math.random() + 0.5));
			}

			// Projectiles
			for (int i = 0; i < _items; i++)
			{
				Vector rand = new Vector((Math.random() - 0.5) * 0.525, (Math.random() - 0.5) * 0.525, (Math.random() - 0.5) * 0.525);

				Item ent = player.getWorld().dropItem(player.getEyeLocation().add(player.getLocation().getDirection()).subtract(0, 0.5, 0), new ItemStack(Material.ROTTEN_FLESH));
				UtilAction.velocity(ent, player.getLocation().getDirection().add(rand), 0.8, false, 0, 0.2, 10, false);
				Manager.GetProjectile().AddThrow(ent, player, this, 2000, true, true, true, false, 0.5f);
			}
		}
	}

	@EventHandler
	public void death(PlayerDeathEvent event)
	{
		_active.remove(event.getEntity().getUniqueId());
	}

	@Override
	public void Collide(LivingEntity target, Block block, ProjectileUser data)
	{
		data.GetThrown().remove();

		if (target == null)
		{
			return;
		}
		
		if (UtilPlayer.isSpectator(target))
		{
			return;
		}
			
		// Damage Event
		Manager.GetDamage().NewDamageEvent(target, data.GetThrower(), null, DamageCause.CUSTOM, _damage, true, false, false, UtilEnt.getName(data.GetThrower()), GetName());

		data.GetThrown().remove();
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
	
	public void ChunkUnload(ProjectileUser data)
	{
		data.GetThrown().remove();
	}

	@EventHandler
	public void Knockback(CustomDamageEvent event)
	{
		if (event.GetReason() == null || !event.GetReason().contains(GetName()))
		{
			return;
		}
		
		event.AddKnockback(GetName(), _knockbackMagnitude);
	}
}