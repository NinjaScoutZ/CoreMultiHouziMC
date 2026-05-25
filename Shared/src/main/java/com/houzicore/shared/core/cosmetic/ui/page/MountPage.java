package com.houzicore.shared.core.cosmetic.ui.page;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;

import com.houzicore.shared.account.CoreClientManager;
import com.houzicore.shared.common.CurrencyType;
import com.houzicore.shared.common.util.C;
import com.houzicore.shared.core.cosmetic.CosmeticManager;
import com.houzicore.shared.core.cosmetic.CosmeticProgression;
import com.houzicore.shared.core.cosmetic.ui.CosmeticShop;
import com.houzicore.shared.core.cosmetic.ui.button.ActivateMountButton;
import com.houzicore.shared.core.cosmetic.ui.button.DeactivateMountButton;
import com.houzicore.shared.core.cosmetic.ui.button.MountButton;
import com.houzicore.shared.core.donation.DonationManager;
import com.houzicore.shared.core.gadget.CosmeticRarity;
import com.houzicore.shared.core.mount.Mount;
import com.houzicore.shared.core.shop.item.IButton;
import com.houzicore.shared.core.shop.item.ShopItem;
import com.houzicore.shared.core.shop.page.ShopPageBase;
import com.houzicore.shared.core.cosmetic.ui.GuiUtil;

public class MountPage extends ShopPageBase<CosmeticManager, CosmeticShop> {
	public MountPage(CosmeticManager plugin, CosmeticShop shop, CoreClientManager clientManager,
			DonationManager donationManager, String name, Player player) {
		super(plugin, shop, clientManager, donationManager, name, player, 54);

		buildPage();
	}

	protected void addMount(final Mount<?> mount, int slot) {
		CosmeticRarity rarity = CosmeticProgression.getShopRarity(mount);

		boolean owns = false;
		if (getDonationManager().Get(getPlayer().getName()).OwnsUnknownPackage(mount.GetName())) {
			owns = true;
		} else if (getPlugin().getInventoryManager().Get(getPlayer()).getItemCount(mount.GetName()) > 0) {
			owns = true;
		}
		boolean isActive = mount.GetActive().containsKey(getPlayer());

		final List<String> itemLore = new ArrayList<>();

		// ── 1. Ownership Banner ──
		if (owns) {
			if (isActive) {
				itemLore.add(C.cGreen + C.Bold + "▶ " + com.houzicore.shared.core.lang.LangManager.get().get(getPlayer(), "cosmetic.active"));
			} else {
				itemLore.add(C.cGreen + "✔ " + com.houzicore.shared.core.lang.LangManager.get().get(getPlayer(), "cosmetic.unlocked"));
			}
		} else {
			itemLore.add(C.cRed + "✖ " + com.houzicore.shared.core.lang.LangManager.get().get(getPlayer(), "cosmetic.locked"));
		}

		// ── 2. Rarity ──
		itemLore.add(" ");
		itemLore.add(rarity.getDisplayName());
		itemLore.add(" ");

		// ── 3. Description ──
		for (String line : mount.GetDescription()) {
			itemLore.add(C.cGray + line);
		}
		itemLore.add(" ");

		// ── 4. Price ──
		if (owns) {
			if (mount.GetCost(CurrencyType.Essence) >= 0) {
				itemLore.add(C.cDGray + com.houzicore.shared.core.lang.LangManager.get().get(getPlayer(), "cosmetic.cost") + mount.GetCost(CurrencyType.Essence) + (com.houzicore.shared.core.lang.LangManager.get().isThai(getPlayer()) ? " เอสเซนส์" : " Essence"));
			}
		} else {
			if (mount.GetCost(CurrencyType.Essence) > 0) {
				boolean canAfford = getDonationManager().Get(getPlayer().getName()).GetBalance(CurrencyType.Essence) >= mount.GetCost(CurrencyType.Essence);
				itemLore.add(C.cDGray + com.houzicore.shared.core.lang.LangManager.get().get(getPlayer(), "cosmetic.cost") + (canAfford ? C.cGray : C.cDGray) + mount.GetCost(CurrencyType.Essence) + (com.houzicore.shared.core.lang.LangManager.get().isThai(getPlayer()) ? " เอสเซนส์" : " Essence"));
			} else if (mount.GetCost(CurrencyType.Essence) == -2) {
				itemLore.add(C.cDGray + com.houzicore.shared.core.lang.LangManager.get().get(getPlayer(), "cosmetic.treasure_found"));
			} else {
				itemLore.add(C.cGreen + com.houzicore.shared.core.lang.LangManager.get().get(getPlayer(), "cosmetic.free"));
			}
		}

		// ── 5. Action Hint ──
		itemLore.add(" ");
		if (owns) {
			if (isActive) {
				itemLore.add(C.cGray + com.houzicore.shared.core.lang.LangManager.get().get(getPlayer(), "cosmetic.dismount"));
			} else {
				itemLore.add(C.cYellow + com.houzicore.shared.core.lang.LangManager.get().get(getPlayer(), "cosmetic.mount"));
			}
		} else {
			if (mount.GetCost(CurrencyType.Essence) > 0 && getDonationManager().Get(getPlayer().getName()).GetBalance(CurrencyType.Essence) >= mount.GetCost(CurrencyType.Essence)) {
				itemLore.add(C.cYellow + com.houzicore.shared.core.lang.LangManager.get().get(getPlayer(), "cosmetic.purchase"));
			} else if (mount.GetCost(CurrencyType.Essence) == -2) {
				itemLore.add(C.cDGray + com.houzicore.shared.core.lang.LangManager.get().get(getPlayer(), "cosmetic.unlock_info"));
			} else if (mount.GetCost(CurrencyType.Essence) == 0) {
				itemLore.add(C.cYellow + com.houzicore.shared.core.lang.LangManager.get().get(getPlayer(), "cosmetic.claim_free"));
			} else {
				itemLore.add(C.cDGray + (com.houzicore.shared.core.lang.LangManager.get().isThai(getPlayer()) ? "เอสเซนส์ไม่พอ" : "Not enough Essence"));
			}
		}

		String title;
		if (owns) {
			title = isActive
					? (C.cGreen + C.Bold + mount.GetName())
					: (rarity.getColor() + "" + org.bukkit.ChatColor.BOLD + mount.GetName());
		} else {
			title = C.cRed + mount.GetName();
		}

		ShopItem item = new ShopItem(mount.GetDisplayMaterial(), mount.GetDisplayData(),
				title, itemLore.toArray(new String[0]), 1, !owns, false);

		if (owns) {
			if (isActive) {
				addButton(slot, item, new DeactivateMountButton(mount, this));
				addGlow(slot);
			} else {
				addButton(slot, item, new ActivateMountButton(mount, this));
			}
		} else {
			if (mount.GetCost(CurrencyType.Essence) >= 0 && getDonationManager().Get(getPlayer().getName()).GetBalance(CurrencyType.Essence) >= mount.GetCost(CurrencyType.Essence)) {
				addButton(slot, item, new IButton() {
					@Override
					public void onClick(Player player, ClickType clickType) {
						final Mount<?> finalMount = mount;
						getShop().openPageForPlayer(getPlayer(), new com.houzicore.shared.core.shop.page.ConfirmationPage<>(
							getPlugin(), getShop(), getClientManager(), getDonationManager(),
							// Post-success hook: only refresh — purchase is handled by processTransaction() via SalesPackageBase
							() -> refresh(),
							MountPage.this,
							new com.houzicore.shared.core.shop.item.SalesPackageBase(
									finalMount.GetName(), finalMount.GetDisplayMaterial(), (byte) 0,
									new String[]{}, finalMount.GetCost(CurrencyType.Essence)) {
								@Override
								public void Sold(Player p, CurrencyType type) {
									// Intentionally empty — ownership granted by PurchaseUnknownSalesPackage in processTransaction()
								}
							},
							CurrencyType.Essence, getPlayer()
						));
					}
				});
			} else {
				setItem(slot, item);
			}
		}
	}

