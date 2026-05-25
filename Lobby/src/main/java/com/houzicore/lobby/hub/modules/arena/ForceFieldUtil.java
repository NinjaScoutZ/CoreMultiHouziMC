package com.houzicore.lobby.hub.modules.arena;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;

public class ForceFieldUtil {

    public ForceFieldUtil() {}

    public List<Block> spawnRing(Location center, double radius, int heightBlocks) {
        List<Block> placed = new ArrayList<>();
        int cx = center.getBlockX();
        int cy = center.getBlockY();
        int cz = center.getBlockZ();
        int r = (int) Math.ceil(radius);
        int rSq = r * r;
        int innerRSq = (r - 1) * (r - 1);

        for (int x = cx - r; x <= cx + r; x++) {
            for (int z = cz - r; z <= cz + r; z++) {
                int distSq = (x - cx) * (x - cx) + (z - cz) * (z - cz);
                if (distSq <= rSq && distSq > innerRSq) {
                    for (int dy = 0; dy < heightBlocks; dy++) {
                        Block block = center.getWorld().getBlockAt(x, cy + dy, z);
                        if (block.getType() == Material.AIR || block.getType() == Material.TALL_GRASS || block.getType() == Material.SHORT_GRASS) {
                            block.setType(Material.BARRIER);
                            placed.add(block);
                        }
                    }
                }
            }
        }
        return placed;
    }

    public void removeRing(List<Block> blocks) {
        for (Block block : blocks) {
            if (block.getType() == Material.BARRIER) {
                block.setType(Material.AIR);
            }
        }
        blocks.clear();
    }
}
