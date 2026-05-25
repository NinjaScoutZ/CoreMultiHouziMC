package com.houzicore.shared.core.battlepass;

import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.event.inventory.ClickType;

import com.houzicore.shared.account.CoreClientManager;
import com.houzicore.shared.common.util.C;
import com.houzicore.shared.common.util.F;
import com.houzicore.shared.common.util.UtilPlayer;
import com.houzicore.shared.core.donation.DonationManager;
import com.houzicore.shared.core.itemstack.ItemBuilder;
import com.houzicore.shared.core.shop.item.IButton;
import com.houzicore.shared.core.shop.page.ShopPageBase;

public class BattlePassPage extends ShopPageBase<BattlePassManager, BattlePassShop> {

	private final int _page;

	public BattlePassPage(BattlePassManager plugin, BattlePassShop shop, CoreClientManager clientManager, DonationManager donationManager, Player player, int page) {
		super(plugin, shop, clientManager, donationManager, "Battle Pass - " + plugin.getCurrentSeason(), player, 54);
		_page = page;
		buildPage();
	}

	public BattlePassPage(BattlePassManager plugin, BattlePassShop shop, CoreClientManager clientManager, DonationManager donationManager, Player player) {
		this(plugin, shop, clientManager, donationManager, player, 0);
	}

