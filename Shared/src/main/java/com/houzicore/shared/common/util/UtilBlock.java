package com.houzicore.shared.common.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.inventory.ItemStack;

public class UtilBlock
{
    /**
     * A list of blocks that are usable
     */
    public static HashSet<Material> blockUseSet = new HashSet<>();
    /**
     * A list of blocks that are always solid and can be stood on
     */
    public static HashSet<Material> fullSolid = new HashSet<>();
    /**
     * A list of blocks that are non-solid, but offer resistance. Eg lily, fence gate, portal
     */
    public static HashSet<Material> blockPassSet = new HashSet<>();
    /**
     * A list of blocks that offer zero resistance (long grass, torch, flower)
     */
    public static HashSet<Material> blockAirFoliageSet = new HashSet<>();
    
    static {
        blockAirFoliageSet.add(Material.AIR);
        blockAirFoliageSet.add(Material.CAVE_AIR);
        blockAirFoliageSet.add(Material.VOID_AIR);
        blockAirFoliageSet.add(Material.TALL_GRASS);
        blockAirFoliageSet.add(Material.SHORT_GRASS);
        blockAirFoliageSet.add(Material.FERN);
        blockAirFoliageSet.add(Material.LARGE_FERN);
        blockAirFoliageSet.add(Material.DANDELION);
        blockAirFoliageSet.add(Material.POPPY);
        blockAirFoliageSet.add(Material.BLUE_ORCHID);
        blockAirFoliageSet.add(Material.ALLIUM);
        blockAirFoliageSet.add(Material.AZURE_BLUET);
        blockAirFoliageSet.add(Material.RED_TULIP);
        blockAirFoliageSet.add(Material.ORANGE_TULIP);
        blockAirFoliageSet.add(Material.WHITE_TULIP);
        blockAirFoliageSet.add(Material.PINK_TULIP);
        blockAirFoliageSet.add(Material.OXEYE_DAISY);
        blockAirFoliageSet.add(Material.CORNFLOWER);
        blockAirFoliageSet.add(Material.LILY_OF_THE_VALLEY);
        blockAirFoliageSet.add(Material.WITHER_ROSE);
        blockAirFoliageSet.add(Material.SUNFLOWER);
        blockAirFoliageSet.add(Material.LILAC);
        blockAirFoliageSet.add(Material.ROSE_BUSH);
        blockAirFoliageSet.add(Material.PEONY);

        blockPassSet.add(Material.AIR);
        blockPassSet.add(Material.WATER);
        blockPassSet.add(Material.LAVA);
        blockPassSet.add(Material.WHEAT);
        blockPassSet.add(Material.SUGAR_CANE);
        blockPassSet.add(Material.TORCH);
        blockPassSet.add(Material.WALL_TORCH);
        blockPassSet.add(Material.VINE);
        // ... many more could be added, but this is a start for 1.21.1
        
        // Full Solid
        fullSolid.add(Material.STONE);
        fullSolid.add(Material.GRASS_BLOCK);
        fullSolid.add(Material.DIRT);
        fullSolid.add(Material.COBBLESTONE);
        
        // Use Set
        blockUseSet.add(Material.CHEST);
        blockUseSet.add(Material.TRAPPED_CHEST);
        blockUseSet.add(Material.ENDER_CHEST);
        blockUseSet.add(Material.CRAFTING_TABLE);
        blockUseSet.add(Material.FURNACE);
        blockUseSet.add(Material.DISPENSER);
        blockUseSet.add(Material.DROPPER);
        blockUseSet.add(Material.HOPPER);
    }
	
	public static boolean solid(Block block)
	{
		if (block == null)			return false;
		return solid(block.getType());
	}
	public static boolean solid(Material material)
	{
		return !blockPassSet.contains(material);
	}

	public static boolean airFoliage(Block block)
	{
		if (block == null)			return false;
		return airFoliage(block.getType());
	}
	public static boolean airFoliage(Material material)
	{
		return blockAirFoliageSet.contains(material);
	}

	public static boolean fullSolid(Block block)
	{
		if (block == null)
			return false;

		return fullSolid(block.getType());
	}
	public static boolean fullSolid(Material material)
	{
		return fullSolid.contains(material);
	}

	public static boolean usable(Block block)
	{
		if (block == null)
			return false;

		return usable(block.getType());
	}
	public static boolean usable(Material material)
	{
		return blockUseSet.contains(material);
	}

	public static HashMap<Block, Double> getInRadius(Location loc, double dR)
	{
		return getInRadius(loc, dR, 9999);
	}

