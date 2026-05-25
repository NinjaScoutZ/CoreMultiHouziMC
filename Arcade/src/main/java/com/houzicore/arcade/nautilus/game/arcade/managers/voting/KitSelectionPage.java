package com.houzicore.arcade.nautilus.game.arcade.managers.voting;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import com.houzicore.shared.account.CoreClientManager;
import com.houzicore.shared.common.util.C;
import com.houzicore.shared.core.donation.DonationManager;
import com.houzicore.shared.core.shop.page.ShopPageBase;
import com.houzicore.arcade.ArcadeManager;
import com.houzicore.arcade.nautilus.game.arcade.kit.Kit;

public class KitSelectionPage extends ShopPageBase<ArcadeManager, KitSelectionShop> {

    public KitSelectionPage(ArcadeManager plugin, KitSelectionShop shop, CoreClientManager clientManager,
                            DonationManager donationManager, Player player) {
        super(plugin, shop, clientManager, donationManager, 
              com.houzicore.shared.core.lang.LangManager.get().get(player, "arcade.kit_select_gui_title", "Select Kit"), 
              player, 54);
        buildPage();
    }

    @Override
    protected void buildPage() {
        if (getInventory() == null) return;
        
        getInventory().clear();
        getButtonMap().clear();

        // 1. Blue Glass Border
        int balance = getDonationManager().Get(getPlayer().getName()).GetBalance(com.houzicore.shared.common.CurrencyType.Essence);
        ItemStack borderGlass = new com.houzicore.shared.core.itemstack.ItemBuilder(Material.LIGHT_BLUE_STAINED_GLASS_PANE)
            .setTitle(C.cAqua + "Essence: " + C.cGreen + balance)
            .build();
            
        for (int i = 0; i < getSize(); i++) {
            if (i < 9 || i >= 45 || i % 9 == 0 || i % 9 == 8) {
                getInventory().setItem(i, borderGlass);
            }
        }

        if (getPlugin().GetGame() == null) return;
        
        Kit[] kits = getPlugin().GetGame().GetKits();
        java.util.List<Kit> visibleKits = new java.util.ArrayList<>();
        for (Kit k : kits) {
            if (k.GetAvailability() != com.houzicore.arcade.nautilus.game.arcade.kit.KitAvailability.Hide &&
                k.GetAvailability() != com.houzicore.arcade.nautilus.game.arcade.kit.KitAvailability.Null) {
                visibleKits.add(k);
            }
        }

        // Center kits dynamically
        int count = visibleKits.size();
        int[] slots;
        if (count <= 7) {
            slots = new int[count];
            int startSlot = 22 - (count / 2);
            for (int i = 0; i < count; i++) {
                slots[i] = startSlot + i;
            }
        } else {
            // Two rows
            slots = new int[count];
            int row1Count = Math.min(7, count);
            int row2Count = count - row1Count;
            
            int startSlot1 = 13 - (row1Count / 2);
            for (int i = 0; i < row1Count; i++) {
                slots[i] = startSlot1 + i;
            }
            
            int startSlot2 = 31 - (row2Count / 2);
            for (int i = 0; i < row2Count; i++) {
                slots[row1Count + i] = startSlot2 + i;
            }
        }

        Kit selectedKit = getPlugin().GetGame().GetKit(getPlayer());

        for (int i = 0; i < visibleKits.size() && i < slots.length; i++) {
            final Kit kit = visibleKits.get(i);
            int slot = slots[i];

            boolean isSelected = selectedKit != null && selectedKit.GetName().equalsIgnoreCase(kit.GetName());
            
            // Build item icon
            Material mat = kit.getDisplayMaterial();
            if (mat == null || mat == Material.AIR) mat = Material.CHEST;
            
            com.houzicore.shared.core.itemstack.ItemBuilder builder = new com.houzicore.shared.core.itemstack.ItemBuilder(mat)
                .setTitle((isSelected ? C.cGreen + C.Bold : C.cYellow) + kit.GetName(getPlayer()));
            
            // Lore
            java.util.List<String> lore = new java.util.ArrayList<>();
            lore.add("§8───────────────────");
            for (String desc : kit.GetDesc(getPlayer())) {
                lore.add("§7" + desc);
            }
            lore.add("§8───────────────────");
            
            if (isSelected) {
                lore.add("§a✓ Selected");
                // Enchant glow for selected kit
                builder.addEnchantment(org.bukkit.enchantments.Enchantment.UNBREAKING, 1);
            } else {
                lore.add("§eClick to select this Kit");
            }
            
            builder.addLore(lore.toArray(new String[0]));
            
            addButton(slot, builder.build(), (clicker, clickType) -> {
                playAcceptSound(clicker);
                getPlugin().GetGamePlayerManager().KitClick(clicker, kit, null);
                // Refresh page
                buildPage();
            });
        }
    }
}
