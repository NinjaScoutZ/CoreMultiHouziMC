package com.houzicore.lobby.hub.modules.nonstop.shop;

import com.houzicore.shared.account.CoreClientManager;
import com.houzicore.shared.core.donation.DonationManager;
import com.houzicore.shared.core.shop.ShopBase;
import com.houzicore.shared.core.shop.page.ShopPageBase;
import com.houzicore.lobby.hub.modules.nonstop.NonstopParkourManager;
import org.bukkit.entity.Player;

public class ParkourShop extends ShopBase<NonstopParkourManager> {
    public ParkourShop(NonstopParkourManager plugin, CoreClientManager clientManager, DonationManager donationManager) {
        super(plugin, clientManager, donationManager, "Parkour Menu");
    }

    @Override
    protected ShopPageBase<NonstopParkourManager, ParkourShop> buildPagesFor(Player player) {
        return new ParkourPage(getPlugin(), this, getClientManager(), getDonationManager(), player);
    }
}
