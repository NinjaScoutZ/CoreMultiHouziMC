package com.houzicore.arcade.nautilus.game.arcade.game.games.smash.perks.snowman;

import java.util.HashMap;
import java.util.Map;

import org.bukkit.Sound;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.entity.Snowball;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.util.Vector;

import com.houzicore.shared.common.util.C;
import com.houzicore.shared.common.util.UtilAction;
import com.houzicore.shared.common.util.UtilEnt;
import com.houzicore.shared.common.util.UtilMath;
import com.houzicore.shared.recharge.Recharge;
import com.houzicore.shared.updater.UpdateType;
import com.houzicore.shared.updater.event.UpdateEvent;
import com.houzicore.shared.core.damage.CustomDamageEvent;
import com.houzicore.arcade.nautilus.game.arcade.kit.Perk;

public class PerkBlizzard extends Perk
{

	private static final float MAX_ENERGY = 0.99F;
	private static final float ENERGY_PER_TICK = 1 / 60;
	private static final float ENERGY_PER_USE = 1 / 9;
	private static final int SNOWBALL_PER_USE = 4;

	private Map<Projectile, Player> _snowball = new HashMap<>();

	public PerkBlizzard()
	{
		super("Blizzard", new String[] { C.cYellow + "Hold Block" + C.cGray + " to use " + C.cGreen + "Blizzard" });
	}

	@EventHandler
	public void EnergyUpdate(UpdateEvent event)
	{
		if (event.getType() != UpdateType.TICK)
		{
			return;
		}

		for (Player player : Manager.GetGame().GetPlayers(true))
		{
			if (!hasPerk(player))
			{
				continue;
			}

			if (player.isBlocking())
			{
				continue;
			}

			player.setExp(Math.min(MAX_ENERGY, player.getExp() + ENERGY_PER_TICK));
		}
	}

	@EventHandler
	public void Snow(UpdateEvent event)
	{
		if (event.getType() != UpdateType.FASTEST)
		{
			return;
		}

		for (Player player : Manager.GetGame().GetPlayers(true))
		{
			if (!player.isBlocking())
			{
				continue;
			}

			if (!hasPerk(player))
			{
				continue;
			}

			// Energy
			if (player.getExp() < 0.1)
			{
				continue;
			}

			player.setExp(Math.max(0, player.getExp() - ENERGY_PER_USE));

			for (int i = 0; i < SNOWBALL_PER_USE; i++)
			{
				Snowball snow = player.getWorld().spawn(player.getEyeLocation().add(player.getLocation().getDirection()), Snowball.class);
				double x = 0.1 - (UtilMath.r(10) / 100d);
				double y = UtilMath.r(20) / 100d;
				double z = 0.1 - (UtilMath.r(10) / 100d);
				snow.setShooter(player);
				snow.setVelocity(player.getLocation().getDirection().add(new Vector(x, y, z)).multiply(2));
				_snowball.put(snow, player);
			}

			// Effect
			player.getWorld().playSound(player.getLocation(), Sound.BLOCK_SNOW_STEP, 0.1f, 0.5f);
		}
	}

	@EventHandler(priority = EventPriority.LOW)
	public void Snowball(CustomDamageEvent event)
	{
		if (event.GetCause() != DamageCause.PROJECTILE)
		{
			return;
		}

		Projectile proj = event.GetProjectile();

		if (proj == null)
		{
			return;
		}

		if (!(proj instanceof Snowball))
		{
			return;
		}

		if (!_snowball.containsKey(proj))
		{
			return;
		}

		LivingEntity damagee = event.GetDamageeEntity();

		if (damagee == null)
		{
			return;
		}

		event.SetCancelled("Blizzard");

		damagee.setVelocity(proj.getVelocity().multiply(0.15).add(new Vector(0, 0.15, 0)));

		// Damage Event
		if (damagee instanceof Player)
		{
			if (Recharge.Instance.use((Player) damagee, GetName(), 200, false, false))
			{
				Manager.GetDamage().NewDamageEvent(damagee, event.GetDamagerEntity(true), null, DamageCause.CUSTOM, 1, false, true, false, UtilEnt.getName(event.GetDamagerEntity(true)), GetName());
			}
		}
	}

	@EventHandler
	public void SnowballForm(ProjectileHitEvent event)
	{
		Projectile proj = event.getEntity();
		
		if (!(proj instanceof Snowball))
		{
			return;
		}
		
		if (_snowball.remove(proj) == null)
		{
			return;
		}
		
		Manager.GetBlockRestore().Snow(proj.getLocation().getBlock(), (byte) 1, (byte) 7, 2000, 250, 0);
	}
}
