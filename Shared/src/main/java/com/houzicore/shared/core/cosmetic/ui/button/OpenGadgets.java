package com.houzicore.shared.core.cosmetic.ui.button;

import com.houzicore.shared.core.cosmetic.ui.page.GadgetPage;
import com.houzicore.shared.core.cosmetic.ui.page.Menu;
import com.houzicore.shared.core.shop.item.IButton;

import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;

public class OpenGadgets implements IButton {
	private final Menu _page;

	public OpenGadgets(Menu page) {
		_page = page;
	}

	@Override
	public void onClick(Player player, ClickType clickType) {
		String pageTitle = com.houzicore.shared.core.lang.LangManager.get().isThai(player)
				? "ของเล่นและแกดเจ็ต"
				: "Toys & Gadgets";
		_page.getShop().openPageForPlayer(player, new GadgetPage(_page.getPlugin(), _page.getShop(),
				_page.getClientManager(), _page.getDonationManager(), pageTitle, player));
	}
}
