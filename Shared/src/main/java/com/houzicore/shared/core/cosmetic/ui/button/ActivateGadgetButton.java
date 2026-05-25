package com.houzicore.shared.core.cosmetic.ui.button;

import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;

import com.houzicore.shared.core.cosmetic.ui.page.GadgetPage;
import com.houzicore.shared.core.gadget.types.Gadget;
import com.houzicore.shared.core.shop.item.IButton;

public class ActivateGadgetButton implements IButton {
	private final Gadget _gadget;
	private final GadgetPage _page;

	public ActivateGadgetButton(Gadget gadget, GadgetPage page) {
		_gadget = gadget;
		_page = page;
	}

	@Override
	public void onClick(Player player, ClickType clickType) {
		if (clickType.isLeftClick()) {
			_page.activateGadget(player, _gadget);
		} else if (clickType.isRightClick()) {
			_page.handleRightClick(player, _gadget);
		}
	}
}
