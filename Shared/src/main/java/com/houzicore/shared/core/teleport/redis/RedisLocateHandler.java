package com.houzicore.shared.core.teleport.redis;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import com.houzicore.shared.core.teleport.Teleport;
import com.houzicore.shared.serverdata.commands.CommandCallback;
import com.houzicore.shared.serverdata.commands.ServerCommand;

public class RedisLocateHandler implements CommandCallback {
	private final Teleport _plugin;
	private final String _serverName;

	public RedisLocateHandler(Teleport plugin) {
		_plugin = plugin;
		_serverName = _plugin.getPlugin().getConfig().getString("serverstatus.name");
	}

	@Override
	public void run(ServerCommand command) {
		if (command instanceof RedisLocate) {
			final RedisLocate locate = (RedisLocate) command;

			final Player target = Bukkit.getPlayerExact(locate.getTarget());

			if (target != null) {
				final RedisLocateCallback callback = new RedisLocateCallback(locate, _serverName, target.getName());
				callback.publish();
			}
		} else if (command instanceof RedisLocateCallback) {
			_plugin.handleLocateCallback((RedisLocateCallback) command);
		}
	}

}