	@Override
	protected void buildPage() {
		BattlePassData data = getPlugin().Get(getPlayer().getName());
		if (data == null) return;
		
		int xp = data.getXp();
		boolean ownsPremium = getDonationManager().Get(getPlayer()).OwnsUnknownPackage("Battle Pass Premium");

		// Info item
		ItemStack infoItem = new ItemBuilder(Material.BOOK)
				.setTitle(C.cGold + C.Bold + "Battle Pass Progress")
				.addLore(C.cGray + "Season: " + C.cYellow + getPlugin().getCurrentSeason())
				.addLore(C.cGray + "XP: " + C.cGreen + xp)
				.addLore("")
				.addLore(C.cGray + "Play games and get kills")
				.addLore(C.cGray + "to earn Battle Pass XP!")
				.build();
		addButton(4, infoItem, new IButton() {
			@Override
			public void onClick(Player p, ClickType clickType) {}
		});

		// Info Premium Pass
		ItemStack premiumInfoItem = new ItemBuilder(Material.NETHER_STAR)
				.setTitle(C.cPurple + C.Bold + "Premium Pass")
				.addLore(C.cGray + "Status: " + (ownsPremium ? C.cGreen + "UNLOCKED" : C.cRed + "LOCKED"))
				.addLore("")
				.addLore(C.cGray + "Unlock the premium pass to")
				.addLore(C.cGray + "access the top reward track!")
				.build();
		addButton(8, premiumInfoItem, new IButton() {
			@Override
			public void onClick(Player p, ClickType clickType) {
				if (!ownsPremium) {
					p.sendMessage(F.main("Battle Pass", "Visit the store to unlock the Premium track!"));
				}
			}
		});

		BattlePassTier[] tiers = BattlePassTier.getTiers();
		
		int startIndex = _page * 7;
		int endIndex = Math.min(startIndex + 7, tiers.length);

		for (int i = startIndex; i < endIndex; i++) {
			final BattlePassTier tier = tiers[i];
			int relativeIndex = i - startIndex;
			
			int slotPremium = 10 + relativeIndex;
			int slotIndicator = 19 + relativeIndex;
			int slotFree = 28 + relativeIndex;

			boolean unlocked = xp >= tier.getRequiredXp();
			boolean claimedFree = data.hasClaimed(tier.getTier());
			boolean claimedPremium = data.hasClaimedPremium(tier.getTier());

			// -------- FREE TRACK --------
			ItemBuilder freeBuilder;
			if (claimedFree) {
				freeBuilder = new ItemBuilder(Material.LIME_STAINED_GLASS_PANE)
					.setTitle(C.cGreen + C.Bold + "Free Reward " + tier.getTier() + " (Claimed)")
					.addLore(C.cGray + "Reward: " + C.cAqua + tier.getEssenceReward() + " Essence");
				if (tier.getCosmeticReward() != null) freeBuilder.addLore(C.cGray + "Cosmetic: " + C.cPurple + tier.getCosmeticReward());
				freeBuilder.addLore("").addLore(C.cRed + "Already Claimed");
			} else if (unlocked) {
				freeBuilder = new ItemBuilder(Material.EMERALD_BLOCK)
					.setTitle(C.cGold + C.Bold + "Free Reward " + tier.getTier())
					.addLore(C.cGray + "Reward: " + C.cAqua + tier.getEssenceReward() + " Essence");
				if (tier.getCosmeticReward() != null) freeBuilder.addLore(C.cGray + "Cosmetic: " + C.cPurple + tier.getCosmeticReward());
				freeBuilder.addLore("").addLore(C.cGreen + "Left-Click to claim!");
			} else {
				freeBuilder = new ItemBuilder(Material.RED_STAINED_GLASS_PANE)
					.setTitle(C.cRed + C.Bold + "Free Reward " + tier.getTier() + " (Locked)")
					.addLore(C.cGray + "Reward: " + C.cAqua + tier.getEssenceReward() + " Essence");
				if (tier.getCosmeticReward() != null) freeBuilder.addLore(C.cGray + "Cosmetic: " + C.cPurple + tier.getCosmeticReward());
			}

			addButton(slotFree, freeBuilder.build(), (p, clickType) -> {
				if (claimedFree || !unlocked) {
					p.playSound(p.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASS, 1f, 0.5f);
					return;
				}
				data.claimTier(tier.getTier());
				getPlugin().saveData(p.getName(), data);
				getPlugin().getDonationManager().RewardEssenceLater("Battle Pass Free Tier " + tier.getTier(), p, tier.getEssenceReward());
				p.playSound(p.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1f, 1f);
				if (tier.getCosmeticReward() != null) {
					getPlugin().getDonationManager().PurchaseUnknownSalesPackage(null, p.getName(), getClientManager().Get(p.getName()).getAccountId(), tier.getCosmeticReward(), false, 1, false);
					UtilPlayer.message(p, F.main("Battle Pass", "You claimed " + C.cPurple + tier.getCosmeticReward() + C.cGray + " for reaching Tier " + tier.getTier() + "!"));
				}
				buildPage();
			});

			// -------- PREMIUM TRACK --------
			ItemBuilder premiumBuilder;
			if (claimedPremium) {
				premiumBuilder = new ItemBuilder(Material.LIME_STAINED_GLASS_PANE)
					.setTitle(C.cPurple + C.Bold + "Premium Reward " + tier.getTier() + " (Claimed)")
					.addLore(C.cGray + "Reward: " + C.cAqua + tier.getPremiumEssenceReward() + " Essence");
				if (tier.getPremiumCosmeticReward() != null) premiumBuilder.addLore(C.cGray + "Cosmetic: " + C.cPurple + tier.getPremiumCosmeticReward());
				premiumBuilder.addLore("").addLore(C.cRed + "Already Claimed");
			} else if (unlocked && ownsPremium) {
				premiumBuilder = new ItemBuilder(Material.DIAMOND_BLOCK)
					.setTitle(C.cPurple + C.Bold + "Premium Reward " + tier.getTier())
					.addLore(C.cGray + "Reward: " + C.cAqua + tier.getPremiumEssenceReward() + " Essence");
				if (tier.getPremiumCosmeticReward() != null) premiumBuilder.addLore(C.cGray + "Cosmetic: " + C.cPurple + tier.getPremiumCosmeticReward());
				premiumBuilder.addLore("").addLore(C.cGreen + "Left-Click to claim!").setGlow(true);
			} else {
				premiumBuilder = new ItemBuilder(Material.RED_STAINED_GLASS_PANE)
					.setTitle(C.cRed + C.Bold + "Premium Reward " + tier.getTier() + " (Locked)")
					.addLore(C.cGray + "Reward: " + C.cAqua + tier.getPremiumEssenceReward() + " Essence");
				if (tier.getPremiumCosmeticReward() != null) premiumBuilder.addLore(C.cGray + "Cosmetic: " + C.cPurple + tier.getPremiumCosmeticReward());
				if (!ownsPremium) premiumBuilder.addLore("").addLore(C.cRed + "Requires Premium Pass");
			}

			addButton(slotPremium, premiumBuilder.build(), (p, clickType) -> {
				if (!ownsPremium) {
					p.sendMessage(F.main("Battle Pass", "You do not own the Premium Pass!"));
					p.playSound(p.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASS, 1f, 0.5f);
					return;
				}
				if (claimedPremium || !unlocked) {
					p.playSound(p.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASS, 1f, 0.5f);
					return;
				}
				data.claimPremiumTier(tier.getTier());
				getPlugin().saveData(p.getName(), data);
				getPlugin().getDonationManager().RewardEssenceLater("Battle Pass Premium Tier " + tier.getTier(), p, tier.getPremiumEssenceReward());
				p.playSound(p.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1f, 1f);
				if (tier.getPremiumCosmeticReward() != null) {
					getPlugin().getDonationManager().PurchaseUnknownSalesPackage(null, p.getName(), getClientManager().Get(p.getName()).getAccountId(), tier.getPremiumCosmeticReward(), false, 1, false);
					UtilPlayer.message(p, F.main("Battle Pass", "You claimed " + C.cPurple + tier.getPremiumCosmeticReward() + C.cGray + " from the Premium Track!"));
				}
				buildPage();
			});

			// -------- INDICATOR --------
			ItemBuilder indicatorBuilder = unlocked 
				? new ItemBuilder(Material.LIGHT_BLUE_STAINED_GLASS_PANE).setTitle(C.cGold + "Tier " + tier.getTier() + " Reached") 
				: new ItemBuilder(Material.GRAY_STAINED_GLASS_PANE).setTitle(C.cGray + "Tier " + tier.getTier() + " (Requires " + tier.getRequiredXp() + " XP)");
			addButton(slotIndicator, indicatorBuilder.build(), (p, clickType) -> {});
		}

		// Pagination Buttons
		if (_page > 0) {
			addButton(45, new ItemBuilder(Material.ARROW).setTitle(C.cGreen + "Previous Page").build(), new IButton() {
				@Override
				public void onClick(Player p, ClickType clickType) {
					getShop().openPageForPlayer(p, new BattlePassPage(getPlugin(), getShop(), getClientManager(), getDonationManager(), p, _page - 1));
				}
			});
		}
		
		if (endIndex < tiers.length) {
			addButton(53, new ItemBuilder(Material.ARROW).setTitle(C.cGreen + "Next Page").build(), new IButton() {
				@Override
				public void onClick(Player p, ClickType clickType) {
					getShop().openPageForPlayer(p, new BattlePassPage(getPlugin(), getShop(), getClientManager(), getDonationManager(), p, _page + 1));
				}
			});
		}
	}
}
