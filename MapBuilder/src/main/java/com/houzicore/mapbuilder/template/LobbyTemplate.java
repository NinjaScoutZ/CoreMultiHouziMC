package com.houzicore.mapbuilder.template;

import com.houzicore.mapbuilder.domain.MapPointDefinition;

import java.util.Arrays;
import java.util.Collections;

/** Template for the Lobby server map. */
public final class LobbyTemplate {
    private LobbyTemplate() {}

    public static MapTemplate create() {
        return new MapTemplate(
            "Lobby",
            "§6Lobby",
            true,
            Arrays.asList(
                MapPointDefinition.LOBBY_SPAWN,
                MapPointDefinition.ARENA_CENTER,
                MapPointDefinition.ARENA_SPAWN_A,
                MapPointDefinition.ARENA_SPAWN_B,
                MapPointDefinition.NPC_FISHING,
                MapPointDefinition.NPC_PARKOUR,
                MapPointDefinition.NPC_ARENA,
                MapPointDefinition.NPC_FARM,
                MapPointDefinition.FISHING_ZONE,
                MapPointDefinition.PARKOUR_ZONE,
                MapPointDefinition.FARM_ZONE
            ),
            Arrays.asList(
                MapPointDefinition.TREASURE,
                MapPointDefinition.HOLO_LEADER,
                MapPointDefinition.HOLO_CUSTOM,
                MapPointDefinition.WATERFALL_EMITTER,
                MapPointDefinition.CUSTOM_DATA
            )
        );
    }
}
