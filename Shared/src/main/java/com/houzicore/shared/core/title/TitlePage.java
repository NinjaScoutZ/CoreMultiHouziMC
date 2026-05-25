package com.houzicore.shared.core.title;

import java.util.List;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.meta.ItemMeta;

import com.houzicore.shared.account.CoreClientManager;
import com.houzicore.shared.common.Rank;
import com.houzicore.shared.common.util.C;
import com.houzicore.shared.common.util.F;
import com.houzicore.shared.common.util.GuiUtil;
import com.houzicore.shared.common.util.HouziColorParser;
import com.houzicore.shared.core.donation.DonationManager;
import com.houzicore.shared.core.lang.LangManager;
import com.houzicore.shared.core.shop.item.IButton;
import com.houzicore.shared.core.shop.item.ShopItem;
import com.houzicore.shared.core.shop.page.ShopPageBase;

public class TitlePage extends ShopPageBase<TitleManager, TitleShop> {

    private static final IButton NO_OP = new IButton() {
        @Override public void onClick(Player player, ClickType clickType) {}
    };

    public TitlePage(TitleManager plugin, TitleShop shop, CoreClientManager clientManager,
            DonationManager donationManager, String name, Player player) {
        super(plugin, shop, clientManager, donationManager, name, player, 54);
        buildPage();
    }

    @Override
    protected void buildPage() {
        clear();
        GuiUtil.fillBorders(getInventory());

        final Player p = getPlayer();
        final boolean thai = LangManager.get() != null && LangManager.get().isThai(p);
        final String lang = thai ? "TH" : "EN";

        // Title Header (Slot 13)
        String menuTitle = thai ? "เมนูฉายา" : "Title Menu";
        addButton(13, new ShopItem(Material.NAME_TAG, (byte) 0,
                ChatColor.LIGHT_PURPLE + "" + ChatColor.BOLD + menuTitle,
                new String[]{ChatColor.GRAY + (thai ? "เลือกและติดตั้งฉายาใต้ชื่อของคุณ" : "Select and equip titles below your name tag")}, 1, false, false), NO_OP);

        // Close Button (Slot 16)
        addButton(16, new ShopItem(Material.BARRIER, ChatColor.RED + "" + ChatColor.BOLD
                + (thai ? "ปิดเมนู" : "Close"), new String[]{ChatColor.GRAY + (thai ? "คลิกเพื่อปิดเมนูนี" : "Click to close this menu")}, 1, false),
                new IButton() {
                    @Override public void onClick(Player player, ClickType clickType) { player.closeInventory(); }
                });

        // Dividers / Glass partition
        GuiUtil.fillRow(getInventory(), 18, Material.BLUE_STAINED_GLASS_PANE, " ");
        GuiUtil.fillRow(getInventory(), 36, Material.BLUE_STAINED_GLASS_PANE, " ");

        // Re-add border corners that might have been overwritten by dividers
        getInventory().setItem(18, new ShopItem(Material.BLACK_STAINED_GLASS_PANE, " ", 1, false));
        getInventory().setItem(26, new ShopItem(Material.BLACK_STAINED_GLASS_PANE, " ", 1, false));
        getInventory().setItem(36, new ShopItem(Material.BLACK_STAINED_GLASS_PANE, " ", 1, false));
        getInventory().setItem(44, new ShopItem(Material.BLACK_STAINED_GLASS_PANE, " ", 1, false));

        // Let's lay out the 7 level-based / free titles:
        // Slots 19 to 25
        TitleType[] levelTitles = {
            TitleType.NEWCOMER,
            TitleType.ADVENTURER,
            TitleType.FIGHTER,
            TitleType.BRAVE,
            TitleType.COMMANDER,
            TitleType.LEGEND,
            TitleType.TRANSCENDENT
        };

        for (int i = 0; i < levelTitles.length; i++) {
            final TitleType title = levelTitles[i];
            int itemSlot = 19 + i;
            int indicatorSlot = 28 + i; // Directly below item slot

            addTitleButton(itemSlot, indicatorSlot, title, thai, lang);
        }

        // Let's lay out the 3 staff/rank-based titles:
        // Slots 38, 40, 42
        TitleType[] staffTitles = {
            TitleType.STAFF_BUILDER,
            TitleType.STAFF_MOD,
            TitleType.STAFF_DEV
        };

        int[] staffItemSlots = {38, 40, 42};
        int[] staffIndicatorSlots = {47, 49, 51};

        for (int i = 0; i < staffTitles.length; i++) {
            final TitleType title = staffTitles[i];
            int itemSlot = staffItemSlots[i];
            int indicatorSlot = staffIndicatorSlots[i];

            addTitleButton(itemSlot, indicatorSlot, title, thai, lang);
        }
    }

