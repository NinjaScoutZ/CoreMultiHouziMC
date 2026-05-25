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

public class KitChameleon extends Kit
{
    public KitChameleon(ArcadeManager manager)
    {
        super(manager, "Chameleon", KitAvailability.Free,
                // EN
                new String[] 
                {
                        com.houzicore.shared.common.util.C.cGray + "Baseline Hider that wins by blending into the room.",
                        com.houzicore.shared.common.util.C.cGray + "Use Decoy to make Hunters waste checks on the wrong prop.",
                        "",
                        com.houzicore.shared.common.util.C.cGreen + "Pros: " + com.houzicore.shared.common.util.C.cWhite + "Strong deception and room-read pressure",
                        com.houzicore.shared.common.util.C.cRed + "Cons: " + com.houzicore.shared.common.util.C.cWhite + "No escape tool once revealed"
                }, 
                // TH
                new String[] 
                {
                        com.houzicore.shared.common.util.C.cGray + "สายพื้นฐานของ Hider ที่ชนะด้วยการเนียนไปกับฉาก",
                        com.houzicore.shared.common.util.C.cGray + "ใช้ Decoy หลอกให้ Hunter เช็กผิดและเสียจังหวะ",
                        "",
                        com.houzicore.shared.common.util.C.cGreen + "Pros: " + com.houzicore.shared.common.util.C.cWhite + "เก่งเรื่องหลอกเช็กห้องและบังคับให้ตีพลาด",
                        com.houzicore.shared.common.util.C.cRed + "Cons: " + com.houzicore.shared.common.util.C.cWhite + "ถ้าโดนเปิดตำแหน่งแล้วจะหนียาก"
                }, 
                new Perk[] 
                {
                        new PerkDummy("Decoy", new String[] {
                                "Right-click Armor Stand to place a fake prop check.",
                                "Best for baiting spam-hits or covering your real hiding spot."
                        })
                }, 
                EntityType.SLIME,
                new ItemStack(Material.SLIME_BALL));
        
        setLanguageKey("chameleon");
        for (Perk perk : GetPerks()) {
            if (perk.GetName().equals("Decoy")) {
                perk.setLanguageKey("decoy");
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
