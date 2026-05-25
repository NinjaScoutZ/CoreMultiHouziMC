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

public class KitBloodhound extends Kit
{
    public KitBloodhound(ArcadeManager manager)
    {
        super(manager, "Bloodhound", KitAvailability.Gem, 4000,
                // EN
                new String[] 
                {
                        com.houzicore.shared.common.util.C.cGray + "Chase Hunter that turns first contact into a full track.",
                        com.houzicore.shared.common.util.C.cGray + "Use Bloodhound Sense to keep pressure on runners and route breakers.",
                        "",
                        com.houzicore.shared.common.util.C.cGreen + "Pros: " + com.houzicore.shared.common.util.C.cWhite + "Fast pursuit and single-target tracking",
                        com.houzicore.shared.common.util.C.cRed + "Cons: " + com.houzicore.shared.common.util.C.cWhite + "No wide-area reveal or ranged finisher"
                }, 
                // TH
                new String[]
                {
                        com.houzicore.shared.common.util.C.cGray + "นักล่าสายตามรอยที่เปลี่ยนการเห็นครั้งแรกให้เป็นการไล่ต่อเนื่อง",
                        com.houzicore.shared.common.util.C.cGray + "เด่นมากกับเป้าหมายที่เพิ่งหลุดมุมหรือพยายามรีเซ็ตเส้นหนี",
                        "",
                        com.houzicore.shared.common.util.C.cGreen + "Pros: " + com.houzicore.shared.common.util.C.cWhite + "ตามติดเป้าหมายเดี่ยวได้โหดและกดดันต่อเนื่อง",
                        com.houzicore.shared.common.util.C.cRed + "Cons: " + com.houzicore.shared.common.util.C.cWhite + "ไม่เก่งเรื่องเปิดหลายเป้าพร้อมกัน"
                }, 
                new Perk[] 
                {
                        new PerkDummy("Bloodhound Sense", new String[] {
                                "Right-click Bone to lock onto a nearby Hider.",
                                "Best after first contact, when a runner is trying to break your chase route."
                        })
                }, 
                EntityType.WOLF,
                new ItemStack(Material.BONE));

        setLanguageKey("bloodhound");
        for (Perk perk : GetPerks())
        {
            perk.setLanguageKey("bloodhound_sense");
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
