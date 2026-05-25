package com.houzicore.mapbuilder.template;

import com.houzicore.mapbuilder.domain.MapPointDefinition;

import java.util.Arrays;
import java.util.Collections;

/**
 * Fallback template for any GameType not explicitly registered.
 * Exposes all known point types as optional, requires only a boundary.
 */
public final class GenericTemplate {
    private GenericTemplate() {}

    public static MapTemplate create(String gameType) {
        return new MapTemplate(
            gameType,
            "§7Generic (" + gameType + ")",
            false,
            Collections.emptyList(),
            Arrays.asList(MapPointDefinition.values())
        );
    }
}
