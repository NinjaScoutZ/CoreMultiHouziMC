package com.houzicore.lobby.hub.commands;

import org.bukkit.entity.Player;

import com.houzicore.shared.core.command.CommandBase;
import com.houzicore.shared.common.Rank;
import com.houzicore.shared.common.util.F;
import com.houzicore.shared.common.util.UtilPlayer;
import com.houzicore.lobby.hub.server.ServerManager;

public class LobbyReloadCommand extends CommandBase<ServerManager>
{
	public LobbyReloadCommand(ServerManager plugin)
	{
		super(plugin, Rank.ADMIN, new Rank[] {}, new String[] {"lobbyreload"});
	}

	@Override
	public void Execute(Player caller, String[] args)
	{
		if (args == null || args.length == 0)
		{
			UtilPlayer.message(caller, F.main("Lobby Reload", "Usage: /lobbyreload <npc|servers>"));
			return;
		}

		switch (args[0].toLowerCase())
		{
			case "npc":
				Plugin.reloadNpcMappings();
				UtilPlayer.message(caller, F.main("Lobby Reload", "NPC mappings reloaded from npc-mappings.yml."));
				break;

			case "servers":
				Plugin.LoadServers();
				UtilPlayer.message(caller, F.main("Lobby Reload", "All server configurations reloaded."));
				break;

			default:
				UtilPlayer.message(caller, F.main("Lobby Reload", "Unknown target. Try: npc, servers"));
		}
	}
}
