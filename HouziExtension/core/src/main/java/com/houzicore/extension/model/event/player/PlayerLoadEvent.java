package com.houzicore.extension.model.event.player;

import lombok.With;
import com.houzicore.extension.model.entity.FPlayer;

@With
public record PlayerLoadEvent(
        boolean cancelled,
        FPlayer player,
        boolean reload
) implements PlayerEvent {

    public PlayerLoadEvent(FPlayer player, boolean reload) {
        this(false, player, reload);
    }

    public PlayerLoadEvent(FPlayer player) {
        this(player, false);
    }

}
