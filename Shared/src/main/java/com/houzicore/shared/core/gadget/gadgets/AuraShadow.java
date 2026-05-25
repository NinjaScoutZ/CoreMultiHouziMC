package com.houzicore.shared.core.gadget.gadgets;

import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.entity.Player;

import com.houzicore.shared.common.util.C;
import com.houzicore.shared.core.gadget.CosmeticRarity;
import com.houzicore.shared.core.gadget.GadgetManager;
import com.houzicore.shared.core.gadget.types.AuraGadget;

public class AuraShadow extends AuraGadget {

    public AuraShadow(GadgetManager manager) {
        super(manager, "Shadow Aura",
                new String[] { C.cWhite + "Dark tendrils of shadow", C.cWhite + "swirl around you." },
                -2, Material.WITHER_SKELETON_SKULL, (byte) 0, CosmeticRarity.MYTHIC);
    }

    @Override
    protected void renderAura(Player player, int tick) {
        Particle.DustOptions darkDust = new Particle.DustOptions(Color.fromRGB(20, 0, 30), 1.8f);
        Particle.DustOptions purpleDust = new Particle.DustOptions(Color.fromRGB(80, 0, 120), 1.0f);

        // Dark tendrils spiraling upward
        for (int i = 0; i < 4; i++) {
            double angle = (tick * 0.1) + (i * Math.PI / 2);
            double radius = 0.6 + Math.sin(tick * 0.05) * 0.2;
            double x = Math.cos(angle) * radius;
            double z = Math.sin(angle) * radius;
            double y = ((tick + i * 5) % 30) * 0.07;

            player.getWorld().spawnParticle(Particle.DUST,
                    player.getLocation().add(x, y, z),
                    1, 0.02, 0.02, 0.02, 0, darkDust);
        }

        // Smoke wisps
        if (tick % 4 == 0) {
            player.getWorld().spawnParticle(Particle.SMOKE,
                    player.getLocation().add(0, 0.2, 0),
                    2, 0.4, 0.1, 0.4, 0.01);
        }

        // Purple accent
        if (tick % 6 == 0) {
            double a = tick * 0.2;
            player.getWorld().spawnParticle(Particle.DUST,
                    player.getLocation().add(Math.cos(a) * 0.5, 1.5, Math.sin(a) * 0.5),
                    1, 0, 0, 0, 0, purpleDust);
        }
    }
}
