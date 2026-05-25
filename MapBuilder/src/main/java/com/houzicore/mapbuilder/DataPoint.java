package com.houzicore.mapbuilder;

import org.bukkit.Location;

public class DataPoint {
    private final String type;
    private final Location location;

    public DataPoint(String type, Location location) {
        this.type = type;
        this.location = location;
    }

    public String getType() {
        return type;
    }

    public Location getLocation() {
        return location;
    }
}
