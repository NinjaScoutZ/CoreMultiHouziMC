package com.houzicore.shared.core.status;

import com.houzicore.shared.common.util.F;
import com.houzicore.shared.core.portal.Portal;
import com.houzicore.shared.serverdata.Region;
import com.houzicore.shared.serverdata.commands.CommandCallback;
import com.houzicore.shared.serverdata.commands.ServerCommand;
import com.houzicore.shared.serverdata.commands.SuicideCommand;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public class SuicideHandler implements CommandCallback {
	private final ServerStatusManager _statusManager;
	private final String _serverName;
	private final Region _region;

	public SuicideHandler(ServerStatusManager statusManager, String serverName, Region region) {
		_statusManager = statusManager;
		_serverName = serverName;
		_region = region;
	}

	@Override
	public void run(ServerCommand command) {
		if (command instanceof SuicideCommand) {
			final String serverName = ((SuicideCommand) command).getServerName();
			final Region region = ((SuicideCommand) command).getRegion();

			if (!serverName.equalsIgnoreCase(_serverName) || _region != region)
				return;

			for (final Player player : Bukkit.getOnlinePlayers()) {
				player.sendMessage(F.main("Cleanup", "Server is being cleaned up, you're being sent to a lobby."));
			}

			Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(Bukkit.getPluginManager().getPlugins()[0],
					new Runnable() {
						@Override
						public void run() {
							Portal.getInstance().sendAllPlayers("Lobby");
						}
					}, 60L);

			_statusManager.disableStatus();
		}
	}
}
