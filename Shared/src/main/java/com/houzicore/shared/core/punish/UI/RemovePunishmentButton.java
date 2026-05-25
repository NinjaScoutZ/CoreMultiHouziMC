package com.houzicore.shared.core.punish.UI;

import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;

import com.houzicore.shared.core.punish.Punishment;
import com.houzicore.shared.core.shop.item.ShopItem;
import com.houzicore.shared.core.shop.item.IButton;

public class RemovePunishmentButton implements IButton {
	private final PunishPage _punishPage;
	private final Punishment _punishment;
	private final ShopItem _item;

	public RemovePunishmentButton(PunishPage punishPage, Punishment punishment, ShopItem item) {
		_punishPage = punishPage;
		_punishment = punishment;
		_item = item;
	}

	@Override
	public void onClick(Player player, ClickType clickType) {
		_punishPage.RemovePunishment(_punishment, _item);
	}
}
