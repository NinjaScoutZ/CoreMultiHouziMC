package com.houzicore.lobby.hub.modules.arena;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityRegainHealthEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;

import com.houzicore.shared.MiniPlugin;
import com.houzicore.shared.common.util.C;
import com.houzicore.shared.common.util.F;
import com.houzicore.shared.common.util.UtilParticle;
import com.houzicore.shared.common.util.UtilParticle.ParticleType;
import com.houzicore.shared.common.util.UtilParticle.ViewDist;
import com.houzicore.shared.common.util.UtilPlayer;
import com.houzicore.shared.common.util.UtilServer;
import com.houzicore.shared.common.actionbar.ActionBarChannel;
import com.houzicore.shared.common.actionbar.ActionBarService;
import com.houzicore.shared.core.donation.DonationManager;
import com.houzicore.shared.core.hologram.Hologram;
import com.houzicore.shared.core.hologram.HologramManager;
import com.houzicore.shared.core.stats.StatsManager;
import com.houzicore.shared.updater.UpdateType;
import com.houzicore.shared.updater.event.UpdateEvent;
import com.houzicore.shared.core.scoreboard.ScoreboardData;
import com.houzicore.shared.core.scoreboard.ScoreboardManager;
import com.houzicore.shared.core.lang.LangManager;
import com.houzicore.lobby.hub.HubManager;

import net.kyori.adventure.text.Component;

public class ArenaManager extends MiniPlugin {

    private final HubManager _hub;
    private final DonationManager _donation;
    private final StatsManager _stats;
    private final List<ArenaMatch> _matches = new ArrayList<>();
    private final Map<ArenaMatch, org.bukkit.boss.BossBar> _bossBars = new java.util.HashMap<>();
    private final Map<ArenaMatch, Hologram> _streakHolograms = new java.util.HashMap<>();
    private HologramManager _hologramManager;

    private static final double ARENA_RADIUS = 20.0;

    public ArenaManager(HubManager hub, DonationManager donation, StatsManager stats) {
        this(hub, donation, stats, null);
    }

