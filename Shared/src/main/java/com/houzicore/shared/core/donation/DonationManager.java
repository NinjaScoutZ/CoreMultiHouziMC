package com.houzicore.shared.core.donation;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;

import org.bukkit.Bukkit;
import com.google.gson.Gson;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.plugin.java.JavaPlugin;

import com.houzicore.shared.api.IEconomyService;
import com.houzicore.shared.core.database.BatchQueryProcessor;

import com.houzicore.shared.MiniDbClientPlugin;
import com.houzicore.shared.account.CoreClientManager;
import com.houzicore.shared.account.event.ClientWebResponseEvent;
import com.houzicore.shared.common.CurrencyType;
import com.houzicore.shared.common.util.Callback;
import com.houzicore.shared.common.util.NautHashMap;
import com.houzicore.shared.core.donation.command.CoinCommand;
import com.houzicore.shared.core.donation.command.CurrencyUpdateCommand;
import com.houzicore.shared.core.donation.command.EssenceCommand;
import com.houzicore.shared.core.donation.command.GoldCommand;
import com.houzicore.shared.core.donation.repository.DonationRepository;
import com.houzicore.shared.core.donation.repository.token.DonorTokenWrapper;
import com.houzicore.shared.server.util.TransactionResponse;
import com.houzicore.shared.serverdata.commands.ServerCommand;
import com.houzicore.shared.serverdata.commands.CommandCallback;
import com.houzicore.shared.serverdata.commands.ServerCommandManager;
import com.houzicore.shared.updater.UpdateType;
import com.houzicore.shared.updater.event.UpdateEvent;

public class DonationManager extends MiniDbClientPlugin<Donor> implements CommandCallback, IEconomyService {
	private final DonationRepository _repository;

	private final NautHashMap<Player, NautHashMap<String, Integer>> _essenceQueue = new NautHashMap<>();
	private final NautHashMap<Player, NautHashMap<String, Integer>> _coinQueue = new NautHashMap<>();
	private final NautHashMap<Player, NautHashMap<String, Integer>> _goldQueue = new NautHashMap<>();

	public DonationManager(JavaPlugin plugin, CoreClientManager clientManager, String webAddress) {
		super("Donation", plugin, clientManager);

		if (BatchQueryProcessor.getInstance() == null) {
			new BatchQueryProcessor(plugin);
		}

		_repository = new DonationRepository(plugin, webAddress);
	}

	@Override
	public void addCommands() {
		addCommand(new EssenceCommand(this));
		addCommand(new CoinCommand(this));
		addCommand(new GoldCommand(this));

		ServerCommandManager.getInstance().registerCommandType("CurrencyUpdateCommand", CurrencyUpdateCommand.class, this);
	}

	@Override
	protected Donor AddPlayer(String player) {
		return new Donor();
	}

	public void applyKits(String playerName) {
		_repository.applyKits(playerName);
	}

	@Override
	public String getQuery(int accountId, String uuid, String name) {
		return "SELECT essence, coins, gold FROM accounts WHERE id = '" + accountId + "';";
	}

	private void LoadDonor(DonorTokenWrapper token, UUID uuid) {
		Get(token.Name).loadToken(token.DonorToken);
		// _repository.updateEssenceAndCoins(uuid, Get(token.Name).GetEssence(),
		// Get(token.Name).getCoins());
	}

	@EventHandler
	public void OnClientWebResponse(ClientWebResponseEvent event) {
		final DonorTokenWrapper token = new Gson().fromJson(event.GetResponse(), DonorTokenWrapper.class);
		LoadDonor(token, event.getUniqueId());
	}

	@Override
	public void processLoginResultSet(String playerName, int accountId, ResultSet resultSet) throws SQLException {
		Donor donor = _repository.retrieveDonorInfo(resultSet);
		Get(playerName).AddEssence(donor.GetEssence());
		Get(playerName).addCoins(donor.getCoins());
		Get(playerName).addGold(donor.getGold());
	}

	public void PurchaseKnownSalesPackage(final Callback<TransactionResponse> callback, final String name,
			final UUID uuid, final int cost, final int salesPackageId) {
		_repository.PurchaseKnownSalesPackage(new Callback<TransactionResponse>() {
			@Override
			public void run(TransactionResponse response) {
				if (response == TransactionResponse.Success) {
					final Donor donor = Get(name);

					if (donor != null) {
						donor.AddSalesPackagesOwned(salesPackageId);
					}
				}

				if (callback != null) {
					callback.run(response);
				}
			}
		}, name, uuid.toString(), cost, salesPackageId);
	}

