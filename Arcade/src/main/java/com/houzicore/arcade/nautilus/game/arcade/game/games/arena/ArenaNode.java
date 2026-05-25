package com.houzicore.arcade.nautilus.game.arcade.game.games.arena;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;

import com.houzicore.shared.common.util.UtilAlg;

import java.util.ArrayList;
import java.util.HashMap;

public class ArenaNode {
	private final ArenaGame host;
	private final ArenaType type;
	private final Location mid;
	private final ArrayList<Location> spawns;

	private ArenaNode parent;
	private final ArenaNode[] children = new ArenaNode[2];
	private boolean isUsed;

	private final ArrayList<Location> doorBlocks;
	private boolean isOpenDoor;
	private boolean doBye;

	private final ArrayList<Player> pastPlayers;
	private ArenaState state;
	private long stateTime;

	public ArenaNode(ArenaGame host, Location mid, ArenaType type) {
		this.host = host;
		this.mid = mid;
		this.type = type;
		this.spawns = new ArrayList<>();
		this.doorBlocks = new ArrayList<>();
		this.pastPlayers = new ArrayList<>();
		this.state = ArenaState.EMPTY;
		this.stateTime = System.currentTimeMillis();

		setupSpawns();
	}

	private void setupSpawns() {
		ArrayList<Location> possible = new ArrayList<>(host.WorldData.GetDataLocs("BLACK"));
		mid.setY(UtilAlg.findClosest(mid, possible).getY());

		spawns.add(correctFace(UtilAlg.findClosest(mid, possible)));
		possible.remove(spawns.get(0));
		spawns.add(correctFace(UtilAlg.findClosest(mid, possible)));
	}

	private Location correctFace(Location l) {
		l.setPitch(UtilAlg.GetPitch(UtilAlg.getTrajectory(l, mid)));
		l.setYaw(UtilAlg.GetYaw(UtilAlg.getTrajectory(l, mid)));
		return l;
	}

	public void setChild(int index, ArenaNode child) {
		this.children[index] = child;
		if (child != null) child.parent = this;
	}

	public int getCapacity() {
		int cap = children.length;
		for (ArenaNode child : children) {
			if (child != null && child.isUsed()) cap--;
		}
		return cap;
	}

	public ArrayList<Location> capacitySpawns() {
		ArrayList<Location> ret = new ArrayList<>();
		if (getCapacity() == 0) return ret;
		if (getCapacity() == 1) {
			ret.add(spawns.get(0));
			return ret;
		}
		if (getCapacity() == 2) {
			ret.add(spawns.get(0));
			ret.add(spawns.get(1));
			return ret;
		}
		return ret;
	}

	public void getUsageMap(HashMap<ArenaNode, Integer> used) {
		if (isUsed()) used.put(this, getCapacity());
		for (ArenaNode child : children) {
			if (child != null) child.getUsageMap(used);
		}
	}

	public ArenaGame getHost() { return host; }
	public ArenaType getType() { return type; }
	public Location getMid() { return mid; }
	public ArrayList<Location> getSpawns() { return spawns; }
	public ArenaNode getParent() { return parent; }
	public ArenaNode getChildAt(int index) { return children[index]; }
	public ArenaNode[] getChildren() { return children; }
	public boolean isUsed() { return isUsed; }
	public void setUsed(boolean used) { isUsed = used; }
	public boolean isDoBye() { return doBye; }
	public void setDoBye(boolean doBye) { this.doBye = doBye; }
	public ArrayList<Player> getPastPlayers() { return pastPlayers; }
	public ArenaState getState() { return state; }
	public void setState(ArenaState state) { this.state = state; }
	public long getStateTime() { return stateTime; }
	public void setStateTime(long stateTime) { this.stateTime = stateTime; }
}
