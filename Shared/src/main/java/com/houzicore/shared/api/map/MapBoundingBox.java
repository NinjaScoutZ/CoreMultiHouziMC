package com.houzicore.shared.api.map;

import java.util.Objects;

/**
 * Axis-aligned bounding box for a map's playable region.
 * All coordinates are in block-integer space.
 */
public final class MapBoundingBox {

    private final int minX;
    private final int minY;
    private final int minZ;
    private final int maxX;
    private final int maxY;
    private final int maxZ;

    public MapBoundingBox(int minX, int minY, int minZ, int maxX, int maxY, int maxZ) {
        this.minX = minX; this.minY = minY; this.minZ = minZ;
        this.maxX = maxX; this.maxY = maxY; this.maxZ = maxZ;
    }

    public int getMinX() { return minX; }
    public int getMinY() { return minY; }
    public int getMinZ() { return minZ; }
    public int getMaxX() { return maxX; }
    public int getMaxY() { return maxY; }
    public int getMaxZ() { return maxZ; }

    /** Returns true if the given block coordinate falls within this bounding box (inclusive). */
    public boolean contains(int x, int y, int z) {
        return x >= minX && x <= maxX
            && y >= minY && y <= maxY
            && z >= minZ && z <= maxZ;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof MapBoundingBox)) return false;
        MapBoundingBox that = (MapBoundingBox) o;
        return minX == that.minX && minY == that.minY && minZ == that.minZ
            && maxX == that.maxX && maxY == that.maxY && maxZ == that.maxZ;
    }

    @Override
    public int hashCode() {
        return Objects.hash(minX, minY, minZ, maxX, maxY, maxZ);
    }

    @Override
    public String toString() {
        return "MapBoundingBox{(" + minX + "," + minY + "," + minZ + ")-("
                + maxX + "," + maxY + "," + maxZ + ")}";
    }
}
