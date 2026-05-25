package com.houzicore.extension.model.event.player;

import com.houzicore.extension.model.entity.FPlayer;
import com.houzicore.extension.model.event.Event;

public interface PlayerEvent extends Event {

    FPlayer player();

    PlayerEvent withPlayer(FPlayer player);

}
