package com.houzicore.extension.module.message.bubble.listener;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.RequiredArgsConstructor;
import com.houzicore.extension.annotation.Pulse;
import com.houzicore.extension.listener.PulseListener;
import com.houzicore.extension.model.entity.FPlayer;
import com.houzicore.extension.model.event.player.PlayerQuitEvent;
import com.houzicore.extension.module.message.bubble.service.BubbleService;

@Singleton
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class BubblePulseListener implements PulseListener {

    private final BubbleService bubbleService;

    @Pulse
    public void onPlayerQuitEvent(PlayerQuitEvent event) {
        FPlayer fPlayer = event.player();
        bubbleService.clear(fPlayer);
    }

}