	public void PurchaseUnknownSalesPackage(final Callback<TransactionResponse> callback, final String name,
			final int accountId, final String packageName, final boolean coinPurchase, final int cost,
			boolean oneTimePurchase) {
		final Donor donor = Bukkit.getPlayerExact(name) != null ? Get(name) : null;

		if (donor != null) {
			if (oneTimePurchase && donor.OwnsUnknownPackage(packageName)) {
				if (callback != null) {
					callback.run(TransactionResponse.AlreadyOwns);
				}

				return;
			}
		}

		_repository.PurchaseUnknownSalesPackage(new Callback<TransactionResponse>() {
			@Override
			public void run(TransactionResponse response) {
				if (response == TransactionResponse.Success) {
					if (donor != null) {
						donor.AddUnknownSalesPackagesOwned(packageName);
						donor.DeductCost(cost, coinPurchase ? CurrencyType.Coins : CurrencyType.Essence);
					}
				}

				if (callback != null) {
					callback.run(response);
				}
			}
		}, name, accountId, packageName, coinPurchase, cost);
	}

	public void RewardCoins(Callback<Boolean> callback, String caller, String name, int accountId, int amount) {
		RewardCoins(callback, caller, name, accountId, amount, true);
	}

	public void RewardCoins(final Callback<Boolean> callback, final String caller, final String name,
			final int accountId, final int amount, final boolean updateTotal) {
		_repository.rewardCoins(new Callback<Boolean>() {
			@Override
			public void run(Boolean success) {
				if (success) {
					if (updateTotal) {
						final Donor donor = Get(name);

						if (donor != null) {
							donor.addCoins(amount);
						}

						new CurrencyUpdateCommand(name, 0, amount).publish();
					}
					
					// Transaction Logging
					BatchQueryProcessor.getInstance().addQueryToQueue(
						String.format("INSERT INTO accountCoinTransactions (accountId, reason, coins) VALUES (%d, '%s', %d)",
							accountId, caller.replace("'", "''"), amount)
					);
				}

				if (callback != null) {
					callback.run(success);
				}
			}
		}, caller, name, accountId, amount);
	}

	public void RewardCoinsLater(final String caller, final Player player, final int amount) {
		if (!_coinQueue.containsKey(player)) {
			_coinQueue.put(player, new NautHashMap<String, Integer>());
		}

		int totalAmount = amount;

		if (_coinQueue.get(player).containsKey(caller)) {
			totalAmount += _coinQueue.get(player).get(caller);
		}

		_coinQueue.get(player).put(caller, totalAmount);

		// Do Temp Change
		final Donor donor = Get(player.getName());

		if (donor != null) {
			donor.addCoins(amount);
		}
	}

	public void RewardEssence(Callback<Boolean> callback, String caller, String name, UUID uuid, int amount) {
		RewardEssence(callback, caller, name, uuid, amount, true);
	}

	public void RewardEssence(final Callback<Boolean> callback, final String caller, final String name, final UUID uuid,
			final int amount, final boolean updateTotal) {
		_repository.essenceReward(new Callback<Boolean>() {
			@Override
			public void run(Boolean success) {
				if (success) {
					if (updateTotal) {
						final Donor donor = Get(name);

						if (donor != null) {
							donor.AddEssence(amount);
						}

						new CurrencyUpdateCommand(name, amount, 0).publish();
					}
					
					// Transaction Logging
					int aid = -1;
					Player p = Bukkit.getPlayerExact(name);
					if (p != null) {
						aid = ClientManager.Get(p).getAccountId();
					} else {
						aid = ClientManager.getCachedClientAccountId(uuid);
					}
					if (aid > 0) {
						BatchQueryProcessor.getInstance().addQueryToQueue(
							String.format("INSERT INTO accountEssenceTransactions (accountId, reason, essence) VALUES (%d, '%s', %d)",
								aid, caller.replace("'", "''"), amount)
						);
					}
				}

				if (callback != null) {
					callback.run(success);
				}
			}
		}, caller, name, uuid != null ? uuid.toString() : null, amount);
	}

	public void RewardEssenceLater(final String caller, final Player player, final int amount) {
		if (!_essenceQueue.containsKey(player)) {
			_essenceQueue.put(player, new NautHashMap<String, Integer>());
		}

		int totalAmount = amount;

		if (_essenceQueue.get(player).containsKey(caller)) {
			totalAmount += _essenceQueue.get(player).get(caller);
		}

		_essenceQueue.get(player).put(caller, totalAmount);

		// Do Temp Change
		final Donor donor = Get(player.getName());

		if (donor != null) {
			donor.AddEssence(amount);
		}
	}

	public void RewardGold(Callback<Boolean> callback, String caller, String name, int accountId, int amount) {
		RewardGold(callback, caller, name, accountId, amount, true);
	}

