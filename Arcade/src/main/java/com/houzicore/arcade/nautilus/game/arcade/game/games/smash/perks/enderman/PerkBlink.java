package com.houzicore.arcade.nautilus.game.arcade.game.games.smash.perks.enderman;

import org.bukkit.Color;
import org.bukkit.FireworkEffect.Type;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerInteractEvent;

import org.bukkit.Location;
import org.bukkit.block.BlockFace;
import org.bukkit.util.Vector;

import com.houzicore.shared.common.util.C;
import com.houzicore.shared.common.util.F;
import com.houzicore.shared.common.util.UtilBlock;
import com.houzicore.shared.common.util.UtilEvent;
import com.houzicore.shared.common.util.UtilEvent.ActionType;
import com.houzicore.shared.common.util.UtilFirework;
import com.houzicore.shared.common.util.UtilItem;
import com.houzicore.shared.common.util.UtilParticle.ParticleType;
import com.houzicore.shared.common.util.UtilPlayer;
import com.houzicore.shared.common.util.UtilServer;
import com.houzicore.shared.recharge.Recharge;
import com.houzicore.arcade.nautilus.game.arcade.game.games.smash.perks.SmashPerk;

public class PerkBlink extends SmashPerk
{

	private static final float INCREMENTATION = 0.2F;
	
	private String _name;
	private double _range;
	private int _recharge;

	public PerkBlink(String name)
	{
		this(name, 0, 0);
	}

	public PerkBlink(String name, double range, int recharge)
	{
		super(name, new String[] { C.cYellow + "Right-Click" + C.cGray + " with Axe to " + C.cGreen + name });

		_name = name;
		_range = range;
		_recharge = recharge;
	}

	@Override
	public void setupValues()
	{
		_range = getPerkDouble("Range");
		_recharge = getPerkTime("Cooldown");
	}

	@EventHandler
	public void Blink(PlayerInteractEvent event)
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

		if (!Recharge.Instance.use(player, _name, _recharge, true, true))
		{
			return;
		}

		Location start = player.getEyeLocation();
		Vector direction = player.getLocation().getDirection();
		Location lastLocation = start.clone();
		double curRange = 0;
		boolean done = false;

		while (!done)
		{
			if (curRange > _range)
			{
				done = true;
			}
			Location newTarget = start.clone().add(direction.clone().multiply(curRange));

			if (newTarget.getY() < 0)
			{
				newTarget.add(0, 0.2, 0);
			}

			lastLocation = newTarget;

			if (UtilBlock.solid(newTarget.getBlock()) && UtilBlock.solid(newTarget.getBlock().getRelative(BlockFace.UP)))
			{
				done = true;
			}

			curRange += INCREMENTATION;

			com.houzicore.shared.common.util.UtilParticle.PlayParticle(ParticleType.LARGE_SMOKE, newTarget, 0f, 0f, 0f, 0f, 1, com.houzicore.shared.common.util.UtilParticle.ViewDist.LONG, UtilServer.getPlayers());
		}

		Location destination = lastLocation.subtract(direction);

		// Firework
		UtilFirework.playFirework(player.getEyeLocation(), Type.BALL, Color.BLACK, false, false);

		player.playSound(player.getLocation(), Sound.ENTITY_ENDERMAN_TELEPORT, 1f, 1f);
		player.teleport(destination);
		player.setFallDistance(0);
		player.playSound(player.getLocation(), Sound.ENTITY_ENDERMAN_TELEPORT, 1f, 1f);

		// Firework
		UtilFirework.playFirework(player.getEyeLocation(), Type.BALL, Color.BLACK, false, false);

		// Inform
		UtilPlayer.message(player, F.main("Game", "You used " + F.skill(_name) + "."));
	}
}
