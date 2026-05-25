package com.houzicore.arcade.nautilus.game.arcade.game.games.skywars.data;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;

import com.houzicore.shared.common.util.C;
import com.houzicore.shared.common.util.UtilMath;
import com.houzicore.shared.common.util.UtilParticle;
import com.houzicore.shared.common.util.UtilParticle.ParticleType;
import com.houzicore.shared.common.util.UtilParticle.ViewDist;
import com.houzicore.shared.common.util.UtilServer;
import com.houzicore.shared.common.util.UtilTime;
import com.houzicore.shared.core.hologram.Hologram;
import com.houzicore.arcade.nautilus.game.arcade.game.games.skywars.Skywars;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Ore Node — a cluster of blocks where 20-30% become ore and the rest
 * remain as stone, creating a natural vein appearance.
 *
 * When a player mines an ore block, only that single block depletes.
 * The entire node regenerates together after the cooldown.
 */
public class OreNode
{
	public enum OreState
	{
		READY,
		DEPLETED
	}

	private Skywars _host;

	/** The center of the ore node cluster. */
	private Location _center;

	/** All blocks that belong to this cluster. */
	private List<Location> _clusterBlocks = new ArrayList<>();

	/** Which blocks in the cluster are currently ore (vs stone). */
	private List<Location> _oreBlocks = new ArrayList<>();

	private Material _oreType;
	private OreState _state;

	private long _depletedTime;
	private long _regenTime = 45000; // 45 seconds

	/** How many ore blocks have been mined in the current cycle. */
	private int _minedCount = 0;

	private Hologram _hologram;

	/** Fraction of the cluster that becomes ore (20-30%). */
	private static final double ORE_FRACTION_MIN = 0.20;
	private static final double ORE_FRACTION_MAX = 0.30;
	private static final long HOLOGRAM_UPDATE_INTERVAL = 1000;

	private long _lastHologramUpdate;

	public OreNode(Skywars host, Location loc)
	{
		_host = host;
		_center = loc;
		_state = OreState.DEPLETED;
		_oreType = Material.STONE;
		_depletedTime = System.currentTimeMillis();

		// Build the cluster: scan a 3x3x3 cube around the center
		// Only include blocks that are currently stone-like (part of the terrain)
		buildCluster();

		// Set all cluster blocks to stone initially
		for (Location cLoc : _clusterBlocks)
		{
			cLoc.getBlock().setType(Material.STONE);
		}

		_hologram = new Hologram(_host.Manager.getHologramManager(), _center.clone().add(0.5, 2.3, 0.5), getHologramLines());
		_hologram.start();
		_lastHologramUpdate = System.currentTimeMillis();
	}

	/**
	 * Scan a 3x3x3 area around the center for blocks that can be
	 * part of the ore cluster. Only solid, non-air blocks qualify.
	 */
	private void buildCluster()
	{
		_clusterBlocks.clear();

		for (int dx = -1; dx <= 1; dx++)
		{
			for (int dy = -1; dy <= 1; dy++)
			{
				for (int dz = -1; dz <= 1; dz++)
				{
					Location bLoc = _center.clone().add(dx, dy, dz);
					Block b = bLoc.getBlock();

					// Include the center always, and neighbours if they're
					// solid terrain (stone, cobble, etc.) or air-adjacent
					if (dx == 0 && dy == 0 && dz == 0)
					{
						_clusterBlocks.add(bLoc);
					}
					else if (b.getType().isSolid() && !b.getType().name().contains("CHEST")
							&& !b.getType().name().contains("BELL"))
					{
						_clusterBlocks.add(bLoc);
					}
				}
			}
		}

		// If cluster scan only found the center, that's fine — it'll be a 1-block node
	}

	private String[] getHologramLines()
	{
		if (_state == OreState.DEPLETED)
		{
			long elapsed = System.currentTimeMillis() - _depletedTime;
			long remaining = _regenTime - elapsed;
			if (remaining < 0) remaining = 0;
			int seconds = (int) (remaining / 1000);
			String timeStr = "00:" + (seconds < 10 ? "0" + seconds : seconds);

			return new String[] {
				C.cRed + C.Bold + "DEPLETED NODE",
				C.cGray + "Regrowing in: " + C.cYellow + timeStr
			};
		}

		String oreName = "";
		String color = "";
		if (_oreType == Material.DIAMOND_ORE) { oreName = "Diamond Vein"; color = C.cAqua; }
		else if (_oreType == Material.GOLD_ORE) { oreName = "Gold Vein"; color = C.cYellow; }
		else if (_oreType == Material.IRON_ORE) { oreName = "Iron Vein"; color = C.cWhite; }
		else { oreName = "Coal Vein"; color = C.cDGray; }

		return new String[] {
			color + C.Bold + "♦ " + oreName + " ♦",
			C.cGray + "Harvestable: " + color + _oreBlocks.size() + C.cGray + " blocks",
			C.cYellow + "Mine to extract resources!"
		};
	}

	public boolean isReady()
	{
		return _state == OreState.READY;
	}

	/**
	 * Called when a player breaks a single ore block within this cluster.
	 * Only depletes that one block, not the entire node.
	 */
	public void depleteBlock(Location blockLoc)
	{
		_oreBlocks.remove(blockLoc);
		_minedCount++;

		// Set the mined block back to stone after 1 tick (let the break event finish)
		_host.Manager.getPlugin().getServer().getScheduler().runTaskLater(_host.Manager.getPlugin(), () -> {
			blockLoc.getBlock().setType(Material.STONE);
			UtilParticle.PlayParticle(ParticleType.LARGE_SMOKE, blockLoc.clone().add(0.5, 0.5, 0.5), 0.2f, 0.2f, 0.2f, 0f, 5, ViewDist.NORMAL, UtilServer.getPlayers());
		}, 1L);

		// If all ore blocks are mined, the node is fully depleted
		if (_oreBlocks.isEmpty())
		{
			_state = OreState.DEPLETED;
			_depletedTime = System.currentTimeMillis();
			_minedCount = 0;
		}

		updateHologram(true);
	}

