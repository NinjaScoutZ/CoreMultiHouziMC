package com.houzicore.arcade.nautilus.game.arcade.managers.voting;

import org.bukkit.entity.Player;
import com.houzicore.shared.account.CoreClientManager;
import com.houzicore.shared.core.donation.DonationManager;
import com.houzicore.shared.core.shop.ShopBase;
import com.houzicore.shared.core.shop.page.ShopPageBase;
import com.houzicore.arcade.ArcadeManager;

public class KitSelectionShop extends ShopBase<ArcadeManager> {

    public KitSelectionShop(ArcadeManager plugin, CoreClientManager clientManager, DonationManager donationManager) {
        super(plugin, clientManager, donationManager, com.houzicore.shared.core.lang.LangManager.get().get("arcade.kit_select_gui_title", "Select Kit"));
    }

    @Override
    protected ShopPageBase<ArcadeManager, ? extends ShopBase<ArcadeManager>> buildPagesFor(Player player) {
        return new KitSelectionPage(getPlugin(), this, getClientManager(), getDonationManager(), player);
    }
}
