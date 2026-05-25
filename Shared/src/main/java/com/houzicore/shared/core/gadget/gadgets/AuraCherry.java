package com.houzicore.shared.core.gadget.gadgets;

import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.entity.Player;

import com.houzicore.shared.common.util.C;
import com.houzicore.shared.core.gadget.CosmeticRarity;
import com.houzicore.shared.core.gadget.GadgetManager;
import com.houzicore.shared.core.gadget.types.AuraGadget;

public class AuraCherry extends AuraGadget {

    public AuraCherry(GadgetManager manager) {
        super(manager, "Cherry Blossom Aura",
                new String[] { C.cWhite + "Gentle cherry blossom petals", C.cWhite + "drift around you." },
                -2, Material.CHERRY_SAPLING, (byte) 0, CosmeticRarity.RARE);
    }

    @Override
    protected void renderAura(Player player, int tick) {
        // Cherry blossom petals floating around
        if (tick % 3 == 0) {
            double angle = Math.random() * Math.PI * 2;
            double radius = 0.5 + Math.random() * 1.0;
            double x = Math.cos(angle) * radius;
            double z = Math.sin(angle) * radius;
            double y = 0.5 + Math.random() * 1.5;

            player.getWorld().spawnParticle(Particle.CHERRY_LEAVES,
                    player.getLocation().add(x, y, z),
                    1, 0.1, 0.1, 0.1, 0.01);
        }

        // Gentle ground petals
        if (tick % 8 == 0) {
            player.getWorld().spawnParticle(Particle.CHERRY_LEAVES,
                    player.getLocation().add(0, 0.1, 0),
                    3, 0.8, 0.1, 0.8, 0.005);
        }
    }
}
