package com.houzicore.arcade.nautilus.game.arcade.managers;

import java.util.HashMap;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;

import com.houzicore.shared.common.util.C;
import com.houzicore.shared.common.util.F;
import com.houzicore.shared.core.combat.event.CombatDeathEvent;
import com.houzicore.shared.core.damage.CustomDamageEvent;
import com.houzicore.arcade.ArcadeManager;
import com.houzicore.arcade.nautilus.game.arcade.events.GameStateChangeEvent;
import com.houzicore.arcade.nautilus.game.arcade.game.Game.GameState;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.title.Title;
import java.time.Duration;

public class KillStreakManager implements org.bukkit.event.Listener {

    private ArcadeManager Manager;
    private HashMap<UUID, Integer> _streaks = new HashMap<>();

    public KillStreakManager(ArcadeManager manager) {
        Manager = manager;
        Manager.getPluginManager().registerEvents(this, Manager.getPlugin());

        com.houzicore.shared.core.reward.math.MultiplierEngine.registerProvider(new com.houzicore.shared.core.reward.math.MultiplierEngine.MultiplierProvider() {
            @Override
            public String getName() { return "Kill Streak"; }

            @Override
            public double getBonus(org.bukkit.entity.Player player) {
                int killStreak = _streaks.getOrDefault(player.getUniqueId(), 0);
                if (killStreak >= 3) return killStreak * 0.05; // +5% Essence per live kill streak record!
                return 0.0;
            }
        });
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onGameStateChange(GameStateChangeEvent event) {
        if (event.GetState() == GameState.Prepare || event.GetState() == GameState.Dead) {
            _streaks.clear();
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onDeath(CombatDeathEvent event) {
        if (Manager.GetGame() == null || Manager.GetGame().GetState() != GameState.Live) {
            return;
        }

        if (!(event.GetEvent().getEntity() instanceof Player)) {
            return;
        }
        Player killed = (Player) event.GetEvent().getEntity();

        // Reset victim's streak
        int oldStreak = _streaks.getOrDefault(killed.getUniqueId(), 0);
        
        Player killer = null;
        if (event.GetLog().GetKiller() != null && event.GetLog().GetKiller().IsPlayer()) {
            killer = Bukkit.getPlayerExact(event.GetLog().GetKiller().GetName());
        }

        if (oldStreak >= 5) {
            // Announce ending a high streak
            if (killer != null && !killer.equals(killed)) {
                Manager.GetGame().Announce(C.cGold + C.Bold + killer.getName() + C.cGray + " ended " + C.cRed + C.Bold + killed.getName() + "'s" + C.cGray + " killstreak of " + oldStreak + "!");
            }
        }
        _streaks.put(killed.getUniqueId(), 0);

        if (killer != null && !killer.equals(killed)) {
            int streak = _streaks.getOrDefault(killer.getUniqueId(), 0) + 1;
            _streaks.put(killer.getUniqueId(), streak);

            // Play streak notification
            if (streak == 3 || streak == 5 || streak == 10 || streak == 15 || streak == 20) {
                broadcastStreak(killer, streak);
            } else if (streak == 2) { // 2 kills
                killer.playSound(killer.getLocation(), Sound.ENTITY_FIREWORK_ROCKET_LAUNCH, 1f, 1.4f);
            }
        }
    }

    private void broadcastStreak(Player player, int streak) {
        String msg = "";
        String sub = "";
        Sound sound = Sound.ENTITY_LIGHTNING_BOLT_THUNDER;
        
        if (streak == 3) {
            msg = C.cRed + "KILLING SPREE!";
            sub = C.cGray + "3 Kills";
        } else if (streak == 5) {
            msg = C.cGold + "RAMPAGE!";
            sub = C.cGray + "5 Kills";
        } else if (streak == 10) {
            msg = C.cPurple + "UNSTOPPABLE!";
            sub = C.cGray + "10 Kills";
        } else if (streak >= 15) {
            msg = C.cRed + C.Bold + "GODLIKE!";
            sub = C.cGray + streak + " Kills";
        }
        
        Manager.GetGame().Announce(F.main("Streak", C.cYellow + player.getName() + " is on a " + msg + C.cGray + " (" + streak + " kills)"));
        
        Title.Times times = Title.Times.times(Duration.ofMillis(250), Duration.ofMillis(1500), Duration.ofMillis(500));
        Component mainTitle = net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer.legacySection().deserialize(msg);
        Component subtitle = net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer.legacySection().deserialize(sub);
        
        player.showTitle(Title.title(mainTitle, subtitle, times));
        player.playSound(player.getLocation(), sound, 1f, 1.0f);
        
        if (streak >= 5) {
            player.playSound(player.getLocation(), Sound.ENTITY_FIREWORK_ROCKET_TWINKLE, 1f, 1.0f);
        }
    }
}
