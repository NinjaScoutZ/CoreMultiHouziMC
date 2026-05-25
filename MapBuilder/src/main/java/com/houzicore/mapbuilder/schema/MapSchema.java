package com.houzicore.mapbuilder.schema;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Typed, immutable representation of a complete map definition.
 * This is the single source of truth for export — WorldConfigExporter serialises
 * FROM this schema, not directly from MapSession fields.
 *
 * Backward-compat note: the serialised format (WorldConfig.dat) must remain
 * byte/line identical to the legacy buildConfigContent() output so that
 * Arcade-side ParseData continues to read correctly without any change.
 *
 * MB-09D: Added first-class `properties` field. Old schema.json files that
 * lack this field will deserialise with null → treated as empty by callers.
 */
public final class MapSchema {

    /**
     * Keys that are reserved by the WorldConfig.dat format and MUST NOT be
     * used as custom property names. Export will reject or warn on these.
     */
    public static final Set<String> RESERVED_DAT_KEYS = Set.of(
            "MAP_NAME", "MAP_AUTHOR",
            "MIN_X", "MAX_X", "MIN_Y", "MAX_Y", "MIN_Z", "MAX_Z",
            "TEAM_NAME", "TEAM_DIRECTION", "TEAM_SPAWNS",
            "DATA_NAME", "DATA_LOCS",
            "CUSTOM_NAME", "CUSTOM_LOCS",
            "BLOCK_DISPLAY", "BLOCK_DISPLAY_LOCS"
    );

    /**
     * Returns true if the key is reserved and MUST NOT be used as a custom property.
     *
     * Blocks both:
     *  - exact reserved keys: e.g. "MAP_NAME"
     *  - structured-prefix keys: e.g. "TEAM_NAME:RED", "DATA_NAME:SPAWN", "CUSTOM_NAME:X"
     *    (prefix before the first ':' is in RESERVED_DAT_KEYS)
     */
    public static boolean isReservedKey(String key) {
        if (key == null) return true;
        String upper = key.toUpperCase();
        if (RESERVED_DAT_KEYS.contains(upper)) return true;
        int colonIdx = upper.indexOf(':');
        if (colonIdx > 0) {
            String prefix = upper.substring(0, colonIdx);
            return RESERVED_DAT_KEYS.contains(prefix);
        }
        return false;
    }


    private final String mapName;
    private final String author;
    private final String gameType;
    private final BoundingBox bounds;
    private final Map<String, List<SchemaPoint>> dataPoints;
    /** Arbitrary string properties (e.g. DISGUISE_TYPE=SKELETON). May be null for legacy schemas. */
    private final Map<String, String> properties;

    public MapSchema(String mapName, String author, String gameType,
                     BoundingBox bounds, Map<String, List<SchemaPoint>> dataPoints) {
        this(mapName, author, gameType, bounds, dataPoints, Collections.emptyMap());
    }

    public MapSchema(String mapName, String author, String gameType,
                     BoundingBox bounds, Map<String, List<SchemaPoint>> dataPoints,
                     Map<String, String> properties) {
        this.mapName    = mapName;
        this.author     = author;
        this.gameType   = gameType;
        this.bounds     = bounds;
        this.dataPoints = Collections.unmodifiableMap(dataPoints);
        this.properties = properties != null ? Collections.unmodifiableMap(properties) : Collections.emptyMap();
    }

    public String getMapName()    { return mapName; }
    public String getAuthor()     { return author; }
    public String getGameType()   { return gameType; }
    public BoundingBox getBounds(){ return bounds; }
    public Map<String, List<SchemaPoint>> getDataPoints() { return dataPoints; }
    /** Never null — returns empty map for legacy schemas. */
    public Map<String, String> getProperties() { return properties; }

    // -------------------------------------------------------------------------

    public static final class BoundingBox {
        public final int minX, minY, minZ, maxX, maxY, maxZ;

        public BoundingBox(int minX, int minY, int minZ, int maxX, int maxY, int maxZ) {
            this.minX = minX; this.minY = minY; this.minZ = minZ;
            this.maxX = maxX; this.maxY = maxY; this.maxZ = maxZ;
        }
    }

    public static final class SchemaPoint {
        public final int x, y, z;

        public SchemaPoint(int x, int y, int z) {
            this.x = x; this.y = y; this.z = z;
        }
    }
}

