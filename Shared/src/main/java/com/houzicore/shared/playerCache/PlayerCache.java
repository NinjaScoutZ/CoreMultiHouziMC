package com.houzicore.shared.playerCache;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import com.houzicore.shared.serverdata.Region;
import com.houzicore.shared.serverdata.redis.RedisDataRepository;
import com.houzicore.shared.serverdata.servers.ServerManager;

public class PlayerCache
{
	private RedisDataRepository<PlayerInfo> _repository;
	
	// In-memory fallback when Redis is unavailable
	private Map<String, PlayerInfo> _localCache;
	private boolean _useLocalCache;

	public PlayerCache()
	{
		_useLocalCache = !ServerManager.isRedisEnabled();
		
		if (_useLocalCache)
		{
			_localCache = new HashMap<String, PlayerInfo>();
		}
		else
		{
			try
			{
				_repository = new RedisDataRepository<PlayerInfo>(
						ServerManager.getMasterConnection(), 
						ServerManager.getSlaveConnection(),
						Region.ALL, 
						PlayerInfo.class, 
						"playercache");
			}
			catch (Exception e)
			{
				_useLocalCache = true;
				_localCache = new HashMap<String, PlayerInfo>();
			}
		}
	}
	
	public void addPlayer(PlayerInfo player)
	{
		if (_useLocalCache)
		{
			_localCache.put(player.getUUID().toString(), player);
			return;
		}
		_repository.addElement(player, 60 * 60 * 6);  // 6 Hours
	}
	
	public PlayerInfo getPlayer(UUID uuid)
	{
		if (_useLocalCache)
		{
			return _localCache.get(uuid.toString());
		}
		return _repository.getElement(uuid.toString());
	}
	
	public void clean()
	{
		if (_useLocalCache)
		{
			// No-op for local cache (no TTL management needed)
			return;
		}
		_repository.clean();
	}
}
