package com.houzicore.shared.core.donation.repository;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import com.houzicore.shared.common.util.Callback;
import com.houzicore.shared.core.database.DBPool;
import com.houzicore.shared.core.database.DatabaseRunnable;
import com.houzicore.shared.core.database.RepositoryBase;
import com.houzicore.shared.core.database.column.ColumnInt;
import com.houzicore.shared.core.database.column.ColumnVarChar;
import com.houzicore.shared.core.donation.Donor;
import com.houzicore.shared.server.util.TransactionResponse;

public class DonationRepository extends RepositoryBase {
	private static String CREATE_COIN_TRANSACTION_TABLE = "CREATE TABLE IF NOT EXISTS accountCoinTransactions (id INT NOT NULL AUTO_INCREMENT, accountId INT, reason VARCHAR(100), coins INT, PRIMARY KEY (id), FOREIGN KEY (accountId) REFERENCES accounts(id));";
	private static String CREATE_ESSENCE_TRANSACTION_TABLE = "CREATE TABLE IF NOT EXISTS accountEssenceTransactions (id INT NOT NULL AUTO_INCREMENT, accountId INT, reason VARCHAR(100), essence INT, PRIMARY KEY (id), FOREIGN KEY (accountId) REFERENCES accounts(id));";
	private static String UPDATE_ACCOUNT_COINS = "UPDATE accounts SET coins = coins + ? WHERE id = ?;";
	private static String UPDATE_ACCOUNT_COINS_BY_UUID = "UPDATE accounts SET coins = coins + ? WHERE uuid = ?;";
	private static String UPDATE_ACCOUNT_ESSENCE = "UPDATE accounts SET essence = essence + ? WHERE uuid = ?;";
	private static String UPDATE_ACCOUNT_GOLD = "UPDATE accounts SET gold = gold + ? WHERE id = ?;";
	private static String UPDATE_NULL_ACCOUNT_ESSENCE_AND_COINS_ = "UPDATE accounts SET essence = ?, coins = ? WHERE id = ? AND essence IS NULL AND coins IS NULL;";
	private static String INSERT_INVENTORY = "INSERT INTO accountInventory (accountId, itemId, count) VALUES (?, ?, 1) ON DUPLICATE KEY UPDATE count = count + 1;";

	public DonationRepository(JavaPlugin plugin, String webAddress) {
		super(plugin, DBPool.ACCOUNT);
	}

	public void applyKits(String playerName) {
		// No-op: kits loaded from accountInventory at login
	}

	public void essenceReward(final Callback<Boolean> callback, final String giver, String name, final String uuid,
			final int greenEssence) {
		final Callback<Boolean> extraCallback = new Callback<Boolean>() {
			@Override
			public void run(final Boolean response) {
				Bukkit.getServer().getScheduler().runTask(Plugin, new Runnable() {
					@Override
					public void run() {
						callback.run(response);
					}
				});
			}
		};

		handleDatabaseCall(new DatabaseRunnable(new Runnable() {
			@Override
			public void run() {
				int rows = executeUpdate(UPDATE_ACCOUNT_ESSENCE, new ColumnInt("essence", greenEssence),
						new ColumnVarChar("uuid", 100, uuid));
				extraCallback.run(rows > 0);
			}
		}), "Error updating player essence amount in DonationRepository : ");
	}

	protected void initialize() {
		executeUpdate(CREATE_COIN_TRANSACTION_TABLE);
		executeUpdate(CREATE_ESSENCE_TRANSACTION_TABLE);
	}

	public void PurchaseKnownSalesPackage(final Callback<TransactionResponse> callback, String name, final String uuid,
			final int cost, final int salesPackageId) {
		final Callback<TransactionResponse> extraCallback = new Callback<TransactionResponse>() {
			@Override
			public void run(final TransactionResponse response) {
				Bukkit.getServer().getScheduler().runTask(Plugin, new Runnable() {
					@Override
					public void run() {
						callback.run(response);
					}
				});
			}
		};

		handleDatabaseCall(new DatabaseRunnable(new Runnable() {
			@Override
			public void run() {
				int rows = executeUpdate(UPDATE_ACCOUNT_COINS_BY_UUID, new ColumnInt("coins", -cost),
						new ColumnVarChar("uuid", 100, uuid));
				if (rows > 0) {
					extraCallback.run(TransactionResponse.Success);
				} else {
					extraCallback.run(TransactionResponse.Failed);
				}
			}
		}), "Error purchasing known sales package in DonationRepository : ");
	}

