package com.houzicore.mapbuilder.template;

import com.houzicore.mapbuilder.domain.MapPointDefinition;

import java.util.Arrays;

/** Template for Hide-and-Seek maps. */
public final class HideAndSeekTemplate {
    private HideAndSeekTemplate() {}

    public static MapTemplate create() {
        return new MapTemplate(
            "HideAndSeek",
            "§dHide and Seek",
            true,
            Arrays.asList(
                MapPointDefinition.HIDER_SPAWN,
                MapPointDefinition.HUNTER_SPAWN
            ),
            Arrays.asList(
                MapPointDefinition.FENCE_WALL,
                MapPointDefinition.PIG_SPAWN,
                MapPointDefinition.SHEEP_SPAWN,
                MapPointDefinition.CHICKEN_SPAWN,
                MapPointDefinition.COW_SPAWN,
                MapPointDefinition.KIT_NPC,
                MapPointDefinition.TREASURE,
                MapPointDefinition.HOLO_LEADER,
                MapPointDefinition.HOLO_CUSTOM,
                MapPointDefinition.WATERFALL_EMITTER,
                MapPointDefinition.CUSTOM_DATA
            )
        );
    }
}
