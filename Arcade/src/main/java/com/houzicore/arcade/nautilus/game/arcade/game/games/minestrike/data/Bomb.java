package com.houzicore.arcade.nautilus.game.arcade.game.games.minestrike.data;

import com.houzicore.shared.common.util.UtilParticle;
import com.houzicore.shared.common.util.UtilServer;
import com.houzicore.shared.common.util.UtilTime;
import com.houzicore.shared.common.util.UtilParticle.ParticleType;
import com.houzicore.shared.common.util.UtilParticle.ViewDist;

import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.Player;

public class Bomb
{
	public long BombTime = 45000;
	
	public Block Block; 
	
	public Material Type;
	public BlockData Data;
	
	public long StartTime;
	public long LastBeep;
	
	public long MinBeepTime = 40;
	public long StartBeepTime = 2000;
	
	public Player Planter;
	
	public Bomb(Player planter)
	{
		Planter = planter;
		
		Block = planter.getLocation().getBlock();
		
		Type = Block.getType();
		Data = Block.getBlockData();
		
		Block.setType(Material.DAYLIGHT_DETECTOR);
		
		StartTime = System.currentTimeMillis();
	}
	
	public boolean update()
	{
		if (Block.getType() != Material.DAYLIGHT_DETECTOR)
			Block.setType(Material.DAYLIGHT_DETECTOR);
		
		double scale = (double)(System.currentTimeMillis() - StartTime)/(double)BombTime;
		
		long beepDuration = MinBeepTime + (long)(StartBeepTime * (1-scale));
		float volume = 1f + 4f*(float)scale;
		
		if (UtilTime.elapsed(LastBeep, beepDuration))
		{
			Block.getWorld().playSound(Block.getLocation(), Sound.BLOCK_ANVIL_BREAK, volume, 1f);
			UtilParticle.PlayParticle(ParticleType.HAPPY_VILLAGER, Block.getLocation().add(0.5, 0.5, 0.5), 0, 0, 0, 0, 1,
					ViewDist.LONG, UtilServer.getPlayers());
			
			LastBeep = System.currentTimeMillis();
		}
		
		if (UtilTime.elapsed(StartTime, BombTime))
		{
			clean();
			
			//Effect
			UtilParticle.PlayParticle(ParticleType.HUGE_EXPLOSION, Block.getLocation(), 10f, 10f, 10f, 0, 30,
					ViewDist.MAX, UtilServer.getPlayers());
			
			for (int i=0 ; i<3 ; i++)
				Block.getWorld().playSound(Block.getLocation(), Sound.BLOCK_ANVIL_LAND, 20f, (float)(Math.random() * 0.5 + 0.5));
			
			return true;
		}
		
		return false;
	}
	
	public boolean isBlock(Block block)
	{
		if (block == null)
			return false;
		
		return block.equals(Block);
	}

	public void defuse()
	{
		clean();
		
		//Effect
		UtilParticle.PlayParticle(ParticleType.CLOUD, Block.getLocation().add(0.5, 0.5, 0.5), 0, 0, 0, 0, 1,
				ViewDist.LONG, UtilServer.getPlayers());
	}

	public void clean()
	{
		Block.setBlockData(Data);
	}
}
