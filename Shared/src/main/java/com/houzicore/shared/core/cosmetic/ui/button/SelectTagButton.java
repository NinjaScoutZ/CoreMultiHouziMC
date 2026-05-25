package com.houzicore.shared.core.cosmetic.ui.button;

import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;

import com.houzicore.shared.core.cosmetic.ui.page.PetTagPage;
import com.houzicore.shared.core.shop.item.IButton;

public class SelectTagButton implements IButton {
	private final PetTagPage _page;

	public SelectTagButton(PetTagPage page) {
		_page = page;
	}

	@Override
	public void onClick(Player player, ClickType clickType) {
		//_page.SelectTag();
	}
}
