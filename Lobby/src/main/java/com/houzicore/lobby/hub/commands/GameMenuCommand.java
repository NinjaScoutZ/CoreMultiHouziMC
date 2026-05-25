package com.houzicore.lobby.hub.commands;

import org.bukkit.Sound;
import org.bukkit.entity.Player;

import com.houzicore.lobby.hub.server.ServerManager;
import com.houzicore.shared.common.Rank;
import com.houzicore.shared.core.command.CommandBase;

public class GameMenuCommand extends CommandBase<ServerManager> {
	public GameMenuCommand(ServerManager plugin) {
		super(plugin, Rank.ALL, "games", "play", "quickplay", "minigames");
	}

	@Override
	public void Execute(Player caller, String[] args) {
		caller.playSound(caller.getLocation(), Sound.UI_BUTTON_CLICK, 0.85f, 1.15f);

		if (args.length > 0)
		{
			String joined = String.join(" ", args).toLowerCase().replace("-", "").replace("_", "").replace(" ", "");
			String serverKey = null;

			if (joined.equals("prop") || joined.equals("proprush") || joined.equals("hideandseek") || joined.equals("hideseek") || joined.equals("hs") || joined.equals("bh"))
			{
				serverKey = "BH";
			}
			else if (joined.equals("arcade") || joined.equals("micro") || joined.equals("microgames") || joined.equals("min") || joined.equals("minigames"))
			{
				serverKey = "MIN";
			}
			else if (joined.equals("primal") || joined.equals("survival") || joined.equals("survivalgames") || joined.equals("sg") || joined.equals("hg"))
			{
				serverKey = "HG";
			}

			if (serverKey != null)
			{
				caller.playSound(caller.getLocation(), Sound.BLOCK_AMETHYST_BLOCK_CHIME, 0.75f, 1.4f);
				Plugin.performQuickMatch(caller, serverKey);
				return;
			}
		}

		Plugin.getQuickShop().attemptShopOpen(caller);
	}
}
