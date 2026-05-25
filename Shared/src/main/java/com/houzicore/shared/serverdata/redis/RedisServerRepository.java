package com.houzicore.shared.serverdata.redis;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import com.houzicore.shared.serverdata.Region;
import com.houzicore.shared.serverdata.Utility;
import com.houzicore.shared.serverdata.data.DedicatedServer;
import com.houzicore.shared.serverdata.data.MinecraftServer;
import com.houzicore.shared.serverdata.data.ServerGroup;
import com.houzicore.shared.serverdata.servers.ConnectionData;
import com.houzicore.shared.serverdata.servers.ServerRepository;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.Pipeline;
import redis.clients.jedis.Response;
import redis.clients.jedis.Transaction;
import redis.clients.jedis.resps.Tuple;
import redis.clients.jedis.exceptions.JedisConnectionException;

/**
 * RedisServerRepository offers a Redis-based implementation of {@link ServerRepository}
 * using a mixture of hash and JSON encoded storage.
 * @author Ty
 *
 */
public class RedisServerRepository implements ServerRepository
{

	// The delimiter character used for redis key paths
	public final char KEY_DELIMITER = '.';
	
	// The pool used to retrieve jedis instances.
	private JedisPool _writePool;
	private JedisPool _readPool;
	
	// The geographical region of the servers stored by this ServerRepository
	private Region _region;
	
	// Whether Redis is available for this repository
	private boolean _redisAvailable;
	
	private static boolean _loggedStandalone = false;
	
	/**
	 * Class constructor
	 * @param host
	 * @param port
	 */
	public RedisServerRepository(ConnectionData writeConn, ConnectionData readConn, Region region)
	{
		_region = region;
		
		if (writeConn != null && readConn != null)
		{
			try
			{
				_writePool = Utility.generatePool(writeConn);
				_readPool = (writeConn == readConn) ? _writePool : Utility.generatePool(readConn);
				_redisAvailable = (_writePool != null && _readPool != null);
			}
			catch (Exception e)
			{
				_redisAvailable = false;
			}
		}
		else
		{
			_redisAvailable = false;
		}
		
		if (!_redisAvailable && !_loggedStandalone)
		{
			_loggedStandalone = true;
		}
	}
	
	@Override
	public Collection<MinecraftServer> getServerStatuses() 
	{
		return getServerStatusesByPrefix("");
	}

	@Override
	public Collection<MinecraftServer> getServerStatusesByPrefix(String prefix) 
	{
		Collection<MinecraftServer> servers = new HashSet<MinecraftServer>();
		
		if (!_redisAvailable) return servers;
		
		Jedis jedis = null;
		
		try
		{
			jedis = _readPool.getResource();
			String setKey = concatenate("serverstatus", "minecraft", _region.toString());
			Pipeline pipeline = jedis.pipelined();

			List<Response<String>> responses = new ArrayList<Response<String>>();
			for (String serverName : getActiveNames(setKey))
			{
				if (prefix.isEmpty() || serverName.startsWith(prefix))
				{
					String dataKey = concatenate(setKey, serverName);
					responses.add(pipeline.get(dataKey));
				}
			}
			
			pipeline.sync();
			
			for (Response<String> response : responses)
			{
				String serializedData = response.get();
				MinecraftServer server = Utility.deserialize(serializedData, MinecraftServer.class);
				
				if (server != null)
				{
					servers.add(server);
				}
			}
		}
		catch (Exception exception)
		{
			if (exception instanceof JedisConnectionException)
				System.out.println("[RedisServerRepository] Warning: " + "Redis Timeout in getServerStatusesByPrefix: " + exception.getMessage());
			else
				{ System.out.println("[RedisServerRepository] Error: " + exception.getMessage()); exception.printStackTrace(); }
			
			if (jedis != null) {
				_readPool.returnBrokenResource(jedis);
				jedis = null;
			}
		}
		finally
		{
			if (jedis != null)
			{
				_readPool.returnResource(jedis);
			}
		}
		
		return servers;
	}
	
