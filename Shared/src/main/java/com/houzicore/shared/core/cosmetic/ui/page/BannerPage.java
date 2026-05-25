package com.houzicore.shared.core.cosmetic.ui.page;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;

import com.houzicore.shared.account.CoreClientManager;
import com.houzicore.shared.common.util.C;
import com.houzicore.shared.core.cosmetic.CosmeticManager;
import com.houzicore.shared.core.cosmetic.ui.CosmeticShop;
import com.houzicore.shared.core.cosmetic.ui.GuiUtil;
import com.houzicore.shared.core.donation.DonationManager;
import com.houzicore.shared.core.gadget.types.Gadget;
import com.houzicore.shared.core.gadget.types.GadgetType;
import com.houzicore.shared.core.shop.item.IButton;
import com.houzicore.shared.core.shop.item.ShopItem;

public class BannerPage extends GadgetPage {
    public BannerPage(CosmeticManager plugin, CosmeticShop shop, CoreClientManager clientManager,
            DonationManager donationManager, String name, Player player) {
        super(plugin, shop, clientManager, donationManager, name, player);
    }

    @Override
    protected void buildPage() {
        GuiUtil.fillBorders(getInventory());

        java.util.List<Gadget> gadgets = new java.util.ArrayList<>();
        if (getPlugin().getGadgetManager().getGadgets(GadgetType.Banner) != null) {
            gadgets.addAll(getPlugin().getGadgetManager().getGadgets(GadgetType.Banner));
        }

        int[] slots = getCenteredSlots(gadgets.size());
        for (int i = 0; i < gadgets.size() && i < slots.length; i++) {
            addGadget(gadgets.get(i), slots[i]);
            if (getPlugin().getGadgetManager().getActive(getPlayer(), GadgetType.Banner) == gadgets.get(i)) {
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
