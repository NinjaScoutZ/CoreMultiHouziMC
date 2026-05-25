package com.houzicore.arcade.nautilus.game.arcade.game.games.speedbuilders.data;

import java.util.ArrayList;
import java.util.List;

import com.houzicore.shared.common.util.C;
import com.houzicore.shared.common.util.MapUtil;
import com.houzicore.shared.common.util.NautHashMap;
import com.houzicore.shared.common.util.UtilAlg;
import com.houzicore.shared.common.util.UtilBlock;
import com.houzicore.shared.common.util.UtilEnt;
import com.houzicore.shared.common.util.UtilInv;
import com.houzicore.shared.core.hologram.Hologram;
import com.houzicore.arcade.nautilus.game.arcade.game.games.speedbuilders.SpeedBuilders;
import com.houzicore.arcade.nautilus.game.arcade.game.games.speedbuilders.SpeedBuildersState;
import com.houzicore.arcade.nautilus.game.arcade.game.games.speedbuilders.lang.SpeedBuildersLang;

import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.BlockState;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.material.Bed;
import org.bukkit.material.Door;
import org.bukkit.util.Vector;

public class RecreationData
{

	public SpeedBuilders Game;

	public Player Player;

	public BlockState[][] DefaultGround;

	public Location OriginalBuildLocation;

	public Location CornerA;
	public Location CornerB;

	public Location PlayerSpawn;

	public NautHashMap<Item, Long> DroppedItems = new NautHashMap<Item, Long>();

	public ArrayList<DemolitionData> BlocksForDemolition = new ArrayList<DemolitionData>();

	public ArrayList<Entity> Mobs = new ArrayList<Entity>();

	private Hologram _hologram;

	public RecreationData(SpeedBuilders game, Player player, Location loc, Location playerSpawn)
	{
		Game = game;
		
		DefaultGround = new BlockState[game.BuildSize][game.BuildSize];
		
		Player = player;
		
		OriginalBuildLocation = loc;
		
		// Use the marker location's X, Y, and Z directly from the YELLOW location
		Location buildCenter = loc.clone();
		
		// Calculate corners with proper Y boundaries - only modify X and Z for horizontal bounds
		// Y bounds should be from the buildCenter Y to buildCenter Y + BuildSizeMin1
		CornerA = new Location(
			buildCenter.getWorld(),
			Math.floor(buildCenter.getX()) - game.BuildSizeDiv2,
			buildCenter.getBlockY(),
			Math.floor(buildCenter.getZ()) - game.BuildSizeDiv2
		);
		CornerB = new Location(
			buildCenter.getWorld(),
			Math.floor(buildCenter.getX()) + game.BuildSizeDiv2,
			buildCenter.getBlockY() + game.BuildSizeMin1,
			Math.floor(buildCenter.getZ()) + game.BuildSizeDiv2
		);
		
		PlayerSpawn = playerSpawn;
		
		for (int x = 0; x < game.BuildSize; x++)
		{
			for (int z = 0; z < game.BuildSize; z++)
			{
				DefaultGround[x][z] = CornerA.clone().add(x, -1, z).getBlock().getState();
			}
		}

		Vector mid = game.getJudgeSpawn().toVector().subtract(loc.toVector()).multiply(0.4);
		Location hologramLocation = loc.clone().add(mid).add(0, 1, 0);
		Location safeCenter = loc.clone();
		safeCenter.setX(Math.floor(loc.getX()) + 0.5);
		safeCenter.setZ(Math.floor(loc.getZ()) + 0.5);
		Location above = safeCenter.clone().add(0, game.BuildSize + 1.5, 0);
		_hologram = new Hologram(game.getArcadeManager().getHologramManager(), above, C.cYellow + player.getName());
		_hologram.start();
	}

