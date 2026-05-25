package com.houzicore.shared.core.component;

import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * A base class for dynamically registered listeners linked to a precise lifecycle phase.
 * Prevents memory leaks by automatically unregistering itself upon deactivation.
 */
public abstract class ListenerComponent implements PhasedLifetime, Listener {

    protected final JavaPlugin _plugin;
    private boolean _active;

    public ListenerComponent(JavaPlugin plugin) {
        _plugin = plugin;
    }

    @Override
    public void activate() {
        if (!_active) {
            _active = true;
            _plugin.getServer().getPluginManager().registerEvents(this, _plugin);
            onActivate();
        }
    }

    @Override
    public void deactivate() {
        if (_active) {
            _active = false;
            HandlerList.unregisterAll(this);
            onDeactivate();
        }
    }

    @Override
    public boolean isActive() {
        return _active;
    }

    /**
     * Called when this component transitions from inactive to active.
     */
    protected void onActivate() {}

    /**
     * Called when this component transitions from active to inactive.
     */
    protected void onDeactivate() {}
}
