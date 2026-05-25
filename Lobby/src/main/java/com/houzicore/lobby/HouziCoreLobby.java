package com.houzicore.lobby;

import org.bukkit.plugin.java.JavaPlugin;
import com.houzicore.shared.HouziCoreShared;

public class HouziCoreLobby extends JavaPlugin {
    @Override
    public void onEnable() {
        getLogger().info("HouziCore Lobby started: " + HouziCoreShared.getVersion());
    }

    @Override
    public void onDisable() {
        getLogger().info("HouziCore Lobby stopped");
    }
}
