package com.houzicore.shared.core.gadget.gadgets;

import org.bukkit.*;
import org.bukkit.entity.Firework;
import org.bukkit.entity.Player;
import org.bukkit.inventory.meta.FireworkMeta;
import org.bukkit.scheduler.BukkitRunnable;

import com.houzicore.shared.common.util.C;
import com.houzicore.shared.core.gadget.CosmeticRarity;
import com.houzicore.shared.core.gadget.GadgetManager;
import com.houzicore.shared.core.gadget.types.WinEffectGadget;

public class WinEffectFireworks extends WinEffectGadget {

    public WinEffectFireworks(GadgetManager manager) {
        super(manager, "Fireworks Finale",
                new String[] { C.cWhite + "Celebrate your victory", C.cWhite + "with a dazzling fireworks show!" },
                -2, Material.FIREWORK_ROCKET, (byte) 0, CosmeticRarity.COMMON);
    }

    @Override
    public void playEffect(Player player) {
        Location base = getEffectLocation(player);
        new BukkitRunnable() {
            int count = 0;
            @Override
            public void run() {
                if (count >= 8 || !player.isOnline()) {
                    cancel();
                    return;
                }

                Location loc = base.clone().add(
                        (Math.random() - 0.5) * 4,
                        1 + Math.random() * 2,
                        (Math.random() - 0.5) * 4);

                Firework fw = base.getWorld().spawn(loc, Firework.class);
                FireworkMeta meta = fw.getFireworkMeta();

                Color[] colors = {Color.RED, Color.YELLOW, Color.AQUA, Color.LIME, Color.PURPLE, Color.ORANGE};
                FireworkEffect.Type[] types = {FireworkEffect.Type.BALL_LARGE, FireworkEffect.Type.STAR, FireworkEffect.Type.BURST};

                meta.addEffect(FireworkEffect.builder()
                        .with(types[count % types.length])
                        .withColor(colors[count % colors.length])
                        .withFade(colors[(count + 2) % colors.length])
                        .flicker(true)
                        .trail(true)
                        .build());
                meta.setPower(0);
                fw.setFireworkMeta(meta);

                // Detonate after short delay
                Bukkit.getScheduler().runTaskLater(Manager.getPlugin(), fw::detonate, 2L);

                count++;
            }
        }.runTaskTimer(Manager.getPlugin(), 0L, 5L);
    }
}
