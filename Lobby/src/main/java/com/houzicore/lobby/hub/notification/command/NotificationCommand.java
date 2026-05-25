package com.houzicore.lobby.hub.notification.command;

import org.bukkit.entity.Player;

import com.houzicore.shared.core.command.CommandBase;
import com.houzicore.shared.common.Rank;
import com.houzicore.lobby.hub.mail.MailManager;
import com.houzicore.lobby.hub.mail.MailMessage;
import com.houzicore.lobby.hub.mail.PlayerMailData;
import com.houzicore.lobby.hub.notification.NotificationManager;

public class NotificationCommand extends CommandBase<NotificationManager>
{
	public NotificationCommand(NotificationManager plugin)
	{
		super(plugin, Rank.ALL, "notifications");
	}

	@Override
	public void Execute(Player caller, String[] args)
	{
		Plugin.openShop(caller);
	}
}
