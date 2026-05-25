package com.houzicore.shared.core.map;

/**
 * Classifies how a map was loaded by HybridMapDataProvider.
 *
 * MB-09E: Used for diagnostics, logging, and retirement gate decisions.
 * Must never gate gameplay — used only for reporting.
 */
public enum MapLoadOutcome {

    /**
     * All data (including properties) came from schema.json alone.
     * This is the expected mode for maps exported after MB-09D.
     * When all maps report SCHEMA_ONLY, dat supplement can be retired.
     */
    SCHEMA_ONLY,

    /**
     * Structural data (locs, teams, bounds) came from schema.json,
     * but properties were supplemented from WorldConfig.dat because
     * the schema had none. Expected for legacy schemas pre-MB-09D.
     */
    SCHEMA_PLUS_DAT_SUPPLEMENT,

    /**
     * schema.json was absent or invalid. Full load from WorldConfig.dat.
     * Expected for very old maps that were never re-exported.
     */
    DAT_FALLBACK,

    /**
     * Neither schema.json nor WorldConfig.dat could be loaded.
     * Map data is unavailable.
     */
    LOAD_FAILED
}
