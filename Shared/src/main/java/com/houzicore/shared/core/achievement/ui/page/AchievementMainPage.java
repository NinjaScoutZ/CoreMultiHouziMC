package com.houzicore.shared.core.achievement.ui.page;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;

import com.houzicore.shared.account.CoreClientManager;
import com.houzicore.shared.core.achievement.Achievement;
import com.houzicore.shared.core.achievement.AchievementCategory;
import com.houzicore.shared.core.achievement.AchievementData;
import com.houzicore.shared.core.achievement.AchievementManager;
import com.houzicore.shared.core.achievement.ui.AchievementMenu;
import com.houzicore.shared.core.achievement.ui.button.ArcadeButton;
import com.houzicore.shared.core.achievement.ui.button.CategoryButton;
import com.houzicore.shared.common.util.C;
import com.houzicore.shared.core.donation.DonationManager;
import com.houzicore.shared.core.itemstack.ItemLayout;
import com.houzicore.shared.core.shop.item.ShopItem;
import com.houzicore.shared.core.shop.page.ShopPageBase;
import com.houzicore.shared.core.stats.StatsManager;

public class AchievementMainPage extends ShopPageBase<AchievementManager, AchievementMenu> {
	protected Player _target;
	protected StatsManager _statsManager;

	public AchievementMainPage(AchievementManager plugin, StatsManager statsManager, AchievementMenu shop,
			CoreClientManager clientManager, DonationManager donationManager, String name, Player player,
			Player target) {
		super(plugin, shop, clientManager, donationManager, name, player, 9 * 4);

		_target = target;
		_statsManager = statsManager;

		buildPage();
	}

	protected void addAchievements(AchievementCategory category, List<String> lore, int max) {
		int achievementCount = 0;
		for (int i = 0; i < Achievement.values().length && achievementCount < max; i++) {
			final Achievement achievement = Achievement.values()[i];
			if (achievement.getCategory() == category) {
				// Don't display achievements that have multiple levels
				if (achievement.getMaxLevel() > 1) {
					continue;
				}

				final AchievementData data = getPlugin().get(_target, achievement);
				final boolean finished = data.getLevel() >= achievement.getMaxLevel();

				lore.add((finished ? C.cGreen : C.cRed) + achievement.getLangName(getPlayer()));

				achievementCount++;
			}
		}
	}

	protected void addArcadeButton(int slot) {
		final ArcadeButton button = new ArcadeButton(getShop(), getPlugin(), _statsManager, getDonationManager(),
				getClientManager(), _target);
		final ShopItem shopItem = new ShopItem(Material.BOW, (byte) 0, C.Bold + "Arcade Games",
				new String[] { " ", ChatColor.RESET + "Click for more!" }, 1, false, false);

		addButton(slot, shopItem, button);
	}

	@Override
	protected void buildPage() {
		final ArrayList<Integer> pageLayout = new ItemLayout("XXXXOXXXX", "OXOXOXOXO", "OXOXOXOXO", "XXOXOXOXX")
				.getItemSlots();
		int listSlot = 0;

		for (final AchievementCategory category : AchievementCategory.values()) {
			if (category.getGameCategory() == AchievementCategory.GameCategory.ARCADE) {
				continue;
			}

			final CategoryButton button = new CategoryButton(getShop(), getPlugin(), _statsManager, category,
					getDonationManager(), getClientManager(), _target);

			final ArrayList<String> lore = new ArrayList<>();
			lore.add(" ");
			category.addStats(getClientManager(), _statsManager, lore, category == AchievementCategory.GLOBAL ? 5 : 2,
					getPlayer(), _target);
			lore.add(" ");
			addAchievements(category, lore, 9);
			lore.add(" ");
			lore.add(ChatColor.RESET + "Click for more details!");

			final ShopItem shopItem = new ShopItem(category.getIcon(), category.getIconData(),
					C.Bold + category.getFriendlyName(), lore.toArray(new String[0]), 1, false, false);
			addButton(pageLayout.get(listSlot++), shopItem, button);
		}

		addArcadeButton(pageLayout.get(listSlot++));
	}
}
