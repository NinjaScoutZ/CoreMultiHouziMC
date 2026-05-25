package com.houzicore.shared.core.achievement.ui.page;

import java.util.ArrayList;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;

import com.houzicore.shared.account.CoreClientManager;
import com.houzicore.shared.core.achievement.AchievementCategory;
import com.houzicore.shared.core.achievement.AchievementManager;
import com.houzicore.shared.core.achievement.ui.AchievementMenu;
import com.houzicore.shared.core.achievement.ui.button.CategoryButton;
import com.houzicore.shared.common.util.C;
import com.houzicore.shared.core.donation.DonationManager;
import com.houzicore.shared.core.shop.item.IButton;
import com.houzicore.shared.core.shop.item.ShopItem;
import com.houzicore.shared.core.stats.StatsManager;

public class ArcadeMainPage extends AchievementMainPage {
	public ArcadeMainPage(AchievementManager plugin, StatsManager statsManager, AchievementMenu shop,
			CoreClientManager clientManager, DonationManager donationManager, String name, Player player,
			Player target) {
		super(plugin, statsManager, shop, clientManager, donationManager, name, player, target);
	}

	private void addBackButton() {
		addButton(4, new ShopItem(Material.RED_BED, C.cGray + " \u21FD Go Back", new String[] {}, 1, false), new IButton() {
			@Override
			public void onClick(Player player, ClickType clickType) {
				getShop().openPageForPlayer(getPlayer(), new AchievementMainPage(getPlugin(), _statsManager, getShop(),
						getClientManager(), getDonationManager(), _target.getName() + "'s Stats", player, _target));
				player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1, 1);
			}
		});
	}

	@Override
	protected void buildPage() {
		int slot = 9;

		for (final AchievementCategory category : AchievementCategory.values()) {
			if (category.getGameCategory() != AchievementCategory.GameCategory.ARCADE) {
				continue;
			}

			final CategoryButton button = new CategoryButton(getShop(), getPlugin(), _statsManager, category,
					getDonationManager(), getClientManager(), _target);

			final ArrayList<String> lore = new ArrayList<>();
			lore.add(" ");
			category.addStats(getClientManager(), _statsManager, lore, 2, getPlayer(), _target);
			lore.add(" ");
			addAchievements(category, lore, 9);
			lore.add(" ");
			lore.add(ChatColor.RESET + "Click for more details!");

			final ShopItem shopItem = new ShopItem(category.getIcon(), category.getIconData(),
					C.Bold + category.getFriendlyName(), lore.toArray(new String[0]), 1, false, false);
			addButton(slot, shopItem, button);

			slot += (slot + 1) % 9 == 0 ? 1 : 2;
		}

		addBackButton();
	}

}
