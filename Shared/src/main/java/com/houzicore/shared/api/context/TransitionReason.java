package com.houzicore.shared.api.context;

public enum TransitionReason {
    JOIN,
    QUIT,
    COMMAND,
    LOBBY_ENTER,
    LOBBY_EXIT,
    ARENA_JOIN,
    ARENA_LEAVE,
    GAME_START,
    GAME_END,
    PLAYER_DEATH,
    PLAYER_RESPAWN,
    SPECTATE,
    RETURN_TO_LOBBY,
    MAP_EDIT_START,
    MAP_EDIT_END,
    SYSTEM
}
