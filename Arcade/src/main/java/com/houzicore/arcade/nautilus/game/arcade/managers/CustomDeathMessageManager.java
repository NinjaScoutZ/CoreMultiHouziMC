package com.houzicore.arcade.nautilus.game.arcade.managers;

import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import com.houzicore.shared.common.util.C;
import com.houzicore.shared.common.util.UtilServer;
import com.houzicore.shared.common.util.UtilText;
import com.houzicore.arcade.ArcadeManager;
import com.houzicore.shared.core.combat.event.CombatDeathEvent;
import com.houzicore.arcade.nautilus.game.arcade.game.Game;

/**
 * Custom Death Message Manager (#40)
 * Produces a premium bilingual death broadcast with styled formatting.
 * Replaces generic Bukkit/Spigot death messages.
 */
public class CustomDeathMessageManager implements Listener {

    private final ArcadeManager Manager;

    public CustomDeathMessageManager(ArcadeManager manager) {
        Manager = manager;
        Manager.getPluginManager().registerEvents(this, Manager.getPlugin());
    }

    @EventHandler
    public void onDeath(CombatDeathEvent event) {
        Game game = Manager.GetGame();
        if (game == null || game.GetState() != Game.GameState.Live) return;
        if (!(event.GetEvent().getEntity() instanceof Player)) return;

        Player victim = (Player) event.GetEvent().getEntity();

        Player killer = null;
        if (event.GetLog().GetKiller() != null && event.GetLog().GetKiller().IsPlayer()) {
            killer = org.bukkit.Bukkit.getPlayerExact(event.GetLog().GetKiller().GetName());
        }

        boolean isThai = com.houzicore.shared.core.lang.LangManager.get().isThai(victim);
        String message = buildDeathMessage(victim, killer, isThai);

        // Broadcast to all in-game players
        game.Announce(message);

        // Sound for killer
        if (killer != null && killer.isOnline()) {
            killer.playSound(killer.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 0.8f, 1.3f);
        }
    }

    private String buildDeathMessage(Player victim, Player killer, boolean isThai) {
        String victimName = C.cRed + victim.getName() + C.cGray;
        String icon = "§8⚔ ";

        if (killer != null) {
            String killerName = C.cYellow + killer.getName() + C.cGray;
            if (isThai) {
                return icon + victimName + " §7ถูก " + killerName + " §7สังหารแล้ว";
            } else {
                return icon + victimName + " §7was slain by " + killerName;
            }
        } else {
            if (isThai) {
                return icon + victimName + " §7ถูกกำจัดออกไปแล้ว";
            } else {
                return icon + victimName + " §7was eliminated";
            }
        }
    }
}
