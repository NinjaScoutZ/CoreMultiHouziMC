package com.houzicore.arcade.nautilus.game.arcade.gui.privateServer;

import org.bukkit.entity.Player;

import com.houzicore.shared.account.CoreClientManager;
import com.houzicore.shared.core.donation.DonationManager;
import com.houzicore.shared.core.shop.ShopBase;
import com.houzicore.shared.core.shop.page.ShopPageBase;
import com.houzicore.arcade.ArcadeManager;
import com.houzicore.arcade.nautilus.game.arcade.gui.privateServer.page.MenuPage;

public class PrivateServerShop extends ShopBase<ArcadeManager>
{
	public PrivateServerShop(ArcadeManager plugin, CoreClientManager clientManager, DonationManager donationManager)
	{
		super(plugin, clientManager, donationManager, "Private Server Menu");
	}

	@Override
	protected ShopPageBase<ArcadeManager, ? extends ShopBase<ArcadeManager>> buildPagesFor(Player player)
	{
		return new MenuPage(getPlugin(), this, player);
	}
}