    public ArenaManager(HubManager hub, DonationManager donation, StatsManager stats,
                        com.houzicore.lobby.hub.modules.LobbyNpcManager npcManager) {
        super("Arena Manager", hub.getPlugin());
        _hub = hub;
        _donation = donation;
        _stats = stats;
        _hologramManager = hub.getHologramManager();

        // Register match rings from MapBuilder (WorldConfig.dat)
        java.util.List<Location> rawCenters = hub.getMapData("DATA_NAME:ZONE_ARENA");
        java.util.List<Location> rawSpawnsA = hub.getMapData("DATA_NAME:ARENA_SPAWN_A");
        java.util.List<Location> rawSpawnsB = hub.getMapData("DATA_NAME:ARENA_SPAWN_B");

        java.util.List<Location> centers = new ArrayList<>();
        for (Location l : rawCenters) {
            boolean found = false;
            for (Location c : centers) if (c.distanceSquared(l) < 1) found = true;
            if (!found) centers.add(l);
        }

        java.util.List<Location> spawnsA = new ArrayList<>();
        for (Location l : rawSpawnsA) {
            boolean found = false;
            for (Location c : spawnsA) if (c.distanceSquared(l) < 1) found = true;
            if (!found) spawnsA.add(l);
        }
        
        java.util.List<Location> spawnsB = new ArrayList<>();
        for (Location l : rawSpawnsB) {
            boolean found = false;
            for (Location c : spawnsB) if (c.distanceSquared(l) < 1) found = true;
            if (!found) spawnsB.add(l);
        }


        if (centers.isEmpty()) {
            Location world = hub.GetSpawn();
            _matches.add(new ArenaMatch(new Location(world.getWorld(), 0, 65, 0), ARENA_RADIUS));
        } else {
            for (int i = 0; i < centers.size(); i++) {
                ArenaMatch match = new ArenaMatch(centers.get(i), ARENA_RADIUS);
                if (i < spawnsA.size()) match.spawnA = spawnsA.get(i);
                if (i < spawnsB.size()) match.spawnB = spawnsB.get(i);
                _matches.add(match);
            }
        }

        for (ArenaMatch m : _matches) {
            org.bukkit.boss.BossBar bar = _plugin.getServer().createBossBar(LangManager.get().get("arena.arena_empty"), org.bukkit.boss.BarColor.GREEN, org.bukkit.boss.BarStyle.SOLID);
            _bossBars.put(m, bar);
        }

        // Spawn Arena NPC — only if MapBuilder data exists
        if (npcManager != null) {
            java.util.List<Location> rawNpcLocs = hub.getMapData("DATA_NAME:NPC_ARENA");
            java.util.List<Location> npcLocs = new ArrayList<>();
            for (Location l : rawNpcLocs) if (!npcLocs.contains(l)) npcLocs.add(l);
            
            // Guard: skip if no explicit NPC point AND no ZONE_ARENA was defined
            if (npcLocs.isEmpty() && centers.isEmpty()) {
                System.out.println("[ArenaManager] No NPC_ARENA or ZONE_ARENA in WorldConfig — skipping NPC spawn");
            } else {
                Location npcLoc = npcLocs.isEmpty()
                    ? _matches.get(0).center.clone().add(-ARENA_RADIUS - 3, 0, 0)
                    : npcLocs.get(0);

                npcManager.spawnNpc(npcLoc,
                    C.cRed + "§l⚔ " + com.houzicore.shared.common.util.UtilText.toSmallCaps("Arena PvP"),
                    C.cGray + "ลานประลอง 1v1",
                    C.cYellow + "§o» คลิกเพื่อเข้า «",
                    org.bukkit.entity.Villager.Profession.WEAPONSMITH,
                    org.bukkit.Color.fromRGB(180, 40, 40),
                    player -> {
                        if (_hub.isAdminBuilder(player)) {
                            UtilPlayer.message(player, com.houzicore.shared.common.util.F.main("Arena", C.cRed + "คุณอยู่ในโหมด AdminBuilder"));
                            return;
                        }
                        if (!_matches.isEmpty()) {
                            ArenaMatch first = _matches.get(0);
                            player.teleport(first.center.clone().add(0, 1, 0));
                            boolean isThai = LangManager.get().isThai(player);
                            UtilPlayer.message(player, com.houzicore.shared.common.util.F.main("Arena",
                                isThai ? C.cGreen + "เดินเข้าไปในวงแหวนเพื่อเริ่ม!" : C.cGreen + "Walk into the ring to start!"));
                        }
                    }
                );
            }
        }

        // Scoreboard registration is delayed to first tick
    }

    private boolean _scoreboardRegistered = false;

    private void registerScoreboard() {
        if (_scoreboardRegistered || ScoreboardManager.getInstance() == null) return;
        _scoreboardRegistered = true;
        ScoreboardData data = ScoreboardManager.getInstance().getData("arena", true);
        data.addElement(new com.houzicore.shared.core.scoreboard.elements.ScoreboardElement() {
            @Override
            public java.util.ArrayList<String> GetLines(ScoreboardManager manager, Player player) {
                java.util.ArrayList<String> output = new java.util.ArrayList<>();
                ArenaMatch match = getMatchOf(player);
                if (match == null) return output; // Should not happen
                
                boolean isThai = LangManager.get().isThai(player);
                
                output.add("");
                output.add(" §e👑 " + (isThai ? "แชมป์เปี้ยน" : "King") + ":");
                output.add(" §f" + (match.king != null ? match.king.getName() : "None"));
                
                output.add(" ");
                output.add(" §6🔥 " + (isThai ? "ชนะรวด" : "Win Streak") + ":");
                output.add(" §f" + match.winStreak);
                
                if (match.state == ArenaMatch.MatchState.COUNTDOWN || match.state == ArenaMatch.MatchState.FIGHTING) {
                    Player opp = match.getOpponent(player);
                    if (opp == null && match.king != null) opp = match.getOpponent(match.king);
                    output.add("  ");
                    output.add(" §c⚔ " + (isThai ? "คู่ต่อสู้" : "Opponent") + ":");
                    output.add(" §f" + (opp != null ? opp.getName() : "Waiting..."));
                }
                
                output.add("   ");
                output.add("    ");
                output.add(" §a" + (isThai ? "สถิติของคุณ" : "Your Stats") + ":");
                output.add(" §f" + (isThai ? "ชนะรวม" : "Wins") + ": " + _stats.Get(player).getStat("Arena.Win"));
                output.add("     ");
                
                return output;
            }
        });
    }

