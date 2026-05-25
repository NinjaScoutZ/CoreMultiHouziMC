package com.houzicore.shared.core.cosmetic.collection;

import com.houzicore.shared.account.CoreClientManager;
import com.houzicore.shared.common.util.C;
import com.houzicore.shared.core.cosmetic.CosmeticManager;
import com.houzicore.shared.core.cosmetic.ui.CosmeticShop;
import com.houzicore.shared.core.cosmetic.ui.GuiUtil;
import com.houzicore.shared.core.donation.DonationManager;
import com.houzicore.shared.core.shop.item.IButton;
import com.houzicore.shared.core.shop.item.ShopItem;
import com.houzicore.shared.core.shop.page.ShopPageBase;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;

public class CollectionPage extends ShopPageBase<CosmeticManager, CosmeticShop> {

    public CollectionPage(CosmeticManager plugin, CosmeticShop shop, CoreClientManager clientManager, DonationManager donationManager, String name, Player player) {
        super(plugin, shop, clientManager, donationManager, name, player, 54);
        buildPage();
    }

    @Override
    protected void buildPage() {
        GuiUtil.fillBorders(getInventory());

        int slot = 19;
        for (CosmeticCollection collection : CosmeticCollection.values()) {
            if (slot == 26 || slot == 35) slot += 2; // Skip right border
            if (slot >= 44) break;
            
            int owned = 0;
            int total = collection.getRequiredItems().size();
            
            for (String item : collection.getRequiredItems()) {
                if (getDonationManager().Get(getPlayer().getName()).OwnsUnknownPackage(item)) {
                    owned++;
                }
            }
            
            boolean completed = owned >= total;
            
            String[] lore = new String[total + 6]; // +1 for " ", +2 for header, +1 for " ", +1 for " ", +1 for bonus
            lore[0] = " ";
            lore[1] = C.cGray + "Collect all items in this";
            lore[2] = C.cGray + "set to unlock a special bonus!";
            lore[3] = " ";
            
            int loreIndex = 4;
            for (String item : collection.getRequiredItems()) {
                boolean hasItem = getDonationManager().Get(getPlayer().getName()).OwnsUnknownPackage(item);
                lore[loreIndex++] = (hasItem ? C.cGreen + "✔ " : C.cRed + "✖ ") + C.cWhite + item;
            }
            
            lore[loreIndex] = " ";
            loreIndex++;
            if (completed) {
                lore[loreIndex] = C.cYellow + "Bonus: " + C.cGreen + collection.getBonusDescription();
            } else {
                lore[loreIndex] = C.cYellow + "Bonus: " + C.cDGray + "???";
            }
            
            String title = (completed ? C.cGreen + C.Bold : collection.getColor() + "" + ChatColor.BOLD) + collection.getDisplayName();
            String progress = C.cYellow + " (" + owned + "/" + total + ")";
            
            ShopItem shopItem = new ShopItem(collection.getIcon(), (byte) 0, title + progress, lore, 1, false, false);
            
            addButton(slot, shopItem, new IButton() {
                @Override
                public void onClick(Player player, ClickType clickType) {
                    playAcceptSound(player);
                }
            });
            
            if (completed) {
                addGlow(slot);
            }
            
            slot++;
        }

        addButton(4, new ShopItem(Material.RED_BED, C.cGray + " \u21FD Go Back", new String[]{}, 1, false), new IButton() {
            @Override
            public void onClick(Player player, ClickType clickType) {
                getShop().openPageForPlayer(getPlayer(),
                        new com.houzicore.shared.core.cosmetic.ui.page.Menu(getPlugin(), getShop(), getClientManager(), getDonationManager(), player));
            }
        });
    }
}
