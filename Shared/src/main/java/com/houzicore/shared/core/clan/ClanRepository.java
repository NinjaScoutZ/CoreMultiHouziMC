package com.houzicore.shared.core.clan;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import com.houzicore.shared.core.database.DBPool;
import com.houzicore.shared.core.database.RepositoryBase;
import org.bukkit.plugin.java.JavaPlugin;

public class ClanRepository extends RepositoryBase {

	public ClanRepository(JavaPlugin plugin) {
		super(plugin, DBPool.ACCOUNT);
	}

	@Override
	protected void update() {
	}

	@Override
	protected void initialize() {
	}

	public Clan getClan(String name) {
		try (Connection connection = getConnection();
			 PreparedStatement statement = connection.prepareStatement("SELECT * FROM clans WHERE name = ?;")) {
			statement.setString(1, name);
			try (ResultSet resultSet = statement.executeQuery()) {
				if (resultSet.next()) {
					return new Clan(resultSet.getInt("id"), resultSet.getString("name"), resultSet.getString("description"), resultSet.getInt("leaderId"));
				}
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return null;
	}

	public Clan getClanByAccountId(int accountId) {
		try (Connection connection = getConnection();
			 PreparedStatement statement = connection.prepareStatement(
					 "SELECT c.* FROM clans c INNER JOIN clanMembers m ON c.id = m.clanId WHERE m.accountId = ?;")) {
			statement.setInt(1, accountId);
			try (ResultSet resultSet = statement.executeQuery()) {
				if (resultSet.next()) {
					return new Clan(resultSet.getInt("id"), resultSet.getString("name"), resultSet.getString("description"), resultSet.getInt("leaderId"));
				}
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return null;
	}

	public boolean createClan(String name, int leaderAccountId) {
		try (Connection connection = getConnection()) {
			connection.setAutoCommit(false);
			
			int clanId = -1;
			try (PreparedStatement statement = connection.prepareStatement(
					"INSERT INTO clans (name, leaderId) VALUES (?, ?);", Statement.RETURN_GENERATED_KEYS)) {
				statement.setString(1, name);
				statement.setInt(2, leaderAccountId);
				statement.executeUpdate();
				
				try (ResultSet keys = statement.getGeneratedKeys()) {
					if (keys.next()) {
						clanId = keys.getInt(1);
					}
				}
			}
			
			if (clanId != -1) {
				try (PreparedStatement statement = connection.prepareStatement(
						"INSERT INTO clanMembers (clanId, accountId, role) VALUES (?, ?, 'LEADER');")) {
					statement.setInt(1, clanId);
					statement.setInt(2, leaderAccountId);
					statement.executeUpdate();
				}
			}
			
			connection.commit();
			return true;
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return false;
	}

	public void deleteClan(int clanId) {
		try (Connection connection = getConnection();
			 PreparedStatement statement = connection.prepareStatement("DELETE FROM clans WHERE id = ?;")) {
			statement.setInt(1, clanId);
			statement.executeUpdate();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	public void joinClan(int clanId, int accountId) {
		try (Connection connection = getConnection();
			 PreparedStatement statement = connection.prepareStatement(
					 "INSERT INTO clanMembers (clanId, accountId, role) VALUES (?, ?, 'MEMBER');")) {
			statement.setInt(1, clanId);
			statement.setInt(2, accountId);
			statement.executeUpdate();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	public void leaveClan(int accountId) {
		try (Connection connection = getConnection();
			 PreparedStatement statement = connection.prepareStatement("DELETE FROM clanMembers WHERE accountId = ?;")) {
			statement.setInt(1, accountId);
			statement.executeUpdate();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	public List<String> getClanMembers(int clanId) {
		List<String> members = new ArrayList<>();
		try (Connection connection = getConnection();
			 PreparedStatement statement = connection.prepareStatement(
					 "SELECT a.name FROM accounts a INNER JOIN clanMembers m ON a.id = m.accountId WHERE m.clanId = ?;")) {
			statement.setInt(1, clanId);
			try (ResultSet resultSet = statement.executeQuery()) {
				while (resultSet.next()) {
					members.add(resultSet.getString("name"));
				}
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return members;
	}
}
