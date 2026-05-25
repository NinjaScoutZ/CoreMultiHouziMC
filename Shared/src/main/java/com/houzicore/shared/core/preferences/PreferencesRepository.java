package com.houzicore.shared.core.preferences;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map.Entry;

import org.bukkit.plugin.java.JavaPlugin;

import com.houzicore.shared.common.util.NautHashMap;
import com.houzicore.shared.core.database.DBPool;
import com.houzicore.shared.core.database.RepositoryBase;
import com.houzicore.shared.core.database.column.ColumnVarChar;

public class PreferencesRepository extends RepositoryBase {
	// private static String CREATE_ACCOUNT_TABLE = "CREATE TABLE IF NOT EXISTS
	// accountPreferences (id INT NOT NULL AUTO_INCREMENT, uuid VARCHAR(256), games
	// BOOL NOT NULL DEFAULT 1, visibility BOOL NOT NULL DEFAULT 1, showChat BOOL
	// NOT NULL DEFAULT 1, friendChat BOOL NOT NULL DEFAULT 1, privateMessaging BOOL
	// NOT NULL DEFAULT 1, partyRequests BOOL NOT NULL DEFAULT 0, invisibility BOOL
	// NOT NULL DEFAULT 0, forcefield BOOL NOT NULL DEFAULT 0, showMacReports BOOL
	// NOT NULL DEFAULT 0, ignoreVelocity BOOL NOT NULL DEFAULT 0, PRIMARY KEY (id),
	// UNIQUE INDEX uuid_index (uuid));";
	private static String INSERT_ACCOUNT = "INSERT INTO accountPreferences (uuid) VALUES (?) ON DUPLICATE KEY UPDATE uuid=uuid;";
	private volatile Boolean _hasPlayRadioColumn;
	private volatile boolean _loggedPlayRadioCompatibilityWarning;
	private volatile Boolean _hasActiveTitleColumn;

	public PreferencesRepository(JavaPlugin plugin) {
		super(plugin, DBPool.ACCOUNT);
	}

	@Override
	protected void initialize() {
		// executeUpdate(CREATE_ACCOUNT_TABLE);
	}

	private String buildSelectQuery(String uuid) {
		StringBuilder sb = new StringBuilder("SELECT games, visibility, showChat, friendChat, privateMessaging, partyRequests, invisibility, forcefield, showMacReports, ignoreVelocity, pendingFriendRequests, friendDisplayInventoryUI, language");
		if (hasPlayRadioColumn()) {
			sb.append(", playRadio");
		}
		if (hasActiveTitleColumn()) {
			sb.append(", activeTitle");
		}
		sb.append(" FROM accountPreferences WHERE uuid = '").append(uuid).append("' LIMIT 1;");
		return sb.toString();
	}

	public String buildLoadQuery(String uuid) {
		return buildSelectQuery(uuid);
	}

	public UserPreferences loadClientInformation(final ResultSet resultSet) throws SQLException {
		final UserPreferences preferences = new UserPreferences();

		if (resultSet.next()) {
			preferences.HubGames = resultSet.getBoolean(1);
			preferences.ShowPlayers = resultSet.getBoolean(2);
			preferences.ShowChat = resultSet.getBoolean(3);
			preferences.FriendChat = resultSet.getBoolean(4);
			preferences.PrivateMessaging = resultSet.getBoolean(5);
			preferences.PartyRequests = resultSet.getBoolean(6);
			preferences.Invisibility = resultSet.getBoolean(7);
			preferences.HubForcefield = resultSet.getBoolean(8);
			preferences.ShowMacReports = resultSet.getBoolean(9);
			preferences.IgnoreVelocity = resultSet.getBoolean(10);
			preferences.PendingFriendRequests = resultSet.getBoolean(11);
			preferences.friendDisplayInventoryUI = resultSet.getBoolean(12);
			preferences.Language = resultSet.getString(13);
			
			int index = 14;
			if (hasPlayRadioColumn()) {
				preferences.PlayRadio = resultSet.getBoolean(index++);
			}
			if (hasActiveTitleColumn()) {
				preferences.ActiveTitle = resultSet.getString(index++);
			}
		}

		return preferences;
	}

