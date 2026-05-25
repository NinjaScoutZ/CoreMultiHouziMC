package com.houzicore.shared.core.cosmetic.ui.button;

import com.houzicore.shared.core.cosmetic.ui.page.Menu;
import com.houzicore.shared.core.cosmetic.ui.page.SprayPage;
import com.houzicore.shared.core.shop.item.IButton;

import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;

public class OpenSprays implements IButton {
    private final Menu _page;

    public OpenSprays(Menu page) {
        _page = page;
    }

    @Override
    public void onClick(Player player, ClickType clickType) {
        _page.getShop().openPageForPlayer(player, new SprayPage(_page.getPlugin(), _page.getShop(),
                _page.getClientManager(), _page.getDonationManager(), "Sprays", player));
    }
}
