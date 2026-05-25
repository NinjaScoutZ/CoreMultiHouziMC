package com.houzicore.shared.common.util;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

// NMS 1.7.10 imports removed for 1.21.1 compatibility
import org.bukkit.block.Block;
import org.bukkit.block.Container;
import org.bukkit.block.data.BlockData;

import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.world.WorldUnloadEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.util.Vector;

public class MapUtil
{
	public static void ReplaceOreInChunk(org.bukkit.Chunk chunk, Material replacee, Material replacer)
	{
		// NMS method stubbed for 1.21.1
		for (int x = 0; x < 16; x++)
		{
			for (int z = 0; z < 16; z++)
			{
				for (int y = 0; y < chunk.getWorld().getMaxHeight(); y++)
				{
					if (chunk.getBlock(x, y, z).getType() == replacee)
					{
						chunk.getBlock(x, y, z).setType(replacer, false);
					}
				}
			}
		}
	}

	public static void QuickChangeBlockAt(Location location, Material setTo)
	{
		QuickChangeBlockAt(location.getWorld(), location.getBlockX(), location.getBlockY(), location.getBlockZ(), setTo);
	}

	public static void QuickChangeBlockAt(Location location, Material setTo, byte data)
	{
		QuickChangeBlockAt(location.getWorld(), location.getBlockX(), location.getBlockY(), location.getBlockZ(), setTo,
				data);
	}

	public static void QuickChangeBlockAt(Location location, int id, byte data)
	{
		QuickChangeBlockAt(location.getWorld(), location.getBlockX(), location.getBlockY(), location.getBlockZ(), id,
				data);
	}

	public static void QuickChangeBlockAt(World world, int x, int y, int z, Material setTo)
	{
		QuickChangeBlockAt(world, x, y, z, setTo, 0);
	}

	public static void QuickChangeBlockAt(World world, int x, int y, int z, Material setTo, int data)
	{
		Material material = asPlaceableBlock(setTo);
		int legacyId = IdUtil.getTypeId(material);
		if (legacyId >= 0)
		{
			world.getBlockAt(x, y, z).setBlockData(IdUtil.getBlockData(legacyId, (byte) data), false);
		}
		else
		{
			world.getBlockAt(x, y, z).setType(material, false);
		}
	}

	public static void QuickChangeBlockAt(World world, int x, int y, int z, int id, int data)
	{
		IdUtil.setTypeIdAndData(world.getBlockAt(x, y, z), id, (byte) data, false);
	}

	public static int GetHighestBlockInCircleAt(World world, int bx, int bz, int radius)
	{
		int count = 0;
		int totalHeight = 0;

		final double invRadiusX = 1 / radius;
		final double invRadiusZ = 1 / radius;

		final int ceilRadiusX = (int) Math.ceil(radius);
		final int ceilRadiusZ = (int) Math.ceil(radius);

		double nextXn = 0;
		forX: for (int x = 0; x <= ceilRadiusX; ++x)
		{
			final double xn = nextXn;
			nextXn = (x + 1) * invRadiusX;
			double nextZn = 0;
			forZ: for (int z = 0; z <= ceilRadiusZ; ++z)
			{
				final double zn = nextZn;
				nextZn = (z + 1) * invRadiusZ;

				double distanceSq = xn * xn + zn * zn;
				if (distanceSq > 1)
				{
					if (z == 0)
					{
						break forX;
					}
					break forZ;
				}

				totalHeight += world.getHighestBlockAt(bx + x, bz + z).getY();
				count++;
			}
		}

		return totalHeight / count;
	}

	public static void ChunkBlockChange(Location location, int id, byte data, boolean notifyPlayers)
	{
		ChunkBlockChange(location.getWorld(), location.getBlockX(), location.getBlockY(), location.getBlockZ(), id,
				data, notifyPlayers);
	}

	public static void ChunkBlockChange(World world, int x, int y, int z, int id, byte data, boolean notifyPlayers)
	{
		IdUtil.setTypeIdAndData(world.getBlockAt(x, y, z), id, data, notifyPlayers);
	}
	
	public static void ChunkBlockSet(World world, int x, int y, int z, int id, byte data, boolean notifyPlayers)
	{
		IdUtil.setTypeIdAndData(world.getBlockAt(x, y, z), id, data, notifyPlayers);
	}

	private static Material asPlaceableBlock(Material material)
	{
		if (material == null)
			return Material.AIR;

		if (material == Material.WATER_BUCKET)
			return Material.WATER;

		if (material == Material.LAVA_BUCKET)
			return Material.LAVA;

		if (!material.isBlock())
			return Material.AIR;

		return material;
	}

	// changeChunkBlock removed - using Bukkit API instead

	public static void SendChunkForPlayer(org.bukkit.Chunk chunk, Player player)
	{
		SendChunkForPlayer(chunk.getX(), chunk.getZ(), player);
	}

	@SuppressWarnings("unchecked")
	public static void SendChunkForPlayer(int x, int z, Player player)
	{
		// Modern Bukkit does not require manual chunk coord pair queueing for most tasks
		// player.getWorld().getChunkAt(x, z); 
	}

	@SuppressWarnings("unchecked")
	public static void SendMultiBlockForPlayer(int x, int z, short[] dirtyBlocks, int dirtyCount, World world,
			Player player)
	{
		// NMS PacketPlayOutMultiBlockChange removed for 1.21.1
		// world.getChunkAt(x, z).getBlock(0, 0, 0).getState().update();
	}

	public static void UnloadWorld(JavaPlugin plugin, World world)
	{
		UnloadWorld(plugin, world, false);
	}

	public static void UnloadWorld(JavaPlugin plugin, World world, boolean save)
	{
		if (save)
		{
			world.save();
		}

		world.setAutoSave(save);
		Bukkit.unloadWorld(world, save);
	}

	@SuppressWarnings({ "rawtypes" })
	public static boolean ClearWorldReferences(String worldName)
	{
		// NMS RegionFileCache stubbed for 1.21.1
		return true;
	}

	public static <K, V extends Comparable<? super V>> java.util.List<java.util.Map.Entry<K, V>> sortByValue(Map<K, V> map) {
		java.util.List<java.util.Map.Entry<K, V>> list = new java.util.ArrayList<>(map.entrySet());
		list.sort(java.util.Map.Entry.comparingByValue());
		java.util.Collections.reverse(list); // Descending order
		return list;
	}

	public static <K, V extends Comparable<? super V>> K getMax(Map<K, V> map) {
		java.util.Map.Entry<K, V> maxEntry = null;
		for (java.util.Map.Entry<K, V> entry : map.entrySet()) {
			if (maxEntry == null || entry.getValue().compareTo(maxEntry.getValue()) > 0) {
				maxEntry = entry;
			}
		}
		return maxEntry != null ? maxEntry.getKey() : null;
	}
}
