package com.houzicore.arcade.nautilus.game.arcade.command;

import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;

import com.houzicore.arcade.ArcadeManager;
import com.houzicore.arcade.nautilus.game.arcade.game.Game;
import com.houzicore.arcade.nautilus.game.arcade.game.Game.GameState;
import com.houzicore.shared.core.command.CommandBase;
import com.houzicore.shared.common.Rank;
import com.houzicore.shared.common.util.C;
import com.houzicore.shared.common.util.F;

public class ForceStopCommand extends CommandBase<ArcadeManager>
{
	public ForceStopCommand(ArcadeManager plugin)
	{
		super(plugin, Rank.ADMIN, new Rank[] {Rank.MAPLEAD, Rank.JNR_DEV}, "stop");
	}

	@Override
	public void Execute(Player caller, String[] args)
	{
		Game game = Plugin.GetGame();
		
		if (game == null)
		{
			caller.sendMessage(F.main("Game", "There is no game currently running."));
			return;
		}

		if (game.GetState() == GameState.Dead)
		{
			caller.sendMessage(F.main("Game", "Game is already stopped."));
			return;
		}

		game.SetState(GameState.Dead);
		HandlerList.unregisterAll(game);

		game.Announce(C.cRed + C.Bold + caller.getName() + " has force stopped the game.");
	}
}
