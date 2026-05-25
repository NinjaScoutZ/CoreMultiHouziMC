package com.houzicore.shared.core.simpleStats;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import com.houzicore.shared.common.util.NautHashMap;

public class SimpleStatsRepository {
	private static Object _connectionLock = new Object();

	private static String RETRIEVE_STATS_RECORDS = "SELECT simpleStats.statName, simpleStats.statValue FROM simpleStats;";
	private static String STORE_STATS_RECORD = "INSERT INTO simpleStats (statName,statValue) VALUES(?,?);";
	private static String RETRIEVE_STAT_RECORD = "SELECT simpleStats.statName, simpleStats.statValue FROM simpleStats WHERE statName = '?';";

	private final String _connectionString = "jdbc:mysql://127.0.0.1:3306/houzi?autoReconnect=true&failOverReadOnly=false&maxReconnects=10";
	private final String _userName = "root";
	private final String _password = ""; // Try to obfuscate this in the future!

	private Connection _connection = null;

	public void initialize() {
		/*
		 * PreparedStatement preparedStatement = null;
		 * 
		 * try { Class.forName("com.mysql.jdbc.Driver");
		 * 
		 * _connection = DriverManager.getConnection(_connectionString, _userName,
		 * _password);
		 * 
		 * // Create table preparedStatement =
		 * _connection.prepareStatement(CREATE_STATS_TABLE);
		 * preparedStatement.execute(); } catch (Exception exception) {
		 * org.bukkit.Bukkit.getServer().getLogger().log(java.util.logging.Level.SEVERE, exception.getMessage(), exception); } finally { if (preparedStatement != null) { try
		 * { preparedStatement.close(); } catch (SQLException e) { org.bukkit.Bukkit.getServer().getLogger().log(java.util.logging.Level.SEVERE, e.getMessage(), e);
		 * } } }
		 */
	}

	public NautHashMap<String, String> retrieveStat(String statName) {
		ResultSet resultSet = null;
		PreparedStatement preparedStatement = null;
		final NautHashMap<String, String> statRecords = new NautHashMap<>();

		try {
			synchronized (_connectionLock) {
				if (_connection.isClosed()) {
					_connection = DriverManager.getConnection(_connectionString, _userName, _password);
				}

				preparedStatement = _connection.prepareStatement(RETRIEVE_STAT_RECORD);
				preparedStatement.setString(1, statName);

				resultSet = preparedStatement.executeQuery();

				while (resultSet.next()) {
					statRecords.put(resultSet.getString(1), resultSet.getString(2));
				}
			}
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

			if (resultSet != null) {
				try {
					resultSet.close();
				} catch (final SQLException e) {
					org.bukkit.Bukkit.getServer().getLogger().log(java.util.logging.Level.SEVERE, e.getMessage(), e);
				}
			}
		}

		return statRecords;
	}

	public NautHashMap<String, String> retrieveStatRecords() {
		ResultSet resultSet = null;
		PreparedStatement preparedStatement = null;
		final NautHashMap<String, String> statRecords = new NautHashMap<>();

		try {
			synchronized (_connectionLock) {
				if (_connection.isClosed()) {
					_connection = DriverManager.getConnection(_connectionString, _userName, _password);
				}

				preparedStatement = _connection.prepareStatement(RETRIEVE_STATS_RECORDS);

				resultSet = preparedStatement.executeQuery();

				while (resultSet.next()) {
					statRecords.put(resultSet.getString(1), resultSet.getString(2));
				}
			}
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

			if (resultSet != null) {
				try {
					resultSet.close();
				} catch (final SQLException e) {
					org.bukkit.Bukkit.getServer().getLogger().log(java.util.logging.Level.SEVERE, e.getMessage(), e);
				}
			}
		}

		return statRecords;
	}

	public void storeStatValue(String statName, String statValue) {
		PreparedStatement preparedStatement = null;

		try {
			synchronized (_connectionLock) {
				if (_connection.isClosed()) {
					_connection = DriverManager.getConnection(_connectionString, _userName, _password);
				}

				preparedStatement = _connection.prepareStatement(STORE_STATS_RECORD);
				preparedStatement.setString(1, statName);
				preparedStatement.setString(2, statValue);

				preparedStatement.executeUpdate();
			}
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
}
