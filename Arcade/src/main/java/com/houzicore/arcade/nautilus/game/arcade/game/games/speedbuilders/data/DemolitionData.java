package com.houzicore.arcade.nautilus.game.arcade.game.games.speedbuilders.data;

import java.util.ArrayList;

import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.BlockState;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.material.Bed;
import org.bukkit.material.Door;

import com.houzicore.shared.common.util.MapUtil;
import com.houzicore.shared.common.util.NautHashMap;
import com.houzicore.shared.common.util.UtilAlg;
import com.houzicore.shared.common.util.UtilBlock;
import com.houzicore.shared.common.util.UtilEnt;
import com.houzicore.shared.common.util.UtilInv;
import com.houzicore.shared.common.util.UtilTime;
import com.houzicore.shared.core.hologram.Hologram;

public class DemolitionData
{

	public RecreationData Parent;

	public NautHashMap<Block, BlockState> Blocks;
	public ArrayList<Entity> Mobs;

	public long Start;

	private Hologram _hologram;

	private boolean _flickerAir = true;
	private long _lastFlicker = System.currentTimeMillis();

	public DemolitionData(RecreationData parent, ArrayList<Block> blocks, ArrayList<Entity> mobs)
	{
		Parent = parent;
		
		Blocks = new NautHashMap<Block, BlockState>();
		Mobs = mobs;
		
		for (Block block : blocks)
		{
			Blocks.put(block, block.getState());
		}
		
		Start = System.currentTimeMillis();
		
		spawnHologram();
	}

	private void setAir(Block block)
	{
		block.setType(Material.AIR, true);
	}

	public void spawnHologram()
	{
		if (Parent.Game.InstaBreak)
			return;
		
		Location loc = Parent.getMidpoint();
		
		if (!Blocks.isEmpty())
			loc = Blocks.keySet().iterator().next().getLocation().add(0.5, 0.5, 0.5);
		else if (!Mobs.isEmpty())
			loc = UtilAlg.Random(Mobs).getLocation().add(0, 1, 0);
		
		_hologram = new Hologram(Parent.Game.Manager.getHologramManager(), loc, "3");
		
		_hologram.start();
	}

	public void despawnHologram()
	{
		if (_hologram == null)
			return;
		
		_hologram.stop();
		
		_hologram = null;
	}

	public void update()
	{
		if (Parent.Game.InstaBreak)
		{
			breakBlocks();
			
			return;
		}
		
		if (_hologram == null)
			spawnHologram();
		
		int secondsLeft = (int) Math.ceil((3000 - (System.currentTimeMillis() - Start)) / 1000.0D);
		
		if (secondsLeft < 0)
			secondsLeft = 0;
			
		_hologram.setText("" + secondsLeft);
		
		if (UtilTime.elapsed(_lastFlicker, 500))
		{
			_lastFlicker = System.currentTimeMillis();
			
			for (Block block : Blocks.keySet())
			{
				if (_flickerAir)
					setAir(block);
				else
					Blocks.get(block).update(true, false);
			}
			
			for (Entity entity : Mobs)
			{
				if (_flickerAir)
					UtilEnt.ghost(entity, true, true);
				else
					UtilEnt.ghost(entity, true, false);
			}
			
			_flickerAir = !_flickerAir;
		}
		
		if (secondsLeft == 0)
			breakBlocks();
	}

	public void cancelBreak()
	{
		despawnHologram();

		for (Block block : Blocks.keySet())
		{
			Blocks.get(block).update(true, false);
		}
		
		for (Entity entity : Mobs)
		{
			UtilEnt.ghost(entity, true, false);
		}

		Parent.BlocksForDemolition.remove(this);
	}

	public void breakBlocks()
	{
		despawnHologram();
		
		try
		{
			//Effect will play for all blocks even two-parted ones
			for (Block block : Blocks.keySet())
			{
				Blocks.get(block).update(true, false);
				
				block.getWorld().playEffect(block.getLocation(), Effect.STEP_SOUND, block.getType());
			}
			
			for (Block block : Blocks.keySet())
			{
				if (block.getType() == Material.AIR)
					continue;
				
				if (block.getBlockData() instanceof org.bukkit.block.data.Bisected)
				{
					org.bukkit.block.data.Bisected bisected = (org.bukkit.block.data.Bisected) block.getBlockData();
					if (bisected.getHalf() == org.bukkit.block.data.Bisected.Half.TOP)
						continue;
				}
				
				Material type = block.getType();
				if (type.isItem())
				{
					UtilInv.insert(Parent.Player, new ItemStack(type, 1));
				}
				
				//Destroy the other part
				if (block.getType().name().endsWith("_BED"))
				{
					org.bukkit.block.data.type.Bed bed = (org.bukkit.block.data.type.Bed) block.getBlockData();
					
					if (bed.getPart() == org.bukkit.block.data.type.Bed.Part.HEAD)
						setAir(block.getRelative(bed.getFacing().getOppositeFace()));
					else
						setAir(block.getRelative(bed.getFacing()));
				}
				else if (block.getBlockData() instanceof org.bukkit.block.data.Bisected)
				{
					org.bukkit.block.data.Bisected bisected = (org.bukkit.block.data.Bisected) block.getBlockData();
					if (bisected.getHalf() == org.bukkit.block.data.Bisected.Half.BOTTOM)
					{
						setAir(block.getRelative(BlockFace.UP));
					}
				}
				
				setAir(block);
			}
			
			for (Entity entity : Mobs)
			{
				ItemStack spawnEgg = getSpawnEggItem(entity.getType());
				
				UtilInv.insert(Parent.Player, spawnEgg);
				
				entity.remove();
				
				Parent.Mobs.remove(entity);
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		finally
		{
			Parent.BlocksForDemolition.remove(this);
			
			Parent.Game.checkPerfectBuild(Parent.Player);
		}
	}

	private ItemStack getSpawnEggItem(org.bukkit.entity.EntityType type)
	{
		String name = type.name() + "_SPAWN_EGG";
		if (type == org.bukkit.entity.EntityType.MOOSHROOM) name = "MOOSHROOM_SPAWN_EGG";
		try
		{
			Material material = Material.valueOf(name);
			return new ItemStack(material, 1);
		}
		catch (IllegalArgumentException e)
		{
			return new ItemStack(Material.EGG, 1);
		}
	}

}
