package com.houzicore.lobby.hub.modules;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;

import com.houzicore.lobby.hub.modules.profile.PlayerProfileShop;

import com.houzicore.shared.common.util.C;
import com.houzicore.shared.common.util.HouziColorParser;
import com.houzicore.shared.common.util.UtilText;
import com.houzicore.shared.core.lang.LangManager;
import com.houzicore.shared.core.itemstack.ItemBuilder;
import com.houzicore.shared.core.stats.StatsManager;
import com.houzicore.shared.account.CoreClientManager;
import com.houzicore.lobby.hub.HubManager;

/**
 * Player Profile Card GUI
 * Right-clicking the Profile head in the hotbar opens your stats GUI.
 */
public class PlayerProfileManager implements Listener {

    private final HubManager _hub;
    private final StatsManager _stats;
    private final CoreClientManager _clients;
    private final com.houzicore.shared.core.level.LvlManager _levels;
    private final PlayerProfileShop _shop;

    public PlayerProfileManager(HubManager hub, StatsManager stats, CoreClientManager clients, com.houzicore.shared.core.level.LvlManager levels) {
        _hub = hub;
        _stats = stats;
        _clients = clients;
        _levels = levels;
        _shop = new PlayerProfileShop(hub, clients, stats, levels);
        hub.getPlugin().getServer().getPluginManager().registerEvents(this, hub.getPlugin());
    }

    @EventHandler
    public void onInteractProfileItem(PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_AIR && event.getAction() != Action.RIGHT_CLICK_BLOCK) return;
        Player player = event.getPlayer();
        if (event.getItem() == null) return;
        
        if (event.getItem().getType() == org.bukkit.Material.PLAYER_HEAD) {
            event.setCancelled(true);
            openProfile(player, player);
        }
    }

    public void openProfile(Player viewer, Player target) {
        _shop.setTarget(viewer, target);
        _shop.attemptShopOpen(viewer);
    }
}
