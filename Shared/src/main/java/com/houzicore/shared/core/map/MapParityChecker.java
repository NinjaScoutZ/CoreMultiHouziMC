package com.houzicore.shared.core.map;

import com.houzicore.shared.api.map.MapDefinition;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Compares a schema-derived MapDefinition against a dat-derived MapDefinition
 * for parity verification.
 *
 * MB-09E: Diagnostic tool only. Never blocks loading. Used by
 * HybridMapDataProvider when both schema.json and WorldConfig.dat coexist,
 * so operators can see if they diverge after export.
 *
 * Surfaces checked:
 *  - map name
 *  - author
 *  - bounds (minX/maxX/minY/maxY/minZ/maxZ)
 *  - team names (set equality)
 *  - team spawn counts per team
 *  - custom/data point keys (set equality)
 *  - custom/data point counts per key
 *  - property keys (set equality)
 *  - property values per key
 */
public final class MapParityChecker {

    private MapParityChecker() {}

    /**
     * Runs a full parity check between schema and dat definitions.
     * Returns a list of human-readable warning strings.
     * Returns empty list if everything matches.
     */
    public static List<String> check(MapDefinition schema, MapDefinition dat) {
        List<String> warnings = new ArrayList<>();

        // Map name
        if (!schema.getMapName().equals(dat.getMapName())) {
            warnings.add("mapName mismatch: schema='" + schema.getMapName()
                    + "' dat='" + dat.getMapName() + "'");
        }

        // Author
        if (!schema.getAuthor().equals(dat.getAuthor())) {
            warnings.add("author mismatch: schema='" + schema.getAuthor()
                    + "' dat='" + dat.getAuthor() + "'");
        }

        // Bounding box
        boolean schemaHasBounds = schema.getBoundingBox().isPresent();
        boolean datHasBounds    = dat.getBoundingBox().isPresent();
        if (schemaHasBounds != datHasBounds) {
            warnings.add("bounds presence mismatch: schema=" + schemaHasBounds + " dat=" + datHasBounds);
        } else if (schemaHasBounds) {
            var sb = schema.getBoundingBox().get();
            var db = dat.getBoundingBox().get();
            if (sb.getMinX() != db.getMinX() || sb.getMaxX() != db.getMaxX()
                    || sb.getMinY() != db.getMinY() || sb.getMaxY() != db.getMaxY()
                    || sb.getMinZ() != db.getMinZ() || sb.getMaxZ() != db.getMaxZ()) {
                warnings.add("bounds mismatch: schema=["
                        + sb.getMinX() + "," + sb.getMinY() + "," + sb.getMinZ() + " → "
                        + sb.getMaxX() + "," + sb.getMaxY() + "," + sb.getMaxZ() + "] dat=["
                        + db.getMinX() + "," + db.getMinY() + "," + db.getMinZ() + " → "
                        + db.getMaxX() + "," + db.getMaxY() + "," + db.getMaxZ() + "]");
            }
        }

        // Team names
        Set<String> schemaTeams = schema.getTeamNames();
        Set<String> datTeams    = dat.getTeamNames();
        if (!schemaTeams.equals(datTeams)) {
            warnings.add("teamNames mismatch: schema=" + schemaTeams + " dat=" + datTeams);
        }
        // Team spawn counts
        for (String team : schemaTeams) {
            int sc = schema.getTeamSpawns(team).size();
            int dc = dat.getTeamSpawns(team).size();
            if (sc != dc) {
                warnings.add("teamSpawns[" + team + "] count mismatch: schema=" + sc + " dat=" + dc);
            }
        }

        // Custom/data point keys
        Set<String> schemaKeys = schema.getCustomPointKeys();
        Set<String> datKeys    = dat.getCustomPointKeys();
        if (!schemaKeys.equals(datKeys)) {
            Set<String> extraInSchema = new java.util.HashSet<>(schemaKeys);
            extraInSchema.removeAll(datKeys);
            Set<String> extraInDat = new java.util.HashSet<>(datKeys);
            extraInDat.removeAll(schemaKeys);
            if (!extraInSchema.isEmpty()) warnings.add("customPointKeys only in schema: " + extraInSchema);
            if (!extraInDat.isEmpty())    warnings.add("customPointKeys only in dat: "    + extraInDat);
        }
        // Custom point counts
        for (String key : schemaKeys) {
            int sc = schema.getCustomPoints(key).size();
            int dc = dat.getCustomPoints(key).size();
            if (sc != dc) {
                warnings.add("customPoints[" + key + "] count mismatch: schema=" + sc + " dat=" + dc);
            }
        }

        // Properties
        Set<String> schemaPropKeys = schema.getProperties().keySet();
        Set<String> datPropKeys    = dat.getProperties().keySet();
        if (!schemaPropKeys.equals(datPropKeys)) {
            Set<String> extraInSchema = new java.util.HashSet<>(schemaPropKeys);
            extraInSchema.removeAll(datPropKeys);
            Set<String> extraInDat = new java.util.HashSet<>(datPropKeys);
            extraInDat.removeAll(schemaPropKeys);
            if (!extraInSchema.isEmpty()) warnings.add("properties only in schema: " + extraInSchema);
            if (!extraInDat.isEmpty())    warnings.add("properties only in dat: "    + extraInDat);
        }
        for (String key : schemaPropKeys) {
            String sv = schema.getProperties().get(key);
            String dv = dat.getProperties().get(key);
            if (dv != null && !sv.equals(dv)) {
                warnings.add("property[" + key + "] mismatch: schema='" + sv + "' dat='" + dv + "'");
            }
        }

        return warnings;
    }
}
