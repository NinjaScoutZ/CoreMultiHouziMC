package com.houzicore.shared.core.report.command;

import org.bukkit.entity.Player;

import com.houzicore.shared.common.util.UtilServer;
import com.houzicore.shared.core.report.ReportManager;
import com.houzicore.shared.serverdata.commands.ServerCommand;

public class ReportNotification extends ServerCommand {

	private String notification;

	public ReportNotification(String notification) {
		super(); // Send to all servers
		this.notification = notification;
	}

	@Override
	public void run() {
		// Message all players that can receive report notifications.
		net.kyori.adventure.text.Component msg = net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer.legacySection().deserialize(notification)
			.clickEvent(net.kyori.adventure.text.event.ClickEvent.suggestCommand("/report "));
			
		for (final Player player : UtilServer.getPlayers()) {
			if (ReportManager.getInstance().hasReportNotifications(player)) {
				player.sendMessage(msg);
			}
		}
	}
}
