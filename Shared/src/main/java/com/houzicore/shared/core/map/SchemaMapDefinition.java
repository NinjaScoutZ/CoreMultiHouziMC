package com.houzicore.shared.core.map;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;

import com.houzicore.shared.api.map.MapBoundingBox;
import com.houzicore.shared.api.map.MapDefinition;
import com.houzicore.shared.api.map.MapPoint;
import com.houzicore.shared.api.map.MapPointType;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.util.*;
import java.util.logging.Logger;

/**
 * Shared read-side adapter that loads a schema.json written by MapBuilder
 * and exposes it as a MapDefinition without importing any MapBuilder class.
 *
 * schema.json shape (produced by MapBuilder's MapSchema / WorldConfigExporter):
 * {
 *   "mapName": "...",
 *   "author": "...",
 *   "gameType": "...",
 *   "bounds": { "minX": 0, "minY": 0, "minZ": 0, "maxX": 0, "maxY": 0, "maxZ": 0 },
 *   "dataPoints": { "DATA_NAME:SPAWN": [ { "x": 0, "y": 0, "z": 0 } ] }
 * }
 *
 * This class is intentionally schema-shape-aware but MapBuilder-independent.
 * If the schema shape changes, only this file needs updating in Shared.
 */
public class SchemaMapDefinition implements MapDefinition {

    private static final Gson GSON = new GsonBuilder().create();
    private static final Logger LOG = Logger.getLogger("SchemaMapDefinition");

    // ── Internal JSON-mirrored POJOs (no MapBuilder dependency) ─────────────

    private static class RawSchema {
        String mapName;
        String author;
        String gameType;
        RawBounds bounds;
        Map<String, List<RawPoint>> dataPoints;
        /** Null for legacy schemas that pre-date MB-09D. */
        Map<String, String> properties;
    }

    private static class RawBounds {
        int minX, minY, minZ, maxX, maxY, maxZ;
    }

    private static class RawPoint {
        int x, y, z;
    }

    // ── Parsed state ──────────────────────────────────────────────────────────

    private final String mapName;
    private final String author;
    private final String gameType;
    private final MapBoundingBox boundingBox;
    private final Map<MapPointType, List<MapPoint>> typedPoints;
    /** Keys are normalized: "DATA_NAME:TREASURE" → "TREASURE", matching DirectoryMapDataProvider. */
    private final Map<String, List<MapPoint>> customPoints;
    /**
     * Keys are the team name: "TEAM_NAME:RED" → "RED".
     * MapBuilder stores team spawns in dataPoints with TEAM_NAME:<name> keys.
     */
    private final Map<String, List<MapPoint>> teamSpawns;
    private final Map<String, String> properties;

    private SchemaMapDefinition(String mapName, String author, String gameType,
                                MapBoundingBox boundingBox,
                                Map<MapPointType, List<MapPoint>> typedPoints,
                                Map<String, List<MapPoint>> customPoints,
                                Map<String, List<MapPoint>> teamSpawns,
                                Map<String, String> properties) {
        this.mapName      = mapName;
        this.author       = author;
        this.gameType     = gameType;
        this.boundingBox  = boundingBox;
        this.typedPoints  = typedPoints;
        this.customPoints = customPoints;
        this.teamSpawns   = teamSpawns;
        this.properties   = properties != null ? properties : Collections.emptyMap();
    }

    // ── Factory ───────────────────────────────────────────────────────────────

    /**
     * Loads a SchemaMapDefinition from the schema.json in the given directory.
     * Returns null if the file does not exist or cannot be parsed.
     */
    public static SchemaMapDefinition loadFrom(File directory) {
        File file = new File(directory, "schema.json");
        if (!file.exists()) return null;

        try (Reader reader = new FileReader(file)) {
            RawSchema raw = GSON.fromJson(reader, RawSchema.class);
            if (raw == null || raw.mapName == null) return null;
            return parse(raw);
        } catch (IOException | JsonSyntaxException e) {
            LOG.warning("[SchemaMapDefinition] Failed to parse schema.json in "
                    + directory.getName() + ": " + e.getMessage());
            return null;
        }
    }

