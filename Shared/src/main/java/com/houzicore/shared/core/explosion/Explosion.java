package com.houzicore.shared.core.explosion;

import java.util.AbstractMap;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.UUID;

import com.houzicore.shared.MiniPlugin;
import com.houzicore.shared.common.util.UtilPlayer;
import com.houzicore.shared.updater.event.UpdateEvent;
import com.houzicore.shared.updater.UpdateType;
import com.houzicore.shared.core.blockrestore.BlockRestore;
import com.houzicore.shared.common.util.UtilAction;
import com.houzicore.shared.common.util.UtilAlg;
import com.houzicore.shared.common.util.UtilBlock;
import com.houzicore.shared.common.util.UtilMath;

import org.bukkit.Particle;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.FallingBlock;
import org.bukkit.entity.Player;
import org.bukkit.entity.TNTPrimed;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.entity.ExplosionPrimeEvent;
import org.bukkit.event.entity.ItemSpawnEvent;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.util.Vector;

public class Explosion extends MiniPlugin {
	private boolean _regenerateGround = false;
	private boolean _temporaryDebris = true;
	private boolean _enableDebris = false;
	private boolean _tntSpread = true;
	private boolean _liquidDamage = true;
	private final HashSet<FallingBlock> _explosionBlocks = new HashSet<>();

	private final BlockRestore _blockRestore;

	public Explosion(JavaPlugin plugin, BlockRestore blockRestore) {
		super("Block Restore", plugin);

		_blockRestore = blockRestore;
	}

	public void BlockExplosion(Collection<Block> blockSet, Location mid, boolean onlyAbove) {
		BlockExplosion(blockSet, mid, onlyAbove, true);
	}

	public void BlockExplosion(Collection<Block> blockSet, Location mid, boolean onlyAbove, boolean removeBlock) {
		if (blockSet.isEmpty())
			return;

		// Save
		final HashMap<Block, Entry<Integer, Byte>> blocks = new HashMap<>();

		for (final Block cur : blockSet) {
			if (com.houzicore.shared.common.util.IdUtil.getTypeId(cur) == 0) {
				continue;
			}

			if (onlyAbove && cur.getY() < mid.getY()) {
				continue;
			}

			blocks.put(cur, new AbstractMap.SimpleEntry<>(com.houzicore.shared.common.util.IdUtil.getTypeId(cur),
					com.houzicore.shared.common.util.IdUtil.getData(cur)));

			if (removeBlock) {
				if (cur.getType() == Material.STONE_BRICKS) {
					cur.setType(Material.CRACKED_STONE_BRICKS);
				} else if (cur.getType().name().contains("CHEST") || cur.getType().name().contains("ORE")) {
					cur.breakNaturally();
				} else {
					cur.setType(Material.AIR);
				}
			}
		}

		// DELAY
		final Location fLoc = mid;
		_plugin.getServer().getScheduler().runTaskLater(_plugin, new Runnable() {
			@Override
			public void run() {
				// Launch
				for (final Block cur : blocks.keySet()) {
					if (blocks.get(cur).getKey() == 98)
						if (blocks.get(cur).getValue() == 0 || blocks.get(cur).getValue() == 3) {
							continue;
						}

					final double chance = 0.2 + (double) _explosionBlocks.size() / (double) 80;
					if (Math.random() > Math.min(0.98, chance)) {
						final FallingBlock fall = cur.getWorld().spawnFallingBlock(cur.getLocation().add(0.5, 0.5, 0.5),
							com.houzicore.shared.common.util.IdUtil.getBlockData(blocks.get(cur).getKey(), blocks.get(cur).getValue()));

						final Vector vec = UtilAlg.getTrajectory(fLoc, fall.getLocation());
						if (vec.getY() < 0) {
							vec.setY(vec.getY() * -1);
						}

						UtilAction.velocity(fall, vec, 0.5 + 0.25 * Math.random(), false, 0, 0.4 + 0.20 * Math.random(),
								10, false);

						_explosionBlocks.add(fall);
					}

				}
			}
		}, 1);
	}

	@EventHandler(priority = EventPriority.LOW)
	public void ExplosionBlocks(EntityExplodeEvent event) {
		if (event.getEntity() == null) {
			event.blockList().clear();
		}
	}

