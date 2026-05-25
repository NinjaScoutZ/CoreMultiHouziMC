package com.houzicore.shared.core.cosmetic.ui.button;

import com.houzicore.shared.core.cosmetic.ui.page.Menu;
import com.houzicore.shared.core.cosmetic.ui.page.WinEffectPage;
import com.houzicore.shared.core.shop.item.IButton;

import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;

public class OpenWinEffects implements IButton {
    private final Menu _page;

    public OpenWinEffects(Menu page) {
        _page = page;
    }

    @Override
    public void onClick(Player player, ClickType clickType) {
        _page.getShop().openPageForPlayer(player, new WinEffectPage(_page.getPlugin(), _page.getShop(),
                _page.getClientManager(), _page.getDonationManager(), "Win Effects", player));
    }
}
