package com.houzicore.arcade.nautilus.game.arcade.game.games.smash.perks.squid;

import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.entity.Item;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.util.Vector;

import com.houzicore.shared.common.util.C;
import com.houzicore.shared.common.util.F;
import com.houzicore.shared.common.util.UtilAction;
import com.houzicore.shared.common.util.UtilBlock;
import com.houzicore.shared.common.util.UtilEnt;
import com.houzicore.shared.common.util.UtilEvent;
import com.houzicore.shared.common.util.UtilEvent.ActionType;
import com.houzicore.shared.common.util.UtilInv;
import com.houzicore.shared.common.util.UtilItem;
import com.houzicore.shared.common.util.UtilParticle;
import com.houzicore.shared.common.util.UtilParticle.ParticleType;
import com.houzicore.shared.common.util.UtilParticle.ViewDist;
import com.houzicore.shared.common.util.UtilPlayer;
import com.houzicore.shared.common.util.UtilServer;
import com.houzicore.shared.core.itemstack.ItemStackFactory;
import com.houzicore.shared.core.projectile.IThrown;
import com.houzicore.shared.core.projectile.ProjectileUser;
import com.houzicore.shared.recharge.Recharge;
import com.houzicore.shared.updater.UpdateType;
import com.houzicore.shared.core.damage.CustomDamageEvent;
import com.houzicore.arcade.nautilus.game.arcade.game.games.smash.perks.SmashPerk;

public class PerkInkBlast extends SmashPerk implements IThrown
{

	private int _cooldown;
	private float _spread;
	private float _projectileVelocity;
	private int _knockbackMagnitude;
	private int _bullets;
	private double _damagePerBullet;

	public PerkInkBlast()
	{
		super("Ink Shotgun", new String[] { C.cYellow + "Right-Click" + C.cGray + " with Axe to use " + C.cGreen + "Ink Shotgun" });
	}

	@Override
	public void setupValues()
	{
		_cooldown = getPerkTime("Cooldown");
		_spread = getPerkFloat("Spread");
		_projectileVelocity = getPerkFloat("Projectile Velocity");
		_knockbackMagnitude = getPerkInt("Knockback Magnitude");
		_bullets = getPerkInt("Bullets");
		_damagePerBullet = getPerkDouble("Damage Per Bullet");
	}

	@EventHandler
	public void shoot(PlayerInteractEvent event)
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

		if (isSuperActive(player))
		{
			return;
		}

		if (!Recharge.Instance.use(player, GetName(), _cooldown, true, true))
		{
			return;
		}

		event.setCancelled(true);

		UtilInv.Update(player);

		for (int i = 0; i < _bullets; i++)
		{
			Item ent = player.getWorld().dropItem(player.getEyeLocation().add(player.getLocation().getDirection()), ItemStackFactory.Instance.CreateStack(Material.INK_SAC, (byte) 0, 1, "Ink" + Math
					.random()));

			Vector random = new Vector((Math.random() - 0.5) * _spread, (Math.random() - 0.5) * _spread, (Math.random() - 0.5) * _spread);
			random.normalize();
			random.multiply(_projectileVelocity);

			if (i == 0)
			{
				random.multiply(0);
			}

			UtilAction.velocity(ent, player.getLocation().getDirection().add(random), 1 + 0.4 * Math.random(), false, 0, 0.2, 10, false);

			Manager.GetProjectile().AddThrow(ent, player, this, -1, true, true, true, null, 0f, 0f, ParticleType.EXPLODE, UpdateType.FASTEST, 0.5f);
		}

		// Inform
		UtilPlayer.message(player, F.main("Game", "You used " + F.skill(GetName()) + "."));

		// Effect
		player.getWorld().playSound(player.getLocation(), Sound.ENTITY_GENERIC_EXPLODE, 1.5f, 0.75f);
		player.getWorld().playSound(player.getLocation(), Sound.ENTITY_GENERIC_SPLASH, 0.75f, 1f);
	}

	@Override
	public void Collide(LivingEntity target, Block block, ProjectileUser data)
	{
		Explode(data);

		if (target == null)
		{
			return;
		}

		// Damage Event
		Manager.GetDamage().NewDamageEvent(target, data.GetThrower(), null, DamageCause.PROJECTILE, _damagePerBullet, true, true, false, UtilEnt.getName(data.GetThrower()), GetName());

		UtilParticle.PlayParticle(ParticleType.EXPLODE, target.getLocation().add(0, 1, 0), 1f, 1f, 1f, 0, 12, ViewDist.LONG, UtilServer.getPlayers());
	}

	@Override
	public void Idle(ProjectileUser data)
	{
		Explode(data);
	}

	@Override
	public void Expire(ProjectileUser data)
	{
		Explode(data);
	}

	public void Explode(ProjectileUser data)
	{
		data.GetThrown().getWorld().playSound(data.GetThrown().getLocation(), Sound.ENTITY_GENERIC_EXPLODE, 0.75f, 1.25f);
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