package com.houzicore.shared.core.treasure.gui;

import java.util.ArrayList;
import java.util.List;

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
 * Detail page for a single treasure tier.
 * Reached by clicking a chest icon on TreasurePage.
 *
 * Layout (54 slots, 6 rows × 9 cols):
 *
 *  Row 0 (0 -  8): [ACCENT] ×9             ← tier-coloured top accent row
 *  Row 1 (9 - 17): ▓ ▪ ▪ ▪ [CHEST] ▪ ▪ ▪ ▓  ← big chest icon (slot 13)
 *  Row 2 (18- 26): ▓ ▪ ▪ ▪ [INFO]  ▪ ▪ ▪ ▓  ← drop odds / info (slot 22)
 *  Row 3 (27- 35): ▓ ▪ [BUY] ▪ ▪ ▪ [OPEN] ▪ ▓  ← action buttons (29, 33)
 *  Row 4 (36- 44): ▓ ▪ ▪ ▪ [OWN]  ▪ ▪ ▪ ▓  ← owned count (slot 40)
 *  Row 5 (45- 53): ▓ [BACK] ▓ ▓ [COIN] ▓ ▓ ▓ ▓  ← nav (46, 49)
 */
public class TreasureDetailPage extends ShopPageBase<TreasureManager, TreasureShop> {

    // Content slots
    private static final int SLOT_CHEST = 13;
    private static final int SLOT_INFO  = 22;
    private static final int SLOT_BUY   = 29;
    private static final int SLOT_OPEN  = 33;
    private static final int SLOT_OWNED = 40;
    private static final int SLOT_BACK  = 46;
    private static final int SLOT_COIN  = 49;

    private final TreasureType     _type;
    private final TreasureLocation _treasureLocation;
    private final TreasureInventoryService _invService;
    private final TreasurePage     _mainPage;

    public TreasureDetailPage(TreasureManager plugin, TreasureShop shop,
                              TreasureLocation treasureLocation,
                              CoreClientManager clientManager, DonationManager donationManager,
                              TreasureInventoryService invService,
                              TreasureType type, TreasurePage mainPage, Player player) {
        super(plugin, shop, clientManager, donationManager,
              "   " + com.houzicore.shared.common.util.UtilText.toSmallCaps(TreasureLang.get(player, "ui.page.detail_title", "Treasure Details")),
              player, 54);
        _type             = type;
        _treasureLocation = treasureLocation;
        _invService       = invService;
        _mainPage         = mainPage;
        buildPage();
    }

