package com.houzicore.shared.core.cosmetic.ui.page;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;

import com.houzicore.shared.account.CoreClientManager;
import com.houzicore.shared.common.CurrencyType;
import com.houzicore.shared.core.cosmetic.CosmeticProgression;
import com.houzicore.shared.core.cosmetic.CosmeticManager;
import com.houzicore.shared.core.cosmetic.ui.CosmeticShop;
import com.houzicore.shared.core.donation.DonationManager;
import com.houzicore.shared.core.gadget.CosmeticRarity;
import com.houzicore.shared.core.gadget.types.BaitGadget;
import com.houzicore.shared.core.gadget.types.Gadget;
import com.houzicore.shared.core.gadget.types.GadgetType;
import com.houzicore.shared.core.shop.item.IButton;

public class BaitPage extends AnimatedMenuPage {

	public BaitPage(CosmeticManager plugin, CosmeticShop shop, CoreClientManager clientManager,
			DonationManager donationManager, String name, Player player) {
		super(plugin, shop, clientManager, donationManager, name, player);
		buildPage();
		startAnimations();
	}

	@Override
	protected void buildPage() {
		com.houzicore.shared.core.cosmetic.ui.GuiUtil.fillBorders(getInventory());
		
		int slot = 10;
		List<Gadget> gadgets = new ArrayList<>(getPlugin().getGadgetManager().getGadgets(GadgetType.Bait));
		gadgets.sort(CosmeticProgression.gadgetComparator());
		for (Gadget gadget : gadgets) {
			BaitGadget bait = (BaitGadget) gadget;
			if (slot > 43) break;

			addBaitButton(slot, bait);

			slot++;
			if (slot % 9 == 8) slot += 2;
		}
	}

	private void addBaitButton(int slot, final BaitGadget bait) {
		addButton(slot, getCosmeticItem(bait), new IButton() {
			@Override
			public void onClick(Player player, ClickType clickType) {
				if (clickType == ClickType.RIGHT) {
					// Purchase Ammo
					purchaseGadget(player, bait.getAmmo());
					return;
				}

				if (!owns(bait)) {
					purchaseGadget(player, bait);
					return;
				}

				if (bait.IsActive(player)) {
					bait.Disable(player);
					playAcceptSound(player);
				} else {
					bait.Enable(player);
					playAcceptSound(player);
				}
				refresh();
			}
		});
	}

	private boolean owns(Gadget gadget) {
		return gadget.IsFree() || getDonationManager().Get(getPlayer().getName()).OwnsUnknownPackage(gadget.GetName()) || getPlugin().getInventoryManager().Get(getPlayer()).getItemCount(gadget.GetName()) > 0;
	}

	public void purchaseGadget(final Player player, final com.houzicore.shared.core.shop.item.SalesPackageBase packageBase) {
		getShop().openPageForPlayer(getPlayer(), new com.houzicore.shared.core.shop.page.ConfirmationPage<>(getPlugin(), getShop(), getClientManager(),
				getDonationManager(), new Runnable() {
					@Override
                    public void run() {
                        if (packageBase instanceof com.houzicore.shared.core.gadget.gadgets.Ammo) {
                            getPlugin().getInventoryManager().addItemToInventory(getPlayer(), GadgetType.Bait.name(), packageBase.GetName(), ((com.houzicore.shared.core.gadget.gadgets.Ammo)packageBase).getQuantity());
                        } else if (packageBase instanceof Gadget) {
                            getPlugin().getInventoryManager().addItemToInventory(getPlayer(), GadgetType.Bait.name(), packageBase.GetName(), 1);
                        }
                        refresh();
                    }
				}, this, packageBase, com.houzicore.shared.common.CurrencyType.Essence, getPlayer()));
	}

	private com.houzicore.shared.core.shop.item.ShopItem getCosmeticItem(Gadget gadget) {
		boolean ownsGadget = owns(gadget);
		boolean isActive = gadget.GetActive().contains(getPlayer());
		CosmeticRarity rarity = CosmeticProgression.getShopRarity(gadget);
		java.util.List<String> itemLore = new java.util.ArrayList<>();
		
		if (ownsGadget) {
			if (isActive) itemLore.add("§a§l▶ " + com.houzicore.shared.core.lang.LangManager.get().get(getPlayer(), "cosmetic.active"));
			else itemLore.add("§a✔ " + com.houzicore.shared.core.lang.LangManager.get().get(getPlayer(), "cosmetic.unlocked"));
		} else {
			itemLore.add("§c✖ " + com.houzicore.shared.core.lang.LangManager.get().get(getPlayer(), "cosmetic.locked"));
		}
		itemLore.add(" ");
		itemLore.add(rarity.getDisplayName());
		itemLore.add(" ");
		for (String line : gadget.GetDescription()) itemLore.add("§7" + line);
		itemLore.add(" ");
		itemLore.add("§8" + com.houzicore.shared.core.lang.LangManager.get().get(getPlayer(), "cosmetic.cost")
				+ gadget.GetCost(CurrencyType.Essence)
				+ (com.houzicore.shared.core.lang.LangManager.get().isThai(getPlayer()) ? " เอสเซนส์" : " Essence"));
		itemLore.add(" ");
		itemLore.add("§fAmmo: §a" + getPlugin().getInventoryManager().Get(getPlayer()).getItemCount(gadget.GetName()));
		itemLore.add("§8Right-Click to Buy Ammo");

		String title = ownsGadget ? (isActive ? "§a§l" + gadget.GetName() : rarity.getColor() + "§l" + gadget.GetName()) : "§c" + gadget.GetName();
		return new com.houzicore.shared.core.shop.item.ShopItem(gadget.GetDisplayMaterial(), gadget.GetDisplayData(), title, itemLore.toArray(new String[0]), 1, !ownsGadget, false);
	}
}
