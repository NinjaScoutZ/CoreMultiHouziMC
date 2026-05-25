package com.houzicore.shared.core.achievement.ui;

import org.bukkit.entity.Player;

import com.houzicore.shared.account.CoreClientManager;
import com.houzicore.shared.core.achievement.AchievementManager;
import com.houzicore.shared.core.achievement.ui.page.AchievementMainPage;
import com.houzicore.shared.core.donation.DonationManager;
import com.houzicore.shared.core.shop.ShopBase;
import com.houzicore.shared.core.shop.page.ShopPageBase;
import com.houzicore.shared.core.stats.StatsManager;

/**
 * Created by Shaun on 8/21/2014.
 */
public class AchievementMenu extends ShopBase<AchievementManager> {
	private final StatsManager _statsManager;

	public AchievementMenu(AchievementManager plugin, StatsManager statsManager, CoreClientManager clientManager,
			DonationManager donationManager, String name) {
		super(plugin, clientManager, donationManager, name);
		_statsManager = statsManager;
	}

	public boolean attemptShopOpen(Player player, Player target) {
		if (!getOpenedShop().contains(player.getName())) {
			if (!canOpenShop(player))
				return false;

			getOpenedShop().add(player.getName());

			openShopForPlayer(player);
			if (!getPlayerPageMap().containsKey(player.getName())) {
				getPlayerPageMap().put(player.getName(), BuildPagesFor(player, target));
			}

			openPageForPlayer(player, getOpeningPageForPlayer(player));

			return true;
		}

		return false;
	}

	@Override
	protected ShopPageBase<AchievementManager, ? extends ShopBase<AchievementManager>> buildPagesFor(Player player) {
		return BuildPagesFor(player, player);
	}

	protected ShopPageBase<AchievementManager, ? extends ShopBase<AchievementManager>> BuildPagesFor(Player player,
			Player target) {
		return new AchievementMainPage(getPlugin(), _statsManager, this, getClientManager(), getDonationManager(),
				target.getName() + "'s Stats", player, target);
	}
}
