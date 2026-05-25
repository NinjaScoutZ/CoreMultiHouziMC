package com.houzicore.kittester;

import org.bukkit.plugin.java.JavaPlugin;

public class KitTester extends JavaPlugin {

    private static KitTester instance;

    @Override
    public void onEnable() {
        instance = this;
        
        // Register commands
        if (getCommand("testkit") != null) {
            getCommand("testkit").setExecutor(new TestKitCommand(this));
        }

        // Register listeners
        getServer().getPluginManager().registerEvents(new SkillListener(this), this);

        getLogger().info("KitTester has been enabled! Use /testkit to test your perks.");
    }

    @Override
    public void onDisable() {
        getLogger().info("KitTester has been disabled.");
    }

    public static KitTester getInstance() {
        return instance;
    }
}
