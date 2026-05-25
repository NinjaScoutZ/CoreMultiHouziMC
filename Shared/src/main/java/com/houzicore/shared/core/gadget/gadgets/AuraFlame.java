package com.houzicore.shared.core.gadget.gadgets;

import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.entity.Player;

import com.houzicore.shared.common.util.C;
import com.houzicore.shared.common.util.UtilParticle;
import com.houzicore.shared.common.util.UtilServer;
import com.houzicore.shared.common.util.UtilParticle.ParticleType;
import com.houzicore.shared.common.util.UtilParticle.ViewDist;
import com.houzicore.shared.core.gadget.CosmeticRarity;
import com.houzicore.shared.core.gadget.GadgetManager;
import com.houzicore.shared.core.gadget.types.AuraGadget;

public class AuraFlame extends AuraGadget {

    public AuraFlame(GadgetManager manager) {
        super(manager, "Flame Aura",
                new String[] { C.cWhite + "Blazing fire spirals", C.cWhite + "around your body." },
                -2, Material.BLAZE_POWDER, (byte) 0, CosmeticRarity.EPIC);
    }

    @Override
    protected void renderAura(Player player, int tick) {
        double time = tick * 0.15;
        double radius = 0.8;

        for (int i = 0; i < 2; i++) {
            double angle = time + (i * Math.PI);
            double x = Math.cos(angle) * radius;
            double z = Math.sin(angle) * radius;
            double y = (tick % 20) * 0.1;

            UtilParticle.PlayParticle(ParticleType.FLAME,
                    player.getLocation().add(x, y, z),
                    0f, 0f, 0f, 0, 1,
                    ViewDist.NORMAL, UtilServer.getPlayers());
        }
    }
}
