package com.houzicore.gateway.auth;

import com.houzicore.gateway.GatewayPlugin;

import org.bukkit.scheduler.BukkitRunnable;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URI;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

/**
 * Asynchronously queries the Mojang API to determine if a username
 * belongs to a premium (paid) Minecraft account.
 *
 * Result is cached for 60 seconds to avoid rate limiting.
 */
public class MojangChecker {

    private static final String MOJANG_URL = "https://api.mojang.com/users/profiles/minecraft/";
    private static final long CACHE_TTL_MS = 60_000L;

    private final GatewayPlugin plugin;
    private final Map<String, CacheEntry> cache = new ConcurrentHashMap<>();

    public MojangChecker(GatewayPlugin plugin) {
        this.plugin = plugin;
    }

    /**
     * Check if the given username is a premium account.
     * This runs async and calls back on the main thread.
     *
     * @param name     Minecraft username (case-insensitive)
     * @param callback receives true if premium, false otherwise
     */
    public void checkAsync(String name, Consumer<Boolean> callback) {
        String key = name.toLowerCase();

        // Check cache first
        CacheEntry cached = cache.get(key);
        if (cached != null && (System.currentTimeMillis() - cached.timestamp) < CACHE_TTL_MS) {
            callback.accept(cached.premium);
            return;
        }

        new BukkitRunnable() {
            @Override
            public void run() {
                boolean premium = false;
                try {
                    URI uri = URI.create(MOJANG_URL + name);
                    HttpURLConnection conn = (HttpURLConnection) uri.toURL().openConnection();
                    conn.setRequestMethod("GET");
                    conn.setConnectTimeout(5000);
                    conn.setReadTimeout(5000);

                    int code = conn.getResponseCode();
                    if (code == 200) {
                        try (InputStreamReader reader = new InputStreamReader(conn.getInputStream())) {
                            JsonObject obj = JsonParser.parseReader(reader).getAsJsonObject();
                            premium = obj.has("id");
                        }
                    }
                    conn.disconnect();
                } catch (Exception e) {
                    plugin.getLogger().warning("MojangChecker error for " + name + ": " + e.getMessage());
                }

                final boolean result = premium;
                cache.put(key, new CacheEntry(result));

                // Callback back on main thread
                new BukkitRunnable() {
                    @Override
                    public void run() {
                        callback.accept(result);
                    }
                }.runTask(plugin);
            }
        }.runTaskAsynchronously(plugin);
    }

    public void clearCache() {
        cache.clear();
    }

    // -----------------------------------------------------------------------
    // Cache entry
    // -----------------------------------------------------------------------

    private static class CacheEntry {
        final boolean premium;
        final long    timestamp;

        CacheEntry(boolean premium) {
            this.premium   = premium;
            this.timestamp = System.currentTimeMillis();
        }
    }
}
