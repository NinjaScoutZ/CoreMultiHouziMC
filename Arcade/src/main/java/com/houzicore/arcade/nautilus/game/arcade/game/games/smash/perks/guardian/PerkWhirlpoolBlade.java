package com.houzicore.arcade.nautilus.game.arcade.game.games.smash.perks.guardian;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.entity.Item;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

import com.houzicore.shared.common.util.C;
import com.houzicore.shared.common.util.UtilAction;
import com.houzicore.shared.common.util.UtilAlg;
import com.houzicore.shared.common.util.UtilBlock;
import com.houzicore.shared.common.util.UtilEvent;
import com.houzicore.shared.common.util.UtilEvent.ActionType;
import com.houzicore.shared.common.util.UtilParticle.ParticleType;
import com.houzicore.shared.common.util.UtilParticle.ViewDist;
import com.houzicore.shared.common.util.UtilItem;
import com.houzicore.shared.common.util.UtilParticle;
import com.houzicore.shared.core.projectile.IThrown;
import com.houzicore.shared.core.projectile.ProjectileUser;
import com.houzicore.shared.recharge.Recharge;
import com.houzicore.shared.updater.UpdateType;
import com.houzicore.shared.updater.event.UpdateEvent;
import com.houzicore.shared.core.damage.CustomDamageEvent;

import com.houzicore.arcade.nautilus.game.arcade.game.games.smash.perks.SmashPerk;

public class PerkWhirlpoolBlade extends SmashPerk implements IThrown
{

	private int _cooldown;
	private int _expireTime;
	private float _velocity;
	private float _hitBox;
	private int _damage;
	
	private Set<Item> _items = new HashSet<>();
	
	public PerkWhirlpoolBlade()
	{
		super("Whirlpool Axe", new String[] { C.cYellow + "Right-Click" + C.cGray + " with Axe to use " + C.cGreen + "Whirlpool Axe" });
	}

	@Override
	public void setupValues()
	{
		_cooldown = getPerkTime("Cooldown");
		_expireTime = getPerkTime("Expire Time");
		_velocity = getPerkFloat("Velocity");
		_hitBox = getPerkFloat("Hit Box");
		_damage = getPerkInt("Damage");
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
		
		player.playSound(player.getLocation(), Sound.BLOCK_SNOW_BREAK, 1, 1);
		
		Item item = player.getWorld().dropItem(player.getEyeLocation(), new ItemStack(Material.PRISMARINE_SHARD));
		
		item.setVelocity(player.getLocation().getDirection().multiply(_velocity));
		Manager.GetProjectile().AddThrow(item, player, this, _expireTime, true, true, true, false, _hitBox);
		_items.add(item);
	}
	
	@EventHandler
	public void update(UpdateEvent event)
	{
		if (event.getType() != UpdateType.TICK)
		{
			return;
		}
		
		Iterator<Item> iterator = _items.iterator();
		
		while (iterator.hasNext())
		{
			Item item = iterator.next();
			
			if (!item.isValid())
			{
				iterator.remove();
				continue;
			}
			
			UtilParticle.PlayParticleToAll(ParticleType.DRIP_WATER, item.getLocation(), 0, 0, 0, 0.01F, 1, ViewDist.LONG);
		}
	}

	@Override
	public void Collide(LivingEntity target, Block block, ProjectileUser data)
	{
		if (!UtilBlock.airFoliage(block))
		{
			data.GetThrown().remove();
		}
		
		if (target == null)
		{
			return;
		}
		
		Manager.GetDamage().NewDamageEvent(target, data.GetThrower(), null, DamageCause.CUSTOM, _damage, false, true, true, data.GetThrower().getName(), GetName());
		target.setVelocity(UtilAlg.getTrajectory(target, data.GetThrower()).setY(0.5));
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
}