	@Override
	public Collection<MinecraftServer> getServersByGroup(String serverGroup)
	{
		Collection<MinecraftServer> servers = new HashSet<MinecraftServer>();
		
		for (MinecraftServer server : getServerStatuses())
		{
			if (server.getGroup().equalsIgnoreCase(serverGroup))
			{
				servers.add(server);
			}
		}
		
		return servers;
	}
	
	@Override
	public MinecraftServer getServerStatus(String serverName)
	{
		if (!_redisAvailable) return null;
		
		MinecraftServer server = null;
		Jedis jedis = null;
		
		try
		{
			jedis = _readPool.getResource();
			String setKey = concatenate("serverstatus", "minecraft", _region.toString());
			String dataKey = concatenate(setKey, serverName);
			String serializedData = jedis.get(dataKey);
			server = Utility.deserialize(serializedData, MinecraftServer.class);
		}
		catch (Exception exception)
		{
			if (exception instanceof JedisConnectionException)
				System.out.println("[RedisServerRepository] Warning: " + "Redis Timeout in getServerStatus: " + exception.getMessage());
			else
				{ System.out.println("[RedisServerRepository] Error: " + exception.getMessage()); exception.printStackTrace(); }
				
			if (jedis != null) {
				_readPool.returnBrokenResource(jedis);
				jedis = null;
			}
		}
		finally
		{
			if (jedis != null)
			{
				_readPool.returnResource(jedis);
			}
		}
		
		return server;
	}

	@Override
	public void updataServerStatus(MinecraftServer serverData, int timeout) 
	{
		if (!_redisAvailable) return;
		
		Jedis jedis = null;
		
		try
		{
			jedis = _writePool.getResource();
			String serializedData = Utility.serialize(serverData);
			String serverName = serverData.getName();
			String setKey = concatenate("serverstatus", "minecraft", _region.toString());
			String dataKey = concatenate(setKey, serverName);
			long expiry = Utility.currentTimeSeconds() + timeout;
			
			Transaction transaction = jedis.multi();
			transaction.set(dataKey, serializedData);
			transaction.zadd(setKey, expiry, serverName);
			transaction.exec();
		}
		catch (Exception exception)
		{
			if (exception instanceof JedisConnectionException)
				System.out.println("[RedisServerRepository] Warning: " + "Redis Timeout in updataServerStatus: " + exception.getMessage());
			else
				{ System.out.println("[RedisServerRepository] Error: " + exception.getMessage()); exception.printStackTrace(); }
				
			if (jedis != null) {
				_writePool.returnBrokenResource(jedis);
				jedis = null;
			}
		}
		finally
		{
			if (jedis != null)
			{
				_writePool.returnResource(jedis);
			}
		}
	}

	@Override
	public void removeServerStatus(MinecraftServer serverData) 
	{
		if (!_redisAvailable) return;
		
		Jedis jedis = null;
		
		try
		{
			jedis = _writePool.getResource();
			String serverName = serverData.getName();
			String setKey = concatenate("serverstatus", "minecraft", _region.toString());
			String dataKey = concatenate(setKey, serverName);
			
			Transaction transaction = jedis.multi();
			transaction.del(dataKey);
			transaction.zrem(setKey, serverName);
			transaction.exec();
		}
		catch (Exception exception)
		{
			if (exception instanceof JedisConnectionException)
				System.out.println("[RedisServerRepository] Warning: " + "Redis Timeout in removeServerStatus: " + exception.getMessage());
			else
				{ System.out.println("[RedisServerRepository] Error: " + exception.getMessage()); exception.printStackTrace(); }
				
			if (jedis != null) {
				_writePool.returnBrokenResource(jedis);
				jedis = null;
			}
		}
		finally
		{
			if (jedis != null)
			{
				_writePool.returnResource(jedis);
			}
		}
	}

	@Override
	public boolean serverExists(String serverName)
	{
		return getServerStatus(serverName) != null;
	}
	
