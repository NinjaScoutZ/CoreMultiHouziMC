package com.houzicore.lobby.hub.modules;

import org.bukkit.Location;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;

import com.houzicore.lobby.hub.HubManager;
import com.houzicore.shared.common.util.C;
import com.houzicore.shared.core.bonuses.BonusManager;
import com.houzicore.shared.core.bonuses.BonusMenu;
import com.houzicore.shared.core.plugin.PluginRegistry;

public class KeeperOfRewardModule {

    public KeeperOfRewardModule(HubManager hub, LobbyNpcManager npcManager, java.util.List<Location> locs) {
        this(hub, npcManager, null, locs);
    }

    public KeeperOfRewardModule(HubManager hub, LobbyNpcManager npcManager, DailyLoginManager dailyLoginManager, java.util.List<Location> locs) {
        if (locs == null || locs.isEmpty()) {
            System.out.println("[KeeperOfRewardModule] No NPC_KEEPER points in WorldConfig.dat - Keeper will not spawn.");
            return;
        }

        BonusManager bonusManager = null;
        try {
            bonusManager = PluginRegistry.require(BonusManager.class);
        } catch (Exception e) {}

        final BonusMenu menu = bonusManager != null ? new BonusMenu(bonusManager, hub.GetClients(), hub.GetDonation()) : null;
        
        String clickHint = C.cYellow + "คลิกเพื่อรับรางวัล (Click to claim!)";
        
        for (Location loc : locs) {
            npcManager.spawnEntityNpc(loc, C.cGold + C.Bold + "Keeper of Reward", C.cWhite + "Bonus Menu", clickHint, EntityType.ILLUSIONER, 2.4, new LobbyNpcManager.NpcClickHandler() {
                @Override
                public void onInteract(Player player) {
                    if (menu != null) {
                        menu.attemptShopOpen(player);
                    }
                }
            });
        }
    }
}
