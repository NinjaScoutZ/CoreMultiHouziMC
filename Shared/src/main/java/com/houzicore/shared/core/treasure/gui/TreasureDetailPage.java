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
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import com.houzicore.shared.common.util.ItemBuilder;

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
        net.kyori.adventure.text.minimessage.MiniMessage mm = net.kyori.adventure.text.minimessage.MiniMessage.miniMessage();
        String frameSeparator = "<dark_gray>▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬</dark_gray>";

        Material accentMat = getAccentMaterial(_type);
        ItemStack accent = makeItem(accentMat, " ");
        ItemStack gray   = makeItem(Material.GRAY_STAINED_GLASS_PANE, " ");
        ItemStack black  = makeItem(Material.BLACK_STAINED_GLASS_PANE, " ");

        for (int i = 0; i < 54; i++) getInventory().setItem(i, black);
        for (int i = 0; i < 9; i++) getInventory().setItem(i, accent);
        for (int i = 45; i < 54; i++) getInventory().setItem(i, gray);
        for (int row = 1; row < 5; row++) {
            getInventory().setItem(row * 9,     gray);
            getInventory().setItem(row * 9 + 8, gray);
        }

        // ── Big Chest Icon Refactor (สลอตไอเท็มโชว์ใบประกาศสเตตัสเรทกล่อง) ──
        ItemStack chestItem = new ItemStack(_type.getMaterial());
        var chestMeta = chestItem.getItemMeta();
        if (chestMeta != null) {
            chestMeta.displayName(mm.deserialize("<gradient:#ffcc00:#ffaa00><bold>🎁 " + _type.getDisplayName(isThai) + "</bold></gradient>"));
            
            List<net.kyori.adventure.text.Component> chestLore = new ArrayList<>();
            chestLore.add(mm.deserialize(frameSeparator));
            chestLore.add(mm.deserialize(isThai ? "<gray>หมวดหมู่สินค้า: </gray><yellow>กล่องสมบัติล็อบบี้ส่วนกลาง</yellow>" : "<gray>Category: </gray><yellow>Lobby Treasure Chest</yellow>"));
            chestLore.add(mm.deserialize(isThai ? "<gray>ราคาต้นทุนต่อใบ: </gray><gold>" + String.format("%,d", _type.getCostCoins()) + " คอยน์</gold>" : "<gray>Cost per Chest: </gray><gold>" + String.format("%,d", _type.getCostCoins()) + " Coins</gold>"));
            chestLore.add(Component.empty());
            chestLore.add(mm.deserialize(isThai ? "<white><bold>📊 อัตราการสุ่มเปิดเจอไอเท็มแรร์:</bold></white>" : "<white><bold>📊 Rare Item Random Odds:</bold></white>"));
            for (String oddLine : _type.getRarityOddsLore(isThai)) {
                chestLore.add(mm.deserialize("<dark_gray> ▪ </dark_gray>" + oddLine.replace("§7", "<gray>").replace("§6", "<gold>").replace("§b", "<aqua>")));
            }
            chestLore.add(mm.deserialize(frameSeparator));
            chestMeta.lore(chestLore);
            chestItem.setItemMeta(chestMeta);
        }
        UtilInv.addDullEnchantment(chestItem);
        hideInfo(chestItem);
        getInventory().setItem(SLOT_CHEST, chestItem);

        // ── Buy Button Refactor (ปุ่ม🛒เลือกจำนวนสั่งซื้อคราฟต์พรีเมียม) ──
        ItemStack buyItem = new ItemStack(Material.GOLD_INGOT);
        var buyMeta = buyItem.getItemMeta();
        if (buyMeta != null) {
            buyMeta.displayName(mm.deserialize(isThai ? "<gradient:#ff5555:#ffaa00><bold>🛒 สั่งซื้อกล่องสมบัติเพิ่มเติม</bold></gradient>" : "<gradient:#ff5555:#ffaa00><bold>🛒 Purchase Additional Chests</bold></gradient>"));
            buyMeta.lore(List.of(
                Component.empty(),
                mm.deserialize(isThai ? "<gray>• ราคาเริ่มต้น: <gold>" + String.format("%,d", _type.getCostCoins()) + " คอยน์</gold></gray>" : "<gray>• Starting Price: <gold>" + String.format("%,d", _type.getCostCoins()) + " Coins</gold></gray>"),
                mm.deserialize(isThai ? "<gray>• ชุดแพ็คเกจโบนัส: <white>ซื้อเหมา 1 ใบ / 5 ใบ / 10 ใบ</white></gray>" : "<gray>• Bonus Packages: <white>Bundles of 1 / 5 / 10 Chests</white></gray>"),
                Component.empty(),
                mm.deserialize(isThai ? "<yellow>» คลิกเพื่อระบุจำนวนสินค้าที่ต้องการสั่งซื้อ</yellow>" : "<yellow>» Click to choose purchase quantity</yellow>")
            ));
            buyItem.setItemMeta(buyMeta);
        }
        hideInfo(buyItem);
        addButton(SLOT_BUY, buyItem, (player, click) -> {
            playAcceptSound(player);
            getShop().openPageForPlayer(player, new TreasureBuyQuantityPage(
                    getPlugin(), getShop(), getClientManager(), getDonationManager(),
                    _invService, _type, this, player));
        });

        // ── Open Button Refactor (ปุ่มเปิดกล่อง/ล็อกสถานะ) ──
        if (owned > 0) {
            ItemStack openItem = new ItemStack(Material.NETHER_STAR);
            var openMeta = openItem.getItemMeta();
            if (openMeta != null) {
                openMeta.displayName(mm.deserialize(isThai ? "<gradient:#a8ff78:#78ffd6><bold>▶ ปลดปล่อยคาถาเปิดกล่องสมบัติ</bold></gradient>" : "<gradient:#a8ff78:#78ffd6><bold>▶ Unlock Treasure Chest</bold></gradient>"));
                openMeta.lore(List.of(
                    Component.empty(),
                    mm.deserialize(isThai ? "<gray>• คลังกล่องที่มีในครอบครอง: <green><bold>" + owned + " ใบ</bold></green></gray>" : "<gray>• Available Chests Owned: <green><bold>" + owned + " Chests</bold></green></gray>"),
                    Component.empty(),
                    mm.deserialize(isThai ? "<green>» คลิกเพื่อร่ายอนิเมชันเปิดกล่องลุ้นของแรร์ระดับสากล!</green>" : "<green>» Click to play opening animation and claim rewards!</green>")
                ));
                openItem.setItemMeta(openMeta);
            }
            UtilInv.addDullEnchantment(openItem);
            hideInfo(openItem);
            addButton(SLOT_OPEN, openItem, (player, click) -> {
                playAcceptSound(player);
                org.bukkit.Bukkit.getScheduler().runTask(getPlugin().getPlugin(), () -> player.closeInventory());
                _treasureLocation.attemptOpenTreasure(player, _type);
            });
        } else {
            ItemStack lockedItem = new ItemStack(Material.BARRIER);
            var lockMeta = lockedItem.getItemMeta();
            if (lockMeta != null) {
                lockMeta.displayName(mm.deserialize(isThai ? "<red><bold>✖ ไม่สามารถเปิดกล่องสมบัติได้</bold></red>" : "<red><bold>✖ Chest Unavailable</bold></red>"));
                lockMeta.lore(List.of(
                    Component.empty(),
                    mm.deserialize(isThai ? "<gray>คุณไม่มีไอเท็มกล่องประเภทนี้เหลืออยู่ในคลังเก็บของ</gray>" : "<gray>You have no chests of this type in your storage</gray>"),
                    mm.deserialize(isThai ? "<gray>กรุณากดซื้อแพ็คเกจผ่านปุ่มรถเข็นซ้ายมือเพื่อสะสมก่อน</gray>" : "<gray>Please purchase chests using the cart button on the left</gray>")
                ));
                lockedItem.setItemMeta(lockMeta);
            }
            hideInfo(lockedItem);
            addButton(SLOT_OPEN, lockedItem, (player, click) -> playDenySound(player));
        }

        // ── Storage count & Navigation footer (ช่องคลังพัสดุและปุ่มกลับ) ──
        ItemStack ownedItem = new ItemBuilder(Material.CHEST)
                .setTitle(isThai ? "§f📦 คลังเก็บรักษาสมบัติส่วนตัว" : "§fChest Storage")
                .addLore("", isThai ? " §7จำนวนกล่องที่มีทั้งหมด: " + (owned > 0 ? "§a" : "§c") + owned + " ใบ" : " §7Chests owned: " + owned)
                .build();
        hideInfo(ownedItem);
        getInventory().setItem(SLOT_OWNED, ownedItem);

        ItemStack backItem = new ItemBuilder(Material.ARROW)
                .setTitle(isThai ? "§c« ย้อนกลับ" : "§c« Back")
                .addLore(isThai ? " §7กลับไปยังหน้ารายการเลือกประเภทกล่อง" : " §7Return to chest selection")
                .build();
        hideInfo(backItem);
        addButton(SLOT_BACK, backItem, (player, click) -> {
            _mainPage.refresh();
            getShop().openPageForPlayer(player, _mainPage);
        });

        int coins = getDonationManager().Get(getPlayer().getName()) != null
                    ? getDonationManager().Get(getPlayer().getName()).getCoins()
                    : 0;
        ItemStack coinItem = new ItemBuilder(Material.SUNFLOWER)
                .setTitleComponent(mm.deserialize("<bold><gold>🪙 กระเป๋าเงิน: " + String.format("%,d", coins) + " คอยน์</gold></bold>"))
                .build();
        hideInfo(coinItem);
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
