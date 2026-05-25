package com.houzicore.shared.serverdata.commands.impl;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import com.houzicore.shared.serverdata.commands.CommandCallback;
import com.houzicore.shared.serverdata.commands.ServerCommand;
import com.houzicore.shared.serverdata.commands.ServerCommandManager;

public class CrossServerAnnouncer implements CommandCallback {

    private static CrossServerAnnouncer instance;

    public static void init() {
        if (instance == null) {
            instance = new CrossServerAnnouncer();
            ServerCommandManager.getInstance().registerCommandType("RedisAnnounceCommand", RedisAnnounceCommand.class, instance);
        }
    }

    public static void broadcast(String message) {
        new RedisAnnounceCommand(message, null).publish();
    }

    public static void broadcastAdmin(String message) {
        new RedisAnnounceCommand(message, "houzicore.admin").publish();
    }

    @Override
    public void run(ServerCommand command) {
        if (command instanceof RedisAnnounceCommand) {
            RedisAnnounceCommand announce = (RedisAnnounceCommand) command;
            
            // Broadcast locally on this server instance
            String msg = announce.getMessage();
            String perm = announce.getPermission();
            
            if (perm == null || perm.isEmpty()) {
                Bukkit.broadcastMessage(msg);
            } else {
                for (Player p : Bukkit.getOnlinePlayers()) {
                    if (p.hasPermission(perm)) {
                        p.sendMessage(msg);
                    }
                }
            }
        }
    }
}
