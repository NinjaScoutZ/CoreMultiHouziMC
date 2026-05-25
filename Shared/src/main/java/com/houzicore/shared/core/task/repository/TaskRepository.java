package com.houzicore.shared.core.task.repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.bukkit.plugin.java.JavaPlugin;

import com.houzicore.shared.core.database.DBPool;
import com.houzicore.shared.core.database.RepositoryBase;
import com.houzicore.shared.core.database.ResultSetCallable;
import com.houzicore.shared.core.database.column.ColumnInt;
import com.houzicore.shared.core.database.column.ColumnVarChar;
import com.houzicore.shared.core.task.Task;
import com.houzicore.shared.core.task.TaskClient;

public class TaskRepository extends RepositoryBase {
	private static String ADD_ACCOUNT_TASK = "INSERT INTO accountTasks (accountId, taskId) VALUES (?, ?);";

	private static String ADD_TASK = "INSERT INTO tasks (name) VALUES (?);";
	private static String RETRIEVE_TASKS = "SELECT id, name FROM tasks;";

	public TaskRepository(JavaPlugin plugin) {
		super(plugin, DBPool.ACCOUNT);
	}

	public boolean addAccountTask(int accountId, int taskId) {
		return executeUpdate(ADD_ACCOUNT_TASK, new ColumnInt("accountId", accountId),
				new ColumnInt("taskId", taskId)) > 0;
	}

	public void addTask(String task) {
		executeUpdate(ADD_TASK, new ColumnVarChar("name", 100, task));
	}

	@Override
	protected void initialize() {
	}

	public TaskClient loadClientInformation(ResultSet resultSet) throws SQLException {
		final TaskClient taskClient = new TaskClient();

		while (resultSet.next()) {
			taskClient.TasksCompleted.add(resultSet.getInt(1));
		}

		return taskClient;
	}

	public List<Task> retrieveTasks() {
		final List<Task> tasks = new ArrayList<>();

		executeQuery(RETRIEVE_TASKS, new ResultSetCallable() {
			@Override
			public void processResultSet(ResultSet resultSet) throws SQLException {
				while (resultSet.next()) {
					tasks.add(new Task(resultSet.getInt(1), resultSet.getString(2)));
				}
			}
		});

		return tasks;
	}

	@Override
	protected void update() {
	}
}