    @EventHandler
    public void onTick(UpdateEvent event) {
        if (event.getType() != UpdateType.TICK) return; // TICK for sneak tracking & smooth bossbar

        registerScoreboard();

        for (ArenaMatch match : _matches) {
            updateBossBar(match);
            
            switch (match.state) {
                case IDLE -> tickIdle(match);
                case OCCUPIED -> tickOccupied(match);
                case COUNTDOWN -> tickCountdown(match);
                case FIGHTING -> tickFighting(match);
                default -> {}
            }
        }
    }

    private void updateBossBar(ArenaMatch match) {
        org.bukkit.boss.BossBar bar = _bossBars.get(match);
        if (bar == null) return;
        
        switch (match.state) {
            case IDLE -> {
                bar.setTitle(LangManager.get().get("arena.arena_empty"));
                bar.setColor(org.bukkit.boss.BarColor.GREEN);
            }
            case OCCUPIED -> {
                bar.setTitle(LangManager.get().get("arena.arena_occupied", match.king != null ? match.king.getName() : "?"));
                bar.setColor(org.bukkit.boss.BarColor.YELLOW);
            }
            case COUNTDOWN -> {
                bar.setTitle(LangManager.get().get("arena.arena_countdown"));
                bar.setColor(org.bukkit.boss.BarColor.YELLOW);
                bar.setProgress(match.countdownTick / 5.0);
            }
            case FIGHTING -> {
                String p1 = match.playerA != null ? match.playerA.getName() : "?";
                String p2 = match.playerB != null ? match.playerB.getName() : "?";
                bar.setTitle(LangManager.get().get("arena.arena_fighting", p1, p2));
                bar.setColor(org.bukkit.boss.BarColor.RED);
            }
        }

        // Show to nearby players (distance < 40)
        for (Player p : UtilServer.getPlayers()) {
            if (p.getWorld().equals(match.center.getWorld()) && p.getLocation().distanceSquared(match.center) <= 40 * 40) {
                bar.addPlayer(p);
            } else {
                bar.removePlayer(p);
            }
        }
    }

    private void tickIdle(ArenaMatch match) {
        for (Player p : UtilServer.getPlayers()) {
            if (_hub.isAdminBuilder(p)) continue;
            if (match.inRing(p.getLocation())) {
                match.king = p;
                match.playerA = p;
                match.winStreak = 0;
                match.setState(ArenaMatch.MatchState.OCCUPIED);
                setArenaScoreboard(p, true);
                
                // Track B: Context Transitions (Enter LOBBY_ARENA_PREP)
                _hub.getTransitionCoordinator().enterArenaPrep(p);
                
                // Invisible forcefield will just push people away later
                
                boolean isThai = com.houzicore.shared.core.lang.LangManager.get().isThai(p);
                UtilPlayer.message(p, F.main("Arena", isThai ? C.cGold + "คุณได้ครองลานประลอง! " + C.cGray + "รอผู้ท้าชิงคนถัดไป..." : C.cGold + "You claim the arena! " + C.cGray + "Waiting for challenger..."));
                p.playSound(p.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 1f, 1.2f);
                break; // Only 1 person triggers it
            }
        }
    }

