package com.houzicore.mapbuilder;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.WorldCreator;
import org.bukkit.entity.Player;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class WorldImporter {

    public static void importMap(Player player, String gameType, String mapName) {
        String zipPath = "../../Maps/" + gameType + "/" + mapName + ".zip";
        File zipFile = new File(zipPath);
        
        // Try fallback paths just in case (e.g., running from different directories)
        if (!zipFile.exists()) {
            zipPath = "../Maps/" + gameType + "/" + mapName + ".zip";
            zipFile = new File(zipPath);
        }
        if (!zipFile.exists()) {
            zipPath = "Maps/" + gameType + "/" + mapName + ".zip";
            zipFile = new File(zipPath);
        }

        if (!zipFile.exists()) {
            player.sendMessage(ChatColor.RED + "Could not find map zip at: " + zipPath);
            return;
        }

        String worldName = "edit_" + mapName;
        File worldFolder = new File(worldName);
        
        if (worldFolder.exists()) {
            player.sendMessage(ChatColor.YELLOW + "World folder already exists. Unloading and replacing...");
            World existingWorld = Bukkit.getWorld(worldName);
            if (existingWorld != null) {
                for (Player p : existingWorld.getPlayers()) {
                    p.teleport(Bukkit.getWorlds().get(0).getSpawnLocation());
                }
                Bukkit.unloadWorld(existingWorld, false);
            }
            deleteDirectory(worldFolder);
        }

        player.sendMessage(ChatColor.GREEN + "Unzipping map " + mapName + "...");

        try {
            unzip(zipPath, worldFolder.getPath());
        } catch (Exception e) {
            player.sendMessage(ChatColor.RED + "Failed to unzip the map.");
            e.printStackTrace();
            return;
        }

        // Clean useless files to force a clean load
        new File(worldFolder, "uid.dat").delete();
        new File(worldFolder, "session.lock").delete();

        // Load the world
        player.sendMessage(ChatColor.GREEN + "Loading world into server...");
        World world = Bukkit.createWorld(new WorldCreator(worldName));
        
        if (world == null) {
            player.sendMessage(ChatColor.RED + "Failed to create/load the world!");
            return;
        }

        // Setup session
        MapBuilderPlugin.getInstance().startSession(player, gameType, mapName, world, com.houzicore.mapbuilder.session.EditMode.PACKAGED_MAP);
        MapSession session = MapBuilderPlugin.getInstance().getSession(player);
        if (session == null) {
            // Something went wrong or they cancelled immediately
            return; 
        }

        Location spawnLoc = world.getSpawnLocation(); // Default teleport target
        spawnLoc = loadFilesIntoSession(player, session, spawnLoc);

        // Teleport
        player.teleport(spawnLoc);
        player.sendMessage(ChatColor.GREEN + "Map import complete! You are now editing " + ChatColor.YELLOW + mapName);
    }

    public static void editCurrentWorld(Player player, String gameType, String mapName) {
        World world = player.getWorld();

        MapBuilderPlugin.getInstance().startSession(player, gameType, mapName, world, com.houzicore.mapbuilder.session.EditMode.CURRENT_WORLD);
        MapSession session = MapBuilderPlugin.getInstance().getSession(player);
        if (session == null) {
            return;
        }

        loadFilesIntoSession(player, session, player.getLocation());
    }

    public static Location loadFilesIntoSession(Player player, MapSession session, Location defaultSpawn) {
        File configFile = session.getWorldConfigFile();
        File schemaFile = new File(configFile.getParentFile(), "schema.json");
        World world = session.getEditWorld();  // authoritative world from session
        
        if (schemaFile.exists()) {
            player.sendMessage(ChatColor.GREEN + "Loading schema.json from " + ChatColor.YELLOW + session.getEditWorldName());
            Location loadedLoc = loadSchemaIntoSession(player, session, schemaFile.getParentFile(), world, defaultSpawn);
            if (loadedLoc != null) {
                return loadedLoc;
            } else {
                player.sendMessage(ChatColor.RED + "Failed to parse schema.json! Falling back...");
            }
        }
        
        if (configFile.exists()) {
            player.sendMessage(ChatColor.GREEN + "Loading legacy WorldConfig.dat from " + ChatColor.YELLOW + session.getEditWorldName());
            return loadConfigIntoSession(player, session, configFile, world, defaultSpawn);
        }
        
        player.sendMessage(ChatColor.YELLOW + "No schema/config found in the target directory. Starting fresh.");
        return defaultSpawn;
    }

    public static Location parseConfig(File configFile, World world, MapSession session, Location defaultSpawn) {
        Location firstSpawn = null;
        try (BufferedReader br = new BufferedReader(new FileReader(configFile))) {
            String line;
            String currentType = null;
            Integer minX = null, minY = null, minZ = null;
            Integer maxX = null, maxY = null, maxZ = null;

            while ((line = br.readLine()) != null) {
                if (line.trim().isEmpty()) continue;
                String[] parts = line.split(":", 2);
                if (parts.length < 2) continue;

                String key = parts[0];
                String value = parts[1];

                if (key.equals("TEAM_NAME") || key.equals("DATA_NAME") || key.equals("CUSTOM_NAME")) {
                    currentType = key + ":" + value;
                }
                else if (key.equals("TEAM_SPAWNS") || key.equals("DATA_LOCS") || key.equals("CUSTOM_LOCS")) {
                    if (currentType != null) {
                        String[] locs = value.split(":");
                        for (String locStr : locs) {
                            String[] coords = locStr.split(",");
                            if (coords.length >= 3) {
                                try {
                                    int x = Integer.parseInt(coords[0]);
                                    int y = Integer.parseInt(coords[1]);
                                    int z = Integer.parseInt(coords[2]);
                                    Location l = new Location(world, x, y, z);
                                    session.addDataPoint(currentType, l);
                                    if (firstSpawn == null) firstSpawn = l;
                                } catch (NumberFormatException ignored) {}
                            }
                        }
                    }
                }
                else if (key.equals("MIN_X")) minX = Integer.parseInt(value);
                else if (key.equals("MIN_Y")) minY = Integer.parseInt(value);
                else if (key.equals("MIN_Z")) minZ = Integer.parseInt(value);
                else if (key.equals("MAX_X")) maxX = Integer.parseInt(value);
                else if (key.equals("MAX_Y")) maxY = Integer.parseInt(value);
                else if (key.equals("MAX_Z")) maxZ = Integer.parseInt(value);
            }

            if (minX != null && minY != null && minZ != null) {
                session.setMinBoundary(new Location(world, minX, minY, minZ));
            }
            if (maxX != null && maxY != null && maxZ != null) {
                session.setMaxBoundary(new Location(world, maxX, maxY, maxZ));
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return firstSpawn != null ? (firstSpawn.clone().add(0, 1, 0)) : defaultSpawn;
    }

    private static Location loadSchemaIntoSession(Player player, MapSession session, File directory, World world, Location defaultSpawn) {
        com.houzicore.mapbuilder.schema.MapSchema schema = com.houzicore.mapbuilder.schema.MapSchemaRepository.load(directory);
        if (schema == null) return null;
        
        Location firstSpawn = null;
        
        // Parse Bounding Box
        if (schema.getBounds() != null) {
            session.setMinBoundary(new Location(world, schema.getBounds().minX, schema.getBounds().minY, schema.getBounds().minZ));
            session.setMaxBoundary(new Location(world, schema.getBounds().maxX, schema.getBounds().maxY, schema.getBounds().maxZ));
        }
        
        // Parse Points
        int loadedPoints = 0;
        for (java.util.Map.Entry<String, java.util.List<com.houzicore.mapbuilder.schema.MapSchema.SchemaPoint>> entry : schema.getDataPoints().entrySet()) {
            for (com.houzicore.mapbuilder.schema.MapSchema.SchemaPoint sp : entry.getValue()) {
                Location loc = new Location(world, sp.x, sp.y, sp.z);
                session.addDataPoint(entry.getKey(), loc);
                loadedPoints++;
                if (firstSpawn == null) firstSpawn = loc;
            }
        }

        // MB-09D: load properties — prefer schema; if schema has none, try dat supplement
        if (!schema.getProperties().isEmpty()) {
            // Sanitize: reject any key that matches a reserved dat key or structured prefix.
            // Defends against hand-edited schema.json injecting TEAM_NAME:RED or DATA_NAME:X
            // into properties, which would create ambiguous WorldConfig.dat lines on next export.
            java.util.Map<String, String> safe = new java.util.LinkedHashMap<>();
            schema.getProperties().forEach((k, v) -> {
                if (!com.houzicore.mapbuilder.schema.MapSchema.isReservedKey(k)) {
                    safe.put(k, v);
                } else {
                    session.getBuilder().sendMessage(
                            org.bukkit.ChatColor.YELLOW + "[MapBuilder] Skipped reserved property key '"
                                    + k + "' from schema.json (cannot be used as a property).");
                }
            });
            session.loadProperties(safe);
        } else {
            // Legacy schema or freshly-created schema with no properties: supplement from dat
            File configFile = session.getWorldConfigFile();
            if (configFile.exists()) {
                java.util.Map<String, String> datProps = parseDatProperties(configFile);
                if (!datProps.isEmpty()) {
                    session.loadProperties(datProps);
                }
            }
        }

        
        // Spawn Visuals
        for (String type : session.getDataPoints().keySet()) {
            for (Location loc : session.getDataPoints().get(type)) {
                com.houzicore.mapbuilder.VisualManager.getInstance().spawnVisual(session.getBuilder().getUniqueId(), type, loc);
            }
        }
        
        if (session.getMinBoundary() != null) {
            com.houzicore.mapbuilder.VisualManager.getInstance().updateBoundaryVisual(session);
        }

        player.sendMessage(org.bukkit.ChatColor.AQUA + "Loaded " + loadedPoints + " point(s) from "
                + org.bukkit.ChatColor.WHITE + new File(directory, "schema.json").getAbsolutePath());
                
        return firstSpawn != null ? firstSpawn.clone().add(0, 1, 0) : defaultSpawn;
    }

    private static Location loadConfigIntoSession(Player player, MapSession session, File configFile, World world, Location defaultSpawn) {
        Location spawnLoc = parseConfig(configFile, world, session, defaultSpawn);
        // MB-09D: also load dat properties into session when loading via dat path
        java.util.Map<String, String> datProps = parseDatProperties(configFile);
        session.loadProperties(datProps);
        int loadedPoints = 0;
        for (String type : session.getDataPoints().keySet()) {
            for (Location loc : session.getDataPoints().get(type)) {
                VisualManager.getInstance().spawnVisual(session.getBuilder().getUniqueId(), type, loc);
                loadedPoints++;
            }
        }
        if (session.getMinBoundary() != null) {
            VisualManager.getInstance().updateBoundaryVisual(session);
        }

        player.sendMessage(ChatColor.AQUA + "Loaded " + loadedPoints + " point(s) from "
                + ChatColor.WHITE + configFile.getAbsolutePath());
        return spawnLoc;
    }

    /**
     * Extracts arbitrary string properties from a WorldConfig.dat file.
     * Properties are bare KEY:VALUE lines that do not match any reserved dat key.
     */
    private static java.util.Map<String, String> parseDatProperties(File configFile) {
        java.util.Map<String, String> result = new java.util.LinkedHashMap<>();
        try (BufferedReader br = new BufferedReader(new FileReader(configFile))) {
            String line;
            while ((line = br.readLine()) != null) {
                if (line.trim().isEmpty()) continue;
                String[] parts = line.split(":", 2);
                if (parts.length < 2) continue;
                String key = parts[0].trim();
                // Only collect keys not in the reserved set
                if (!com.houzicore.mapbuilder.schema.MapSchema.RESERVED_DAT_KEYS.contains(key)
                        && !key.startsWith("TEAM_NAME")
                        && !key.startsWith("DATA_NAME")
                        && !key.startsWith("CUSTOM_NAME")
                        && !key.startsWith("BLOCK_DISPLAY")) {
                    result.put(key, parts[1].trim());
                }
            }
        } catch (Exception ignored) {}
        return result;
    }

    private static void deleteDirectory(File dir) {
        if (dir.isDirectory()) {
            File[] children = dir.listFiles();
            if (children != null) {
                for (File child : children) {
                    deleteDirectory(child);
                }
            }
        }
        dir.delete();
    }

    private static void unzip(String zipFilePath, String destDir) throws IOException {
        File dir = new File(destDir);
        if (!dir.exists()) dir.mkdirs();
        try (ZipInputStream zis = new ZipInputStream(new FileInputStream(zipFilePath))) {
            ZipEntry zipEntry = zis.getNextEntry();
            while (zipEntry != null) {
                File newFile = new File(destDir, zipEntry.getName());
                if (zipEntry.isDirectory()) {
                    newFile.mkdirs();
                } else {
                    newFile.getParentFile().mkdirs();
                    try (FileOutputStream fos = new FileOutputStream(newFile)) {
                        byte[] buffer = new byte[1024];
                        int len;
                        while ((len = zis.read(buffer)) > 0) {
                            fos.write(buffer, 0, len);
                        }
                    }
                }
                zipEntry = zis.getNextEntry();
            }
            zis.closeEntry();
        }
    }
}
