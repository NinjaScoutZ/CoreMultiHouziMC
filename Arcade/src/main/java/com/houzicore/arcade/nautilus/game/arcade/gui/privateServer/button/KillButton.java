package com.houzicore.arcade.nautilus.game.arcade.gui.privateServer.button;

import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;

import com.houzicore.shared.common.util.C;
import com.houzicore.shared.common.util.UtilPlayer;
import com.houzicore.shared.common.util.UtilServer;
import com.houzicore.shared.core.shop.item.IButton;
import com.houzicore.arcade.ArcadeManager;
import com.houzicore.arcade.nautilus.game.arcade.managers.GameHostManager;

public class KillButton implements IButton
{
	private ArcadeManager _arcade;
	private GameHostManager _manager;

	public KillButton(ArcadeManager arcadeManager)
	{
		_manager = arcadeManager.GetGameHostManager();
		_arcade = arcadeManager;
	}

	@Override
	public void onClick(Player player, ClickType clickType)
	{
		if (clickType == clickType.SHIFT_RIGHT)
		{
			_manager.setHostExpired(true, "The host has closed this Private Server.");
		}
	}
}
