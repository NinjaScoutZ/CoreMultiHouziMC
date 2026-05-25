package com.houzicore.mapparser;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Sign;
import org.bukkit.block.data.type.Leaves;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.WorldBorder;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.ChatColor;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import com.houzicore.shared.common.util.ZipUtil;

public class Parse extends BukkitRunnable {
    
    private final MapParserPlugin host;
    private final World world;
    private final Location callLoc;
    
    private final int size;
    private int x, y, z;
    
    private final MapData mapData;
    
    private final HashMap<String, ArrayList<Location>> teamLocs = new HashMap<>();
    private final HashMap<String, ArrayList<Location>> dataLocs = new HashMap<>();
    private final HashMap<String, ArrayList<Location>> customLocs = new HashMap<>();
    
    private Location cornerA = null;
    private Location cornerB = null;
    
    private org.bukkit.Chunk currentChunk = null;
    
    private int processed = 0;
    
    public Parse(MapParserPlugin host, World world, Location loc, MapData data, int size) {
        this.host = host;
        this.world = world;
        this.callLoc = new Location(world, loc.getX(), loc.getY(), loc.getZ());
        this.mapData = data;
        this.size = size;
        
        this.x = -size;
        this.z = -size;
        this.y = world.getMinHeight();
        
        WorldBorder border = world.getWorldBorder();
        border.setCenter(callLoc);
        border.setSize(1);
        border.setDamageAmount(0);
        border.setWarningDistance(0);

        host.announce("Commencing Parse of World: " + world.getName());
        this.runTaskTimer(host, 1L, 1L);
    }
    
    @Override
    public void run() {
        long startTime = System.currentTimeMillis();
        
        int maxHeight = world.getMaxHeight();
        WorldBorder border = world.getWorldBorder();
        
        for (; x <= size; x++) {
            double progress = (double)(x + size) / (size * 2);
            double currentSize = Math.max(1, progress * (size * 2));
            border.setSize(currentSize);

            int worldX = callLoc.getBlockX() + x;
            int chunkX = worldX >> 4;

            for (; z <= size; z++) {
                int worldZ = callLoc.getBlockZ() + z;
                int chunkZ = worldZ >> 4;

                if (currentChunk == null || currentChunk.getX() != chunkX || currentChunk.getZ() != chunkZ) {
                    if (!world.isChunkGenerated(chunkX, chunkZ)) {
                        currentChunk = null;
                    } else {
                        currentChunk = world.getChunkAt(chunkX, chunkZ);
                    }
                }

                if (currentChunk == null) {
                    processed += (maxHeight - y);
                    y = world.getMinHeight();
                    continue;
                }

                for (; y < maxHeight; y++) {
                    if ((y & 127) == 0 && System.currentTimeMillis() - startTime >= 25) {
                        return; // Yield to next tick
                    }
                    
                    processed++;
                    if (processed % 10000000 == 0) {
                        host.announce("Scanning World: " + (processed / 1000000) + "M blocks");
                    }
                    
                    Block block = currentChunk.getBlock(worldX & 15, y, worldZ & 15);
                    Material type = block.getType();
                    
                    if (type.isAir()) continue;
                    
                    // Leaves no-decay
                    if (type.name().endsWith("_LEAVES")) {
                        if (block.getBlockData() instanceof Leaves) {
                            Leaves leaves = (Leaves) block.getBlockData();
                            if (!leaves.isPersistent()) {
                                leaves.setPersistent(true);
                                block.setBlockData(leaves);
                            }
                        }
                    }
                    
                    // Signs on Sponge (with Flood-Fill)
                    else if (type.name().endsWith("_SIGN")) {
                        Block below = block.getRelative(BlockFace.DOWN);
                        if (below.getType() == Material.SPONGE) {
                            Sign sign = (Sign) block.getState();
                            StringBuilder nameBuilder = new StringBuilder();
                            for (String line : sign.getLines()) {
                                if (line != null && !line.isEmpty()) {
                                    if (nameBuilder.length() > 0) nameBuilder.append(" ");
                                    nameBuilder.append(line);
                                }
                            }
                            String name = nameBuilder.toString().trim();
                            
                            HashSet<Location> connected = floodFillSponge(below);
                            
                            if (!name.isEmpty()) {
                                customLocs.computeIfAbsent(name, k -> new ArrayList<>()).addAll(connected);
                            }
                            
                            host.announce("Flood-Fill: " + name + " found " + connected.size() + " connected sponge blocks");
                            
                            block.setType(Material.AIR);
                            for (Location loc : connected) {
                                loc.getBlock().setType(Material.AIR);
                            }
                        }
                    }
                    
                    // Boundary & Team Spawns (Light Weighted Pressure Plate / Gold)
                    else if (type == Material.LIGHT_WEIGHTED_PRESSURE_PLATE) {
                        Block wool = block.getRelative(BlockFace.DOWN);
                        if (wool.getType().name().endsWith("_WOOL")) {
                            String color = getWoolColorName(wool.getType());
                            
                            if (color.equals("WHITE")) {
                                if (cornerA == null) {
                                    cornerA = wool.getLocation();
                                    host.announce("Corner A: " + cornerA.getBlockX() + ", " + cornerA.getBlockZ());
                                } else if (cornerB == null) {
                                    cornerB = wool.getLocation();
                                    host.announce("Corner B: " + cornerB.getBlockX() + ", " + cornerB.getBlockZ());
                                } else {
                                    host.announce("More than 2 Corner Markers found!");
                                }
                            } else {
                                String teamName = getTeamNameFromColor(color);
                                teamLocs.computeIfAbsent(teamName, k -> new ArrayList<>()).add(wool.getLocation());
                            }
                            block.setType(Material.AIR);
                            wool.setType(Material.AIR);
                        }
                    }
                    
                    // Data Locs (Heavy Weighted Pressure Plate / Iron)
                    else if (type == Material.HEAVY_WEIGHTED_PRESSURE_PLATE) {
                        Block wool = block.getRelative(BlockFace.DOWN);
                        if (wool.getType().name().endsWith("_WOOL")) {
                            String color = getWoolColorName(wool.getType());
                            dataLocs.computeIfAbsent(color, k -> new ArrayList<>()).add(wool.getLocation());
                            block.setType(Material.AIR);
                            wool.setType(Material.AIR);
                        }
                    }
                }
                y = world.getMinHeight();
            }
            z = -size;
        }
        
        finalizeParse();
    }
    
