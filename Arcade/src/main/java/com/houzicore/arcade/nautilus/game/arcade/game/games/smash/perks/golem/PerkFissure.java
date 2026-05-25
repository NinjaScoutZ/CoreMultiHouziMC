package com.houzicore.arcade.nautilus.game.arcade.game.games.smash.perks.golem;

import java.util.HashSet;
import java.util.Set;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerInteractEvent;

import com.houzicore.shared.common.util.C;
import com.houzicore.shared.common.util.F;
import com.houzicore.shared.common.util.UtilBlock;
import com.houzicore.shared.common.util.UtilEnt;
import com.houzicore.shared.common.util.UtilEvent;
import com.houzicore.shared.common.util.UtilEvent.ActionType;
import com.houzicore.shared.common.util.UtilItem;
import com.houzicore.shared.common.util.UtilPlayer;
import com.houzicore.shared.recharge.Recharge;
import com.houzicore.shared.updater.UpdateType;
import com.houzicore.shared.updater.event.UpdateEvent;

import com.houzicore.arcade.nautilus.game.arcade.game.games.smash.perks.SmashPerk;
import com.houzicore.arcade.nautilus.game.arcade.kit.Perk;
import com.houzicore.arcade.nautilus.game.arcade.kit.perks.data.FissureData;

public class PerkFissure extends SmashPerk
{

	private int _cooldown;

	private Set<FissureData> _active = new HashSet<>();

	public PerkFissure()
	{
		super("Fissure", new String[] { C.cYellow + "Right-Click" + C.cGray + " with Axe to " + C.cGreen + "Fissure" });
	}

	@Override
	public void setupValues()
	{
		_cooldown = getPerkTime("Cooldown");
	}

	@EventHandler
	public void Leap(PlayerInteractEvent event)
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

		if (!UtilEnt.isGrounded(player))
		{
			UtilPlayer.message(player, F.main("Game", "You cannot use " + F.skill(GetName()) + " while airborne."));
			return;
		}

		if (!Recharge.Instance.use(player, GetName(), _cooldown, true, true))
		{
			return;
		}

		Location location = player.getLocation();
		FissureData data = new FissureData(this, player, location.getDirection(), location.add(location.getDirection()).add(0, -0.4, 0));
		_active.add(data);

		// Inform
		UtilPlayer.message(player, F.main("Game", "You used " + F.skill(GetName()) + "."));
	}

	@EventHandler
	public void Update(UpdateEvent event)
	{
		if (event.getType() != UpdateType.TICK)
		{
			return;
		}
		
		Set<FissureData> remove = new HashSet<>();

		for (FissureData data : _active)
		{
			if (data.Update())
			{
				remove.add(data);
			}
		}

		for (FissureData data : remove)
		{
			_active.remove(data);
			data.Clear();
		}
	}
}
