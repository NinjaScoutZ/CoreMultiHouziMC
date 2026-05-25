package com.houzicore.shared.core.stats;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.bukkit.plugin.java.JavaPlugin;

import com.houzicore.shared.common.util.NautHashMap;
import com.houzicore.shared.core.database.DBPool;
import com.houzicore.shared.core.database.RepositoryBase;
import com.houzicore.shared.core.database.ResultSetCallable;
import com.houzicore.shared.core.database.column.ColumnVarChar;
import com.houzicore.shared.database.Tables;

import org.jooq.DSLContext;
import org.jooq.Insert;
import org.jooq.Record2;
import org.jooq.Result;
import org.jooq.SQLDialect;
import org.jooq.Update;
import org.jooq.impl.DSL;

public class StatsRepository extends RepositoryBase {
	private static String RETRIEVE_STATS = "SELECT id, name FROM stats;";
	private static String INSERT_STAT = "INSERT INTO stats (name) VALUES (?);";

	public StatsRepository(JavaPlugin plugin) {
		super(plugin, DBPool.ACCOUNT);
	}

	public void addStat(String name) {
		executeUpdate(INSERT_STAT, new ColumnVarChar("name", 100, name));
	}

	@Override
	protected void initialize() {
		// executeUpdate(CREATE_STAT_TABLE);
		// executeUpdate(CREATE_STAT_RELATION_TABLE);
	}

	public PlayerStats loadClientInformation(ResultSet resultSet) throws SQLException {
		final PlayerStats playerStats = new PlayerStats();

		while (resultSet.next()) {
			playerStats.addStat(resultSet.getString(1), resultSet.getInt(2));
		}

		return playerStats;
	}

	public PlayerStats loadOfflinePlayerStats(String playerName) {
		PlayerStats playerStats = null;

		DSLContext context;

		synchronized (this) {
			context = DSL.using(getConnectionPool(), SQLDialect.MYSQL);
		}

		final Result<Record2<String, Long>> result = context
				.select(Tables.stats.name, Tables.accountStat.value).from(Tables.accountStat).join(Tables.stats)
				.on(Tables.stats.id.eq(Tables.accountStat.statId)).where(Tables.accountStat.accountId.eq(DSL
						.select(Tables.accounts.id).from(Tables.accounts).where(Tables.accounts.name.eq(playerName))))
				.fetch();

		if (result.isNotEmpty()) {
			playerStats = new PlayerStats();
			for (final Record2<String, Long> record : result) {
				playerStats.addStat(record.value1(), record.value2());
			}
		}

		return playerStats;
	}

	public java.util.LinkedHashMap<String, Long> getTopStats(String statName, int limit) {
		java.util.LinkedHashMap<String, Long> top = new java.util.LinkedHashMap<>();
		DSLContext context;

		synchronized (this) {
			context = DSL.using(getConnectionPool(), SQLDialect.MYSQL);
		}

		final Result<Record2<String, Long>> result = context
				.select(Tables.accounts.name, Tables.accountStat.value)
				.from(Tables.accountStat)
				.join(Tables.stats).on(Tables.stats.id.eq(Tables.accountStat.statId))
				.join(Tables.accounts).on(Tables.accounts.id.eq(Tables.accountStat.accountId))
				.where(Tables.stats.name.eq(statName))
				.orderBy(Tables.accountStat.value.desc())
				.limit(limit)
				.fetch();

		if (result.isNotEmpty()) {
			for (final Record2<String, Long> record : result) {
				top.put(record.value1(), record.value2());
			}
		}

		return top;
	}

	public java.util.LinkedHashMap<String, Long> getLowestStats(String statName, int limit) {
		java.util.LinkedHashMap<String, Long> top = new java.util.LinkedHashMap<>();
		DSLContext context;

		synchronized (this) {
			context = DSL.using(getConnectionPool(), SQLDialect.MYSQL);
		}

		final Result<Record2<String, Long>> result = context
				.select(Tables.accounts.name, Tables.accountStat.value)
				.from(Tables.accountStat)
				.join(Tables.stats).on(Tables.stats.id.eq(Tables.accountStat.statId))
				.join(Tables.accounts).on(Tables.accounts.id.eq(Tables.accountStat.accountId))
				.where(Tables.stats.name.eq(statName))
				.orderBy(Tables.accountStat.value.asc())
				.limit(limit)
				.fetch();

		if (result.isNotEmpty()) {
			for (final Record2<String, Long> record : result) {
				top.put(record.value1(), record.value2());
			}
		}

		return top;
	}

	public List<Stat> retrieveStats() {
		final List<Stat> stats = new ArrayList<>();

		executeQuery(RETRIEVE_STATS, new ResultSetCallable() {
			@Override
			public void processResultSet(ResultSet resultSet) throws SQLException {
				while (resultSet.next()) {
					final Stat stat = new Stat();

					stat.Id = resultSet.getInt(1);
					stat.Name = resultSet.getString(2);

					stats.add(stat);
				}
			}
		});

		return stats;
	}

	public void saveLowestStat(int accountId, int statId, long value) {
		org.bukkit.Bukkit.getServer().getScheduler().runTaskAsynchronously(Plugin, () -> {
			try {
				DSLContext context = DSL.using(getConnectionPool(), SQLDialect.MYSQL);
				String sql = "INSERT INTO accountStat (accountId, statId, value) VALUES (?, ?, ?) " +
							 "ON DUPLICATE KEY UPDATE value = IF(value > VALUES(value), VALUES(value), value)";
				context.execute(sql, accountId, statId, value);
			} catch (Exception e) {
				org.bukkit.Bukkit.getServer().getLogger().log(java.util.logging.Level.SEVERE, e.getMessage(), e);
			}
		});
	}

	@SuppressWarnings("rawtypes")
	public void saveStats(NautHashMap<Integer, NautHashMap<Integer, Long>> uploadQueue) {
		try {
			final DSLContext context = DSL.using(getConnectionPool(), SQLDialect.MYSQL);

			final List<Update> updates = new ArrayList<>();
			final List<Insert> inserts = new ArrayList<>();

			for (final int accountId : uploadQueue.keySet()) {
				for (final Integer statId : uploadQueue.get(accountId).keySet()) {
					final Update update = context.update(Tables.accountStat)
							.set(Tables.accountStat.value,
									Tables.accountStat.value.plus(uploadQueue.get(accountId).get(statId)))
							.where(Tables.accountStat.accountId.eq(accountId))
							.and(Tables.accountStat.statId.eq(statId));

					updates.add(update);

					final Insert insert = context.insertInto(Tables.accountStat)
							.set(Tables.accountStat.accountId, accountId).set(Tables.accountStat.statId, statId)
							.set(Tables.accountStat.value, uploadQueue.get(accountId).get(statId));

					inserts.add(insert);
				}
			}

			final int[] updateResult = context.batch(updates).execute();

			for (int i = 0; i < updateResult.length; i++) {
				if (updateResult[i] > 0) {
					inserts.set(i, null);
				}
			}

			inserts.removeAll(Collections.singleton(null));

			context.batch(inserts).execute();
		} catch (final Exception e) {
			org.bukkit.Bukkit.getServer().getLogger().log(java.util.logging.Level.SEVERE, e.getMessage(), e);
		}
	}

	@Override
	protected void update() {
	}
}