	/**
	 * Safe inner slot grid: rows 2-5 (slots 10-16, 19-25, 28-34, 37-43).
	 * Each row holds 7 items, total 28 slots. Items centered per row.
	 */
	private int[] getSafeSlots(int count) {
		int[][] rows = {
			{10, 11, 12, 13, 14, 15, 16},
			{19, 20, 21, 22, 23, 24, 25},
			{28, 29, 30, 31, 32, 33, 34},
			{37, 38, 39, 40, 41, 42, 43}
		};
		int maxPerRow = 7;
		int totalCapacity = maxPerRow * rows.length;
		if (count > totalCapacity) count = totalCapacity;

		int[] result = new int[count];
		int placed = 0;

		for (int r = 0; r < rows.length && placed < count; r++) {
			int remaining = count - placed;
			int rowsLeft = rows.length - r;
			int inThisRow = Math.min(maxPerRow, (remaining + rowsLeft - 1) / rowsLeft);
			if (inThisRow > remaining) inThisRow = remaining;

			int startOffset = (maxPerRow - inThisRow) / 2;
			for (int i = 0; i < inThisRow; i++) {
				result[placed++] = rows[r][startOffset + i];
			}
		}
		return result;
	}

	@Override
	protected void buildPage() {
		GuiUtil.fillBorders(getInventory());

		List<Mount<?>> mounts = new ArrayList<>(getPlugin().getMountManager().getMounts());
		mounts.sort(CosmeticProgression.mountComparator());

		int[] slots = getSafeSlots(mounts.size());
		for (int i = 0; i < mounts.size() && i < slots.length; i++) {
			addMount(mounts.get(i), slots[i]);
		}

		addButton(4, new ShopItem(Material.RED_BED, C.cGray + " \u21FD " + com.houzicore.shared.core.lang.LangManager.get().get(getPlayer(), "cosmetic.go_back"), new String[] {}, 1, false), new IButton() {
			@Override
			public void onClick(Player player, ClickType clickType) {
				getShop().openPageForPlayer(getPlayer(),
						new com.houzicore.shared.core.cosmetic.ui.page.Menu(getPlugin(), getShop(), getClientManager(), getDonationManager(), player));
			}
		});
	}
}
