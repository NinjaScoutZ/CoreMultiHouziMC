package com.houzicore.shared.core.cosmetic.ui.button;

import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;

import com.houzicore.shared.core.shop.item.IButton;

public class CloseButton implements IButton {
	@Override
	public void onClick(Player player, ClickType clickType) {
		player.closeInventory();
	}
}
