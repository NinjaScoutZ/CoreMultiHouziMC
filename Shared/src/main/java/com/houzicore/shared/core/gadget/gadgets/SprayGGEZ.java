package com.houzicore.shared.core.gadget.gadgets;

import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.block.BlockFace;

import com.houzicore.shared.common.util.C;
import com.houzicore.shared.core.gadget.CosmeticRarity;
import com.houzicore.shared.core.gadget.GadgetManager;
import com.houzicore.shared.core.gadget.types.SprayGadget;

public class SprayGGEZ extends SprayGadget {

    public SprayGGEZ(GadgetManager manager) {
        super(manager, "GG EZ Spray",
                new String[] { C.cWhite + "The ultimate taunt", C.cWhite + "to leave your mark after a win." },
                -2, Material.LIME_DYE, (byte) 0, CosmeticRarity.RARE);
    }

    @Override
    protected void renderSpray(org.bukkit.Location center, BlockFace face, int tick) {
        // Pattern for "EZ" to make it fit in small block face easily
        double[][] textPattern = {
            // E
            {-0.4, 0.3}, {-0.3, 0.3}, {-0.2, 0.3},
            {-0.4, 0.2},
            {-0.4, 0.1}, {-0.3, 0.1}, {-0.2, 0.1},
            {-0.4, 0.0},
            {-0.4, -0.1}, {-0.3, -0.1}, {-0.2, -0.1},

            // Z
            {0.1, 0.3}, {0.2, 0.3}, {0.3, 0.3}, {0.4, 0.3},
            {0.3, 0.2}, {0.2, 0.1}, {0.1, 0.0},
            {0.1, -0.1}, {0.2, -0.1}, {0.3, -0.1}, {0.4, -0.1}
        };

        Particle.DustOptions dust = new Particle.DustOptions(Color.fromRGB(50, 255, 50), 1.0f); // Bright green

        for (double[] point : textPattern) {
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
