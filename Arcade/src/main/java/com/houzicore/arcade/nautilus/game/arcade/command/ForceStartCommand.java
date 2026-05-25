package com.houzicore.arcade.nautilus.game.arcade.command;

import org.bukkit.entity.Player;

import com.houzicore.arcade.ArcadeManager;
import com.houzicore.arcade.GameType;
import com.houzicore.arcade.nautilus.game.arcade.game.Game;
import com.houzicore.arcade.nautilus.game.arcade.game.Game.GameState;
import com.houzicore.shared.core.command.CommandBase;
import com.houzicore.shared.common.Rank;
import com.houzicore.shared.common.util.C;
import com.houzicore.shared.common.util.F;
import com.houzicore.shared.common.util.UtilMath;

public class ForceStartCommand extends CommandBase<ArcadeManager>
{
	public ForceStartCommand(ArcadeManager plugin)
	{
		super(plugin, Rank.ADMIN, new Rank[] {Rank.MAPLEAD, Rank.JNR_DEV}, "start");
	}

	@Override
	public void Execute(Player caller, String[] args)
	{
		Game game = Plugin.GetGame();
		
		if (game == null)
		{
			if (Plugin.GetGameList().isEmpty())
			{
				caller.sendMessage(F.main("Game", "No playable games registered."));
				return;
			}
			
			GameType type = Plugin.GetGameList().get(UtilMath.r(Plugin.GetGameList().size()));
			Plugin.GetGameCreationManager().forceCreateGame(type);
			game = Plugin.GetGame();
			
			if (game == null)
			{
				caller.sendMessage(F.main("Game", "Failed to create game of type: " + type.name()));
				return;
			}
		}

		game.ForceStart = true;
		
		GameState state = game.GetState();
		
		if (state == GameState.Vote || state == GameState.Loading)
		{
			java.util.ArrayList<String> maps = game.GetFiles().get(game.GetType());
			String selectedMap = null;
			if (maps != null && !maps.isEmpty())
			{
				selectedMap = maps.get(UtilMath.r(maps.size()));
			}
			
			game.SetState(GameState.Loading);
			game.loadWorld(selectedMap);
			
			game.Announce(C.cGreen + C.Bold + caller.getName() + " has force started the game! Loading random map...");
		}
		else if (state == GameState.Recruit)
		{
			game.SetState(GameState.Prepare);
			game.Announce(C.cGreen + C.Bold + caller.getName() + " has force started the game!");
		}
		else if (state == GameState.Prepare)
		{
			game.Announce(C.cGreen + C.Bold + caller.getName() + " has force started the game!");
		}
		else if (state == GameState.Live)
		{
			caller.sendMessage(F.main("Game", "Game is already live!"));
		}
		else if (state == GameState.End || state == GameState.Dead)
		{
			caller.sendMessage(F.main("Game", "Game is already ending or dead."));
		}
	}
}
