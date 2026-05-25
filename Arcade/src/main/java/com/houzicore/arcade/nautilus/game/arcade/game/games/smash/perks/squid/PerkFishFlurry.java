package com.houzicore.arcade.nautilus.game.arcade.game.games.smash.perks.squid;

import com.houzicore.shared.common.util.*;
import com.houzicore.shared.common.util.UtilEvent.ActionType;
import com.houzicore.shared.common.util.UtilParticle.ParticleType;
import com.houzicore.shared.common.util.UtilParticle.ViewDist;
import com.houzicore.shared.core.itemstack.ItemStackFactory;
import com.houzicore.shared.core.projectile.IThrown;
import com.houzicore.shared.core.projectile.ProjectileUser;
import com.houzicore.shared.recharge.Recharge;
import com.houzicore.shared.updater.UpdateType;
import com.houzicore.shared.updater.event.UpdateEvent;
import com.houzicore.shared.core.damage.CustomDamageEvent;
import com.houzicore.arcade.nautilus.game.arcade.game.games.smash.perks.SmashPerk;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Item;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.util.Vector;

import java.util.*;

public class PerkFishFlurry extends SmashPerk implements IThrown
{

	private int _cooldown;
	private float _damage;
	private int _startTime;
	private int _endTime;
	private float _knockbackMagnitude;
	
	private int _tickCount = 0;
	private List<DataSquidGeyser> _active = new ArrayList<>();

	public PerkFishFlurry()
	{
		super("Fish Flurry", new String[] { C.cYellow + "Right-Click" + C.cGray + " with Spade to use " + C.cGreen + "Fish Flurry" });
	}

	@Override
	public void setupValues()
	{
		_cooldown = getPerkTime("Cooldown");
		_damage = getPerkFloat("Damage");
		_startTime = getPerkTime("Start Time");
		_endTime = getPerkTime("End Time");
		_knockbackMagnitude = getPerkFloat("Knockback Magnitude");
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

		Block block = player.getTargetBlock(UtilBlock.blockAirFoliageSet, 64);

		if (block == null || block.getType() == Material.AIR)
		{
			UtilPlayer.message(player, F.main("Game", "You must target a block."));
			return;
		}

		event.setCancelled(true);

		Set<Block> blocks = new HashSet<>();

		for (Block cur : UtilBlock.getInRadius(block, 3.5d).keySet())
		{
			if (cur == null)
			{
				continue;
			}

			if (UtilBlock.airFoliage(cur))
			{
				continue;
			}

			if (!UtilBlock.airFoliage(cur.getRelative(BlockFace.UP)))
			{
				continue;
			}

			blocks.add(cur);
		}

		if (!blocks.isEmpty())
		{
			if (!Recharge.Instance.use(player, GetName(), _cooldown, true, true))
			{
				return;
			}

			_active.add(new DataSquidGeyser(player, blocks));

			// Inform
			UtilPlayer.message(player, F.main("Game", "You used " + F.skill(GetName()) + "."));
		}
	}

	@EventHandler
	public void update(UpdateEvent event)
	{
		if (event.getType() != UpdateType.TICK)
		{
			return;
		}

		_tickCount++;

		Iterator<DataSquidGeyser> activeIter = _active.iterator();

		while (activeIter.hasNext())
		{
			DataSquidGeyser data = activeIter.next();

			if (_tickCount % 3 == 0)
			{
				// particles
				for (Block block : data.Blocks)
				{
					UtilParticle.PlayParticleToAll(ParticleType.SPLASH, block.getLocation().add(0.5, 1, 0.5), 0.25f, 0, 0.25f, 0, 1, ViewDist.LONG);
				}
			}

			// sound
			Block block = UtilAlg.Random(data.Blocks);
			if (Math.random() > 0.5)
			{
				block.getWorld().playSound(block.getLocation(), Math.random() > 0.5 ? Sound.ENTITY_GENERIC_SPLASH : Sound.ENTITY_PLAYER_SPLASH, 0.5f, 1f);
			}

			if (!UtilTime.elapsed(data.StartTime, _startTime))
			{
				continue;
			}
			
			if (!UtilTime.elapsed(data.StartTime, _endTime))
			{
				Item fish = block.getWorld().dropItem(block.getLocation().add(0.5, 1.5, 0.5), ItemStackFactory.Instance.CreateStack(Material.COD, (byte) UtilMath.r(4), 1, "Fish" + Math.random()));

				Vector random = new Vector(Math.random() - 0.5, 1 + Math.random() * 1, Math.random() - 0.5);

				UtilAction.velocity(fish, random, 0.25 + 0.4 * Math.random(), false, 0, 0.2, 10, false);

				Manager.GetProjectile().AddThrow(fish, data.Player, this, -1, true, false, true, true, 1f);
			}
			else
			{
				activeIter.remove();
			}
		}
	}

	@Override
	public void Collide(LivingEntity target, Block block, ProjectileUser data)
	{
		if (target != null)
		{
			// Damage Event
			Manager.GetDamage().NewDamageEvent(target, data.GetThrower(), null, DamageCause.PROJECTILE, _damage, false, true, false, UtilEnt.getName(data.GetThrower()), GetName());

			UtilParticle.PlayParticle(ParticleType.EXPLODE, target.getLocation().add(0, 1, 0), 1f, 1f, 1f, 0, 12, ViewDist.LONG, UtilServer.getPlayers());
		}

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

	@EventHandler
	public void Knockback(CustomDamageEvent event)
	{
		if (event.GetReason() == null || !event.GetReason().contains(GetName()))
		{
			return;
		}
		
		event.AddKnockback(GetName(), _knockbackMagnitude);
		event.setKnockbackOrigin(event.GetDamageeEntity().getLocation().add(Math.random() - 0.5, -0.1, Math.random() - 0.5));
	}
}