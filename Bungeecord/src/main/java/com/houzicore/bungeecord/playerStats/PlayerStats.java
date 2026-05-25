package com.houzicore.bungeecord.playerStats;

import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.PostLoginEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.api.plugin.Plugin;
import net.md_5.bungee.event.EventHandler;

import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public class PlayerStats implements Listener, Runnable {
    private final Plugin plugin;
    private final PlayerStatsRepository repository;

    public PlayerStats(Plugin plugin) {
        this.plugin = plugin;
        this.repository = new PlayerStatsRepository();

        // Run network stats logger every 5 minutes
        ProxyServer.getInstance().getScheduler().schedule(plugin, this, 5L, 5L, TimeUnit.MINUTES);
        ProxyServer.getInstance().getPluginManager().registerListener(plugin, this);
    }

    @EventHandler
    public void onPostLogin(PostLoginEvent event) {
        ProxiedPlayer player = event.getPlayer();
        String ipAddress = player.getPendingConnection().getAddress().getAddress().getHostAddress();
        String uuid = player.getUniqueId().toString();

        ProxyServer.getInstance().getScheduler().schedule(plugin, () -> {
            int accountId = repository.getAccountId(uuid);
            
            if (accountId == -1) {
                // If account still doesn't exist after 5 seconds, we can't log IP yet.
                // It might be a player stuck in Gateway or login screen.
                return;
            }

            // Fetch GeoIP
            String country = fetchCountry(ipAddress);
            
            // Log to database
            repository.updatePlayerIp(accountId, ipAddress, country);

        }, 5, TimeUnit.SECONDS); // Wait 5 seconds to ensure Spigot creates the account
    }

    private String fetchCountry(String ipAddress) {
        if (ipAddress.equals("127.0.0.1") || ipAddress.startsWith("192.168.") || ipAddress.startsWith("10.")) {
            return "Localhost";
        }
        try {
            URL url = new URL("http://ip-api.com/json/" + ipAddress + "?fields=countryCode");
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setConnectTimeout(3000);
            connection.setReadTimeout(3000);

            try (InputStreamReader reader = new InputStreamReader(connection.getInputStream())) {
                JsonObject json = JsonParser.parseReader(reader).getAsJsonObject();
                if (json.has("countryCode")) {
                    return json.get("countryCode").getAsString();
                }
            }
        } catch (Exception e) {
            plugin.getLogger().log(Level.WARNING, "Failed to fetch GeoIP for " + ipAddress + ": " + e.getMessage());
        }
        return "Unknown";
    }

    @Override
    public void run() {
        int onlineCount = ProxyServer.getInstance().getOnlineCount();
        repository.logNetworkPlayerCount(onlineCount);
    }
}
