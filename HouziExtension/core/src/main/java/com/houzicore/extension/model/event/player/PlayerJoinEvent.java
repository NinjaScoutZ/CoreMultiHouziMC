package com.houzicore.extension.model.event.player;

import lombok.With;
import com.houzicore.extension.model.entity.FPlayer;

@With
public record PlayerJoinEvent(
        boolean cancelled,
        FPlayer player
) implements PlayerEvent {

    public PlayerJoinEvent(FPlayer player) {
        this(false, player);
    }

}
