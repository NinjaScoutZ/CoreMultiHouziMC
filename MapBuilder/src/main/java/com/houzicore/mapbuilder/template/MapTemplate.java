package com.houzicore.mapbuilder.template;

import com.houzicore.mapbuilder.domain.MapPointDefinition;
import com.houzicore.mapbuilder.domain.PointCategory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * Describes what a particular GameType requires and allows.
 * Used by PointPaletteGui to filter the visible palette and by
 * MapValidationService to check completeness.
 */
public final class MapTemplate {

    private final String gameType;
    private final String displayName;
    private final boolean requiresBoundary;

    /** Points that MUST be placed (minCount > 0 per definition, or overridden here). */
    private final List<MapPointDefinition> requiredPoints;

    /** Points that MAY be placed but are not validated as mandatory. */
    private final List<MapPointDefinition> optionalPoints;

    public MapTemplate(String gameType,
                       String displayName,
                       boolean requiresBoundary,
                       List<MapPointDefinition> requiredPoints,
                       List<MapPointDefinition> optionalPoints) {
        this.gameType        = gameType;
        this.displayName     = displayName;
        this.requiresBoundary= requiresBoundary;
        this.requiredPoints  = Collections.unmodifiableList(new ArrayList<>(requiredPoints));
        this.optionalPoints  = Collections.unmodifiableList(new ArrayList<>(optionalPoints));
    }

    public String getGameType()         { return gameType; }
    public String getDisplayName()      { return displayName; }
    public boolean requiresBoundary()   { return requiresBoundary; }
    public List<MapPointDefinition> getRequiredPoints() { return requiredPoints; }
    public List<MapPointDefinition> getOptionalPoints() { return optionalPoints; }

    /** All points defined for this template (required + optional). */
    public List<MapPointDefinition> getAllPoints() {
        List<MapPointDefinition> all = new ArrayList<>(requiredPoints);
        all.addAll(optionalPoints);
        return all;
    }

    /** Filter all points by category for palette display. */
    public List<MapPointDefinition> byCategory(PointCategory cat) {
        List<MapPointDefinition> out = new ArrayList<>();
        for (MapPointDefinition def : getAllPoints()) {
            if (def.category == cat) out.add(def);
        }
        return out;
    }

    /** Template categories in display order, excluding empty categories. */
    public List<PointCategory> getCategories() {
        Set<PointCategory> categories = new LinkedHashSet<>();
        for (MapPointDefinition def : getAllPoints()) {
            categories.add(def.category);
        }
        return new ArrayList<>(categories);
    }

    public boolean isRequired(MapPointDefinition def) {
        return requiredPoints.contains(def);
    }
}
