package com.houzicore.arcade.nautilus.game.arcade.game.games.hideseek.traits;

import org.bukkit.Material;

import com.houzicore.arcade.nautilus.game.arcade.kit.Kit;
import com.houzicore.arcade.nautilus.game.arcade.kit.traits.Trait;

public class TraitUtilityBelt extends Trait {

    public TraitUtilityBelt() {
        super("hideseek_utilitybelt", "Utility Belt", 750, Material.IRON_LEGGINGS, 
            "Reduces the Cooldown of your",
            "main ability by 2 seconds.");
    }

    @Override
    public boolean appliesToKit(Kit kit) {
        return kit.GetName().equals("Saboteur") || kit.GetName().equals("Destroyer");
    }
}
