package com.houzicore.shared.core.database;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import javax.sql.DataSource;

import com.houzicore.shared.common.util.NautHashMap;
import com.houzicore.shared.core.database.column.Column;
import com.houzicore.shared.updater.UpdateType;
import com.houzicore.shared.updater.event.UpdateEvent;

import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;

public abstract class RepositoryBase implements Listener {
	// Queue for failed processes
	private static Object _queueLock = new Object();
	private final NautHashMap<DatabaseRunnable, String> _failedQueue = new NautHashMap<>();

	private final DataSource _dataSource; // Connection pool
	protected JavaPlugin Plugin; // Plugin responsible for this repository

	// Only log DB connection failure once to avoid console spam
	private static volatile boolean _dbWarningLogged = false;

	/**
	 * Constructor
	 * 
	 * @param plugin
	 *            - the {@link JavaPlugin} module responsible for this repository.
	 * @param dataSource
	 *            - the {@link DataSource} responsible for providing the connection
	 *            pool to this repository.
	 */
	public RepositoryBase(JavaPlugin plugin, DataSource dataSource) {
		Plugin = plugin;
		_dataSource = dataSource;

		Bukkit.getServer().getScheduler().runTaskAsynchronously(plugin, new Runnable() {
			@Override
			public void run() {
				initialize();
				update();
			}
		});

		plugin.getServer().getPluginManager().registerEvents(this, plugin);
	}

	protected int executeInsert(String query, ResultSetCallable callable, Column<?>... columns) {
		int affectedRows = 0;

		Connection connection = getConnection();
		if (connection == null) return 0;

		// Automatic resource management for handling/closing objects.
		try (Connection conn = connection;
				PreparedStatement preparedStatement = conn.prepareStatement(query,
						Statement.RETURN_GENERATED_KEYS)) {
			for (int i = 0; i < columns.length; i++) {
				columns[i].setValue(preparedStatement, i + 1);
			}

			affectedRows = preparedStatement.executeUpdate();

			if (callable != null) {
				callable.processResultSet(preparedStatement.getGeneratedKeys());
			}
		} catch (final SQLException exception) {
			org.bukkit.Bukkit.getServer().getLogger().log(java.util.logging.Level.SEVERE, exception.getMessage(), exception);
		} catch (final Exception exception) {
			org.bukkit.Bukkit.getServer().getLogger().log(java.util.logging.Level.SEVERE, exception.getMessage(), exception);
		}

		return affectedRows;
	}

	protected void executeQuery(PreparedStatement statement, ResultSetCallable callable, Column<?>... columns) {
		try {
			for (int i = 0; i < columns.length; i++) {
				columns[i].setValue(statement, i + 1);
			}

			try (ResultSet resultSet = statement.executeQuery()) {
				callable.processResultSet(resultSet);
			}
		} catch (final SQLException exception) {
			org.bukkit.Bukkit.getServer().getLogger().log(java.util.logging.Level.SEVERE, exception.getMessage(), exception);
		} catch (final Exception exception) {
			org.bukkit.Bukkit.getServer().getLogger().log(java.util.logging.Level.SEVERE, exception.getMessage(), exception);
		}
	}

	protected void executeQuery(String query, ResultSetCallable callable, Column<?>... columns) {
		Connection connection = getConnection();
		if (connection == null) return;

		// Automatic resource management for handling/closing objects.
		try (Connection conn = connection;
				PreparedStatement preparedStatement = conn.prepareStatement(query)) {
			executeQuery(preparedStatement, callable, columns);
		} catch (final SQLException exception) {
			org.bukkit.Bukkit.getServer().getLogger().log(java.util.logging.Level.SEVERE, exception.getMessage(), exception);
		} catch (final Exception exception) {
			org.bukkit.Bukkit.getServer().getLogger().log(java.util.logging.Level.SEVERE, exception.getMessage(), exception);
		}
	}

	/**
	 * Execute a query against the repository.
	 * 
	 * @param query
	 *            - the concatenated query to execute in string form.
	 * @param columns
	 *            - the column data values used for insertion into the query.
	 * @return the number of rows affected by this query in the repository.
	 */
	protected int executeUpdate(String query, Column<?>... columns) {
		return executeInsert(query, null, columns);
	}

	/**
	 * Requirements: {@link Connection}s must be closed after usage so they may be
	 * returned to the pool!
	 * 
	 * @see Connection#close()
	 * @return a newly fetched {@link Connection} from the connection pool, if a
	 *         connection can be made, null otherwise.
	 */
	protected Connection getConnection() {
		try {
			return _dataSource.getConnection();
		} catch (final SQLException e) {
			if (!_dbWarningLogged) {
				_dbWarningLogged = true;
			}
			return null;
		}
	}

	/**
	 * @return the {@link DataSource} used by the repository for connection pooling.
	 */
	protected DataSource getConnectionPool() {
		return _dataSource;
	}

	protected void handleDatabaseCall(final DatabaseRunnable databaseRunnable, final String errorMessage) {
		com.houzicore.shared.common.util.HouziAsync.runAsync(() -> {
			try {
				databaseRunnable.run();
			} catch (final Exception exception) {
				processFailedDatabaseCall(databaseRunnable, exception.getMessage(), errorMessage);
			}
		});
	}

	protected abstract void initialize();

	@EventHandler
	public void processDatabaseQueue(UpdateEvent event) {
		if (event.getType() != UpdateType.MIN_01)
			return;

		processFailedQueue();
	}

	protected void processFailedDatabaseCall(DatabaseRunnable databaseRunnable, String errorPreMessage,
			String runnableMessage) {
		if (databaseRunnable.getFailedCounts() < 4) {
			databaseRunnable.incrementFailCount();

			synchronized (_queueLock) {
				_failedQueue.put(databaseRunnable, runnableMessage);
			}
		}
	}

	private void processFailedQueue() {
		synchronized (_queueLock) {
			for (final DatabaseRunnable databaseRunnable : _failedQueue.keySet()) {
				handleDatabaseCall(databaseRunnable, _failedQueue.get(databaseRunnable));
			}
		}
	}

	protected abstract void update();
}
