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
}
