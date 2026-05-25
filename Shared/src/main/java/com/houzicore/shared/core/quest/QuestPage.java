package com.houzicore.shared.core.quest;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;

import com.houzicore.shared.account.CoreClientManager;
import com.houzicore.shared.common.util.C;
import com.houzicore.shared.core.donation.DonationManager;
import com.houzicore.shared.core.itemstack.ItemBuilder;
import com.houzicore.shared.core.shop.item.IButton;
import com.houzicore.shared.core.shop.page.ShopPageBase;

public class QuestPage extends ShopPageBase<QuestManager, QuestShop> {

	public QuestPage(QuestManager plugin, QuestShop shop, CoreClientManager clientManager, DonationManager donationManager, Player player) {
		super(plugin, shop, clientManager, donationManager, "Daily & Weekly Quests", player, 45);
		buildPage();
	}

	@Override
	protected void buildPage() {
		QuestData data = getPlugin().Get(getPlayer().getName());
		if (data == null) return;

		// Split quests by daily and weekly
		List<Quest> dailyQuests = new ArrayList<>();
		List<Quest> weeklyQuests = new ArrayList<>();

		for (Quest q : Quest.values()) {
			if (q.getType() == QuestType.DAILY) dailyQuests.add(q);
			else if (q.getType() == QuestType.WEEKLY) weeklyQuests.add(q);
		}

		// Row 1: Daily Quests (Slots 11, 13, 15)
		int[] dailySlots = {11, 13, 15};
		for (int i = 0; i < dailyQuests.size() && i < dailySlots.length; i++) {
			addQuestButton(dailySlots[i], dailyQuests.get(i), data, Material.GOLD_BLOCK);
		}

		// Row 3: Weekly Quests (Slots 29, 31, 33)
		int[] weeklySlots = {29, 31, 33};
		for (int i = 0; i < weeklyQuests.size() && i < weeklySlots.length; i++) {
			addQuestButton(weeklySlots[i], weeklyQuests.get(i), data, Material.DIAMOND_BLOCK);
		}
	}

	private void addQuestButton(int slot, Quest quest, QuestData data, Material incompleteMat) {
		PlayerQuest pq = data.getOrCreateQuest(quest.getId());
		
		String timeRemaining = "Time Remaining: " + C.cYellow + getFormattedTimeLeft(quest.getType(), pq.getPeriodId());
		
		ItemBuilder builder;
		if (pq.isCompleted()) {
			builder = new ItemBuilder(Material.EMERALD_BLOCK)
				.setTitle(C.cGreen + C.Bold + quest.getName() + " (Completed)")
				.addLore(C.cGray + quest.getDescription())
				.addLore("")
				.addLore(C.cGray + "Reward: " + C.cGreen + "Claimed (" + quest.getRewardEssence() + " Essence)")
				.addLore(C.cGray + "Progress: " + C.cGreen + pq.getProgress() + "/" + quest.getMaxProgress())
				.addLore("")
				.addLore(timeRemaining);
			
			// We can add glow to completed quests
			ItemStack is = builder.build();
			try { com.houzicore.shared.common.util.UtilInv.addDullEnchantment(is); } catch (Exception ignored){}
			
			addButton(slot, is, new IButton() {
				@Override
				public void onClick(Player player, ClickType clickType) {}
			});
		} else {
			builder = new ItemBuilder(incompleteMat)
				.setTitle(C.cGold + C.Bold + quest.getName())
				.addLore(C.cGray + quest.getDescription())
				.addLore("")
				.addLore(C.cGray + "Reward: " + C.cAqua + quest.getRewardEssence() + " Essence")
				.addLore(C.cGray + "Progress: " + C.cYellow + pq.getProgress() + "/" + quest.getMaxProgress() + buildProgressBar(pq.getProgress(), quest.getMaxProgress()))
				.addLore("")
				.addLore(timeRemaining);

			addButton(slot, builder.build(), new IButton() {
				@Override
				public void onClick(Player player, ClickType clickType) {}
			});
		}
	}

	private String buildProgressBar(int current, int max) {
		int totalBars = 10;
		float percent = (float) current / (float) max;
		int filled = Math.min(totalBars, Math.round(percent * totalBars));
		int empty = totalBars - filled;

		StringBuilder bar = new StringBuilder(" " + C.cGreen);
		for (int i = 0; i < filled; i++) bar.append("⬛");
		bar.append(C.cGray);
		for (int i = 0; i < empty; i++) bar.append("⬛");

		return bar.toString();
	}

	private String getFormattedTimeLeft(QuestType type, long periodId) {
		long expirationTime = type.getPeriodExpiration(periodId);
		long timeLeft = expirationTime - System.currentTimeMillis();
		
		if (timeLeft <= 0) return "Expired";
		
		long days = timeLeft / 86400000L;
		long hours = (timeLeft % 86400000L) / 3600000L;
		long minutes = (timeLeft % 3600000L) / 60000L;
		
		if (days > 0) return days + "d " + hours + "h";
		if (hours > 0) return hours + "h " + minutes + "m";
		return minutes + "m";
	}
}
