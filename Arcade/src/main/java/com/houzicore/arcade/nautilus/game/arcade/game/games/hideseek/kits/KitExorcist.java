package com.houzicore.arcade.nautilus.game.arcade.game.games.hideseek.kits;

import com.houzicore.arcade.ArcadeManager;
import com.houzicore.arcade.nautilus.game.arcade.kit.Kit;
import com.houzicore.arcade.nautilus.game.arcade.kit.KitAvailability;
import com.houzicore.arcade.nautilus.game.arcade.kit.Perk;
import com.houzicore.arcade.nautilus.game.arcade.game.games.hideseek.traits.TraitPurgePulse;
import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class KitExorcist extends Kit
{
    public KitExorcist(ArcadeManager manager)
    {
        super(manager, "Exorcist", KitAvailability.Gem, 7000,
                new String[]
                {
                        com.houzicore.shared.common.util.C.cGray + "Counter-utility Hunter that punishes lingering Hider magic.",
                        com.houzicore.shared.common.util.C.cGray + "Best used after decoys, traces, or recent skill effects appear.",
                        "",
                        com.houzicore.shared.common.util.C.cGreen + "Pros: " + com.houzicore.shared.common.util.C.cWhite + "Purges Decoys & reveals recent skill users",
                        com.houzicore.shared.common.util.C.cRed + "Cons: " + com.houzicore.shared.common.util.C.cWhite + "Needs timing and close-range positioning"
                },
                new String[]
                {
                        com.houzicore.shared.common.util.C.cGray + "นักล่าสาย counter-utility ที่ล้างร่องรอยสกิลของ Hider",
                        com.houzicore.shared.common.util.C.cGray + "ยิ่งใช้หลังเจอ decoy หรือ trace ใหม่ ๆ ยิ่งได้ value สูง",
                        "",
                        com.houzicore.shared.common.util.C.cGreen + "Pros: " + com.houzicore.shared.common.util.C.cWhite + "ล้าง Decoy และเปิดเผยคนที่เพิ่งใช้สกิล",
                        com.houzicore.shared.common.util.C.cRed + "Cons: " + com.houzicore.shared.common.util.C.cWhite + "ต้องอ่านจังหวะและอยู่ระยะใกล้พอ"
                },
                new Perk[]
                {
                        new TraitPurgePulse()
                },
                EntityType.EVOKER,
                new ItemStack(Material.AMETHYST_SHARD));

        setLanguageKey("exorcist");
        for (Perk perk : GetPerks())
        {
            perk.setLanguageKey("purge_pulse");
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
