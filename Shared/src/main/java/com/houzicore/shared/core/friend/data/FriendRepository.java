package com.houzicore.shared.core.friend.data;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import com.houzicore.shared.common.util.NautHashMap;
import com.houzicore.shared.core.database.DBPool;
import com.houzicore.shared.core.database.RepositoryBase;
import com.houzicore.shared.core.database.ResultSetCallable;
import com.houzicore.shared.core.database.column.ColumnVarChar;
import com.houzicore.shared.core.friend.FriendStatusType;
import com.houzicore.shared.serverdata.Region;
import com.houzicore.shared.serverdata.data.DataRepository;
import com.houzicore.shared.serverdata.data.PlayerStatus;
import com.houzicore.shared.serverdata.redis.RedisDataRepository;
import com.houzicore.shared.serverdata.servers.ServerManager;

public class FriendRepository extends RepositoryBase {
	private static String RETRIEVE_MULTIPLE_FRIEND_RECORDS = "SELECT uuidSource, tA.Name, status, tA.lastLogin, now(), aF.favorite, tA.uuid AS uuidTarget FROM accountFriend AS aF INNER Join accounts AS fA ON fA.uuid = aF.uuidSource INNER JOIN accounts AS tA ON tA.uuid = aF.uuidTarget WHERE aF.uuidSource IN ";
	private static String ADD_FRIEND_RECORD = "INSERT INTO accountFriend (uuidSource, uuidTarget, status, favorite, created) SELECT fA.uuid AS uuidSource, tA.uuid AS uuidTarget, ?, 0, now() FROM accounts as fA LEFT JOIN accounts AS tA ON tA.name = ? WHERE fA.name = ?;";
	private static String UPDATE_MUTUAL_RECORD = "UPDATE accountFriend AS aF INNER JOIN accounts as fA ON aF.uuidSource = fA.uuid INNER JOIN accounts AS tA ON aF.uuidTarget = tA.uuid SET aF.status = ? WHERE tA.name = ? AND fA.name = ?;";
	private static String UPDATE_FAVORITE_RECORD = "UPDATE accountFriend AS aF INNER JOIN accounts as fA ON aF.uuidSource = fA.uuid INNER JOIN accounts AS tA ON aF.uuidTarget = tA.uuid SET aF.favorite = ? WHERE tA.name = ? AND fA.name = ?;";
	private static String DELETE_FRIEND_RECORD = "DELETE aF FROM accountFriend AS aF INNER JOIN accounts as fA ON aF.uuidSource = fA.uuid INNER JOIN accounts AS tA ON aF.uuidTarget = tA.uuid WHERE fA.name = ? AND tA.name = ?;";

	// Repository holding active PlayerStatus data.
	private final DataRepository<PlayerStatus> _repository;

	public FriendRepository(JavaPlugin plugin) {
		super(plugin, DBPool.ACCOUNT);

		_repository = new RedisDataRepository<>(ServerManager.getMasterConnection(),
				ServerManager.getSlaveConnection(), Region.currentRegion(), PlayerStatus.class, "playerStatus");
	}

	public boolean addFriend(final Player caller, String name) {
		final int rowsAffected = executeUpdate(ADD_FRIEND_RECORD, new ColumnVarChar("status", 100, "Sent"),
				new ColumnVarChar("name", 100, name), new ColumnVarChar("name", 100, caller.getName()));

		if (rowsAffected > 0)
			return executeUpdate(ADD_FRIEND_RECORD, new ColumnVarChar("status", 100, "Pending"),
					new ColumnVarChar("name", 100, caller.getName()), new ColumnVarChar("name", 100, name)) > 0;

		return false;
	}

	/**
	 * @param playerName
	 *            - the name of the player whose current server status is being
	 *            fetched
	 * @return the {@link MinecraftServer} name that the player matching
	 *         {@code playerName} is currently online on, if they are online, null
	 *         otherwise.
	 */
	public String fetchPlayerServer(String playerName) {
		final PlayerStatus status = _repository.getElement(playerName);

		return status == null ? null : status.getServer();
	}

