package com.houzicore.arcade.nautilus.game.arcade.managers;

import org.bukkit.Color;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import com.houzicore.shared.common.util.UtilServer;
import com.houzicore.arcade.ArcadeManager;
import com.houzicore.shared.core.combat.event.CombatDeathEvent;
import com.houzicore.arcade.nautilus.game.arcade.game.Game.GameState;

/**
 * Kill Effect Shop (#35) — plays a configurable particle effect on kill.
 * Effect is tied to the KILLER's equipped cosmetic (stored in Stats/DonationManager).
 * Defaults to CRIT if no effect is selected.
 */
public class KillEffectManager implements Listener {



    private final ArcadeManager Manager;

    public KillEffectManager(ArcadeManager manager) {
        Manager = manager;
        Manager.getPluginManager().registerEvents(this, Manager.getPlugin());
    }

    @EventHandler
    public void onPlayerDeath(CombatDeathEvent event) {
        if (Manager.GetGame() == null || Manager.GetGame().GetState() != GameState.Live) return;
        if (!(event.GetEvent().getEntity() instanceof Player)) return;

        Player victim = (Player) event.GetEvent().getEntity();

        Player killer = null;
        if (event.GetLog().GetKiller() != null && event.GetLog().GetKiller().IsPlayer()) {
            killer = org.bukkit.Bukkit.getPlayerExact(event.GetLog().GetKiller().GetName());
        }

        if (killer == null) return;

        com.houzicore.shared.core.gadget.types.Gadget active = Manager.GetCosmeticManager().getGadgetManager().getActive(killer, com.houzicore.shared.core.gadget.types.GadgetType.KillEffect);
        if (active != null && active instanceof com.houzicore.shared.core.gadget.types.KillEffectGadget) {
            ((com.houzicore.shared.core.gadget.types.KillEffectGadget) active).playEffect(killer, victim.getLocation().add(0, 1, 0));
        } else {
            victim.getWorld().spawnParticle(Particle.CRIT, victim.getLocation().add(0, 1, 0), 20, 0.4, 0.6, 0.4, 0.2);
            killer.playSound(victim.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 0.6f, 1.3f);
        }
    }
}
