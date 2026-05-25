package com.houzicore.shared.api.loadout;

/**
 * Canonical identifiers for loadout profiles used across the HouziCore network.
 * These profiles are mapped to actual item sets within the PlayerStateApplier implementations.
 */
public final class SharedLoadoutProfiles {

    public static final LoadoutProfile LOBBY_MAIN = LoadoutProfile.of("lobby_main");
    public static final LoadoutProfile LOBBY_ARENA_DUEL = LoadoutProfile.of("lobby_arena_duel");
    public static final LoadoutProfile LOBBY_SOCIAL = LoadoutProfile.of("lobby_social");
    public static final LoadoutProfile LOBBY_ACTIVITY = LoadoutProfile.of("lobby_activity_tools");
    public static final LoadoutProfile LOBBY_FISHING = LoadoutProfile.of("lobby_fishing");
    public static final LoadoutProfile LOBBY_FARM = LoadoutProfile.of("lobby_farm");
    public static final LoadoutProfile LOBBY_PARKOUR = LoadoutProfile.of("lobby_parkour");
    public static final LoadoutProfile EMPTY_LOADOUT = LoadoutProfile.of("empty_loadout");
    
    public static final LoadoutProfile ARCADE_LOBBY = LoadoutProfile.of("arcade_lobby");
    public static final LoadoutProfile ARCADE_SPECTATOR = LoadoutProfile.of("arcade_spectator");
    public static final LoadoutProfile ARCADE_POSTGAME = LoadoutProfile.of("arcade_postgame");
    
    public static final LoadoutProfile MAP_EDITOR = LoadoutProfile.of("map_editor");

    private SharedLoadoutProfiles() {
        // Utility class
    }
}
