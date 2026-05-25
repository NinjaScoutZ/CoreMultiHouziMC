package com.houzicore.bungeecord.listener;

import net.md_5.bungee.api.connection.PendingConnection;
import net.md_5.bungee.api.event.PreLoginEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.api.plugin.Plugin;
import net.md_5.bungee.event.EventHandler;

import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URI;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import com.houzicore.shared.core.database.DBPool;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.UUID;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;

/**
 * Intercepts connection early on BungeeCord proxy.
 * If player username exists on Mojang servers (premium account),
 * we force the connection to use online-mode (handshake with Mojang).
 * Cracked players bypass and connect in offline-mode.
 */
public class PremiumBypassListener implements Listener {

    private static final String MOJANG_URL = "https://api.mojang.com/users/profiles/minecraft/";
    private static final long CACHE_TTL_MS = 300_000L; // Cache results for 5 minutes

    private final Plugin plugin;
    private final Map<String, CacheEntry> cache = new ConcurrentHashMap<>();

    public PremiumBypassListener(Plugin plugin) {
        this.plugin = plugin;
        plugin.getProxy().getPluginManager().registerListener(plugin, this);
        plugin.getLogger().info("PremiumBypassListener initialized.");
    }

    @EventHandler
    public void onPreLogin(PreLoginEvent event) {
        PendingConnection conn = event.getConnection();
        String name = conn.getName();

        if (name == null || name.isEmpty()) return;

        String key = name.toLowerCase();
        long now = System.currentTimeMillis();

        CacheEntry cached = cache.get(key);
        if (cached != null && (now - cached.timestamp) < CACHE_TTL_MS) {
            if (cached.premium) {
                conn.setOnlineMode(true);
                plugin.getLogger().info("[PremiumBypass] " + name + " verified via cached premium status.");
            }
            return;
        }

        // Register async intent to hold the connection
        event.registerIntent(plugin);

        plugin.getProxy().getScheduler().runAsync(plugin, () -> {
            boolean premium = false;
            
            // 1. Check if the player already has a registered account in our database
            String offlineUuidStr = getOfflineUuid(name);
            if (offlineUuidStr != null && hasOfflineAccountInDatabase(name, offlineUuidStr)) {
                // Stored UUID is offline, which means they are a registered cracked account.
                plugin.getLogger().info("[PremiumBypass] " + name + " matches registered cracked account in DB. Bypassing online-mode.");
                premium = false;
            } else if (offlineUuidStr != null && hasPremiumAccountInDatabase(name, offlineUuidStr)) {
                // Stored UUID is online/premium. Force online-mode.
                plugin.getLogger().info("[PremiumBypass] " + name + " matches registered premium account in DB. Forcing online-mode.");
                premium = true;
            } else {
                // Not in database. Query Mojang API to see if they are a premium user.
                try {
                    URI uri = URI.create(MOJANG_URL + name);
                    HttpURLConnection http = (HttpURLConnection) uri.toURL().openConnection();
                    http.setRequestMethod("GET");
                    http.setConnectTimeout(3000);
                    http.setReadTimeout(3000);

                    int responseCode = http.getResponseCode();
                    if (responseCode == 200) {
                        try (InputStreamReader reader = new InputStreamReader(http.getInputStream())) {
                            JsonObject obj = JsonParser.parseReader(reader).getAsJsonObject();
                            if (obj.has("id")) {
                                premium = true;
                            }
                        }
                    }
                    http.disconnect();
                } catch (Exception e) {
                    plugin.getLogger().log(Level.WARNING, "Failed to check premium status for " + name, e);
                }

                if (premium) {
                    plugin.getLogger().info("[PremiumBypass] " + name + " is a new premium player. Forcing online-mode.");
                } else {
                    plugin.getLogger().info("[PremiumBypass] " + name + " is a new cracked player. Connecting offline.");
                }
            }

            cache.put(key, new CacheEntry(premium, System.currentTimeMillis()));

            if (premium) {
                conn.setOnlineMode(true);
            }

            event.completeIntent(plugin);
        });
    }

    private boolean hasOfflineAccountInDatabase(String name, String offlineUuid) {
        try (Connection conn = DBPool.ACCOUNT.getConnection();
             PreparedStatement ps = conn.prepareStatement("SELECT 1 FROM accounts WHERE name = ? AND uuid = ? LIMIT 1;")) {
            ps.setString(1, name);
            ps.setString(2, offlineUuid);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        } catch (Exception e) {
            plugin.getLogger().log(Level.WARNING, "Failed to query offline database for player " + name, e);
        }
        return false;
    }

    private boolean hasPremiumAccountInDatabase(String name, String offlineUuid) {
        try (Connection conn = DBPool.ACCOUNT.getConnection();
             PreparedStatement ps = conn.prepareStatement("SELECT 1 FROM accounts WHERE name = ? AND uuid != ? LIMIT 1;")) {
            ps.setString(1, name);
            ps.setString(2, offlineUuid);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        } catch (Exception e) {
            plugin.getLogger().log(Level.WARNING, "Failed to query premium database for player " + name, e);
        }
        return false;
    }

    private String getOfflineUuid(String name) {
        try {
            UUID offlineUuid = UUID.nameUUIDFromBytes(("OfflinePlayer:" + name).getBytes(java.nio.charset.StandardCharsets.UTF_8));
            return offlineUuid.toString();
        } catch (Exception e) {
            return null;
        }
    }

    private static class CacheEntry {
        final boolean premium;
        final long timestamp;

        CacheEntry(boolean premium, long timestamp) {
            this.premium = premium;
            this.timestamp = timestamp;
        }
    }
}

