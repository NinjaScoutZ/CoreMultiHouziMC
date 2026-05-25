package com.houzicore.arcade.nautilus.game.arcade.game.games.smash.perks.chicken;

import org.bukkit.GameMode;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerToggleFlightEvent;

import com.houzicore.shared.common.util.C;
import com.houzicore.shared.common.util.UtilAction;
import com.houzicore.shared.common.util.UtilBlock;
import com.houzicore.shared.common.util.UtilEnt;
import com.houzicore.shared.common.util.UtilPlayer;
import com.houzicore.shared.common.util.UtilServer;
import com.houzicore.shared.recharge.Recharge;
import com.houzicore.shared.updater.UpdateType;
import com.houzicore.shared.updater.event.UpdateEvent;
import com.houzicore.arcade.nautilus.game.arcade.game.games.smash.perks.SmashPerk;

public class PerkFlap extends SmashPerk
{
	
	private static final float MAX_ENERGY = 0.999F;

	private int _cooldown;
	private float _energyPerTick;
	private float _energyPerFlap;
	private double _power;
	private boolean _control;

	public PerkFlap()
	{
		super("Flap", new String[] { C.cYellow + "Tap Jump Twice" + C.cGray + " to " + C.cGreen + "Flap" });
	}

	@Override
	public void setupValues()
	{
		_cooldown = getPerkInt("Cooldown (ms)");
		_energyPerTick = getPerkFloat("Energy Per Tick");
		_energyPerFlap = getPerkFloat("Energy Per Flap");
		_power = getPerkDouble("Power");
		_control = getPerkBoolean("Control");
	}

	@EventHandler
	public void FlightHop(PlayerToggleFlightEvent event)
	{
		Player player = event.getPlayer();

		if (UtilPlayer.isSpectator(player))
		{
			return;
		}
		
		if (!hasPerk(player))
		{
			return;
		}
		
		if (player.getGameMode() == GameMode.CREATIVE)
		{
			return;
		}
		
		event.setCancelled(true);
		player.setFlying(false);

		// Disable Flight
		player.setAllowFlight(false);

		double power = 0.4 + _power;

		// Velocity
		if (_control)
		{
			UtilAction.velocity(player, power, 0.2, 10, true);
		}
		else
		{
			UtilAction.velocity(player, player.getLocation().getDirection(), power, true, power, 0.15, 10, true);
		}

		// Sound
		player.getWorld().playSound(player.getLocation(), Sound.ENTITY_BAT_TAKEOFF, (float) (0.3 + player.getExp()), (float) (Math.random() / 2 + 1));

		// Set Recharge
		Recharge.Instance.use(player, GetName(), _cooldown, false, false);

		// Energy
		if (!isSuperActive(player))
		{
			player.setExp(Math.max(0f, player.getExp() - _energyPerFlap));
		}
	}

	@EventHandler
	public void FlightUpdate(UpdateEvent event)
	{
		if (event.getType() != UpdateType.TICK)
		{
			return;
		}
		
		for (Player player : UtilServer.getPlayers())
		{
			if (player.getGameMode() == GameMode.CREATIVE)
			{
				continue;
			}
			
			if (!hasPerk(player))
			{
				continue;
			}
			
			if (UtilEnt.onBlock(player))
			{
				player.setExp(Math.min(MAX_ENERGY, player.getExp() + _energyPerTick));
				player.setAllowFlight(true);
			}
			else if (Recharge.Instance.usable(player, GetName()) && player.getExp() > 0)
			{
				player.setAllowFlight(true);
			}
		}
	}
}
