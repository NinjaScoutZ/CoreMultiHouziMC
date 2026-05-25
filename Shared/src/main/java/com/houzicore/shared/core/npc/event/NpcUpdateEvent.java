package com.houzicore.shared.core.npc.event;

import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.bukkit.entity.Player;

public class NpcUpdateEvent extends Event {
    private static final HandlerList handlers = new HandlerList();
    private final Player _player;

    public NpcUpdateEvent(Player player) {
        _player = player;
    }

    public Player getPlayer() {
        return _player;
    }

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }
}
