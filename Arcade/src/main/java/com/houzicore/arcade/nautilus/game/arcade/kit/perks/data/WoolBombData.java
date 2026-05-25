package com.houzicore.arcade.nautilus.game.arcade.kit.perks.data;

import org.bukkit.block.Block;
import org.bukkit.Material;

public class WoolBombData
{
	public Block Block;
	public long Time;
	public org.bukkit.block.data.BlockData BlockData;
	
	public WoolBombData(Block block)
	{
		Block = block;
		BlockData = block.getBlockData().clone();
		
		Time = System.currentTimeMillis();
	}

	public void restore()
	{
		Block.setBlockData(BlockData);
	}
}