	public NautHashMap<String, FriendData> getFriendsForAll(Player... players) {
		final NautHashMap<String, FriendData> friends = new NautHashMap<>();

		final StringBuilder stringBuilder = new StringBuilder();
		stringBuilder.append(RETRIEVE_MULTIPLE_FRIEND_RECORDS + "(");

		for (final Player player : players) {
			stringBuilder.append("'" + player.getUniqueId() + "', ");
		}

		stringBuilder.delete(stringBuilder.length() - 2, stringBuilder.length());
		stringBuilder.append(");");

		executeQuery(stringBuilder.toString(), new ResultSetCallable() {
			@Override
			public void processResultSet(ResultSet resultSet) throws SQLException {
				final Set<FriendData> friendDatas = new HashSet<>();
				while (resultSet.next()) {
					final FriendStatus friend = new FriendStatus();

					final String uuidSource = resultSet.getString(1);
					friend.Name = resultSet.getString(2);
					friend.Status = Enum.valueOf(FriendStatusType.class, resultSet.getString(3));
					friend.LastSeenOnline = resultSet.getTimestamp(5).getTime() - resultSet.getTimestamp(4).getTime();
                    friend.Favorite = resultSet.getBoolean(6);
                    String targetUuidStr = resultSet.getString(7);
                    if (targetUuidStr != null) {
                        try {
                            friend.Uuid = java.util.UUID.fromString(targetUuidStr);
                        } catch (Exception ignored) {}
                    }

					if (!friends.containsKey(uuidSource)) {
						friends.put(uuidSource, new FriendData());
					}

					friends.get(uuidSource).getFriends().add(friend);

					friendDatas.add(friends.get(uuidSource));
				}

				// Load the server status of friends for all sources.
				for (final FriendData friendData : friendDatas) {
					loadFriendStatuses(friendData);
				}
			}
		});

		return friends;
	}

	@Override
	protected void initialize() {
		// executeUpdate(CREATE_FRIEND_TABLE);
	}

	public FriendData loadClientInformation(ResultSet resultSet) throws SQLException {
		final FriendData friendData = new FriendData();

		while (resultSet.next()) {
			final FriendStatus friend = new FriendStatus();

			friend.Name = resultSet.getString(1);
			friend.Status = Enum.valueOf(FriendStatusType.class, resultSet.getString(2));
			friend.LastSeenOnline = resultSet.getTimestamp(4).getTime() - resultSet.getTimestamp(3).getTime();
            friend.Favorite = resultSet.getBoolean(5);
            String targetUuidStr = resultSet.getString(6);
            if (targetUuidStr != null) {
                try {
                    friend.Uuid = java.util.UUID.fromString(targetUuidStr);
                } catch (Exception ignored) {}
            }
			friend.ServerName = null;
			friend.Online = friend.ServerName != null;
			friendData.getFriends().add(friend);
		}

		loadFriendStatuses(friendData);

		return friendData;
	}

	/**
	 * Load the server status information for a list of {@link FriendStatus}.
	 * 
	 * @param friendData
	 *            - the {@link FriendStatus} object friends server status' are to be
	 *            updated
	 * @param statuses
	 *            - the fetched {@link PlayerStatus} associated with all online
	 *            {@code friends}.
	 */
	public void loadFriendStatuses(FriendData friendData) {
		// Generate a set of all friend names
		final Set<String> friendNames = new HashSet<>();
		for (final FriendStatus status : friendData.getFriends()) {
			friendNames.add(status.Name);
		}

		// Load PlayerStatus' for friends
		final Collection<PlayerStatus> statuses = _repository.getElements(friendNames);

		// Load player statuses into a mapping
		final Map<String, PlayerStatus> playerStatuses = new HashMap<>();
		for (final PlayerStatus status : statuses) {
			playerStatuses.put(status.getName(), status);
		}

		// Load status information into friend data.
		for (final FriendStatus friend : friendData.getFriends()) {
			final PlayerStatus status = playerStatuses.get(friend.Name);
			friend.Online = status != null;
			friend.ServerName = friend.Online ? status.getServer() : null;
		}
	}

	public boolean removeFriend(String caller, String name) {
		final int rowsAffected = executeUpdate(DELETE_FRIEND_RECORD, new ColumnVarChar("name", 100, name),
				new ColumnVarChar("name", 100, caller));

		if (rowsAffected > 0)
			return executeUpdate(DELETE_FRIEND_RECORD, new ColumnVarChar("name", 100, caller),
					new ColumnVarChar("name", 100, name)) > 0;

		return false;
	}

	@Override
	protected void update() {
	}

	public boolean updateFriend(String caller, String name, String status) {
		return executeUpdate(UPDATE_MUTUAL_RECORD, new ColumnVarChar("status", 100, status),
				new ColumnVarChar("name", 100, name), new ColumnVarChar("name", 100, caller)) > 0;
	}

    public boolean updateFavorite(String caller, String name, boolean favorite) {
        return executeUpdate(UPDATE_FAVORITE_RECORD, new com.houzicore.shared.core.database.column.ColumnBoolean("favorite", favorite),
                new ColumnVarChar("name", 100, name), new ColumnVarChar("name", 100, caller)) > 0;
    }
}
