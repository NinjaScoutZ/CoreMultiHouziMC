package com.houzicore.shared.core.treasure.gui;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import com.houzicore.shared.account.CoreClientManager;
import com.houzicore.shared.core.donation.DonationManager;
import com.houzicore.shared.core.lang.LangManager;
import com.houzicore.shared.core.shop.page.ShopPageBase;
import com.houzicore.shared.common.util.UtilInv;
import com.houzicore.shared.core.treasure.TreasureInventoryService;
import com.houzicore.shared.core.treasure.TreasureLang;
import com.houzicore.shared.core.treasure.TreasureLocation;
import com.houzicore.shared.core.treasure.TreasureManager;
import com.houzicore.shared.core.treasure.TreasureType;

/**
 * Main Treasure page — choose one of three chest tiers.
 */
public class TreasurePage extends ShopPageBase<TreasureManager, TreasureShop> {

    private static final int SLOT_OLD      = 19;
    private static final int SLOT_ANCIENT  = 21;
    private static final int SLOT_MYTHICAL = 23;
    private static final int SLOT_IMMORTAL = 25;
    private static final int SLOT_DIVINE   = 31;
    private static final int SLOT_COIN = 49;

    private final TreasureLocation _treasureLocation;
    private final TreasureInventoryService _invService;

    public TreasurePage(TreasureManager plugin, TreasureShop shop, TreasureLocation treasureLocation,
                        CoreClientManager clientManager, DonationManager donationManager,
                        TreasureInventoryService invService, Player player) {
        super(plugin, shop, clientManager, donationManager,
              "   " + com.houzicore.shared.common.util.UtilText.toSmallCaps(TreasureLang.get(player, "ui.page.main_title", "Treasure Vault")),
              player, 54);
        _treasureLocation = treasureLocation;
        _invService = invService;
        buildPage();
    }

    @Override
    protected void buildPage() {
        boolean isThai = LangManager.get().isThai(getPlayer());

        ItemStack gray  = makeItem(Material.GRAY_STAINED_GLASS_PANE,  " ");
        ItemStack black = makeItem(Material.BLACK_STAINED_GLASS_PANE, " ");

        for (int i = 0; i < 54; i++) getInventory().setItem(i, black);

        for (int i = 0; i < 9; i++)  getInventory().setItem(i, gray);
        for (int i = 45; i < 54; i++) getInventory().setItem(i, gray);
        for (int row = 1; row < 5; row++) {
            getInventory().setItem(row * 9,     gray);
            getInventory().setItem(row * 9 + 8, gray);
        }
        for (int row = 1; row < 5; row++) {
            getInventory().setItem(row * 9 + 3, gray);
            getInventory().setItem(row * 9 + 6, gray);
        }

        List<String> titleLore = new ArrayList<>();
        titleLore.add(" ");
        titleLore.add(" §7" + TreasureLang.get(getPlayer(), "ui.subtitle", "Select a chest to inspect or open."));
        ItemStack titleItem = makeItem(Material.CHEST, "§6§l" + com.houzicore.shared.common.util.UtilText.toSmallCaps(TreasureLang.get(getPlayer(), "ui.title", "Treasure")), titleLore);
        UtilInv.addDullEnchantment(titleItem);
        hideInfo(titleItem);
        getInventory().setItem(4, titleItem);

        renderChestIcon(TreasureType.OLD,      SLOT_OLD,      isThai);
        renderChestIcon(TreasureType.ANCIENT,  SLOT_ANCIENT,  isThai);
        renderChestIcon(TreasureType.MYTHICAL, SLOT_MYTHICAL, isThai);
        renderChestIcon(TreasureType.IMMORTAL, SLOT_IMMORTAL, isThai);
        renderChestIcon(TreasureType.DIVINE,   SLOT_DIVINE,   isThai);

        int coins = getDonationManager().Get(getPlayer().getName()) != null
                    ? getDonationManager().Get(getPlayer().getName()).getCoins()
                    : 0;
        List<String> coinLore = new ArrayList<>();
        coinLore.add(" §7" + TreasureLang.get(getPlayer(), "ui.coin_balance", "Your coin balance"));
        ItemStack coinItem = makeItem(Material.SUNFLOWER,
                "§6" + String.format("%,d", coins) + " " + com.houzicore.shared.common.util.UtilText.toSmallCaps("Coins"), coinLore);
        getInventory().setItem(SLOT_COIN, coinItem);
    }

    private void renderChestIcon(TreasureType type, int slot, boolean isThai) {
        int owned = _invService.getOwnedCount(getPlayer(), type);

        List<String> lore = new ArrayList<>();
        lore.add(" ");
        lore.add(" §7" + TreasureLang.get(getPlayer(), "ui.labels.tier", "Tier") + ": " + type.getDisplayName(isThai));
        lore.add(" §7" + TreasureLang.get(getPlayer(), "ui.labels.owned", "Owned") + ": "
                + (owned > 0 ? "§a" : "§c") + owned + "x");
        lore.add(" §7" + TreasureLang.get(getPlayer(), "ui.labels.price", "Price") + ": "
                + "§6" + String.format("%,d", type.getCostCoins()) + " " + com.houzicore.shared.common.util.UtilText.toSmallCaps("Coins"));
        lore.add(" ");
        lore.addAll(type.getRarityOddsLore(isThai));
        lore.add(" ");
        lore.add(" §a▶ " + TreasureLang.get(getPlayer(), "ui.actions.view_details", "Click to view details"));

        ItemStack item = makeItem(type.getMaterial(), type.getDisplayName(getPlayer()), lore);
        UtilInv.addDullEnchantment(item);
        hideInfo(item);

        addButton(slot, item, (player, click) -> {
            Bukkit.getLogger().info("[ShopDebug] Clicked Chest: " + type.name());
            playAcceptSound(player);
            
            Bukkit.getScheduler().runTask(getPlugin().getPlugin(), () -> {
                try {
                    TreasureDetailPage detailPage = new TreasureDetailPage(getPlugin(), getShop(), _treasureLocation,
                            getClientManager(), getDonationManager(),
                            _invService, type, this, player);
                    Bukkit.getLogger().info("[ShopDebug] Opening detail page...");
                    getShop().openPageForPlayer(player, detailPage);
                } catch (Exception e) {
                    Bukkit.getLogger().severe("[ShopDebug] Error opening detail page: " + e.getMessage());
                    e.printStackTrace();
                }
            });
        });
    }

    private ItemStack makeItem(Material mat, String name) {
        return makeItem(mat, name, new ArrayList<>());
    }

    private ItemStack makeItem(Material mat, String name, List<String> lore) {
        net.kyori.adventure.text.minimessage.MiniMessage mm = net.kyori.adventure.text.minimessage.MiniMessage.miniMessage();
        ItemStack item = new ItemStack(mat);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            if (name != null) {
                if (name.startsWith("§")) {
                    meta.setDisplayName(name);
                } else {
                    meta.displayName(mm.deserialize(name));
                }
            }
            if (lore != null) {
                List<net.kyori.adventure.text.Component> componentLore = new ArrayList<>();
                for (String line : lore) {
                    if (line.startsWith("§")) {
                        componentLore.add(net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer.legacySection().deserialize(line));
                    } else {
                        componentLore.add(mm.deserialize(line));
                    }
                }
                meta.lore(componentLore);
            }
            item.setItemMeta(meta);
        }
        return item;
    }
}