	public void saveUserPreferences(NautHashMap<String, UserPreferences> preferences) {
		boolean includePlayRadio = hasPlayRadioColumn();
		boolean includeActiveTitle = hasActiveTitleColumn();
		
		StringBuilder queryBuilder = new StringBuilder("UPDATE accountPreferences SET games = ?, visibility = ?, showChat = ?, friendChat = ?, privateMessaging = ?, partyRequests = ?, invisibility = ?, forcefield = ?, showMacReports = ?, ignoreVelocity = ?, pendingFriendRequests = ?, friendDisplayInventoryUI = ?, language = ?");
		if (includePlayRadio) {
			queryBuilder.append(", playRadio = ?");
		}
		if (includeActiveTitle) {
			queryBuilder.append(", activeTitle = ?");
		}
		queryBuilder.append(" WHERE uuid=?;");
		
		String updateQuery = queryBuilder.toString();
		Connection connection = getConnection();
		if (connection == null) return;

		try (Connection conn = connection;
				PreparedStatement preparedStatement = conn.prepareStatement(updateQuery)) {
			for (final Entry<String, UserPreferences> entry : preferences.entrySet()) {
				bindUpdate(preparedStatement, entry, includePlayRadio, includeActiveTitle);
				preparedStatement.addBatch();
			}

			final int[] rowsAffected = preparedStatement.executeBatch();
			int i = 0;

			for (final Entry<String, UserPreferences> entry : preferences.entrySet()) {
				if (rowsAffected[i] < 1) {
					executeUpdate(INSERT_ACCOUNT, new ColumnVarChar("uuid", 100, entry.getKey()));

					bindUpdate(preparedStatement, entry, includePlayRadio, includeActiveTitle);
					preparedStatement.execute();
				}

				i++;
			}
		} catch (final Exception exception) {
			org.bukkit.Bukkit.getServer().getLogger().log(java.util.logging.Level.SEVERE, exception.getMessage(), exception);
		}
	}

	@Override
	protected void update() {
	}

	private void bindUpdate(PreparedStatement preparedStatement, Entry<String, UserPreferences> entry,
			boolean includePlayRadio, boolean includeActiveTitle) throws SQLException {
		preparedStatement.setBoolean(1, entry.getValue().HubGames);
		preparedStatement.setBoolean(2, entry.getValue().ShowPlayers);
		preparedStatement.setBoolean(3, entry.getValue().ShowChat);
		preparedStatement.setBoolean(4, entry.getValue().FriendChat);
		preparedStatement.setBoolean(5, entry.getValue().PrivateMessaging);
		preparedStatement.setBoolean(6, entry.getValue().PartyRequests);
		preparedStatement.setBoolean(7, entry.getValue().Invisibility);
		preparedStatement.setBoolean(8, entry.getValue().HubForcefield);
		preparedStatement.setBoolean(9, entry.getValue().ShowMacReports);
		preparedStatement.setBoolean(10, entry.getValue().IgnoreVelocity);
		preparedStatement.setBoolean(11, entry.getValue().PendingFriendRequests);
		preparedStatement.setBoolean(12, entry.getValue().friendDisplayInventoryUI);
		preparedStatement.setString(13, entry.getValue().Language);

		int index = 14;
		if (includePlayRadio) {
			preparedStatement.setBoolean(index++, entry.getValue().PlayRadio);
		}
		if (includeActiveTitle) {
			preparedStatement.setString(index++, entry.getValue().ActiveTitle);
		}

		preparedStatement.setString(index, entry.getKey());
	}

	private boolean hasPlayRadioColumn() {
		Boolean cached = _hasPlayRadioColumn;
		if (cached != null) {
			return cached.booleanValue();
		}

		synchronized (this) {
			if (_hasPlayRadioColumn != null) {
				return _hasPlayRadioColumn.booleanValue();
			}

			boolean exists = false;
			Connection connection = getConnection();
			if (connection != null) {
				try (Connection conn = connection) {
					DatabaseMetaData metaData = conn.getMetaData();
					exists = columnExists(metaData, conn.getCatalog(), "accountPreferences", "playRadio");
					_hasPlayRadioColumn = Boolean.valueOf(exists);
				} catch (SQLException ignored) {
					return false;
				}
			} else {
				return false;
			}

			if (!exists && !_loggedPlayRadioCompatibilityWarning) {
				_loggedPlayRadioCompatibilityWarning = true;
				Plugin.getLogger().warning("accountPreferences.playRadio is missing; using compatibility mode without radio persistence.");
			}
			return exists;
		}
	}

	private boolean hasActiveTitleColumn() {
		Boolean cached = _hasActiveTitleColumn;
		if (cached != null) {
			return cached.booleanValue();
		}

		synchronized (this) {
			if (_hasActiveTitleColumn != null) {
				return _hasActiveTitleColumn.booleanValue();
			}

			boolean exists = false;
			Connection connection = getConnection();
			if (connection != null) {
				try (Connection conn = connection) {
					DatabaseMetaData metaData = conn.getMetaData();
					exists = columnExists(metaData, conn.getCatalog(), "accountPreferences", "activeTitle");
					_hasActiveTitleColumn = Boolean.valueOf(exists);
				} catch (SQLException ignored) {
					return false;
				}
			} else {
				return false;
			}
			return exists;
		}
	}

	private boolean columnExists(DatabaseMetaData metaData, String catalog, String tableName, String columnName)
			throws SQLException {
		try (ResultSet columns = metaData.getColumns(catalog, null, tableName, columnName)) {
			if (columns.next()) {
				return true;
			}
		}

		try (ResultSet columns = metaData.getColumns(catalog, null, tableName.toLowerCase(), columnName)) {
			if (columns.next()) {
				return true;
			}
		}

		try (ResultSet columns = metaData.getColumns(catalog, null, tableName.toUpperCase(), columnName.toUpperCase())) {
			return columns.next();
		}
	}
}
