package com.houzicore.bungeecord.redis;

import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.config.ServerInfo;
import redis.clients.jedis.JedisPubSub;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.net.InetSocketAddress;

public class BungeePubSubListener extends JedisPubSub {

    @Override
    public void onMessage(String channel, String message) {
        if (!channel.equals("houzicore:network:updates")) return;

        try {
            JsonObject json = JsonParser.parseString(message).getAsJsonObject();
            String type = json.get("type").getAsString();
            String serverName = json.get("server").getAsString();

            ProxyServer proxy = ProxyServer.getInstance();

            switch (type) {
                case "SERVER_BOOTING":
                    proxy.getLogger().info("[Network] Server " + serverName + " is starting up...");
                    break;
                    
                case "SERVER_READY":
                    String ip = json.get("ip").getAsString();
                    int port = json.get("port").getAsInt();
                    proxy.getLogger().info("[Network] " + serverName + " is READY at " + ip + ":" + port);
                    
                    // Create ServerInfo and register dynamically
                    ServerInfo info = proxy.constructServerInfo(serverName, new InetSocketAddress(ip, port), "Auto-Scaled Server", false);
                    proxy.getServers().put(serverName, info);
                    break;
                    
                case "SERVER_STOPPING":
                    proxy.getLogger().info("[Network] " + serverName + " is shutting down. Removing from network.");
                    
                    ServerInfo existing = proxy.getServers().remove(serverName);
                    // Kick players back to lobby if they are on it
                    if (existing != null && !existing.getPlayers().isEmpty()) {
                        ServerInfo fallback = proxy.getServerInfo("Lobby");
                        if (fallback != null) {
                            existing.getPlayers().forEach(player -> player.connect(fallback));
                        }
                    }
                    break;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
