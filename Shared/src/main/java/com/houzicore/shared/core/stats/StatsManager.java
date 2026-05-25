package com.houzicore.shared.core.stats;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Iterator;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import com.houzicore.shared.MiniDbClientPlugin;
import com.houzicore.shared.account.CoreClientManager;
import com.houzicore.shared.common.util.NautHashMap;
import com.houzicore.shared.common.util.UtilServer;
import com.houzicore.shared.core.stats.command.GiveStatCommand;
import com.houzicore.shared.core.stats.command.TimeCommand;
import com.houzicore.shared.core.stats.event.StatChangeEvent;

public class StatsManager extends MiniDbClientPlugin<PlayerStats> {
	private static Object _statSync = new Object();

	private final StatsRepository _repository;

	private final NautHashMap<String, Integer> _stats = new NautHashMap<>();
	private final NautHashMap<Integer, NautHashMap<String, Long>> _statUploadQueue = new NautHashMap<>();
	private Runnable _saveRunnable;

	public StatsManager(JavaPlugin plugin, CoreClientManager clientManager) {
		super("Stats Manager", plugin, clientManager);

		_repository = new StatsRepository(plugin);

		if (_saveRunnable == null) {
			_saveRunnable = new Runnable() {
				@Override
				public void run() {
					saveStats();
				}
			};

			plugin.getServer().getScheduler().runTaskTimerAsynchronously(plugin, _saveRunnable, 20L, 20L);
		}

		for (final Stat stat : _repository.retrieveStats()) {
			_stats.put(stat.Name, stat.Id);
		}

		clientManager.addStoredProcedureLoginProcessor(new SecondaryStatHandler(this, _repository));
	}

	@Override
	public void addCommands() {
		addCommand(new TimeCommand(this));
		addCommand(new GiveStatCommand(this));
	}

	@Override
	protected PlayerStats AddPlayer(String player) {
		return new PlayerStats();
	}

	private void addToQueue(String statName, Player player, long value) {
		final com.houzicore.shared.account.CoreClient client = getClientManager().Get(player);
		if (client == null || client.getAccountId() <= 0) return;
		final int accountId = client.getAccountId();

		synchronized (_statSync) {
			if (!_statUploadQueue.containsKey(accountId)) {
				_statUploadQueue.put(accountId, new NautHashMap<String, Long>());
			}

			if (!_statUploadQueue.get(accountId).containsKey(statName)) {
				_statUploadQueue.get(accountId).put(statName, 0L);
			}

			_statUploadQueue.get(accountId).put(statName, _statUploadQueue.get(accountId).get(statName) + value);
		}
	}

	public PlayerStats getOfflinePlayerStats(String playerName) throws SQLException {
		return _repository.loadOfflinePlayerStats(playerName);
	}

	public void getTopStatsAsync(final String statName, final int limit, final com.houzicore.shared.common.util.Callback<java.util.LinkedHashMap<String, Long>> callback) {
		Bukkit.getScheduler().runTaskAsynchronously(getPlugin(), new Runnable() {
			@Override
			public void run() {
				final java.util.LinkedHashMap<String, Long> top = _repository.getTopStats(statName, limit);
				Bukkit.getScheduler().runTask(getPlugin(), new Runnable() {
					@Override
					public void run() {
						callback.run(top);
					}
				});
			}
		});
	}

	public void getLowestStatsAsync(final String statName, final int limit, final com.houzicore.shared.common.util.Callback<java.util.LinkedHashMap<String, Long>> callback) {
		Bukkit.getScheduler().runTaskAsynchronously(getPlugin(), new Runnable() {
			@Override
			public void run() {
				final java.util.LinkedHashMap<String, Long> top = _repository.getLowestStats(statName, limit);
				Bukkit.getScheduler().runTask(getPlugin(), new Runnable() {
					@Override
					public void run() {
						callback.run(top);
					}
				});
			}
		});
	}

	@Override
	public String getQuery(int accountId, String uuid, String name) {
		return "SELECT stats.name, value FROM accountStat INNER JOIN stats ON stats.id = accountStat.statId WHERE accountStat.accountId = '"
				+ accountId + "';";
	}

