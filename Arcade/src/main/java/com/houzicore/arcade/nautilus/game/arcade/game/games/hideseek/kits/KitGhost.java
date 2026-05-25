package com.houzicore.arcade.nautilus.game.arcade.game.games.hideseek.kits;

import com.houzicore.arcade.ArcadeManager;
import com.houzicore.arcade.nautilus.game.arcade.kit.Kit;
import com.houzicore.arcade.nautilus.game.arcade.kit.KitAvailability;
import com.houzicore.arcade.nautilus.game.arcade.kit.Perk;
import com.houzicore.arcade.nautilus.game.arcade.kit.perks.PerkPhaseShift;
import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class KitGhost extends Kit
{
    public KitGhost(ArcadeManager manager)
    {
        super(manager, "Ghost", KitAvailability.Gem, 4000,
                // EN
                new String[] 
                {
                        com.houzicore.shared.common.util.C.cGray + "Mobility Hider that breaks chases through walls.",
                        com.houzicore.shared.common.util.C.cGray + "Phase Shift picks the safest short exit when pressure gets close.",
                        "",
                        com.houzicore.shared.common.util.C.cGreen + "Pros: " + com.houzicore.shared.common.util.C.cWhite + "Strong escape through tight routes",
                        com.houzicore.shared.common.util.C.cRed + "Cons: " + com.houzicore.shared.common.util.C.cWhite + "Teleport sound and particles expose your route"
                }, 
                // TH
                new String[] 
                {
                        com.houzicore.shared.common.util.C.cGray + "สายวาร์ปหนีที่ตัดเส้นไล่ล่าด้วยการทะลุกำแพง",
                        com.houzicore.shared.common.util.C.cGray + "เด่นตอนโดนกดดันใกล้ตัวและต้องรีเซ็ต line-of-sight แบบไม่ติดจุดลงเสี่ยง",
                        "",
                        com.houzicore.shared.common.util.C.cGreen + "Pros: " + com.houzicore.shared.common.util.C.cWhite + "หนีผ่านมุมแคบและกำแพงได้ดีมาก",
                        com.houzicore.shared.common.util.C.cRed + "Cons: " + com.houzicore.shared.common.util.C.cWhite + "เสียงและเอฟเฟกต์ทำให้ยังถูกตามรอยได้"
                }, 
                new Perk[] 
                {
                        new PerkPhaseShift()
                }, 
                EntityType.ENDERMAN,
                new ItemStack(Material.ENDER_PEARL));

        setLanguageKey("ghost");
        for (Perk perk : GetPerks())
        {
            perk.setLanguageKey("phase_shift");
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
