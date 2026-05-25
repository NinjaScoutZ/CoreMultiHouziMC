package com.houzicore.shared.account.repository;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.bukkit.Bukkit;
import com.google.gson.reflect.TypeToken;
import com.google.gson.Gson;
import org.bukkit.plugin.java.JavaPlugin;

import com.houzicore.shared.account.ILoginProcessor;
import com.houzicore.shared.account.repository.token.LoginToken;
import com.houzicore.shared.account.repository.token.RankUpdateToken;
import com.houzicore.shared.common.Rank;
import com.houzicore.shared.common.util.Callback;
import com.houzicore.shared.common.util.NautHashMap;
import com.houzicore.shared.core.database.DBPool;
import com.houzicore.shared.core.database.DatabaseRunnable;
import com.houzicore.shared.core.database.RepositoryBase;
import com.houzicore.shared.core.database.ResultSetCallable;
import com.houzicore.shared.core.database.column.ColumnBoolean;
import com.houzicore.shared.core.database.column.ColumnTimestamp;
import com.houzicore.shared.core.database.column.ColumnVarChar;


public class AccountRepository extends RepositoryBase {
	private static String ACCOUNT_LOGIN_NEW = "INSERT INTO accounts (uuid, name, lastLogin) values(?, ?, now());";
	private static String UPDATE_ACCOUNT_RANK = "UPDATE accounts SET `rank`=?, rankPerm=false, rankExpire=now() + INTERVAL 1 MONTH WHERE uuid = ?;";
	private static String UPDATE_ACCOUNT_RANK_DONOR = "UPDATE accounts SET `rank`=?, donorRank=?, rankPerm=false, rankExpire=now() + INTERVAL 1 MONTH WHERE uuid = ?;";
	private static String UPDATE_ACCOUNT_RANK_PERM = "UPDATE accounts SET `rank`=?, rankPerm=true WHERE uuid = ?;";
	private static String UPDATE_ACCOUNT_RANK_DONOR_PERM = "UPDATE accounts SET `rank`=?, donorRank=?, rankPerm=true WHERE uuid = ?;";
	private static String UPDATE_ACCOUNT_NULL_RANK = "UPDATE accounts SET `rank`=?, donorRank=?, rankPerm=?, rankExpire=? WHERE uuid = ? AND rank IS NULL;";

	private static String SELECT_ACCOUNT_UUID_BY_NAME = "SELECT uuid FROM accounts WHERE name = ? ORDER BY lastLogin DESC;";

	private static String UPSERT_ACCOUNT = "INSERT INTO accounts (uuid, name, lastLogin) VALUES (?, ?, NOW()) ON DUPLICATE KEY UPDATE name=VALUES(name), lastLogin=NOW();";
	private static String SELECT_ACCOUNT = "SELECT id, `rank`, donorRank, coins, essence FROM accounts WHERE uuid = ?;";

	public AccountRepository(JavaPlugin plugin, String webAddress) {
		super(plugin, DBPool.ACCOUNT);
		// webAddress is no longer used - all auth is local DB
	}

	public String GetClient(String name, UUID uuid, String ipAddress) {
		// Upsert player into local DB
		try (Connection conn = DBPool.ACCOUNT.getConnection()) {
			if (conn == null) return null;
			try (java.sql.PreparedStatement ps = conn.prepareStatement(UPSERT_ACCOUNT)) {
				ps.setString(1, uuid.toString());
				ps.setString(2, name);
				ps.executeUpdate();
			}
			// Read back account data and return as JSON ClientToken
			try (java.sql.PreparedStatement ps = conn.prepareStatement(SELECT_ACCOUNT)) {
				ps.setString(1, uuid.toString());
				ResultSet rs = ps.executeQuery();
				if (rs.next()) {
					String rank = rs.getString("rank");
					if (rank == null || rank.isEmpty()) rank = "ALL";
					String donorRank = rs.getString("donorRank");
					if (donorRank == null || donorRank.isEmpty()) donorRank = "ALL";
					int coins = rs.getInt("coins");
					int gems = rs.getInt("essence");
					int accountId = rs.getInt("id");
					return "{\"AccountId\":" + accountId + ",\"Name\":\"" + name + "\",\"Rank\":\"" + rank + "\",\"DonorToken\":{\"Gems\":" + gems + ",\"Coins\":" + coins + ",\"SalesPackages\":[]},\"RankPerm\":true,\"EconomyBalance\":0}";
				}
			}
		} catch (Exception e) {
		}
		return null;
	}

