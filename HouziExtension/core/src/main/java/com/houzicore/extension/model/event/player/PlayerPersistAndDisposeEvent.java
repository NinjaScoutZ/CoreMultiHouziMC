package com.houzicore.extension.model.event.player;

import lombok.With;
import com.houzicore.extension.model.entity.FPlayer;

@With
public record PlayerPersistAndDisposeEvent(
        boolean cancelled,
        FPlayer player
) implements PlayerEvent {

    public PlayerPersistAndDisposeEvent(FPlayer player) {
        this(false, player);
    }

}
