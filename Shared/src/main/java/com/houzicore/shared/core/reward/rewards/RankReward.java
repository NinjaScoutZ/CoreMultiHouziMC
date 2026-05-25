package com.houzicore.shared.core.reward.rewards;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import com.houzicore.shared.account.CoreClientManager;
import com.houzicore.shared.common.Rank;
import com.houzicore.shared.core.reward.Reward;
import com.houzicore.shared.core.reward.RewardData;
import com.houzicore.shared.core.reward.RewardRarity;

public class RankReward extends Reward {
	private final CoreClientManager _clientManager;

	public RankReward(CoreClientManager clientManager, int weight, RewardRarity rarity) {
		super(rarity, weight);

		_clientManager = clientManager;
	}

	@Override
	public boolean canGiveReward(Player player) {
		return !_clientManager.Get(player).GetRank().Has(Rank.DIVINE);
	}

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof RankReward)
			return true;

		return false;
	}

	@Override
	public RewardData giveRewardCustom(Player player) {
		Rank rank = null;
		if (_clientManager.Get(player).GetRank() == Rank.ALL) {
			rank = Rank.WARRIOR;
		} else if (_clientManager.Get(player).GetRank() == Rank.WARRIOR) {
			rank = Rank.SOVEREIGN;
		} else if (_clientManager.Get(player).GetRank() == Rank.SOVEREIGN) {
			rank = Rank.DIVINE;
		}

		if (rank == null)
			return new RewardData(getRarity().getColor() + "Rank Upgrade Error", new ItemStack(Material.PAPER));

		_clientManager.Get(player).SetRank(rank);
		_clientManager.getRepository().saveRank(null, player.getName(), player.getUniqueId(), rank, true);

		return new RewardData(getRarity().getColor() + rank.Name + " Rank", new ItemStack(Material.NETHER_STAR));
	}

	@Override
	public RewardData getFakeRewardData(Player player) {
		return new RewardData(getRarity().getColor() + "Rank Upgrade", new ItemStack(Material.NETHER_STAR));
	}
}