	public String getClientByName(String playerName) {
		return null; // Not needed for local operation
	}

	public String getClientByUUID(UUID uuid) {
		return GetClient("", uuid, "");
	}

	public UUID getClientUUID(String name) {
		final List<UUID> uuids = new ArrayList<>();

		executeQuery(SELECT_ACCOUNT_UUID_BY_NAME, new ResultSetCallable() {
			@Override
			public void processResultSet(ResultSet resultSet) throws SQLException {
				while (resultSet.next()) {
					uuids.add(UUID.fromString(resultSet.getString(1)));
				}
			}
		}, new ColumnVarChar("name", 100, name));

		if (uuids.size() > 0)
			return uuids.get(uuids.size() - 1);
		else
			return null;
	}

	public int getAccountId(String name) {
		final List<Integer> ids = new ArrayList<>();

		executeQuery("SELECT id FROM accounts WHERE name = ? ORDER BY lastLogin DESC LIMIT 1;", new ResultSetCallable() {
			@Override
			public void processResultSet(ResultSet resultSet) throws SQLException {
				if (resultSet.next()) {
					ids.add(resultSet.getInt(1));
				}
			}
		}, new ColumnVarChar("name", 100, name));

		if (ids.size() > 0)
			return ids.get(0);
		else
			return -1;
	}

	@Override
	protected void initialize() {
		// executeUpdate(CREATE_ACCOUNT_TABLE);
	}

	public int login(NautHashMap<String, ILoginProcessor> loginProcessors, String uuid, String name) {
		int accountId = -1;
		Connection connection = getConnection();
		if (connection == null) return accountId;

		try (Connection conn = connection; Statement statement = conn.createStatement()) {
			statement.execute("SELECT id FROM accounts WHERE accounts.uuid = '" + uuid + "' LIMIT 1;");
			final ResultSet resultSet = statement.getResultSet();

			while (resultSet.next()) {
				accountId = resultSet.getInt(1);
			}

			if (accountId == -1) {
				final List<Integer> tempList = new ArrayList<>(1);

				executeInsert(ACCOUNT_LOGIN_NEW, new ResultSetCallable() {
					@Override
					public void processResultSet(ResultSet resultSet) throws SQLException {
						while (resultSet.next()) {
							tempList.add(resultSet.getInt(1));
						}
					}
				}, new ColumnVarChar("uuid", 100, uuid), new ColumnVarChar("name", 100, name));

				accountId = tempList.get(0);
			}

			/*
			 * boolean statementStatus = statement.execute( "UPDATE accounts SET name='" +
			 * name + "', lastLogin=now() WHERE accounts.uuid = '" + uuid + "';" +
			 * "SELECT games, visibility, showChat, friendChat, privateMessaging, partyRequests, invisibility, forcefield, showMacReports, ignoreVelocity, pendingFriendRequests FROM accountPreferences WHERE accountPreferences.uuid = '"
			 * + uuid + "' LIMIT 1;" +
			 * "SELECT items.name, ic.name as category, count FROM accountInventory AS ai INNER JOIN items ON items.id = ai.itemId INNER JOIN itemCategories AS ic ON ic.id = items.categoryId INNER JOIN accounts ON accounts.id = ai.accountId WHERE accounts.uuid = '"
			 * + uuid + "';" +
			 * "SELECT benefit FROM rankBenefits WHERE rankBenefits.uuid = '" + uuid + "';"
			 * +
			 * "SELECT stats.name, value FROM accountStats INNER JOIN stats ON stats.id = accountStats.statId INNER JOIN accounts ON accountStats.accountId = accounts.id WHERE accounts.uuid = '"
			 * + uuid + "';" +
			 * "SELECT tA.Name, status, serverName, tA.lastLogin, now() FROM accountFriend INNER Join accounts AS fA ON fA.uuid = uuidSource INNER JOIN accounts AS tA ON tA.uuid = uuidTarget LEFT JOIN playerMap ON tA.name = playerName WHERE uuidSource = '"
			 * + uuid + "';" + "SELECT gameType, elo FROM eloRating WHERE uuid = '" + uuid +
			 * "';" );
			 */

			statement.executeUpdate("UPDATE accounts SET name='" + name + "', lastLogin=now() WHERE id = '" + accountId + "';");

			for (final ILoginProcessor loginProcessor : loginProcessors.values()) {
				String query = loginProcessor.getQuery(accountId, uuid, name);
				if (query != null && !query.isEmpty()) {
					try {
						boolean isResultSet = statement.execute(query);
						if (isResultSet) {
							try (ResultSet rs = statement.getResultSet()) {
								loginProcessor.processLoginResultSet(name, accountId, rs);
							}
						} else {
							loginProcessor.processLoginResultSet(name, accountId, null);
						}
					} catch (Exception ex) {
						org.bukkit.Bukkit.getServer().getLogger().log(java.util.logging.Level.SEVERE, ex.getMessage(), ex);
					}
				}
			}
		} catch (final Exception exception) {
			org.bukkit.Bukkit.getServer().getLogger().log(java.util.logging.Level.SEVERE, exception.getMessage(), exception);
		}

		return accountId;
	}

