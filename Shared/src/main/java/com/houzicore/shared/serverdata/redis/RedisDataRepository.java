package com.houzicore.shared.serverdata.redis;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.houzicore.shared.serverdata.Region;
import com.houzicore.shared.serverdata.Utility;
import com.houzicore.shared.serverdata.data.Data;
import com.houzicore.shared.serverdata.data.DataRepository;
import com.houzicore.shared.serverdata.servers.ConnectionData;
import com.houzicore.shared.serverdata.servers.ServerManager;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;
import redis.clients.jedis.Pipeline;
import redis.clients.jedis.Response;
import redis.clients.jedis.Transaction;
import redis.clients.jedis.exceptions.JedisConnectionException;

public class RedisDataRepository<T extends Data> implements DataRepository<T>
{
	
	// The delimiter character used for redis key paths
	public final char KEY_DELIMITER = '.';
	
	// The pools used to retrieve jedis instances.
	private JedisPool _writePool;
	private JedisPool _readPool;
	
	// The geographical region of the servers stored by this ServerRepository
	private Region _region;
	
	// The class type of the elements stored in this repository
	private Class<T> _elementType;
	
	// A unique label designating the elements and this repository.
	private String _elementLabel;
	
	// Whether Redis is available for this repository
	private boolean _redisAvailable;
	
	private static boolean _loggedStandalone = false;
	