	/**
	 * Legacy compatibility — deplete the entire node at once.
	 */
	public void deplete()
	{
		_state = OreState.DEPLETED;
		_oreType = Material.STONE;
		_depletedTime = System.currentTimeMillis();
		_minedCount = 0;
		_oreBlocks.clear();

		_host.Manager.getPlugin().getServer().getScheduler().runTaskLater(_host.Manager.getPlugin(), () -> {
			for (Location cLoc : _clusterBlocks)
			{
				cLoc.getBlock().setType(Material.STONE);
			}
			UtilParticle.PlayParticle(ParticleType.LARGE_SMOKE, _center.clone().add(0.5, 0.5, 0.5), 0.2f, 0.2f, 0.2f, 0f, 5, ViewDist.NORMAL, UtilServer.getPlayers());
		}, 1L);

		updateHologram(true);
	}

	public void update()
	{
		if (_state == OreState.DEPLETED)
		{
			long elapsed = System.currentTimeMillis() - _depletedTime;
			long remaining = _regenTime - elapsed;

			if (remaining <= 0)
			{
				// 20% chance to regenerate per check
				if (UtilMath.r(100) < 20)
				{
					regenerate();
				}
				else
				{
					// Failed roll, wait another 15 seconds before trying again
					_depletedTime = System.currentTimeMillis() - _regenTime + 15000;
				}
			}
		}

		updateHologram(false);
	}

	private void regenerate()
	{
		_state = OreState.READY;
		_oreBlocks.clear();
		_minedCount = 0;

		// Determine ore type based on game phase
		long gameTime = System.currentTimeMillis() - _host.GetStateTime();

		int r = UtilMath.r(100);
		if (gameTime < 120000) // Phase 1 (0-2 min): Coal
		{
			_oreType = Material.COAL_ORE;
		}
		else if (gameTime < 240000) // Phase 2 (2-4 min): Coal 60%, Iron 40%
		{
			if (r < 40) _oreType = Material.IRON_ORE;
			else _oreType = Material.COAL_ORE;
		}
		else if (gameTime < 360000) // Phase 3 (4-6 min): Coal 30%, Iron 50%, Gold 20%
		{
			if (r < 20) _oreType = Material.GOLD_ORE;
			else if (r < 70) _oreType = Material.IRON_ORE;
			else _oreType = Material.COAL_ORE;
		}
		else // Phase 4 (6+ min): Iron 40%, Gold 40%, Diamond 20%
		{
			if (r < 20) _oreType = Material.DIAMOND_ORE;
			else if (r < 60) _oreType = Material.GOLD_ORE;
			else _oreType = Material.IRON_ORE;
		}

		// Determine how many blocks become ore (20-30% of cluster)
		double fraction = ORE_FRACTION_MIN + Math.random() * (ORE_FRACTION_MAX - ORE_FRACTION_MIN);
		int oreCount = Math.max(1, (int) Math.round(_clusterBlocks.size() * fraction));

		// Shuffle cluster blocks and pick the first N as ore
		List<Location> shuffled = new ArrayList<>(_clusterBlocks);
		Collections.shuffle(shuffled);

		// First, ensure all cluster blocks are stone
		for (Location cLoc : _clusterBlocks)
		{
			cLoc.getBlock().setType(Material.STONE);
		}

		// Then set the selected blocks to ore
		for (int i = 0; i < oreCount && i < shuffled.size(); i++)
		{
			Location oreLoc = shuffled.get(i);
			oreLoc.getBlock().setType(_oreType);
			_oreBlocks.add(oreLoc);
		}

		_center.getWorld().playSound(_center, Sound.ENTITY_PLAYER_LEVELUP, 1.0f, 1.5f);
		UtilParticle.PlayParticle(ParticleType.HAPPY_VILLAGER, _center.clone().add(0.5, 0.5, 0.5), 0.4f, 0.4f, 0.4f, 0f, 10, ViewDist.NORMAL, UtilServer.getPlayers());

		updateHologram(true);
	}

	private void updateHologram(boolean force)
	{
		if (_hologram == null)
			return;

		if (!force && !UtilTime.elapsed(_lastHologramUpdate, HOLOGRAM_UPDATE_INTERVAL))
			return;

		_lastHologramUpdate = System.currentTimeMillis();
		_hologram.setText(getHologramLines());
	}

	/**
	 * Check if a block location belongs to this ore node's cluster.
	 */
	public boolean containsBlock(Location blockLoc)
	{
		for (Location cLoc : _clusterBlocks)
		{
			if (cLoc.getBlockX() == blockLoc.getBlockX()
					&& cLoc.getBlockY() == blockLoc.getBlockY()
					&& cLoc.getBlockZ() == blockLoc.getBlockZ())
			{
				return true;
			}
		}
		return false;
	}

	/**
	 * Check if a block location is currently an active ore block.
	 */
	public boolean isOreBlock(Location blockLoc)
	{
		for (Location oLoc : _oreBlocks)
		{
			if (oLoc.getBlockX() == blockLoc.getBlockX()
					&& oLoc.getBlockY() == blockLoc.getBlockY()
					&& oLoc.getBlockZ() == blockLoc.getBlockZ())
			{
				return true;
			}
		}
		return false;
	}

	public Location getLocation()
	{
		return _center;
	}

	public Material getType()
	{
		return _oreType;
	}

	public void cleanUp()
	{
		if (_hologram != null)
		{
			_hologram.stop();
		}
	}
}
