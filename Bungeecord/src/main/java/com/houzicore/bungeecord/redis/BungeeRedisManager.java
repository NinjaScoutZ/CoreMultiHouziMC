package com.houzicore.bungeecord.redis;

import java.io.File;
import java.io.FileInputStream;
import java.util.Properties;
import net.md_5.bungee.api.plugin.Plugin;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

public class BungeeRedisManager {

    private final Plugin plugin;
    private JedisPool jedisPool;
    private Thread subscriberThread;
    private BungeePubSubListener listener;

    public BungeeRedisManager(Plugin plugin) {
        this.plugin = plugin;
        initializeRedis();
    }

    private void initializeRedis() {
        String host = "127.0.0.1";
        int port = 6379;
        
        try {
            File propFile = new File("../server/houzicore-database.properties");
            if (!propFile.exists()) {
                propFile = new File("houzicore-database.properties");
            }
            if (propFile.exists()) {
                Properties props = new Properties();
                try (FileInputStream fis = new FileInputStream(propFile)) {
                    props.load(fis);
                    host = props.getProperty("redis.host", "127.0.0.1");
                    port = Integer.parseInt(props.getProperty("redis.port", "6379"));
                }
            }
        } catch (Exception e) {
            plugin.getLogger().warning("Failed to load Redis properties. Using defaults.");
        }

        try {
            jedisPool = new JedisPool(host, port);
            plugin.getLogger().info("Connected to Redis at " + host + ":" + port);
            startSubscriber();
        } catch (Exception e) {
            plugin.getLogger().severe("Failed to connect to Redis!");
            e.printStackTrace();
        }
    }

    private void startSubscriber() {
        listener = new BungeePubSubListener();
        subscriberThread = new Thread(() -> {
            try (Jedis jedis = jedisPool.getResource()) {
                jedis.subscribe(listener, "houzicore:network:updates");
            } catch (Exception e) {
                if (!jedisPool.isClosed()) {
                    plugin.getLogger().warning("Redis subscriber disconnected: " + e.getMessage());
                }
            }
        }, "HouziCore-Redis-Subscriber");
        
        subscriberThread.start();
    }

    public void shutdown() {
        if (listener != null) {
            listener.unsubscribe();
        }
        if (jedisPool != null && !jedisPool.isClosed()) {
            jedisPool.close();
        }
    }
}
