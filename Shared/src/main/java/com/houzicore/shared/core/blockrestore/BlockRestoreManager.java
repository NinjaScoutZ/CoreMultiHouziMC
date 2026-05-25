package com.houzicore.shared.core.blockrestore;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.data.BlockData;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.BlockBreakEvent;

import com.houzicore.shared.MiniPlugin;
import com.houzicore.shared.updater.UpdateType;
import com.houzicore.shared.updater.event.UpdateEvent;

public class BlockRestoreManager extends MiniPlugin {
	private Map<Block, RestoredBlock> _blocks = new HashMap<>();

	private static BlockRestoreManager _instance;

	public BlockRestoreManager(org.bukkit.plugin.java.JavaPlugin plugin) {
		super("Block Restore", plugin);
		_instance = this;
	}

	public static BlockRestoreManager getInstance() {
		return _instance;
	}

	public void add(Block block, Material newType, long expireTimeMs) {
		if (!_blocks.containsKey(block)) {
			_blocks.put(block, new RestoredBlock(block.getState(), System.currentTimeMillis() + expireTimeMs));
		} else {
			_blocks.get(block).expiration = System.currentTimeMillis() + expireTimeMs;
		}
		block.setType(newType, false);
	}

	public void add(Block block, BlockData newData, long expireTimeMs) {
		if (!_blocks.containsKey(block)) {
			_blocks.put(block, new RestoredBlock(block.getState(), System.currentTimeMillis() + expireTimeMs));
		} else {
			_blocks.get(block).expiration = System.currentTimeMillis() + expireTimeMs;
		}
		block.setBlockData(newData, false);
	}

	@EventHandler(priority = EventPriority.LOW)
	public void onBreak(BlockBreakEvent event) {
		if (_blocks.containsKey(event.getBlock())) {
			event.setCancelled(true);
			restore(event.getBlock());
		}
	}

	@EventHandler
	public void expireBlocks(UpdateEvent event) {
		if (event.getType() != UpdateType.TICK)
			return;

		long now = System.currentTimeMillis();
		Iterator<Entry<Block, RestoredBlock>> it = _blocks.entrySet().iterator();
		while (it.hasNext()) {
			Entry<Block, RestoredBlock> entry = it.next();
			if (now >= entry.getValue().expiration) {
				entry.getValue().originalState.update(true, false);
				it.remove();
			}
		}
	}

	public void restore(Block block) {
		RestoredBlock data = _blocks.remove(block);
		if (data != null) {
			data.originalState.update(true, false);
		}
	}

	@Override
	public void disable() {
		for (RestoredBlock mapped : _blocks.values()) {
			mapped.originalState.update(true, false);
		}
		_blocks.clear();
	}

	private static class RestoredBlock {
		BlockState originalState;
		long expiration;

		RestoredBlock(BlockState state, long exp) {
			this.originalState = state;
			this.expiration = exp;
		}
	}
}
