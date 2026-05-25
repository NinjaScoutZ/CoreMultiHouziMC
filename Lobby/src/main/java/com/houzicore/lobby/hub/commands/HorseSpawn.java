package com.houzicore.lobby.hub.commands;

import org.bukkit.entity.Player;

import com.houzicore.shared.core.command.CommandBase;
import com.houzicore.shared.common.Rank;
import com.houzicore.lobby.hub.modules.AdminMountManager;

public class HorseSpawn extends CommandBase<AdminMountManager>
{
	public HorseSpawn(AdminMountManager plugin)
	{
		super(plugin, Rank.OWNER, new String[] {"horse"});
	}

	@Override
	public void Execute(Player caller, String[] args)
	{
		Plugin.HorseCommand(caller, args);
	}
}
