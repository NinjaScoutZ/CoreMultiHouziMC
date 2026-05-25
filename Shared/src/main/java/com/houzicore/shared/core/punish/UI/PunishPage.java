package com.houzicore.shared.core.punish.UI;

import org.bukkit.entity.Player;

import com.houzicore.shared.core.punish.Category;
import com.houzicore.shared.core.punish.Punish;
import com.houzicore.shared.core.punish.Punishment;
import com.houzicore.shared.core.shop.item.ShopItem;
import com.houzicore.shared.core.shop.page.ShopPageBase;

public class PunishPage extends ShopPageBase {
    private final Punish _punish;
    private final String _target;
    private final String _reason;

    // Full constructor
    public PunishPage(com.houzicore.shared.MiniPlugin plugin, com.houzicore.shared.core.shop.ShopBase shop,
            com.houzicore.shared.account.CoreClientManager clientManager,
            com.houzicore.shared.core.donation.DonationManager donationManager,
            String name, Player player) {
        super(plugin, shop, clientManager, donationManager, name, player);
        _punish = null;
        _target = null;
        _reason = null;
    }

    // Convenience constructor used by PunishCommand
    public PunishPage(Punish punish, Player caller, String target, String reason) {
        super(punish, null, punish.GetClients(), null, "Punish " + target, caller);
        _punish = punish;
        _target = target;
        _reason = reason;
    }

    @Override
    protected void buildPage() {
        // Stub - builds punish UI
    }

    public void AddInfraction(Category category, int severity, boolean ban, long time) {
        // Stub - processes infraction
    }

    public void RemovePunishment(Punishment punishment, ShopItem item) {
        // Stub - removes punishment
    }
}
