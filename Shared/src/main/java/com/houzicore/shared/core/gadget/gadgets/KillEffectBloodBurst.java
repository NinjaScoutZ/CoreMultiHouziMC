package com.houzicore.shared.core.gadget.gadgets;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.entity.Player;
import org.bukkit.block.data.BlockData;

import com.houzicore.shared.core.gadget.CosmeticRarity;
import com.houzicore.shared.core.gadget.GadgetManager;
import com.houzicore.shared.core.gadget.types.KillEffectGadget;

public class KillEffectBloodBurst extends KillEffectGadget {
    public KillEffectBloodBurst(GadgetManager manager) {
        super(manager, "Blood Burst", new String[] {
                "§7A violent explosion of red",
                "§7marks your fatal strike."
        }, -1, Material.REDSTONE, (byte) 0, CosmeticRarity.COMMON);
    }

    @Override
    public void playEffect(Player killer, Location deathLoc) {
        BlockData data = Material.REDSTONE_BLOCK.createBlockData();
        deathLoc.getWorld().spawnParticle(Particle.BLOCK, deathLoc.clone().add(0, 1, 0), 150, 0.5, 0.5, 0.5, 0.5, data);
        deathLoc.getWorld().playSound(deathLoc, org.bukkit.Sound.ENTITY_ZOMBIE_BREAK_WOODEN_DOOR, 1f, 0.5f);
    }
}
