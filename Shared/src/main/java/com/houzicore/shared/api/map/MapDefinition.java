package com.houzicore.shared.api.map;

import java.util.List;
import java.util.Optional;

/**
 * Shared contract for a fully-compiled map definition.
 *
 * This interface is the single source of truth that Arcade ParseData and
 * Lobby map readers must depend on. Neither consumers must never import
 * MapBuilder-internal classes (MapSchema, MapSchemaExporter) directly.
 *
 * A MapDefinition is produced by MapBuilder, stored/loaded by MapDataProvider,
 * and consumed by Arcade game initialisation and Lobby map-aware systems.
 */
public interface MapDefinition {

    /** The human-readable map name (e.g. "Dusty Dunes"). */
    String getMapName();

    /** The username of the player who built the map. */
    String getAuthor();

    /**
     * The game type key this map was built for (e.g. "ARENA", "BRIDGES").
     * Must match the GameType identifiers used by Arcade ParseData.
     */
    String getGameType();

    /**
     * Returns all labelled points of the given type.
     * Returns an empty list (never null) if no points of this type exist.
     *
     * @param type the point type to query
     */
    List<MapPoint> getPoints(MapPointType type);

    /**
     * Returns the primary playable bounding box, if defined.
     * Maps without explicit boundaries return Optional.empty().
     */
    Optional<MapBoundingBox> getBoundingBox();

    /**
     * Convenience: returns the first point of the given type, if any.
     * Commonly used for single-point lookups (e.g. ZONE_ARENA centre).
     */
    default Optional<MapPoint> getFirstPoint(MapPointType type) {
        List<MapPoint> pts = getPoints(type);
        return pts.isEmpty() ? Optional.empty() : Optional.of(pts.get(0));
    }

    /**
     * Returns all custom data points associated with the given string key
     * (e.g. "RED", "54", "IRON_BLOCK").
     * Returns an empty list if no points exist for this key.
     */
    List<MapPoint> getCustomPoints(String key);

    /**
     * Returns all team spawns associated with the given team name.
     * Returns an empty list if no team spawns exist for this team.
     */
    List<MapPoint> getTeamSpawns(String teamName);

    /**
     * Returns a map of custom string properties from the map definition.
     */
    java.util.Map<String, String> getProperties();

    /**
     * Returns a set of all team names defined in this map.
     */
    java.util.Set<String> getTeamNames();

    /**
     * Returns a set of all custom point keys defined in this map.
     */
    java.util.Set<String> getCustomPointKeys();
}
