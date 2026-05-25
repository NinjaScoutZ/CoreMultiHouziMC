package com.houzicore.bungeecord.playerTracker;

import java.io.File;

import com.houzicore.shared.serverdata.Region;
import com.houzicore.shared.serverdata.data.DataRepository;
import com.houzicore.shared.serverdata.data.PlayerStatus;
import com.houzicore.shared.serverdata.redis.RedisDataRepository;
import com.houzicore.shared.serverdata.servers.ServerManager;
import net.md_5.bungee.api.event.PlayerDisconnectEvent;
import net.md_5.bungee.api.event.ServerConnectedEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.api.plugin.Plugin;
import net.md_5.bungee.event.EventHandler;

public class PlayerTracker implements Listener
{
	// Default period before status expiry (8 hours)
	private static final int DEFAULT_STATUS_TIMEOUT = 60 * 60 * 8;

	// Repository storing player status' across network.
	private DataRepository<PlayerStatus> _repository;
	
	private Plugin _plugin;
	
	public PlayerTracker(Plugin plugin)
	{
		_plugin = plugin;

		_plugin.getProxy().getPluginManager().registerListener(_plugin, this);
		
		Region region = !new File("th.dat").exists() ? Region.ASIA : Region.TH;
		_repository = new RedisDataRepository<PlayerStatus>(ServerManager.getMasterConnection(), ServerManager.getSlaveConnection(),
				region, PlayerStatus.class, "playerStatus");
		
	}

	@EventHandler
	public void playerConnect(final ServerConnectedEvent event)
	{
		_plugin.getProxy().getScheduler().runAsync(_plugin, new Runnable()
		{
			public void run()
			{
				PlayerStatus snapshot = new PlayerStatus(event.getPlayer().getName(), event.getServer().getInfo().getName());
				_repository.addElement(snapshot, DEFAULT_STATUS_TIMEOUT);
			}
		});
	}

	@EventHandler
	public void playerDisconnect(final PlayerDisconnectEvent event)
	{
		_plugin.getProxy().getScheduler().runAsync(_plugin, new Runnable()
		{
			public void run()
			{
				_repository.removeElement(event.getPlayer().getName());
			}
		});
	}

	public PlayerStatus getPlayerStatus(String playerName)
	{
		return _repository.getElement(playerName);
	}
}