	@EventHandler
	public void ExplosionBlockUpdate(UpdateEvent event) {
		if (event.getType() != UpdateType.TICK)
			return;

		// Clean Archived Games
		final Iterator<FallingBlock> fallingIterator = _explosionBlocks.iterator();

		while (fallingIterator.hasNext()) {
			final FallingBlock cur = fallingIterator.next();

			if (cur.isDead() || !cur.isValid() || cur.getTicksLived() > 400 || !cur.getWorld()
					.isChunkLoaded(cur.getLocation().getBlockX() >> 4, cur.getLocation().getBlockZ() >> 4)) {
				fallingIterator.remove();

				// Expire
				if (cur.getTicksLived() > 400 || !cur.getWorld().isChunkLoaded(cur.getLocation().getBlockX() >> 4,
						cur.getLocation().getBlockZ() >> 4)) {
					cur.remove();
					return;
				}

				final Block block = cur.getLocation().getBlock();
				com.houzicore.shared.common.util.IdUtil.setTypeIdAndData(block, 0, (byte) 0, true);

				// Block Replace
				if (_enableDebris) {
					if (_temporaryDebris) {
						_blockRestore.Add(block, com.houzicore.shared.common.util.IdUtil.getTypeId(cur.getMaterial()), (byte) 0, 10000);
					} else {
						com.houzicore.shared.common.util.IdUtil.setTypeIdAndData(block, cur.getMaterial(), cur.getBlockData(), true);
					}
				} else {
					cur.getWorld().spawnParticle(
							Particle.BLOCK, 
							block.getLocation().add(0.5, 0.5, 0.5), 
							10, 0.2, 0.2, 0.2, 0.0, 
							cur.getBlockData());
				}

				cur.remove();
			}
		}
	}

