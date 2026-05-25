package com.houzicore.lobby.hub.notification.gui.page;

import java.util.List;

import org.bukkit.entity.Player;

import com.houzicore.shared.account.CoreClientManager;
import com.houzicore.shared.core.donation.DonationManager;
import com.houzicore.shared.core.shop.item.ShopItem;
import com.houzicore.shared.core.shop.page.ShopPageBase;
import com.houzicore.lobby.hub.notification.NotificationManager;
import com.houzicore.lobby.hub.notification.api.Notification;
import com.houzicore.lobby.hub.notification.gui.NotificationShop;
import com.houzicore.lobby.hub.notification.gui.button.NotificationButton;

public class NotificationPage extends ShopPageBase<NotificationManager, NotificationShop>
{
	public NotificationPage(NotificationManager plugin, NotificationShop shop, CoreClientManager clientManager, DonationManager donationManager, Player player)
	{
		super(plugin, shop, clientManager, donationManager, com.houzicore.shared.common.util.UtilText.toSmallCaps(com.houzicore.shared.core.lang.LangManager.get().get(player, "gui.notification.title")), player);

		refresh();
	}

	@Override
	protected void buildPage()
	{
		List<Notification> notifications = getPlugin().getNotifications(getPlayer());

		for (int i = 0; i < notifications.size(); i++)
		{
			Notification message = notifications.get(i);

			ShopItem item = getItem(message);

			addButton(i, item, new NotificationButton(getPlugin(), getPlayer(), this, message));
		}
	}

	private ShopItem getItem(Notification notification)
	{
		return new ShopItem(notification.getMaterial(), notification.getData(), notification.getTitle(), notification.getTitle(), notification.getText(), 1, false, false);
	}
}