    @Override
    protected void buildPage() {
        boolean isThai = LangManager.get().isThai(getPlayer());
        int owned = _invService.getOwnedCount(getPlayer(), _type);

        Material accentMat = getAccentMaterial(_type);
        ItemStack accent = makeItem(accentMat, " ");
        ItemStack gray   = makeItem(Material.GRAY_STAINED_GLASS_PANE, " ");
        ItemStack black  = makeItem(Material.BLACK_STAINED_GLASS_PANE, " ");

        // ── Full fill (black) ──────────────────────────────────────────
        for (int i = 0; i < 54; i++) getInventory().setItem(i, black);

        // ── Tier accent top row ────────────────────────────────────────
        for (int i = 0; i < 9; i++) getInventory().setItem(i, accent);

        // ── Gray side/bottom frame ─────────────────────────────────────
        for (int i = 45; i < 54; i++) getInventory().setItem(i, gray);
        for (int row = 1; row < 5; row++) {
            getInventory().setItem(row * 9,     gray);
            getInventory().setItem(row * 9 + 8, gray);
        }

        // ── Big Chest icon ─────────────────────────────────────────────
        List<String> chestLore = new ArrayList<>();
        chestLore.add(" ");
        chestLore.add(" §7" + TreasureLang.get(getPlayer(), "ui.labels.tier", "Tier") + ": " + _type.getDisplayName(isThai));
        chestLore.add(" §7" + TreasureLang.get(getPlayer(), "ui.labels.price_each", "Price each") + ": "
                + "§6" + String.format("%,d", _type.getCostCoins()) + " " + com.houzicore.shared.common.util.UtilText.toSmallCaps("Coins"));
        chestLore.add(" ");
        chestLore.addAll(_type.getRarityOddsLore(isThai));
        ItemStack chestItem = makeItem(_type.getMaterial(), _type.getDisplayName(getPlayer()), chestLore);
        UtilInv.addDullEnchantment(chestItem);
        hideInfo(chestItem);
        getInventory().setItem(SLOT_CHEST, chestItem);

        // ── Info item (drop odds) ──────────────────────────────────────
        List<String> infoLore = new ArrayList<>();
        infoLore.add(" ");
        infoLore.addAll(_type.getRarityOddsLore(isThai));
        infoLore.add(" ");
        infoLore.add(" §7💡 " + TreasureLang.get(getPlayer(), "ui.labels.buy_or_open_hint", "Buy or open a chest below"));
        ItemStack infoItem = makeItem(Material.PAPER,
                "§f" + com.houzicore.shared.common.util.UtilText.toSmallCaps(TreasureLang.get(getPlayer(), "ui.info_title", "Chest Info")), infoLore);
        getInventory().setItem(SLOT_INFO, infoItem);

        // ── Buy button ─────────────────────────────────────────────────
        List<String> buyLore = new ArrayList<>();
        buyLore.add(" ");
        buyLore.add(" §7" + TreasureLang.get(getPlayer(), "ui.labels.starting_at", "Starting at") + ": "
                + "§6" + String.format("%,d", _type.getCostCoins()) + " " + com.houzicore.shared.common.util.UtilText.toSmallCaps("Coins"));
        buyLore.add(" §7" + TreasureLang.get(getPlayer(), "ui.labels.bundles", "Bundles") + ": §f"
                + TreasureLang.get(getPlayer(), "ui.bundles", "1x / 5x / 10x"));
        buyLore.add(" ");
        buyLore.add(" §a" + TreasureLang.get(getPlayer(), "ui.actions.choose_quantity", "Click to choose quantity"));
        ItemStack buyItem = makeItem(Material.GOLD_INGOT,
                "§6§l🛒 " + com.houzicore.shared.common.util.UtilText.toSmallCaps(TreasureLang.get(getPlayer(), "ui.actions.buy_chest", "Buy Chest")), buyLore);
        addButton(SLOT_BUY, buyItem, (player, click) -> {
            playAcceptSound(player);
            getShop().openPageForPlayer(player, new TreasureBuyQuantityPage(
                    getPlugin(), getShop(), getClientManager(), getDonationManager(),
                    _invService, _type, this, player));
        });

        // ── Open button ────────────────────────────────────────────────
        if (owned > 0) {
            List<String> openLore = new ArrayList<>();
            openLore.add(" ");
            openLore.add(" §7" + TreasureLang.get(getPlayer(), "ui.labels.available", "Available") + ": §a" + owned + "x");
            openLore.add(" ");
            openLore.add(" §a" + TreasureLang.get(getPlayer(), "ui.actions.click_open", "Click to open"));
            ItemStack openItem = makeItem(Material.NETHER_STAR,
                    "§a§l▶ " + com.houzicore.shared.common.util.UtilText.toSmallCaps(TreasureLang.get(getPlayer(), "ui.actions.open_chest", "Open Chest")), openLore);
            UtilInv.addDullEnchantment(openItem);
            hideInfo(openItem);
            addButton(SLOT_OPEN, openItem, (player, click) -> {
                playAcceptSound(player);
                org.bukkit.Bukkit.getScheduler().runTask(getPlugin().getPlugin(), () -> player.closeInventory());
                _treasureLocation.attemptOpenTreasure(player, _type);
            });
        } else {
            List<String> lockedLore = new ArrayList<>();
            lockedLore.add(" §c" + TreasureLang.get(getPlayer(), "ui.state.no_chests_line_1", "You have no chests of this type."));
            lockedLore.add(" §7" + TreasureLang.get(getPlayer(), "ui.state.no_chests_line_2", "Buy some first."));
            ItemStack lockedItem = makeItem(Material.BARRIER,
                    "§c" + com.houzicore.shared.common.util.UtilText.toSmallCaps(TreasureLang.get(getPlayer(), "ui.state.no_chests_title", "No Chests Available")), lockedLore);
            addButton(SLOT_OPEN, lockedItem, (player, click) -> playDenySound(player));
        }

        // ── Owned count ────────────────────────────────────────────────
        List<String> ownedLore = new ArrayList<>();
        ownedLore.add(" ");
        ownedLore.add(" §7" + TreasureLang.get(getPlayer(), "ui.labels.chests_owned", "Chests owned") + ": "
                + (owned > 0 ? "§a" : "§c") + owned + "x");
        ItemStack ownedItem = makeItem(Material.CHEST,
                "§f" + com.houzicore.shared.common.util.UtilText.toSmallCaps(TreasureLang.get(getPlayer(), "ui.inventory_title", "Chest Storage")), ownedLore);
        getInventory().setItem(SLOT_OWNED, ownedItem);

        // ── Back button ────────────────────────────────────────────────
        List<String> backLore = new ArrayList<>();
        backLore.add(" §7" + TreasureLang.get(getPlayer(), "ui.actions.back_to_selection", "Return to chest selection"));
        ItemStack backItem = makeItem(Material.ARROW,
                "§c« " + TreasureLang.get(getPlayer(), "ui.actions.back", "Back"), backLore);
        addButton(SLOT_BACK, backItem, (player, click) -> {
            _mainPage.refresh();
            getShop().openPageForPlayer(player, _mainPage);
        });

        // ── Coin balance ───────────────────────────────────────────────
        int coins = getDonationManager().Get(getPlayer().getName()) != null
                    ? getDonationManager().Get(getPlayer().getName()).getCoins()
                    : 0;
        List<String> coinLore = new ArrayList<>();
        coinLore.add(" §7" + TreasureLang.get(getPlayer(), "ui.coin_balance", "Your coin balance"));
        ItemStack coinItem = makeItem(Material.SUNFLOWER,
                "§6" + String.format("%,d", coins) + " " + com.houzicore.shared.common.util.UtilText.toSmallCaps("Coins"), coinLore);
        getInventory().setItem(SLOT_COIN, coinItem);
    }

    /** Returns the accent glass-pane colour for a given tier. */
    private static Material getAccentMaterial(TreasureType type) {
        switch (type) {
            case OLD:      return Material.YELLOW_STAINED_GLASS_PANE;
            case ANCIENT:  return Material.ORANGE_STAINED_GLASS_PANE;
            case MYTHICAL: return Material.MAGENTA_STAINED_GLASS_PANE;
            default:       return Material.GRAY_STAINED_GLASS_PANE;
        }
    }

    // ── Util ─────────────────────────────────────────────────────────────────

    private ItemStack makeItem(Material mat, String name) {
        return makeItem(mat, name, new ArrayList<>());
    }

    private ItemStack makeItem(Material mat, String name, List<String> lore) {
        ItemStack item = new ItemStack(mat);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(name);
            meta.setLore(lore);
            item.setItemMeta(meta);
        }
        return item;
    }
}
