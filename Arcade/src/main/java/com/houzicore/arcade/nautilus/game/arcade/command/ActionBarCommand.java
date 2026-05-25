package com.houzicore.arcade.nautilus.game.arcade.command;

import java.util.Arrays;
import java.util.List;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.houzicore.arcade.ArcadeManager;
import com.houzicore.shared.common.Rank;
import com.houzicore.shared.common.actionbar.ActionBarDebugSupport;
import com.houzicore.shared.core.command.CommandBase;

public class ActionBarCommand extends CommandBase<ArcadeManager>
{
	public ActionBarCommand(ArcadeManager plugin)
	{
		super(plugin, Rank.ADMIN, new Rank[] {Rank.MAPLEAD, Rank.JNR_DEV}, "actionbar", "abar");
	}

	@Override
	public void Execute(Player caller, String[] args)
	{
		ActionBarDebugSupport.execute(caller, args, "/game actionbar", Rank.ADMIN);
	}

	@Override
	public List<String> onTabComplete(CommandSender sender, String commandLabel, String[] args)
	{
		if (args != null && args.length > 0 && (args[0].equalsIgnoreCase("actionbar") || args[0].equalsIgnoreCase("abar")))
			return ActionBarDebugSupport.tabComplete(sender, Arrays.copyOfRange(args, 1, args.length));

		return ActionBarDebugSupport.tabComplete(sender, args);
	}
}
