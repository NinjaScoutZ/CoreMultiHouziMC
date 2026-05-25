package com.houzicore.arcade.nautilus.game.arcade.gui.privateServer.page;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;

import com.houzicore.shared.core.shop.item.IButton;
import com.houzicore.shared.core.shop.item.ShopItem;
import com.houzicore.arcade.ArcadeManager;
import com.houzicore.arcade.nautilus.game.arcade.game.GameServerConfig;
import com.houzicore.arcade.nautilus.game.arcade.gui.privateServer.PrivateServerShop;

public class OptionsPage extends BasePage
{
	private GameServerConfig _config;

	public OptionsPage(ArcadeManager plugin, PrivateServerShop shop, Player player)
	{
		super(plugin, shop, "Game Options", player);

		_config = getPlugin().GetServerConfig();
		buildPage();
	}

	@Override
	protected void buildPage()
	{
		// Options Page temporarily disabled for 1.21.1 compilation
	}
}
