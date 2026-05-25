package com.houzicore.shared.api.map;

import java.util.Optional;

/**
 * Loads and checks existence of compiled map definitions.
 *
 * This is the only entry-point Arcade and Lobby should use to access
 * map data. Implementation details (file-based, database-backed, etc.)
 * are hidden behind this contract.
 *
 * Current implementations are expected in:
 * - MapBuilder: FileBackedMapDataProvider (reads WorldConfig.dat, wraps in MapDefinition)
 * - Tests: InMemoryMapDataProvider
 */
public interface MapDataProvider {

    /**
     * Loads a map definition by name and game type.
     * Returns empty if the map does not exist or cannot be parsed.
     *
     * @param mapName  the human-readable map name (case-sensitive)
     * @param gameType the game type key the map was built for
     */
    Optional<MapDefinition> load(String mapName, String gameType);

    /**
     * Loads a map definition directly from an unzipped directory containing WorldConfig.dat.
     * Returns empty if the configuration file cannot be found or parsed.
     * 
     * @param directory the unzipped map directory
     */
    Optional<MapDefinition> loadFromDirectory(java.io.File directory);

    /**
     * Returns true if a map with the given name and game type exists and is readable.
     * Lightweight check — does not guarantee {@link #load} will succeed.
     *
     * @param mapName  the human-readable map name
     * @param gameType the game type key
     */
    boolean exists(String mapName, String gameType);
}
