package com.houzicore.shared.core.map;

import com.houzicore.shared.api.map.MapBoundingBox;
import com.houzicore.shared.api.map.MapDataProvider;
import com.houzicore.shared.api.map.MapDefinition;
import com.houzicore.shared.api.map.MapPoint;
import com.houzicore.shared.api.map.MapPointType;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

/**
 * Wraps a primary MapDefinition (schema-first) and supplements its
 * getProperties() with values from a secondary source (dat-backed) when
 * the primary has an empty properties map.
 *
 * This is the Option A bridge for MB-09C.2: all locs/team/custom/bounds come
 * from the schema path; properties fall through to dat only when needed.
 *
 * No game code or MapBuilder code should ever import this class directly.
 * It is an implementation detail of HybridMapDataProvider.
 */
class PropertiesSupplementedMapDefinition implements MapDefinition {

    private final MapDefinition primary;
    private final Map<String, String> supplementedProperties;

    PropertiesSupplementedMapDefinition(MapDefinition primary, Map<String, String> supplementedProperties) {
        this.primary                  = primary;
        this.supplementedProperties   = supplementedProperties;
    }

    // ── All structural data comes from the schema-backed primary ──────────────

    @Override public String getMapName()  { return primary.getMapName(); }
    @Override public String getAuthor()   { return primary.getAuthor(); }
    @Override public String getGameType() { return primary.getGameType(); }

    @Override
    public List<MapPoint> getPoints(MapPointType type) {
        return primary.getPoints(type);
    }

    @Override
    public Optional<MapBoundingBox> getBoundingBox() {
        return primary.getBoundingBox();
    }

    @Override
    public List<MapPoint> getCustomPoints(String key) {
        return primary.getCustomPoints(key);
    }

    @Override
    public List<MapPoint> getTeamSpawns(String teamName) {
        return primary.getTeamSpawns(teamName);
    }

    @Override
    public Set<String> getTeamNames() {
        return primary.getTeamNames();
    }

    @Override
    public Set<String> getCustomPointKeys() {
        return primary.getCustomPointKeys();
    }

    // ── Properties: supplemented from dat when schema properties are empty ────

    @Override
    public Map<String, String> getProperties() {
        return supplementedProperties;
    }
}
