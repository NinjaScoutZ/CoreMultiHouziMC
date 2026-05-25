package com.houzicore.arcade.nautilus.game.arcade.command;

import org.bukkit.entity.Player;

import com.houzicore.arcade.ArcadeManager;
import com.houzicore.shared.core.command.CommandBase;
import com.houzicore.shared.common.Rank;

public class KitUnlockCommand extends CommandBase<ArcadeManager>
{
	public KitUnlockCommand(ArcadeManager plugin)
	{
		super(plugin, Rank.OWNER, new Rank[] {Rank.YOUTUBE, Rank.TWITCH}, new String[] {"youtube", "twitch", "kits"});
	}

	@Override
	public void Execute(Player caller, String[] args)
	{
		Plugin.toggleUnlockKits(caller);
	}
}
