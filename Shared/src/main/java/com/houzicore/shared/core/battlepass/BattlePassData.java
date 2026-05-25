package com.houzicore.shared.core.battlepass;

import java.util.HashSet;
import java.util.Set;

public class BattlePassData {
	private int _xp = 0;
	private final Set<Integer> _claimedTiers = new HashSet<>();

	public BattlePassData() {
	}

	public int getXp() {
		return _xp;
	}

	public void setXp(int xp) {
		_xp = xp;
	}

	public void addXp(int amount) {
		_xp += amount;
	}

	public Set<Integer> getClaimedTiers() {
		return _claimedTiers;
	}

	public boolean hasClaimed(int tier) {
		return _claimedTiers.contains(tier);
	}
	
	public boolean hasClaimedPremium(int tier) {
		return _claimedTiers.contains(tier + 100);
	}

	public void claimTier(int tier) {
		_claimedTiers.add(tier);
	}

	public void claimPremiumTier(int tier) {
		_claimedTiers.add(tier + 100);
	}

	public String getClaimedTiersString() {
		StringBuilder sb = new StringBuilder();
		for (Integer tier : _claimedTiers) {
			if (sb.length() > 0) sb.append(",");
			sb.append(tier);
		}
		return sb.toString();
	}

	public void loadClaimedTiersFromString(String str) {
		_claimedTiers.clear();
		if (str == null || str.isEmpty()) return;
		for (String tierStr : str.split(",")) {
			try {
				_claimedTiers.add(Integer.parseInt(tierStr));
			} catch (NumberFormatException ignored) {}
		}
	}
}
