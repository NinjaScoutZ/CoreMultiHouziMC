package com.houzicore.shared.core.gadget.gadgets;

import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.block.BlockFace;

import com.houzicore.shared.common.util.C;
import com.houzicore.shared.core.gadget.CosmeticRarity;
import com.houzicore.shared.core.gadget.GadgetManager;
import com.houzicore.shared.core.gadget.types.SprayGadget;

public class SprayCat extends SprayGadget {

    public SprayCat(GadgetManager manager) {
        super(manager, "Cat Spray",
                new String[] { C.cWhite + "Spray a cute cat face", C.cWhite + "on any surface!" },
                -2, Material.CAT_SPAWN_EGG, (byte) 0, CosmeticRarity.COMMON);
    }

    @Override
    protected void renderSpray(org.bukkit.Location center, BlockFace face, int tick) {
        // Simple cat face pixel pattern using DUST particles
        // Pattern is drawn on the XZ plane (for top face) or rotated for side faces
        double[][] catPattern = {
            {-0.3, 0.4}, {0.3, 0.4},     // ears
            {-0.2, 0.3}, {0.2, 0.3},     // ear inner
            {-0.2, 0.1}, {0.2, 0.1},     // eyes
            {0.0, 0.0},                    // nose
            {-0.1, -0.1}, {0.1, -0.1},   // whiskers start
            {-0.3, -0.1}, {0.3, -0.1},   // whiskers end
            {-0.1, -0.2}, {0.1, -0.2},   // mouth
        };

        Particle.DustOptions dust = new Particle.DustOptions(Color.fromRGB(255, 165, 0), 1.0f);

        for (double[] point : catPattern) {
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
