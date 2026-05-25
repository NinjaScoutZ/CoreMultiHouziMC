package com.houzicore.lobby.hub.modules.nonstop;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.bukkit.Location;

public class NonstopParkourData {

    public final String name;
    public final Location start;
    public final Location finish;

    // Bounding box — set from config
    public final double minX, maxX, minY, maxY, minZ, maxZ;

    // Ordered checkpoints (sorted by Y ascending so lower = earlier)
    private final List<Location> checkpoints;

    public NonstopParkourData(String name, Location start, Location finish,
                              double minX, double maxX, double minY, double maxY, double minZ, double maxZ,
                              List<Location> checkpoints) {
        this(name, start, finish, minX, maxX, minY, maxY, minZ, maxZ, checkpoints, true);
    }

    public NonstopParkourData(String name, Location start, Location finish,
                              double minX, double maxX, double minY, double maxY, double minZ, double maxZ,
                              List<Location> checkpoints, boolean sortByY) {
        this.name   = name;
        this.start  = start;
        this.finish = finish;
        this.minX = minX; this.maxX = maxX;
        this.minY = minY; this.maxY = maxY;
        this.minZ = minZ; this.maxZ = maxZ;

        this.checkpoints = new ArrayList<>(checkpoints != null ? checkpoints : Collections.emptyList());
        if (sortByY) {
            this.checkpoints.sort(Comparator.comparingDouble(Location::getY));
        }
    }

    /** Legacy compat — no checkpoints */
    public NonstopParkourData(String name, Location start, Location finish, int ignored,
                              double minX, double maxX, double minY, double maxY, double minZ, double maxZ) {
        this(name, start, finish, minX, maxX, minY, maxY, minZ, maxZ, Collections.emptyList());
    }

    public boolean inBoundary(Location loc) {
        return loc.getX() >= minX && loc.getX() <= maxX
            && loc.getY() >= minY && loc.getY() <= maxY
            && loc.getZ() >= minZ && loc.getZ() <= maxZ;
    }

    /**
     * Returns the highest checkpoint whose Y is at or below the player's Y.
     * If no checkpoint qualifies, returns start.
     */
    public Location getLastCheckpoint(Location playerLoc) {
        Location best = start;
        for (Location cp : checkpoints) {
            if (cp.getY() <= playerLoc.getY() + 2.0) {
                best = cp;
            }
        }
        return best;
    }

    /**
     * Returns the ordinal index (1-based) of the checkpoint the player is near.
     * Returns -1 if not near any checkpoint.
     */
    public int getCheckpointIndex(Location playerLoc, double radius) {
        for (int i = 0; i < checkpoints.size(); i++) {
            if (checkpoints.get(i).distanceSquared(playerLoc) <= radius * radius) {
                return i + 1;
            }
        }
        return -1;
    }

    public int getCheckpointCount() {
        return checkpoints.size();
    }

    public List<Location> getCheckpoints() {
        return Collections.unmodifiableList(checkpoints);
    }
}
