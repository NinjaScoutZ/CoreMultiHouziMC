package com.houzicore.extension.model.event.player;

import lombok.With;
import com.houzicore.extension.model.entity.FPlayer;
import net.kyori.adventure.text.Component;

@With
public record PlayerPreLoginEvent(
        boolean cancelled,
        FPlayer player,
        Component kickReason,
        boolean allowed
) implements PlayerEvent {

    public PlayerPreLoginEvent(FPlayer player) {
        this(false, player, Component.empty(), true);
    }

}
