package com.houzicore.arcade.nautilus.game.arcade.command;

import com.houzicore.shared.common.util.F;
import com.houzicore.shared.common.util.UtilPlayer;
import org.bukkit.entity.Player;

import com.houzicore.arcade.ArcadeManager;
import com.houzicore.shared.core.command.MultiCommandBase;
import com.houzicore.shared.common.Rank;
import com.houzicore.shared.command.CommandMeta;

@CommandMeta(
    description = "Arcade minigame management commands.",
    usage = "/game <start|stop|set|actionbar|kit|...>",
    permission = Rank.ADMIN,
    aliases = {"game"}
)
public class GameCommand extends MultiCommandBase<ArcadeManager>
{
	public GameCommand(ArcadeManager plugin)
	{
		super(plugin, Rank.ADMIN, new Rank[] {Rank.MAPLEAD, Rank.JNR_DEV}, "game");
		
		AddCommand(new StartCommand(Plugin));
		AddCommand(new StopCommand(Plugin));
		AddCommand(new SetCommand(Plugin));
		AddCommand(new ActionBarCommand(Plugin));
		AddCommand(new ForceCommand(Plugin));

	}

	@Override
	protected void Help(Player caller, String[] args)
	{
		UtilPlayer.message(caller, F.main(Plugin.getName(), "Commands List:"));
		UtilPlayer.message(caller, F.help("/game start", "Start the current game", Rank.ADMIN));
		UtilPlayer.message(caller, F.help("/game stop", "Stop the current game", Rank.ADMIN));
		UtilPlayer.message(caller, F.help("/game force <start|stop>", "Force start or stop the game immediately", Rank.ADMIN));
		UtilPlayer.message(caller, F.help("/game set <GameType> (MapSource) (Map)", "Set the current game or next game", Rank.ADMIN));
		UtilPlayer.message(caller, F.help("/game actionbar ...", "Inspect or send actionbar smoke messages", Rank.ADMIN));
		UtilPlayer.message(caller, F.help("/game diag [cleanup]", "Runtime diagnostics and emergency cleanup", Rank.ADMIN));
		UtilPlayer.message(caller, F.main("Tip", "Use TAB for games/maps!"));
	}
}
