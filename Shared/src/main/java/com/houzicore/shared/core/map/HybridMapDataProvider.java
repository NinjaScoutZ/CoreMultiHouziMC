package com.houzicore.shared.core.map;

import com.houzicore.shared.api.map.MapDataProvider;
import com.houzicore.shared.api.map.MapDefinition;

import java.io.File;
import java.util.List;
import java.util.Optional;
import java.util.logging.Logger;

/**
 * A MapDataProvider that tries schema.json first, then falls back to WorldConfig.dat.
 *
 * This is the primary provider that Lobby and Arcade should use. It guarantees:
 *   1. If schema.json is present and has properties   → SCHEMA_ONLY
 *   2. If schema.json is present but lacks properties → SCHEMA_PLUS_DAT_SUPPLEMENT + parity check
 *   3. If schema.json is absent or corrupted          → DAT_FALLBACK
 *   4. If neither exists                              → LOAD_FAILED
 *
 * MB-09E: Added loadWithResult() for callers that need load-mode visibility
 * (WorldData logging, retirement gate diagnostics). The loadFromDirectory()
 * method is kept for backward-compatible callers that only need Optional<MapDefinition>.
 */
public class HybridMapDataProvider implements MapDataProvider {

    private static final Logger LOG = Logger.getLogger("HybridMapDataProvider");

    private final SchemaMapDataProvider    schemaProvider  = new SchemaMapDataProvider();
    private final DirectoryMapDataProvider legacyProvider  = new DirectoryMapDataProvider();

    // ── MapDataProvider contract (backward-compatible) ────────────────────────

    @Override
    public Optional<MapDefinition> load(String mapName, String gameType) {
        File dir = new File("Maps/" + gameType + "/" + mapName);
        if (!dir.exists() || !dir.isDirectory()) return Optional.empty();
        return loadFromDirectory(dir);
    }

    @Override
    public Optional<MapDefinition> loadFromDirectory(File directory) {
        MapLoadResult result = loadWithResult(directory);
        return Optional.ofNullable(result.getDefinition());
    }

    @Override
    public boolean exists(String mapName, String gameType) {
        return schemaProvider.exists(mapName, gameType)
                || legacyProvider.exists(mapName, gameType);
    }

    // ── Extended API (MB-09E) ─────────────────────────────────────────────────

    /**
     * Loads the map and returns a full {@link MapLoadResult} that includes:
     * - the MapDefinition (null for LOAD_FAILED)
     * - the MapLoadOutcome classification
     * - any parity warnings (populated when both schema.json and WorldConfig.dat exist)
     *
     * Callers that only need a MapDefinition should use loadFromDirectory() instead.
     */
    public MapLoadResult loadWithResult(File directory) {
        Optional<MapDefinition> fromSchema = schemaProvider.loadFromDirectory(directory);

        if (fromSchema.isPresent()) {
            MapDefinition schemaDef = fromSchema.get();
            File datFile = new File(directory, "WorldConfig.dat");

            // Phase 2: schema has no properties → supplement from dat + run parity check
            if (schemaDef.getProperties().isEmpty() && datFile.exists()) {
                Optional<MapDefinition> fromDat = legacyProvider.loadFromDirectory(directory);

                List<String> parityWarnings = java.util.Collections.emptyList();
                if (fromDat.isPresent()) {
                    // Run parity diagnostics whenever both exist
                    parityWarnings = MapParityChecker.check(schemaDef, fromDat.get());

                    if (!fromDat.get().getProperties().isEmpty()) {
                        LOG.info("[HybridMapDataProvider] SCHEMA_PLUS_DAT_SUPPLEMENT for "
                                + directory.getName()
                                + (parityWarnings.isEmpty() ? " [parity OK]"
                                   : " [parity WARNINGS: " + parityWarnings.size() + "]"));
                        if (!parityWarnings.isEmpty()) {
                            parityWarnings.forEach(w ->
                                    LOG.warning("[HybridMapDataProvider] Parity warning for "
                                            + directory.getName() + ": " + w));
                        }
                        MapDefinition supplemented = new PropertiesSupplementedMapDefinition(
                                schemaDef, fromDat.get().getProperties());
                        return MapLoadResult.schemaWithSupplement(supplemented, parityWarnings);
                    }

                    // dat exists but has no properties either → schema_only but still run parity
                    if (!parityWarnings.isEmpty()) {
                        LOG.warning("[HybridMapDataProvider] Parity warnings for " + directory.getName()
                                + " (no supplement needed): " + parityWarnings.size());
                        parityWarnings.forEach(w ->
                                LOG.warning("[HybridMapDataProvider] Parity warning: " + w));
                    }
                }

                // Schema had no properties and dat had none or was absent → schema_only
                // Pass any parity warnings discovered so callers see the real picture
                LOG.fine("[HybridMapDataProvider] SCHEMA_ONLY (no properties in either source) for "
                        + directory.getName());
                return MapLoadResult.schemaOnly(schemaDef, parityWarnings);
            }

            LOG.fine("[HybridMapDataProvider] SCHEMA_ONLY for " + directory.getName());
            // Pass parity warnings if dat comparison ran (dat may or may not exist)
            List<String> schemaOnlyWarnings = java.util.Collections.emptyList();
            if (datFile.exists()) {
                Optional<MapDefinition> fromDat = legacyProvider.loadFromDirectory(directory);
                if (fromDat.isPresent()) {
                    schemaOnlyWarnings = MapParityChecker.check(schemaDef, fromDat.get());
                    if (!schemaOnlyWarnings.isEmpty()) {
                        LOG.warning("[HybridMapDataProvider] Parity warnings for SCHEMA_ONLY map "
                                + directory.getName() + ": " + schemaOnlyWarnings.size());
                        schemaOnlyWarnings.forEach(w ->
                                LOG.warning("[HybridMapDataProvider] Parity warning: " + w));
                    } else {
                        LOG.fine("[HybridMapDataProvider] SCHEMA_ONLY parity OK for " + directory.getName());
                    }
                }
            }
            return MapLoadResult.schemaOnly(schemaDef, schemaOnlyWarnings);
        }

        // Phase 3: schema absent/invalid → full fallback to WorldConfig.dat
        File datFile = new File(directory, "WorldConfig.dat");
        if (datFile.exists()) {
            Optional<MapDefinition> fromDat = legacyProvider.loadFromDirectory(directory);
            if (fromDat.isPresent()) {
                LOG.info("[HybridMapDataProvider] DAT_FALLBACK for " + directory.getName());
                return MapLoadResult.datFallback(fromDat.get());
            }
        }

        LOG.warning("[HybridMapDataProvider] LOAD_FAILED for " + directory.getName());
        return MapLoadResult.failed();
    }
}
