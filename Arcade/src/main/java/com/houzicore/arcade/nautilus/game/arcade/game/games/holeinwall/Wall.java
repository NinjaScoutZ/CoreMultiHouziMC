package com.houzicore.arcade.nautilus.game.arcade.game.games.holeinwall;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.UUID;

import com.houzicore.shared.common.util.UtilAlg;
import com.houzicore.shared.common.util.UtilEnt;
import com.houzicore.shared.common.util.UtilMath;
import com.houzicore.shared.common.util.UtilPlayer;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;


public class Wall
{
	private class WallEntry
	{
		public WallEntry(byte data)
		{
			this.data = data;
		}

		int chicken = UtilEnt.getNewEntityId(false);
		int block = UtilEnt.getNewEntityId(false);
		byte data;
		// In 1.21.1, we should use Display entities or actual FallingBlocks for walls.
		// For now, these IDs are placeholders since we removed the packet logic.

	}

	private HashMap<Location, WallEntry> _wallEntities = new HashMap<Location, WallEntry>();
	private Vector _catchup = new Vector();
	private double _minX = -0.501;
	private double _maxX = 0.501;
	private double _minZ = -0.501;
	private double _maxZ = 0.501;
	private ArrayList<UUID> _knockedPlayers = new ArrayList<UUID>();

	public Wall(Location corner, Collection<Entry<Integer, Integer>> blocks, boolean wallXWise, double wallWidth)
	{
		int i = UtilMath.r(16);

		for (Entry<Integer, Integer> entry : blocks)
		{
			Location loc = corner.clone().add(wallXWise ? entry.getKey() - (wallWidth / 2) : 0, entry.getValue(),
					wallXWise ? 0 : entry.getKey() - (wallWidth / 2));

			_wallEntities.put(loc, new WallEntry((byte) ((i + (entry.getKey() / 3D)) % 16)));
		}

		if (!wallXWise)
		{
			_minX = -0.05;
			_maxX = 0.05;
		}
		else
		{
			_minZ = -0.05;
			_maxZ = 0.05;
		}
	}

	public ArrayList<UUID> getKnockedPlayers()
	{
		return _knockedPlayers;
	}

	public Location getLocation()
	{
		return _wallEntities.keySet().iterator().next();
	}

	public boolean hasInterception(Location playerLocation, Vector vec)
	{
		for (Location l : _wallEntities.keySet())
		{
			if (hasInterception(playerLocation, l, l.clone().subtract(vec)))
			{
				return true;
			}
		}

		return false;
	}

	public boolean hasInterception(Location pointA, Location pointB)
	{
		Location loc1 = pointA.clone();
		Location loc2 = pointB.clone();

		loc1.setX(Math.min(pointA.getX(), pointB.getX()) - .3);
		loc1.setY(Math.min(pointA.getY(), pointB.getY()));
		loc1.setZ(Math.min(pointA.getZ(), pointB.getZ()) - .3);
		loc2.setX(Math.max(pointA.getX(), pointB.getX()) + .3);
		loc2.setY(Math.max(pointA.getY(), pointB.getY()));
		loc2.setZ(Math.max(pointA.getZ(), pointB.getZ()) + .3);

		double[] box = new double[]
			{
					(loc2.getX() - loc1.getX()) / 2, (loc2.getY() - loc1.getY()) / 2, (loc2.getZ() - loc1.getZ()) / 2
			};
		double[] bBox = new double[]
			{
					_maxX, 1.2D, _maxZ
			};

		Location mid = loc1.add(loc2).multiply(0.5);

		for (Location loc : _wallEntities.keySet())
		{
			if (checkCollision(mid, loc.clone().add(0, -0.9, 0), box, bBox))
			{
				return true;
			}
		}

		return false;
	}

	/**
	 * Used to get the bounding box of a block, pointA and pointB is for when the wall moves to expand the box.
	 */
	private Location[] getBoundingBox(Location pointA, Location pointB)
	{
		Location loc1 = pointA.clone();
		Location loc2 = pointB.clone();

		loc1.setX(Math.min(pointA.getX(), pointB.getX()));
		loc1.setZ(Math.min(pointA.getZ(), pointB.getZ()));

		loc2.setX(Math.max(pointA.getX(), pointB.getX()));
		loc2.setZ(Math.max(pointA.getZ(), pointB.getZ()));

		loc1.add(_minX, -0.45 - 1.65 + 0.5, _minZ);
		loc2.add(_maxX, 0.3 + 0.5, _maxZ);

		return new Location[]
			{
					loc1, loc2
			};
	}

	private boolean hasInterception(Location pLoc, Location fLoc, Location sLoc)
	{
		Location[] loc = getBoundingBox(fLoc, sLoc);

		return UtilAlg.inBoundingBox(pLoc, loc[0], loc[1]);
	}

	public void spawnWall(Player player)
	{
		// Packet-based wall spawning removed for modernization.
		// A full implementation would use BlockDisplay entities (1.19.4+) or similar.
	}


	public void moveWall(Vector vector)
	{
		for (Entry<Location, WallEntry> entry : _wallEntities.entrySet())
		{
			entry.getKey().add(vector);
		}
		// Packet-based wall movement removed for modernization.
	}


	private boolean checkCollision(Location loc1, Location loc2, double[] box1, double[] box2)
	{
		// check the X axis
		if (Math.abs(loc1.getX() - loc2.getX()) < box1[0] + box2[0])
		{
			// check the Y axis
			if (Math.abs(loc1.getY() - loc2.getY()) < box1[1] + box2[1])
			{
				// check the Z axis
				if (Math.abs(loc1.getZ() - loc2.getZ()) < box1[2] + box2[2])
				{
					return true;
				}
			}
		}

		return false;
	}

	public void destroyWall()
	{
		// Packet-based wall destruction removed for modernization.
	}

}
