package com.houzicore.shared.core.gadget.gadgets;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import com.houzicore.shared.common.util.C;
import com.houzicore.shared.core.gadget.CosmeticRarity;
import com.houzicore.shared.core.gadget.GadgetManager;
import com.houzicore.shared.core.gadget.types.WinEffectGadget;

public class WinEffectLightning extends WinEffectGadget {

    public WinEffectLightning(GadgetManager manager) {
        super(manager, "Lightning Storm",
                new String[] { C.cWhite + "Bolts of lightning", C.cWhite + "strike around you!" },
                -2, Material.TRIDENT, (byte) 0, CosmeticRarity.EPIC);
    }

    @Override
    public void playEffect(Player player) {
        Location base = getEffectLocation(player);
        new BukkitRunnable() {
            int count = 0;
            @Override
            public void run() {
                if (count >= 6 || !player.isOnline()) {
                    cancel();
                    return;
                }

                double angle = Math.random() * Math.PI * 2;
                double radius = 2 + Math.random() * 3;
                Location strike = base.clone().add(
                        Math.cos(angle) * radius, 0, Math.sin(angle) * radius);

                // Visual lightning (no damage)
                base.getWorld().strikeLightningEffect(strike);
                base.getWorld().playSound(strike, Sound.ENTITY_LIGHTNING_BOLT_THUNDER, 0.5f, 1.2f);

                count++;
            }
        }.runTaskTimer(Manager.getPlugin(), 5L, 8L);
    }
}
