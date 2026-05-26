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

    public static ItemStack buildPremiumCard(Material material, String title, CosmeticRarity rarity, List<String> description, boolean isUnlocked, String costText) {
        ItemBuilder builder = new ItemBuilder(material).hideFlags();
        
        String rarityColor = switch (rarity) {
            case COMMON -> "gray";
            case RARE -> "blue";
            case EPIC -> "dark_purple";
            case LEGENDARY -> "#ffaa00";
            case MYTHIC -> "light_purple";
        };

        builder.setTitleComponent(mm.deserialize("<bold><" + rarityColor + ">" + title + "</" + rarityColor + "></bold>"));

        List<Component> lore = new ArrayList<>();
        lore.add(mm.deserialize("<dark_gray>▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬</dark_gray>"));
        lore.add(mm.deserialize("<gray>ระดับความหายาก: </gray><" + rarityColor + "><bold>" + rarity.name() + "</bold></" + rarityColor + ">"));
        lore.add(mm.deserialize("<gray>หมวดหมู่: </gray><aqua>Lobby Cosmetic</aqua>"));
        lore.add(Component.empty());

        lore.add(mm.deserialize("<white><bold>🔮 คุณสมบัติพิเศษ</bold></white>"));
        for (String line : description) {
            lore.add(mm.deserialize("<dark_gray> ▪ </dark_gray><gray>" + line + "</gray>"));
        }
        lore.add(mm.deserialize("<dark_gray>▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬</dark_gray>"));

        if (isUnlocked) {
            lore.add(mm.deserialize("<green>● ครอบครองแล้ว</green> <dark_gray>(คลิกขวาในล็อบบี้เพื่อเปิดใช้งาน)</dark_gray>"));
        } else {
            lore.add(mm.deserialize("<red>🔒 ยังไม่ได้ปลดล็อก</red> <dark_gray>(ต้องใช้: </dark_gray><gold>" + costText + "</gold><dark_gray>)</dark_gray>"));
        }

        builder.setLoreComponents(lore);
        return builder.build();
    }
}
