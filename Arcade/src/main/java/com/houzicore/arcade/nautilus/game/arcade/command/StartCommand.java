package com.houzicore.arcade.nautilus.game.arcade.command;

import org.bukkit.entity.Player;

import com.houzicore.arcade.ArcadeManager;
import com.houzicore.arcade.nautilus.game.arcade.game.Game.GameState;
import com.houzicore.shared.core.command.CommandBase;
import com.houzicore.shared.common.Rank;
import com.houzicore.shared.common.util.C;

public class StartCommand extends CommandBase<ArcadeManager>
{
	public StartCommand(ArcadeManager plugin)
	{
		super(plugin, Rank.ADMIN, new Rank[] {Rank.MAPLEAD, Rank.JNR_DEV}, "start");
	}

	@Override
	public void Execute(Player caller, String[] args)
	{
		if (Plugin.GetGame() == null)
			return;
		
		if (Plugin.GetGame().GetState() == GameState.Loading)
		{
			caller.sendMessage(C.cRed + "The map is currently loading chunks. Please wait...");
			return;
		}
		else if (Plugin.GetGame().GetState() != GameState.Recruit)
		{
			caller.sendMessage(C.cRed + "Game is already in progress...");
			return;
		}

		int seconds;
		if(args != null && args.length > 0)
			seconds = Integer.parseInt(args[0]);
		else 
			seconds = 10;

		Plugin.GetGameManager().StateCountdown(Plugin.GetGame(), seconds, true);

		Plugin.GetGame().Announce(C.cGreen + caller.getName() + " \u00A77\u0e40\u0e23\u0e34\u0e48\u0e21\u0e40\u0e01\u0e21\u0e41\u0e25\u0e49\u0e27");
	}
}
