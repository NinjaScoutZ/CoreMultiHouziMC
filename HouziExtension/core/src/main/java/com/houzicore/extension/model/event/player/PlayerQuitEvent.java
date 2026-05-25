package com.houzicore.extension.model.event.player;

import lombok.With;
import com.houzicore.extension.model.entity.FPlayer;

@With
public record PlayerQuitEvent(
        boolean cancelled,
        FPlayer player
) implements PlayerEvent {

    public PlayerQuitEvent(FPlayer player) {
        this(false, player);
    }

}