    private void tickOccupied(ArenaMatch match) {
        if (match.king == null || !match.king.isOnline() || !match.inRing(match.king.getLocation())) {
            resetArena(match, null); // King left gracefully or unexpectedly
            return;
        }

        // Sneak to exit logic
        if (match.king.isSneaking() && com.houzicore.shared.common.util.UtilEnt.isGrounded(match.king)) {
            match.sneakExitTicks++;
            if (match.sneakExitTicks % 10 == 0) {
                int sec = match.sneakExitTicks / 20;
                String progress = "§e" + "⬛".repeat(sec) + "⬜".repeat(3 - sec);
                boolean isThai = com.houzicore.shared.core.lang.LangManager.get().isThai(match.king);
                ActionBarService.display(match.king, ActionBarChannel.TOOL_HINT, Component.text(progress + (isThai ? " กำลังออก..." : " Leaving...")));
            }
            if (match.sneakExitTicks >= 60) {
                boolean isThai = com.houzicore.shared.core.lang.LangManager.get().isThai(match.king);
                UtilPlayer.message(match.king, F.main("Arena", isThai ? C.cGreen + "คุณออกจากลานประลองแล้ว" : C.cGreen + "You left the arena."));
                match.king.teleport(_hub.GetSpawn());
                resetArena(match, null);
                return;
            }
        } else {
            if (match.sneakExitTicks > 0) {
                match.sneakExitTicks = 0;
                ActionBarService.clear(match.king, ActionBarChannel.TOOL_HINT);
            }
            ActionBarService.display(match.king, ActionBarChannel.TOOL_HINT, Component.text(LangManager.get().get(match.king, "arena.sneak_exit")));
        }

        // Matchup random challengers standing in radius 20
        List<Player> challengers = new ArrayList<>();
        for (Player p : UtilServer.getPlayers()) {
            if (_hub.isAdminBuilder(p)) continue;
            if (p.equals(match.king)) continue;
            if (match.inRing(p.getLocation())) {
                challengers.add(p);
            }
        }

        if (!challengers.isEmpty()) {
            Player challenger = challengers.get(new java.util.Random().nextInt(challengers.size()));
            match.playerB = challenger;
            
            boolean isChallengerThai = com.houzicore.shared.core.lang.LangManager.get().isThai(challenger);
            boolean isKingThai = com.houzicore.shared.core.lang.LangManager.get().isThai(match.king);
            UtilPlayer.message(challenger, F.main("Arena", isChallengerThai ? C.cGreen + "ระบบสุ่มให้คุณท้าทาย " + match.king.getName() + "!" : C.cGreen + "System matched you against " + match.king.getName() + "!"));
            UtilPlayer.message(match.king, F.main("Arena", isKingThai ? C.cRed + challenger.getName() + " เป็นผู้ท้าชิง!" : C.cRed + challenger.getName() + " is challenging you!"));
            
            setArenaScoreboard(match.playerA, true);
            setArenaScoreboard(match.playerB, true);

            match.setState(ArenaMatch.MatchState.COUNTDOWN);
            match.countdownTick = 5;

            // Track B: Context Transitions (Enter LOBBY_ARENA_PREP)
            // match.playerA (King) is already in Arena Prep context
            _hub.getTransitionCoordinator().enterArenaPrep(match.playerB);
        }
    }

    private void broadcastSubtitle(ArenaMatch match, String subtitle, int fadeIn, int stay, int fadeOut) {
        for (Player p : UtilServer.getPlayers()) {
            if (match.inRing(p.getLocation()) || p.equals(match.playerA) || p.equals(match.playerB)) {
                com.houzicore.shared.common.util.UtilTextMiddle.display("", subtitle, fadeIn, stay, fadeOut, p);
            }
        }
    }

