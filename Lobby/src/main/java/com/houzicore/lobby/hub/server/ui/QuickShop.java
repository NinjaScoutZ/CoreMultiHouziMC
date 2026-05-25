package com.houzicore.lobby.hub.server.ui;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import com.houzicore.shared.account.CoreClientManager;
import com.houzicore.shared.core.shop.ShopBase;
import com.houzicore.shared.core.shop.page.ShopPageBase;
import com.houzicore.lobby.hub.server.ServerManager;

public class QuickShop extends ShopBase<ServerManager>
{
	public QuickShop(ServerManager plugin, CoreClientManager clientManager,	com.houzicore.shared.core.donation.DonationManager donationManager,	String name)
	{
		super(plugin, clientManager, donationManager, name);
	}

	@Override
	protected ShopPageBase<ServerManager, ? extends ShopBase<ServerManager>> buildPagesFor(Player player)
	{
		return new ServerGameMenu(getPlugin(), this, getClientManager(), getDonationManager(), "          " + ChatColor.UNDERLINE + "Quick Game Menu", player);
	}

	public void UpdatePages()
	{
		for (ShopPageBase<ServerManager, ? extends ShopBase<ServerManager>> page : getPlayerPageMap().values())
		{
			if (page instanceof ServerGameMenu)
			{
				((ServerGameMenu)page).Update();
			}
		}
	}
}