    private static SchemaMapDefinition parse(RawSchema raw) {
        // Bounding box
        MapBoundingBox bbox = null;
        if (raw.bounds != null) {
            bbox = new MapBoundingBox(
                    raw.bounds.minX, raw.bounds.minY, raw.bounds.minZ,
                    raw.bounds.maxX, raw.bounds.maxY, raw.bounds.maxZ);
        }

        Map<MapPointType, List<MapPoint>> typedPoints  = new HashMap<>();
        Map<String, List<MapPoint>>       customPoints = new HashMap<>();
        Map<String, List<MapPoint>>       teamSpawns   = new HashMap<>();

        if (raw.dataPoints != null) {
            for (Map.Entry<String, List<RawPoint>> entry : raw.dataPoints.entrySet()) {
                String rawKey = entry.getKey();  // e.g. "DATA_NAME:TREASURE", "TEAM_NAME:RED"
                List<MapPoint> pts = new ArrayList<>();
                if (entry.getValue() != null) {
                    for (RawPoint rp : entry.getValue()) {
                        pts.add(new MapPoint(rp.x, rp.y, rp.z));
                    }
                }

                // Split on first ':' to determine prefix and logical name
                int colon = rawKey.indexOf(':');
                String prefix = colon >= 0 ? rawKey.substring(0, colon).toUpperCase() : "";
                String name   = colon >= 0 ? rawKey.substring(colon + 1) : rawKey;

                if ("TEAM_NAME".equals(prefix)) {
                    // Team spawn — key is team name (e.g. "RED"), matching DirectoryMapDataProvider
                    teamSpawns.put(name, pts);
                } else {
                    // DATA_NAME or CUSTOM_NAME → normalize to stripped name key (e.g. "TREASURE")
                    // This matches what DirectoryMapDataProvider stores in customPoints.
                    customPoints.put(name, pts);

                    // Also map to typed enum where recognisable
                    MapPointType type = MapPointType.fromKey(name);
                    if (type != MapPointType.CUSTOM) {
                        typedPoints.computeIfAbsent(type, k -> new ArrayList<>()).addAll(pts);
                    }
                }
            }
        }

        // MB-09D: parse root properties — null-safe for legacy schemas
        Map<String, String> parsedProperties = new HashMap<>();
        if (raw.properties != null) {
            parsedProperties.putAll(raw.properties);
        }

        return new SchemaMapDefinition(
                raw.mapName  != null ? raw.mapName  : "Unknown",
                raw.author   != null ? raw.author   : "Unknown",
                raw.gameType != null ? raw.gameType : "UNKNOWN",
                bbox,
                typedPoints,
                customPoints,
                teamSpawns,
                parsedProperties);
    }

    // ── MapDefinition implementation ──────────────────────────────────────────

    @Override public String getMapName()  { return mapName; }
    @Override public String getAuthor()   { return author; }
    @Override public String getGameType() { return gameType; }
    @Override public Map<String, String> getProperties() { return properties; }

    @Override
    public List<MapPoint> getPoints(MapPointType type) {
        return typedPoints.getOrDefault(type, Collections.emptyList());
    }

    @Override
    public Optional<MapBoundingBox> getBoundingBox() {
        return Optional.ofNullable(boundingBox);
    }

    @Override
    public List<MapPoint> getCustomPoints(String key) {
        return customPoints.getOrDefault(key, Collections.emptyList());
    }

    @Override
    public List<MapPoint> getTeamSpawns(String teamName) {
        return teamSpawns.getOrDefault(teamName, Collections.emptyList());
    }

    @Override
    public Set<String> getTeamNames() {
        return teamSpawns.keySet();
    }

    @Override
    public Set<String> getCustomPointKeys() {
        return customPoints.keySet();
    }
}
