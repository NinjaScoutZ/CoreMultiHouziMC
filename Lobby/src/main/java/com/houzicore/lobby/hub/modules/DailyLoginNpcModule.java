package com.houzicore.lobby.hub.modules;

import org.bukkit.Location;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;

import com.houzicore.lobby.hub.HubManager;
import com.houzicore.shared.common.util.C;
import com.houzicore.shared.core.plugin.PluginRegistry;

public class DailyLoginNpcModule {

    public DailyLoginNpcModule(HubManager hub, LobbyNpcManager npcManager, java.util.List<Location> locs, DailyLoginManager dailyLoginManager) {
        if (locs == null || locs.isEmpty()) {
            System.out.println("[DailyLoginNpcModule] No NPC_DAILY_LOGIN points in WorldConfig.dat - NPC will not spawn.");
            return;
        }
        
        String clickHint = C.cYellow + "คลิกเพื่อรับรางวัลรายวัน (Daily Login)";
        
        for (Location loc : locs) {
            // For now, spawn as a Villager (defaults to Villager model unless specified)
            // The user hasn't specified the exact skin yet, so we use VILLAGER as placeholder.
            npcManager.spawnEntityNpc(loc, C.cGreen + C.Bold + "Daily Login", C.cWhite + "Rewards", clickHint, EntityType.VILLAGER, 2.4, new LobbyNpcManager.NpcClickHandler() {
                @Override
                public void onInteract(Player player) {
                    if (dailyLoginManager != null) {
                        dailyLoginManager.openRewards(player);
                    }
                }
            });
        }
    }
}
