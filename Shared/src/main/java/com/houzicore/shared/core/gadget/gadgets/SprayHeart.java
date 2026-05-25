package com.houzicore.shared.core.gadget.gadgets;

import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.block.BlockFace;

import com.houzicore.shared.common.util.C;
import com.houzicore.shared.core.gadget.CosmeticRarity;
import com.houzicore.shared.core.gadget.GadgetManager;
import com.houzicore.shared.core.gadget.types.SprayGadget;

public class SprayHeart extends SprayGadget {

    public SprayHeart(GadgetManager manager) {
        super(manager, "Heart Spray",
                new String[] { C.cWhite + "Spray a lovely heart", C.cWhite + "on any surface!" },
                -2, Material.ROSE_BUSH, (byte) 0, CosmeticRarity.EPIC);
    }

    @Override
    protected void renderSpray(org.bukkit.Location center, BlockFace face, int tick) {
        // Cardioid curve heart shape
        Particle.DustOptions dust = new Particle.DustOptions(Color.RED, 1.0f);

        for (double t = 0; t < Math.PI * 2; t += 0.3) {
            double scale = 0.03;
            double x = scale * 16 * Math.pow(Math.sin(t), 3);
            double y = scale * (13 * Math.cos(t) - 5 * Math.cos(2 * t) - 2 * Math.cos(3 * t) - Math.cos(4 * t));

            double px, py, pz;
            if (face == BlockFace.UP || face == BlockFace.DOWN) {
                px = center.getX() + x;
                py = center.getY();
                pz = center.getZ() + y;
            } else {
                px = center.getX() + (face.getModZ() != 0 ? x : 0);
                py = center.getY() + y;
                pz = center.getZ() + (face.getModX() != 0 ? x : 0);
            }
            center.getWorld().spawnParticle(Particle.DUST, px, py, pz, 1, 0, 0, 0, 0, dust);
        }
    }
}
