package com.houzicore.arcade.nautilus.game.arcade.game.games.hideseek.forms;

import org.bukkit.block.Block;

public class InfestedData
{
	public Block Block;
	public org.bukkit.block.data.BlockData BlockData;
	
	public InfestedData(Block block)
	{
		Block = block;
		BlockData = block.getBlockData().clone();
		
		block.setType(org.bukkit.Material.AIR);
	}
	
	public void restore()
	{
		Block.setBlockData(BlockData);
	}
}