	public void matchPlayerName(final Callback<List<String>> callback, final String userName) {
		// Local DB implementation - search accounts table
		com.houzicore.shared.common.util.HouziAsync.runAsync(() -> {
			final List<String> matches = new ArrayList<>();
			executeQuery("SELECT name FROM accounts WHERE name LIKE ? LIMIT 10;", new ResultSetCallable() {
				@Override
				public void processResultSet(ResultSet resultSet) throws SQLException {
					while (resultSet.next()) {
						matches.add(resultSet.getString(1));
					}
				}
			}, new ColumnVarChar("name", 100, userName + "%"));
			callback.run(matches);
		});
	}

	public void saveRank(final Callback<Rank> callback, final String name, final UUID uuid, final Rank rank,
			final boolean perm) {
		final RankUpdateToken token = new RankUpdateToken();
		token.Name = name;
		token.Rank = rank.toString();
		token.Perm = perm;

		final Callback<Rank> extraCallback = new Callback<Rank>() {
			@Override
			public void run(final Rank response) {
				if (rank == Rank.WARRIOR || rank == Rank.SOVEREIGN || rank == Rank.DIVINE) {
					if (perm) {
						executeUpdate(UPDATE_ACCOUNT_RANK_DONOR_PERM, new ColumnVarChar("rank", 100, rank.toString()),
								new ColumnVarChar("donorRank", 100, rank.toString()),
								new ColumnVarChar("uuid", 100, uuid.toString()));
					} else {
						executeUpdate(UPDATE_ACCOUNT_RANK_DONOR, new ColumnVarChar("rank", 100, rank.toString()),
								new ColumnVarChar("donorRank", 100, rank.toString()),
								new ColumnVarChar("uuid", 100, uuid.toString()));
					}
				} else {
					if (perm) {
						executeUpdate(UPDATE_ACCOUNT_RANK_PERM, new ColumnVarChar("rank", 100, rank.toString()),
								new ColumnVarChar("uuid", 100, uuid.toString()));
					} else {
						executeUpdate(UPDATE_ACCOUNT_RANK, new ColumnVarChar("rank", 100, rank.toString()),
								new ColumnVarChar("uuid", 100, uuid.toString()));
					}
				}

				Bukkit.getServer().getScheduler().runTask(Plugin, new Runnable() {
					@Override
					public void run() {
						callback.run(response);
					}
				});
			}
		};

		handleDatabaseCall(new DatabaseRunnable(new Runnable() {
			@Override
			public void run() {
				// Local DB rank update - no remote API needed
				extraCallback.run(rank);
			}
		}), "Error saving player  " + token.Name + "'s rank in AccountRepository : ");

	}

	@Override
	protected void update() {
	}

	public void updateMysqlRank(final String uuid, final String rank, final boolean perm, final String rankExpire) {
		handleDatabaseCall(new DatabaseRunnable(new Runnable() {
			@Override
			public void run() {
				executeUpdate(UPDATE_ACCOUNT_NULL_RANK, new ColumnVarChar("rank", 100, rank),
						new ColumnVarChar("donorRank", 100, rank), new ColumnBoolean("rankPerm", perm),
						new ColumnTimestamp("rankExpire", Timestamp.valueOf(rankExpire)),
						new ColumnVarChar("uuid", 100, uuid));
			}
		}), "Error updating player's mysql rank AccountRepository : ");
	}
}
