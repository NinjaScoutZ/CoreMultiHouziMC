package com.houzicore.arcade.nautilus.game.arcade.game.games.smash.perks.blaze;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

import com.houzicore.shared.common.util.C;
import com.houzicore.shared.common.util.F;
import com.houzicore.shared.common.util.UtilBlock;
import com.houzicore.shared.common.util.UtilEvent;
import com.houzicore.shared.common.util.UtilEvent.ActionType;
import com.houzicore.shared.common.util.UtilItem;
import com.houzicore.shared.common.util.UtilMath;
import com.houzicore.shared.common.util.UtilPlayer;
import com.houzicore.shared.common.util.UtilServer;
import com.houzicore.shared.updater.UpdateType;
import com.houzicore.shared.updater.event.UpdateEvent;
import com.houzicore.arcade.nautilus.game.arcade.game.games.smash.perks.SmashPerk;

public class PerkInferno extends SmashPerk
{

	private static final float MAX_ENERGY = 0.999F;
	private static final ItemStack POWDER = new ItemStack(Material.BLAZE_POWDER);

	private float _energyTick = 0.025F;
	private float _energyItem = 0.035F;

	private double _itemExpireTime = 0.7;
	private double _itemBurnTime = 0.5;
	private double _itemDamage = 0.25;
	private float _itemVelocityMagnitude = 1.6F;

	private Map<UUID, Long> _active = new HashMap<>();

	public PerkInferno()
	{
		super("Inferno", new String[] { C.cYellow + "Hold Block" + C.cGray + " to use " + C.cGreen + "Inferno" });
	}

	@Override
	public void setupValues()
	{
		_energyTick = getPerkFloat("Energy Tick");
		_energyItem = getPerkFloat("Energy Item");
		_itemExpireTime = getPerkDouble("Expire Time");
		_itemBurnTime = getPerkDouble("Burn Time");
		_itemDamage = getPerkDouble("Damage");
		_itemVelocityMagnitude = getPerkFloat("Velocity Magnitude");
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

			if (!player.isBlocking())
			{
				player.setExp(Math.min(MAX_ENERGY, player.getExp() + _energyTick));
			}
		}
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

		_active.put(player.getUniqueId(), System.currentTimeMillis());

		UtilPlayer.message(player, F.main("Skill", "You used " + F.skill(GetName()) + "."));
	}

	@EventHandler
	public void Update(UpdateEvent event)
	{
		if (event.getType() != UpdateType.TICK)
		{
			return;
		}

		for (Player cur : UtilServer.getPlayers())
		{
			UUID key = cur.getUniqueId();

			if (!_active.containsKey(key))
			{
				continue;
			}

			if (!cur.isBlocking())
			{
				_active.remove(key);
				continue;
			}

			cur.setExp(cur.getExp() - _energyItem);

			if (cur.getExp() <= 0)
			{
				_active.remove(key);
				continue;
			}

			// Fire
			Location location = cur.getEyeLocation();
			Item fire = cur.getWorld().dropItem(location, POWDER);
			Manager.GetFire().Add(fire, cur, _itemExpireTime, 0.0, _itemBurnTime, (int) _itemDamage, GetName());

			fire.setVelocity(location.getDirection().multiply(_itemVelocityMagnitude));

			// Effect
			cur.getWorld().playSound(location, Sound.ENTITY_GHAST_SHOOT, 0.1f, 1f);
		}
	}
	
//	private Vector getRandomVector()
//	{
//		double x = 0.07 - (UtilMath.r(14) / 100);
//		double y = 0.07 - (UtilMath.r(14) / 100);
//		double z = 0.07 - (UtilMath.r(14) / 100);
//
//		return new Vector(x, y, z);
//	}
}
