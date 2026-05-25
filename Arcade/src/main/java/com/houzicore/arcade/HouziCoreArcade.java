package com.houzicore.arcade;

import org.bukkit.plugin.java.JavaPlugin;
import com.houzicore.shared.HouziCoreShared;

public class HouziCoreArcade extends JavaPlugin {
    @Override
    public void onEnable() {
        getLogger().info("HouziCore Arcade started: " + HouziCoreShared.getVersion());
    }

    @Override
    public void onDisable() {
        getLogger().info("HouziCore Arcade stopped");
    }
}