    private void applyBarrier(ArenaMatch match) {
        double rSq = match.radius * match.radius;
        for (Player p : UtilServer.getPlayers()) {
            if (!p.getWorld().equals(match.center.getWorld())) continue;

            boolean isFighter = (p.equals(match.playerA) || p.equals(match.playerB));
            double distSq = p.getLocation().distanceSquared(match.center);
            
            if (isFighter && distSq > rSq) {
                // Bounce fighters back in
                com.houzicore.shared.common.util.UtilAction.velocity(p, com.houzicore.shared.common.util.UtilAlg.getTrajectory(p.getLocation(), match.center), 1.2, false, 0, 0.2, 1, true);
                p.playSound(p.getLocation(), Sound.ENTITY_ZOMBIE_ATTACK_IRON_DOOR, 0.5f, 0.5f);
                UtilParticle.PlayParticle(ParticleType.CLOUD, p.getLocation().add(0, 1, 0), 0.5f, 0.5f, 0.5f, 0.05f, 10, ViewDist.NORMAL, UtilServer.getPlayers());
            } else if (!isFighter && distSq <= rSq) {
                // Bounce spectators out
                com.houzicore.shared.common.util.UtilAction.velocity(p, com.houzicore.shared.common.util.UtilAlg.getTrajectory(match.center, p.getLocation()), 1.2, false, 0, 0.2, 1, true);
                p.playSound(p.getLocation(), Sound.ENTITY_ZOMBIE_ATTACK_IRON_DOOR, 0.5f, 0.5f);
                UtilParticle.PlayParticle(ParticleType.CLOUD, p.getLocation().add(0, 1, 0), 0.5f, 0.5f, 0.5f, 0.05f, 10, ViewDist.NORMAL, UtilServer.getPlayers());
            }
        }
    }

    private void tickCountdown(ArenaMatch match) {
        if (match.playerA == null || match.playerB == null || !match.playerA.isOnline() || !match.playerB.isOnline()) {
            resetArena(match, null);
            return;
        }

        applyBarrier(match);

        long elapsed = System.currentTimeMillis() - match.stateChangedAt;
        int secondsIn = (int) (elapsed / 1000);

        if (secondsIn < 5) {
            int displayTick = 5 - secondsIn;
            if (displayTick != match.countdownTick) {
                match.countdownTick = displayTick;
                String color = displayTick <= 2 ? C.cRed : (displayTick <= 3 ? C.cYellow : C.cGreen);
                broadcastSubtitle(match, "arena.countdown_title", 0, 25, 5, color, String.valueOf(displayTick));
                broadcastSound(match, Sound.BLOCK_NOTE_BLOCK_PLING, 1.2f + (0.2f * (5 - displayTick)));
            }
        } else {
            // Teleport players to their spawn corners
            match.playerA.teleport(match.spawnA);
            match.playerB.teleport(match.spawnB);

            match.setState(ArenaMatch.MatchState.FIGHTING);
            broadcastSubtitle(match, "arena.fight_title", 0, 40, 10);
            broadcastSound(match, Sound.ENTITY_ENDER_DRAGON_GROWL, 1.0f);

            _hub.getTransitionCoordinator().enterArenaLive(match.playerA);
            com.houzicore.shared.common.util.UtilTextBottom.display(
                com.houzicore.shared.common.actionbar.ActionBarChannel.TOOL_HINT,
                com.houzicore.shared.core.lang.LangManager.get().isThai(match.playerA) ? "§c⚔ ฆ่ามันให้ตายเพื่อเป็น King!" : "§c⚔ Kill to become King!",
                match.playerA);

            _hub.getTransitionCoordinator().enterArenaLive(match.playerB);
            com.houzicore.shared.common.util.UtilTextBottom.display(
                com.houzicore.shared.common.actionbar.ActionBarChannel.TOOL_HINT,
                com.houzicore.shared.core.lang.LangManager.get().isThai(match.playerB) ? "§c⚔ ฆ่ามันให้ตายเพื่อเป็น King!" : "§c⚔ Kill to become King!",
                match.playerB);
        }
    }

