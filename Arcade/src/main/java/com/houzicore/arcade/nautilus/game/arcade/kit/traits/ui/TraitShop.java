package com.houzicore.arcade.nautilus.game.arcade.kit.traits.ui;

import com.houzicore.shared.account.CoreClientManager;
import com.houzicore.shared.core.donation.DonationManager;
import com.houzicore.shared.core.shop.ShopBase;
import com.houzicore.shared.core.shop.page.ShopPageBase;
import org.bukkit.entity.Player;
import com.houzicore.arcade.ArcadeManager;
import com.houzicore.arcade.nautilus.game.arcade.kit.Kit;

public class TraitShop extends ShopBase<ArcadeManager> {
    
    public TraitShop(ArcadeManager plugin, CoreClientManager clientManager, DonationManager donationManager) {
        super(plugin, clientManager, donationManager, "Traits & Upgrades");
    }

    @Override
    protected ShopPageBase<ArcadeManager, ? extends ShopBase<ArcadeManager>> buildPagesFor(Player player) {
        return null;
    }

    public void openForPlayer(Player player, Kit kit) {
        openPageForPlayer(player, new TraitPage(getPlugin(), this, getClientManager(), getDonationManager(), player, kit));
    }
}
