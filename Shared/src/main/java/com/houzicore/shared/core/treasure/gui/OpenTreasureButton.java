package com.houzicore.shared.core.treasure.gui;

import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;

import com.houzicore.shared.core.shop.item.IButton;
import com.houzicore.shared.core.treasure.TreasureLocation;
import com.houzicore.shared.core.treasure.TreasureType;

public class OpenTreasureButton implements IButton {

	private final TreasureLocation _treasureLocation;
	private final TreasureType _treasureType;

	public OpenTreasureButton(Player player, TreasureLocation treasureLocation, TreasureType treasureType) {
		_treasureLocation = treasureLocation;
		_treasureType = treasureType;
	}

	@Override
	public void onClick(Player player, ClickType clickType) {
		player.playSound(player.getLocation(), org.bukkit.Sound.BLOCK_NOTE_BLOCK_PLING, 1f, 1.6f);
		_treasureLocation.attemptOpenTreasure(player, _treasureType);
		player.closeInventory();
	}
}
