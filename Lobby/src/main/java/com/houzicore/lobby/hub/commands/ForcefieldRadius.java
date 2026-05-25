package com.houzicore.lobby.hub.commands;

import org.bukkit.entity.Player;

import com.houzicore.shared.core.command.CommandBase;
import com.houzicore.shared.common.Rank;
import com.houzicore.lobby.hub.modules.ForcefieldManager;

public class ForcefieldRadius extends CommandBase<ForcefieldManager>
{
	public ForcefieldRadius(ForcefieldManager plugin)
	{
		super(plugin, Rank.OWNER, new String[] {"radius"});
	}

	@Override
	public void Execute(Player caller, String[] args)
	{
		Plugin.ForcefieldRadius(caller, args);
	}
}
