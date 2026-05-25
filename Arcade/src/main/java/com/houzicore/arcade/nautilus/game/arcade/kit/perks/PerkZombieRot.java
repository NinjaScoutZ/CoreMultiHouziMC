package com.houzicore.arcade.nautilus.game.arcade.kit.perks;

import java.util.HashMap;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;

import com.houzicore.shared.common.util.C;
import com.houzicore.shared.common.util.UtilBlock;
import com.houzicore.shared.common.util.UtilMath;
import com.houzicore.shared.updater.UpdateType;
import com.houzicore.shared.updater.event.UpdateEvent;
import com.houzicore.arcade.nautilus.game.arcade.kit.SmashPerk;

public class PerkZombieRot extends SmashPerk
{
	public PerkZombieRot()
	{
		super("Rot", new String[] 
				{
				C.cGray + "Leave a path that slows and prevents jumping.",
				});
	}

	@EventHandler 
	public void SnowAura(UpdateEvent event) 
	{
		if (event.getType() != UpdateType.TICK)
			return;

		for (Player player : Manager.GetGame().GetPlayers(true))
		{	
			if (!Kit.HasKit(player))
				continue;

			//Blocks
			HashMap<Block, Double> blocks = UtilBlock.getInRadius(player.getLocation(), 3);
			for (Block block : blocks.keySet())
			{			
				if (UtilBlock.solid(block.getRelative(BlockFace.UP)))
					continue;
				
				if (!UtilBlock.solid(block))
					continue;
				
				//Snow
				Manager.GetBlockRestore().Add(block, 159, (byte)12, 3000);
			}
		}
		
		for (Player player : Manager.GetGame().GetPlayers(true))
		{
			if (Kit.HasKit(player))
				continue;
			
			Block under = player.getLocation().getBlock().getRelative(BlockFace.DOWN);
			if (com.houzicore.shared.common.util.IdUtil.getTypeId(under) != 159 ||
				com.houzicore.shared.common.util.IdUtil.getData(under) != 12)
				continue;
			
			Manager.GetCondition().Factory().Slow(GetName(), player, null, 1.9, 1, false, false, false, false);
			Manager.GetCondition().Factory().Jump(GetName(), player, null, 1.9, 244, false, false, false);
		}
	}
}
