package com.houzicore.arcade.nautilus.game.arcade.game.games.primalgames.kit;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;

import com.houzicore.shared.common.util.C;
import com.houzicore.shared.common.util.UtilMath;
import com.houzicore.arcade.nautilus.game.arcade.kit.Perk;
import com.houzicore.arcade.nautilus.game.arcade.kit.Kit;

public class PerkLumberjack extends Perk
{
	public PerkLumberjack() 
	{
		super("Timber", new String[] 
				{ 
				C.cGray + "Breaking a log block breaks the whole tree.",
				C.cGray + "10% chance to drop apples from logs."
				});
	}
		
	@EventHandler
	public void onTreeChop(BlockBreakEvent event)
	{
		Player player = event.getPlayer();
		if (!Kit.HasKit(player))
			return;

		Block block = event.getBlock();
		// Match any log
		if (block.getType().name().contains("LOG"))
		{
			timberDown(block);
		}
	}

	private void timberDown(Block block)
	{
		Block current = block;
		while (current.getType().name().contains("LOG"))
		{
			current.breakNaturally();

			if (UtilMath.r(100) < 10)
			{
				current.getWorld().dropItemNaturally(current.getLocation(), new ItemStack(Material.APPLE));
			}

			current = current.getRelative(BlockFace.UP);
		}
	}
}
