package com.houzicore.shared.api.map;

import java.util.Objects;

/**
 * An immutable (x, y, z) coordinate representing a single labelled point on a map.
 * All coordinates are in block-integer space (world coordinates).
 */
public final class MapPoint {

    private final int x;
    private final int y;
    private final int z;
    private final float yaw;
    private final float pitch;

    public MapPoint(int x, int y, int z) {
        this(x, y, z, 0f, 0f);
    }

    public MapPoint(int x, int y, int z, float yaw, float pitch) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.yaw = yaw;
        this.pitch = pitch;
    }

    public int getX() { return x; }
    public int getY() { return y; }
    public int getZ() { return z; }
    public float getYaw() { return yaw; }
    public float getPitch() { return pitch; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof MapPoint)) return false;
        MapPoint that = (MapPoint) o;
        return x == that.x && y == that.y && z == that.z
            && Float.compare(that.yaw, yaw) == 0 && Float.compare(that.pitch, pitch) == 0;
    }

    @Override
    public int hashCode() {
        return Objects.hash(x, y, z, yaw, pitch);
    }

    @Override
    public String toString() {
        return "MapPoint{" + x + ", " + y + ", " + z + ", yaw=" + yaw + ", pitch=" + pitch + "}";
    }
}
