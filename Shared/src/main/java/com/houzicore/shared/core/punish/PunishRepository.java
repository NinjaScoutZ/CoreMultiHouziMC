package com.houzicore.shared.core.punish;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

import com.houzicore.shared.common.util.Callback;
import com.houzicore.shared.core.database.DBPool;
import com.houzicore.shared.core.punish.Tokens.PunishClientToken;
import com.houzicore.shared.core.punish.Tokens.PunishmentToken;

public class PunishRepository {

	public PunishRepository() {
	}

	public void LoadPunishClient(final String target, final Callback<PunishClientToken> callback) {
		com.houzicore.shared.common.util.HouziAsync.runAsync(() -> {
			final PunishClientToken token = new PunishClientToken();
			token.Name = target;
			token.Time = System.currentTimeMillis();
			token.Punishments = new ArrayList<>();

			try (Connection conn = DBPool.ACCOUNT.getConnection();
				 PreparedStatement stmt = conn.prepareStatement(
						 "SELECT * FROM punishments WHERE targetName = ?")) {
				stmt.setString(1, target);
				try (ResultSet rs = stmt.executeQuery()) {
					while (rs.next()) {
						final PunishmentToken pt = new PunishmentToken();
						pt.PunishmentId = rs.getInt("id");
						pt.Admin = rs.getString("admin");
						pt.Time = rs.getTimestamp("created").getTime();
						pt.Sentence = rs.getString("sentence");
						pt.Category = rs.getString("category");
						pt.Reason = rs.getString("reason");
						pt.Severity = rs.getInt("severity");
						pt.Duration = rs.getLong("duration");
						pt.Removed = rs.getBoolean("removed");
						pt.RemoveAdmin = rs.getString("removedAdmin");
						pt.RemoveReason = rs.getString("removedReason");
						pt.Active = rs.getBoolean("active");
						token.Punishments.add(pt);
					}
				}
			} catch (Exception e) {
				org.bukkit.Bukkit.getServer().getLogger().log(java.util.logging.Level.SEVERE, e.getMessage(), e);
			}

			callback.run(token);
		});
	}

	public void MatchPlayerName(final Callback<List<String>> callback, final String userName) {
		com.houzicore.shared.common.util.HouziAsync.runAsync(() -> {
			final List<String> names = new ArrayList<>();

			try (Connection conn = DBPool.ACCOUNT.getConnection();
				 PreparedStatement stmt = conn.prepareStatement(
						 "SELECT name FROM accounts WHERE name LIKE ? LIMIT 20")) {
				stmt.setString(1, "%" + userName + "%");
				try (ResultSet rs = stmt.executeQuery()) {
					while (rs.next()) {
						names.add(rs.getString("name"));
					}
				}
			} catch (Exception e) {
				org.bukkit.Bukkit.getServer().getLogger().log(java.util.logging.Level.SEVERE, e.getMessage(), e);
			}

			callback.run(names);
		});
	}

	public void Punish(final Callback<String> callback, String target, String category,
			PunishmentSentence punishment, String reason, double duration, String admin, int severity) {

		final String fTarget = target;
		final String fCategory = category;
		final String fSentence = punishment.toString();
		final String fReason = reason;
		final long fDuration = (long) duration;
		final String fAdmin = admin;
		final int fSeverity = severity;

		com.houzicore.shared.common.util.HouziAsync.runAsync(() -> {
			String result = "Punished";

			try (Connection conn = DBPool.ACCOUNT.getConnection()) {
				// Check if the account exists
				try (PreparedStatement check = conn.prepareStatement(
						"SELECT COUNT(*) FROM accounts WHERE name = ?")) {
					check.setString(1, fTarget);
					try (ResultSet rs = check.executeQuery()) {
						if (rs.next() && rs.getInt(1) == 0) {
							callback.run("AccountDoesNotExist");
							return;
						}
					}
				}

				// Insert punishment
				try (PreparedStatement stmt = conn.prepareStatement(
						"INSERT INTO punishments (targetName, category, sentence, reason, duration, severity, admin) VALUES (?, ?, ?, ?, ?, ?, ?)")) {
					stmt.setString(1, fTarget);
					stmt.setString(2, fCategory);
					stmt.setString(3, fSentence);
					stmt.setString(4, fReason);
					stmt.setLong(5, fDuration);
					stmt.setInt(6, fSeverity);
					stmt.setString(7, fAdmin);
					stmt.executeUpdate();
				}
			} catch (Exception e) {
				org.bukkit.Bukkit.getServer().getLogger().log(java.util.logging.Level.SEVERE, e.getMessage(), e);
				result = "Error";
			}

			callback.run(result);
		});
	}

	public void RemoveBan(String name, String reason) {
		try (Connection conn = DBPool.ACCOUNT.getConnection();
			 PreparedStatement stmt = conn.prepareStatement(
					 "UPDATE punishments SET active = 0 WHERE targetName = ? AND sentence = 'Ban' AND active = 1")) {
			stmt.setString(1, name);
			stmt.executeUpdate();
		} catch (Exception e) {
			org.bukkit.Bukkit.getServer().getLogger().log(java.util.logging.Level.SEVERE, e.getMessage(), e);
		}
	}

	public void RemovePunishment(final Callback<String> callback, final int id, String target,
			String reason, String admin) {

		final String fReason = reason;
		final String fAdmin = admin;

		com.houzicore.shared.common.util.HouziAsync.runAsync(() -> {
			String result = "Punished";

			try (Connection conn = DBPool.ACCOUNT.getConnection();
				 PreparedStatement stmt = conn.prepareStatement(
						 "UPDATE punishments SET active = 0, removed = 1, removedAdmin = ?, removedReason = ? WHERE id = ?")) {
				stmt.setString(1, fAdmin);
				stmt.setString(2, fReason);
				stmt.setInt(3, id);
				stmt.executeUpdate();
			} catch (Exception e) {
				org.bukkit.Bukkit.getServer().getLogger().log(java.util.logging.Level.SEVERE, e.getMessage(), e);
				result = "Error";
			}

			callback.run(result);
		});
	}
}
