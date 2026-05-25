package com.houzicore.shared.core.gadget.gadgets;

import org.bukkit.*;
import org.bukkit.entity.EnderDragon;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import com.houzicore.shared.common.util.C;
import com.houzicore.shared.common.util.UtilParticle;
import com.houzicore.shared.common.util.UtilServer;
import com.houzicore.shared.common.util.UtilParticle.ParticleType;
import com.houzicore.shared.common.util.UtilParticle.ViewDist;
import com.houzicore.shared.core.gadget.CosmeticRarity;
import com.houzicore.shared.core.gadget.GadgetManager;
import com.houzicore.shared.core.gadget.types.WinEffectGadget;

public class WinEffectDragonRise extends WinEffectGadget {

    public WinEffectDragonRise(GadgetManager manager) {
        super(manager, "Dragon Rise",
                new String[] { C.cWhite + "A mighty dragon rises", C.cWhite + "from your position!" },
                -2, Material.DRAGON_EGG, (byte) 0, CosmeticRarity.MYTHIC);
    }

    @Override
    public void playEffect(Player player) {
        Location start = getEffectLocation(player);

        // Particle spiral rising effect (no actual dragon entity spawn to avoid issues)
        new BukkitRunnable() {
            int tick = 0;
            double y = 0;
            @Override
            public void run() {
                if (tick >= 60 || !player.isOnline()) {
                    cancel();
                    return;
                }

                y += 0.3;
                double radius = 1.5 - (y * 0.02);
                if (radius < 0.3) radius = 0.3;

                // Dragon spiral particles
                for (int i = 0; i < 8; i++) {
                    double angle = (tick * 0.3) + (i * Math.PI / 4);
                    double x = Math.cos(angle) * radius;
                    double z = Math.sin(angle) * radius;

                    Particle.DustOptions dust = new Particle.DustOptions(
                            Color.fromRGB(80, 0, 120), 2.0f);
                    start.getWorld().spawnParticle(Particle.DUST,
                            start.getX() + x, start.getY() + y, start.getZ() + z,
                            1, 0, 0, 0, 0, dust);
                }

                // Fire trail
                UtilParticle.PlayParticle(ParticleType.FLAME,
                        new Location(start.getWorld(), start.getX(), start.getY() + y, start.getZ()),
                        0.5f, 0.2f, 0.5f, 0.02f, 5,
                        ViewDist.LONG, UtilServer.getPlayers());

                // Dragon roar at peak
                if (tick == 30) {
                    start.getWorld().playSound(start, Sound.ENTITY_ENDER_DRAGON_GROWL, 1.5f, 0.8f);
                }

                tick++;
            }
        }.runTaskTimer(Manager.getPlugin(), 0L, 1L);
    }
}
