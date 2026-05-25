package com.houzicore.lobby.hub.notification.gui.button;

import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;

import com.houzicore.shared.common.util.Callback;
import com.houzicore.shared.core.shop.item.IButton;
import com.houzicore.lobby.hub.mail.MailManager;
import com.houzicore.lobby.hub.mail.MailMessage;
import com.houzicore.lobby.hub.notification.NotificationManager;
import com.houzicore.lobby.hub.notification.api.Notification;
import com.houzicore.lobby.hub.notification.gui.page.NotificationPage;

public class NotificationButton implements IButton
{
	private NotificationManager _manager;
	private NotificationPage _page;
	private Notification _notification;

	private Player _player;

	public NotificationButton(NotificationManager manager, Player player, NotificationPage page, Notification notification)
	{
		_manager = manager;
		_page = page;
		_notification = notification;
		_player = player;
	}

	@Override
	public void onClick(Player player, ClickType clickType)
	{
		_notification.clicked(player, clickType);
	}
}
