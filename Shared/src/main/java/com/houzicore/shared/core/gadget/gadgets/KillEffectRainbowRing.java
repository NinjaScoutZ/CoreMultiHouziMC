package com.houzicore.shared.core.gadget.gadgets;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Color;
import org.bukkit.entity.Player;

import com.houzicore.shared.core.gadget.CosmeticRarity;
import com.houzicore.shared.core.gadget.GadgetManager;
import com.houzicore.shared.core.gadget.types.KillEffectGadget;

public class KillEffectRainbowRing extends KillEffectGadget {
    public KillEffectRainbowRing(GadgetManager manager) {
        super(manager, "Rainbow Ring", new String[] {
                "§7A beautiful ring of colors",
                "§7blossoms from the death location."
        }, -1, Material.MAGENTA_GLAZED_TERRACOTTA, (byte) 0, CosmeticRarity.EPIC);
    }

    @Override
    public void playEffect(Player killer, Location deathLoc) {
        for (int i = 0; i < 30; i++) {
            Manager.getPlugin().getServer().getScheduler().runTaskLater(Manager.getPlugin(), () -> {
                for (double theta = 0; theta < 2 * Math.PI; theta += Math.PI / 16) {
                    double radius = 1.5;
                    Location loc = deathLoc.clone().add(radius * Math.cos(theta), 0.2, radius * Math.sin(theta));
                    Particle.DustOptions dust = new Particle.DustOptions(
                        Color.fromRGB((int)(Math.random() * 255), (int)(Math.random() * 255), (int)(Math.random() * 255)), 2f
                    );
                    deathLoc.getWorld().spawnParticle(Particle.DUST, loc, 1, 0, 0, 0, 0, dust);
                }
            }, i * 2);
        }
        deathLoc.getWorld().playSound(deathLoc, org.bukkit.Sound.ENTITY_PLAYER_LEVELUP, 1f, 2f);
    }
}
