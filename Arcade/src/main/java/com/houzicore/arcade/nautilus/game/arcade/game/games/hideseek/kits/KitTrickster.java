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

public class KitTrickster extends Kit
{
    public KitTrickster(ArcadeManager manager)
    {
        super(manager, "Trickster", KitAvailability.Gem, 6000,
                // EN
                new String[] 
                {
                        com.houzicore.shared.common.util.C.cGray + "Tempo Hider that survives by never running straight.",
                        com.houzicore.shared.common.util.C.cGray + "Blind the pursuer, swap routes, and keep the chase messy.",
                        "",
                        com.houzicore.shared.common.util.C.cGreen + "Pros: " + com.houzicore.shared.common.util.C.cWhite + "Permanent speed and melee blind",
                        com.houzicore.shared.common.util.C.cRed + "Cons: " + com.houzicore.shared.common.util.C.cWhite + "Snow trail makes repeated paths easy to read"
                }, 
                // TH
                new String[] 
                {
                        com.houzicore.shared.common.util.C.cGray + "สายป่วนที่เอาตัวรอดด้วยการเปลี่ยนจังหวะไล่จับ",
                        com.houzicore.shared.common.util.C.cGray + "วิ่งไว ตีตาบอด แล้วต้องรีบสลับเส้นหนีทันที",
                        "",
                        com.houzicore.shared.common.util.C.cGreen + "Pros: " + com.houzicore.shared.common.util.C.cWhite + "ความเร็วสูงและตีคนหาให้เสียมุมมองได้",
                        com.houzicore.shared.common.util.C.cRed + "Cons: " + com.houzicore.shared.common.util.C.cWhite + "รอยหิมะทำให้วิ่งวนเส้นเดิมแล้วโดนอ่านง่าย"
                }, 
                new Perk[] 
                {
                        new PerkDummy("Trickery", new String[] {
                                "Permanent Speed I keeps your chase tempo high.",
                                "Use it to change routes before Hunters can settle their aim."
                        }),
                        new PerkDummy("Blinding Strike", new String[] {
                                "Melee hits briefly Blind Hunters.",
                                "Best used right before cutting a corner or escaping a tight chase."
                        })
                }, 
                EntityType.RABBIT,
                new ItemStack(Material.FEATHER));

        setLanguageKey("trickster");
        for (Perk perk : GetPerks())
        {
            if (perk.GetName().equals("Trickery"))
            {
                perk.setLanguageKey("trickery");
            }
            else if (perk.GetName().equals("Blinding Strike"))
            {
                perk.setLanguageKey("blinding_strike");
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
        player.addPotionEffect(new org.bukkit.potion.PotionEffect(org.bukkit.potion.PotionEffectType.SPEED, Integer.MAX_VALUE, 0));
    }
}
