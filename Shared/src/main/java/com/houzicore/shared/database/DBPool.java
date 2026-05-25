package com.houzicore.shared.core.database;

import javax.sql.DataSource;
import java.sql.Connection;

import org.apache.commons.dbcp2.BasicDataSource;

public final class DBPool {
	private static final java.util.Properties _props = new java.util.Properties();
	static {
		java.io.File file = new java.io.File("houzicore-database.properties");
		if (!file.exists()) {
			file = new java.io.File("../houzicore-database.properties");
		}
		if (file.exists()) {
			try (java.io.FileInputStream in = new java.io.FileInputStream(file)) {
				_props.load(in);
			} catch (Exception e) {
				org.bukkit.Bukkit.getServer().getLogger().log(java.util.logging.Level.SEVERE, e.getMessage(), e);
			}
		}
	}

	private static String get(String key, String def) {
		String envKey = "HOUZI_" + key.toUpperCase().replace('.', '_');
		String envVal = System.getenv(envKey);
		if (envVal != null && !envVal.isEmpty()) {
			return envVal;
		}
		return _props.getProperty(key, def);
	}

	public static final DataSource ACCOUNT = openDataSource(
			get("account.url", "jdbc:mysql://127.0.0.1:3306/account"),
			get("account.user", "root"),
			get("account.pass", ""));

	public static final DataSource QUEUE = openDataSource(
			get("queue.url", "jdbc:mysql://127.0.0.1:3306/queue"),
			get("queue.user", "root"),
			get("queue.pass", ""));

	public static final DataSource HOUZI = openDataSource(
			get("houzi.url", "jdbc:mysql://127.0.0.1:3306/houzi"),
			get("houzi.user", "root"),
			get("houzi.pass", ""));

	public static final DataSource STATS_HOUZI = openDataSource(
			get("stats.url", "jdbc:mysql://127.0.0.1:3306/houzi"),
			get("stats.user", "root"),
			get("stats.pass", ""));

	private static DataSource openDataSource(String url, String username, String password) {
		final BasicDataSource source = new BasicDataSource();
		source.addConnectionProperty("autoReconnect", "true");
		source.addConnectionProperty("allowMultiQueries", "true");
		source.setDefaultTransactionIsolation(Connection.TRANSACTION_READ_COMMITTED);
		source.setDriverClassName("com.mysql.cj.jdbc.Driver");
		source.setUrl(url);
		source.setUsername(username);
		source.setPassword(password);
		source.setMaxTotal(4);
		source.setMaxIdle(4);
		source.setTimeBetweenEvictionRunsMillis(180 * 1000);
		source.setSoftMinEvictableIdleTimeMillis(180 * 1000);

		return source;
	}

	private DBPool() {

	}
}
