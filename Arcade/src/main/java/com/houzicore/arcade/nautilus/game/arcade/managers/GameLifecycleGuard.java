package com.houzicore.arcade.nautilus.game.arcade.managers;

import com.houzicore.arcade.ArcadeManager;
import com.houzicore.arcade.nautilus.game.arcade.events.GameStateChangeEvent;
import com.houzicore.arcade.nautilus.game.arcade.game.Game;
import com.houzicore.arcade.nautilus.game.arcade.game.Game.GameState;
import com.houzicore.arcade.nautilus.game.arcade.game.GameTeam;
import com.houzicore.shared.common.util.UtilServer;

import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

/**
 * Centralized Game Lifecycle Guard — defensive cleanup enforcer.
 * Runs at MONITOR priority as a safety net after all game-specific handlers.
 * Prevents scoreboard bleeding, ghost entities, and disguise leaks.
 */
public class GameLifecycleGuard implements Listener {

    private static final Logger LOG = Logger.getLogger("GameLifecycleGuard");
    private final ArcadeManager manager;
    private long lastTransitionTime;
    private GameState lastState;

    public GameLifecycleGuard(ArcadeManager manager) {
        this.manager = manager;
        manager.getPluginManager().registerEvents(this, manager.getPlugin());
        LOG.info("[GameLifecycleGuard] Initialized — defensive cleanup enforcer active.");
    }

    // === TRANSITION AUDIT ===

    @EventHandler(priority = EventPriority.LOWEST)
    public void onTransitionAudit(GameStateChangeEvent event) {
        long now = System.currentTimeMillis();
        long elapsed = lastTransitionTime > 0 ? now - lastTransitionTime : 0;
        String fromState = lastState != null ? lastState.name() : "NONE";
        LOG.info("[Transition] " + fromState + " -> " + event.GetState().name()
                + " | Game: " + event.GetGame().GetName()
                + " | Players: " + UtilServer.getPlayers().length
                + " | delta=" + elapsed + "ms");
        lastTransitionTime = now;
        lastState = event.GetState();
    }

    // === END STATE GUARD ===

    @EventHandler(priority = EventPriority.LOWEST)
    public void onEndStateValidation(GameStateChangeEvent event) {
        if (event.GetState() != GameState.End) return;
        Game game = event.GetGame();
        if (game.GetTeamList() == null || game.GetTeamList().isEmpty()) {
            LOG.warning("[Guard] Game ending with NULL/EMPTY team list! Game: " + game.GetName());
        }
        if (game.SpectatorSpawn == null && game.WorldData != null) {
            LOG.warning("[Guard] SpectatorSpawn is null at End state.");
        }
    }

    // === DEAD STATE CLEANUP ===

    @EventHandler(priority = EventPriority.MONITOR)
    public void onDeadStateCleanup(GameStateChangeEvent event) {
        if (event.GetState() != GameState.Dead) return;
        Game game = event.GetGame();
        List<String> report = new ArrayList<>();

        int bb = cleanupBossBars();
        if (bb > 0) report.add("BossBars cleared for " + bb + " players");

        int dg = cleanupDisguises();
        if (dg > 0) report.add("Leaked disguises cleared: " + dg);

        int oe = cleanupOrphanedEntities(game);
        if (oe > 0) report.add("Orphaned entities removed: " + oe);

        int pe = cleanupPotionEffects();
        if (pe > 0) report.add("Potion effects cleared from " + pe + " players");

        int pa = restorePlayerAttributes();
        if (pa > 0) report.add("Player attributes restored: " + pa);

        if (!report.isEmpty()) {
            LOG.info("[Guard] Dead state cleanup:");
            for (String e : report) LOG.info("[Guard]   + " + e);
        } else {
            LOG.info("[Guard] Dead state cleanup — no leaks detected.");
        }
    }

    // === CLEANUP METHODS ===

    private int cleanupBossBars() {
        int c = 0;
        for (Player p : UtilServer.getPlayers()) {
            try {
                // Remove all boss bars using Iterator (getBossBars returns Iterator, not Iterable)
                java.util.Iterator<org.bukkit.boss.KeyedBossBar> it = Bukkit.getBossBars();
                while (it.hasNext()) {
                    org.bukkit.boss.BossBar bar = it.next();
                    if (bar.getPlayers().contains(p)) {
                        bar.removePlayer(p);
                        c++;
                    }
                }
            } catch (Exception ignored) {}
        }
        return c;
    }

    private int cleanupDisguises() {
        try {
            var engine = manager.GetDisguise().getEngine();
            int active = 0;
            for (Player p : UtilServer.getPlayers()) {
                if (engine.isDisguised(p)) active++;
            }
            if (active > 0) {
                LOG.warning("[Guard] " + active + " active disguises at Dead state — forcing cleanup.");
                manager.GetDisguise().clearDisguises();
            }
            return active;
        } catch (Exception e) {
            LOG.warning("[Guard] Disguise cleanup error: " + e.getMessage());
            return 0;
        }
    }