	public boolean inBuildArea(Block block) 
	{
		boolean xMin = block.getX() >= Math.min(CornerA.getBlockX(), CornerB.getBlockX());
		boolean yMin = block.getY() >= Math.min(CornerA.getBlockY(), CornerB.getBlockY());
		boolean zMin = block.getZ() >= Math.min(CornerA.getBlockZ(), CornerB.getBlockZ());
		boolean xMax = block.getX() <= Math.max(CornerA.getBlockX(), CornerB.getBlockX());
		boolean yMax = block.getY() <= Math.max(CornerA.getBlockY(), CornerB.getBlockY());
		boolean zMax = block.getZ() <= Math.max(CornerA.getBlockZ(), CornerB.getBlockZ());
		
		boolean result = xMin && yMin && zMin && xMax && yMax && zMax;
		if (!result && Player != null)
		{
			System.out.println("[SpeedBuilders-Debug] inBuildArea check failed for player " + Player.getName() 
				+ " | block: " + block.getType() + " at " + block.getX() + "," + block.getY() + "," + block.getZ() 
				+ " | CornerA: " + CornerA.getBlockX() + "," + CornerA.getBlockY() + "," + CornerA.getBlockZ()
				+ " | CornerB: " + CornerB.getBlockX() + "," + CornerB.getBlockY() + "," + CornerB.getBlockZ()
				+ " | OriginalBuildLocation: " + (OriginalBuildLocation != null ? (OriginalBuildLocation.getBlockX() + "," + OriginalBuildLocation.getBlockY() + "," + OriginalBuildLocation.getBlockZ()) : "null")
				+ " | Checks: xMin=" + xMin + " yMin=" + yMin + " zMin=" + zMin + " xMax=" + xMax + " yMax=" + yMax + " zMax=" + zMax);
		}
		return result;
	}

	public boolean inBuildArea(Location loc)
	{
		if (loc.getX() < Math.min(CornerA.getBlockX(), CornerB.getBlockX()))
			return false;
		
		if (loc.getY() < Math.min(CornerA.getBlockY(), CornerB.getBlockY()))
			return false;
		
		if (loc.getZ() < Math.min(CornerA.getBlockZ(), CornerB.getBlockZ()))
			return false;
		
		if (loc.getX() > Math.max(CornerA.getBlockX(), CornerB.getBlockX()) + 1)
			return false;
		
		if (loc.getY() > Math.max(CornerA.getBlockY(), CornerB.getBlockY()) + 1)
			return false;
		
		if (loc.getZ() > Math.max(CornerA.getBlockZ(), CornerB.getBlockZ()) + 1)
			return false;
		
		return true;
	}

	private void setAir(Block block)
	{
		block.setType(Material.AIR, true);
	}

	public void clearBuildArea(boolean resetGround)
	{
		for (Block block : getBlocks())
		{
			setAir(block);
		}
		
		for (Entity entity : Mobs)
		{
			entity.remove();
		}
		
		Mobs.clear();
		
		if (resetGround)
		{
			for (int x = 0; x < Game.BuildSize; x++)
			{
				for (int z = 0; z < Game.BuildSize; z++)
				{
					Block block = CornerA.clone().add(x, -1, z).getBlock();
					block.setBlockData(DefaultGround[x][z].getBlockData(), true);
				}
			}
		}
	}

	public void pasteBuildData(BuildData buildData)
	{
		clearBuildArea(true);
		
		for (int x = 0; x < Game.BuildSize; x++)
		{
			for (int z = 0; z < Game.BuildSize; z++)
			{
				Block block = CornerA.clone().add(x, -1, z).getBlock();
				block.setBlockData(buildData.Ground[x][z].getBlockData(), true);
			}
		}
		
		for (int x = 0; x < Game.BuildSize; x++)
		{
			for (int y = 0; y < Game.BuildSize; y++)
			{
				for (int z = 0; z < Game.BuildSize; z++)
				{
					Block block = CornerA.clone().add(x, y, z).getBlock();
					block.setBlockData(buildData.Build[x][y][z].getBlockData(), true);
				}
			}
		}
		
		Game.CreatureAllowOverride = true;
		
		for (MobData mobData : buildData.Mobs)
		{
			Location loc = CornerA.clone().add(mobData.DX + 0.5, mobData.DY, mobData.DZ + 0.5);
			
			Entity entity = loc.getWorld().spawnEntity(loc, mobData.EntityType);
			
			UtilEnt.Vegetate(entity, true);
			UtilEnt.ghost(entity, true, false);
			
			Mobs.add(entity);
		}
		
		Game.CreatureAllowOverride = false;
	}

