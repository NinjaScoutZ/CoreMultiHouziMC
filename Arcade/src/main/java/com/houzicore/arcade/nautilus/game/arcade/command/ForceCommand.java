package com.houzicore.arcade.nautilus.game.arcade.command;

import org.bukkit.entity.Player;

import com.houzicore.arcade.ArcadeManager;
import com.houzicore.shared.core.command.MultiCommandBase;
import com.houzicore.shared.common.Rank;
import com.houzicore.shared.common.util.F;
import com.houzicore.shared.common.util.UtilPlayer;

public class ForceCommand extends MultiCommandBase<ArcadeManager>
{
	public ForceCommand(ArcadeManager plugin)
	{
		super(plugin, Rank.ADMIN, new Rank[] {Rank.MAPLEAD, Rank.JNR_DEV}, "force");
		
		AddCommand(new ForceStartCommand(Plugin));
		AddCommand(new ForceStopCommand(Plugin));
	}

	@Override
	protected void Help(Player caller, String[] args)
	{
		UtilPlayer.message(caller, F.main(Plugin.getName(), "Force Commands List:"));
		UtilPlayer.message(caller, F.help("/game force start", "Force start the game immediately", Rank.ADMIN));
		UtilPlayer.message(caller, F.help("/game force stop", "Force stop the game immediately", Rank.ADMIN));
	}
}
