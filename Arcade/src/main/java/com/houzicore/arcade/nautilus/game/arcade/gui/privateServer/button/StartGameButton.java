package com.houzicore.arcade.nautilus.game.arcade.gui.privateServer.button;

import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;

import com.houzicore.shared.common.util.C;
import com.houzicore.shared.core.shop.item.IButton;
import com.houzicore.arcade.ArcadeManager;
import com.houzicore.arcade.nautilus.game.arcade.game.Game.GameState;

public class StartGameButton implements IButton
{
	private ArcadeManager _arcadeManager;

	public StartGameButton(ArcadeManager arcadeManager)
	{
		_arcadeManager = arcadeManager;
	}

	@Override
	public void onClick(Player player, ClickType clickType)
	{
		if (_arcadeManager.GetGame().GetState() == GameState.Loading)
		{
			player.sendMessage(C.cRed + "The map is currently loading chunks. Please wait...");
			return;
		}
		else if (_arcadeManager.GetGame().GetState() != GameState.Recruit)
		{
			player.sendMessage(C.cRed + "Game is already in progress...");
			return;
		}

		_arcadeManager.GetGameManager().StateCountdown(_arcadeManager.GetGame(), 20, true);

		_arcadeManager.GetGame().Announce(C.cGreen + player.getName() + " \u00A77\u0e40\u0e23\u0e34\u0e48\u0e21\u0e40\u0e01\u0e21\u0e41\u0e25\u0e49\u0e27");
	}
}
