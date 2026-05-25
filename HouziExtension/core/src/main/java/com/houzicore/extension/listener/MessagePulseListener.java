package com.houzicore.extension.listener;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.RequiredArgsConstructor;
import com.houzicore.extension.annotation.Pulse;
import com.houzicore.extension.model.entity.FPlayer;
import com.houzicore.extension.model.event.Event;
import com.houzicore.extension.model.event.message.MessageSendEvent;
import com.houzicore.extension.model.util.Destination;
import com.houzicore.extension.platform.adapter.PlatformPlayerAdapter;
import com.houzicore.extension.platform.render.*;
import com.houzicore.extension.platform.sender.MessageSender;
import net.kyori.adventure.text.Component;

@Singleton
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class MessagePulseListener implements PulseListener {

    private final MessageSender messageSender;
    private final ActionBarRender actionBarRender;
    private final BossBarRender bossBarRender;
    private final BrandRender brandRender;
    private final ListFooterRender listFooterRender;
    private final TextScreenRender textScreenRender;
    private final TitleRender titleRender;
    private final ToastRender toastRender;
    private final PlatformPlayerAdapter platformPlayerAdapter;

    @Pulse(priority = Event.Priority.HIGHEST)
    public void onSenderToReceiverMessageEvent(MessageSendEvent event) {
        Component message = event.message();
        if (!Component.IS_NOT_EMPTY.test(message)) return;

        FPlayer fReceiver = event.receiver();

        Destination destination = event.eventMetadata().destination();

        if (fReceiver.isConsole() && destination.type() != Destination.Type.CHAT) {
            messageSender.sendToConsole(message);
            return;
        }

        switch (destination.type()) {
            case TITLE -> titleRender.render(fReceiver, message, event.submessage(), destination.times());
            case SUBTITLE -> titleRender.render(fReceiver, event.submessage(), message, destination.times());
            case ACTION_BAR -> actionBarRender.render(fReceiver, message, destination.times().stayTicks());
            case BOSS_BAR -> bossBarRender.render(fReceiver, message, destination.bossBar());
            case TAB_HEADER -> listFooterRender.render(fReceiver, message, platformPlayerAdapter.getPlayerListFooter(fReceiver));
            case TAB_FOOTER -> listFooterRender.render(fReceiver, platformPlayerAdapter.getPlayerListHeader(fReceiver), message);
            case TOAST -> toastRender.render(fReceiver, message, event.submessage(), destination.toast());
            case BRAND -> brandRender.render(fReceiver, message);
            case TEXT_SCREEN -> textScreenRender.render(fReceiver, message, destination.textScreen());
            default -> messageSender.sendMessage(fReceiver, message, false);
        }
    }
}
