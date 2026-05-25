package com.houzicore.bungeecord.lobbyBalancer;

import java.io.File;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;

import com.houzicore.shared.serverdata.Region;
import com.houzicore.shared.serverdata.data.MinecraftServer;
import com.houzicore.shared.serverdata.servers.ServerManager;
import com.houzicore.shared.serverdata.servers.ServerRepository;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.event.ServerConnectEvent;
import net.md_5.bungee.api.event.ServerKickEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.api.plugin.Plugin;
import net.md_5.bungee.event.EventHandler;


@SuppressWarnings("unused")
public class LobbyBalancer implements Listener, Runnable
{
	private Plugin _plugin;
	private ServerRepository _repository;
	
	private List<MinecraftServer> _sortedLobbies = new ArrayList<MinecraftServer>();
	private static Object _serverLock = new Object();
	
	private int _lobbyIndex = 0;
	
	public LobbyBalancer(Plugin plugin)
	{
		_plugin = plugin;
		
		Region region = !new File("th.dat").exists() ? Region.ASIA : Region.TH;
		_repository = ServerManager.getServerRepository(region);
		
		loadLobbyServers();
		
		_plugin.getProxy().getPluginManager().registerListener(_plugin, this);
		_plugin.getProxy().getScheduler().schedule(_plugin, this, 250L, 250L, TimeUnit.MILLISECONDS);
	}
	
	public ServerInfo getBestLobby(String excludeServer)
	{
		synchronized (_serverLock)
		{
			if (_sortedLobbies.isEmpty())
				return null;

			// Ensure index is within bounds
			if (_lobbyIndex >= _sortedLobbies.size())
				_lobbyIndex = 0;

			// Check first target
			MinecraftServer target = _sortedLobbies.get(_lobbyIndex);
			
			// Try finding a non-excluded server that isn't full, up to N times
			for (int i = 0; i < _sortedLobbies.size(); i++)
			{
				if (target.getName().equalsIgnoreCase(excludeServer) || target.getPlayerCount() >= target.getMaxPlayerCount())
				{
					_lobbyIndex++;
					if (_lobbyIndex >= _sortedLobbies.size())
						_lobbyIndex = 0;
					target = _sortedLobbies.get(_lobbyIndex);
				}
				else
				{
					break; // Found good target
				}
			}

			// If STILL excluded (meaning ALL lobbies match the excluded server), we have to return it or null.
			if (target.getName().equalsIgnoreCase(excludeServer))
				return null;

			ServerInfo serverInfo = _plugin.getProxy().getServerInfo(target.getName());
			if (serverInfo != null)
			{
				target.incrementPlayerCount(1);
				_lobbyIndex++;
				return serverInfo;
			}
		}
		
		return null;
	}

	@EventHandler
	public void playerConnect(ServerConnectEvent event)
	{
		if (!event.getTarget().getName().equalsIgnoreCase("Lobby"))
			return;

		ServerInfo targetInfo = getBestLobby(null);
		if (targetInfo != null)
		{
			event.setTarget(targetInfo);
		}
		else
		{
		}
	}
	
	@EventHandler
	public void onServerKick(ServerKickEvent event)
	{
		// Don't intercept kicks from the Bungee proxy itself
		if (event.getKickedFrom() == null)
			return;
			
		String kickReason = net.md_5.bungee.api.chat.BaseComponent.toPlainText(event.getKickReasonComponent()).toLowerCase();
		
		// Typically manual kicks/bans explicitly contain these words. 
		// We only want to rescue players who are kicked due to backend shutdown/closure.
		if (kickReason.contains("banned") || kickReason.contains("kicked") || kickReason.contains("afk") || kickReason.contains("spam"))
			return;
			
		// Proceed with fallback for shutdowns, "Server closed", "Restarting", or standard closures
		ServerInfo targetInfo = getBestLobby(event.getKickedFrom().getName());
		
		if (targetInfo != null)
		{
			event.setCancelled(true);
			event.setCancelServer(targetInfo);
			
			net.md_5.bungee.api.chat.BaseComponent[] message = net.md_5.bungee.api.chat.TextComponent.fromLegacyText(
				"§c§l\u26A0 §fThe server you were on was closed. Sending you to " + targetInfo.getName() + "..."
			);
			event.getPlayer().sendMessage(message);
			
		}
	}
	
	public void run()
	{
		loadLobbyServers();
	}
    
	public void loadLobbyServers()
	{		
		Collection<MinecraftServer> servers = _repository.getServerStatuses();
			
		synchronized (_serverLock)
		{
			long startTime = System.currentTimeMillis();
			_sortedLobbies.clear();
			
			for (MinecraftServer server : servers)
			{
				if (server.getName() == null)
					continue;
				
				InetSocketAddress socketAddress = new InetSocketAddress(server.getPublicAddress(), server.getPort());
				_plugin.getProxy().getServers().put(server.getName(), _plugin.getProxy().constructServerInfo(server.getName(), socketAddress, "LobbyBalancer", false));
				
	    		if (server.getName().toUpperCase().contains("LOBBY"))
	    		{
	    			if (server.getMotd() == null || !server.getMotd().contains("Restarting"))
	    			{
	    				_sortedLobbies.add(server);
	    			}
	    		}
			}
			
			Collections.sort(_sortedLobbies, new LobbySorter());
			
            long timeSpentInLock = System.currentTimeMillis() - startTime;
            
            // if (timeSpentInLock > 50)
		}
	}
}