	public void PurchaseUnknownSalesPackage(final Callback<TransactionResponse> callback, final String name,
			final int accountId, final String packageName, final boolean coinPurchase, final int cost) {
		final Callback<TransactionResponse> extraCallback = new Callback<TransactionResponse>() {
			@Override
			public void run(final TransactionResponse response) {
				Bukkit.getServer().getScheduler().runTask(Plugin, new Runnable() {
					@Override
					public void run() {
						callback.run(response);
					}
				});
			}
		};

		handleDatabaseCall(new DatabaseRunnable(new Runnable() {
			@Override
			public void run() {
				int rows;
				if (coinPurchase) {
					rows = executeUpdate(UPDATE_ACCOUNT_COINS, new ColumnInt("coins", -cost),
							new ColumnInt("id", accountId));
				} else {
					// Essence purchase — not common, but handle it
					rows = executeUpdate("UPDATE accounts SET essence = essence - ? WHERE id = ? AND essence >= ?;",
							new ColumnInt("essence", cost), new ColumnInt("id", accountId),
							new ColumnInt("essenceCheck", cost));
				}
				if (rows > 0) {
					extraCallback.run(TransactionResponse.Success);
				} else {
					extraCallback.run(TransactionResponse.InsufficientFunds);
				}
			}
		}), "Error purchasing unknown sales package in DonationRepository : ");
	}

	public Donor retrieveDonorInfo(ResultSet resultSet) throws SQLException {
		final Donor donor = new Donor();

		while (resultSet.next()) {
			donor.AddEssence(resultSet.getInt(1));
			donor.addCoins(resultSet.getInt(2));
			donor.addGold(resultSet.getInt(3));
		}

		return donor;
	}

	public void rewardCoins(final Callback<Boolean> callback, final String giver, String name, final int accountId,
			final int coins) {
		final Callback<Boolean> extraCallback = new Callback<Boolean>() {
			@Override
			public void run(final Boolean response) {
				Bukkit.getServer().getScheduler().runTask(Plugin, new Runnable() {
					@Override
					public void run() {
						callback.run(response);
					}
				});
			}
		};

		handleDatabaseCall(new DatabaseRunnable(new Runnable() {
			@Override
			public void run() {
				int rows = executeUpdate(UPDATE_ACCOUNT_COINS, new ColumnInt("coins", coins),
						new ColumnInt("id", accountId));
				extraCallback.run(rows > 0);
			}
		}), "Error updating player coin amount in DonationRepository : ");
	}

	public void rewardGold(final Callback<Boolean> callback, final String giver, final String name, final int accountId,
			final int gold) {
		handleDatabaseCall(new DatabaseRunnable(new Runnable() {
			@Override
			public void run() {
				if (executeUpdate(UPDATE_ACCOUNT_GOLD, new ColumnInt("gold", gold),
						new ColumnInt("id", accountId)) < 1) {
					callback.run(false);
				} else {
					callback.run(true);
				}
			}
		}), "Error updating player gold amount in DonationRepository : ");
	}

	@Override
	protected void update() {
	}

	public void updateEssenceAndCoins(final int accountId, final int essence, final int coins) {
		handleDatabaseCall(new DatabaseRunnable(new Runnable() {
			@Override
			public void run() {
				executeUpdate(UPDATE_NULL_ACCOUNT_ESSENCE_AND_COINS_, new ColumnInt("essence", essence),
						new ColumnInt("coins", coins), new ColumnInt("id", accountId));
			}
		}), "Error updating player's null essence and coins DonationRepository : ");
	}
}
