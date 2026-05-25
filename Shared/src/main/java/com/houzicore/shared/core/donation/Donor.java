package com.houzicore.shared.core.donation;

import java.util.ArrayList;
import java.util.List;

import com.houzicore.shared.common.CurrencyType;
import com.houzicore.shared.core.donation.repository.token.CoinTransactionToken;
import com.houzicore.shared.core.donation.repository.token.DonorToken;
import com.houzicore.shared.core.donation.repository.token.TransactionToken;

public class Donor {
	private int _essence;
	private int _coins;
	private int _gold;
	private boolean _donated;
	private List<Integer> _salesPackagesOwned = new ArrayList<>();
	private List<String> _unknownSalesPackagesOwned = new ArrayList<>();
	private List<TransactionToken> _transactions = new ArrayList<>();
	private List<CoinTransactionToken> _coinTransactions = new ArrayList<>();

	private boolean _update = true;

	public Donor() {
	}

	public void addCoins(int amount) {
		_coins += amount;
	}

	public void AddEssence(int essence) {
		_essence += essence;
	}

	public void addGold(int amount) {
		_gold += amount;
	}

	public void AddSalesPackagesOwned(int salesPackageId) {
		_salesPackagesOwned.add(salesPackageId);
	}

	public void AddUnknownSalesPackagesOwned(String packageName) {
		_unknownSalesPackagesOwned.add(packageName);
	}

	public void DeductCost(int cost, CurrencyType currencyType) {
		switch (currencyType) {
		case Essence:
			_essence -= cost;
			_update = true;
			break;
		case Coins:
			_coins -= cost;
			_update = true;
			break;
		default:
			break;
		}
	}

	public int GetBalance(CurrencyType currencyType) {
		switch (currencyType) {
		case Essence:
			return _essence;
		case Coins:
			return _coins;
		case Tokens:
			return 0;
		default:
			return 0;
		}
	}

	public int getCoins() {
		return _coins;
	}

	public List<CoinTransactionToken> getCoinTransactions() {
		return _coinTransactions;
	}

	public int GetEssence() {
		return _essence;
	}

	public int getGold() {
		return _gold;
	}

	public List<Integer> GetSalesPackagesOwned() {
		return _salesPackagesOwned;
	}

	public List<TransactionToken> getTransactions() {
		return _transactions;
	}

	public List<String> GetUnknownSalesPackagesOwned() {
		return _unknownSalesPackagesOwned;
	}

	public boolean HasDonated() {
		return _donated;
	}

	public void loadToken(DonorToken token) {
		_essence = token.Gems;
		_coins = token.Coins;
		_donated = token.Donated;

		_salesPackagesOwned = token.SalesPackages;
		_unknownSalesPackagesOwned = token.UnknownSalesPackages;
		_transactions = token.Transactions;
		_coinTransactions = token.CoinRewards;
	}

	public boolean Owns(Integer salesPackageId) {
		return salesPackageId == -1 || _salesPackagesOwned.contains(salesPackageId);
	}

	public boolean OwnsUltraPackage() {
		for (final String packageName : _unknownSalesPackagesOwned) {
			if (packageName.contains("ULTRA"))
				return true;
		}

		return false;
	}

	public boolean OwnsUnknownPackage(String packageName) {
		return _unknownSalesPackagesOwned.contains(packageName);
	}

	public boolean Updated() {
		return _update;
	}
}
