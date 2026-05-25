package com.houzicore.gateway.auth;

import com.houzicore.gateway.GatewayPlugin;

import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Manages in-memory AuthSession objects and the login-timeout timer per player.
 */
public class AuthSessionManager {

    private final GatewayPlugin plugin;
    private final Map<UUID, AuthSession> sessions = new HashMap<>();
    private final Map<UUID, Integer> timeoutTasks = new HashMap<>();

    public AuthSessionManager(GatewayPlugin plugin) {
        this.plugin = plugin;
    }

    // -----------------------------------------------------------------------
    // Session lifecycle
    // -----------------------------------------------------------------------

    /** Called on PlayerJoinEvent after premium check is complete. */
    public AuthSession open(Player player, AuthSession.State initialState) {
        String ip = player.getAddress() != null ? player.getAddress().getAddress().getHostAddress() : "unknown";
        AuthSession session = new AuthSession(player.getName(), ip);
        session.setState(initialState);
        sessions.put(player.getUniqueId(), session);
        scheduleTimeout(player);
        return session;
    }

    public void close(UUID uuid) {
        sessions.remove(uuid);
        cancelTimeout(uuid);
    }

    public void clearAll() {
        sessions.clear();
        timeoutTasks.forEach((uuid, taskId) -> plugin.getServer().getScheduler().cancelTask(taskId));
        timeoutTasks.clear();
    }

    // -----------------------------------------------------------------------
    // Getters
    // -----------------------------------------------------------------------

    /** Returns null if no session (e.g. player already authed or admin). */
    public AuthSession get(Player player) {
        return sessions.get(player.getUniqueId());
    }

    public boolean isAuthenticated(Player player) {
        AuthSession s = sessions.get(player.getUniqueId());
        return s == null || s.isAuthenticated();  // null = no session guard = allowed
    }

    // -----------------------------------------------------------------------
    // Timeout
    // -----------------------------------------------------------------------

    private void scheduleTimeout(Player player) {
        int secs = plugin.getGateConfig().loginTimeoutSeconds();
        if (secs <= 0) return;

        int taskId = new BukkitRunnable() {
            @Override
            public void run() {
                if (!player.isOnline()) return;
                AuthSession s = sessions.get(player.getUniqueId());
                if (s != null && !s.isAuthenticated()) {
                    player.kickPlayer(plugin.getGateConfig().timeoutKick());
                }
            }
        }.runTaskLater(plugin, secs * 20L).getTaskId();

        timeoutTasks.put(player.getUniqueId(), taskId);
    }

    private void cancelTimeout(UUID uuid) {
        Integer taskId = timeoutTasks.remove(uuid);
        if (taskId != null) plugin.getServer().getScheduler().cancelTask(taskId);
    }

    /** Call this when auth is completed to cancel the timeout. */
    public void markAuthenticated(Player player) {
        AuthSession s = get(player);
        if (s != null) s.setState(AuthSession.State.AUTHENTICATED);
        cancelTimeout(player.getUniqueId());
    }
}