	@Override
	public Collection<DedicatedServer> getDedicatedServers()
	{
		Collection<DedicatedServer> servers = new HashSet<DedicatedServer>();
		
		if (!_redisAvailable) return servers;
		
		Jedis jedis = null;
		
		try
		{
			jedis = _readPool.getResource();
			String key = concatenate("serverstatus", "dedicated");
			Set<String> serverNames = jedis.smembers(key);
			HashMap<String, Response<Map<String, String>>> serverDatas = new HashMap<String, Response<Map<String, String>>>();
			
			Pipeline pipeline = jedis.pipelined();
			
			for (String serverName : serverNames)
			{
				String dataKey = concatenate(key, serverName);
				serverDatas.put(serverName, pipeline.hgetAll(dataKey));
			}
			
			pipeline.sync();

			for (Entry<String, Response<Map<String, String>>> responseEntry : serverDatas.entrySet())
			{
				Map<String, String> data = responseEntry.getValue().get();
				
				try
				{
					DedicatedServer server = new DedicatedServer(data);
					
					if (server.getRegion() == _region)
						servers.add(server);
				}
				catch (Exception ex)
				{
					throw ex;
				}
			}
		}
		catch (Exception exception)
		{
			{ System.out.println("[RedisServerRepository] Error: " + exception.getMessage()); exception.printStackTrace(); }
			_readPool.returnBrokenResource(jedis);
			jedis = null;
		}
		finally
		{
			if (jedis != null)
			{
				_readPool.returnResource(jedis);
			}
		}
		
		return servers;
	}

	@Override
	public Collection<ServerGroup> getServerGroups(Collection<MinecraftServer> serverStatuses) 
	{
		Collection<ServerGroup> servers = new HashSet<ServerGroup>();
		
		if (!_redisAvailable) return servers;
		
		Jedis jedis = null;
		try
		{
			jedis = _readPool.getResource();
			
			String key = "servergroups";
			Set<String> names = jedis.smembers(key);
			Set<Response<Map<String, String>>> serverDatas = new HashSet<Response<Map<String, String>>>();

			Pipeline pipeline = jedis.pipelined();

			for (String serverName : names)
			{
				String dataKey = concatenate(key, serverName);
				serverDatas.add(pipeline.hgetAll(dataKey));
			}
			
			pipeline.sync();
			
			for (Response<Map<String, String>> response : serverDatas)
			{
				Map<String, String> data = response.get();
				
				try
				{
					ServerGroup serverGroup = new ServerGroup(data, serverStatuses);
					
					if (serverGroup.getRegion() == Region.ALL || serverGroup.getRegion() == _region)
						servers.add(serverGroup);
				}
				catch (Exception exception)
				{
					{ System.out.println("[RedisServerRepository] Error: " + exception.getMessage()); exception.printStackTrace(); }
				}
			}
		}
		catch (Exception exception)
		{
			{ System.out.println("[RedisServerRepository] Error: " + exception.getMessage()); exception.printStackTrace(); }
			_readPool.returnBrokenResource(jedis);
			jedis = null;
		}
		finally
		{
			if (jedis != null)
			{
				_readPool.returnResource(jedis);
			}
		}
		
		return servers;
	}

	/**
	 * @param key - the key where the sorted set of server sessions is stored
	 * @return the {@link Set} of active server names stored at {@code key} for non-expired
	 * servers.
	 */
	protected Set<String> getActiveNames(String key)
	{
		Set<String> names = new HashSet<String>();
		
		if (!_redisAvailable) return names;
		
		Jedis jedis = null;
		
		try
		{
			jedis = _readPool.getResource();
			String min = "(" + Utility.currentTimeSeconds();
			String max = "+inf";
			names = new HashSet<>(jedis.zrangeByScore(key, min, max));
		}
		catch (Exception exception)
		{
			{ System.out.println("[RedisServerRepository] Error: " + exception.getMessage()); exception.printStackTrace(); }
			_readPool.returnBrokenResource(jedis);
			jedis = null;
		}
		finally
		{
			if (jedis != null)
			{
				_readPool.returnResource(jedis);
			}
		}
		
		return names;
	}
	
