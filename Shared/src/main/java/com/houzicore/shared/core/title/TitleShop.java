package com.houzicore.shared.core.title;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import com.houzicore.shared.account.CoreClientManager;
import com.houzicore.shared.core.donation.DonationManager;
import com.houzicore.shared.core.lang.LangManager;
import com.houzicore.shared.core.shop.ShopBase;
import com.houzicore.shared.core.shop.page.ShopPageBase;

public class TitleShop extends ShopBase<TitleManager> {

    public TitleShop(TitleManager plugin, CoreClientManager clientManager, DonationManager donationManager) {
        super(plugin, clientManager, donationManager, "Title Menu");
    }

    @Override
    protected ShopPageBase<TitleManager, ? extends ShopBase<TitleManager>> buildPagesFor(Player player) {
        boolean thai = LangManager.get() != null && LangManager.get().isThai(player);
        String name = "          " + ChatColor.UNDERLINE + (thai ? "เมนูฉายา" : "Title Menu");
        return new TitlePage(getPlugin(), this, getClientManager(), getDonationManager(), name, player);
    }
}
