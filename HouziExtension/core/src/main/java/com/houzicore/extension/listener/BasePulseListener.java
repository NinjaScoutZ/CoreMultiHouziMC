package com.houzicore.extension.listener;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.RequiredArgsConstructor;
import com.houzicore.extension.annotation.Pulse;
import com.houzicore.extension.model.entity.FPlayer;
import com.houzicore.extension.model.event.Event;
import com.houzicore.extension.model.event.EventMetadata;
import com.houzicore.extension.model.event.lifecycle.DisableEvent;
import com.houzicore.extension.model.event.lifecycle.EnableEvent;
import com.houzicore.extension.model.event.message.MessagePrepareEvent;
import com.houzicore.extension.model.event.message.MessageSendEvent;
import com.houzicore.extension.model.event.player.PlayerJoinEvent;
import com.houzicore.extension.model.event.player.PlayerPersistAndDisposeEvent;
import com.houzicore.extension.platform.adapter.PlatformPlayerAdapter;
import com.houzicore.extension.platform.sender.IntegrationSender;
import com.houzicore.extension.platform.sender.ProxySender;
import com.houzicore.extension.platform.sender.SoundPlayer;
import com.houzicore.extension.service.FPlayerService;
import com.houzicore.extension.util.constant.ModuleName;

@Singleton
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class BasePulseListener implements PulseListener {

    private final FPlayerService fPlayerService;
    private final PlatformPlayerAdapter platformPlayerAdapter;
    private final ProxySender proxySender;
    private final IntegrationSender integrationSender;
    private final SoundPlayer soundPlayer;

    @Pulse(priority = Event.Priority.LOWEST, ignoreCancelled = true)
    public PlayerJoinEvent onPlayerJoinEvent(PlayerJoinEvent event) {
        FPlayer fPlayer = event.player().withIp(platformPlayerAdapter.getIp(event.player()));

        fPlayerService.saveFPlayerData(fPlayer);

        return event.withPlayer(fPlayer);
    }

    @Pulse
    public PlayerPersistAndDisposeEvent onPlayerPersistAndDispose(PlayerPersistAndDisposeEvent event) {
        FPlayer fPlayer = fPlayerService.clearAndSave(event.player());

        return event.withPlayer(fPlayer);
    }

    @Pulse
    public void onMessageSendEvent(MessageSendEvent event) {
        EventMetadata<?> eventMetadata = event.eventMetadata();
        if (eventMetadata.sound() != null) {
            soundPlayer.play(eventMetadata.sound(), eventMetadata.sender(), event.receiver());
        }
    }

    @Pulse
    public Event onMessagePrepareEvent(MessagePrepareEvent event) {
        ModuleName moduleName = event.moduleName();
        String rawFormat = event.rawFormat();
        EventMetadata<?> eventMetadata = event.eventMetadata();

        integrationSender.asyncSend(moduleName, rawFormat, eventMetadata);

        if (proxySender.send(moduleName, eventMetadata)) {
            return event.withCancelled(true);
        }

        return event;
    }

    @Pulse
    public void onEnableEvent(EnableEvent event) {
        integrationSender.send(ModuleName.SERVER_ENABLE, "", EventMetadata.builder()
                .sender(FPlayer.UNKNOWN)
                .format("")
                .integration()
                .build()
        );
    }

    @Pulse
    public void onDisableEvent(DisableEvent event) {
        integrationSender.send(ModuleName.SERVER_DISABLE, "", EventMetadata.builder()
                .sender(FPlayer.UNKNOWN)
                .format("")
                .integration()
                .build()
        );
    }
}
