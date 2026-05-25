package com.houzicore.shared.core.cosmetic.ui.button;

import com.houzicore.shared.core.cosmetic.ui.page.Menu;
import com.houzicore.shared.core.cosmetic.ui.page.PetPage;
import com.houzicore.shared.core.shop.item.IButton;

import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;

public class OpenPets implements IButton {
	private final Menu _page;

	public OpenPets(Menu page) {
		_page = page;
	}

	@Override
	public void onClick(Player player, ClickType clickType) {
		_page.getShop().openPageForPlayer(player, new PetPage(_page.getPlugin(), _page.getShop(),
				_page.getClientManager(), _page.getDonationManager(), "Pets", player));
	}
}
