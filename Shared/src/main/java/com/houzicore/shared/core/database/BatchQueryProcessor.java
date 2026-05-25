package com.houzicore.shared.core.database;

import java.sql.Connection;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;

import org.bukkit.event.EventHandler;
import org.bukkit.plugin.java.JavaPlugin;

import com.houzicore.shared.MiniPlugin;
import com.houzicore.shared.updater.UpdateType;
import com.houzicore.shared.updater.event.UpdateEvent;

public class BatchQueryProcessor extends MiniPlugin {

	private static BatchQueryProcessor _instance;
	private final ConcurrentLinkedQueue<String> _queryQueue;

	public BatchQueryProcessor(JavaPlugin plugin) {
		super("Batch Query Processor", plugin);
		_instance = this;
		_queryQueue = new ConcurrentLinkedQueue<>();
	}

	public static BatchQueryProcessor getInstance() {
		return _instance;
	}

	public void addQueryToQueue(String query) {
		_queryQueue.add(query);
	}

	@EventHandler
	public void processQueue(UpdateEvent event) {
		// Flush the queue every 4 seconds to minimize DB connection acquisition logic.
		if (event.getType() != UpdateType.SLOW)
			return;
			
		if (_queryQueue.isEmpty())
			return;

		List<String> bulkQueries = new ArrayList<>();
		String q;
		// Drain queue up to 500 queries per batch, controlling transaction size
		int count = 0;
		while ((q = _queryQueue.poll()) != null && count < 500) {
			bulkQueries.add(q);
			count++;
		}

		if (bulkQueries.isEmpty())
			return;

		runAsync(() -> {
			try (Connection c = DBPool.ACCOUNT.getConnection(); 
			     Statement s = c.createStatement()) {
				for (String query : bulkQueries) {
					s.addBatch(query);
				}
				s.executeBatch();
			} catch (Exception e) {
				getPlugin().getLogger().severe("==================================");
				getPlugin().getLogger().severe("[BatchQueryProcessor] Failed to execute batch query.");
				e.printStackTrace();
				getPlugin().getLogger().severe("==================================");
			}
		});
	}
}
