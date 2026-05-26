package com.houzicore.shared.core.cosmetic.ui;

import com.houzicore.shared.common.util.ItemBuilder;
import com.houzicore.shared.core.gadget.CosmeticRarity;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import java.util.ArrayList;
import java.util.List;

public class CosmeticUITemplate {

    private static final MiniMessage mm = MiniMessage.miniMessage();
    private static final String LINE_SEPARATOR = "<dark_gray>▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬</dark_gray>";

    /**
     * Legacy helper to build premium cards for Lobby Cosmetics.
     */
    public static ItemStack buildPremiumCard(Material material, String title, CosmeticRarity rarity, List<String> description, boolean isUnlocked, String costText) {
        return createItem(material, title, "Lobby Cosmetic", rarity, description, isUnlocked, costText);
    }

    /**
     * Modern template to build premium cards for any cosmetic category.
     *
     * @param material   Vanilla item material (e.g. Material.BLAZE_ROD)
     * @param title      Cosmetic item name
     * @param category   Cosmetic category (e.g. "Lobby Gadget", "Pet", "MiniStrike Skin")
     * @param rarity     Rarity tier enum
     * @param infoLines  Lore description lines
     * @param isUnlocked Whether the player has unlocked this cosmetic
     * @param costText   Unlock requirement text (e.g. "5,000 Coins")
     * @return Formatted ItemStack ready for GUI display
     */
    public static ItemStack createItem(Material material, String title, String category, 
                                      CosmeticRarity rarity, List<String> infoLines, 
                                      boolean isUnlocked, String costText) {
        
        String rColor = rarity.getMiniMessageColor();
        String rarityName = rarity.getDisplayName().replace("✦ ", "");

        String formattedTitle = "<bold><" + rColor + ">" + title + "</" + rColor + ">";

        List<String> rawLore = new ArrayList<>();
        rawLore.add(LINE_SEPARATOR);
        rawLore.add("<gray>ระดับความหายาก: </gray><" + rColor + "><bold>" + rarityName + "</bold></" + rColor + ">");
        rawLore.add("<gray>หมวดหมู่: </gray><aqua>" + category + "</aqua>");
        rawLore.add("");
        
        rawLore.add("<white><bold>🔮 คุณสมบัติพิเศษ</bold></white>");
        for (String line : infoLines) {
            rawLore.add("<dark_gray> ▪ </dark_gray><gray>" + line + "</gray>");
        }
        rawLore.add(LINE_SEPARATOR);

        if (isUnlocked) {
            rawLore.add("<green>● ปลดล็อกแล้ว</green> <dark_gray>(คลิกเพื่อเปิดสวมใส่)</dark_gray>");
        } else {
            rawLore.add("<red>🔒 ยังไม่ได้ครอบครอง</red> <dark_gray>(เงื่อนไข: </dark_gray><gold>" + costText + "</gold><dark_gray>)</dark_gray>");
        }

        ItemBuilder builder = new ItemBuilder(material).hideFlags();
        builder.setTitleComponent(mm.deserialize(formattedTitle));
        
        List<Component> componentLore = new ArrayList<>();
        for (String rawLine : rawLore) {
            componentLore.add(mm.deserialize(rawLine));
        }
        builder.setLoreComponents(componentLore);
        
        if (isUnlocked) {
            builder.addGlow();
        }

        return builder.build();
    }

    /**
     * Build a cosmetic display item (e.g. for Pets) with modern MiniMessage/Adventure styling.
     */
    public static ItemStack buildCosmeticCard(
            Material material,
            byte data,
            String title,
            CosmeticRarity rarity,
            List<String> description,
            boolean owned,
            boolean active,
            int ammoCount,
            int cost,
            boolean isThai
    ) {
        String rColor = rarity.getMiniMessageColor();
        String rarityName = rarity.getDisplayName().replace("✦ ", "");

        String formattedTitle;
        if (owned) {
            formattedTitle = active
                    ? "<bold><green>" + title + "</green></bold>"
                    : "<bold><" + rColor + ">" + title + "</" + rColor + ">";
        } else {
            formattedTitle = "<red>" + title + "</red>";
        }

        List<String> rawLore = new ArrayList<>();
        rawLore.add(LINE_SEPARATOR);

        if (owned) {
            if (active) {
                rawLore.add(isThai ? "<green>● กำลังใช้งานอยู่</green>" : "<green>● Currently Active</green>");
            } else {
                rawLore.add(isThai ? "<green>✔ ครอบครองแล้ว</green>" : "<green>✔ Unlocked</green>");
            }
        } else {
            rawLore.add(isThai ? "<red>✖ ยังไม่ได้ครอบครอง</red>" : "<red>✖ Locked</red>");
        }
        rawLore.add("");

        rawLore.add(isThai 
                ? "<gray>ระดับความหายาก: </gray><" + rColor + "><bold>" + rarityName + "</bold></" + rColor + ">"
                : "<gray>Rarity: </gray><" + rColor + "><bold>" + rarityName + "</bold></" + rColor + ">");
        
        rawLore.add("");

        // Description
        for (String line : description) {
            rawLore.add("<gray>" + line + "</gray>");
        }

        if (ammoCount >= 0) {
            rawLore.add("");
            rawLore.add(isThai 
                    ? "<gray>จำนวนกระสุน: </gray><yellow>" + ammoCount + "</yellow>"
                    : "<gray>Ammo: </gray><yellow>" + ammoCount + "</yellow>");
        }

        rawLore.add(LINE_SEPARATOR);

        // Price/Essence info
        if (owned) {
            rawLore.add(isThai
                    ? "<dark_gray>ราคา: " + cost + " เอสเซนส์</dark_gray>"
                    : "<dark_gray>Cost: " + cost + " Essence</dark_gray>");
        } else {
            rawLore.add(isThai
                    ? "<dark_gray>ราคา: </dark_gray><gold>" + cost + " เอสเซนส์</gold>"
                    : "<dark_gray>Cost: </dark_gray><gold>" + cost + " Essence</gold>");
        }
        rawLore.add("");

        if (owned) {
            if (active) {
                rawLore.add(isThai ? "<gray>คลิกเพื่อเก็บสัตว์เลี้ยง</gray>" : "<gray>Click to dismiss pet</gray>");
            } else {
                rawLore.add(isThai ? "<yellow>คลิกเพื่อเสกสัตว์เลี้ยง</yellow>" : "<yellow>Click to summon pet</yellow>");
            }
        } else {
            rawLore.add(isThai ? "<yellow>คลิกเพื่อสั่งซื้อ</yellow>" : "<yellow>Click to purchase</yellow>");
        }

        ItemBuilder builder = new ItemBuilder(material).hideFlags();
        builder.setTitleComponent(mm.deserialize(formattedTitle));

        List<Component> componentLore = new ArrayList<>();
        for (String rawLine : rawLore) {
            componentLore.add(mm.deserialize(rawLine));
        }
        builder.setLoreComponents(componentLore);

        if (active) {
            builder.addGlow();
        }

        return builder.build();
    }

    /**
     * Get hex color or MiniMessage color string for a rarity.
     */
    public static String getRarityHexColor(CosmeticRarity rarity) {
        return rarity.getMiniMessageColor();
    }
}