	/**
	 * @param key - the key where the sorted set of server sessions is stored
	 * @return the {@link Set} of dead (expired) server names stored at {@code key}.
	 */
	protected Set<String> getDeadNames(String key)
	{
		Set<String> names = new HashSet<String>();
		
		if (!_redisAvailable) return names;
		
		Jedis jedis = null;
		
		try
		{
			jedis = _readPool.getResource();
			String min = "-inf";
			String max = Utility.currentTimeSeconds() + "";
			names = new HashSet<>(jedis.zrangeByScore(key, min, max));
		}
		catch (Exception exception)
		{
			{ System.out.println("[RedisServerRepository] Error: " + exception.getMessage()); exception.printStackTrace(); }
			_readPool.returnBrokenResource(jedis);
			jedis = null;
		}
		finally
		{
			if (jedis != null)
			{
				_readPool.returnResource(jedis);
			}
		}
		
		return names;
	}
	
	/**
	 * @param elements - the elements to concatenate together
	 * @return the concatenated form of all {@code elements} 
	 * separated by the delimiter {@value KEY_DELIMITER}.
	 */
	protected String concatenate(String... elements)
	{
		return Utility.concatenate(KEY_DELIMITER, elements);
	}
	
	@Override
	public Collection<MinecraftServer> getDeadServers()
	{
		Set<MinecraftServer> servers = new HashSet<MinecraftServer>();
		
		if (!_redisAvailable) return servers;
		
		Jedis jedis = null;
		
		try
		{
			jedis = _readPool.getResource();
			Pipeline pipeline = jedis.pipelined();
			String setKey = concatenate("serverstatus", "minecraft", _region.toString());
			String min = "-inf";
			String max = Utility.currentTimeSeconds() + "";
		
			List<Response<String>> responses = new ArrayList<Response<String>>();
			for (Tuple serverName : jedis.zrangeByScoreWithScores(setKey, min, max))
			{
				String dataKey = concatenate(setKey, serverName.getElement());
				responses.add(pipeline.get(dataKey));
			}
			
			pipeline.sync();
			
			for (Response<String> response : responses)
			{
				String serializedData = response.get();
				MinecraftServer server = Utility.deserialize(serializedData, MinecraftServer.class);
				
				if (server != null)
					servers.add(server);
			}
		}
		catch (Exception exception)
		{
			if (exception instanceof JedisConnectionException)
				System.out.println("[RedisServerRepository] Warning: " + "Redis Timeout in getDeadServers: " + exception.getMessage());
			else
				{ System.out.println("[RedisServerRepository] Error: " + exception.getMessage()); exception.printStackTrace(); }
				
			if (jedis != null) {
				_readPool.returnBrokenResource(jedis);
				jedis = null;
			}
		}
		finally
		{
			if (jedis != null)
			{
				_readPool.returnResource(jedis);
			}
		}
		
		return servers;
	}
	
	@Override
	public void updateServerGroup(ServerGroup serverGroup)
	{
		if (!_redisAvailable) return;
		
		Jedis jedis = null;
		
		try
		{
			jedis = _writePool.getResource();
			HashMap<String, String> serializedData = serverGroup.getDataMap();
			String serverGroupName = serverGroup.getName();
			String key = "servergroups";
			String dataKey = concatenate(key, serverGroupName);
			
			Transaction transaction = jedis.multi();
			transaction.hmset(dataKey, serializedData);
			transaction.sadd(key, serverGroupName);
			transaction.exec();
		}
		catch (Exception exception)
		{
			if (exception instanceof JedisConnectionException)
				System.out.println("[RedisServerRepository] Warning: " + "Redis Timeout in updateServerGroup: " + exception.getMessage());
			else
				{ System.out.println("[RedisServerRepository] Error: " + exception.getMessage()); exception.printStackTrace(); }
			
			if (jedis != null) {
				_writePool.returnBrokenResource(jedis);
				jedis = null;
			}
		}
		finally
		{
			if (jedis != null)
			{
				_writePool.returnResource(jedis);
			}
		}
	}

