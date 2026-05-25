package com.houzicore.bungeecord.playerStats;

import com.houzicore.shared.core.database.DBPool;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class PlayerStatsRepository {
    
    private static final String INSERT_OR_UPDATE_IP = "INSERT INTO accountIps (accountId, ipAddress, country) VALUES (?, ?, ?) ON DUPLICATE KEY UPDATE country = VALUES(country), lastSeen = CURRENT_TIMESTAMP;";
    private static final String INSERT_NETWORK_STAT = "INSERT INTO networkPlayerStats (playerCount) VALUES (?);";
    private static final String SELECT_ACCOUNT_ID = "SELECT id FROM accounts WHERE uuid = ?;";

    public PlayerStatsRepository() {
    }

    private Connection getConnection() throws SQLException {
        return DBPool.ACCOUNT.getConnection();
    }

    public void updatePlayerIp(int accountId, String ipAddress, String country) {
        try (Connection connection = getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(INSERT_OR_UPDATE_IP)) {
            
            preparedStatement.setInt(1, accountId);
            preparedStatement.setString(2, ipAddress);
            preparedStatement.setString(3, country);
            
            preparedStatement.executeUpdate();
        } catch (Exception exception) {
            exception.printStackTrace();
        }
    }

    public void logNetworkPlayerCount(int playerCount) {
        try (Connection connection = getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(INSERT_NETWORK_STAT)) {
            
            preparedStatement.setInt(1, playerCount);
            preparedStatement.executeUpdate();
            
        } catch (Exception exception) {
            exception.printStackTrace();
        }
    }

    public int getAccountId(String uuid) {
        try (Connection connection = getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(SELECT_ACCOUNT_ID)) {
            
            preparedStatement.setString(1, uuid);
            try (java.sql.ResultSet resultSet = preparedStatement.executeQuery()) {
                if (resultSet.next()) {
                    return resultSet.getInt("id");
                }
            }
        } catch (Exception exception) {
            exception.printStackTrace();
        }
        return -1;
    }
}
