package com.houzicore.arcade.nautilus.game.arcade.kit.perks;

import java.util.HashSet;

import org.bukkit.Effect;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerToggleFlightEvent;

import com.houzicore.shared.common.util.C;
import com.houzicore.shared.common.util.UtilAction;
import com.houzicore.shared.common.util.UtilBlock;
import com.houzicore.shared.common.util.UtilEnt;
import com.houzicore.shared.common.util.UtilServer;
import com.houzicore.shared.recharge.Recharge;
import com.houzicore.shared.updater.UpdateType;
import com.houzicore.shared.updater.event.UpdateEvent;
import com.houzicore.arcade.nautilus.game.arcade.kit.Perk;

public class PerkDoubleJump extends Perk
{
	private double _power;
	private double _heightMax;
	private boolean _control;
	private long _recharge;
	private boolean _displayForce;
	
	private HashSet<Player> _disabled = new HashSet<Player>();
	
	public PerkDoubleJump(String name)
	{
		this(name, 0.9, 0.9, true);
	}

	public PerkDoubleJump(String name, double power, double heightLimit, boolean control) 
	{
		super(name, new String[] 
				{
				C.cYellow + "Tap Jump Twice" + C.cGray + " to " + C.cGreen + name
				});
		
		_power = power;
		_heightMax = heightLimit;
		_control = control;
		_recharge = 0;
		_displayForce = false;
	}
	
	public PerkDoubleJump(String name, double power, double heightLimit, boolean control, long recharge, boolean displayForce) 
	{
		super(name, new String[] 
				{
				C.cYellow + "Tap Jump Twice" + C.cGray + " to " + C.cGreen + name
				});
		
		_power = power;
		_heightMax = heightLimit;
		_control = control;
		_recharge = recharge;
		_displayForce = displayForce;
	}

	@EventHandler
	public void FlightHop(PlayerToggleFlightEvent event)
	{
		Player player = event.getPlayer();
		
		if (!Kit.HasKit(player))
			return;
		
		if (Manager.isSpectator(player))
			return;
		
		if (player.getGameMode() == GameMode.CREATIVE)
			return;
		
		event.setCancelled(true);
		player.setFlying(false);
		
		//Disable Flight
		player.setAllowFlight(false);
		
		//Velocity
		if (_control)
		{
			UtilAction.velocity(player, _power, 0.2, _heightMax, true);
		}
		else
		{
			UtilAction.velocity(player, player.getLocation().getDirection(), _power, true, _power, 0, _heightMax, true);
		}
		
		//Sound
		player.getWorld().playEffect(player.getLocation(), Effect.BLAZE_SHOOT, 0);
		
		//Recharge
		if (_recharge > 0)
		{
			Recharge.Instance.useForce(player, GetName(), _recharge);
			
			if (_displayForce)
			{
				Recharge.Instance.setDisplayForce(player, GetName(), true);
			}
		}
	} 
	
	@EventHandler
	public void FlightUpdate(UpdateEvent event)
	{
		if (event.getType() != UpdateType.TICK)
			return;

		for (Player player : UtilServer.getPlayers())
		{
			if (Manager.isSpectator(player))
				continue;
			
			if (!Kit.HasKit(player))
				continue;
			
			if (_recharge > 0 && !Recharge.Instance.usable(player, GetName()))
				continue;
			
			if (UtilEnt.isGrounded(player) || UtilBlock.solid(player.getLocation().getBlock().getRelative(BlockFace.DOWN)))
				player.setAllowFlight(true);
		}
	}

	public void disableForPlayer(Player player)
	{
		_disabled.add(player);
	}

	public void enableForPlayer(Player player)
	{
		_disabled.remove(player);
	}
}
