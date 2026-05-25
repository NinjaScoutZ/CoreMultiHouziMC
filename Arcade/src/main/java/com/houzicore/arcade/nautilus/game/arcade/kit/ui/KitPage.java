package com.houzicore.arcade.nautilus.game.arcade.kit.ui;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import com.houzicore.arcade.ArcadeManager;
import com.houzicore.arcade.nautilus.game.arcade.game.Game;
import com.houzicore.arcade.nautilus.game.arcade.kit.Kit;
import com.houzicore.arcade.nautilus.game.arcade.kit.KitAvailability;
import com.houzicore.arcade.nautilus.game.arcade.shop.ArcadeShop;
import com.houzicore.shared.account.CoreClientManager;
import com.houzicore.shared.common.util.C;
import com.houzicore.shared.common.util.F;
import com.houzicore.shared.common.util.UtilPlayer;
import com.houzicore.shared.core.donation.DonationManager;
import com.houzicore.shared.core.itemstack.ItemStackFactory;
import com.houzicore.shared.common.util.GUILayout;
import com.houzicore.shared.core.shop.RefreshableGUI;
import com.houzicore.shared.core.shop.page.ShopPageBase;
import com.houzicore.shared.core.lang.LangManager;

public class KitPage extends ShopPageBase<ArcadeManager, ArcadeShop> implements RefreshableGUI {

    private Game _game;

    public KitPage(ArcadeManager plugin, ArcadeShop shop, CoreClientManager clientManager, DonationManager donationManager, Player player, Game game) {
        super(plugin, shop, clientManager, donationManager, LangManager.get().get(player, "prop_rush.lobby.select_kit", "Select Kit"), player, 54);
        _game = game;
        buildPage();
    }

    @Override
    protected void buildPage() {
        // Background
        ItemStack glass = ItemStackFactory.Instance.CreateStack(
            Material.CYAN_STAINED_GLASS_PANE, (byte) 3, 1, " "
        );
        GUILayout.border(getInventory(), glass, 0, 53);

        updateClock();

        if (_game == null) {
            return;
        }

        Kit currentKit = _game.GetKit(getPlayer());
        Kit[] kits = _game.GetKits();

        if (kits == null || kits.length == 0) {
            return;
        }

        // Determine starting slot to center them
        int row = 2;
        int col = (9 - Math.min(kits.length, 7)) / 2;
        int slot = (row * 9) + col;

        for (Kit kit : kits) {
            if (kit.GetAvailability() == KitAvailability.Hide || kit.GetAvailability() == KitAvailability.Null) {
                continue;
            }

            boolean isEquipped = currentKit != null && currentKit.GetName().equalsIgnoreCase(kit.GetName());

            String[] rawDesc = kit.GetDesc();
            String[] lore = new String[rawDesc.length + 3];
            for (int i = 0; i < rawDesc.length; i++) {
                lore[i] = "§7" + rawDesc[i];
            }
            lore[rawDesc.length] = "§8───────────";
            
            if (isEquipped) {
                lore[rawDesc.length + 1] = "§a" + (LangManager.get().isThai(getPlayer()) ? "กำลังใช้งาน" : "Equipped");
            } else {
                lore[rawDesc.length + 1] = "§e" + (LangManager.get().isThai(getPlayer()) ? "คลิกเพื่อเลือกสายนี้" : "Click to select this Kit");
            }
            lore[rawDesc.length + 2] = "";

            Material mat = Material.LEATHER_CHESTPLATE; // default fallback
            if (kit.getDisplayMaterial() != null) {
                mat = kit.getDisplayMaterial();
            }

            String displayName = (isEquipped ? "§a§l" : "§e§l") + kit.GetName();
            if (kit.getDisplayColor() != null) {
                displayName = kit.getDisplayColor() + "§l" + kit.GetName();
            }

            // Count players using this kit
            int playerCount = 0;
            if (_game.GetPlayerKits() != null) {
                for (Kit k : _game.GetPlayerKits().values()) {
                    if (k.GetName().equals(kit.GetName())) {
                        playerCount++;
                    }
                }
            }
            if (playerCount > 0) {
                lore[rawDesc.length + 2] = "§b👤 " + playerCount + " " + (LangManager.get().isThai(getPlayer()) ? "คนเลือกสายนี้" : "players selected");
            } else {
                lore[rawDesc.length + 2] = "";
            }

            ItemStack item = ItemStackFactory.Instance.CreateStack(mat, (byte) 0, 1, displayName, lore);
            
            if (isEquipped) {
                ItemMeta meta = item.getItemMeta();
                meta.addEnchant(org.bukkit.enchantments.Enchantment.PROTECTION, 1, true);
                meta.addItemFlags(org.bukkit.inventory.ItemFlag.HIDE_ENCHANTS, org.bukkit.inventory.ItemFlag.HIDE_ATTRIBUTES);
                item.setItemMeta(meta);
            }

            addButton(slot, item, (p, clickType) -> {
                if (isEquipped) {
                    playDenySound(p);
                } else {
                    playAcceptSound(p);
                    _game.SetKit(p, kit, false);
                    if (_game.InProgress()) {
                        kit.ApplyKit(p);
                    }
                    UtilPlayer.message(p, F.main("Kit", LangManager.get().isThai(p) ? "§7เปลี่ยนสายเป็น §a" + kit.GetName() + "§7 แล้ว" : "§7Switched kit to §a" + kit.GetName() + "§7."));
                    p.closeInventory();
                }
            });

            slot++;
            // Wrap to next line if needed
            if (slot % 9 >= 8) {
                row++;
                col = (9 - Math.min(kits.length - (row - 2) * 7, 7)) / 2;
                slot = (row * 9) + col;
            }
        }
    }

    private void updateClock() {
        if (_game == null) return;
        int countdown = _game.GetCountdown();
        if (countdown >= 0 && _game.GetState() != Game.GameState.Live) {
            boolean isThai = LangManager.get().isThai(getPlayer());
            String title = isThai ? "§e§lเกมกำลังจะเริ่ม!" : "§e§lGame Starting Soon!";
            String[] lore = new String[] {
                "§7",
                isThai ? "§fเริ่มเกมใน: §a" + countdown + " §fวินาที" : "§fStarting in: §a" + countdown + " §fseconds",
                "§7"
            };
            ItemStack clock = ItemStackFactory.Instance.CreateStack(Material.CLOCK, (byte) 0, countdown > 0 ? countdown : 1, title, lore);
            getInventory().setItem(4, clock);
        } else {
            getInventory().setItem(4, null); // Clear if no countdown
            ItemStack glass = ItemStackFactory.Instance.CreateStack(Material.CYAN_STAINED_GLASS_PANE, (byte) 3, 1, " ");
            getInventory().setItem(4, glass);
        }
    }

    @Override
    public void refreshItems(Player player) {
        // Redraw the entire page to update player counts and clock
        refresh();
        
        // Auto-close if game started
        if (_game != null && _game.GetState() == Game.GameState.Live) {
            player.closeInventory();
        }
    }

    @Override
    public int refreshRateTicks() {
        return 20; // Every 1 second
    }
}
