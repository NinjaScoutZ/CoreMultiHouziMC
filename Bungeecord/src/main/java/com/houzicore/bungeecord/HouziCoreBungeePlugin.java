package com.houzicore.bungeecord;

import net.md_5.bungee.api.plugin.Plugin;
import java.util.logging.Logger;

import com.houzicore.bungeecord.lobbyBalancer.LobbyBalancer;
import com.houzicore.bungeecord.playerTracker.PlayerTracker;
import com.houzicore.bungeecord.motd.MotdManager;
import com.houzicore.bungeecord.command.FindCommand;
import com.houzicore.bungeecord.messaging.PluginMessageManager;
import com.houzicore.bungeecord.redis.BungeeRedisManager;

public class HouziCoreBungeePlugin extends Plugin {
    private Logger log;
    private BungeeRedisManager redisManager;
    private com.houzicore.bungeecord.punishment.PunishListener punishListener;

    public void onEnable() {
        log = getLogger();
        log.info("HouziCore Bungee starting...");

        redisManager = new BungeeRedisManager(this);
        punishListener = new com.houzicore.bungeecord.punishment.PunishListener(this);

        new LobbyBalancer(this);
        PlayerTracker tracker = new PlayerTracker(this);
        new MotdManager(this);

        getProxy().getPluginManager().registerCommand(this, new FindCommand(this, tracker));
        new PluginMessageManager(this);
        
        new com.houzicore.bungeecord.playerStats.PlayerStats(this);

        new com.houzicore.bungeecord.listener.PremiumBypassListener(this);
        
        log.info("HouziCore Bungee started successfully.");
    }

    @Override
    public void onDisable() {
        if (redisManager != null) {
            redisManager.shutdown();
        }
        if (punishListener != null) {
            punishListener.shutdown();
        }
        log.info("HouziCore Bungee stopped");
    }
}