    private void addTitleButton(int itemSlot, int indicatorSlot, final TitleType title, boolean thai, String lang) {
        final Player p = getPlayer();
        boolean owned = getPlugin().hasTitle(p, title);
        TitleType equipped = getPlugin().getEquippedTitle(p);
        boolean isActive = equipped == title;

        Material mat = getMaterialForTitle(title);

        // Build Title Display Name and formatted preview
        String titleName = title.getDisplayName(lang);
        String previewText = title.getIcon() + " " + titleName;
        String previewFormat = "<GRADIENT:" + title.getGradient() + ">" + previewText + "</GRADIENT>";
        String titleFormatted = HouziColorParser.parse(previewFormat);

        // Construct Lore details
        String reqText = "";
        if (title.getUnlockType() == TitleType.UnlockType.FREE) {
            reqText = thai ? "ทุกคนสามารถใช้งานได้" : "Available to everyone";
        } else if (title.getUnlockType() == TitleType.UnlockType.LEVEL) {
            reqText = (thai ? "เลเวล " : "Level ") + title.getRequiredLevel();
        } else if (title.getUnlockType() == TitleType.UnlockType.RANK) {
            reqText = (thai ? "ยศ " : "Rank ") + title.getRequiredRank().name();
        }

        String statusText;
        if (isActive) {
            statusText = ChatColor.GOLD + (thai ? "ติดตั้งอยู่" : "Equipped");
        } else if (owned) {
            statusText = ChatColor.GREEN + (thai ? "ปลดล็อคแล้ว" : "Unlocked");
        } else {
            statusText = ChatColor.RED + (thai ? "ยังไม่ปลดล็อค" : "Locked");
        }

        String[] lore = {
            ChatColor.DARK_GRAY + "----------------------",
            ChatColor.GRAY + (thai ? "ฉายา: " : "Title: ") + titleFormatted,
            ChatColor.GRAY + (thai ? "เงื่อนไข: " : "Requirement: ") + ChatColor.WHITE + reqText,
            ChatColor.GRAY + (thai ? "สถานะ: " : "Status: ") + statusText,
            ChatColor.DARK_GRAY + "----------------------",
            owned ? (isActive ? ChatColor.YELLOW + (thai ? "คลิกเพื่อถอดฉายา" : "Click to unequip") : ChatColor.GREEN + (thai ? "คลิกเพื่อติดตั้ง" : "Click to equip")) : ChatColor.RED + (thai ? "เงื่อนไขการปลดล็อคไม่เพียงพอ" : "Requirements not met")
        };

        // Create Item
        ShopItem item = new ShopItem(mat, titleName, lore, 1, !owned, true);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(titleFormatted);
        item.setItemMeta(meta);

        if (isActive) {
            item.addGlow();
        }

        // Add item button click listener
        IButton clickHandler = new IButton() {
            @Override
            public void onClick(Player player, ClickType clickType) {
                if (!owned) {
                    playDenySound(player);
                    return;
                }

                if (isActive) {
                    getPlugin().unequipTitle(player);
                    player.sendMessage(F.main(thai ? "ระบบฉายา" : "Title System", thai ? "ถอดฉายาเรียบร้อยแล้ว" : "Unequipped title successfully."));
                } else {
                    getPlugin().equipTitle(player, title);
                    player.sendMessage(F.main(thai ? "ระบบฉายา" : "Title System", (thai ? "ติดตั้งฉายา " : "Equipped title ") + titleFormatted + C.cGreen + (thai ? " เรียบร้อยแล้ว" : " successfully.")));
                }

                playAcceptSound(player);
                refresh();
            }
        };

        addButton(itemSlot, item, clickHandler);

        // Add Indicator Pane
        Material indicatorMat;
        String indicatorName;
        if (isActive) {
            indicatorMat = Material.YELLOW_STAINED_GLASS_PANE;
            indicatorName = ChatColor.GOLD + (thai ? "ติดตั้งอยู่" : "Equipped");
        } else if (owned) {
            indicatorMat = Material.LIME_STAINED_GLASS_PANE;
            indicatorName = ChatColor.GREEN + (thai ? "ติดตั้ง" : "Click to Equip");
        } else {
            indicatorMat = Material.RED_STAINED_GLASS_PANE;
            indicatorName = ChatColor.RED + (thai ? "ยังไม่ปลดล็อค" : "Locked");
        }

        ShopItem indicator = new ShopItem(indicatorMat, indicatorName, 1, false);
        addButton(indicatorSlot, indicator, clickHandler);
    }

    private Material getMaterialForTitle(TitleType title) {
        return switch (title) {
            case NEWCOMER -> Material.OAK_SAPLING;
            case ADVENTURER -> Material.COMPASS;
            case FIGHTER -> Material.IRON_SWORD;
            case BRAVE -> Material.BLAZE_POWDER;
            case COMMANDER -> Material.SHIELD;
            case LEGEND -> Material.NETHER_STAR;
            case TRANSCENDENT -> Material.GOLDEN_HELMET;
            case STAFF_BUILDER -> Material.IRON_PICKAXE;
            case STAFF_MOD -> Material.LIGHTNING_ROD;
            case STAFF_DEV -> Material.DIAMOND;
        };
    }
}
