package com.houzicore.shared.core.cosmetic.ui.button;

import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;

import com.houzicore.shared.core.cosmetic.ui.page.Menu;
import com.houzicore.shared.core.shop.item.IButton;

public class OpenCostumes implements IButton {
	private final Menu _menu;

	public OpenCostumes(Menu menu) {
		_menu = menu;
	}

	@Override
	public void onClick(Player player, ClickType clickType) {
		_menu.openCostumes(player);
	}
}