	public void breakAndDropItems()
	{
		try
		{
			for (Block block : getBlocks())
			{
				if (block.getType() == Material.AIR)
					continue;
				
				if (block.getBlockData() instanceof org.bukkit.block.data.Bisected)
				{
					org.bukkit.block.data.Bisected bisected = (org.bukkit.block.data.Bisected) block.getBlockData();
					if (bisected.getHalf() == org.bukkit.block.data.Bisected.Half.TOP)
					{
						block.setType(Material.AIR, false);
						continue;
					}
				}
				
				Material type = block.getType();
				if (type.isItem())
				{
					UtilInv.insert(Player, new ItemStack(type, 1));
				}
				
				//Destroy the other part
				if (block.getType().name().endsWith("_BED"))
				{
					org.bukkit.block.data.type.Bed bed = (org.bukkit.block.data.type.Bed) block.getBlockData();
					Block relative = bed.getPart() == org.bukkit.block.data.type.Bed.Part.HEAD
						? block.getRelative(bed.getFacing().getOppositeFace())
						: block.getRelative(bed.getFacing());
					relative.setType(Material.AIR, false);
				}
				else if (block.getBlockData() instanceof org.bukkit.block.data.Bisected)
				{
					org.bukkit.block.data.Bisected bisected = (org.bukkit.block.data.Bisected) block.getBlockData();
					if (bisected.getHalf() == org.bukkit.block.data.Bisected.Half.BOTTOM)
					{
						block.getRelative(BlockFace.UP).setType(Material.AIR, false);
					}
				}
				
				block.setType(Material.AIR, false);
				for (Player p : Game.GetPlayers(true))
				{
					p.sendBlockChange(block.getLocation(), Material.AIR.createBlockData());
				}
			}
			
			for (Entity entity : Mobs)
			{
				ItemStack spawnEgg = getSpawnEggItem(entity.getType());
				UtilInv.insert(Player, spawnEgg);
				entity.remove();
			}
					
			CornerA.getWorld().playEffect(getMidpoint(), Effect.STEP_SOUND, Material.OAK_LOG);
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		finally
		{
			clearBuildArea(false);
		}
	}

	public boolean isEmptyBuild(BuildData buildData)
	{
		for (Block block : getBlocks())
		{
			if (block.getType() != Material.AIR)
				return false;
		}
		
		if (!buildData.Mobs.isEmpty())
			return Mobs.isEmpty();
		
		return true;
	}

	public int calculateScoreFromBuild(BuildData buildData)
	{
		int score = 0; 
		
		for (int x = 0; x < Game.BuildSize; x++)
		{
			for (int y = 0; y < Game.BuildSize; y++)
			{
				for (int z = 0; z < Game.BuildSize; z++)
				{
					Block currentBlock = CornerA.clone().add(x, y, z).getBlock();
					BlockState expectedState = buildData.Build[x][y][z];
					
					if (expectedState.getType() == Material.AIR)
						continue;
					
					if (expectedState.getType() == currentBlock.getType())
					{
						// 1. Redstone Ore (ignore lit state)
						if (currentBlock.getType() == Material.REDSTONE_ORE)
						{
							score++;
							continue;
						}
						
						// 2. Stairs check
						if (currentBlock.getBlockData() instanceof org.bukkit.block.data.type.Stairs && expectedState.getBlockData() instanceof org.bukkit.block.data.type.Stairs)
						{
							org.bukkit.block.data.type.Stairs currentStairs = (org.bukkit.block.data.type.Stairs) currentBlock.getBlockData();
							org.bukkit.block.data.type.Stairs expectedStairs = (org.bukkit.block.data.type.Stairs) expectedState.getBlockData();
							
							if (currentStairs.getFacing() == expectedStairs.getFacing() && currentStairs.getHalf() == expectedStairs.getHalf())
							{
								org.bukkit.block.data.type.Stairs.Shape expectedShape = buildData.StairShapes[x][y][z];
								org.bukkit.block.data.type.Stairs.Shape currentShape = currentStairs.getShape();
								
								if (expectedShape != null && currentShape != null)
								{
									boolean isExpectedInner = expectedShape == org.bukkit.block.data.type.Stairs.Shape.INNER_LEFT || expectedShape == org.bukkit.block.data.type.Stairs.Shape.INNER_RIGHT;
									boolean isCurrentInner = currentShape == org.bukkit.block.data.type.Stairs.Shape.INNER_LEFT || currentShape == org.bukkit.block.data.type.Stairs.Shape.INNER_RIGHT;
									boolean isExpectedOuter = expectedShape == org.bukkit.block.data.type.Stairs.Shape.OUTER_LEFT || expectedShape == org.bukkit.block.data.type.Stairs.Shape.OUTER_RIGHT;
									boolean isCurrentOuter = currentShape == org.bukkit.block.data.type.Stairs.Shape.OUTER_LEFT || currentShape == org.bukkit.block.data.type.Stairs.Shape.OUTER_RIGHT;
									
									if ((isExpectedInner && isCurrentInner) || (isExpectedOuter && isCurrentOuter) || (expectedShape == currentShape))
									{
										score++;
									}
								}
								else
								{
									score++;
								}
							}
							continue;
						}
						
						// 3. Directional check (e.g. chests, furnaces, pistons)
						if (currentBlock.getBlockData() instanceof org.bukkit.block.data.Directional && expectedState.getBlockData() instanceof org.bukkit.block.data.Directional)
						{
							org.bukkit.block.data.Directional currentDir = (org.bukkit.block.data.Directional) currentBlock.getBlockData();
							org.bukkit.block.data.Directional expectedDir = (org.bukkit.block.data.Directional) expectedState.getBlockData();
							if (currentDir.getFacing() == expectedDir.getFacing())
							{
								score++;
							}
							continue;
						}
						
						// 4. Default check
						score++;
					}
				}
			}
		}
		
		for (MobData mobData : buildData.Mobs)
		{
			for (Entity entity : Mobs)
			{
				int dx = (int) (entity.getLocation().getX() - (CornerA.getX() + 0.5));
				int dy = (int) (entity.getLocation().getY() - CornerA.getY());
				int dz = (int) (entity.getLocation().getZ() - (CornerA.getZ() + 0.5));
				
				if (mobData.EntityType == entity.getType() && mobData.DX == dx && mobData.DY == dy && mobData.DZ == dz)
				{
					score++;
					
					break;
				}
			}
		}
		
		return score;
	}

	public Location getMidpoint()
	{
		return UtilAlg.getMidpoint(CornerA, CornerB.clone().add(1, 1, 1));
	}

	public List<Block> getBlocks()
	{
		return UtilBlock.getInBoundingBox(CornerA, CornerB);
	}

	public boolean isQueuedForDemolition(Block block)
	{
		for (DemolitionData demolition : BlocksForDemolition)
		{
			if (demolition.Blocks.containsKey(block))
				return true;
		}
		
		return false;
	}

	public boolean isQueuedForDemolition(Entity entity)
	{
		for (DemolitionData demolition : BlocksForDemolition)
		{
			if (demolition.Mobs.contains(entity))
				return true;
		}
		
		return false;
	}

	public void addToDemolition(Block block)
	{
		if (isQueuedForDemolition(block))
			return;
		
		ArrayList<Block> blocks = new ArrayList<Block>();
		blocks.add(block);
		
		//Add the other part of the block
		if (block.getType().name().endsWith("_BED"))
		{
			org.bukkit.block.data.type.Bed bed = (org.bukkit.block.data.type.Bed) block.getBlockData();
			
			if (bed.getPart() == org.bukkit.block.data.type.Bed.Part.HEAD)
				blocks.add(block.getRelative(bed.getFacing().getOppositeFace()));
			else
				blocks.add(block.getRelative(bed.getFacing()));
		}
		else if (block.getBlockData() instanceof org.bukkit.block.data.Bisected)
		{
			org.bukkit.block.data.Bisected bisected = (org.bukkit.block.data.Bisected) block.getBlockData();
			
			if (bisected.getHalf() == org.bukkit.block.data.Bisected.Half.TOP)
				blocks.add(block.getRelative(BlockFace.DOWN));
			else
				blocks.add(block.getRelative(BlockFace.UP));
		}
		
		BlocksForDemolition.add(new DemolitionData(this, blocks, new ArrayList<Entity>()));
	}

	public void addToDemolition(Entity entity)
	{
		if (isQueuedForDemolition(entity))
			return;
		
		ArrayList<Entity> mobs = new ArrayList<Entity>();
		mobs.add(entity);
		
		BlocksForDemolition.add(new DemolitionData(this, new ArrayList<Block>(), mobs));
	}

	public void updateHologramProgress()
	{
		Integer ts = Game.getCumulativeScores().get(Player);
		int totalScore = (ts != null) ? ts : 0;
		Integer cs = Game.getComboStreaks().get(Player);
		int combo = (cs != null) ? cs : 0;
		
		if (Game.getSpeedBuilderState() == SpeedBuildersState.BUILDING)
		{
			int current = calculateScoreFromBuild(Game.getCurrentBuild());
			int total = Game.getCurrentBuild().getPerfectScore();
			int percent = total > 0 ? (int) (((double) current / total) * 100d) : 0;
			
			String percentColor;
			if (percent >= 100) percentColor = C.cAqua + C.Bold;
			else if (percent >= 75) percentColor = C.cGreen;
			else if (percent >= 50) percentColor = C.cYellow;
			else percentColor = C.cRed;
			
			String completedStr = SpeedBuildersLang.get().get(Player, "speedbuilders.hologram.completed");
			_hologram.setText(
				C.cYellow + Player.getName() + " §7- " + C.cGreen + totalScore + " pts",
				percentColor + percent + "% " + completedStr + " §7(x" + combo + ")"
			);
		}
		else
		{
			_hologram.setText(
				C.cYellow + Player.getName() + " §7- " + C.cGreen + totalScore + " pts",
				C.cGold + "Combo: " + combo + "x"
			);
		}
	}

	public void removeHologram()
	{
		_hologram.stop();
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