	public void RewardGold(final Callback<Boolean> callback, final String caller, final String name,
			final int accountId, final int amount, final boolean updateTotal) {
		_repository.rewardGold(new Callback<Boolean>() {
			@Override
			public void run(Boolean success) {
				if (success) {
					if (updateTotal) {
						final Donor donor = Get(name);

						if (donor != null) {
							donor.addGold(amount);
						}
					}
				} else {
				}

				if (callback != null) {
					callback.run(true);
				}
			}
		}, caller, name, accountId, amount);
	}

	public void RewardGoldLater(final String caller, final Player player, final int amount) {
		if (!_goldQueue.containsKey(player)) {
			_goldQueue.put(player, new NautHashMap<String, Integer>());
		}

		int totalAmount = amount;

		if (_goldQueue.get(player).containsKey(caller)) {
			totalAmount += _goldQueue.get(player).get(caller);
		}

		_goldQueue.get(player).put(caller, totalAmount);

		// Do Temp Change
		final Donor donor = Get(player.getName());

		if (donor != null) {
			donor.addGold(amount);
		}
	}

	@Override
	public void run(ServerCommand command) {
		if (command instanceof CurrencyUpdateCommand) {
			CurrencyUpdateCommand update = (CurrencyUpdateCommand) command;
			if (update.getServerPort() == Bukkit.getPort()) {
				// Ignore commands published by this exact server instance
				// because they were already applied locally before publishing
				return;
			}

			Donor donor = Get(update.getPlayerName());
			if (donor != null) {
				if (update.getGemsAdded() > 0) donor.AddEssence(update.getGemsAdded());
				if (update.getCoinsAdded() > 0) donor.addCoins(update.getCoinsAdded());
			}
		}
	}

	@EventHandler
	public void UpdateCoinQueue(UpdateEvent event) {
		if (event.getType() != UpdateType.SLOWER)
			return;

		for (final Player player : _coinQueue.keySet()) {
			String tempCaller = null;
			int tempTotal = 0;

			for (final String curCaller : _coinQueue.get(player).keySet()) {
				tempCaller = curCaller;
				tempTotal += _coinQueue.get(player).get(curCaller);
			}

			final int total = tempTotal;
			final String caller = tempCaller;

			if (caller == null) {
				continue;
			}

			if (player.isOnline() && player.isValid()) {
				RewardCoins(null, caller, player.getName(), ClientManager.Get(player).getAccountId(), total, false);
			} else {
				Bukkit.getServer().getScheduler().runTaskAsynchronously(getPlugin(), new Runnable() {
					@Override
					public void run() {
						RewardCoins(null, caller, player.getName(),
								ClientManager.getCachedClientAccountId(player.getUniqueId()), total, false);
					}
				});
			}


			// Clean
			_coinQueue.get(player).clear();
		}

		// Clean
		_coinQueue.clear();
	}

	@EventHandler
	public void UpdateEssenceQueue(UpdateEvent event) {
		if (event.getType() != UpdateType.SLOWER)
			return;

		for (final Player player : _essenceQueue.keySet()) {
			String caller = null;
			int total = 0;

			for (final String curCaller : _essenceQueue.get(player).keySet()) {
				caller = curCaller;
				total += _essenceQueue.get(player).get(curCaller);
			}

			if (caller == null) {
				continue;
			}

			// Actually Add Essence
			RewardEssence(null, caller, player.getName(), player.getUniqueId(), total, false);


			// Clean
			_essenceQueue.get(player).clear();
		}

		// Clean
		_essenceQueue.clear();
	}

	@EventHandler
	public void UpdateGoldQueue(UpdateEvent event) {
		if (event.getType() != UpdateType.SLOWER)
			return;

		for (final Player player : _goldQueue.keySet()) {
			String caller = null;
			int total = 0;

			for (final String curCaller : _goldQueue.get(player).keySet()) {
				caller = curCaller;
				total += _goldQueue.get(player).get(curCaller);
			}

			if (caller == null) {
				continue;
			}

			// Actually Add Gold
			RewardGold(null, caller, player.getName(), ClientManager.Get(player).getAccountId(), total, false);


			// Clean
			_goldQueue.get(player).clear();
		}

		// Clean
		_goldQueue.clear();
	}

	@Override
	public int getCoins(Player player) {
		Donor donor = Get(player);
		return donor != null ? donor.getCoins() : 0;
	}

	@Override
	public int getEssence(Player player) {
		Donor donor = Get(player);
		return donor != null ? donor.GetEssence() : 0;
	}

	@Override
	public void addCoins(Player player, String reason, int amount) {
		RewardCoins(null, reason, player.getName(), ClientManager.Get(player).getAccountId(), amount);
	}

	@Override
	public void addEssence(Player player, String reason, int amount) {
		RewardEssence(null, reason, player.getName(), player.getUniqueId(), amount);
	}

	@Override
	public boolean hasCoins(Player player, int amount) {
		return getCoins(player) >= amount;
	}
}
