package com.houzicore.shared.serverdata;

import java.util.concurrent.ConcurrentHashMap;

import com.houzicore.shared.serverdata.servers.ConnectionData;
import com.houzicore.shared.serverdata.servers.ServerManager;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;
import redis.clients.jedis.exceptions.JedisConnectionException;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

/**
 * Utility offers various necessary utility-based methods for use in com.houzicore.shared.serverdata.
 * @author Ty
 *
 */
public class Utility
{

	// The Gson instance used to serialize/deserialize objects in JSON form.
	private static Gson _gson = new GsonBuilder().create();
	public static Gson getGson() { return _gson; }

    // map of all instantiated connection pools, distinguished by their ip:port combination
    private static final ConcurrentHashMap<String, JedisPool> _pools = new ConcurrentHashMap<String, JedisPool>();

	// Public static jedis pool for interacting with central default jedis repo.
	private static JedisPool _masterPool;
	private static JedisPool _slavePool;

	/**
	 * @param object - the (non-null) object to serialize
	 * @return the serialized form of {@code object}.
	 */
	public static String serialize(Object object)
	{
		return _gson.toJson(object);
	}

	/**
	 * @param serializedData - the serialized data to be deserialized
	 * @param type - the resulting class type of the object to be deserialized
	 * @return the deserialized form of {@code serializedData} for class {@code type}.
	 */
	public static <T> T deserialize(String serializedData, Class<T> type)
	{
		return _gson.fromJson(serializedData, type);
	}

	/**
	 * @param delimiter - the delimiter character used to separate the concatenated elements
	 * @param elements - the set of string elements to be concatenated and returned.
	 * @return the concatenated string of all {@code elements} separated by the {@code delimiter}.
	 */
	public static String concatenate(char delimiter, String... elements)
	{
		int length = elements.length;
		String result = length > 0 ? elements[0] : new String();

		for (int i = 1; i < length; i++)
		{
			result += delimiter + elements[i];
		}

		return result;
	}

	/**
	 * @return the current timestamp (in seconds) fetched from the central jedis repository
	 * for synced timestamps. Falls back to System.currentTimeMillis() if Redis is unavailable.
	 */
	public static long currentTimeSeconds()
	{
		JedisPool pool = getPool(false);
		
		if (pool == null)
		{
			return System.currentTimeMillis() / 1000;
		}
		
		long currentTime = 0;
		Jedis jedis = null;

		try
		{
			jedis = pool.getResource();
			currentTime = Long.parseLong(jedis.time().get(0));
		}
		catch (Exception exception)
		{
			if (jedis != null)
			{
				pool.returnBrokenResource(jedis);
				jedis = null;
			}
			// Fallback to local time
			currentTime = System.currentTimeMillis() / 1000;
		}
		finally
		{
			if (jedis != null)
			{
				pool.returnResource(jedis);
			}
		}

		return currentTime;
	}

	/**
	 * @return the current timestamp (in milliseconds) fetched from the central jedis repository
	 * for synced timestamps. Falls back to System.currentTimeMillis() if Redis is unavailable.
	 */
	public static long currentTimeMillis()
	{
		JedisPool pool = getPool(false);
		
		if (pool == null)
		{
			return System.currentTimeMillis();
		}
		
		long currentTime = 0;
		Jedis jedis = null;

		try
		{
			jedis = pool.getResource();
			currentTime = Long.parseLong(jedis.time().get(0));
		}
		catch (Exception exception)
		{
			if (jedis != null)
			{
				pool.returnBrokenResource(jedis);
				jedis = null;
			}
			// Fallback to local time
			return System.currentTimeMillis();
		}
		finally
		{
			if (jedis != null)
			{
				pool.returnResource(jedis);
			}
		}

		return currentTime * 1000;
	}

	/**
	 * @param connData - the connection data specifying the database to be connected to.
	 * @return a newly instantiated {@link JedisPool} connected to the provided {@link ConnectionData} repository.
	 *         Returns null if connData is null (Redis disabled).
	 */
	public static JedisPool generatePool(ConnectionData connData)
	{
		if (connData == null)
		{
			return null;
		}
		
	    String key = getConnKey(connData);
	    JedisPool pool = _pools.get(key);
	    if (pool == null)
	    {
	        JedisPoolConfig jedisPoolConfig = new JedisPoolConfig();
	        jedisPoolConfig.setMaxWaitMillis(1000);
	        jedisPoolConfig.setMinIdle(5);
	        jedisPoolConfig.setTestOnBorrow(true);

	        jedisPoolConfig.setMaxTotal(20);
	        jedisPoolConfig.setBlockWhenExhausted(true);

	        pool = new JedisPool(jedisPoolConfig, connData.getHost(), connData.getPort());
	        _pools.put(key, pool);
	    }

	    return pool;
	}

	/**
	 * @param writeable - whether or not the Jedis connections returned should be writeable to.
	 * @return a globally available {@link JedisPool}, or null if Redis is disabled.
	 */
	public static JedisPool getPool(boolean writeable)
	{
		if (!ServerManager.isRedisEnabled())
		{
			return null;
		}
		
		if (writeable)
		{
			if (_masterPool == null)
			{
				ConnectionData masterConn = ServerManager.getMasterConnection();
				if (masterConn == null) return null;
				_masterPool = generatePool(masterConn);
			}

			return _masterPool;
		}
		else
		{
			if (_slavePool == null)
			{
				ConnectionData slave = ServerManager.getSlaveConnection();
				if (slave == null) return null;
				_slavePool = generatePool(slave);
			}

			return _slavePool;
		}
	}

    private static String getConnKey(ConnectionData connData)
    {
        return connData.getHost() + ":" + connData.getPort();
    }

}
