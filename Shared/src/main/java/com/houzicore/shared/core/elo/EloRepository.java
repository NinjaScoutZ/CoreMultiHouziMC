package com.houzicore.shared.core.elo;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.bukkit.plugin.java.JavaPlugin;

import com.houzicore.shared.core.database.DBPool;
import com.houzicore.shared.core.database.RepositoryBase;
import com.houzicore.shared.core.database.column.ColumnInt;
import com.houzicore.shared.core.database.column.ColumnVarChar;

public class EloRepository extends RepositoryBase {
	private static String INSERT_ELO = "INSERT INTO eloRating (uuid, gameType, elo) VALUES (?, ?, ?) ON DUPLICATE KEY UPDATE elo=VALUES(elo);";

	public EloRepository(JavaPlugin plugin) {
		super(plugin, DBPool.ACCOUNT);

		initialize();
	}

	@Override
	public void initialize() {
		// executeUpdate(CREATE_ELO_TABLE);
	}

	public EloClientData loadClientInformation(ResultSet resultSet) throws SQLException {
		final EloClientData clientData = new EloClientData();

		while (resultSet.next()) {
			clientData.Elos.put(resultSet.getString(1), resultSet.getInt(2));
		}

		return clientData;
	}

	public void saveElo(String uuid, String gameType, int elo) {
		executeUpdate(INSERT_ELO, new ColumnVarChar("uuid", 100, uuid), new ColumnVarChar("gameType", 100, gameType),
				new ColumnInt("elo", elo));
	}

	@Override
	protected void update() {
	}
}
