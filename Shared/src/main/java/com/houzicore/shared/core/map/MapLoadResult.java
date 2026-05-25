package com.houzicore.shared.core.map;

import com.houzicore.shared.api.map.MapDefinition;

import java.util.Collections;
import java.util.List;

/**
 * Wraps the result of a HybridMapDataProvider load, including:
 * - the loaded MapDefinition (may be null for LOAD_FAILED)
 * - the load outcome classification
 * - parity warnings (empty unless schema+dat parity check ran and found differences)
 *
 * MB-09E: Used by callers that need outcome visibility (e.g. WorldData logging).
 * The underlying loadFromDirectory() method in MapDataProvider still returns
 * Optional<MapDefinition> for backward-compatible callers.
 */
public final class MapLoadResult {

    private final MapDefinition definition;
    private final MapLoadOutcome outcome;
    private final List<String> parityWarnings;

    MapLoadResult(MapDefinition definition, MapLoadOutcome outcome, List<String> parityWarnings) {
        this.definition     = definition;
        this.outcome        = outcome;
        this.parityWarnings = parityWarnings != null ? List.copyOf(parityWarnings) : Collections.emptyList();
    }

    /** The loaded MapDefinition. Null only when outcome is LOAD_FAILED. */
    public MapDefinition getDefinition() { return definition; }

    /** How the map was loaded. Never null. */
    public MapLoadOutcome getOutcome() { return outcome; }

    /** Parity warnings found when comparing schema and dat. Empty if schema_only or dat_fallback. */
    public List<String> getParityWarnings() { return parityWarnings; }

    public boolean hasParityWarnings() { return !parityWarnings.isEmpty(); }

    /** Convenience — true when no dat involvement at all. */
    public boolean isSchemaOnly() { return outcome == MapLoadOutcome.SCHEMA_ONLY; }

    // ── Static factories ──────────────────────────────────────────────────────

    static MapLoadResult schemaOnly(MapDefinition def) {
        return new MapLoadResult(def, MapLoadOutcome.SCHEMA_ONLY, Collections.emptyList());
    }

    /** SCHEMA_ONLY result that carries parity warnings from comparison with WorldConfig.dat. */
    static MapLoadResult schemaOnly(MapDefinition def, List<String> parityWarnings) {
        return new MapLoadResult(def, MapLoadOutcome.SCHEMA_ONLY, parityWarnings);
    }

    static MapLoadResult schemaWithSupplement(MapDefinition def, List<String> parityWarnings) {
        return new MapLoadResult(def, MapLoadOutcome.SCHEMA_PLUS_DAT_SUPPLEMENT, parityWarnings);
    }

    static MapLoadResult datFallback(MapDefinition def) {
        return new MapLoadResult(def, MapLoadOutcome.DAT_FALLBACK, Collections.emptyList());
    }

    static MapLoadResult failed() {
        return new MapLoadResult(null, MapLoadOutcome.LOAD_FAILED, Collections.emptyList());
    }
}
