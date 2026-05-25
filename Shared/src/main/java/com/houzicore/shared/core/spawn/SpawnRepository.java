package com.houzicore.shared.core.spawn;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.bukkit.plugin.java.JavaPlugin;

import com.houzicore.shared.core.database.DBPool;
import com.houzicore.shared.core.database.RepositoryBase;
import com.houzicore.shared.core.database.ResultSetCallable;
import com.houzicore.shared.core.database.column.ColumnVarChar;

public class SpawnRepository extends RepositoryBase {
	private static String RETRIEVE_SPAWNS = "SELECT location FROM spawns WHERE serverName = ?;";
	private static String ADD_SERVER_SPAWN = "INSERT INTO spawns (serverName, location) VALUES (?, ?);";
	private static String DELETE_SERVER_SPAWN = "DELETE FROM spawns WHERE serverName = ?;";

	private final String _serverName;

	public SpawnRepository(JavaPlugin plugin, String serverName) {
		super(plugin, DBPool.ACCOUNT);
		_serverName = serverName;
	}

	public void addSpawn(String location) {
		executeUpdate(ADD_SERVER_SPAWN, new ColumnVarChar("serverName", 100, _serverName),
				new ColumnVarChar("location", 100, location));
	}

	public void clearSpawns() {
		executeUpdate(DELETE_SERVER_SPAWN, new ColumnVarChar("serverName", 100, _serverName));
	}

	@Override
	protected void initialize() {
		// executeUpdate(CREATE_SPAWN_TABLE);
	}

	public List<String> retrieveSpawns() {
		final List<String> spawns = new ArrayList<>();

		executeQuery(RETRIEVE_SPAWNS, new ResultSetCallable() {
			@Override
			public void processResultSet(ResultSet resultSet) throws SQLException {
				while (resultSet.next()) {
					spawns.add(resultSet.getString(1));
				}
			}
		}, new ColumnVarChar("serverName", 100, _serverName));

		return spawns;
	}

	@Override
	protected void update() {
	}
}
