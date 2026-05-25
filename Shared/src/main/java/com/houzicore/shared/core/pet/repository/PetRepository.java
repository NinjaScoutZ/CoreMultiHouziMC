package com.houzicore.shared.core.pet.repository;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import com.houzicore.shared.core.pet.repository.token.ClientPetToken;
import com.houzicore.shared.core.pet.repository.token.PetChangeToken;
import com.houzicore.shared.core.pet.repository.token.PetExtraToken;
import com.houzicore.shared.core.pet.repository.token.PetSalesToken;
import com.houzicore.shared.core.pet.repository.token.PetToken;
import com.houzicore.shared.core.database.DBPool;

public class PetRepository {
	public PetRepository(String webAddress) {
		// Ignored. Kept for constructor compatibility.
	}

	public void AddPet(PetChangeToken token) {
		String query = "INSERT IGNORE INTO accountPets (accountId, petType, petName, equipment) SELECT id, ?, ?, ? FROM accounts WHERE name = ?;";
		try (Connection c = DBPool.ACCOUNT.getConnection(); PreparedStatement ps = c.prepareStatement(query)) {
			ps.setString(1, token.PetType);
			ps.setString(2, token.PetName);
			ps.setString(3, token.Equipment);
			ps.setString(4, token.Name);
			ps.executeUpdate();
		} catch (SQLException e) {
			org.bukkit.Bukkit.getServer().getLogger().log(java.util.logging.Level.SEVERE, e.getMessage(), e);
		}
	}

	public void AddPetNameTag(String name) {
		String query = "UPDATE accounts SET petNameTagCount = petNameTagCount + 1 WHERE name = ?;";
		try (Connection c = DBPool.ACCOUNT.getConnection(); PreparedStatement ps = c.prepareStatement(query)) {
			ps.setString(1, name);
			ps.executeUpdate();
		} catch (SQLException e) {
			org.bukkit.Bukkit.getServer().getLogger().log(java.util.logging.Level.SEVERE, e.getMessage(), e);
		}
	}

	public List<PetExtraToken> GetPetExtras(List<PetExtraToken> petExtraTokens) {
		return new ArrayList<>();
	}

	public List<PetSalesToken> GetPets(List<PetSalesToken> petTokens) {
		return new ArrayList<>();
	}

	public void RemovePet(PetChangeToken token) {
		String query = "DELETE p FROM accountPets p JOIN accounts a ON p.accountId = a.id WHERE a.name = ? AND p.petType = ?;";
		try (Connection c = DBPool.ACCOUNT.getConnection(); PreparedStatement ps = c.prepareStatement(query)) {
			ps.setString(1, token.Name);
			ps.setString(2, token.PetType);
			ps.executeUpdate();
		} catch (SQLException e) {
			org.bukkit.Bukkit.getServer().getLogger().log(java.util.logging.Level.SEVERE, e.getMessage(), e);
		}
	}

	public void RemovePetNameTag(String name) {
		String query = "UPDATE accounts SET petNameTagCount = petNameTagCount - 1 WHERE name = ? AND petNameTagCount > 0;";
		try (Connection c = DBPool.ACCOUNT.getConnection(); PreparedStatement ps = c.prepareStatement(query)) {
			ps.setString(1, name);
			ps.executeUpdate();
		} catch (SQLException e) {
			org.bukkit.Bukkit.getServer().getLogger().log(java.util.logging.Level.SEVERE, e.getMessage(), e);
		}
	}

	public void UpdatePet(PetChangeToken token) {
		String query = "UPDATE accountPets p JOIN accounts a ON p.accountId = a.id SET p.petName = ?, p.equipment = ? WHERE a.name = ? AND p.petType = ?;";
		try (Connection c = DBPool.ACCOUNT.getConnection(); PreparedStatement ps = c.prepareStatement(query)) {
			ps.setString(1, token.PetName);
			ps.setString(2, token.Equipment);
			ps.setString(3, token.Name);
			ps.setString(4, token.PetType);
			ps.executeUpdate();
		} catch (SQLException e) {
			org.bukkit.Bukkit.getServer().getLogger().log(java.util.logging.Level.SEVERE, e.getMessage(), e);
		}
	}

	public ClientPetToken LoadClientPets(int accountId) {
		ClientPetToken token = new ClientPetToken();
		token.Pets = new ArrayList<>();
		token.PetNameTagCount = 0;

		if (accountId == -1) return token;

		String query = "SELECT petNameTagCount FROM accounts WHERE id = ?;";
		try (Connection c = DBPool.ACCOUNT.getConnection(); PreparedStatement ps = c.prepareStatement(query)) {
			ps.setInt(1, accountId);
			try (ResultSet rs = ps.executeQuery()) {
				if (rs.next()) {
					token.PetNameTagCount = rs.getInt("petNameTagCount");
				}
			}
		} catch (SQLException e) {
			org.bukkit.Bukkit.getServer().getLogger().log(java.util.logging.Level.SEVERE, e.getMessage(), e);
		}

		String petsQuery = "SELECT petName, petType, equipment FROM accountPets WHERE accountId = ?;";
		try (Connection c = DBPool.ACCOUNT.getConnection(); PreparedStatement ps = c.prepareStatement(petsQuery)) {
			ps.setInt(1, accountId);
			try (ResultSet rs = ps.executeQuery()) {
				while (rs.next()) {
					PetToken p = new PetToken();
					p.PetName = rs.getString("petName");
					p.PetType = rs.getString("petType");
					p.Equipment = rs.getString("equipment");
					token.Pets.add(p);
				}
			}
		} catch (SQLException e) {
			org.bukkit.Bukkit.getServer().getLogger().log(java.util.logging.Level.SEVERE, e.getMessage(), e);
		}

		return token;
	}
}
