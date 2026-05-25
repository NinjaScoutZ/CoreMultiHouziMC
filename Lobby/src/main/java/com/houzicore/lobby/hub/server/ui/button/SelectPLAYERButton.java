package com.houzicore.lobby.hub.server.ui.button;

import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;

import com.houzicore.shared.core.shop.item.IButton;
import com.houzicore.lobby.hub.server.ui.ServerGameMenu;

public class SelectPLAYERButton implements IButton
{
	private ServerGameMenu _menu;

	public SelectPLAYERButton(ServerGameMenu menu)
	{
		_menu = menu;
	}

	@Override
	public void onClick(Player player, ClickType clickType)
	{
		_menu.openPlayerGames(player);
	}
}
