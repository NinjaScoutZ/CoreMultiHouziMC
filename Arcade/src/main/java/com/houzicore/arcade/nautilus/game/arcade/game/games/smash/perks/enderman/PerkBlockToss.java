package com.houzicore.arcade.nautilus.game.arcade.game.games.smash.perks.enderman;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.bukkit.Effect;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.EnderCrystal;
import org.bukkit.entity.FallingBlock;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityChangeBlockEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.player.PlayerInteractEvent;

import com.houzicore.shared.common.util.C;
import com.houzicore.shared.common.util.F;
import com.houzicore.shared.common.util.UtilAction;
import com.houzicore.shared.common.util.UtilBlock;
import com.houzicore.shared.common.util.UtilEnt;
import com.houzicore.shared.common.util.UtilEvent;
import com.houzicore.shared.common.util.UtilEvent.ActionType;
import com.houzicore.shared.common.util.UtilItem;
import com.houzicore.shared.common.util.UtilPlayer;
import com.houzicore.shared.common.util.UtilServer;
import com.houzicore.shared.core.projectile.IThrown;
import com.houzicore.shared.core.projectile.ProjectileUser;
import com.houzicore.shared.recharge.Recharge;
import com.houzicore.shared.updater.UpdateType;
import com.houzicore.shared.updater.event.UpdateEvent;
import com.houzicore.shared.core.damage.CustomDamageEvent;

import com.houzicore.arcade.nautilus.game.arcade.game.games.smash.perks.SmashPerk;
import com.houzicore.arcade.nautilus.game.arcade.kit.perks.data.BlockTossData;
import com.houzicore.arcade.nautilus.game.arcade.kit.perks.event.PerkBlockGrabEvent;
import com.houzicore.arcade.nautilus.game.arcade.kit.perks.event.PerkBlockThrowEvent;

public class PerkBlockToss extends SmashPerk implements IThrown
{

	private int _cooldown;
	private int _chargeTime;
	private int _damage;
	private int _maxDamage;
	private float _knockbackMagnitude;

	private Map<UUID, BlockTossData> _hold = new HashMap<>();
	private Set<UUID> _charged = new HashSet<>();

	public PerkBlockToss()
	{
		super("Block Toss", new String[] { C.cYellow + "Hold Block" + C.cGray + " to " + C.cGreen + "Grab Block", C.cYellow + "Release Block" + C.cGray + " to " + C.cGreen + "Throw Block" });
	}

	@Override
	public void setupValues()
	{
		_cooldown = getPerkInt("Cooldown (ms)");
		_chargeTime = getPerkInt("Charge Time (ms)");
		_damage = getPerkInt("Damage");
		_maxDamage = getPerkInt("Max Damage");
		_knockbackMagnitude = getPerkFloat("Knockback Magnitude");
	}

	@Override
	public void unregisteredEvents()
	{
	    _hold.clear();
	}

	@EventHandler
	public void Grab(PlayerInteractEvent event)
	{
		if (event.isCancelled())
		{
			return;
		}

		if (!UtilEvent.isAction(event, ActionType.R_BLOCK))
		{
			return;
		}

		Material material = event.getClickedBlock().getType();
		if (UtilBlock.usable(event.getClickedBlock()) || material == Material.REDSTONE_WIRE || material.name().contains("SKULL") || material.name().contains("HEAD"))
		{
			return;
		}

		Player player = event.getPlayer();

		if (!player.getInventory().getItemInMainHand().getType().name().endsWith("_SWORD"))
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

		if (_hold.containsKey(player.getUniqueId()))
		{
			return;
		}

		Block grab = event.getClickedBlock();

		if (!Recharge.Instance.usable(player, GetName()))
		{
			return;
		}

		if (!UtilBlock.airFoliage(grab.getRelative(BlockFace.UP)) || Manager.GetBlockRestore().Contains(grab.getRelative(BlockFace.UP)))
		{
			UtilPlayer.message(player, F.main("Game", "You can only pick up blocks with Air above them."));
			return;
		}

		// Event
		PerkBlockGrabEvent blockEvent = new PerkBlockGrabEvent(player, com.houzicore.shared.common.util.IdUtil.getTypeId(grab), com.houzicore.shared.common.util.IdUtil.getData(grab));
		UtilServer.getServer().getPluginManager().callEvent(blockEvent);

		// Block to Data
		int id = com.houzicore.shared.common.util.IdUtil.getTypeId(grab);
		byte data = com.houzicore.shared.common.util.IdUtil.getData(grab);

		// Remove Block
		event.getClickedBlock().getWorld().playEffect(event.getClickedBlock().getLocation(), Effect.STEP_SOUND, event.getClickedBlock().getType());

		_hold.put(player.getUniqueId(), new BlockTossData(id, data, System.currentTimeMillis()));

		// Effect
		player.getWorld().playEffect(event.getClickedBlock().getLocation(), Effect.STEP_SOUND, grab.getType());
	}

