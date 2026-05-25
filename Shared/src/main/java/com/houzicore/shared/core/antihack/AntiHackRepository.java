package com.houzicore.shared.core.antihack;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import com.houzicore.shared.core.database.DBPool;

////import org.bukkit.craftbukkit.v1_21_R1.entity.CraftPlayer;
import org.bukkit.entity.Player;

public class AntiHackRepository {
	private static String CREATE_TABLE = "CREATE TABLE IF NOT EXISTS AntiHack_Kick_Log (id INT NOT NULL AUTO_INCREMENT, updated LONG, playerName VARCHAR(256), motd VARCHAR(56), gameType VARCHAR(56), map VARCHAR(256), serverName VARCHAR(256), report VARCHAR(256), ping VARCHAR(25), PRIMARY KEY (id));";

	private static String UPDATE_PLAYER_OFFENSES = "INSERT INTO AntiHack_Kick_Log (updated, playerName, motd, gameType, map, serverName, report, ping) VALUES (now(), ?, ?, ?, ?, ?, ?, ?);";
	private final String _serverName;

	public AntiHackRepository(String serverName) {
		_serverName = serverName;
	}

	public void initialize() {
		PreparedStatement preparedStatement = null;

		try (Connection connection = DBPool.STATS_HOUZI.getConnection()) {
			// Create table
			preparedStatement = connection.prepareStatement(CREATE_TABLE);
			preparedStatement.execute();
		} catch (final Exception exception) {
			org.bukkit.Bukkit.getServer().getLogger().log(java.util.logging.Level.SEVERE, exception.getMessage(), exception);
		} finally {
			if (preparedStatement != null) {
				try {
					preparedStatement.close();
				} catch (final SQLException e) {
					org.bukkit.Bukkit.getServer().getLogger().log(java.util.logging.Level.SEVERE, e.getMessage(), e);
				}
			}
		}
	}

	public void saveOffense(final Player player, final String motd, final String game, final String map,
			final String report) {
		new Thread(new Runnable() {
			@Override
			public void run() {
				PreparedStatement preparedStatement = null;

				try (Connection connection = DBPool.STATS_HOUZI.getConnection()) {
					preparedStatement = connection.prepareStatement(UPDATE_PLAYER_OFFENSES);

					preparedStatement.setString(1, player.getName());
					preparedStatement.setString(2, motd);
					preparedStatement.setString(3, game);
					preparedStatement.setString(4, map);
					preparedStatement.setString(5, _serverName);
					preparedStatement.setString(6, report);
					preparedStatement.setString(7, player.getPing() + "ms");

					preparedStatement.execute();
				} catch (final Exception exception) {
					org.bukkit.Bukkit.getServer().getLogger().log(java.util.logging.Level.SEVERE, exception.getMessage(), exception);
				} finally {
					if (preparedStatement != null) {
						try {
							preparedStatement.close();
						} catch (final SQLException e) {
							org.bukkit.Bukkit.getServer().getLogger().log(java.util.logging.Level.SEVERE, e.getMessage(), e);
						}
					}
				}
			}
		}).start();
	}
}
