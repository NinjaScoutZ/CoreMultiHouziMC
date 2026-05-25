package com.houzicore.shared.core.cosmetic.ui.button;

import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;

import com.houzicore.shared.core.cosmetic.ui.page.Menu;
import com.houzicore.shared.core.cosmetic.ui.page.BaitPage;
import com.houzicore.shared.core.shop.item.IButton;

public class OpenBaits implements IButton {
	private Menu _menu;

	public OpenBaits(Menu menu) {
		_menu = menu;
	}

	@Override
	public void onClick(Player player, ClickType clickType) {
		_menu.getShop().openPageForPlayer(player,
				new BaitPage(_menu.getPlugin(), _menu.getShop(), _menu.getClientManager(), _menu.getDonationManager(),
						"Fishing Baits", player));
	}
}