    private int cleanupOrphanedEntities(Game game) {
        int removed = 0;
        if (game.WorldData != null && game.WorldData.World != null) {
            removed = cleanupWorldEntities(game.WorldData.World);
        } else {
            for (org.bukkit.World w : Bukkit.getWorlds()) removed += cleanupWorldEntities(w);
        }
        return removed;
    }

    private int cleanupWorldEntities(org.bukkit.World world) {
        int removed = 0;
        for (Entity e : world.getEntities()) {
            if (e instanceof Player) continue;
            if (e.getScoreboardTags().contains("disguise-poc") || e.getScoreboardTags().contains("game-entity")) {
                e.remove();
                removed++;
            }
        }
        return removed;
    }

    private int cleanupPotionEffects() {
        int c = 0;
        for (Player p : UtilServer.getPlayers()) {
            if (p.hasPotionEffect(org.bukkit.potion.PotionEffectType.INVISIBILITY)) {
                p.removePotionEffect(org.bukkit.potion.PotionEffectType.INVISIBILITY);
                c++;
            }
        }
        return c;
    }

    private int restorePlayerAttributes() {
        int c = 0;
        for (Player p : UtilServer.getPlayers()) {
            if (!p.isCollidable()) { p.setCollidable(true); c++; }
            var scale = p.getAttribute(org.bukkit.attribute.Attribute.SCALE);
            if (scale != null && scale.getBaseValue() != 1.0) { scale.setBaseValue(1.0); c++; }
        }
        return c;
    }

    // === PUBLIC API (for DiagCommand) ===

    public List<String> generateDiagnostics() {
        List<String> lines = new ArrayList<>();
        Game game = manager.GetGame();

        lines.add("\u00A78\u00A7m                                        ");
        lines.add("\u00A7b\u00A7l  " + com.houzicore.shared.core.common.BrandConfig.mainServerName().toUpperCase() + " DIAGNOSTICS");
        lines.add("\u00A78\u00A7m                                        ");

        if (game != null) {
            lines.add("\u00A7e\u2764 Game: \u00A7f" + game.GetName() + " \u00A77| \u00A7eState: \u00A7f" + game.GetState().name()
                    + " \u00A77| \u00A7ePlayers: \u00A7f" + game.GetPlayers(true).size());
            if (game.GetTeamList() != null) {
                for (GameTeam t : game.GetTeamList()) {
                    lines.add("  \u00A77" + t.GetColor() + "  " + t.GetName() + ": \u00A7f" + t.GetPlayers(true).size() + " alive");
                }
            }
        } else {
            lines.add("\u00A7e\u2764 Game: \u00A7cNone");
        }

        int disguiseCount = 0;
        try {
            var engine = manager.GetDisguise().getEngine();
            for (Player p : UtilServer.getPlayers()) if (engine.isDisguised(p)) disguiseCount++;
        } catch (Exception ignored) {}
        lines.add("\u00A7d Disguises: \u00A7f" + disguiseCount + " active");

        int totalE = 0, pocE = 0;
        for (org.bukkit.World w : Bukkit.getWorlds()) {
            for (Entity e : w.getEntities()) {
                if (e instanceof Player) continue;
                totalE++;
                if (e.getScoreboardTags().contains("disguise-poc")) pocE++;
            }
        }
        lines.add("\u00A76 Entities: \u00A7f" + totalE + " total \u00A77| POC: " + pocE);

        int invis = 0, noColl = 0;
        for (Player p : UtilServer.getPlayers()) {
            if (p.hasPotionEffect(org.bukkit.potion.PotionEffectType.INVISIBILITY)) invis++;
            if (!p.isCollidable()) noColl++;
        }
        if (invis > 0 || noColl > 0) {
            lines.add("\u00A7c Warning: \u00A7f" + invis + " invisible, " + noColl + " no-collision");
        } else {
            lines.add("\u00A7a OK: \u00A7fNo issues detected");
        }

        if (lastState != null) {
            long ago = (System.currentTimeMillis() - lastTransitionTime) / 1000;
            lines.add("\u00A77 Last transition: \u00A7f" + lastState.name() + " (" + ago + "s ago)");
        }
        lines.add("\u00A78\u00A7m                                        ");
        return lines;
    }

    public int forceEmergencyCleanup() {
        int total = 0;
        total += cleanupBossBars();
        total += cleanupDisguises();
        total += cleanupPotionEffects();
        total += restorePlayerAttributes();
        for (org.bukkit.World w : Bukkit.getWorlds()) total += cleanupWorldEntities(w);
        LOG.info("[Guard] Emergency cleanup completed. Resources cleaned: " + total);
        return total;
    }
}
