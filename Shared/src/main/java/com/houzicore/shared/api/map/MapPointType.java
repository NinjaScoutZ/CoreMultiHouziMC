package com.houzicore.shared.api.map;

/**
 * Canonical identifiers for typed point locations within a map.
 * Used by MapBuilder (to tag data points) and by Arcade/Lobby (to query them).
 *
 * New point types may be added here as the schema evolves.
 * Consumers MUST use the typed enum rather than raw string keys to avoid
 * schema drift between MapBuilder exports and game readers.
 */
public enum MapPointType {

    // ── Spawn / position points ──────────────────────────────────────────────
    /** Generic spawn fallback (used when team-specific spawns are not defined). */
    SPAWN,

    // ── Arena data points ───────────────────────────────────────────────────
    /** Centre of the arena zone used for boundary checks and effect origins. */
    ZONE_ARENA,
    ARENA_SPAWN_A,
    ARENA_SPAWN_B,

    // ── Flag / objective points ─────────────────────────────────────────────
    FLAG_POST,

    // ── Structure / decoration points ───────────────────────────────────────
    BLOCK_DISPLAY,
    WATERFALL_EMITTER,

    // ── Custom / game-specific points ───────────────────────────────────────
    /**
     * Catch-all for game-specific points that have not yet been promoted to
     * a named entry. Avoid relying on CUSTOM in production paths.
     */
    CUSTOM;

    /**
     * Resolves a raw string key (as stored in MapSchema.dataPoints) back to
     * a MapPointType. Returns CUSTOM for unknown keys instead of throwing.
     *
     * Format expected: "DATA_NAME:ZONE_ARENA" → ZONE_ARENA,
     *                  "CUSTOM_NAME:FLAG_POST" → FLAG_POST, etc.
     */
    public static MapPointType fromKey(String rawKey) {
        if (rawKey == null) return CUSTOM;
        String stripped = rawKey.contains(":") ? rawKey.substring(rawKey.indexOf(':') + 1) : rawKey;
        try {
            return MapPointType.valueOf(stripped.toUpperCase());
        } catch (IllegalArgumentException e) {
            return CUSTOM;
        }
    }
}
