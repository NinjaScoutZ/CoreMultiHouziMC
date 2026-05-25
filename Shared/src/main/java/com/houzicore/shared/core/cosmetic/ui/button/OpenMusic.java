package com.houzicore.shared.core.cosmetic.ui.button;

import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;

import com.houzicore.shared.core.cosmetic.ui.page.Menu;
import com.houzicore.shared.core.shop.item.IButton;

/**
 * Created by shaun on 14-09-15.
 */
public class OpenMusic implements IButton {
	private final Menu _menu;

	public OpenMusic(Menu menu) {
		_menu = menu;
	}

	@Override
	public void onClick(Player player, ClickType clickType) {
		_menu.openMusic(player);
	}
}
