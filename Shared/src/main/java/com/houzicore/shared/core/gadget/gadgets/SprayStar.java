package com.houzicore.shared.core.gadget.gadgets;

import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.block.BlockFace;

import com.houzicore.shared.common.util.C;
import com.houzicore.shared.core.gadget.CosmeticRarity;
import com.houzicore.shared.core.gadget.GadgetManager;
import com.houzicore.shared.core.gadget.types.SprayGadget;

public class SprayStar extends SprayGadget {

    public SprayStar(GadgetManager manager) {
        super(manager, "Star Spray",
                new String[] { C.cWhite + "Spray a shining star", C.cWhite + "on any surface!" },
                -2, Material.NETHER_STAR, (byte) 0, CosmeticRarity.RARE);
    }

    @Override
    protected void renderSpray(org.bukkit.Location center, BlockFace face, int tick) {
        // 5-point star using parametric equations
        Particle.DustOptions dust = new Particle.DustOptions(Color.YELLOW, 1.2f);
        int points = 5;
        double outerR = 0.4;
        double innerR = 0.15;

        for (int i = 0; i < points * 2; i++) {
            double angle = Math.toRadians(i * 36 - 90); // 36 = 360/(5*2)
            double r = (i % 2 == 0) ? outerR : innerR;
            double dx = Math.cos(angle) * r;
            double dy = Math.sin(angle) * r;

            double px, py, pz;
            if (face == BlockFace.UP || face == BlockFace.DOWN) {
                px = center.getX() + dx;
                py = center.getY();
                pz = center.getZ() + dy;
            } else {
                px = center.getX() + (face.getModZ() != 0 ? dx : 0);
                py = center.getY() + dy;
                pz = center.getZ() + (face.getModX() != 0 ? dx : 0);
            }
            center.getWorld().spawnParticle(Particle.DUST, px, py, pz, 1, 0, 0, 0, 0, dust);
        }
    }
}
