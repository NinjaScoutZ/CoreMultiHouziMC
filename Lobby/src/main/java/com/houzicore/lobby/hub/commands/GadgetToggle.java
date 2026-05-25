package com.houzicore.lobby.hub.commands;

import org.bukkit.entity.Player;

import com.houzicore.shared.core.command.CommandBase;
import com.houzicore.shared.common.Rank;
import com.houzicore.lobby.hub.HubManager;

public class GadgetToggle extends CommandBase<HubManager>
{
	public GadgetToggle(HubManager plugin)
	{
		super(plugin, Rank.ADMIN, new Rank[] {Rank.JNR_DEV}, new String[] {"gadget"});
	}

	@Override
	public void Execute(Player caller, String[] args)
	{
		Plugin.ToggleGadget(caller);
	}
}