    private void finalizeParse() {
        this.cancel(); // Stop ticking
        host.setCurrentParse(null);
        
        world.getWorldBorder().reset();
        
        if (cornerA == null || cornerB == null) {
            host.announce("Missing Corner Locations! Defaulting to -256 to +256.");
            cornerA = new Location(world, -256, 0, -256);
            cornerB = new Location(world, 256, 0, 256);
        }
        
        try {
            File configFile = new File(world.getWorldFolder(), "WorldConfig.dat");
            try (BufferedWriter out = new BufferedWriter(new FileWriter(configFile))) {
                out.write("MAP_NAME:" + mapData.MapName + "\n");
                out.write("MAP_AUTHOR:" + mapData.MapCreator + "\n\n");
                
                out.write("MIN_X:" + Math.min(cornerA.getBlockX(), cornerB.getBlockX()) + "\n");
                out.write("MAX_X:" + Math.max(cornerA.getBlockX(), cornerB.getBlockX()) + "\n");
                out.write("MIN_Z:" + Math.min(cornerA.getBlockZ(), cornerB.getBlockZ()) + "\n");
                out.write("MAX_Z:" + Math.max(cornerA.getBlockZ(), cornerB.getBlockZ()) + "\n\n");
                
                if (cornerA.getBlockY() == cornerB.getBlockY()) {
                    out.write("MIN_Y:" + world.getMinHeight() + "\n");
                    out.write("MAX_Y:" + world.getMaxHeight() + "\n");
                } else {
                    out.write("MIN_Y:" + Math.min(cornerA.getBlockY(), cornerB.getBlockY()) + "\n");
                    out.write("MAX_Y:" + Math.max(cornerA.getBlockY(), cornerB.getBlockY()) + "\n");
                }
                
                for (String team : teamLocs.keySet()) {
                    out.write("\n\nTEAM_NAME:" + team + "\n");
                    out.write("TEAM_SPAWNS:" + locationsToString(teamLocs.get(team)));
                }
                
                for (String data : dataLocs.keySet()) {
                    out.write("\n\nDATA_NAME:" + data + "\n");
                    out.write("DATA_LOCS:" + locationsToString(dataLocs.get(data)));
                }
                
                for (String custom : customLocs.keySet()) {
                    out.write("\n\nCUSTOM_NAME:" + custom + "\n");
                    out.write("CUSTOM_LOCS:" + locationsToString(customLocs.get(custom)));
                }
            }
            
            // Also generate schema.json
            com.houzicore.mapparser.SchemaExporter.export(world.getWorldFolder(), mapData, cornerA, cornerB, teamLocs, dataLocs, customLocs);
            
            host.announce("WorldConfig.dat and schema.json Saved.");
            
            // Generate ZIP file safely without deleting live world files
            try {
                File outputDir = new File(host.getServer().getWorldContainer(), "parsed_maps/" + mapData.MapGameType);
                outputDir.mkdirs();
                String zipName = mapData.MapGameType + "_" + mapData.MapName + ".zip";
                File zipFile = new File(outputDir, zipName);
                
                String basePath = world.getWorldFolder().getAbsolutePath();
                java.util.List<String> filesToZip = new java.util.ArrayList<>();
                
                File levelDat = new File(world.getWorldFolder(), "level.dat");
                if (levelDat.exists()) filesToZip.add(levelDat.getAbsolutePath());
                
                File worldConfig = new File(world.getWorldFolder(), "WorldConfig.dat");
                if (worldConfig.exists()) filesToZip.add(worldConfig.getAbsolutePath());
                
                File schemaJson = new File(world.getWorldFolder(), "schema.json");
                if (schemaJson.exists()) filesToZip.add(schemaJson.getAbsolutePath());
                
                java.util.List<String> foldersToZip = new java.util.ArrayList<>();
                File regionFolder = new File(world.getWorldFolder(), "region");
                if (regionFolder.exists()) foldersToZip.add(regionFolder.getAbsolutePath());
                
                ZipUtil.ZipFolders(basePath, zipFile.getAbsolutePath(), foldersToZip, filesToZip);
                host.announce("ZIP saved: " + zipFile.getAbsolutePath());
            } catch (Exception e) {
                host.announce("Error creating ZIP file");
                e.printStackTrace();
            }
            
        } catch (Exception e) {
            host.announce("Error: File Write Error");
            e.printStackTrace();
        }
    }
    
