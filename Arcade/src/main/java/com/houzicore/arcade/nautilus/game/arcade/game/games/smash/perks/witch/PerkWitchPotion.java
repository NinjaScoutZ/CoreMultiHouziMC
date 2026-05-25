package com.houzicore.arcade.nautilus.game.arcade.game.games.smash.perks.witch;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.bukkit.block.Block;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.entity.ThrownPotion;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;

import com.houzicore.shared.common.util.C;
import com.houzicore.shared.common.util.F;
import com.houzicore.shared.common.util.UtilAction;
import com.houzicore.shared.common.util.UtilBlock;
import com.houzicore.shared.common.util.UtilEnt;
import com.houzicore.shared.common.util.UtilEvent;
import com.houzicore.shared.common.util.UtilEvent.ActionType;
import com.houzicore.shared.common.util.UtilItem;
import com.houzicore.shared.common.util.UtilParticle;
import com.houzicore.shared.common.util.UtilParticle.ParticleType;
import com.houzicore.shared.common.util.UtilParticle.ViewDist;
import com.houzicore.shared.common.util.UtilPlayer;
import com.houzicore.shared.common.util.UtilServer;
import com.houzicore.shared.core.projectile.IThrown;
import com.houzicore.shared.core.projectile.ProjectileUser;
import com.houzicore.shared.recharge.Recharge;
import com.houzicore.shared.updater.UpdateType;
import com.houzicore.shared.updater.event.UpdateEvent;
import com.houzicore.shared.core.damage.CustomDamageEvent;

import com.houzicore.arcade.nautilus.game.arcade.game.games.smash.TeamSuperSmash;
import com.houzicore.arcade.nautilus.game.arcade.game.games.smash.perks.SmashPerk;

public class PerkWitchPotion extends SmashPerk implements IThrown
{

	private int _cooldown;
	private int _range;
	private int _damageDirect;
	private int _damageDistance;
	private int _knockbackMagnitude;

	private List<Projectile> _proj = new ArrayList<>();

	public PerkWitchPotion()
	{
		super("Daze Potion", new String[] { C.cYellow + "Right-Click" + C.cGray + " with Axe to use " + C.cGreen + "Daze Potion" });
	}

	@Override
	public void setupValues()
	{
		_cooldown = getPerkTime("Cooldown");
		_range = getPerkInt("Range");
		_damageDirect = getPerkInt("Damage Direct");
		_damageDistance = getPerkInt("Damage Distance");
		_knockbackMagnitude = getPerkInt("Knockback Magnitude");
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

		// Start
		ThrownPotion potion = player.launchProjectile(ThrownPotion.class);
		UtilAction.velocity(potion, player.getLocation().getDirection(), 1, false, 0, 0.2, 10, false);

		_proj.add(potion);

		Manager.GetProjectile().AddThrow(potion, player, this, 10000, true, true, true, false, 0.5f);

		// Inform
		UtilPlayer.message(player, F.main("Skill", "You used " + F.skill(GetName()) + "."));
	}

	@EventHandler
	public void Update(UpdateEvent event)
	{
		if (event.getType() != UpdateType.TICK)
		{
			return;
		}

		Iterator<Projectile> potionIterator = _proj.iterator();

		while (potionIterator.hasNext())
		{
			Projectile proj = potionIterator.next();

			if (!proj.isValid())
			{
				potionIterator.remove();
				continue;
			}

			UtilParticle.PlayParticle(ParticleType.MOB_SPELL, proj.getLocation(), 0, 0, 0, 0, 1, ViewDist.LONGER, UtilServer.getPlayers());
		}
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

	@Override
	public void Collide(LivingEntity target, Block block, ProjectileUser data)
	{
		Player thrower = (Player) data.GetThrower();

		List<Player> players = new ArrayList<>(Manager.GetGame().GetPlayers(true));
		players.removeAll(TeamSuperSmash.getTeam(Manager, thrower, true));

		List<Player> directHit = new ArrayList<>();
		for (Player player : players)
		{
			if (player.getBoundingBox().overlaps(data.GetThrown().getBoundingBox()))
			{
				directHit.add(player);
			}
		}

		for (Player player : directHit)
		{
			Manager.GetDamage().NewDamageEvent(player, thrower, null, DamageCause.CUSTOM, _damageDirect, true, true, false, thrower.getName(), GetName());
			Manager.GetCondition().Factory().Slow(GetName(), player, thrower, 2, 1, true, true, false, false);
		}

		players.removeAll(directHit);

		Vector a = data.GetThrown().getLocation().subtract(_range, _range, _range).toVector();
		Vector b = data.GetThrown().getLocation().add(_range, _range, _range).toVector();
		org.bukkit.util.BoundingBox box = org.bukkit.util.BoundingBox.of(a, b);
		for (Player player : players)
		{
			if (!box.contains(player.getLocation().toVector())) continue;

			Manager.GetDamage().NewDamageEvent(player, thrower, null, DamageCause.CUSTOM, _damageDistance, true, true, false, thrower.getName(), GetName());
			Manager.GetCondition().Factory().Slow(GetName(), player, thrower, 2, 0, true, true, false, false);
		}
	}

	@Override
	public void Idle(ProjectileUser data)
	{
		Collide(null, null, data);
	}

	@Override
	public void Expire(ProjectileUser data)
	{
		Collide(null, null, data);
	}
	

}