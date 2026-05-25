package com.houzicore.arcade.nautilus.game.arcade.game.games.mineware.challenges;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Entity;
import org.bukkit.entity.FallingBlock;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;

import com.houzicore.shared.common.util.MapUtil;
import com.houzicore.shared.common.util.IdUtil;
import com.houzicore.shared.common.util.UtilMath;
import com.houzicore.shared.common.util.UtilTime;
import com.houzicore.shared.updater.UpdateType;
import com.houzicore.shared.updater.event.UpdateEvent;
import com.houzicore.arcade.nautilus.game.arcade.game.games.mineware.Challenge;
import com.houzicore.arcade.nautilus.game.arcade.game.games.mineware.MineWare;

public class ChallengeRunner extends Challenge
{
	private ArrayList<Entity> _fallingBlocks = new ArrayList<Entity>();

	public ChallengeRunner(MineWare host)
	{
		super(host, ChallengeType.LastStanding,
				"Blocks are disappearing beneath you! Run away!");
	}

	@Override
	public ArrayList<Location> getSpawns()
	{
		return _spawns;
	}

	@Override
	public void cleanupRoom()
	{
		for (Entity ent : _fallingBlocks)
		{
			ent.remove();
		}
	}

	@Override
	public void setupPlayers()
	{
	}

	private ArrayList<Location> _spawns = new ArrayList<Location>();

	@Override
	public void generateRoom()
	{
		int amount = (int) Math.ceil(Math.sqrt(getChallengers().size()));
		int a = UtilMath.r(16);

		for (int pX = 0; pX < amount; pX++)
		{
			for (int pZ = 0; pZ < amount; pZ++)
			{
				if (++a > 15)
				{
					a = 0;
				}
				else if (a == 14)
				{
					a++;
				}

				_spawns.add(getCenter()
						.add((pX * 4) + 1.5, 1.1, (pZ * 4) + 1.5));

				for (int x = pX * 4; x < (pX * 4) + 2; x++)
				{
					for (int z = pZ * 4; z < (pZ * 4) + 2; z++)
					{
						Block b = getCenter().getBlock().getRelative(x, 0, z);
						b.setType(IdUtil.getMaterial(159, (byte) a));

						addBlock(b);
					}
				}
			}
		}
	}

	@EventHandler
	public void BlockBreak(UpdateEvent event)
	{
		if (event.getType() != UpdateType.TICK)
			return;

		// Add Blocks
		for (Player player : getChallengers())
		{
			// Side Standing
			double xMod = player.getLocation().getX() % 1;
			if (player.getLocation().getX() < 0)
				xMod += 1;

			double zMod = player.getLocation().getZ() % 1;
			if (player.getLocation().getZ() < 0)
				zMod += 1;

			int xMin = 0;
			int xMax = 0;
			int zMin = 0;
			int zMax = 0;

			if (xMod < 0.3)
				xMin = -1;
			if (xMod > 0.7)
				xMax = 1;

			if (zMod < 0.3)
				zMin = -1;
			if (zMod > 0.7)
				zMax = 1;

			for (int x = xMin; x <= xMax; x++)
			{
				for (int z = zMin; z <= zMax; z++)
				{
					AddBlock(player.getLocation().add(x, -0.5, z).getBlock());
				}
			}
		}

		Iterator<Block> blockIterator = _blocks.keySet().iterator();

		while (blockIterator.hasNext())
		{
			Block block = blockIterator.next();

			if (!UtilTime.elapsed(_blocks.get(block), 600))
				continue;

			// Fall
			int id = IdUtil.getTypeId(block);
			byte data = IdUtil.getData(block);
			MapUtil.QuickChangeBlockAt(block.getLocation(), Material.AIR);
			// FallingBlock ent = block.getWorld().spawnFallingBlock( // incomplete statement fix
					// block.getLocation(), id, data);
			// _fallingBlocks.add(ent);

			blockIterator.remove();
		}
	}

	public void AddBlock(Block block)
	{
		if (block == null || IdUtil.getTypeId(block) == 0 || IdUtil.getTypeId(block) == 7
				|| block.isLiquid())
			return;

		if (IdUtil.getTypeId(block.getRelative(BlockFace.UP)) != 0)
			return;

		if (_blocks.containsKey(block))
			return;

		_blocks.put(block, System.currentTimeMillis());

		// block.setType(Material.values()[159]) /* TODO metadata (byte) 14 */; // int→Material migration needed
	}

	private HashMap<Block, Long> _blocks = new HashMap<Block, Long>();

}
