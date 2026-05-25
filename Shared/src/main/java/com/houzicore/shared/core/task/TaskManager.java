package com.houzicore.shared.core.task;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import com.houzicore.shared.MiniDbClientPlugin;
import com.houzicore.shared.account.CoreClientManager;
import com.houzicore.shared.common.util.Callback;
import com.houzicore.shared.common.util.NautHashMap;
import com.houzicore.shared.core.task.repository.TaskRepository;

public class TaskManager extends MiniDbClientPlugin<TaskClient> {
	private static Object _taskLock = new Object();
	private final TaskRepository _repository;

	private final NautHashMap<String, Integer> _tasks = new NautHashMap<>();

	public TaskManager(JavaPlugin plugin, CoreClientManager clientManager, String webServerAddress) {
		super("Task Manager", plugin, clientManager);

		_repository = new TaskRepository(plugin);
		updateTasks();
	}

	@Override
	protected TaskClient AddPlayer(String playerName) {
		return new TaskClient();
	}

	public void addTaskForOfflinePlayer(final Callback<Boolean> callback, final UUID uuid, final String task) {
		Bukkit.getServer().getScheduler().runTaskAsynchronously(getPlugin(), new Runnable() {
			@Override
			public void run() {
				synchronized (_taskLock) {
					if (!_tasks.containsKey(task)) {
						_repository.addTask(task);
					}
				}

				updateTasks();

				synchronized (_taskLock) {
					final boolean success = _repository.addAccountTask(ClientManager.getCachedClientAccountId(uuid),
							_tasks.get(task));

					if (callback != null) {
						Bukkit.getServer().getScheduler().runTask(getPlugin(), new Runnable() {
							@Override
							public void run() {
								callback.run(success);
							}
						});
					}
				}
			}
		});
	}

	public void completedTask(final Callback<Boolean> callback, final Player player, final String taskName) {
		synchronized (_taskLock) {
			if (_tasks.containsKey(taskName)) {
				Get(player.getName()).TasksCompleted.add(_tasks.get(taskName));
			}
		}

		addTaskForOfflinePlayer(new Callback<Boolean>() {
			@Override
			public void run(Boolean success) {
				if (!success) {

					synchronized (_taskLock) {
						if (_tasks.containsKey(taskName)) {
							Get(player.getName()).TasksCompleted.remove(_tasks.get(taskName));
						}
					}
				}

				if (callback != null) {
					callback.run(success);
				}
			}
		}, player.getUniqueId(), taskName);
	}

	@Override
	public String getQuery(int accountId, String uuid, String name) {
		return "SELECT taskId FROM accountTasks WHERE accountId = '" + accountId + "';";
	}

	public Integer getTaskId(String taskName) {
		synchronized (_taskLock) {
			return _tasks.get(taskName);
		}
	}

	public boolean hasCompletedTask(Player player, String taskName) {
		synchronized (_taskLock) {
			if (!_tasks.containsKey(taskName))
				return false;

			return Get(player.getName()).TasksCompleted.contains(_tasks.get(taskName));
		}
	}

	@Override
	public void processLoginResultSet(String playerName, int accountId, ResultSet resultSet) throws SQLException {
		Set(playerName, _repository.loadClientInformation(resultSet));
	}

	private void updateTasks() {
		final List<Task> tasks = _repository.retrieveTasks();

		synchronized (_taskLock) {
			for (final Task task : tasks) {
				_tasks.put(task.Name, task.Id);
			}
		}
	}
}
