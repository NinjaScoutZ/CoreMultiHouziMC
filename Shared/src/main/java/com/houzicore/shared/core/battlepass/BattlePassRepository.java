package com.houzicore.shared.core.battlepass;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.bukkit.plugin.java.JavaPlugin;

import com.houzicore.shared.core.database.DBPool;
import com.houzicore.shared.core.database.RepositoryBase;
import com.houzicore.shared.core.database.column.ColumnInt;
import com.houzicore.shared.core.database.column.ColumnVarChar;

public class BattlePassRepository extends RepositoryBase {

	private static final String CREATE_TABLE = "CREATE TABLE IF NOT EXISTS battlepass_progress ("
			+ "accountId INT NOT NULL, "
			+ "season VARCHAR(50) NOT NULL, "
			+ "xp INT DEFAULT 0, "
			+ "claimedTiers VARCHAR(255) DEFAULT '', "
			+ "PRIMARY KEY (accountId, season)"
			+ ");";

	private static final String SAVE_DATA = "INSERT INTO battlepass_progress (accountId, season, xp, claimedTiers) "
			+ "VALUES (?, ?, ?, ?) ON DUPLICATE KEY UPDATE xp = ?, claimedTiers = ?;";

	public BattlePassRepository(JavaPlugin plugin) {
		super(plugin, DBPool.ACCOUNT);
	}

	@Override
	protected void initialize() {
		executeUpdate(CREATE_TABLE);
	}

	@Override
	protected void update() {
	}

	public BattlePassData loadClientInformation(ResultSet resultSet) throws SQLException {
		BattlePassData data = new BattlePassData();
		if (resultSet.next()) {
			data.setXp(resultSet.getInt("xp"));
			data.loadClaimedTiersFromString(resultSet.getString("claimedTiers"));
		}
		return data;
	}

	public void saveClientInformation(int accountId, String season, BattlePassData data) {
		executeUpdate(SAVE_DATA, 
				new ColumnInt("accountId", accountId),
				new ColumnVarChar("season", 50, season),
				new ColumnInt("xp", data.getXp()),
				new ColumnVarChar("claimedTiers", 255, data.getClaimedTiersString()),
				new ColumnInt("xp_update", data.getXp()),
				new ColumnVarChar("claimedTiers_update", 255, data.getClaimedTiersString())
		);
	}
}
