package com.houzicore.arcade.nautilus.game.arcade.game.games.hideseek.traits;

import org.bukkit.Material;

import com.houzicore.arcade.nautilus.game.arcade.kit.Kit;
import com.houzicore.arcade.nautilus.game.arcade.kit.traits.Trait;

public class TraitBrutalHit extends Trait {

    public TraitBrutalHit() {
        super("hideseek_brutalhit", "Brutal Hit", 1000, Material.IRON_SWORD, 
            "The very first hit you land",
            "on a Hider deals +3 Damage.",
            "Make your opening strike count!");
    }

    @Override
    public boolean appliesToKit(Kit kit) {
        return kit.GetName().equals("Tracker") || kit.GetName().equals("Trapper");
    }
}
