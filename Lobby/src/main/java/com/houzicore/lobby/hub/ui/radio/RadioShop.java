package com.houzicore.lobby.hub.ui.radio;

import com.houzicore.lobby.hub.HubManager;
import com.houzicore.shared.account.CoreClientManager;
import com.houzicore.shared.core.donation.DonationManager;
import com.houzicore.shared.core.music.RadioManager;
import com.houzicore.shared.core.shop.ShopBase;
import com.houzicore.shared.core.shop.page.ShopPageBase;
import org.bukkit.entity.Player;

public class RadioShop extends ShopBase<HubManager> {
    private final RadioManager _radioManager;

    public RadioShop(HubManager plugin, CoreClientManager clientManager, DonationManager donationManager, RadioManager radioManager) {
        super(plugin, clientManager, donationManager, "Music Radio");
        _radioManager = radioManager;
    }

    @Override
    protected ShopPageBase<HubManager, ? extends ShopBase<HubManager>> buildPagesFor(Player player) {
        return new RadioPage(getPlugin(), this, getClientManager(), getDonationManager(), player, _radioManager);
    }
}
