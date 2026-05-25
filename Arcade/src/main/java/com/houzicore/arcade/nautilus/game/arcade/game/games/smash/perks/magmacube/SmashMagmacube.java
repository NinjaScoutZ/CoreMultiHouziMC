package com.houzicore.arcade.nautilus.game.arcade.game.games.smash.perks.magmacube;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;

import com.houzicore.shared.common.util.UtilBlock;
import com.houzicore.shared.common.util.UtilPlayer;
import com.houzicore.shared.updater.UpdateType;
import com.houzicore.shared.updater.event.UpdateEvent;
import com.houzicore.arcade.nautilus.game.arcade.game.games.smash.perks.SmashUltimate;
import com.houzicore.arcade.nautilus.game.arcade.kit.perks.data.MeteorShowerData;

public class SmashMagmacube extends SmashUltimate
{

	private final List<MeteorShowerData> _meteors = new ArrayList<>();

	public SmashMagmacube()
	{
		super("Meteor Shower", new String[] {}, Sound.ENTITY_LIGHTNING_BOLT_THUNDER, 0);
	}

	@Override
	public void activate(Player player)
	{
		super.activate(player);

		_meteors.add(new MeteorShowerData(player, player.getTargetBlock(UtilBlock.blockPassSet, 128).getLocation()));
	}

	@EventHandler
	public void update(UpdateEvent event)
	{
		if (event.getType() != UpdateType.TICK)
		{
			return;
		}

		_meteors.removeIf(MeteorShowerData::update);
	}
}
