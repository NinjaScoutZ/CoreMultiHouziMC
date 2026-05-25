package com.houzicore.extension.platform.proxy;

import com.google.common.collect.Iterables;
import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Singleton;
import lombok.RequiredArgsConstructor;
import com.houzicore.extension.listener.BukkitProxyListener;
import com.houzicore.extension.model.entity.FEntity;
import com.houzicore.extension.util.constant.ModuleName;
import com.houzicore.extension.util.file.FileFacade;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.jspecify.annotations.Nullable;

@Singleton
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class BukkitProxy implements Proxy {

    private final FileFacade fileFacade;
    private final Plugin plugin;
    private final Provider<BukkitProxyListener> proxyListenerProvider;

    private String channel;

    @Override
    public boolean isEnable() {
        return channel != null;
    }

    @Override
    public void onEnable() {
        channel = getChannel();
        if (channel == null) return;

        plugin.getServer().getMessenger().unregisterOutgoingPluginChannel(plugin);
        plugin.getServer().getMessenger().unregisterIncomingPluginChannel(plugin);
        plugin.getServer().getMessenger().registerOutgoingPluginChannel(plugin, channel);
        plugin.getServer().getMessenger().registerIncomingPluginChannel(plugin, channel, proxyListenerProvider.get());
    }

    @Override
    public void onDisable() {
        if (!isEnable()) return;

        plugin.getServer().getMessenger().unregisterOutgoingPluginChannel(plugin);
        plugin.getServer().getMessenger().unregisterIncomingPluginChannel(plugin);

        channel = null;
    }

    @Override
    public boolean sendMessage(FEntity sender, ModuleName tag, byte[] message) {
        if (!isEnable()) return false;
        if (tag == null) return false;

        Player player = Bukkit.getPlayer(sender.uuid());
        if (player == null) {
            player = Iterables.getFirst(Bukkit.getOnlinePlayers(), null);
        }

        if (player == null) return false;

        player.sendPluginMessage(plugin, channel, message);
        return true;
    }

    public @Nullable String getChannel() {
        if (fileFacade.config().proxy().bungeecord()) {
            return "BungeeCord";
        }

        if (fileFacade.config().proxy().velocity()) {
            return "houziextension:main";
        }

        return null;
    }
}
