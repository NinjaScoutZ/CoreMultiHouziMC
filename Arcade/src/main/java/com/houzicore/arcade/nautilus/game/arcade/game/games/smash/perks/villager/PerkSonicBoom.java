package com.houzicore.arcade.nautilus.game.arcade.game.games.smash.perks.villager;

import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

import com.houzicore.shared.common.util.C;
import com.houzicore.shared.common.util.F;
import com.houzicore.shared.common.util.UtilAction;
import com.houzicore.shared.common.util.UtilAlg;
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

import com.houzicore.arcade.nautilus.game.arcade.game.games.smash.perks.SmashPerk;

public class PerkSonicBoom extends SmashPerk
{

	private int _cooldown;
	private int _distance;
	private double _damage;
	private float _hitBox, _velocityFactor;

	public PerkSonicBoom()
	{
		super("Sonic Hurr", new String[]
				{
						C.cYellow + "Right-Click" + C.cGray + " Axe to use " + C.cGreen + "Sonic Hurr",
				});
	}

	@Override
	public void setupValues()
	{
		_cooldown = getPerkTime("Cooldown");
		_distance = getPerkInt("Distance");
		_damage = getPerkDouble("Damage");
		_hitBox = getPerkFloat("Hitbox");
		_velocityFactor = getPerkFloat("Velocity Factor");
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	public void playerInteract(PlayerInteractEvent event)
	{
		if (event.isCancelled() || !UtilEvent.isAction(event, ActionType.R) || UtilBlock.usable(event.getClickedBlock()))
		{
			return;
		}

		Player player = event.getPlayer();
		ItemStack itemStack = player.getInventory().getItemInMainHand();

		if (!itemStack.getType().name().endsWith("_AXE") || !hasPerk(player) || !Recharge.Instance.use(player, GetName(), _cooldown, true, true))
		{
			return;
		}

		Location location = player.getEyeLocation();
		location.add(location.getDirection());

		player.sendMessage(F.main("Game", "You used " + F.skill(GetName()) + "."));
		player.getWorld().playSound(location, Sound.ENTITY_VILLAGER_AMBIENT, 1.5F, 1.2F);

		playSonicBoomEffect(location, _distance);

		Location center = location.clone().add(location.getDirection().multiply(_distance / 2D)).subtract(0, 0.5, 0);

		for (Player nearby : UtilPlayer.getNearby(center, _hitBox))
		{
			if (player.equals(nearby) || isTeamDamage(player, nearby))
			{
				return;
			}

			double scale = 1 - UtilMath.offset(player, nearby) / _distance / 2;

			Manager.GetDamage().NewDamageEvent(nearby, player, null, DamageCause.CUSTOM, scale * _damage, false, true, false, player.getName(), GetName());

			UtilAction.velocity(nearby, UtilAlg.getTrajectory(player, nearby), scale * _velocityFactor, false, 0, 0.5, 0.8, true);
		}
	}

	private void playSonicBoomEffect(Location origin, int distance)
	{
		Vector dir = origin.getDirection().normalize();
		Vector right;
		if (Math.abs(dir.getY()) > 0.9)
		{
			right = dir.getCrossProduct(new Vector(1, 0, 0)).normalize();
		}
		else
		{
			right = dir.getCrossProduct(new Vector(0, 1, 0)).normalize();
		}
		Vector up = right.getCrossProduct(dir).normalize();

		double radius = 0.0;
		double theta = 0.0;
		double z = 0.0;
		double maxRadius = 2.0;

		for (int i = 0; i < distance * 10; i++)
		{
			double x = radius * Math.cos(theta);
			double y = radius * Math.sin(theta);
			z += 0.1;

			Location loc1 = origin.clone().add(dir.clone().multiply(z)).add(right.clone().multiply(x)).add(up.clone().multiply(y));
			Location loc2 = origin.clone().add(dir.clone().multiply(z)).subtract(right.clone().multiply(x)).subtract(up.clone().multiply(y));

			UtilParticle.PlayParticleToAll(ParticleType.CLOUD, loc1, 0f, 0f, 0f, 0f, 1, ViewDist.NORMAL);
			UtilParticle.PlayParticleToAll(ParticleType.CLOUD, loc2, 0f, 0f, 0f, 0f, 1, ViewDist.NORMAL);

			if (Math.random() < 0.05)
			{
				UtilParticle.PlayParticleToAll(ParticleType.ANGRY_VILLAGER, loc1, 0.3F, 0.3F, 0.3F, 0f, 1, ViewDist.NORMAL);
				UtilParticle.PlayParticleToAll(ParticleType.ANGRY_VILLAGER, loc2, 0.3F, 0.3F, 0.3F, 0f, 1, ViewDist.NORMAL);
			}

			theta += Math.PI / 20D;
			if (radius < maxRadius)
			{
				radius += 0.05;
			}
		}
	}
}
