package com.houzicore.arcade.nautilus.game.arcade.kit.traits.ui;

import java.util.HashSet;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import com.houzicore.shared.account.CoreClientManager;
import com.houzicore.shared.core.donation.DonationManager;
import com.houzicore.shared.core.itemstack.ItemStackFactory;
import com.houzicore.shared.core.shop.page.ShopPageBase;
import com.houzicore.shared.common.util.C;
import com.houzicore.arcade.ArcadeManager;
import com.houzicore.arcade.nautilus.game.arcade.kit.Kit;
import com.houzicore.arcade.nautilus.game.arcade.kit.traits.Trait;

public class TraitPage extends ShopPageBase<ArcadeManager, TraitShop> {

    private Kit _kit;

    public TraitPage(ArcadeManager plugin, TraitShop shop, CoreClientManager clientManager,
                     DonationManager donationManager, Player player, Kit kit) {
        super(plugin, shop, clientManager, donationManager, "Traits: " + kit.GetName(), player, 54);
        _kit = kit;
        buildPage();
    }

    @Override
    protected void buildPage() {
        // 1. Blue glass border
        ItemStack glass = ItemStackFactory.Instance.CreateStack(
            Material.CYAN_STAINED_GLASS_PANE, (byte) 3, 1,
            "§bGems: " + getDonationManager().Get(getPlayer()).GetEssence()
        );
        for (int i = 0; i < getSize(); i++) {
            getInventory().setItem(i, glass);
        }

        HashSet<Trait> traits = getPlugin().getTraitManager().getTraitsForKit(_kit);
        Trait equipped = getPlugin().getTraitManager().getEquippedTrait(getPlayer(), _kit);

        int slot = 20;

        for (Trait trait : traits) {
            boolean owns = getPlugin().getTraitManager().ownsTrait(getPlayer(), trait);
            boolean isEquipped = (equipped != null && equipped.getKey().equals(trait.getKey()));

            String[] lore = new String[trait.getDesc().length + 3];
            for (int i = 0; i < trait.getDesc().length; i++) {
                lore[i] = "§7" + trait.getDesc()[i];
            }
            lore[trait.getDesc().length] = "§8───────────";
            
            if (isEquipped) {
                lore[trait.getDesc().length + 1] = "§aClick to Unequip";
            } else if (owns) {
                lore[trait.getDesc().length + 1] = "§eClick to Equip";
            } else {
                lore[trait.getDesc().length + 1] = "§cClick to Buy (" + trait.getCost() + " Gems)";
            }
            lore[trait.getDesc().length + 2] = "";

            ItemStack item = ItemStackFactory.Instance.CreateStack(
                trait.getDisplayMaterial(), (byte) 0, 1,
                (isEquipped ? "§a§l" : (owns ? "§e§l" : "§c§l")) + trait.getName(),
                lore
            );
            
            if (isEquipped) {
                ItemMeta meta = item.getItemMeta();
                meta.addEnchant(org.bukkit.enchantments.Enchantment.PROTECTION, 1, true);
                meta.addItemFlags(org.bukkit.inventory.ItemFlag.HIDE_ENCHANTS);
                item.setItemMeta(meta);
            }

            addButton(slot, item, (p, clickType) -> {
                if (isEquipped) {
                    playAcceptSound(p);
                    getPlugin().getTraitManager().clearEquippedTrait(p, _kit);
                    p.sendMessage(C.cGreen + "Unequipped " + trait.getName() + ".");
                    refresh();
                } else if (owns) {
                    playAcceptSound(p);
                    getPlugin().getTraitManager().equipTrait(p, _kit, trait);
                    p.sendMessage(C.cGreen + "Equipped " + trait.getName() + "!");
                    refresh();
                } else {
                    // Quick purchase bypass for testing
                    if (getDonationManager().Get(p).GetEssence() >= trait.getCost()) {
                        getDonationManager().PurchaseUnknownSalesPackage(res -> {
                            org.bukkit.Bukkit.getScheduler().runTask(getPlugin().getPlugin(), () -> {
                                if (!p.isOnline()) return;
                                if (res == com.houzicore.shared.server.util.TransactionResponse.Success) {
                                    p.sendMessage(C.cGreen + "You bought " + trait.getName() + "!");
                                    playAcceptSound(p);
                                    refresh();
                                } else if (res == com.houzicore.shared.server.util.TransactionResponse.AlreadyOwns) {
                                    p.sendMessage(C.cRed + "You already own this!");
                                    refresh();
                                } else {
                                    playDenySound(p);
                                    p.sendMessage(C.cRed + "Purchase failed: " + res.name());
                                }
                            });
                        }, p.getName(), getClientManager().Get(p).getAccountId(), getPlugin().getTraitManager().getTraitSalesPackageName(trait), false, trait.getCost(), true);
                    } else {
                        playDenySound(p);
                        p.sendMessage(C.cRed + "You do not have enough Gems.");
                    }
                }
            });

            slot += 2;
        }
    }
}