	@Override
	public void removeServerGroup(ServerGroup serverGroup)
	{
		if (!_redisAvailable) return;
		
		Jedis jedis = null;
		
		try
		{
			jedis = _writePool.getResource();
			String serverName = serverGroup.getName();
			String setKey = "servergroups";
			String dataKey = concatenate(setKey, serverName);
			
			Transaction transaction = jedis.multi();
			transaction.del(dataKey);
			transaction.srem(setKey, serverName);
			transaction.exec();
		}
		catch (Exception exception)
		{
			if (exception instanceof JedisConnectionException)
				System.out.println("[RedisServerRepository] Warning: " + "Redis Timeout in removeServerGroup: " + exception.getMessage());
			else
				{ System.out.println("[RedisServerRepository] Error: " + exception.getMessage()); exception.printStackTrace(); }
			
			if (jedis != null) {
				_writePool.returnBrokenResource(jedis);
				jedis = null;
			}
		}
		finally
		{
			if (jedis != null)
			{
				_writePool.returnResource(jedis);
			}
		}
	}

	@Override
	public ServerGroup getServerGroup(String serverGroup)
	{
		if (!_redisAvailable) return null;
		
		ServerGroup server = null;
		Jedis jedis = null;
		try
		{
			jedis = _readPool.getResource();
			String key = concatenate("servergroups", serverGroup);
			Map<String, String> data = jedis.hgetAll(key);

			if (data != null && !data.isEmpty())
			{
				server = new ServerGroup(data, null);
			}
		}
		catch (Exception exception)
		{
			if (exception instanceof JedisConnectionException)
				System.out.println("[RedisServerRepository] Warning: " + "Redis Timeout in getServerGroup: " + exception.getMessage());
			else
				{ System.out.println("[RedisServerRepository] Error: " + exception.getMessage()); exception.printStackTrace(); }
			
			if (jedis != null) {
				_readPool.returnBrokenResource(jedis);
				jedis = null;
			}
		}
		finally
		{
			if (jedis != null)
			{
				_readPool.returnResource(jedis);
			}
		}
		
		return server;
	}

	@Override
	public void publishNetworkUpdate(String message)
	{
		if (!_redisAvailable) return;
		
		Jedis jedis = null;
		try
		{
			jedis = _writePool.getResource();
			jedis.publish("houzicore:network:updates", message);
		}
		catch (Exception exception)
		{
			if (exception instanceof JedisConnectionException)
				System.out.println("[RedisServerRepository] Warning: " + "Redis Timeout in publishNetworkUpdate: " + exception.getMessage());
			else
				{ System.out.println("[RedisServerRepository] Error: " + exception.getMessage()); exception.printStackTrace(); }
			
			if (jedis != null) {
				_writePool.returnBrokenResource(jedis);
				jedis = null;
			}
		}
		finally
		{
			if (jedis != null)
			{
				_writePool.returnResource(jedis);
			}
		}
	}
	
	/*
	 * <region> = "US" or "EU"
	 * serverstatus.minecraft.<region>.<name> stores the JSON encoded information of an active MinecraftServer instance.
	 * serverstatus.minecraft.<region> stores a sorted set with the set of name's for MinecraftServers 
	 * with a value of their expiry date (in ms)
	 * 
	 * -----------------------
	 * 
	 * serverstatus.dedicated.<name> stores the hash containing information of an active dedicated server instance
	 * serverstatus.dedicated stores the set of active dedicated server names.
	 * serverstatus.dedicated uses a hash with the following keys:
	 * name, publicAddress, privateAddress, region, cpu, ram
	 * 
	 * Example commands for adding/creating a new dedicated server:
	 * 1. HMSET serverstatus.dedicated.<name> name <?> publicAddress <?> privateAddress <?> region <?> cpu <?> ram <?>
	 * 2. SADD serverstatus.dedicated <name>
	 * 
	 * ------------------------
	 * 
	 * servergroups.<name> stores the hash-set containing information for the server group type.
	 * servergroups stores the set of active server group names.
	 * servergroups.<name> stores a hash of the following key name/values
	 * name, prefix, scriptName, ram, cpu, totalServers, joinableServers
	 * 
	 * Example commands for adding/creating a new server group:
	 * 
	 * 1. HMSET servergroups.<name> name <?> prefix <?> scriptName <?> ram <?> cpu <?> totalServers <?> joinableServers <?>
	 * 2. SADD servergroups <name>
	 */
}
