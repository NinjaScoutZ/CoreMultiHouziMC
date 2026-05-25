package com.houzicore.arcade.nautilus.game.arcade.gui.privateServer.button;

import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;

import com.houzicore.shared.core.shop.item.IButton;
import com.houzicore.arcade.ArcadeManager;
import com.houzicore.arcade.nautilus.game.arcade.gui.privateServer.PrivateServerShop;
import com.houzicore.arcade.nautilus.game.arcade.gui.privateServer.page.GameVotingPage;

/**
 * Created by WilliamTiger.
 * All the code and any API's associated with it
 * are not to be used anywhere else without written
 * consent of William Burns. 2015.
 * 24/07/15
 */
public class GameVotingButton implements IButton
{
	private ArcadeManager _arcadeManager;
	private PrivateServerShop _shop;

	public GameVotingButton(ArcadeManager arcadeManager, PrivateServerShop shop)
	{
		_shop = shop;
		_arcadeManager = arcadeManager;
	}

	@Override
	public void onClick(Player player, ClickType clickType)
	{
		_shop.openPageForPlayer(player, new GameVotingPage(_arcadeManager, _shop, player));
	}
}
