package com.houzicore.shared.core.achievement.ui.page;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import com.houzicore.shared.account.CoreClientManager;
import com.houzicore.shared.core.achievement.Achievement;
import com.houzicore.shared.core.achievement.AchievementCategory;
import com.houzicore.shared.core.achievement.AchievementData;
import com.houzicore.shared.core.achievement.AchievementManager;
import com.houzicore.shared.core.achievement.ui.AchievementMenu;
import com.houzicore.shared.common.util.C;
import com.houzicore.shared.core.donation.DonationManager;
import com.houzicore.shared.core.shop.item.IButton;
import com.houzicore.shared.core.shop.item.ShopItem;
import com.houzicore.shared.core.shop.page.ShopPageBase;
import com.houzicore.shared.core.stats.StatsManager;

public class AchievementPage extends ShopPageBase<AchievementManager, AchievementMenu> {
	private static int ACHIEVEMENT_MIDDLE_INDEX = 31;

	private final AchievementCategory _category;
	private final StatsManager _statsManager;
	private final Player _target;

	public AchievementPage(AchievementManager plugin, StatsManager statsManager, AchievementCategory category,
			AchievementMenu shop, CoreClientManager clientManager, DonationManager donationManager, Player player,
			Player target) {
		super(plugin, shop, clientManager, donationManager, category.getFriendlyName(), player);

		_statsManager = statsManager;
		_category = category;
		_target = target;

		buildPage();
	}

	private void addBackButton() {
		addButton(4, new ShopItem(Material.RED_BED, C.cGray + " \u21FD Go Back", new String[] {}, 1, false), new IButton() {
			@Override
			public void onClick(Player player, ClickType clickType) {
				AchievementMainPage page;
				if (_category.getGameCategory() == AchievementCategory.GameCategory.ARCADE) {
					page = new ArcadeMainPage(getPlugin(), _statsManager, getShop(), getClientManager(),
							getDonationManager(), "Arcade Games", player, _target);
				} else {
					page = new AchievementMainPage(getPlugin(), _statsManager, getShop(), getClientManager(),
							getDonationManager(), _target.getName() + "'s Stats", player, _target);
				}
				;

				getShop().openPageForPlayer(getPlayer(), page);
				player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1, 1);
			}
		});
	}

	private void addStats() {
		// Don't show if this category has no stats to display
		if (_category.getStatsToDisplay().length == 0)
			return;

		final Material material = Material.BOOK;
		final String itemName = C.Bold + _category.getFriendlyName() + " Stats";
		final List<String> lore = new ArrayList<>();
		lore.add(" ");
		_category.addStats(getClientManager(), _statsManager, lore, getPlayer(), _target);

		final ItemStack item = new ItemStack(material);
		final ItemMeta meta = item.getItemMeta();
		meta.setDisplayName(ChatColor.RESET + itemName);
		meta.setLore(lore);
		item.setItemMeta(meta);

		setItem(22, item);
	}

	@Override
	protected void buildPage() {
		int currentIndex = ACHIEVEMENT_MIDDLE_INDEX - getAchievements().size() / 2;
		boolean hasAllAchievements = true;
		int achievementCount = 0;

		final ArrayList<String> masterAchievementLore = new ArrayList<>();
		masterAchievementLore.add(" ");

		final List<Achievement> achievements = getAchievements();
		for (final Achievement achievement : achievements) {
			final AchievementData data = getPlugin().get(_target, achievement);
			final boolean singleLevel = achievement.isSingleLevel();
			final boolean hasUnlocked = data.getLevel() >= achievement.getMaxLevel();

			if (!hasUnlocked) {
				hasAllAchievements = false;
			}

			{
				final Material material = hasUnlocked ? Material.EXPERIENCE_BOTTLE : Material.GLASS_BOTTLE;
				String itemName = (hasUnlocked ? C.cGreen : C.cRed) + achievement.getLangName(getPlayer());

				if (!singleLevel) {
					itemName += ChatColor.WHITE + " Level " + data.getLevel() + "/" + achievement.getMaxLevel();
				}

				final ArrayList<String> lore = new ArrayList<>();
				lore.add(" ");
				for (final String descLine : achievement.getLangDesc(getPlayer())) {
					lore.add(ChatColor.RESET + descLine);
				}

				if (!hasUnlocked && achievement.isOngoing()) {
					lore.add(" ");
					lore.add(C.cYellow + (singleLevel ? "Progress: " : "Next Level: ") + C.cWhite
							+ data.getExpRemainder() + "/" + data.getExpNextLevel());
				}

				if (!hasUnlocked && singleLevel) {
					lore.add(" ");
					lore.add(C.cYellow + "Reward: " + C.cGreen + achievement.getGemReward() + " Gems");
				}

				if (hasUnlocked && data.getLevel() == achievement.getMaxLevel()) {
					lore.add(" ");
					lore.add(C.cAqua + com.houzicore.shared.core.lang.LangManager.get().get(getPlayer(), "achievement.ui.complete", "Completed"));
				}

				addItem(currentIndex, new ShopItem(material, (byte) (hasUnlocked ? 0 : 0), itemName,
						lore.toArray(new String[0]), 1, false, false));
			}

			masterAchievementLore.add((hasUnlocked ? C.cGreen : C.cRed) + achievement.getLangName(getPlayer()));

			currentIndex++;
			achievementCount++;
		}

		// Master Achievement
		if (!_category.getFriendlyName().startsWith("Global") && achievementCount > 0) {
			final String itemName = ChatColor.RESET + _category.getFriendlyName() + " Master Achievement";
			masterAchievementLore.add(" ");
			if (getPlayer().equals(_target)) {
				if (_category.getReward() != null) {
					masterAchievementLore
							.add(C.cYellow + C.Bold + "Reward: " + ChatColor.RESET + _category.getReward());
				} else {
					masterAchievementLore.add(C.cYellow + C.Bold + "Reward: " + ChatColor.RESET + "Coming Soon...");
				}
			}

			addItem(40, new ShopItem(hasAllAchievements ? Material.EMERALD_BLOCK : Material.REDSTONE_BLOCK, (byte) 0,
					itemName, masterAchievementLore.toArray(new String[0]), 1, false, true));
		}

		addBackButton();
		addStats();
	}

	public List<Achievement> getAchievements() {
		final List<Achievement> achievements = new ArrayList<>();

		for (final Achievement achievement : Achievement.values()) {
			if (achievement.getCategory() == _category) {
				achievements.add(achievement);
			}
		}

		return achievements;
	}
}
