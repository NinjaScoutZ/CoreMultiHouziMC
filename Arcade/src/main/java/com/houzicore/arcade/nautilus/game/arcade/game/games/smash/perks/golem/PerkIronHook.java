package com.houzicore.arcade.nautilus.game.arcade.game.games.smash.perks.golem;

import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.entity.Item;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.player.PlayerInteractEvent;

import com.houzicore.shared.common.util.C;
import com.houzicore.shared.common.util.F;
import com.houzicore.shared.common.util.UtilAction;
import com.houzicore.shared.common.util.UtilAlg;
import com.houzicore.shared.common.util.UtilBlock;
import com.houzicore.shared.common.util.UtilEvent;
import com.houzicore.shared.common.util.UtilEvent.ActionType;
import com.houzicore.shared.common.util.UtilItem;
import com.houzicore.shared.common.util.UtilParticle.ParticleType;
import com.houzicore.shared.common.util.UtilPlayer;
import com.houzicore.shared.core.itemstack.ItemStackFactory;
import com.houzicore.shared.core.projectile.IThrown;
import com.houzicore.shared.core.projectile.ProjectileUser;
import com.houzicore.shared.recharge.Recharge;
import com.houzicore.shared.updater.UpdateType;
import com.houzicore.arcade.nautilus.game.arcade.game.games.smash.perks.SmashPerk;

public class PerkIronHook extends SmashPerk implements IThrown
{
	
	private static final int COOLDOWN = 8000;
	private static final int DAMAGE = 4;
	
	public PerkIronHook()
	{
		super("Iron Hook", new String[] { C.cYellow + "Right-Click" + C.cGray + " with Pickaxe to " + C.cGreen + "Iron Hook" });
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

		if (!player.getInventory().getItemInMainHand().getType().name().endsWith("_PICKAXE"))
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

		// Action
		Item item = player.getWorld().dropItem(player.getEyeLocation().add(player.getLocation().getDirection()), ItemStackFactory.Instance.CreateStack(131));
		UtilAction.velocity(item, player.getLocation().getDirection(), 1.8, false, 0, 0.2, 10, false);

		Manager.GetProjectile().AddThrow(item, player, this, -1, true, true, true, Sound.ITEM_FLINTANDSTEEL_USE, 1.4f, 0.8f, ParticleType.CRIT, null, 0, UpdateType.TICK, 0.6f);

		// Inform
		UtilPlayer.message(player, F.main("Skill", "You used " + F.skill(GetName()) + "."));

		// Effect
		item.getWorld().playSound(item.getLocation(), Sound.ENTITY_IRON_GOLEM_ATTACK, 2f, 0.8f);
	}

	@Override
	public void Collide(LivingEntity target, Block block, ProjectileUser data)
	{
		// Remove
		double velocity = data.GetThrown().getVelocity().length();
		data.GetThrown().remove();

		if (!(data.GetThrower() instanceof Player))
		{
			return;
		}
		
		Player player = (Player) data.GetThrower();

		if (target == null)
		{
			return;
		}
		
		// Pull
		UtilAction.velocity(target, UtilAlg.getTrajectory(target.getLocation(), player.getLocation()), 2, false, 0, 0.8, 1.5, true);

		// Condition
		Manager.GetCondition().Factory().Falling(GetName(), target, player, 10, false, true);

		// Damage Event
		Manager.GetDamage().NewDamageEvent(target, player, null, DamageCause.CUSTOM, velocity * DAMAGE, false, true, false, player.getName(), GetName());

		// Inform
		UtilPlayer.message(target, F.main("Skill", F.name(player.getName()) + " hit you with " + F.skill(GetName()) + "."));
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