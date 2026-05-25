package com.houzicore.arcade.nautilus.game.arcade.game.games.hideseek.kits;

import com.houzicore.arcade.ArcadeManager;
import com.houzicore.arcade.nautilus.game.arcade.kit.Kit;
import com.houzicore.arcade.nautilus.game.arcade.kit.KitAvailability;
import com.houzicore.arcade.nautilus.game.arcade.kit.Perk;
import com.houzicore.arcade.nautilus.game.arcade.kit.perks.PerkDummy;
import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class KitMimic extends Kit
{
    public KitMimic(ArcadeManager manager)
    {
        super(manager, "Mimic", KitAvailability.Gem, 6500,
                new String[]
                {
                        com.houzicore.shared.common.util.C.cGray + "Illusion Hider that blinds nearby Hunters before changing position.",
                        com.houzicore.shared.common.util.C.cGray + "Mirror Image buys a short blind window to reroute the chase.",
                        "",
                        com.houzicore.shared.common.util.C.cGreen + "Pros: " + com.houzicore.shared.common.util.C.cWhite + "Good at confusing single-target pursuit",
                        com.houzicore.shared.common.util.C.cRed + "Cons: " + com.houzicore.shared.common.util.C.cWhite + "Illusions fade fast if Hunters wait you out"
                },
                new String[]
                {
                        com.houzicore.shared.common.util.C.cGray + "สายภาพลวงตาที่ทำให้ Hunter ใกล้ตัวหน้ามืดแล้วค่อยเปลี่ยนตำแหน่ง",
                        com.houzicore.shared.common.util.C.cGray + "เด่นเวลาโดนไล่แบบตัวต่อตัวและต้องซื้อเสี้ยววินาทีหนี",
                        "",
                        com.houzicore.shared.common.util.C.cGreen + "Pros: " + com.houzicore.shared.common.util.C.cWhite + "ดึงสายตา Hunter ออกจากเส้นหนีจริงได้ดี",
                        com.houzicore.shared.common.util.C.cRed + "Cons: " + com.houzicore.shared.common.util.C.cWhite + "ภาพลวงหมดเร็วถ้าคู่ต่อสู้ไม่รีบ commit"
                },
                new Perk[]
                {
                        new PerkDummy("Mirror Image", new String[] {
                                "Right-click Echo Shard to create illusions and briefly blind nearby Hunters.",
                                "Use it when Hunters commit close and one wrong read is enough to escape."
                        })
                },
                EntityType.FOX,
                new ItemStack(Material.ECHO_SHARD));

        setLanguageKey("mimic");
        for (Perk perk : GetPerks())
        {
            perk.setLanguageKey("mirror_image");
        }
    
        // Configurator Pattern Override
        com.houzicore.arcade.nautilus.game.arcade.kit.KitConfigLoader.KitConfigData fallback = new com.houzicore.arcade.nautilus.game.arcade.kit.KitConfigLoader.KitConfigData();
        fallback.name = this._kitName;
        fallback.availability = this._kitAvailability;
        fallback.cost = this._cost;
        fallback.entityType = this._entityType;
        fallback.displayItem = this._displayItem;
        fallback.descEn = this._kitDesc;
        fallback.descTh = this._kitDescTh;
        
        com.houzicore.arcade.nautilus.game.arcade.kit.KitConfigLoader.KitConfigData data = com.houzicore.arcade.nautilus.game.arcade.kit.KitConfigLoader.loadKitData(manager.getPlugin(), "proprush-kits.yml", this._kitName.toLowerCase().replace(" ", "_"), fallback);
        
        this._kitName = data.name;
        this._kitAvailability = data.availability;
        this._cost = data.cost;
        this._entityType = data.entityType;
        if (data.displayItem != null) {
            this._displayItem = data.displayItem;
            this._itemInHand = new org.bukkit.inventory.ItemStack(data.displayItem);
        }
        this._kitDesc = data.descEn;
        this._kitDescTh = data.descTh;
}

    @Override
    public void GiveItems(Player player)
    {
    }
}
