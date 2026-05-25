package com.houzicore.shared.core.bonuses;

import com.houzicore.shared.core.database.DBPool;
import com.houzicore.shared.core.database.RepositoryBase;
import org.bukkit.plugin.java.JavaPlugin;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class BonusRepository extends RepositoryBase {

    public BonusRepository(JavaPlugin plugin) {
        super(plugin, DBPool.ACCOUNT);
    }

    @Override
    protected void initialize() {
    }

    @Override
    protected void update() {
    }

    public void loadBonusData(int accountId, BonusClientData data) {
        try (Connection connection = getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement("SELECT * FROM accountBonuses WHERE accountId = ?;")) {
            preparedStatement.setInt(1, accountId);
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                if (resultSet.next()) {
                    data.DailyTime = resultSet.getLong("dailyTime");
                    data.VoteTime = resultSet.getLong("voteTime");
                    data.RankTime = resultSet.getLong("rankTime");
                    data.DailyStreak = resultSet.getInt("dailyStreak");
                    data.VoteStreak = resultSet.getInt("voteStreak");
                    data.Tickets = resultSet.getInt("tickets");
                } else {
                    // Create if not exists
                    try (PreparedStatement insert = connection.prepareStatement("INSERT IGNORE INTO accountBonuses (accountId) VALUES (?);")) {
                        insert.setInt(1, accountId);
                        insert.executeUpdate();
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void updateDailyBonus(int accountId, long time, int streak) {
        try (Connection connection = getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement("UPDATE accountBonuses SET dailyTime = ?, dailyStreak = ? WHERE accountId = ?;")) {
            preparedStatement.setLong(1, time);
            preparedStatement.setInt(2, streak);
            preparedStatement.setInt(3, accountId);
            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void updateVoteBonus(int accountId, long time, int streak) {
        try (Connection connection = getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement("UPDATE accountBonuses SET voteTime = ?, voteStreak = ? WHERE accountId = ?;")) {
            preparedStatement.setLong(1, time);
            preparedStatement.setInt(2, streak);
            preparedStatement.setInt(3, accountId);
            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void updateRankBonus(int accountId, long time) {
        try (Connection connection = getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement("UPDATE accountBonuses SET rankTime = ? WHERE accountId = ?;")) {
            preparedStatement.setLong(1, time);
            preparedStatement.setInt(2, accountId);
            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void updateTickets(int accountId, int tickets) {
        try (Connection connection = getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement("UPDATE accountBonuses SET tickets = ? WHERE accountId = ?;")) {
            preparedStatement.setInt(1, tickets);
            preparedStatement.setInt(2, accountId);
            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
