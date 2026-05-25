package com.houzicore.mapbuilder.schema;

import com.houzicore.mapbuilder.MapSession;
import org.bukkit.Location;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Converts a MapSession into a MapSchema and serialises it to the legacy
 * WorldConfig.dat string format.
 *
 * The serialised output is byte/line identical to the old WorldConfigExporter
 * buildConfigContent() method — this is contractually required so that Arcade
 * ParseData continues to work without modification.
 */
public final class MapSchemaExporter {

    private MapSchemaExporter() {}

    // -------------------------------------------------------------------------
    // Session → Schema
    // -------------------------------------------------------------------------

    public static MapSchema build(MapSession session) {
        MapSchema.BoundingBox bounds = null;
        Location min = session.getMinBoundary();
        Location max = session.getMaxBoundary();

        if (min != null && max != null) {
            bounds = new MapSchema.BoundingBox(
                    Math.min(min.getBlockX(), max.getBlockX()),
                    Math.min(min.getBlockY(), max.getBlockY()),
                    Math.min(min.getBlockZ(), max.getBlockZ()),
                    Math.max(min.getBlockX(), max.getBlockX()),
                    Math.max(min.getBlockY(), max.getBlockY()),
                    Math.max(min.getBlockZ(), max.getBlockZ())
            );
        }

        Map<String, List<MapSchema.SchemaPoint>> schemaPoints = new LinkedHashMap<>();
        for (Map.Entry<String, List<Location>> entry : session.getDataPoints().entrySet()) {
            List<MapSchema.SchemaPoint> points = new ArrayList<>();
            for (Location loc : entry.getValue()) {
                points.add(new MapSchema.SchemaPoint(loc.getBlockX(), loc.getBlockY(), loc.getBlockZ()));
            }
            schemaPoints.put(entry.getKey(), points);
        }

        return new MapSchema(
                session.getMapName(),
                session.getAuthor(),
                session.getGameType(),
                bounds,
                schemaPoints,
                new java.util.HashMap<>(session.getProperties())
        );
    }

    // -------------------------------------------------------------------------
    // Schema → String (legacy-compatible serialisation)
    // -------------------------------------------------------------------------

    /**
     * Serialises to the exact same format as the legacy WorldConfigExporter.buildConfigContent().
     * Any change here will break Arcade ParseData — guarded by WorldConfigCompatibilityTest.
     */
    public static String serializeToString(MapSchema schema) {
        StringBuilder sb = new StringBuilder();

        // Map metadata
        sb.append("MAP_NAME:").append(schema.getMapName()).append("\n");
        sb.append("MAP_AUTHOR:").append(schema.getAuthor()).append("\n");

        // Map bounds
        MapSchema.BoundingBox b = schema.getBounds();
        if (b != null) {
            sb.append("MIN_X:").append(b.minX).append("\n");
            sb.append("MAX_X:").append(b.maxX).append("\n");
            sb.append("MIN_Z:").append(b.minZ).append("\n");
            sb.append("MAX_Z:").append(b.maxZ).append("\n");
            sb.append("MIN_Y:").append(b.minY).append("\n");
            sb.append("MAX_Y:").append(b.maxY).append("\n");
        }

        // Data points — format preserved exactly from legacy exporter
        for (Map.Entry<String, List<MapSchema.SchemaPoint>> entry : schema.getDataPoints().entrySet()) {
            String type = entry.getKey();
            List<MapSchema.SchemaPoint> points = entry.getValue();

            if (type.equals("TEAM_NAME:Blue") || type.equals("TEAM_NAME:Red")) {
                sb.append(type).append("\n");
                sb.append("TEAM_DIRECTION:").append(type.equals("TEAM_NAME:Blue") ? "0" : "180").append("\n");
                sb.append("TEAM_SPAWNS:");
                appendPoints(sb, points);
                sb.append("\n");
            } else if (type.startsWith("DATA_NAME:")) {
                sb.append(type).append("\n");
                sb.append("DATA_LOCS:");
                appendPoints(sb, points);
                sb.append("\n");
            } else if (type.startsWith("CUSTOM_NAME:")) {
                sb.append(type).append("\n");
                sb.append("CUSTOM_LOCS:");
                appendPoints(sb, points);
                sb.append("\n");
            } else if (type.startsWith("BLOCK_DISPLAY:")) {
                sb.append(type).append("\n");
                sb.append("BLOCK_DISPLAY_LOCS:");
                appendPoints(sb, points);
                sb.append("\n");
            }
        }

        // Properties (MB-09D) — written as bare KEY:VALUE lines after data points
        // Keys must not collide with reserved dat keys (callers validate before export)
        for (java.util.Map.Entry<String, String> entry : schema.getProperties().entrySet()) {
            sb.append(entry.getKey()).append(":").append(entry.getValue()).append("\n");
        }

        return sb.toString();
    }

    private static void appendPoints(StringBuilder sb, List<MapSchema.SchemaPoint> points) {
        for (int i = 0; i < points.size(); i++) {
            MapSchema.SchemaPoint p = points.get(i);
            sb.append(p.x).append(",").append(p.y).append(",").append(p.z);
            if (i < points.size() - 1) sb.append(":");
        }
    }
}