	/**
	 * Class constructor
	 * @param writeConn
	 * @param readConn
	 * @param host
	 * @param port
	 * @param region
	 */
	public RedisDataRepository(ConnectionData writeConn, ConnectionData readConn, Region region, 
								Class<T> elementType, String elementLabel)
	{
		_region = region;
		_elementType = elementType;
		_elementLabel = elementLabel;
		
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
	
	public RedisDataRepository(ConnectionData conn, Region region, Class<T> elementType, String elementLabel)
	{
		this(conn, conn, region, elementType, elementLabel);
	}
	
	public RedisDataRepository(Region region, Class<T> elementType, String elementLabel)
	{
		this(ServerManager.getMasterConnection(), ServerManager.getSlaveConnection(), region,
				elementType, elementLabel);
	}
	
	public String getElementSetKey()
	{
		return concatenate("data", _elementLabel, _region.toString());
	}
	
	public String generateKey(T element)
	{
		return generateKey(element.getDataId());
	}
	
	public String generateKey(String dataId)
	{
		return concatenate(getElementSetKey(), dataId);
	}
	
	@Override
	public Collection<T> getElements() 
	{
		return getElements(getActiveElements());
	}

	@Override
	public Collection<T> getElements(Collection<String> dataIds)
	{
		Collection<T> elements = new HashSet<T>();
		
		if (!_redisAvailable) return elements;
		
		Jedis jedis = _readPool.getResource();
		
		try
		{
			Pipeline pipeline = jedis.pipelined();

			List<Response<String>> responses = new ArrayList<Response<String>>();
			for (String dataId : dataIds)
			{
				responses.add(pipeline.get(generateKey(dataId)));
			}
			
			// Block until all requests have received pipelined responses
			pipeline.sync();
			
			for (Response<String> response : responses)
			{
				String serializedData = response.get();
				T element = deserialize(serializedData);
				
				if (element != null)
				{
					elements.add(element);
				}
			}
		}
		catch (Exception exception)
		{
			System.out.println("[RedisDataRepository] Error: " + exception.getMessage());
			exception.printStackTrace();
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
		
		return elements;
	}
	
	@Override
	public T getElement(String dataId)
	{
		if (!_redisAvailable) return null;
		
		T element = null;
		Jedis jedis = _readPool.getResource();
		
		try
		{
			String key = generateKey(dataId);
			String serializedData = jedis.get(key);
			element = deserialize(serializedData);
		}
		catch (Exception exception)
		{
			System.out.println("[RedisDataRepository] Error: " + exception.getMessage());
			exception.printStackTrace();
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
		
		return element;
	}

	@Override
	public void addElement(T element, int timeout) 
	{
		if (!_redisAvailable) return;
		
		Jedis jedis = _writePool.getResource();
		
		try
		{
			String serializedData = serialize(element);
			String dataId = element.getDataId();
			String setKey = getElementSetKey();
			String dataKey = generateKey(element);
			long expiry = currentTime() + timeout;
			
			Transaction transaction = jedis.multi();
			transaction.set(dataKey, serializedData);
			transaction.zadd(setKey, expiry, dataId.toString());
			transaction.exec();
		}
		catch (Exception exception)
		{
			System.out.println("[RedisDataRepository] Error: " + exception.getMessage());
			exception.printStackTrace();
			_writePool.returnBrokenResource(jedis);
			jedis = null;
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
	public void addElement(T element)
	{
		addElement(element, 60 * 60 * 24 * 7 * 4 * 12 * 10);	// Set the timeout to 10 years
	}

	@Override
	public void removeElement(T element) 
	{
		removeElement(element.getDataId());
	}
	
	@Override
	public void removeElement(String dataId)
	{
		if (!_redisAvailable) return;
		
		Jedis jedis = _writePool.getResource();
		
		try
		{
			String setKey = getElementSetKey();
			String dataKey = generateKey(dataId);
			
			Transaction transaction = jedis.multi();
			transaction.del(dataKey);
			transaction.zrem(setKey, dataId);
			transaction.exec();
		}
		catch (Exception exception)
		{
			System.out.println("[RedisDataRepository] Error: " + exception.getMessage());
			exception.printStackTrace();
			_writePool.returnBrokenResource(jedis);
			jedis = null;
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
	public boolean elementExists(String dataId)
	{
		return getElement(dataId) != null;
	}

	@Override
	public int clean() 
	{
		if (!_redisAvailable) return 0;
		
		Jedis jedis = _writePool.getResource();
		
		try
		{
			for (String dataId : getDeadElements())
			{
				String dataKey = generateKey(dataId);
				
				Transaction transaction = jedis.multi();
				transaction.del(dataKey);
				transaction.zrem(getElementSetKey(), dataId);
				transaction.exec();
			}
		}
		catch (Exception exception)
		{
			System.out.println("[RedisDataRepository] Error: " + exception.getMessage());
			exception.printStackTrace();
			_writePool.returnBrokenResource(jedis);
			jedis = null;
		}
		finally
		{
			if (jedis != null)
			{
				_writePool.returnResource(jedis);
			}
		}
		
		return 0;
	}

	protected Set<String> getActiveElements()
	{
		Set<String> dataIds = new HashSet<String>();
		
		if (!_redisAvailable) return dataIds;
		
		Jedis jedis = _readPool.getResource();
		
		try
		{
			String min = "(" + currentTime();
			String max = "+inf";
			dataIds.addAll(jedis.zrangeByScore(getElementSetKey(), min, max));
		}
		catch (Exception exception)
		{
			System.out.println("[RedisDataRepository] Error: " + exception.getMessage());
			exception.printStackTrace();
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
		
		return dataIds;
	}
	
	protected Set<String> getDeadElements()
	{
		Set<String> dataIds = new HashSet<String>();
		
		if (!_redisAvailable) return dataIds;
		
		Jedis jedis = _readPool.getResource();
		
		try
		{
			String min = "-inf";
			String max = currentTime() + "";
			dataIds.addAll(jedis.zrangeByScore(getElementSetKey(), min, max));
		}
		catch (Exception exception)
		{
			System.out.println("[RedisDataRepository] Error: " + exception.getMessage());
			exception.printStackTrace();
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
		
		return dataIds;
	}
	
	protected T deserialize(String serializedData)
	{
		return Utility.deserialize(serializedData, _elementType);
	}
	
	protected String serialize(T element)
	{
		return Utility.serialize(element);
	}
	
	protected Long currentTime()
	{
		return Utility.currentTimeSeconds();
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

}
