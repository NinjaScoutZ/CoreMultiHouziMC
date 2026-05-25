package com.houzicore.arcade.nautilus.game.arcade.game.modules;

import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;

import com.houzicore.arcade.nautilus.game.arcade.game.Game;

public abstract class GameModule<T extends Game> implements Listener {

    protected final T _game;
    protected boolean _active;

    public GameModule(T game) {
        _game = game;
        _active = true;
    }

    public void register() {
        _game.Manager.getPluginManager().registerEvents(this, _game.Manager.getPlugin());
    }

    public void unregister() {
        _active = false;
        HandlerList.unregisterAll(this);
    }

    public T getGame() {
        return _game;
    }

    public boolean isActive() {
        return _active;
    }
}
