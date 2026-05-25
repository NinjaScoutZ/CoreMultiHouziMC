package com.houzicore.extension.listener;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.RequiredArgsConstructor;
import com.houzicore.extension.platform.handler.ProxyMessageHandler;
import com.houzicore.extension.platform.proxy.BukkitProxy;
import org.bukkit.entity.Player;
import org.bukkit.plugin.messaging.PluginMessageListener;
import org.jspecify.annotations.NonNull;

@Singleton
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class BukkitProxyListener implements PluginMessageListener {

    private final BukkitProxy bukkitProxy;
    private final ProxyMessageHandler proxyMessageHandler;

    @Override
    public void onPluginMessageReceived(@NonNull String channel, @NonNull Player player, byte[] bytes) {
        if (!channel.equals(bukkitProxy.getChannel()) || !bukkitProxy.isEnable()) {
            return;
        }

        proxyMessageHandler.handleProxyMessage(bytes);
    }
}
