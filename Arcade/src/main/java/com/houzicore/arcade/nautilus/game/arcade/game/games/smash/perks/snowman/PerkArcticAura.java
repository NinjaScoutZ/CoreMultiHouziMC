package com.houzicore.arcade.nautilus.game.arcade.game.games.smash.perks.snowman;

import java.util.Map;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;

import com.houzicore.shared.common.util.UtilBlock;
import com.houzicore.shared.common.util.UtilPlayer;
import com.houzicore.shared.updater.UpdateType;
import com.houzicore.shared.updater.event.UpdateEvent;
import com.houzicore.arcade.nautilus.game.arcade.kit.Perk;

public class PerkArcticAura extends Perk
{
	
	private int _duration;
	private int _range;
	
	public PerkArcticAura()
	{
		super("Arctic Aura", new String[] { "You freeze things around you, slowing enemies." });
	}

	@Override
	public void setupValues()
	{
		_duration = getPerkTime("Duration");
		_range = getPerkInt("Range");
	}

	@EventHandler
	public void SnowAura(UpdateEvent event)
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
			
			if (UtilPlayer.isSpectator(player))
			{
				continue;
			}
			
			double range = _range * player.getExp();
			
			Map<Block, Double> blocks = UtilBlock.getInRadius(player.getLocation(), range);
			
			for (Block block : blocks.keySet())
			{
				if (block.getType() == Material.SNOW_BLOCK)
				{
					continue;
				}

				Manager.GetBlockRestore().Snow(block, (byte) 1, (byte) 1, (int) (_duration * (1 + blocks.get(block))), 250, 0);
			}
		}
	}
}
