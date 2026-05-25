package com.houzicore.arcade.nautilus.game.arcade.game.games.hideseek.traits;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import com.houzicore.arcade.nautilus.game.arcade.kit.Kit;
import com.houzicore.arcade.nautilus.game.arcade.kit.traits.Trait;

public class TraitLightweight extends Trait {

    public TraitLightweight() {
        super("hideseek_lightweight", "Lightweight", 500, Material.FEATHER, 
            "Increase movement speed drastically", 
            "but permanently reduces your",
            "Max Health by -2 Hearts");
    }

    @Override
    public boolean appliesToKit(Kit kit) {
        // Only Hider kits (Not hunters)
        return kit.GetName().equals("Trickster") || kit.GetName().equals("Ghost") || kit.GetName().equals("Swapper"); // basic hiders
    }

    @Override
    public void onEquip(Player player) {
        // Will be applied runtime in HideSeek.java
        // but we can give them Speed I immediately if we want
    }
}
