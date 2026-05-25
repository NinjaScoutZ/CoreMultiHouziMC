package com.houzicore.shared.api.map;

/**
 * Canonical identifiers for named spatial regions within a map.
 * Used by game rules to enforce safe zones, exclusion zones, or other
 * region-based behaviour without coupling to world-specific coordinates.
 */
public enum MapRegionType {

    /** The primary bounded play area. Used for border enforcement and respawn validation. */
    BOUNDING_BOX,

    /** Players are prevented from entering or spawning inside these regions. */
    EXCLUSION_ZONE,

    /** Damage, PvP, and harmful effects are suppressed inside these regions. */
    SAFE_ZONE;
}
