package com.houzicore.shared.core.gadget.gadgets;

import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.block.BlockFace;

import com.houzicore.shared.common.util.C;
import com.houzicore.shared.core.gadget.CosmeticRarity;
import com.houzicore.shared.core.gadget.GadgetManager;
import com.houzicore.shared.core.gadget.types.SprayGadget;

public class SprayHouziLogo extends SprayGadget {

    public SprayHouziLogo(GadgetManager manager) {
        super(manager, "Houzi Logo Spray",
                new String[] { C.cWhite + "Show your server pride", C.cWhite + "with the Houzi logo!" },
                -2, Material.GOLD_BLOCK, (byte) 0, CosmeticRarity.LEGENDARY);
    }

    @Override
    protected void renderSpray(org.bukkit.Location center, BlockFace face, int tick) {
        // Simple monkey face/logo pattern
        double[][] logoPattern = {
            // face outline (circle-ish)
            {-0.3, 0.4}, {0.0, 0.5}, {0.3, 0.4},
            {-0.4, 0.1}, {0.4, 0.1},
            {-0.4, -0.2}, {0.4, -0.2},
            {-0.2, -0.4}, {0.0, -0.5}, {0.2, -0.4},
            // eyes
            {-0.15, 0.15}, {0.15, 0.15},
            // nose
            {0.0, 0.0},
            // smile
            {-0.2, -0.2}, {-0.1, -0.25}, {0.0, -0.25}, {0.1, -0.25}, {0.2, -0.2}
        };

        Particle.DustOptions dust = new Particle.DustOptions(Color.fromRGB(218, 165, 32), 1.0f); // Goldenrod

        for (double[] point : logoPattern) {
            double px, py, pz;
            if (face == BlockFace.UP || face == BlockFace.DOWN) {
                px = center.getX() + point[0];
                py = center.getY();
                pz = center.getZ() + point[1];
            } else {
                px = center.getX() + (face.getModZ() != 0 ? point[0] : 0);
                py = center.getY() + point[1];
                pz = center.getZ() + (face.getModX() != 0 ? point[0] : 0);
            }
            center.getWorld().spawnParticle(Particle.DUST, px, py, pz, 1, 0, 0, 0, 0, dust);
        }
    }
}
