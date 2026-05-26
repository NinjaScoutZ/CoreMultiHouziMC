package com.houzicore.shared.core.treasure.gui;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import com.houzicore.shared.account.CoreClientManager;
import com.houzicore.shared.common.CurrencyType;
import com.houzicore.shared.common.util.UtilInv;
import com.houzicore.shared.core.donation.DonationManager;
import com.houzicore.shared.core.lang.LangManager;
import com.houzicore.shared.core.shop.page.ConfirmationPage;
import com.houzicore.shared.core.shop.page.ShopPageBase;
import com.houzicore.shared.core.treasure.ChestPackage;
import com.houzicore.shared.core.treasure.TreasureInventoryService;
import com.houzicore.shared.core.treasure.TreasureLang;
import com.houzicore.shared.core.treasure.TreasureManager;
import com.houzicore.shared.core.treasure.TreasureType;

/**
 * 54-slot quantity selector — reached from TreasureDetailPage.
 *
 * Layout (6 rows × 9 cols):
 *
 *  Row 0: [ACCENT] ×9                              ← tier-coloured accent
 *  Row 1: ▓ ▪ ▪ ▪ [CHEST] ▪ ▪ ▪ ▓                ← chest display (slot 13)
 *  Row 2: ▓ ▪ ▪ ▪ [CHEST] ▪ ▪ ▪ ▓                ← chest display continued (slot 22)
 *  Row 3: ▓ ▪ [1x] ▪ ▪ [5x] ▪ ▪ [10x] ▓           ← buy buttons (28, 31, 34)
 *  Row 4: ▓ ▪ ▪ ▪ ▪ ▪ ▪ ▪ ▓                       ← spacer
 *  Row 5: ▓ [BACK] ▓ ▓ [COIN] ▓ ▓ ▓ ▓              ← nav (46, 49)
 */
public class TreasureBuyQuantityPage extends ShopPageBase<TreasureManager, TreasureShop> {

    private static final int SLOT_BUY_1  = 28;
    private static final int SLOT_BUY_5  = 31;
    private static final int SLOT_BUY_10 = 34;
    private static final int SLOT_BACK   = 46;
    private static final int SLOT_COIN   = 49;

    // Chest display: spans centre of rows 1–2
    private static final int[] CHEST_DISPLAY_SLOTS = {13, 22};

    private final TreasureType _type;
    private final TreasureInventoryService _invService;
    private final TreasureDetailPage _detailPage;

    public TreasureBuyQuantityPage(TreasureManager plugin, TreasureShop shop,
                                   CoreClientManager clientManager, DonationManager donationManager,
                                   TreasureInventoryService invService,
                                   TreasureType type, TreasureDetailPage detailPage, Player player) {
        super(plugin, shop, clientManager, donationManager,
              "   " + com.houzicore.shared.common.util.UtilText.toSmallCaps(TreasureLang.get(player, "ui.page.buy_title", "Buy Chests")),
              player, 54);
        _type        = type;
        _invService  = invService;
        _detailPage  = detailPage;
        buildPage();
    }

