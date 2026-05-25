package com.houzicore.shared.core.achievement.ui.button;

import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;

import com.houzicore.shared.account.CoreClientManager;
import com.houzicore.shared.core.achievement.AchievementManager;
import com.houzicore.shared.core.achievement.ui.AchievementMenu;
import com.houzicore.shared.core.achievement.ui.page.ArcadeMainPage;
import com.houzicore.shared.core.donation.DonationManager;
import com.houzicore.shared.core.shop.item.IButton;
import com.houzicore.shared.core.stats.StatsManager;

public class ArcadeButton implements IButton {
	private final AchievementMenu _shop;
	private final AchievementManager _achievementManager;
	private final StatsManager _statsManager;
	private final DonationManager _donationManager;
	private final CoreClientManager _clientManager;
	private final Player _target;

	public ArcadeButton(AchievementMenu shop, AchievementManager achievementManager, StatsManager statsManager,
			DonationManager donationManager, CoreClientManager clientManager, Player target) {
		_shop = shop;
		_achievementManager = achievementManager;
		_statsManager = statsManager;
		_donationManager = donationManager;
		_clientManager = clientManager;
		_target = target;
	}

	@Override
	public void onClick(Player player, ClickType clickType) {
		_shop.openPageForPlayer(player, new ArcadeMainPage(_achievementManager, _statsManager, _shop, _clientManager,
				_donationManager, "Arcade Games", player, _target));
		player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1, 1);
	}

}
