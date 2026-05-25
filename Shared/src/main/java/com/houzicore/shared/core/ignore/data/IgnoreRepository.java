package com.houzicore.shared.core.ignore.data;

import java.sql.ResultSet;
import java.sql.SQLException;

import com.houzicore.shared.core.database.DBPool;
import com.houzicore.shared.core.database.RepositoryBase;
import com.houzicore.shared.core.database.column.ColumnVarChar;

import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

public class IgnoreRepository extends RepositoryBase {
	private static String ADD_IGNORE_RECORD = "INSERT INTO accountIgnore (uuidIgnorer, uuidIgnored) SELECT fA.uuid AS uuidIgnorer, tA.uuid AS uuidIgnored FROM accounts as fA LEFT JOIN accounts AS tA ON tA.name = ? WHERE fA.name = ?;";
	private static String DELETE_IGNORE_RECORD = "DELETE aF FROM accountIgnore AS aF INNER JOIN accounts as fA ON aF.uuidIgnorer = fA.uuid INNER JOIN accounts AS tA ON aF.uuidIgnored = tA.uuid WHERE fA.name = ? AND tA.name = ?;";

	public IgnoreRepository(JavaPlugin plugin) {
		super(plugin, DBPool.ACCOUNT);
	}

	public boolean addIgnore(final Player caller, String name) {
		final int rowsAffected = executeUpdate(ADD_IGNORE_RECORD, new ColumnVarChar("name", 100, name),
				new ColumnVarChar("name", 100, caller.getName()));

		return rowsAffected > 0;
	}

	@Override
	protected void initialize() {
	}

	public IgnoreData loadClientInformation(ResultSet resultSet) throws SQLException {
		final IgnoreData ignoreData = new IgnoreData();

		while (resultSet.next()) {
			ignoreData.getIgnored().add(resultSet.getString(1));
		}

		return ignoreData;
	}

	public boolean removeIgnore(String caller, String name) {
		final int rowsAffected = executeUpdate(DELETE_IGNORE_RECORD, new ColumnVarChar("name", 100, caller),
				new ColumnVarChar("name", 100, name));

		return rowsAffected > 0;
	}

	@Override
	protected void update() {
	}
}
