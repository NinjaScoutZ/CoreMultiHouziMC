package com.houzicore.shared.core.bonuses;

import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import com.houzicore.shared.account.CoreClientManager;
import com.houzicore.shared.core.donation.DonationManager;
import com.houzicore.shared.core.shop.ShopBase;
import com.houzicore.shared.core.shop.page.ShopPageBase;

public class BonusMenu extends ShopBase<BonusManager> {

    public BonusMenu(BonusManager plugin, CoreClientManager clientManager, DonationManager donationManager) {
        super(plugin, clientManager, donationManager, "Keeper of Essence");
    }

    @Override
    protected ShopPageBase<BonusManager, ? extends ShopBase<BonusManager>> buildPagesFor(Player player) {
        return new BonusPage(getPlugin(), this, getClientManager(), getDonationManager(), player);
    }
}
