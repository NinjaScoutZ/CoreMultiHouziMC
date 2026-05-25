package com.houzicore.bungeecord.messaging;

import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteStreams;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.PluginMessageEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.api.plugin.Plugin;
import net.md_5.bungee.event.EventHandler;

public class PluginMessageManager implements Listener {

    private final Plugin _plugin;
    private final String CHANNEL = "houzicore:main"; // Must be lowercase and namespace formatted in 1.13+

    public PluginMessageManager(Plugin plugin) {
        _plugin = plugin;
        _plugin.getProxy().registerChannel(CHANNEL);
        _plugin.getProxy().getPluginManager().registerListener(_plugin, this);
        _plugin.getLogger().info("Registered PluginMessageManager on channel: " + CHANNEL);
    }

    @EventHandler
    public void onPluginMessage(PluginMessageEvent event) {
        if (!event.getTag().equals(CHANNEL)) {
            return;
        }

        // Only handle messages coming from the server to BungeeCord
        if (!(event.getSender() instanceof net.md_5.bungee.api.connection.Server)) {
            return;
        }

        ByteArrayDataInput in = ByteStreams.newDataInput(event.getData());
        String subChannel;
        try {
            subChannel = in.readUTF();
        } catch (Exception e) {
            return;
        }

        if (subChannel.equals("SEND_PLAYER")) {
            String targetPlayer = in.readUTF();
            String targetServer = in.readUTF();

            ProxiedPlayer player = _plugin.getProxy().getPlayer(targetPlayer);
            if (player != null) {
                ServerInfo server = _plugin.getProxy().getServerInfo(targetServer);
                if (server != null) {
                    player.connect(server);
                } else {
                    _plugin.getLogger().warning("Received request to send player " + targetPlayer + " to unknown server: " + targetServer);
                }
            }
        }
    }
}
