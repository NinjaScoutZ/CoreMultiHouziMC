package com.houzicore.shared.core.punish.UI;

import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;

import com.houzicore.shared.core.punish.Category;
import com.houzicore.shared.core.shop.item.IButton;

public class PunishButton implements IButton {
	private final PunishPage _punishPage;
	private final Category _category;
	private final int _severity;
	private final boolean _ban;
	private final long _time;

	public PunishButton(PunishPage punishPage, Category category, int severity, boolean ban, long time) {
		_punishPage = punishPage;
		_category = category;
		_severity = severity;
		_ban = ban;
		_time = time;
	}

	@Override
	public void onClick(Player player, ClickType clickType) {
		_punishPage.AddInfraction(_category, _severity, _ban, _time);
	}
}
