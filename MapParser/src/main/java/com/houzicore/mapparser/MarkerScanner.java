package com.houzicore.mapparser;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Sign;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Scans the world for all MapParser markers WITHOUT deleting them.
 * Used by the "/parse check" dry-run command.
 * <p>
 * This is intentionally synchronous and runs on a single tick — for
 * a dry-run we just count markers, not process the full world. We
 * scan within a radius around the player.
 */
public class MarkerScanner {

    /** Result of a dry-run marker scan. */
    public record ScanResult(
            int cornersFound,
            Map<String, List<Location>> teamSpawns,
            Map<String, List<Location>> dataLocs,
            Map<String, List<Location>> customLocs,
            List<String> warnings
    ) {
        public int totalMarkers() {
            int total = cornersFound;
            for (var locs : teamSpawns.values()) total += locs.size();
            for (var locs : dataLocs.values()) total += locs.size();
            for (var locs : customLocs.values()) total += locs.size();
            return total;
        }
    }

    /**
     * Scan all markers in the world within the given radius from center.
     * Does NOT delete anything — purely read-only.
     */
    public static ScanResult scan(World world, Location center, int radius) {
        int corners = 0;
        Map<String, List<Location>> teams = new HashMap<>();
        Map<String, List<Location>> data = new HashMap<>();
        Map<String, List<Location>> custom = new HashMap<>();
        List<String> warnings = new ArrayList<>();

        int cx = center.getBlockX();
        int cz = center.getBlockZ();
        int minHeight = world.getMinHeight();
        int maxHeight = world.getMaxHeight();

        for (int x = cx - radius; x <= cx + radius; x++) {
            for (int z = cz - radius; z <= cz + radius; z++) {
                for (int y = minHeight; y < maxHeight; y++) {
                    Block block = world.getBlockAt(x, y, z);
                    Material type = block.getType();

                    // Gold Pressure Plate → Team Spawn / Corner
                    if (type == Material.LIGHT_WEIGHTED_PRESSURE_PLATE) {
                        Block below = block.getRelative(BlockFace.DOWN);
                        if (below.getType().name().endsWith("_WOOL")) {
                            String color = below.getType().name().replace("_WOOL", "");
                            if (color.equals("WHITE")) {
                                corners++;
                            } else {
                                teams.computeIfAbsent(color, k -> new ArrayList<>()).add(below.getLocation());
                            }
                        } else {
                            warnings.add("Gold Pressure Plate at " + formatLoc(block) + " is NOT on wool!");
                        }
                    }

                    // Iron Pressure Plate → Data Loc
                    if (type == Material.HEAVY_WEIGHTED_PRESSURE_PLATE) {
                        Block below = block.getRelative(BlockFace.DOWN);
                        if (below.getType().name().endsWith("_WOOL")) {
                            String color = below.getType().name().replace("_WOOL", "");
                            data.computeIfAbsent(color, k -> new ArrayList<>()).add(below.getLocation());
                        } else {
                            warnings.add("Iron Pressure Plate at " + formatLoc(block) + " is NOT on wool!");
                        }
                    }

                    // Sign on Sponge → Custom Loc
                    if (type.name().endsWith("_SIGN")) {
                        Block below = block.getRelative(BlockFace.DOWN);
                        if (below.getType() == Material.SPONGE) {
                            String label = "?";
                            if (block.getState() instanceof Sign sign) {
                                StringBuilder sb = new StringBuilder();
                                for (String line : sign.getLines()) {
                                    if (line != null && !line.isEmpty()) {
                                        if (sb.length() > 0) sb.append(" ");
                                        sb.append(line);
                                    }
                                }
                                label = sb.toString().trim();
                                if (label.isEmpty()) label = "(empty)";
                            }
                            custom.computeIfAbsent(label, k -> new ArrayList<>()).add(below.getLocation());
                        }
                    }
                }
            }
        }

        if (corners < 2) {
            warnings.add("Only " + corners + " corner marker(s) found — need exactly 2!");
        } else if (corners > 2) {
            warnings.add(corners + " corner markers found — only 2 are expected!");
        }

        if (teams.isEmpty()) {
            warnings.add("No team spawn markers found!");
        }

        return new ScanResult(corners, teams, data, custom, warnings);
    }

    private static String formatLoc(Block block) {
        return block.getX() + ", " + block.getY() + ", " + block.getZ();
    }
}
