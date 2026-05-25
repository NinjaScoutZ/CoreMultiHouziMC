package com.houzicore.shared.core.quest;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.bukkit.plugin.java.JavaPlugin;

import com.houzicore.shared.core.database.DBPool;
import com.houzicore.shared.core.database.RepositoryBase;
import com.houzicore.shared.core.database.column.ColumnBoolean;
import com.houzicore.shared.core.database.column.ColumnInt;
import com.houzicore.shared.core.database.column.ColumnLong;

public class QuestRepository extends RepositoryBase {

	private static final String CREATE_TABLE = "CREATE TABLE IF NOT EXISTS player_quests ("
			+ "accountId INT NOT NULL, "
			+ "questId INT NOT NULL, "
			+ "periodId BIGINT NOT NULL, "
			+ "progress INT DEFAULT 0, "
			+ "completed BOOLEAN DEFAULT false, "
			+ "PRIMARY KEY (accountId, questId)"
			+ ");";

	private static final String SAVE_QUEST = "INSERT INTO player_quests (accountId, questId, periodId, progress, completed) "
			+ "VALUES (?, ?, ?, ?, ?) ON DUPLICATE KEY UPDATE periodId = ?, progress = ?, completed = ?;";

	public QuestRepository(JavaPlugin plugin) {
		super(plugin, DBPool.ACCOUNT);
	}

	@Override
	protected void initialize() {
		executeUpdate(CREATE_TABLE);
	}

	@Override
	protected void update() {
	}

	public QuestData loadClientInformation(ResultSet resultSet) throws SQLException {
		QuestData data = new QuestData();
		while (resultSet.next()) {
			PlayerQuest q = new PlayerQuest(
				resultSet.getInt("questId"),
				resultSet.getLong("periodId"),
				resultSet.getInt("progress"),
				resultSet.getBoolean("completed")
			);
			data.addQuest(q);
		}
		return data;
	}

	public void saveQuest(int accountId, PlayerQuest quest) {
		executeUpdate(SAVE_QUEST, 
				new ColumnInt("accountId", accountId),
				new ColumnInt("questId", quest.getQuestId()),
				new ColumnLong("periodId", quest.getPeriodId()),
				new ColumnInt("progress", quest.getProgress()),
				new ColumnBoolean("completed", quest.isCompleted()),
				new ColumnLong("periodId_update", quest.getPeriodId()),
				new ColumnInt("progress_update", quest.getProgress()),
				new ColumnBoolean("completed_update", quest.isCompleted())
		);
	}
}
