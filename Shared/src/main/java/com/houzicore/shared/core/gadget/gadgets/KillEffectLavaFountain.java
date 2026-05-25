package com.houzicore.shared.core.gadget.gadgets;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.entity.Player;

import com.houzicore.shared.core.gadget.CosmeticRarity;
import com.houzicore.shared.core.gadget.GadgetManager;
import com.houzicore.shared.core.gadget.types.KillEffectGadget;

public class KillEffectLavaFountain extends KillEffectGadget {
    public KillEffectLavaFountain(GadgetManager manager) {
        super(manager, "Lava Fountain", new String[] {
                "§7Let your victims burn in a",
                "§7fountain of molten lava."
        }, -1, Material.LAVA_BUCKET, (byte) 0, CosmeticRarity.RARE);
    }

    @Override
    public void playEffect(Player killer, Location deathLoc) {
        for (int i = 0; i < 40; i++) {
            Manager.getPlugin().getServer().getScheduler().runTaskLater(Manager.getPlugin(), () -> {
                deathLoc.getWorld().spawnParticle(Particle.LAVA, deathLoc.clone().add(0, 0.5, 0), 10, 0.4, 0.8, 0.4, 0.1);
                deathLoc.getWorld().spawnParticle(Particle.FLAME, deathLoc.clone().add(0, 0.5, 0), 5, 0.2, 0.5, 0.2, 0.1);
            }, i);
        }
        deathLoc.getWorld().playSound(deathLoc, org.bukkit.Sound.BLOCK_LAVA_EXTINGUISH, 1f, 1f);
    }
}
