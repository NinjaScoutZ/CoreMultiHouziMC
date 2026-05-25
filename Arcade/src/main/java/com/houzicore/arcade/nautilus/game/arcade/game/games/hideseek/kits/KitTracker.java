package com.houzicore.arcade.nautilus.game.arcade.game.games.hideseek.kits;

import com.houzicore.arcade.ArcadeManager;
import com.houzicore.arcade.nautilus.game.arcade.kit.Kit;
import com.houzicore.arcade.nautilus.game.arcade.kit.KitAvailability;
import com.houzicore.arcade.nautilus.game.arcade.kit.Perk;
import com.houzicore.arcade.nautilus.game.arcade.kit.perks.PerkDummy;
import com.houzicore.arcade.nautilus.game.arcade.kit.perks.PerkDoubleJump;
import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class KitTracker extends Kit
{
    public KitTracker(ArcadeManager manager)
    {
        super(manager, "Tracker", KitAvailability.Free,
                // EN
                new String[] 
                {
                        com.houzicore.shared.common.util.C.cGray + "Scout Hunter that narrows the map with rough radar sweeps.",
                        com.houzicore.shared.common.util.C.cGray + "Use Scanner Pulse to read distance and direction, then rotate with Double Jump.",
                        "",
                        com.houzicore.shared.common.util.C.cGreen + "Pros: " + com.houzicore.shared.common.util.C.cWhite + "Reliable information and fast vertical sweep",
                        com.houzicore.shared.common.util.C.cRed + "Cons: " + com.houzicore.shared.common.util.C.cWhite + "Needs follow-up to convert scans into kills"
                }, 
                // TH
                new String[] 
                {
                        com.houzicore.shared.common.util.C.cGray + "นักล่าสาย scout ที่ค่อย ๆ บีบพื้นที่ด้วยเรดาร์แบบบอกทิศคร่าว ๆ",
                        com.houzicore.shared.common.util.C.cGray + "ใช้ Scanner Pulse อ่านระยะกับทิศ แล้วหมุนแมพด้วย Double Jump",
                        "",
                        com.houzicore.shared.common.util.C.cGreen + "Pros: " + com.houzicore.shared.common.util.C.cWhite + "ได้ข้อมูลชัวร์และขึ้นเปลี่ยนชั้นได้คล่อง",
                        com.houzicore.shared.common.util.C.cRed + "Cons: " + com.houzicore.shared.common.util.C.cWhite + "ถ้าทีมไม่ตามข้อมูลก็ปิดงานเองยาก"
                }, 
                new Perk[] 
                {
                        new PerkDummy("Scanner Pulse", new String[] {
                                "Right-click Compass to send 5 radar waves out to 20 blocks.",
                                "Each wave gives only rough distance and direction, so you still need the read."
                        }),
                        new PerkDoubleJump("Double Jump", 1.2, 1.2, false, 4000, true)
                }, 
                EntityType.SKELETON,
                new ItemStack(Material.COMPASS));

        setLanguageKey("tracker");
        for (Perk perk : GetPerks())
        {
            if (perk.GetName().equals("Scanner Pulse"))
            {
                perk.setLanguageKey("scanner_pulse");
            }
            else if (perk.GetName().equals("Double Jump"))
            {
                perk.setLanguageKey("double_jump");
            }
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
