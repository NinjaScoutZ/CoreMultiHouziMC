package com.houzicore.shared.core.gadget.gadgets;

import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.entity.Player;

import com.houzicore.shared.common.util.C;
import com.houzicore.shared.common.util.UtilServer;
import com.houzicore.shared.core.gadget.CosmeticRarity;
import com.houzicore.shared.core.gadget.GadgetManager;
import com.houzicore.shared.core.gadget.types.AuraGadget;

public class AuraCrystal extends AuraGadget {

    public AuraCrystal(GadgetManager manager) {
        super(manager, "Crystal Aura",
                new String[] { C.cWhite + "Shimmering crystal shards", C.cWhite + "orbit around you." },
                -2, Material.DIAMOND, (byte) 0, CosmeticRarity.LEGENDARY);
    }

    @Override
    protected void renderAura(Player player, int tick) {
        // 3 crystal shards orbiting at different heights
        Particle.DustOptions dust = new Particle.DustOptions(Color.fromRGB(100, 200, 255), 1.5f);

        for (int i = 0; i < 3; i++) {
            double angle = (tick * 0.12) + (i * Math.PI * 2 / 3);
            double radius = 1.0;
            double x = Math.cos(angle) * radius;
            double z = Math.sin(angle) * radius;
            double y = 0.5 + (i * 0.5) + Math.sin(tick * 0.08 + i) * 0.3;

            player.getWorld().spawnParticle(Particle.DUST,
                    player.getLocation().add(x, y, z),
                    2, 0.05, 0.05, 0.05, 0, dust);
        }

        // Center sparkle
        if (tick % 10 == 0) {
            Particle.DustOptions sparkDust = new Particle.DustOptions(Color.WHITE, 0.8f);
            player.getWorld().spawnParticle(Particle.DUST,
                    player.getLocation().add(0, 1.2, 0),
                    3, 0.3, 0.3, 0.3, 0, sparkDust);
        }
    }
}
