package com.houzicore.shared.core.thank;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.bukkit.plugin.java.JavaPlugin;

import com.houzicore.shared.core.database.DBPool;
import com.houzicore.shared.core.database.RepositoryBase;

public class ThankRepository extends RepositoryBase {
    public ThankRepository(JavaPlugin plugin) {
        super(plugin, DBPool.ACCOUNT);
    }

    @Override
    protected void initialize() {
    }

    @Override
    protected void update() {
    }

    public boolean addThank(int senderAccountId, int receiverAccountId, String reason) {
        try (Connection connection = getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement("INSERT IGNORE INTO accountThanks (senderAccountId, receiverAccountId, reason) VALUES (?, ?, ?);")) {

            preparedStatement.setInt(1, senderAccountId);
            preparedStatement.setInt(2, receiverAccountId);
            preparedStatement.setString(3, reason);

            return preparedStatement.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }
}
