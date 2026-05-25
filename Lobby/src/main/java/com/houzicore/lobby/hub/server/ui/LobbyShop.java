package com.houzicore.lobby.hub.server.ui;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import com.houzicore.shared.account.CoreClientManager;
import com.houzicore.shared.core.shop.ShopBase;
import com.houzicore.shared.core.shop.page.ShopPageBase;
import com.houzicore.lobby.hub.server.ServerManager;

public class LobbyShop extends ShopBase<ServerManager>
{
	public LobbyShop(ServerManager plugin, CoreClientManager clientManager,	com.houzicore.shared.core.donation.DonationManager donationManager,	String name)
	{
		super(plugin, clientManager, donationManager, name);
		
		plugin.addServerGroup("Lobby", "Lobby");
	}

	@Override
	protected ShopPageBase<ServerManager, ? extends ShopBase<ServerManager>> buildPagesFor(Player player)
	{
		return new LobbyMenu(getPlugin(), this, getClientManager(), getDonationManager(), "          " + ChatColor.UNDERLINE + "Lobby Selector", player, "Lobby");
	}

	public void UpdatePages()
	{
		for (ShopPageBase<ServerManager, ? extends ShopBase<ServerManager>> page : getPlayerPageMap().values())
		{
			if (page instanceof LobbyMenu)
			{
				((LobbyMenu)page).Update();
			}
		}
	}
}
