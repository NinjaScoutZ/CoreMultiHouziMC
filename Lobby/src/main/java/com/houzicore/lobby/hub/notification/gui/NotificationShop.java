package com.houzicore.lobby.hub.notification.gui;

import org.bukkit.entity.Player;

import com.houzicore.shared.account.CoreClientManager;
import com.houzicore.shared.core.donation.DonationManager;
import com.houzicore.shared.core.shop.ShopBase;
import com.houzicore.shared.core.shop.page.ShopPageBase;
import com.houzicore.lobby.hub.notification.NotificationManager;
import com.houzicore.lobby.hub.notification.gui.page.NotificationPage;

public class NotificationShop extends ShopBase<NotificationManager>
{
	public NotificationShop(NotificationManager plugin, CoreClientManager clientManager, DonationManager donationManager)
	{
		super(plugin, clientManager, donationManager, "Notifications");
	}

	@Override
	protected ShopPageBase<NotificationManager, ? extends ShopBase<NotificationManager>> buildPagesFor(Player player)
	{
		return new NotificationPage(getPlugin(), this, getClientManager(), getDonationManager(), player);
	}
}
