package com.houzicore.lobby.hub.server;

public enum ServerStatusType {
    STARTING,
    IN_PROGRESS,
    OFFLINE,
    UNKNOWN;

    public static ServerStatusType parse(String motd) {
        if (motd == null) return UNKNOWN;
        String lower = motd.toLowerCase();
        if (lower.contains("starting") || lower.contains("recruiting") ||
            lower.contains("generating") || lower.contains("waiting") ||
            lower.contains("open") || lower.contains("voting") || lower.contains("cup")) {
            return STARTING;
        }
        if (lower.contains("progress") || lower.contains("restarting")) {
            return IN_PROGRESS;
        }
        if (lower.contains("offline")) {
            return OFFLINE;
        }
        return UNKNOWN;
    }
}