    private String locationsToString(ArrayList<Location> locs) {
        StringBuilder out = new StringBuilder();
        for (Location loc : locs) {
            out.append(loc.getBlockX()).append(",")
               .append(loc.getBlockY()).append(",")
               .append(loc.getBlockZ()).append(":");
        }
        return out.toString();
    }
    
    private String getWoolColorName(Material material) {
        String name = material.name();
        return name.replace("_WOOL", "");
    }
    
    private String getTeamNameFromColor(String color) {
        switch (color) {
            case "ORANGE": return "Orange";
            case "MAGENTA": return "Magenta";
            case "LIGHT_BLUE": return "Sky";
            case "YELLOW": return "Yellow";
            case "LIME": return "Lime";
            case "PINK": return "Pink";
            case "GRAY": return "Gray";
            case "LIGHT_GRAY": return "LGray";
            case "CYAN": return "Cyan";
            case "PURPLE": return "Purple";
            case "BLUE": return "Blue";
            case "BROWN": return "Brown";
            case "GREEN": return "Green";
            case "RED": return "Red";
            case "BLACK": return "Black";
            default: return color;
        }
    }
    
    private HashSet<Location> floodFillSponge(Block startBlock) {
        HashSet<Location> visited = new HashSet<>();
        LinkedList<Block> queue = new LinkedList<>();
        queue.add(startBlock);
        visited.add(startBlock.getLocation());

        BlockFace[] faces = {BlockFace.UP, BlockFace.DOWN, BlockFace.NORTH, BlockFace.SOUTH, BlockFace.EAST, BlockFace.WEST};

        while (!queue.isEmpty()) {
            Block current = queue.poll();
            for (BlockFace face : faces) {
                Block neighbor = current.getRelative(face);
                if (neighbor.getType() == Material.SPONGE && !visited.contains(neighbor.getLocation())) {
                    visited.add(neighbor.getLocation());
                    queue.add(neighbor);
                }
            }
        }
        return visited;
    }
}
