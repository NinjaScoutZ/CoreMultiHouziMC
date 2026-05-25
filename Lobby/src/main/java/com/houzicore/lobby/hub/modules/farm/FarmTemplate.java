package com.houzicore.lobby.hub.modules.farm;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;

public class FarmTemplate {

    private final Location _origin;
    // Player UUID → their saved snapshot
    private final Map<UUID, List<BlockState>> _snapshots = new HashMap<>();

    public FarmTemplate(Location origin) {
        _origin = origin;
    }

    public Location getOrigin() {
        return _origin.clone();
    }

    /**
     * Take a snapshot of the farm blocks at origin and associate it with this player.
     */
    public void snapshot(UUID playerUuid) {
        List<BlockState> states = new ArrayList<>();
        // 20x20 scan — adjust to real farm dimensions
        for (int dx = -10; dx <= 10; dx++) {
            for (int dz = -10; dz <= 10; dz++) {
                for (int dy = -1; dy <= 3; dy++) {
                    Block block = _origin.getWorld().getBlockAt(
                        _origin.getBlockX() + dx,
                        _origin.getBlockY() + dy,
                        _origin.getBlockZ() + dz);
                    if (isFarmBlock(block.getType())) {
                        states.add(block.getState());
                    }
                }
            }
        }
        _snapshots.put(playerUuid, states);
    }

    /**
     * Restore this player's farm snapshot.
     */
    public void restore(UUID playerUuid) {
        List<BlockState> states = _snapshots.remove(playerUuid);
        if (states == null) return;
        for (BlockState state : states) {
            state.update(true, false);
        }
    }

    private boolean isFarmBlock(Material mat) {
        return switch (mat) {
            case WHEAT, SUGAR_CANE, PUMPKIN, MELON, OAK_LOG, BAMBOO,
                 FARMLAND, DIRT, GRASS_BLOCK, OAK_LEAVES, PUMPKIN_STEM, MELON_STEM -> true;
            default -> false;
        };
    }

    public static int scoreFor(Material mat) {
        return switch (mat) {
            case WHEAT        -> 1;
            case SUGAR_CANE   -> 1;
            case PUMPKIN      -> 3;
            case MELON        -> 3;
            case OAK_LOG      -> 2;
            case BAMBOO       -> 1;
            default           -> 0;
        };
    }
}