    private void tickFighting(ArenaMatch match) {
        if (match.playerA == null || match.playerB == null || !match.playerA.isOnline() || !match.playerB.isOnline()) {
            Player remaining = (match.playerA != null && match.playerA.isOnline()) ? match.playerA : 
                               (match.playerB != null && match.playerB.isOnline() ? match.playerB : null);
            Player disconnected = match.getOpponent(remaining);
            forfeit(match, disconnected);
            return;
        }

        applyBarrier(match);

        // Clean projectiles crossing boundary
        for (org.bukkit.entity.Projectile proj : match.center.getWorld().getEntitiesByClass(org.bukkit.entity.Projectile.class)) {
            if (proj.getShooter() instanceof Player shooter) {
                boolean shooterInMatch = (shooter.equals(match.playerA) || shooter.equals(match.playerB));
                double distSq = proj.getLocation().distanceSquared(match.center);
                if (shooterInMatch && distSq > match.radius * match.radius) {
                    proj.remove();
                    UtilParticle.PlayParticle(ParticleType.CLOUD, proj.getLocation(), 0.1f, 0.1f, 0.1f, 0.01f, 2, ViewDist.NORMAL, UtilServer.getPlayers());
                } else if (!shooterInMatch && distSq <= match.radius * match.radius) {
                    proj.remove();
                    UtilParticle.PlayParticle(ParticleType.CLOUD, proj.getLocation(), 0.1f, 0.1f, 0.1f, 0.01f, 2, ViewDist.NORMAL, UtilServer.getPlayers());
                }
            }
        }

        // ActionBar HP display
        for (Player p : new Player[]{match.playerA, match.playerB}) {
            Player opp = match.getOpponent(p);
            if (opp == null) continue;
            int myHp  = (int) p.getHealth();
            int oppHp = (int) opp.getHealth();
            ActionBarService.display(p, ActionBarChannel.GAME_STATUS, Component.text(LangManager.get().get(p, "arena.actionbar_hp", myHp, oppHp)));
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onCustomDamage(com.houzicore.shared.core.damage.CustomDamageEvent event) {
        if (!(event.GetDamageeEntity() instanceof Player)) return;
        Player victim = (Player) event.GetDamageeEntity();

        ArenaMatch match = getMatchOf(victim);
        if (match == null) return;
        
        if (match.state == ArenaMatch.MatchState.FIGHTING) {
            // Uncancel Hub's damage protection!
            event.GetCancellers().clear();

            // Eliminate at very low HP
            if (victim.getHealth() - event.GetDamage() <= 1.0) {
                event.SetCancelled("Kill");
                endMatch(match, match.getOpponent(victim), victim);
            }
        } else {
            event.SetCancelled("Arena Match Not Fighting");
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void blockRegen(EntityRegainHealthEvent event) {
        if (!(event.getEntity() instanceof Player)) return;
        Player player = (Player) event.getEntity();
        if (getMatchOf(player) != null) {
            event.setCancelled(true); // No natural regen during match
        }
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        ArenaMatch match = getMatchOf(event.getPlayer());
        if (match == null) return;
        
        if (match.state == ArenaMatch.MatchState.FIGHTING) {
            forfeit(match, event.getPlayer());
        } else {
            resetArena(match, event.getPlayer());
        }
    }

    private void setArenaScoreboard(Player p, boolean active) {
        if (p == null) return;
        if (ScoreboardManager.getInstance() != null) {
            var ps = ScoreboardManager.getInstance().getPlayerScoreboard(p);
            if (ps != null) {
                if (active) {
                    ps.setHidden(false);
                    ps.setScoreboardData("arena");
                    ps.setTitle(C.cRed + C.Bold + "BOXING ARENA");
                } else {
                    ps.setScoreboardData("default");
                    ps.setTitle("   " + com.houzicore.shared.core.common.BrandConfig.mainServerName().toUpperCase(java.util.Locale.ROOT) + "   ");
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onBlockPlace(org.bukkit.event.block.BlockPlaceEvent event) {
        if (event.isCancelled()) return;
        ArenaMatch match = getMatchOf(event.getPlayer());
        if (match == null || match.state != ArenaMatch.MatchState.FIGHTING) return;

        // Track the block so we can remove it later
        match.placedBlocks.add(event.getBlock());
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onBlockBreak(org.bukkit.event.block.BlockBreakEvent event) {
        ArenaMatch match = getMatchOf(event.getPlayer());
        if (match == null || match.state != ArenaMatch.MatchState.FIGHTING) return;

        // Only allow breaking blocks that were placed during this match
        if (match.placedBlocks.contains(event.getBlock())) {
            match.placedBlocks.remove(event.getBlock());
            event.getBlock().setType(Material.AIR);
        } else {
            event.setCancelled(true);
        }
    }

    private void resetArena(ArenaMatch match, Player reasonToTPOut) {
        setArenaScoreboard(match.playerA, false);
        com.houzicore.shared.common.actionbar.ActionBarService.clear(match.playerA, com.houzicore.shared.common.actionbar.ActionBarChannel.TOOL_HINT);
        setArenaScoreboard(match.playerB, false);
        com.houzicore.shared.common.actionbar.ActionBarService.clear(match.playerB, com.houzicore.shared.common.actionbar.ActionBarChannel.TOOL_HINT);

        if (match.playerA != null) { 
            _hub.getTransitionCoordinator().exitArena(match.playerA);
            if (match.playerA.equals(reasonToTPOut)) match.playerA.teleport(_hub.GetSpawn()); 
        }
        if (match.playerB != null) { 
            _hub.getTransitionCoordinator().exitArena(match.playerB);
            if (match.playerB.equals(reasonToTPOut)) match.playerB.teleport(_hub.GetSpawn()); 
        }
        
        // Remove streak hologram when arena resets
        removeStreakHologram(match);

        // Cleanup placed blocks
        for (Block b : match.placedBlocks) {
            b.setType(Material.AIR);
        }
        match.placedBlocks.clear();
        
        match.playerA = null;
        match.playerB = null;
        match.king = null;
        match.winStreak = 0;
        match.sneakExitTicks = 0;
        match.setState(ArenaMatch.MatchState.IDLE);
    }

    private void forfeit(ArenaMatch match, Player forfeitPlayer) {
        Player winner = match.getOpponent(forfeitPlayer);
        if (winner != null) {
            UtilPlayer.message(winner, F.main("Arena", LangManager.get().get(winner, "arena.forfeit_win")));
        }
        endMatch(match, winner, forfeitPlayer);
    }

    private void endMatch(ArenaMatch match, Player winner, Player loser) {
        setArenaScoreboard(loser, false);

        // Cleanup placed blocks early before the next fight
        for (Block b : match.placedBlocks) {
            b.setType(Material.AIR);
        }
        match.placedBlocks.clear();

        if (winner != null) {
            match.winStreak = (match.king != null && match.king.equals(winner)) ? match.winStreak + 1 : 1;
            match.king = winner;
            
            // Reward logic
            int essence = 50;
            if (match.winStreak == 3) {
                essence = 100;
                winner.sendTitle(LangManager.get().get(winner, "arena.win_3_title"), LangManager.get().get(winner, "arena.win_3_sub"), 10, 60, 20);
            } else if (match.winStreak >= 5) {
                essence = 200;
                winner.sendTitle(LangManager.get().get(winner, "arena.win_streak_title", match.winStreak), LangManager.get().get(winner, "arena.win_streak_sub"), 10, 60, 20);
                UtilParticle.PlayParticle(ParticleType.FIREWORKS_SPARK, winner.getLocation().add(0, 1, 0), 0.5f, 1f, 0.5f, 0.2f, 60, ViewDist.LONG, UtilServer.getPlayers());
            } else {
                winner.sendTitle(LangManager.get().get(winner, "arena.win_title"), LangManager.get().get(winner, "arena.win_sub"), 10, 60, 20);
            }
            
            winner.playSound(winner.getLocation(), Sound.UI_TOAST_CHALLENGE_COMPLETE, 1f, 1.2f);
            _donation.RewardEssenceLater("Arena.Win", winner, essence);
            _stats.incrementStat(winner, "Arena.Win", 1);
            
            // Update win streak hologram above king's head
            updateStreakHologram(match);
            
            _hub.getTransitionCoordinator().returnToArenaPrep(winner);
            
            match.playerA = winner;
            match.playerB = null;
            match.setState(ArenaMatch.MatchState.OCCUPIED);
        }
        
        if (loser != null && loser.isOnline()) {
            loser.sendTitle(LangManager.get().get(loser, "arena.lose_title"), LangManager.get().get(loser, "arena.lose_sub"), 10, 40, 20);
            
            _hub.getTransitionCoordinator().exitArena(loser);

            // Teleport loser just outside the ring edge instead of lobby spawn
            loser.teleport(getRingEdgeLocation(match, loser));
        }
        
        if (winner == null) {
            resetArena(match, null);
        }
    }



    /**
     * Calculates a safe location just outside the ring perimeter,
     * along the direction from center to the loser's current position.
     */
    private Location getRingEdgeLocation(ArenaMatch match, Player loser) {
        Location center = match.center.clone();
        Location playerLoc = loser.getLocation();
        
        // Direction from center to lose player
        org.bukkit.util.Vector dir = playerLoc.toVector().subtract(center.toVector());
        if (dir.lengthSquared() < 0.01) {
            // Player is exactly at center — pick a random direction
            dir = new org.bukkit.util.Vector(1, 0, 0);
        }
        dir.setY(0).normalize();
        
        // Place 2 blocks outside the ring edge
        Location edge = center.add(dir.multiply(match.radius + 2));
        edge.setY(playerLoc.getY());
        edge.setYaw(playerLoc.getYaw());
        edge.setPitch(playerLoc.getPitch());
        return edge;
    }

    /**
     * Win streak hologram that floats above the king's head.
     * Displays the streak title (e.g. "🔥 5 WINS 🔥") as a TextDisplay.
     */
    private void updateStreakHologram(ArenaMatch match) {
        removeStreakHologram(match);
        
        if (match.king == null || match.winStreak < 2) return;
        
        String streakText;
        if (match.winStreak >= 5) {
            streakText = C.cGold + "§l🔥 " + match.winStreak + " WINS 🔥";
        } else if (match.winStreak == 3 || match.winStreak == 4) {
            streakText = C.cGold + "§l⚔ " + match.winStreak + " WINS ⚔";
        } else {
            streakText = C.cYellow + "§l" + match.winStreak + " WINS";
        }
        
        Location headLoc = match.king.getLocation().add(0, 2.5, 0);
        Hologram holo = new Hologram(_hologramManager, headLoc, streakText);
        holo.setFollowEntity(match.king);
        holo.setViewDistance(40);
        holo.start();
        
        _streakHolograms.put(match, holo);
    }

    private void removeStreakHologram(ArenaMatch match) {
        Hologram old = _streakHolograms.remove(match);
        if (old != null && old.isInUse()) {
            old.stop();
        }
    }



    private void broadcastMatch(ArenaMatch match, String msg) {
        if (match.playerA != null) UtilPlayer.message(match.playerA, F.main("Arena", msg));
        if (match.playerB != null) UtilPlayer.message(match.playerB, F.main("Arena", msg));
    }

    private void broadcastSubtitle(ArenaMatch match, String key, int in, int stay, int out, Object... args) {
        for (Player p : UtilServer.getPlayers()) {
            if (match.inRing(p.getLocation()) || p.equals(match.playerA) || p.equals(match.playerB)) {
                String translated = LangManager.get().get(p, key, args);
                com.houzicore.shared.common.util.UtilTextMiddle.display("", translated, in, stay, out, p);
            }
        }
    }

    private void broadcastSubtitleRaw(ArenaMatch match, String raw, int in, int stay, int out) {
        for (Player p : UtilServer.getPlayers()) {
            if (match.inRing(p.getLocation()) || p.equals(match.playerA) || p.equals(match.playerB)) {
                com.houzicore.shared.common.util.UtilTextMiddle.display("", raw, in, stay, out, p);
            }
        }
    }

    private void broadcastSound(ArenaMatch match, Sound sound, float pitch) {
        if (match.playerA != null) match.playerA.playSound(match.playerA.getLocation(), sound, 1f, pitch);
        if (match.playerB != null) match.playerB.playSound(match.playerB.getLocation(), sound, 1f, pitch);
    }

    private ArenaMatch getMatchOf(Player player) {
        for (ArenaMatch m : _matches) {
            if (m.hasPlayer(player)) return m;
        }
        return null;
    }

    public boolean isPlayerInMatch(Player player) {
        ArenaMatch match = getMatchOf(player);
        return match != null && match.state == ArenaMatch.MatchState.FIGHTING;
    }
}
