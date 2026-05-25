package com.houzicore.arcade.nautilus.game.arcade.game.games.wizards.spells;

import java.util.HashMap;

import com.houzicore.shared.common.util.UtilAction;
import com.houzicore.shared.common.util.UtilParticle;
import com.houzicore.shared.common.util.UtilServer;
import com.houzicore.shared.common.util.UtilParticle.ParticleType;
import com.houzicore.shared.common.util.UtilParticle.ViewDist;
import com.houzicore.shared.common.util.UtilShapes;
import com.houzicore.shared.core.itemstack.ItemStackFactory;
import com.houzicore.shared.core.projectile.IThrown;
import com.houzicore.shared.core.projectile.ProjectileUser;
import com.houzicore.shared.updater.UpdateType;
import com.houzicore.shared.updater.event.UpdateEvent;
import com.houzicore.arcade.nautilus.game.arcade.game.games.wizards.Spell;
import com.houzicore.arcade.nautilus.game.arcade.game.games.wizards.spellinterfaces.SpellClick;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;

public class SpellIceShards extends Spell implements SpellClick, IThrown
{
	private HashMap<Entity, Location> _lastParticles = new HashMap<Entity, Location>();

	@Override
	public void castSpell(final Player player)
	{
		shoot(player);

		for (int i = 1; i <= getSpellLevel(player); i++)
		{

			Bukkit.getScheduler().scheduleSyncDelayedTask(Wizards.getArcadeManager().getPlugin(), new Runnable()
			{

				@Override
				public void run()
				{
					shoot(player);
				}

			}, i * 5);
		}

		charge(player);
	}

	private void shoot(Player player)
	{

		if (Wizards.IsAlive(player))
		{
			// Boost

			org.bukkit.entity.Item ent = player.getWorld().dropItem(
					player.getEyeLocation(),
					ItemStackFactory.Instance.CreateStack(Material.GHAST_TEAR, (byte) 0, 1, "Ice Shard " + player.getName() + " "
							+ System.currentTimeMillis()));

			UtilAction.velocity(ent, player.getLocation().getDirection(), 2, false, 0, 0.2, 10, false);
			Wizards.getArcadeManager().GetProjectile().AddThrow(ent, player, this, -1, true, true, true, false, 2f);

			player.getWorld().playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1.2F, 0.8F);

			_lastParticles.put(ent, ent.getLocation());

		}
	}

	@Override
	public void Collide(LivingEntity target, Block block, ProjectileUser data)
	{
		if (target != null && target instanceof Player)
		{

			// Damage Event
			Wizards.getArcadeManager()
					.GetDamage()
					.NewDamageEvent(target, data.GetThrower(), null, DamageCause.PROJECTILE, 4 /*+ (timesHit * 2)*/, true, true,
							false, "Ice Shard", "Ice Shard");

		}

		handleShard(data);
	}

	private void handleShard(ProjectileUser data)
	{
		data.GetThrown().remove();
		Location loc = data.GetThrown().getLocation();

		UtilParticle.PlayParticle(ParticleType.SNOW_SHOVEL, loc, 0.3F, 0.3F, 0.3F, 0, 12,
				ViewDist.LONG, UtilServer.getPlayers());
		loc.getWorld().playSound(loc, Sound.BLOCK_GLASS_BREAK, 1.2F, 1);

		for (int x = -1; x <= 1; x++)
		{
			for (int y = -1; y <= 1; y++)
			{
				for (int z = -1; z <= 1; z++)
				{
					Block block = loc.clone().add(x, y, z).getBlock();

					if (block.getType() == Material.FIRE)
					{
						block.setType(Material.AIR);
					}
				}
			}
		}

		_lastParticles.remove(data.GetThrown());
	}

	@EventHandler
	public void onTick(UpdateEvent event)
	{
		if (event.getType() != UpdateType.TICK)
		{
			return;
		}

		for (Entity entity : _lastParticles.keySet())
		{
			for (Location loc : UtilShapes.getLinesDistancedPoints(_lastParticles.get(entity), entity.getLocation(), 0.3))
			{
				UtilParticle.PlayParticle(ParticleType.SNOW_SHOVEL, loc, 0, 0, 0, 0, 1,
						ViewDist.LONG, UtilServer.getPlayers());
			}

			_lastParticles.put(entity, entity.getLocation());
		}
	}

	@Override
	public void Idle(ProjectileUser data)
	{
		handleShard(data);
	}

	@Override
	public void Expire(ProjectileUser data)
	{
		handleShard(data);
	}

}