	public int getStatId(String statName) {
		Integer id = _stats.get(statName);
		return id == null ? -1 : id;
	}

	public boolean incrementStat(final int accountId, final String statName, final long value) {
		if (_stats.containsKey(statName))
			return false;

		final NautHashMap<Integer, NautHashMap<Integer, Long>> uploadQueue = new NautHashMap<>();
		uploadQueue.put(accountId, new NautHashMap<Integer, Long>());
		uploadQueue.get(accountId).put(_stats.get(statName), value);

		runAsync(new Runnable() {
			@Override
			public void run() {
				_repository.saveStats(uploadQueue);
			}
		});

		return true;
	}

	public void incrementStat(final Player player, final String statName, final long value) {
		final long newValue = Get(player).addStat(statName, value);

		// Event
		UtilServer.getServer().getPluginManager()
				.callEvent(new StatChangeEvent(player.getName(), statName, newValue - value, newValue));

		// Verify stat is in our local cache, if not add it remotely.
		if (!_stats.containsKey(statName)) {
			Bukkit.getScheduler().runTaskAsynchronously(getPlugin(), new Runnable() {
				@Override
				public void run() {
					synchronized (_statSync) {
						// If many players come in for a new stat, when the first add finishes the
						// others are queued to add again
						// This makes a second check for the stat name (already added before lock was
						// released)
						// Then it pops into queue and forgets adding the new stat to db.
						if (_stats.containsKey(statName)) {
							addToQueue(statName, player, value);
							return;
						}

						_repository.addStat(statName);

						_stats.clear();

						for (final Stat stat : _repository.retrieveStats()) {
							_stats.put(stat.Name, stat.Id);
						}

						addToQueue(statName, player, value);
					}
				}
			});
		} else {
			addToQueue(statName, player, value);
		}
	}

	public void setStatIfLower(final Player player, final String statName, final long value) {
		final com.houzicore.shared.account.CoreClient client = getClientManager().Get(player);
		if (client == null || client.getAccountId() <= 0) return;
		
		final int accountId = client.getAccountId();

		if (!_stats.containsKey(statName)) {
			Bukkit.getScheduler().runTaskAsynchronously(getPlugin(), new Runnable() {
				@Override
				public void run() {
					synchronized (_statSync) {
						if (!_stats.containsKey(statName)) {
							_repository.addStat(statName);
							_stats.clear();
							for (final Stat stat : _repository.retrieveStats()) {
								_stats.put(stat.Name, stat.Id);
							}
						}
						_repository.saveLowestStat(accountId, _stats.get(statName), value);
					}
				}
			});
		} else {
			_repository.saveLowestStat(accountId, _stats.get(statName), value);
		}
	}

	@Override
	public void processLoginResultSet(String playerName, int accountId, ResultSet resultSet) throws SQLException {
		Set(playerName, _repository.loadClientInformation(resultSet));
	}

	public void replacePlayerHack(String playerName, PlayerStats playerStats) {
		Set(playerName, playerStats);
	}

	protected void saveStats() {
		if (_statUploadQueue.isEmpty())
			return;

		try {
			final NautHashMap<Integer, NautHashMap<Integer, Long>> uploadQueue = new NautHashMap<>();

			synchronized (_statSync) {
				for (final Integer accountId : _statUploadQueue.keySet()) {
					uploadQueue.put(accountId, new NautHashMap<Integer, Long>());

					for (final String statName : _statUploadQueue.get(accountId).keySet()) {
						final int statId = _stats.get(statName);
						uploadQueue.get(accountId).put(statId, _statUploadQueue.get(accountId).get(statName));
					}
				}
				_statUploadQueue.clear();
			}

			_repository.saveStats(uploadQueue);
		} catch (final Exception exception) {
			org.bukkit.Bukkit.getServer().getLogger().log(java.util.logging.Level.SEVERE, exception.getMessage(), exception);
		}
	}
}
