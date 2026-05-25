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
import com.houzicore.shared.core.cosmetic.ui.button.ActivateGadgetButton;
import com.houzicore.shared.core.cosmetic.ui.button.DeactivateGadgetButton;
import com.houzicore.shared.core.cosmetic.ui.button.GadgetButton;
import com.houzicore.shared.core.donation.DonationManager;
import com.houzicore.shared.core.gadget.CosmeticRarity;
import com.houzicore.shared.core.gadget.gadgets.MorphBlock;
import com.houzicore.shared.core.gadget.gadgets.MorphNotch;
import com.houzicore.shared.core.gadget.types.Gadget;
import com.houzicore.shared.core.gadget.types.GadgetType;
import com.houzicore.shared.core.gadget.types.ItemGadget;
import com.houzicore.shared.core.shop.item.IButton;
import com.houzicore.shared.core.shop.item.ShopItem;
import com.houzicore.shared.core.cosmetic.ui.GuiUtil;
import com.houzicore.shared.core.shop.page.ConfirmationPage;
import com.houzicore.shared.core.shop.page.ShopPageBase;

public class GadgetPage extends ShopPageBase<CosmeticManager, CosmeticShop> {
	public GadgetPage(CosmeticManager plugin, CosmeticShop shop, CoreClientManager clientManager,
			DonationManager donationManager, String name, Player player) {
		super(plugin, shop, clientManager, donationManager, name, player, 54);

		buildPage();
	}

	public void activateGadget(Player player, Gadget gadget) {
		if (gadget instanceof ItemGadget) {
			if (getPlugin().getInventoryManager().Get(player).getItemCount(gadget.GetName()) <= 0) {
				purchaseGadget(player, gadget);
				return;
			}
		}

		playAcceptSound(player);
		gadget.Enable(player);

		getShop().openPageForPlayer(getPlayer(),
				new Menu(getPlugin(), getShop(), getClientManager(), getDonationManager(), player));
	}

