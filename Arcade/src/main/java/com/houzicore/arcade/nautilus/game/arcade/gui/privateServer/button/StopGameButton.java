package com.houzicore.arcade.nautilus.game.arcade.gui.privateServer.button;

import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;

import com.houzicore.shared.common.util.C;
import com.houzicore.shared.core.shop.item.IButton;
import com.houzicore.arcade.ArcadeManager;
import com.houzicore.arcade.nautilus.game.arcade.game.Game.GameState;

public class StopGameButton implements IButton
{
	private ArcadeManager _arcadeManager;

	public StopGameButton(ArcadeManager arcadeManager)
	{
		_arcadeManager = arcadeManager;
	}

	@Override
	public void onClick(Player player, ClickType clickType)
	{
		if (_arcadeManager.GetGame().GetState() == GameState.End || _arcadeManager.GetGame().GetState() == GameState.End)
		{
			player.sendMessage("Game is already ending..."); 
			return;
		}
		else if (_arcadeManager.GetGame().GetState() == GameState.Recruit)
		{
			_arcadeManager.GetGame().SetState(GameState.Dead);
		}
		else
		{
			_arcadeManager.GetGame().SetState(GameState.End);
		}


		_arcadeManager.GetGame().Announce(C.cAqua + C.Bold + player.getName() + " has stopped the game.");
	}
}
