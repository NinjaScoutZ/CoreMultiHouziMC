package com.houzicore.shared.core.common.block;

import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.List;

import org.bukkit.DyeColor;
import org.bukkit.Location;

public class DataLocationMap {

    private final EnumMap<DyeColor, List<Location>> _goldDataMap;
    private final EnumMap<DyeColor, List<Location>> _ironDataMap;
    private final EnumMap<DyeColor, List<Location>> _spongeDataMap;

    public DataLocationMap() {
        _goldDataMap = new EnumMap<>(DyeColor.class);
        _ironDataMap = new EnumMap<>(DyeColor.class);
        _spongeDataMap = new EnumMap<>(DyeColor.class);
    }

    public List<Location> getGoldLocations(DyeColor color) {
        List<Location> list = _goldDataMap.get(color);
        return list == null ? Collections.emptyList() : list;
    }

    public void addGoldLocation(DyeColor color, Location location) {
        _goldDataMap.computeIfAbsent(color, k -> new ArrayList<>()).add(location);
    }

    public List<Location> getIronLocations(DyeColor color) {
        List<Location> list = _ironDataMap.get(color);
        return list == null ? Collections.emptyList() : list;
    }

    public void addIronLocation(DyeColor color, Location location) {
        _ironDataMap.computeIfAbsent(color, k -> new ArrayList<>()).add(location);
    }

    public void addSpongeLocation(DyeColor color, Location location) {
        _spongeDataMap.computeIfAbsent(color, k -> new ArrayList<>()).add(location);
    }

    public List<Location> getSpongeLocations(DyeColor color) {
        List<Location> list = _spongeDataMap.get(color);
        return list == null ? Collections.emptyList() : list;
    }
}
