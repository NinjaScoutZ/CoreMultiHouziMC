package com.houzicore.extension.listener;

import com.github.retrooper.packetevents.manager.server.ServerVersion;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.RequiredArgsConstructor;
import com.houzicore.extension.execution.dispatcher.EventDispatcher;
import com.houzicore.extension.execution.scheduler.TaskScheduler;
import com.houzicore.extension.model.entity.FPlayer;
import com.houzicore.extension.model.event.player.PlayerLoadEvent;
import com.houzicore.extension.model.event.player.PlayerPersistAndDisposeEvent;
import com.houzicore.extension.platform.provider.PacketProvider;
import com.houzicore.extension.processing.processor.PlayerPreLoginProcessor;
import com.houzicore.extension.service.FPlayerService;
import com.houzicore.extension.util.file.FileFacade;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.UUID;

@Singleton
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class BukkitBaseListener implements Listener {

    private final FileFacade fileFacade;
    private final FPlayerService fPlayerService;
    private final EventDispatcher eventDispatcher;
    private final PacketProvider packetProvider;
    private final PlayerPreLoginProcessor playerPreLoginProcessor;
    private final TaskScheduler taskScheduler;

    @EventHandler
    public void onAsyncPreLoginEvent(AsyncPlayerPreLoginEvent event) {
        // in older versions (1.20.1 and older), there is no configuration stage
        // so we use Bukkit API
        if (packetProvider.getServerVersion().isOlderThan(ServerVersion.V_1_20_2)
                || fileFacade.config().module().useBukkitPreLoginListener()) {
            UUID uuid = event.getUniqueId();
            String name = event.getName();

            playerPreLoginProcessor.processLogin(uuid, name, loginEvent -> {
                event.setLoginResult(AsyncPlayerPreLoginEvent.Result.KICK_OTHER);

                Component reason = loginEvent.kickReason();
                event.setKickMessage(LegacyComponentSerializer.legacySection().serialize(reason));
            });
        }
    }

    @EventHandler
    public void onPlayerJoinEvent(PlayerJoinEvent event) {
        taskScheduler.runAsync(() -> {
            Player player = event.getPlayer();
            UUID uuid = player.getUniqueId();

            FPlayer fPlayer = fPlayerService.getFPlayer(uuid);
            if (packetProvider.getServerVersion().isOlderThan(ServerVersion.V_1_20_2)) {
                String locale = getPlayerLocale(player);
                fPlayerService.updateLocale(fPlayer, locale);
            }

            eventDispatcher.dispatch(new PlayerLoadEvent(fPlayer));
            eventDispatcher.dispatch(new com.houzicore.extension.model.event.player.PlayerJoinEvent(fPlayer));
        });
    }

    @EventHandler
    public void onPlayerQuitEvent(PlayerQuitEvent event) {
        taskScheduler.runAsync(() -> {
            UUID uuid = event.getPlayer().getUniqueId();
            FPlayer fPlayer = fPlayerService.getFPlayer(uuid);

            eventDispatcher.dispatch(new com.houzicore.extension.model.event.player.PlayerQuitEvent(fPlayer));
            eventDispatcher.dispatch(new PlayerPersistAndDisposeEvent(fPlayer));
        });
    }

    private String getPlayerLocale(Player player) {
        try {
            return player.getLocale();
        } catch (NoSuchMethodError e) {
            return fileFacade.config().language().type();
        }
    }
}