	public static HashMap<Block, Double> getInRadius(Location loc, double dR, double maxHeight)
	{
		HashMap<Block, Double> blockList = new HashMap<Block, Double>();
		int iR = (int)dR + 1;

		for (int x=-iR ; x <= iR ; x++)
			for (int z=-iR ; z <= iR ; z++)
				for (int y=-iR ; y <= iR ; y++)
				{
					if (Math.abs(y) > maxHeight)
						continue;

					Block curBlock = loc.getWorld().getBlockAt((int)(loc.getX()+x), (int)(loc.getY()+y), (int)(loc.getZ()+z));

					double offset = UtilMath.offset(loc, curBlock.getLocation().add(0.5, 0.5, 0.5));;

					if (offset <= dR)
						blockList.put(curBlock, 1 - (offset/dR));
				}

		return blockList;
	}


	public static HashMap<Block, Double> getInRadius(Block block, double dR)
	{
		return getInRadius(block, dR, false);
	}

	public static HashMap<Block, Double> getInRadius(Block block, double dR, boolean hollow)
	{
		HashMap<Block, Double> blockList = new HashMap<Block, Double>();
		int iR = (int)dR + 1;

		for (int x=-iR ; x <= iR ; x++)
			for (int z=-iR ; z <= iR ; z++)
				for (int y=-iR ; y <= iR ; y++)
				{
					Block curBlock = block.getRelative(x, y, z);

					double offset = UtilMath.offset(block.getLocation(), curBlock.getLocation());

					if (offset <= dR && !(hollow && offset < dR - 1))
					{
						blockList.put(curBlock, 1 - (offset / dR));
					}
				}

		return blockList;
	}

	public static ArrayList<Block> getInSquare(Block block, double dR)
	{
		ArrayList<Block> blockList = new ArrayList<Block>();
		int iR = (int)dR + 1;

		for (int x=-iR ; x <= iR ; x++)
			for (int z=-iR ; z <= iR ; z++)
				for (int y=-iR ; y <= iR ; y++)
				{
					blockList.add(block.getRelative(x, y, z));
				}

		return blockList;
	}

	public static boolean isBlock(ItemStack item) 
	{
		if (item == null)
			return false;

		return item.getType().isBlock();
	}

	public static Block getHighest(World world, int x, int z)
	{
		return getHighest(world, x, z, null);
	}

	public static Block getHighest(World world, int x, int z, HashSet<Material> ignore)
	{
		Block block = world.getHighestBlockAt(x, z);

		//Shuffle Down
		while (block.getY() > 0 && 
				(
						airFoliage(block) || 
						block.getType() == Material.OAK_LEAVES || 
						(ignore != null && ignore.contains(block.getType()))
				))
		{
			block = block.getRelative(BlockFace.DOWN);
		}

		return block.getRelative(BlockFace.UP); 
	}

    // Stubbing out getExplosionBlocks as it requires complex NMS logic for raytracing through blocks with resistance
    public static List<Block> getExplosionBlocks(Location location, float strength, boolean damageBlocksEqually)
    {
        // Simple approximation or empty list for now to allow compilation
        return new ArrayList<>();
    }

	public static ArrayList<Block> getSurrounding(Block block, boolean diagonals) 
	{
		ArrayList<Block> blocks = new ArrayList<Block>();

		if (diagonals)
		{
			for (int x=-1 ; x<= 1 ; x++)
				for (int y=-1 ; y<= 1 ; y++)
					for (int z=-1 ; z<= 1 ; z++)
					{
						if (x == 0 && y == 0 && z == 0)
							continue;

						blocks.add(block.getRelative(x, y, z));
					}
		}
		else
		{
			blocks.add(block.getRelative(BlockFace.UP));
			blocks.add(block.getRelative(BlockFace.DOWN));
			blocks.add(block.getRelative(BlockFace.NORTH));
			blocks.add(block.getRelative(BlockFace.SOUTH));
			blocks.add(block.getRelative(BlockFace.EAST));
			blocks.add(block.getRelative(BlockFace.WEST));
		}

		return blocks;
	}

	public static boolean isVisible(Block block)
	{
		for (Block other : UtilBlock.getSurrounding(block, false))
		{
			if (!other.getType().isOccluding()) 
			{
				return true;
			}
		}

		return false;
	}
	public static ArrayList<Block> getInBoundingBox(Location a,	Location b)
	{
		ArrayList<Block> blocks = new ArrayList<Block>();
		
		for (int x=Math.min(a.getBlockX(), b.getBlockX()) ; x<=Math.max(a.getBlockX(), b.getBlockX()) ; x++)
			for (int y=Math.min(a.getBlockY(), b.getBlockY()) ; y<=Math.max(a.getBlockY(), b.getBlockY()) ; y++)
				for (int z=Math.min(a.getBlockZ(), b.getBlockZ()) ; z<=Math.max(a.getBlockZ(), b.getBlockZ()) ; z++)
				{
					Block block = a.getWorld().getBlockAt(x,y,z);
					
					if (block.getType() != Material.AIR)
						blocks.add(block);
				}
		
		return blocks;
	}
}