    @Override
    protected void buildPage() {
        boolean isThai = LangManager.get().isThai(getPlayer());

        Material accentMat = getAccentMaterial(_type);
        ItemStack accent = makeItem(accentMat, " ");
        ItemStack gray   = makeItem(Material.GRAY_STAINED_GLASS_PANE, " ");
        ItemStack black  = makeItem(Material.BLACK_STAINED_GLASS_PANE, " ");

        // ── Full fill (black) ──────────────────────────────────────────
        for (int i = 0; i < 54; i++) getInventory().setItem(i, black);

        // ── Tier accent top row ────────────────────────────────────────
        for (int i = 0; i < 9; i++) getInventory().setItem(i, accent);

        // ── Gray frame ─────────────────────────────────────────────────
        for (int i = 45; i < 54; i++) getInventory().setItem(i, gray);
        for (int row = 1; row < 5; row++) {
            getInventory().setItem(row * 9,     gray);
            getInventory().setItem(row * 9 + 8, gray);
        }

        // ── Chest display ──────────────────────────────────────────────
        List<String> chestLore = new ArrayList<>();
        chestLore.add(" ");
        chestLore.add(" §7" + TreasureLang.get(getPlayer(), "ui.labels.chest", "Chest") + ": " + _type.getDisplayName(isThai));
        chestLore.add(" §7" + TreasureLang.get(getPlayer(), "ui.labels.price_each", "Price each") + ": "
                + "§6" + String.format("%,d", _type.getCostCoins()) + " " + com.houzicore.shared.common.util.UtilText.toSmallCaps("Coins"));
        chestLore.add(" ");
        chestLore.addAll(_type.getRarityOddsLore(isThai));
        chestLore.add(" ");
        chestLore.add(" §7" + TreasureLang.get(getPlayer(), "ui.labels.buy_hint", "Choose a quantity below"));
        ItemStack chestDisplay = makeItem(_type.getMaterial(), _type.getDisplayName(getPlayer()), chestLore);
        UtilInv.addDullEnchantment(chestDisplay);
        hideInfo(chestDisplay);
        for (int s : CHEST_DISPLAY_SLOTS) getInventory().setItem(s, chestDisplay);

        // ── Buy quantity buttons ───────────────────────────────────────
        addQuantityButton(SLOT_BUY_1,  1,  isThai);
        addQuantityButton(SLOT_BUY_5,  5,  isThai);
        addQuantityButton(SLOT_BUY_10, 10, isThai);

        // ── Back button ────────────────────────────────────────────────
        List<String> backLore = new ArrayList<>();
        backLore.add(" §7" + TreasureLang.get(getPlayer(), "ui.actions.back_to_detail", "Return to detail page"));
        addButton(SLOT_BACK, makeItem(Material.ARROW, "§c« " + TreasureLang.get(getPlayer(), "ui.actions.back", "Back"), backLore),
                (player, click) -> {
                    _detailPage.refresh();
                    getShop().openPageForPlayer(player, _detailPage);
                });

        // ── Coin balance ───────────────────────────────────────────────
        int coins = getDonationManager().Get(getPlayer().getName()) != null
                    ? getDonationManager().Get(getPlayer().getName()).getCoins()
                    : 0;
        List<String> coinLore = new ArrayList<>();
        coinLore.add(" §7" + TreasureLang.get(getPlayer(), "ui.coin_balance", "Your coin balance"));
        getInventory().setItem(SLOT_COIN,
                makeItem(Material.SUNFLOWER, "§6" + String.format("%,d", coins) + " " + com.houzicore.shared.common.util.UtilText.toSmallCaps("Coins"), coinLore));
    }

    // ── Helpers ──────────────────────────────────────────────────────────────

    private void addQuantityButton(int slot, int qty, boolean isThai) {
        int totalCost = _type.getCostForQty(qty);
        List<String> lore = new ArrayList<>();
        lore.add(" ");
        lore.add(" §7" + TreasureLang.get(getPlayer(), "ui.labels.quantity", "Quantity") + ": §f" + qty + "x");
        lore.add(" §7" + TreasureLang.get(getPlayer(), "ui.labels.total_cost", "Total cost") + ": "
                + "§6" + String.format("%,d", totalCost) + " " + com.houzicore.shared.common.util.UtilText.toSmallCaps("Coins"));
        lore.add(" ");
        lore.add(" §a" + TreasureLang.get(getPlayer(), "ui.actions.click_purchase", "Click to purchase"));

        Material mat = qty == 1  ? Material.GOLD_INGOT
                     : qty == 5  ? Material.GOLD_BLOCK
                     :             Material.NETHER_STAR;
        String label = "§6§l" + qty + "x §f" + com.houzicore.shared.common.util.UtilText.toSmallCaps(_type.getDisplay(isThai));
        addButton(slot, makeItem(mat, label, lore),
                (player, click) -> openConfirmation(player, qty, totalCost));
    }

    private void openConfirmation(Player player, int qty, int totalCost) {
        playAcceptSound(player);

        ChestPackage pkg = new ChestPackage(
                _type.getPlainName(player) + " x" + qty,
                _type.getMaterial(),
                totalCost);

        getShop().openPageForPlayer(player, new ConfirmationPage<>(
                getPlugin(), getShop(),
                getClientManager(), getDonationManager(),
                () -> {
                    _invService.addChests(player, _type, qty);
                    if (_detailPage != null) _detailPage.refresh();
                },
                _detailPage,
                pkg,
                CurrencyType.Coins,
                player));
    }

    private static Material getAccentMaterial(TreasureType type) {
        switch (type) {
            case OLD:      return Material.YELLOW_STAINED_GLASS_PANE;
            case ANCIENT:  return Material.ORANGE_STAINED_GLASS_PANE;
            case MYTHICAL: return Material.MAGENTA_STAINED_GLASS_PANE;
            default:       return Material.GRAY_STAINED_GLASS_PANE;
        }
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