	protected void addGadget(Gadget gadget, int slot) {
		if (gadget instanceof MorphNotch) {
			// Disabled in 1.8
		} else if (gadget instanceof MorphBlock) {
			if (getPlayer().getPassenger() != null)
				return;
		}

		CosmeticRarity rarity = CosmeticProgression.getShopRarity(gadget);

		boolean ownsGadget = gadget.IsFree()
				|| getDonationManager().Get(getPlayer().getName()).OwnsUnknownPackage(gadget.GetName())
				|| getPlugin().getInventoryManager().Get(getPlayer()).getItemCount(gadget.GetName()) > 0;
		boolean isActive = gadget.GetActive().contains(getPlayer());

		final List<String> itemLore = new ArrayList<>();

		// ── 1. Ownership Banner (top, very prominent) ──
		if (ownsGadget) {
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
		for (String line : gadget.GetDescription()) {
			itemLore.add(C.cGray + line);
		}
		itemLore.add(" ");

		// ── 4. Ammo info (ItemGadgets only) ──
		if (gadget instanceof ItemGadget) {
			itemLore.add(" ");
			itemLore.add(C.cWhite + com.houzicore.shared.core.lang.LangManager.get().get(getPlayer(), "cosmetic.ammo") + C.cGreen
					+ getPlugin().getInventoryManager().Get(getPlayer()).getItemCount(gadget.GetName()));
			itemLore.add(C.cDGray + com.houzicore.shared.core.lang.LangManager.get().get(getPlayer(), "cosmetic.right_click_buy"));
		}

		// ── 5. Price (bottom, dimmed gray — de-emphasized) ──
		itemLore.add(" ");
		if (ownsGadget) {
			// Already owned — show price as dim historical info
			if (gadget.GetCost(CurrencyType.Essence) >= 0) {
				itemLore.add(C.cDGray + com.houzicore.shared.core.lang.LangManager.get().get(getPlayer(), "cosmetic.cost") + gadget.GetCost(CurrencyType.Essence) + (com.houzicore.shared.core.lang.LangManager.get().isThai(getPlayer()) ? " เอสเซนส์" : " Essence"));
			}
		} else {
			// Not owned — show price in gray
			if (gadget.GetCost(CurrencyType.Essence) > 0) {
				boolean canAfford = getDonationManager().Get(getPlayer().getName()).GetBalance(CurrencyType.Essence) >= gadget.GetCost(CurrencyType.Essence);
				itemLore.add(C.cDGray + com.houzicore.shared.core.lang.LangManager.get().get(getPlayer(), "cosmetic.cost")
						+ (canAfford ? C.cGray : C.cDGray) + gadget.GetCost(CurrencyType.Essence) + (com.houzicore.shared.core.lang.LangManager.get().isThai(getPlayer()) ? " เอสเซนส์" : " Essence"));
			} else if (gadget.GetCost(CurrencyType.Essence) == -2) {
				itemLore.add(C.cDGray + com.houzicore.shared.core.lang.LangManager.get().get(getPlayer(), "cosmetic.treasure_found"));
			}
		}

		// ── 6. Action hint (very bottom) ──
		itemLore.add(" ");
		if (ownsGadget) {
			if (isActive) {
				itemLore.add(C.cGray + com.houzicore.shared.core.lang.LangManager.get().get(getPlayer(), "cosmetic.deactivate"));
			} else {
				itemLore.add(C.cYellow + com.houzicore.shared.core.lang.LangManager.get().get(getPlayer(), "cosmetic.activate"));
			}
		} else {
			if (gadget.GetCost(CurrencyType.Essence) > 0 && getDonationManager().Get(getPlayer().getName())
					.GetBalance(CurrencyType.Essence) >= gadget.GetCost(CurrencyType.Essence)) {
				itemLore.add(C.cYellow + com.houzicore.shared.core.lang.LangManager.get().get(getPlayer(), "cosmetic.purchase"));
			} else if (gadget.GetCost(CurrencyType.Essence) == -2) {
				itemLore.add(C.cDGray + com.houzicore.shared.core.lang.LangManager.get().get(getPlayer(), "cosmetic.unlock_info"));
			} else {
				itemLore.add(C.cDGray + (com.houzicore.shared.core.lang.LangManager.get().isThai(getPlayer()) ? "เอสเซนส์ไม่พอ" : "Not enough Essence"));
			}
		}

		// ── Build the item & button ──
		String title;
		if (ownsGadget) {
			title = isActive
					? (C.cGreen + C.Bold + gadget.GetName())
					: (rarity.getColor() + "" + org.bukkit.ChatColor.BOLD + gadget.GetName());
		} else {
			title = C.cRed + gadget.GetName();
		}

		String[] loreArray = itemLore.toArray(new String[0]);

		if (ownsGadget) {
			if (isActive) {
				addButton(slot,
						new ShopItem(gadget.GetDisplayMaterial(), gadget.GetDisplayData(), title, loreArray, 1, false, false),
						new DeactivateGadgetButton(gadget, this));
			} else {
				addButton(slot,
						new ShopItem(gadget.GetDisplayMaterial(), gadget.GetDisplayData(), title, loreArray, 1, false, false),
						new ActivateGadgetButton(gadget, this));
			}
			addGlow(slot);
		} else {
			if (gadget.GetCost(CurrencyType.Essence) > 0 && getDonationManager().Get(getPlayer().getName())
					.GetBalance(CurrencyType.Essence) >= gadget.GetCost(CurrencyType.Essence)) {
				addButton(slot,
						new ShopItem(gadget.GetDisplayMaterial(), gadget.GetDisplayData(), title, loreArray, 1, false, false),
						new GadgetButton(gadget, this));
			} else {
				setItem(slot,
						new ShopItem(rarity.getBorderMaterial(), (byte) 0, title, loreArray, 1, true, false));
			}
		}
	}

	/**
	 * Center items dynamically in rows 2-5 (slots 10-16, 19-25, 28-34, 37-43).
	 * Each row holds up to 7 items. Items are centered within each row.
	 */
	protected int[] getCenteredSlots(int count) {
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
			int inThisRow = Math.min(maxPerRow, (remaining + rowsLeft - 1) / rowsLeft); // distribute evenly
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

		

		// Collect gadgets for this type
		List<Gadget> gadgets = new ArrayList<>();
		if (getPlugin().getGadgetManager().getGadgets(GadgetType.Item) != null) {
			gadgets.addAll(getPlugin().getGadgetManager().getGadgets(GadgetType.Item));
		}

		// Get centered slot positions
		int[] slots = getCenteredSlots(gadgets.size());
		for (int i = 0; i < gadgets.size() && i < slots.length; i++) {
			addGadget(gadgets.get(i), slots[i]);
		}

		// Go Back button
		addButton(4, new ShopItem(Material.RED_BED, C.cGray + " \u21FD " + com.houzicore.shared.core.lang.LangManager.get().get(getPlayer(), "cosmetic.go_back"), new String[] {}, 1, false), new IButton() {
			@Override
			public void onClick(Player player, ClickType clickType) {
				getShop().openPageForPlayer(getPlayer(),
						new Menu(getPlugin(), getShop(), getClientManager(), getDonationManager(), player));
			}
		});
	}

	public void deactivateGadget(Player player, Gadget gadget) {
		playAcceptSound(player);
		gadget.Disable(player);
		refresh();
	}

	public void handleRightClick(Player player, Gadget gadget) {
		if (gadget instanceof ItemGadget) {
			purchaseGadget(player, gadget);
		}
	}

	public void purchaseGadget(final Player player, final Gadget gadget) {
		getShop().openPageForPlayer(getPlayer(), new ConfirmationPage<>(getPlugin(), getShop(), getClientManager(),
				getDonationManager(), new Runnable() {
					@Override
					public void run() {
						getPlugin().getInventoryManager().addItemToInventory(getPlayer(), gadget.getGadgetType().name(),
								gadget.GetName(),
								gadget instanceof ItemGadget ? ((ItemGadget) gadget).getAmmo().getQuantity()
										: gadget.getQuantity());
						refresh();
					}
				}, this, gadget instanceof ItemGadget ? ((ItemGadget) gadget).getAmmo() : gadget, CurrencyType.Essence,
				getPlayer()));
	}
}
