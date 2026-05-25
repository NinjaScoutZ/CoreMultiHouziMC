package com.houzicore.extension.processing.processor;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.RequiredArgsConstructor;
import com.houzicore.extension.execution.dispatcher.EventDispatcher;
import com.houzicore.extension.model.entity.FPlayer;
import com.houzicore.extension.model.event.player.PlayerPreLoginEvent;
import com.houzicore.extension.platform.registry.ProxyRegistry;
import com.houzicore.extension.service.FPlayerService;

import java.util.UUID;
import java.util.function.Consumer;

@Singleton
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class PlayerPreLoginProcessor {

    private final FPlayerService fPlayerService;
    private final ProxyRegistry proxyRegistry;
    private final EventDispatcher eventDispatcher;

    public void processLogin(UUID uuid, String name, Consumer<PlayerPreLoginEvent> kickConsumer) {
        // if no one was on the server, the cache may be invalid for other servers
        // because HouziExtension on Proxy cannot send a message for servers that have no player
        if (fPlayerService.getOnlineFPlayers().isEmpty() && proxyRegistry.hasEnabledProxy()) {
            // clears the cache of players who might have left from other servers
            fPlayerService.clear();
            fPlayerService.addConsole();
        }

        FPlayer fPlayer = fPlayerService.addFPlayer(uuid, name);
        PlayerPreLoginEvent event = eventDispatcher.dispatch(new PlayerPreLoginEvent(fPlayer));
        if (event.allowed()) {
            fPlayerService.saveJoinSession(fPlayer);
            fPlayerService.updateCache(fPlayerService.loadData(fPlayer));
        } else {
            fPlayerService.invalidateOnline(fPlayer.uuid());
            kickConsumer.accept(event);
        }
    }
}
