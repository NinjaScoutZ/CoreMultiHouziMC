package com.houzicore.shared;

import org.bukkit.plugin.java.JavaPlugin;

/**
 * Lightweight entry point for the HouziCore-Shared library plugin.
 * This class exists solely to satisfy Bukkit's plugin.yml main class requirement.
 * All actual initialization is handled by the Lobby plugin which manages the shared modules.
 */
public class SharedPlugin extends JavaPlugin {
    @Override
    public void onEnable() {
        getLogger().info("HouziCore-Shared library loaded.");
        new com.houzicore.shared.core.icon.CustomIconManager(this);
    }

    @Override
    public void onDisable() {
        getLogger().info("HouziCore-Shared library unloaded.");
    }
}
