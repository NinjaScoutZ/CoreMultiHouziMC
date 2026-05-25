package com.houzicore.arcade.nautilus.game.arcade.game.games.smash.perks.pig;

import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerPickupItemEvent;
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
import com.houzicore.shared.common.util.UtilMath;
import com.houzicore.shared.common.util.UtilParticle;
import com.houzicore.shared.common.util.UtilParticle.ParticleType;
import com.houzicore.shared.common.util.UtilParticle.ViewDist;
import com.houzicore.shared.common.util.UtilPlayer;
import com.houzicore.shared.common.util.UtilServer;
import com.houzicore.shared.core.disguise.disguises.DisguiseBase;
import com.houzicore.shared.core.disguise.disguises.DisguisePigZombie;
import com.houzicore.shared.core.itemstack.ItemStackFactory;
import com.houzicore.shared.core.projectile.IThrown;
import com.houzicore.shared.core.projectile.ProjectileUser;
import com.houzicore.shared.recharge.Recharge;
import com.houzicore.arcade.nautilus.game.arcade.game.games.smash.perks.SmashPerk;

public class PerkPigBaconBounce extends SmashPerk implements IThrown
{
	
	private float _energyBacon;
	private float _energyBaconDisgtuiseFactor;
	private float _energyBaconBack;
	private float _hitBox;
	private int _cooldown;
	private int _healthBacon;
	private int _damageBacon;

	public PerkPigBaconBounce()
	{
		super("Bouncy Bacon", new String[] { C.cYellow + "Right-Click" + C.cGray + " with Axe to " + C.cGreen + "Bouncy Bacon", });
	}

	@Override
	public void setupValues()
	{
		_energyBacon = getPerkFloat("Energy Per Bacon");
		_energyBaconDisgtuiseFactor = getPerkFloat("Energy Per Bacon Disguise Factor");
		_energyBaconBack = getPerkFloat("Energy Per Bacon Back");
		_hitBox = getPerkFloat("Hit Box");
		_cooldown = getPerkInt("Cooldown (ms)");
		_healthBacon = getPerkInt("Health Per Bacon");
		_damageBacon = getPerkInt("Bacon Damage");
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

		if (!player.getInventory().getItemInMainHand().getType().name().endsWith("_AXE"))
		{
			return;
		}

		if (!hasPerk(player))
		{
			return;
		}

		float energy = _energyBacon;

		DisguiseBase disguise = Manager.GetDisguise().getDisguise(player);

		if (disguise != null && disguise instanceof DisguisePigZombie)
		{
			energy = energy * _energyBaconDisgtuiseFactor;
		}

		// Energy
		if (player.getExp() < energy)
		{
			UtilPlayer.message(player, F.main("Energy", "Not enough Energy to use " + F.skill(GetName()) + "."));
			return;
		}

		// Recharge
		if (!Recharge.Instance.use(player, GetName(), _cooldown, false, false))
		{
			return;
		}
		
		// Use Energy
		player.setExp(Math.max(0f, player.getExp() - energy));

		// Launch
		Item ent = player.getWorld().dropItem(player.getEyeLocation(), ItemStackFactory.Instance.CreateStack(Material.PORKCHOP, (byte) 0, 1, "Bacon" + System.currentTimeMillis()));
		UtilAction.velocity(ent, player.getLocation().getDirection(), 1.2, false, 0, 0.2, 10, false);
		Manager.GetProjectile().AddThrow(ent, player, this, 5000, true, true, true, false, _hitBox);
		ent.setPickupDelay(9999);

		// Sound
		player.getWorld().playSound(player.getLocation(), Sound.ENTITY_PIG_AMBIENT, 2f, 1.5f);

		// Inform
		UtilPlayer.message(player, F.main("Skill", "You used " + F.skill(GetName()) + "."));
	}

	@Override
	public void Collide(LivingEntity target, Block block, ProjectileUser data)
	{
		Rebound(data.GetThrower(), data.GetThrown());

		if (target == null)
		{
			return;
		}

		if (target instanceof Player && isTeamDamage((Player) target, (Player) data.GetThrower()))
		{
			return;
		}

		// Damage Event
		Manager.GetDamage().NewDamageEvent(target, data.GetThrower(), null, DamageCause.CUSTOM, _damageBacon, true, true, false, UtilEnt.getName(data.GetThrower()), GetName());

		Item item = (Item) data.GetThrown();
		item.setItemStack(new ItemStack(Material.COOKED_PORKCHOP));
	}

	@Override
	public void Idle(ProjectileUser data)
	{
		Rebound(data.GetThrower(), data.GetThrown());
	}

	@Override
	public void Expire(ProjectileUser data)
	{
		Rebound(data.GetThrower(), data.GetThrown());
	}
	
	public void Rebound(LivingEntity player, Entity ent)
	{
		ent.getWorld().playSound(ent.getLocation(), Sound.ENTITY_ITEM_PICKUP, 1f, 0.5f);

		double mult = 0.5 + (0.035 * UtilMath.offset(player.getLocation(), ent.getLocation()));

		// Velocity
		ent.setVelocity(player.getLocation().toVector().subtract(ent.getLocation().toVector()).normalize().add(new Vector(0, 0.4, 0)).multiply(mult));

		// Ticks
		if (ent instanceof Item)
		{
			((Item) ent).setPickupDelay(5);
		}
	}

	@EventHandler(priority = EventPriority.LOWEST)
	public void Pickup(PlayerPickupItemEvent event)
	{
		if (!hasPerk(event.getPlayer()))
		{
			return;
		}
		
		Material type = event.getItem().getItemStack().getType();
		
		if (type != Material.PORKCHOP && type != Material.COOKED_PORKCHOP)
			return;

		// Remove
		event.getItem().remove();

		// Restore Energy
		event.getPlayer().setExp(Math.min(0.999f, event.getPlayer().getExp() + _energyBaconBack));

		// Sound
		event.getPlayer().getWorld().playSound(event.getPlayer().getLocation(), Sound.ENTITY_GENERIC_EAT, 2f, 1f);

		// Heal
		if (event.getItem().getItemStack().getType() == Material.COOKED_PORKCHOP)
		{
			UtilPlayer.health(event.getPlayer(), _healthBacon);
			UtilParticle.PlayParticle(ParticleType.HEART, event.getPlayer().getLocation().add(0, 0.5, 0), 0.2f, 0.2f, 0.2f, 0, 4, ViewDist.LONG, UtilServer.getPlayers());
		}
	}
}