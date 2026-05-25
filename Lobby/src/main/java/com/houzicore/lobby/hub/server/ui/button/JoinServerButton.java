package com.houzicore.lobby.hub.server.ui.button;

import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;

import com.houzicore.shared.core.shop.item.IButton;
import com.houzicore.lobby.hub.server.ServerInfo;
import com.houzicore.lobby.hub.server.ui.IServerPage;

public class JoinServerButton implements IButton
{
	private IServerPage _page;
	private ServerInfo _serverInfo;
	
	public JoinServerButton(IServerPage page, ServerInfo serverInfo)
	{
		_page = page;
		_serverInfo = serverInfo;
	}

	@Override
	public void onClick(Player player, ClickType clickType)
	{
		player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 0.8f, 1.2f);
		player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 0.7f, 1.55f);
		_page.SelectServer(player, _serverInfo);
	}
}
