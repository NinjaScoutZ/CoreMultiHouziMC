package com.houzicore.shared.core.gadget.gadgets;

import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.block.BlockFace;

import com.houzicore.shared.common.util.C;
import com.houzicore.shared.core.gadget.CosmeticRarity;
import com.houzicore.shared.core.gadget.GadgetManager;
import com.houzicore.shared.core.gadget.types.SprayGadget;

public class SpraySkull extends SprayGadget {

    public SpraySkull(GadgetManager manager) {
        super(manager, "Skull Spray",
                new String[] { C.cWhite + "Spray a menacing skull", C.cWhite + "on any surface!" },
                -2, Material.SKELETON_SKULL, (byte) 0, CosmeticRarity.LEGENDARY);
    }

    @Override
    protected void renderSpray(org.bukkit.Location center, BlockFace face, int tick) {
        // Pixel-art skull pattern
        Particle.DustOptions dustWhite = new Particle.DustOptions(Color.WHITE, 1.0f);
        Particle.DustOptions dustBlack = new Particle.DustOptions(Color.fromRGB(30, 30, 30), 0.8f);

        // Skull outline (white) + eye sockets (dark)
        double[][] outline = {
            {-0.2, 0.4}, {-0.1, 0.4}, {0.0, 0.4}, {0.1, 0.4}, {0.2, 0.4},
            {-0.3, 0.3}, {0.3, 0.3},
            {-0.3, 0.2}, {0.3, 0.2},
            {-0.3, 0.1}, {0.3, 0.1},
            {-0.2, 0.0}, {-0.1, 0.0}, {0.0, 0.0}, {0.1, 0.0}, {0.2, 0.0},
            {-0.2, -0.1}, {0.0, -0.1}, {0.2, -0.1}, // teeth gaps
            {-0.2, -0.2}, {-0.1, -0.2}, {0.0, -0.2}, {0.1, -0.2}, {0.2, -0.2},
        };
        double[][] eyes = {
            {-0.15, 0.25}, {0.15, 0.25},
            {-0.15, 0.15}, {0.15, 0.15},
        };

        for (double[] point : outline) {
            spawnAt(center, face, point, dustWhite);
        }
        for (double[] point : eyes) {
            spawnAt(center, face, point, dustBlack);
        }
    }

    private void spawnAt(org.bukkit.Location center, BlockFace face, double[] point, Particle.DustOptions dust) {
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
