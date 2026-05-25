package com.houzicore.shared.core.cosmetic.ui.page;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;

import com.houzicore.shared.account.CoreClientManager;
import com.houzicore.shared.common.util.C;
import com.houzicore.shared.core.cosmetic.CosmeticManager;
import com.houzicore.shared.core.cosmetic.ui.CosmeticShop;
import com.houzicore.shared.core.donation.DonationManager;
import com.houzicore.shared.core.gadget.types.Gadget;
import com.houzicore.shared.core.gadget.types.GadgetType;
import com.houzicore.shared.core.shop.item.ShopItem;
import com.houzicore.shared.core.shop.item.IButton;
import com.houzicore.shared.core.cosmetic.ui.GuiUtil;

import java.util.ArrayList;
import java.util.List;

public class ParticlePage extends GadgetPage {
	public ParticlePage(CosmeticManager plugin, CosmeticShop shop, CoreClientManager clientManager,
			DonationManager donationManager, String name, Player player) {
		super(plugin, shop, clientManager, donationManager, name, player);
	}

	@Override
	protected void buildPage() {
		GuiUtil.fillBorders(getInventory());

		List<Gadget> gadgets = new ArrayList<>();
		if (getPlugin().getGadgetManager().getGadgets(GadgetType.Particle) != null) {
			gadgets.addAll(getPlugin().getGadgetManager().getGadgets(GadgetType.Particle));
		}

		int[] slots = getCenteredSlots(gadgets.size());
		for (int i = 0; i < gadgets.size() && i < slots.length; i++) {
			addGadget(gadgets.get(i), slots[i]);
			if (getPlugin().getGadgetManager().getActive(getPlayer(), GadgetType.Particle) == gadgets.get(i)) {
				addGlow(slots[i]);
			}
		}

		addButton(4, new ShopItem(Material.RED_BED, C.cGray + " \u21FD " + com.houzicore.shared.core.lang.LangManager.get().get(getPlayer(), "cosmetic.go_back"), new String[] {}, 1, false), new IButton() {
			@Override
			public void onClick(Player player, ClickType clickType) {
				getShop().openPageForPlayer(getPlayer(),
						new Menu(getPlugin(), getShop(), getClientManager(), getDonationManager(), player));
			}
		});
	}
}
