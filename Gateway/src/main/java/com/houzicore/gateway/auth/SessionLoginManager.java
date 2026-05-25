package com.houzicore.gateway.auth;

import com.houzicore.gateway.GatewayPlugin;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * In-memory registry for session login (AuthMe-style).
 * Automatically logs players in if they connect with the same username and IP
 * within the configured timeout period.
 */
public class SessionLoginManager {

    private final GatewayPlugin plugin;
    private final Map<String, SessionRecord> sessions = new ConcurrentHashMap<>();

    public SessionLoginManager(GatewayPlugin plugin) {
        this.plugin = plugin;
    }

    /**
     * Creates or updates a session for a player.
     */
    public void createSession(String name, String ip) {
        if (!plugin.getGateConfig().sessionLoginEnabled()) return;
        sessions.put(name.toLowerCase(), new SessionRecord(ip, System.currentTimeMillis()));
    }

    /**
     * Checks if the player has a valid, non-expired session from the same IP.
     */
    public boolean isValidSession(String name, String ip) {
        if (!plugin.getGateConfig().sessionLoginEnabled()) {
            return false;
        }

        SessionRecord record = sessions.get(name.toLowerCase());
        if (record == null) {
            return false;
        }

        // Validate IP
        if (!record.ip.equals(ip)) {
            return false;
        }

        // Validate Timeout
        long timeoutMs = plugin.getGateConfig().sessionTimeoutMinutes() * 60_000L;
        long duration = System.currentTimeMillis() - record.timestamp;
        
        if (duration > timeoutMs) {
            sessions.remove(name.toLowerCase()); // Clear expired session
            return false;
        }

        return true;
    }

    /**
     * Explicitly invalidates/clears a session (e.g. on manual logout or quit).
     */
    public void invalidateSession(String name) {
        sessions.remove(name.toLowerCase());
    }

    private static class SessionRecord {
        final String ip;
        final long timestamp;

        SessionRecord(String ip, long timestamp) {
            this.ip = ip;
            this.timestamp = timestamp;
        }
    }
}
