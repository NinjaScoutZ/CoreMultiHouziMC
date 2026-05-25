package com.houzicore.lobby.hub.modules;

import org.bukkit.entity.Player;

import com.houzicore.lobby.hub.HubManager;
import com.houzicore.shared.account.CoreClientManager;
import com.houzicore.shared.core.donation.DonationManager;
import com.houzicore.shared.core.plugin.PluginRegistry;
import com.houzicore.shared.core.shop.ShopBase;
import com.houzicore.shared.core.shop.page.ShopPageBase;

public class DailyLoginShop extends ShopBase<HubManager> {
    
    private final DailyLoginManager _manager;

    public DailyLoginShop(HubManager plugin, DailyLoginManager manager) {
        super(plugin, 
              PluginRegistry.require(CoreClientManager.class), 
              PluginRegistry.require(DonationManager.class), 
              "Daily Reward");
        _manager = manager;
    }

    @Override
    protected ShopPageBase<HubManager, ? extends ShopBase<HubManager>> buildPagesFor(Player player) {
        return new DailyLoginPage(getPlugin(), this, getClientManager(), getDonationManager(), player, _manager);
    }
}
