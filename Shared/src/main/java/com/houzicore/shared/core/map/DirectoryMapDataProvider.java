package com.houzicore.shared.core.map;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import com.houzicore.shared.api.map.MapBoundingBox;
import com.houzicore.shared.api.map.MapDataProvider;
import com.houzicore.shared.api.map.MapDefinition;
import com.houzicore.shared.api.map.MapPoint;
import com.houzicore.shared.api.map.MapPointType;

public class DirectoryMapDataProvider implements MapDataProvider {

    @Override
    public Optional<MapDefinition> load(String mapName, String gameType) {
        // By default, assume the global Maps folder structure for central loading
        File dir = new File("Maps/" + gameType + "/" + mapName);
        if (!dir.exists() || !dir.isDirectory()) {
            return Optional.empty();
        }
        return loadFromDirectory(dir);
    }

    @Override
    public boolean exists(String mapName, String gameType) {
        return new File("Maps/" + gameType + "/" + mapName).exists();
    }

    @Override
    public Optional<MapDefinition> loadFromDirectory(File directory) {
        File configFile = new File(directory, "WorldConfig.dat");
        if (!configFile.exists()) {
            return Optional.empty();
        }

        try (FileInputStream fstream = new FileInputStream(configFile);
             DataInputStream in = new DataInputStream(fstream);
             BufferedReader br = new BufferedReader(new InputStreamReader(in))) {

            String mapName = "Unknown";
            String author = "Unknown";
            Integer minX = null, maxX = null, minY = null, maxY = null, minZ = null, maxZ = null;

            Map<String, List<MapPoint>> teamSpawns = new HashMap<>();
            Map<String, List<MapPoint>> customPoints = new HashMap<>();
            Map<MapPointType, List<MapPoint>> typedPoints = new HashMap<>();

            Map<String, String> properties = new HashMap<>();

            String line;
            List<MapPoint> currentList = null;
            MapPointType currentType = null;
            String currentTeam = null;
            float currentDirection = 0f;

            while ((line = br.readLine()) != null) {
                String[] tokens = line.split(":");
                if (tokens.length < 2 || tokens[0].length() == 0) continue;

                String key = tokens[0].toUpperCase();
                String val = tokens[1];

                if (key.equals("MAP_NAME")) {
                    mapName = val;
                } else if (key.equals("MAP_AUTHOR")) {
                    author = val;
                } else if (key.equals("TEAM_NAME")) {
                    teamSpawns.put(val, new ArrayList<>());
                    currentList = teamSpawns.get(val);
                    currentDirection = 0f;
                } else if (key.equals("TEAM_DIRECTION")) {
                    try { currentDirection = Float.parseFloat(val); } catch (NumberFormatException ignored) {}
                } else if (key.equals("TEAM_SPAWNS")) {
                    parsePoints(tokens, currentList, currentDirection);
                } else if (key.equals("DATA_NAME") || key.equals("CUSTOM_NAME")) {
                    customPoints.put(val, new ArrayList<>());
                    currentList = customPoints.get(val);
                    
                    // Attempt typed mapping as well
                    MapPointType type = MapPointType.fromKey(val);
                    if (type != MapPointType.CUSTOM) {
                        typedPoints.putIfAbsent(type, new ArrayList<>());
                        currentType = type;
                    } else {
                        currentType = null;
                    }
                } else if (key.equals("DATA_LOCS") || key.equals("CUSTOM_LOCS")) {
                    parsePoints(tokens, currentList, 0f); // Default 0 yaw for standard data points
                    if (currentType != null) {
                        parsePoints(tokens, typedPoints.get(currentType), 0f);
                    }
                } else if (key.equals("MIN_X")) minX = parseInt(val);
                else if (key.equals("MAX_X")) maxX = parseInt(val);
                else if (key.equals("MIN_Y")) minY = parseInt(val);
                else if (key.equals("MAX_Y")) maxY = parseInt(val);
                else if (key.equals("MIN_Z")) minZ = parseInt(val);
                else if (key.equals("MAX_Z")) maxZ = parseInt(val);
                else properties.put(key, val);
            }

            MapBoundingBox bbox = null;
            if (minX != null && maxX != null && minZ != null && maxZ != null) {
                bbox = new MapBoundingBox(minX, minY != null ? minY : -1, minZ, maxX, maxY != null ? maxY : 256, maxZ);
            }

            return Optional.of(new ParsedMapDefinition(mapName, author, "UNKNOWN", teamSpawns, customPoints, typedPoints, properties, bbox));

        } catch (Exception e) {
            e.printStackTrace();
            return Optional.empty();
        }
    }

    private void parsePoints(String[] tokens, List<MapPoint> list, float yaw) {
        if (list == null) return;
        for (int i = 1; i < tokens.length; i++) {
            String[] coords = tokens[i].split(",");
            if (coords.length >= 3) {
                try {
                    list.add(new MapPoint(Integer.parseInt(coords[0]), Integer.parseInt(coords[1]), Integer.parseInt(coords[2]), yaw, 0f));
                } catch (NumberFormatException ignored) {}
            }
        }
    }

    private Integer parseInt(String val) {
        try { return Integer.parseInt(val); } catch (NumberFormatException e) { return null; }
    }

    private static class ParsedMapDefinition implements MapDefinition {
        private final String name, author, gameType;
        private final Map<String, List<MapPoint>> teamSpawns, customPoints;
        private final Map<MapPointType, List<MapPoint>> typedPoints;
        private final MapBoundingBox bounds;
        private final Map<String, String> properties;

        ParsedMapDefinition(String name, String author, String gameType,
                            Map<String, List<MapPoint>> teamSpawns,
                            Map<String, List<MapPoint>> customPoints,
                            Map<MapPointType, List<MapPoint>> typedPoints,
                            Map<String, String> properties,
                            MapBoundingBox bounds) {
            this.name = name;
            this.author = author;
            this.gameType = gameType;
            this.teamSpawns = teamSpawns;
            this.customPoints = customPoints;
            this.typedPoints = typedPoints;
            this.properties = properties;
            this.bounds = bounds;
        }

        @Override public String getMapName() { return name; }
        @Override public String getAuthor() { return author; }
        @Override public String getGameType() { return gameType; }
        @Override public Map<String, String> getProperties() { return properties; }

        @Override public List<MapPoint> getPoints(MapPointType type) {
            return typedPoints.getOrDefault(type, Collections.emptyList());
        }

        @Override public Optional<MapBoundingBox> getBoundingBox() {
            return Optional.ofNullable(bounds);
        }

        @Override public List<MapPoint> getCustomPoints(String key) {
            return customPoints.getOrDefault(key, Collections.emptyList());
        }

        @Override public List<MapPoint> getTeamSpawns(String teamName) {
            return teamSpawns.getOrDefault(teamName, Collections.emptyList());
        }

        @Override public java.util.Set<String> getTeamNames() {
            return teamSpawns.keySet();
        }

        @Override public java.util.Set<String> getCustomPointKeys() {
            return customPoints.keySet();
        }
    }
}