	@EventHandler
	public void Throw(UpdateEvent event)
	{
		if (event.getType() != UpdateType.TICK)
		{
			return;
		}

		Set<Player> throwSet = new HashSet<>();

		for (UUID key : _hold.keySet())
		{
			Player player = UtilPlayer.searchExact(key);

			if (player == null)
			{
				continue;
			}

			// Throw
			if (!player.isBlocking())
			{
				throwSet.add(player);
			}

			// Charged Tick
			if (!_charged.contains(key))
			{
				if (System.currentTimeMillis() - _hold.get(key).Time > _chargeTime)
				{
					_charged.add(key);
					player.getWorld().playEffect(player.getLocation(), Effect.CLICK1, 0);
				}
			}
		}

		for (Player cur : throwSet)
		{
			UUID key = cur.getUniqueId();

			Recharge.Instance.recharge(cur, GetName());
			Recharge.Instance.use(cur, GetName(), _cooldown, false, true);

			BlockTossData data = _hold.remove(key);

			FallingBlock block = cur.getWorld().spawnFallingBlock(cur.getEyeLocation().add(cur.getLocation().getDirection()), com.houzicore.shared.common.util.IdUtil.getBlockData(data.Type, data.Data));

			_charged.remove(key);

			long charge = System.currentTimeMillis() - data.Time;

			// Throw
			double mult = Math.min(1.4, 1.4 * ((double) charge / _chargeTime));

			// Action
			UtilAction.velocity(block, cur.getLocation().getDirection(), mult, false, 0.2, 0, 1, true);
			Manager.GetProjectile().AddThrow(block, cur, this, -1, true, true, true, true, 1f);

			// Event
			PerkBlockThrowEvent blockEvent = new PerkBlockThrowEvent(cur);
			UtilServer.getServer().getPluginManager().callEvent(blockEvent);
		}
	}

	@Override
	public void Collide(LivingEntity target, Block block, ProjectileUser data)
	{
		if (target == null)
		{
			return;
		}

		if (target instanceof EnderCrystal)
		{
			data.GetThrown().remove();
			return;
		}

		// Damage Event
		Manager.GetDamage().NewDamageEvent(target, data.GetThrower(), null, DamageCause.PROJECTILE, Math.min(_maxDamage, data.GetThrown().getVelocity().length() * _damage), true, true, false, UtilEnt.getName(data
				.GetThrower()), GetName());

		// Block to Item
		if (data.GetThrown() instanceof FallingBlock)
		{
			FallingBlock thrown = (FallingBlock) data.GetThrown();

			data.GetThrown().getWorld().spawnFallingBlock(data.GetThrown().getLocation(), thrown.getBlockData());
			thrown.remove();
		}

	}

	@Override
	public void Idle(ProjectileUser data)
	{
	}

	@Override
	public void Expire(ProjectileUser data)
	{
	}



	@EventHandler
	public void BlockForm(EntityChangeBlockEvent event)
	{
		if (!(event.getEntity() instanceof FallingBlock))
		{
			return;
		}

		FallingBlock falling = (FallingBlock) event.getEntity();

		falling.getWorld().playEffect(event.getBlock().getLocation(), Effect.STEP_SOUND, falling.getBlockData().getMaterial());

		falling.remove();

		event.setCancelled(true);
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