	@EventHandler
	public void ExplosionEntity(EntityExplodeEvent event) {
		if (event.isCancelled())
			return;

		try {
			if (event.getEntityType() == EntityType.CREEPER) {
				event.blockList().clear();
			}

			if (event.getEntityType() == EntityType.WITHER_SKULL) {
				event.blockList().clear();
			}
		} catch (final Exception e) {
			// Nothing
		}

		if (event.blockList().isEmpty())
			return;

		// This metadata is used to identify the owner of the explosion for use in other
		// plugins
		Player owner = null;
		final Entity entity = event.getEntity();
		if (entity.hasMetadata("owner")) {
			final FixedMetadataValue ownerData = (FixedMetadataValue) entity.getMetadata("owner").get(0);
			final UUID ownerUUID = (UUID) ownerData.value();

			owner = UtilPlayer.searchExact(ownerUUID);
		}

		// Event for Block awareness
		final ExplosionEvent explodeEvent = new ExplosionEvent(event.blockList(), owner);
		_plugin.getServer().getPluginManager().callEvent(explodeEvent);

		event.setYield(0f);

		// Save
		final HashMap<Block, Entry<Integer, Byte>> blocks = new HashMap<>();

		for (final Block cur : event.blockList()) {
			if (com.houzicore.shared.common.util.IdUtil.getTypeId(cur) == 0 || cur.isLiquid()) {
				continue;
			}

			if (cur.getType() == Material.CHEST || cur.getType() == Material.IRON_ORE
					|| cur.getType() == Material.COAL_ORE || cur.getType() == Material.GOLD_ORE
					|| cur.getType() == Material.DIAMOND_ORE) {
				cur.breakNaturally();
				continue;
			}

			blocks.put(cur, new AbstractMap.SimpleEntry<>(com.houzicore.shared.common.util.IdUtil.getTypeId(cur),
					com.houzicore.shared.common.util.IdUtil.getData(cur)));

			if (!_regenerateGround) {
				byte data = com.houzicore.shared.common.util.IdUtil.getData(cur);
				if (com.houzicore.shared.common.util.IdUtil.getTypeId(cur) != 98 || data != 0 && data != 3) {
					cur.setType(Material.AIR);
				}
			}

			else {
				final int heightDiff = cur.getLocation().getBlockY() - event.getEntity().getLocation().getBlockY();
					_blockRestore.Add(cur, 0, (byte) 0, (long) (20000 + heightDiff * 3000 + Math.random() * 2900));
			}

		}

		event.blockList().clear();

		// DELAY

		final Entity fEnt = event.getEntity();
		final Location fLoc = event.getLocation();
		_plugin.getServer().getScheduler().runTaskLater(_plugin, new Runnable() {
			@Override
			public void run() {
				// Launch
				for (final Block cur : blocks.keySet()) {
					if (blocks.get(cur).getKey() == 98)
						if (blocks.get(cur).getValue() == 0 || blocks.get(cur).getValue() == 3) {
							continue;
						}

					// TNT
					if (_tntSpread && blocks.get(cur).getKey() == 46) {
						final TNTPrimed ent = cur.getWorld().spawn(cur.getLocation().add(0.5, 0.5, 0.5),
								TNTPrimed.class);
						final Vector vec = UtilAlg.getTrajectory(fEnt, ent);
						if (vec.getY() < 0) {
							vec.setY(vec.getY() * -1);
						}

						UtilAction.velocity(ent, vec, 1, false, 0, 0.6, 10, false);

						ent.setFuseTicks(10);
					}
					// Other
					else {
						// XXX ANTILAG
						final double chance = 0.85 + (double) _explosionBlocks.size() / (double) 500;
						if (Math.random() > Math.min(0.975, chance)) {
							final FallingBlock fall = cur.getWorld().spawnFallingBlock(
									cur.getLocation().add(0.5, 0.5, 0.5),
									com.houzicore.shared.common.util.IdUtil.getBlockData(blocks.get(cur).getKey(),
											blocks.get(cur).getValue()));

							final Vector vec = UtilAlg.getTrajectory(fEnt, fall);
							if (vec.getY() < 0) {
								vec.setY(vec.getY() * -1);
							}

							UtilAction.velocity(fall, vec, 0.5 + 0.25 * Math.random(), false, 0,
									0.4 + 0.20 * Math.random(), 10, false);

							_explosionBlocks.add(fall);
						}
					}
				}

				// Items
				/**
				 * for (Item item : event.getEntity().getWorld().getEntitiesByClass(Item.class))
				 * if (UtilMath.offset(item, event.getEntity()) < 5) { Vector vec =
				 * UtilAlg.getTrajectory(event.getEntity(), item); if (vec.getY() < 0)
				 * vec.setY(vec.getY() * -1);
				 * 
				 * UtilAction.velocity(item, vec, 1, false, 0, 0.6, 10, false); }
				 **/

				// Crack
				for (final Block cur : UtilBlock.getInRadius(fLoc, 4d).keySet()) {
					if (com.houzicore.shared.common.util.IdUtil.getTypeId(cur) == 98) {
						byte data = com.houzicore.shared.common.util.IdUtil.getData(cur);
						if (data == 0 || data == 3) {
							com.houzicore.shared.common.util.IdUtil.setTypeIdAndData(cur, 98, (byte) 2, true);
						}
					}
				}
			}
		}, 1);
	}

	@EventHandler
	public void ExplosionItemSpawn(ItemSpawnEvent event) {
		for (final FallingBlock block : _explosionBlocks)
			if (UtilMath.offset(event.getEntity().getLocation(), block.getLocation()) < 1) {
				event.setCancelled(true);
			}
	}

	@EventHandler
	public void ExplosionPrime(ExplosionPrimeEvent event) {
		if (event.getRadius() >= 5)
			return;

		if (_liquidDamage) {
			for (final Block block : UtilBlock.getInRadius(event.getEntity().getLocation(), event.getRadius()).keySet())
				if (block.isLiquid()) {
					//block.setTypeId(0);
				}
		}
	}

	public HashSet<FallingBlock> GetExplosionBlocks() {
		return _explosionBlocks;
	}

	public void SetDebris(boolean value) {
		_enableDebris = value;
	}

	public void SetLiquidDamage(boolean value) {
		_liquidDamage = value;
	}

	public void SetRegenerate(boolean regenerate) {
		_regenerateGround = regenerate;
	}

	public void SetTemporaryDebris(boolean value) {
		_temporaryDebris = value;
	}

	public void SetTNTSpread(boolean value) {
		_tntSpread = value;
	}

	@EventHandler
	public void onChunkUnload(org.bukkit.event.world.ChunkUnloadEvent event) {
		Iterator<FallingBlock> it = _explosionBlocks.iterator();
		while (it.hasNext()) {
			FallingBlock block = it.next();
			if (block.getLocation().getChunk().equals(event.getChunk())) {
				block.remove();
				it.remove();
			}
		}
	}
}
