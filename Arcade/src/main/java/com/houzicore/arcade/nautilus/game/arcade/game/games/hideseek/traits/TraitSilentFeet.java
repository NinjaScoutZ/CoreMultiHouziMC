package com.houzicore.arcade.nautilus.game.arcade.game.games.hideseek.traits;

import org.bukkit.Material;

import com.houzicore.arcade.nautilus.game.arcade.kit.Kit;
import com.houzicore.arcade.nautilus.game.arcade.kit.traits.Trait;

public class TraitSilentFeet extends Trait {

    public TraitSilentFeet() {
        super("hideseek_silentfeet", "Silent Feet", 1000, Material.LEATHER_BOOTS, 
            "Your footsteps are completely",
            "silent to Hunters when you.",
            "are not disguised.");
    }

    @Override
    public boolean appliesToKit(Kit kit) {
        return kit.GetName().equals("Ghost");
    }
}
