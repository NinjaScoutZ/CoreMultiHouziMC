package com.houzicore.arcade.nautilus.game.arcade.gui.privateServer.button;

import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;

import com.houzicore.shared.core.shop.item.IButton;
import com.houzicore.arcade.ArcadeManager;
import com.houzicore.arcade.nautilus.game.arcade.gui.privateServer.PrivateServerShop;
import com.houzicore.arcade.nautilus.game.arcade.gui.privateServer.page.RemoveAdminPage;

public class RemoveAdminButton implements IButton
{
	private ArcadeManager _arcadeManager;
	private PrivateServerShop _shop;

	public RemoveAdminButton(ArcadeManager arcadeManager, PrivateServerShop shop)
	{
		_shop = shop;
		_arcadeManager = arcadeManager;
	}

	@Override
	public void onClick(Player player, ClickType clickType)
	{
		_shop.openPageForPlayer(player, new RemoveAdminPage(_arcadeManager, _shop, player));
	}
}
