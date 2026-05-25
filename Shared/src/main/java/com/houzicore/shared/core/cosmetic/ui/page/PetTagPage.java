package com.houzicore.shared.core.cosmetic.ui.page;

import com.houzicore.shared.core.shop.page.ShopPageBase;
import com.houzicore.shared.core.cosmetic.CosmeticManager;
import com.houzicore.shared.core.cosmetic.ui.CosmeticShop;
import com.houzicore.shared.account.CoreClientManager;
import com.houzicore.shared.core.donation.DonationManager;
import org.bukkit.entity.Player;

public class PetTagPage extends ShopPageBase<CosmeticManager, CosmeticShop> {
    public PetTagPage(CosmeticManager plugin, CosmeticShop shop, CoreClientManager clientManager,
            DonationManager donationManager, String name, Player player) {
        super(plugin, shop, clientManager, donationManager, name, player);
    }

    @Override
    protected void buildPage() {}
}
