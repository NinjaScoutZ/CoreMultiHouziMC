package com.houzicore.shared.core.blockrestore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;

import com.houzicore.shared.MiniPlugin;
import com.houzicore.shared.updater.event.UpdateEvent;
import com.houzicore.shared.updater.UpdateType;
import com.houzicore.shared.common.util.UtilBlock;
import com.houzicore.shared.common.util.UtilMath;

import org.bukkit.Particle;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPistonExtendEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.plugin.java.JavaPlugin;

public class BlockRestore extends MiniPlugin {
	private final HashMap<Block, BlockRestoreData> _blocks = new HashMap<>();

	public BlockRestore(JavaPlugin plugin) {
		super("Block Restore", plugin);
	}

	public void Add(Block block, int toID, byte toData, int fromID, byte fromData, long expireTime) {
		if (!Contains(block)) {
			GetBlocks().put(block, new BlockRestoreData(block, toID, toData, fromID, fromData, expireTime, 0));
		} else {
			GetData(block).update(toID, toData, expireTime);
		}
	}

	public void Add(Block block, int toID, byte toData, long expireTime) {
		Add(block, toID, toData, com.houzicore.shared.common.util.IdUtil.getTypeId(block),
				com.houzicore.shared.common.util.IdUtil.getData(block), expireTime);
	}

	@EventHandler(priority = EventPriority.LOW)
	public void BlockBreak(BlockBreakEvent event) {
		if (Contains(event.getBlock())) {
			event.setCancelled(true);
		}
	}

	@EventHandler(priority = EventPriority.LOW)
	public void BlockPlace(BlockPlaceEvent event) {
		if (Contains(event.getBlockPlaced())) {
			event.setCancelled(true);
		}
	}

	public boolean Contains(Block block) {
		if (GetBlocks().containsKey(block))
			return true;
		return false;
	}

	@EventHandler
	public void ExpireBlocks(UpdateEvent event) {
		if (event.getType() != UpdateType.TICK)
			return;

		final ArrayList<Block> toRemove = new ArrayList<>();

		for (final BlockRestoreData cur : _blocks.values())
			if (cur.expire()) {
				toRemove.add(cur._block);
			}

		// Remove Handled
		for (final Block cur : toRemove) {
			_blocks.remove(cur);
		}
	}

	public HashMap<Block, BlockRestoreData> GetBlocks() {
		return _blocks;
	}

	public BlockRestoreData GetData(Block block) {
		if (_blocks.containsKey(block))
			return _blocks.get(block);
		return null;
	}

	@EventHandler(priority = EventPriority.LOW)
	public void Piston(BlockPistonExtendEvent event) {
		if (event.isCancelled())
			return;

		Block push = event.getBlock();
		for (int i = 0; i < 13; i++) {
			push = push.getRelative(event.getDirection());

			if (push.getType() == Material.AIR)
				return;

			if (Contains(push)) {
				push.getWorld().spawnParticle(
						Particle.BLOCK,
						push.getLocation().add(0.5, 0.5, 0.5),
						5, 0.2, 0.2, 0.2, 0.0,
						push.getBlockData());
				event.setCancelled(true);
				return;
			}
		}
	}

	public void Restore(Block block) {
		if (!Contains(block))
			return;

		_blocks.remove(block).restore();
	}

	public void RestoreAll() {
		for (final BlockRestoreData data : _blocks.values()) {
			data.restore();
		}

		_blocks.clear();
	}

	public HashSet<Location> RestoreBlockAround(Material type, Location location, int radius) {
		final HashSet<Location> restored = new HashSet<>();

		final Iterator<Block> blockIterator = _blocks.keySet().iterator();

		while (blockIterator.hasNext()) {
			final Block block = blockIterator.next();

			if (block.getType() != type) {
				continue;
			}

			if (UtilMath.offset(block.getLocation().add(0.5, 0.5, 0.5), location) > radius) {
				continue;
			}

			restored.add(block.getLocation().add(0.5, 0.5, 0.5));

			_blocks.get(block).restore();

			blockIterator.remove();
		}

		return restored;
	}

	public void Snow(Block block, byte heightAdd, byte heightMax, long expireTime, long meltDelay, int heightJumps) {
		// Fill Above
		if ((com.houzicore.shared.common.util.IdUtil.getTypeId(block) == 78
				&& com.houzicore.shared.common.util.IdUtil.getData(block) >= (byte) 7
				|| com.houzicore.shared.common.util.IdUtil.getTypeId(block) == 80)
				&& GetData(block) != null) {
			GetData(block).update(78, heightAdd, expireTime, meltDelay);

			if (heightJumps > 0) {
				Snow(block.getRelative(BlockFace.UP), heightAdd, heightMax, expireTime, meltDelay, heightJumps - 1);
			}
			if (heightJumps == -1) {
				Snow(block.getRelative(BlockFace.UP), heightAdd, heightMax, expireTime, meltDelay, -1);
			}

			return;
		}

		// Not Grounded
		if (!UtilBlock.solid(block.getRelative(BlockFace.DOWN))
				&& com.houzicore.shared.common.util.IdUtil.getTypeId(block.getRelative(BlockFace.DOWN)) != 78)
			return;

		// Not on Solid Snow
		if (com.houzicore.shared.common.util.IdUtil.getTypeId(block.getRelative(BlockFace.DOWN)) == 78
				&& com.houzicore.shared.common.util.IdUtil.getData(block.getRelative(BlockFace.DOWN)) < (byte) 7)
			return;

		// No Snow on Ice
		if (com.houzicore.shared.common.util.IdUtil.getTypeId(block.getRelative(BlockFace.DOWN)) == 79
				|| com.houzicore.shared.common.util.IdUtil.getTypeId(block.getRelative(BlockFace.DOWN)) == 174)
			return;

		// No Snow on Slabs
		if (com.houzicore.shared.common.util.IdUtil.getTypeId(block.getRelative(BlockFace.DOWN)) == 44
				|| com.houzicore.shared.common.util.IdUtil.getTypeId(block.getRelative(BlockFace.DOWN)) == 126)
			return;

		// No Snow on Stairs
		if (block.getRelative(BlockFace.DOWN).getType().toString().contains("STAIRS"))
			return;

		// No Snow on Fence or Walls
		if (block.getRelative(BlockFace.DOWN).getType().name().toLowerCase().contains("fence")
				|| block.getRelative(BlockFace.DOWN).getType().name().toLowerCase().contains("wall"))
			return;

		// Not Buildable
		if (!UtilBlock.airFoliage(block) && com.houzicore.shared.common.util.IdUtil.getTypeId(block) != 78 && block.getType() != Material.WHITE_CARPET)
			return;

		// Limit Build Height
		if (com.houzicore.shared.common.util.IdUtil.getTypeId(block) == 78)
			if (com.houzicore.shared.common.util.IdUtil.getData(block) >= (byte) (heightMax - 1)) {
				heightAdd = 0;
			}

		// Snow
		if (!Contains(block)) {
			GetBlocks().put(block, new BlockRestoreData(block, 78, (byte) Math.max(0, heightAdd - 1), com.houzicore.shared.common.util.IdUtil.getTypeId(block),
					com.houzicore.shared.common.util.IdUtil.getData(block), expireTime, meltDelay));
		} else {
			GetData(block).update(78, heightAdd, expireTime, meltDelay);
		}
	}

}
