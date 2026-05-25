package com.houzicore.lobby.hub.modules.nonstop;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import com.houzicore.shared.MiniPlugin;
import com.houzicore.shared.common.util.C;
import com.houzicore.shared.common.util.F;
import com.houzicore.shared.common.util.UtilPlayer;
import com.houzicore.shared.common.util.UtilServer;
import com.houzicore.shared.common.actionbar.ActionBarChannel;
import com.houzicore.shared.common.actionbar.ActionBarService;
import com.houzicore.shared.core.donation.DonationManager;
import com.houzicore.shared.core.stats.StatsManager;
import com.houzicore.shared.updater.UpdateType;
import com.houzicore.shared.updater.event.UpdateEvent;
import com.houzicore.lobby.hub.HubManager;
import com.houzicore.lobby.hub.bootstrap.LobbyTransitionCoordinator;

import net.kyori.adventure.text.Component;

/**
 * Checkpoint Parkour — replaces the old Nonstop system.
 * Players climb through checkpoints. If they fall below their last checkpoint,
 * they respawn at that checkpoint. No idle penalty.
 */
public class NonstopParkourManager extends MiniPlugin {
    private static final long AUTO_ENTRY_SUPPRESS_MS = 1500L;
    private static final double FINISH_RADIUS_SQ = 9.0;
    private static final double CHECKPOINT_RADIUS = 3.0;

    private final HubManager _hub;
    private final DonationManager _donation;
    private final StatsManager _stats;
    private final com.houzicore.shared.core.hologram.HologramManager _hologramManager;
    private final com.houzicore.lobby.hub.modules.LobbyNpcManager _npcManager;

    private org.bukkit.entity.Entity _npcEntity;
    private final java.util.List<com.houzicore.shared.core.leaderboard.LeaderboardHologram> _leaderboardHolograms = new java.util.ArrayList<>();

    private final Set<NonstopParkourData> _courses = new HashSet<>();

    // Active runners
    private final Map<Player, NonstopParkourData> _active = new HashMap<>();
    private final Map<Player, Boolean>             _isChallenge = new HashMap<>();
    // RunStart time for timer display
    private final Map<Player, Long>                _startTime = new HashMap<>();
    // Per-player last checkpoint location
    private final Map<Player, Location>            _lastCheckpoint = new HashMap<>();
    // Per-player highest checkpoint index reached
    private final Map<Player, Integer>             _highestCp = new HashMap<>();
    // Per-player BossBar
    private final Map<Player, BossBar>             _bossBars = new HashMap<>();
    private final Set<UUID>                        _zonePresence = new HashSet<>();
    private final Map<UUID, Long>                  _zoneSuppressUntil = new HashMap<>();

    private com.houzicore.lobby.hub.modules.nonstop.shop.ParkourShop _shop;

    private boolean _scoreboardRegistered = false;

    private void registerScoreboard() {
        if (_scoreboardRegistered || com.houzicore.shared.core.scoreboard.ScoreboardManager.getInstance() == null) return;
        _scoreboardRegistered = true;
        com.houzicore.shared.core.scoreboard.ScoreboardData data = com.houzicore.shared.core.scoreboard.ScoreboardManager.getInstance().getData("parkour", true);
        data.addElement(new com.houzicore.shared.core.scoreboard.elements.ScoreboardElement() {
            @Override
            public java.util.ArrayList<String> GetLines(com.houzicore.shared.core.scoreboard.ScoreboardManager manager, org.bukkit.entity.Player player) {
                java.util.ArrayList<String> output = new java.util.ArrayList<>();
                boolean isThai = com.houzicore.shared.core.lang.LangManager.get().isThai(player);
                output.add("");
                output.add(" §f" + (isThai ? "เกมส์" : "Game") + ": §a" + (isThai ? "ปาร์กัวร์" : "Parkour"));
                output.add("");
                NonstopParkourData course = _active.get(player);
                if (course != null) {
                    boolean challenge = _isChallenge.getOrDefault(player, false);
                    if (challenge) {
                        long elapsedMs = System.currentTimeMillis() - _startTime.getOrDefault(player, System.currentTimeMillis());
                        String time = String.format("%d:%05.2f", elapsedMs / 60000, (elapsedMs % 60000) / 1000.0);
                        output.add(" §7" + (isThai ? "เวลา" : "Time") + ": §f" + time);
                    } else {
                        output.add(" §7" + (isThai ? "โหมด" : "Mode") + ": §b" + (isThai ? "ฝึกซ้อม" : "Practice"));
                    }
                    int cpReached = _highestCp.getOrDefault(player, 0);
                    int cpTotal = course.getCheckpointCount();
                    if (cpTotal > 0) {
                        output.add(" §7" + (isThai ? "เช็คพอยต์" : "Checkpoint") + ": §a" + cpReached + "§7/§f" + cpTotal);
                    }
                }
                return output;
            }
        });
    }

    private void setParkourScoreboard(Player p, boolean active) {
        registerScoreboard();
        if (com.houzicore.shared.core.scoreboard.ScoreboardManager.getInstance() != null) {
            com.houzicore.shared.core.scoreboard.PlayerScoreboard ps = com.houzicore.shared.core.scoreboard.ScoreboardManager.getInstance().getPlayerScoreboard(p);
            if (ps != null) {
                ps.setHidden(false);
                ps.setScoreboardData(active ? "parkour" : "default");
            }
        }
    }

    public NonstopParkourManager(HubManager hub, DonationManager donation, StatsManager stats) {
        this(hub, donation, stats, null, null);
    }

    public NonstopParkourManager(HubManager hub, DonationManager donation, StatsManager stats,
                                  com.houzicore.lobby.hub.modules.LobbyNpcManager npcManager,
                                  com.houzicore.shared.core.hologram.HologramManager hologramManager) {
        super("Checkpoint Parkour", hub.getPlugin());
        _hub = hub;
        _donation = donation;
        _stats = stats;
        _hologramManager = hologramManager;
        _npcManager = npcManager;

        _shop = new com.houzicore.lobby.hub.modules.nonstop.shop.ParkourShop(this, hub.GetClients(), donation);
        loadCourseData(hub);
    }

    private void loadCourseData(HubManager hub) {
        java.util.List<Location> zonePoints = hub.getMapData("DATA_NAME:ZONE_PARKOUR");
        if (zonePoints.isEmpty()) {
            zonePoints = hub.getMapData("CUSTOM_NAME:ZONE_PARKOUR");
        }
        java.util.List<Location> npcPoints = hub.getMapData("DATA_NAME:NPC_PARKOUR");
        if (npcPoints.isEmpty()) {
            npcPoints = hub.getMapData("CUSTOM_NAME:NPC_PARKOUR");
        }
        java.util.List<Location> finishPoints = hub.getMapData("DATA_NAME:FINISH_PARKOUR");
        if (finishPoints.isEmpty()) {
            finishPoints = hub.getMapData("CUSTOM_NAME:FINISH_PARKOUR");
        }

        java.util.List<Location> checkpointPoints = new java.util.ArrayList<>();
        java.util.Map<Integer, Location> indexedCheckpoints = new java.util.TreeMap<>();
        java.util.List<Location> unindexedCheckpoints = new java.util.ArrayList<>();

        unindexedCheckpoints.addAll(hub.getMapData("DATA_NAME:PARKOUR_CP"));
        unindexedCheckpoints.addAll(hub.getMapData("CUSTOM_NAME:PARKOUR_CP"));

        boolean hasNumbered = false;
        java.util.regex.Pattern cpPattern = java.util.regex.Pattern.compile("(?i)(?:CUSTOM_NAME|DATA_NAME):(?:PARKOUR_CP|CP)[\\s_]*(\\d+)");
        for (java.util.Map.Entry<String, java.util.List<Location>> entry : hub.getMapDataMap().entrySet()) {
            java.util.regex.Matcher m = cpPattern.matcher(entry.getKey());
            if (m.matches()) {
                try {
                    int index = Integer.parseInt(m.group(1));
                    if (index >= 1) {
                        for (Location loc : entry.getValue()) {
                            indexedCheckpoints.put(index, loc);
                        }
                        hasNumbered = true;
                    }
                } catch (NumberFormatException ignored) {}
            }
        }

        boolean sortByY = hub.getMapData("CUSTOM_NAME:PARKOUR_SORT_BY_Y_FALSE").isEmpty();
        if (hasNumbered) {
            sortByY = false;
            checkpointPoints.addAll(indexedCheckpoints.values());
        } else {
            checkpointPoints.addAll(unindexedCheckpoints);
        }

        if (zonePoints.size() >= 2) {
            Location cornerA = zonePoints.get(0);
            Location cornerB = zonePoints.get(1);
            double minX = Math.min(cornerA.getX(), cornerB.getX());
            double maxX = Math.max(cornerA.getX(), cornerB.getX());
            double minY = Math.min(cornerA.getY(), cornerB.getY()) - 5;
            double maxY = Math.max(cornerA.getY(), cornerB.getY()) + 15;
            double minZ = Math.min(cornerA.getZ(), cornerB.getZ());
            double maxZ = Math.max(cornerA.getZ(), cornerB.getZ());
            Location start = !npcPoints.isEmpty() ? npcPoints.get(0) : createBoundaryStart(cornerA, cornerB);
            Location finish = !finishPoints.isEmpty() ? finishPoints.get(0) : createBoundaryFinish(cornerA, cornerB);

            NonstopParkourData course = new NonstopParkourData("Parkour", start, finish,
                minX, maxX, minY, maxY, minZ, maxZ, checkpointPoints, sortByY);
            _courses.add(course);

            if (_hologramManager != null) {
                int cpIndex = 1;
                for (Location cpLoc : course.getCheckpoints()) {
                    Location holoLoc = cpLoc.clone().add(0, 3.5, 0);
                    _leaderboardHolograms.add(new com.houzicore.shared.core.leaderboard.LeaderboardHologram(
                        _hologramManager, _stats, holoLoc, 
                        com.houzicore.shared.common.util.C.cAqua + "★ Parkour CP " + cpIndex + " ★", 
                        "Parkour.CP." + cpIndex, 10
                    ));
                    cpIndex++;
                }
                
                Location finishHoloLoc = course.finish.clone().add(0, 3.5, 0);
                _leaderboardHolograms.add(new com.houzicore.shared.core.leaderboard.LeaderboardHologram(
                    _hologramManager, _stats, finishHoloLoc, 
                    com.houzicore.shared.common.util.C.cGold + "★ Parkour Finish ★", 
                    "Parkour.CP.Finish", 10
                ));
            }

            if (_npcManager != null) {
                Location parkourNpcLoc = !npcPoints.isEmpty()
                    ? npcPoints.get(0)
                    : course.start.clone();

                _npcEntity = _npcManager.spawnNpc(parkourNpcLoc,
                    com.houzicore.shared.common.util.C.cGreen + "§l⚡ " + com.houzicore.shared.common.util.UtilText.toSmallCaps("Parkour"),
                    com.houzicore.shared.common.util.C.cGray + "กระโดดขึ้นสูงไปให้ถึง!",
                    com.houzicore.shared.common.util.C.cYellow + "§o» คลิกเพื่อเริ่ม «",
                    org.bukkit.entity.Villager.Profession.MASON,
                    org.bukkit.Color.fromRGB(50, 180, 80),
                    player -> {
                        if (_hub.isAdminBuilder(player)) {
                            UtilPlayer.message(player, F.main("Parkour", com.houzicore.shared.common.util.C.cRed + "คุณอยู่ในโหมด AdminBuilder"));
                            return;
                        }
                        if (!_courses.isEmpty()) {
                            if (_active.containsKey(player)) {
                                UtilPlayer.message(player, F.main("Parkour", com.houzicore.shared.common.util.C.cRed + "คุณกำลังเล่นอยู่แล้ว!"));
                                return;
                            }
                            _shop.attemptShopOpen(player);
                        }
                    }
                );
            }
            System.out.println("[ParkourManager] Loaded course with " + checkpointPoints.size() + " checkpoints");
        } else {
            Location world = hub.GetSpawn();
            _courses.add(new NonstopParkourData(
                "Parkour",
                new Location(world.getWorld(), 200, 70, 200),
                new Location(world.getWorld(), 250, 70, 200),
                0,
                190, 260, 60, 120, 190, 210
            ));
        }
    }
    public HubManager getHub() {
        return _hub;
    }

    public void startCourse(Player player, boolean challenge) {
        if (_courses.isEmpty()) return;
        NonstopParkourData course = _courses.iterator().next();
        if (_active.containsKey(player)) {
            UtilPlayer.message(player, F.main("Parkour", com.houzicore.shared.common.util.C.cRed + "คุณกำลังเล่นอยู่แล้ว!"));
            return;
        }
        if (enterCourse(player, course, challenge)) {
            player.teleport(course.start);
        }
    }

    public void reloadCourse() {
        for (Player player : new java.util.ArrayList<>(_active.keySet())) {
            exitCourse(player, false, true);
        }

        _courses.clear();

        if (_npcManager != null && _npcEntity != null) {
            _npcManager.despawnNpc(_npcEntity);
            _npcEntity = null;
        }

        for (com.houzicore.shared.core.leaderboard.LeaderboardHologram holo : _leaderboardHolograms) {
            holo.destroy();
        }
        _leaderboardHolograms.clear();

        loadCourseData(_hub);
    }

    public boolean enterCourse(Player player, NonstopParkourData course, boolean challenge) {
        LobbyTransitionCoordinator transitionCoordinator = _hub.getTransitionCoordinator();
        if (transitionCoordinator.isInAnyLobbyActivity(player) && !transitionCoordinator.isInParkour(player)) {
            boolean isThai = com.houzicore.shared.core.lang.LangManager.get().isThai(player);
            UtilPlayer.message(player, F.main("Parkour", isThai ? C.cRed + "คุณกำลังติดอยู่ในกิจกรรมอื่นอยู่ตอนนี้" : C.cRed + "You are in another activity right now"));
            return false;
        }
        if (!transitionCoordinator.isInParkour(player) && !transitionCoordinator.enterParkour(player)) {
            boolean isThai = com.houzicore.shared.core.lang.LangManager.get().isThai(player);
            UtilPlayer.message(player, F.main("Parkour", isThai ? C.cRed + "ยังไม่สามารถเริ่ม Parkour ได้ตอนนี้" : C.cRed + "Cannot start Parkour right now"));
            return false;
        }

        _isChallenge.put(player, challenge);
        setParkourScoreboard(player, true);
        boolean isThai = com.houzicore.shared.core.lang.LangManager.get().isThai(player);
        com.houzicore.shared.common.util.UtilTextBottom.display(com.houzicore.shared.common.actionbar.ActionBarChannel.TOOL_HINT,
            isThai ? "§a⚡ กระโดดขึ้นไปให้ถึงยอด! §7(/hub เพื่อออก)" : "§a⚡ Jump to the top! §7(/hub to quit)",
            player);

        _zonePresence.add(player.getUniqueId());
        _active.put(player, course);
        _startTime.put(player, System.currentTimeMillis());
        _lastCheckpoint.put(player, course.start.clone());
        _highestCp.put(player, 0);

        int cpCount = course.getCheckpointCount();
        String cpInfo = cpCount > 0
            ? (isThai ? " §7(" + cpCount + " เช็คพอยต์)" : " §7(" + cpCount + " checkpoints)")
            : "";

        BossBar bar = getPlugin().getServer().createBossBar(
            C.cGreen + "▐▐▐▐▐▐ PARKOUR ▐▐▐▐▐▐",
            BarColor.GREEN, BarStyle.SEGMENTED_10);
        bar.setProgress(0.0);
        bar.addPlayer(player);
        _bossBars.put(player, bar);

        UtilPlayer.message(player, F.main("Parkour", isThai
            ? C.cGreen + "เริ่ม! " + C.cGray + "กระโดดขึ้นไปให้ถึงจุดหมาย!" + cpInfo
            : C.cGreen + "Go! " + C.cGray + "Jump your way to the top!" + cpInfo));
        player.playSound(player.getLocation(), Sound.ENTITY_ENDERMAN_TELEPORT, 1f, 1.2f);
        return true;
    }

    public void exitCourse(Player player, boolean finished) {
        exitCourse(player, finished, true);
    }

    private void exitCourse(Player player, boolean finished, boolean restoreLobbyState) {
        setParkourScoreboard(player, false);
        com.houzicore.shared.common.actionbar.ActionBarService.clear(player, com.houzicore.shared.common.actionbar.ActionBarChannel.TOOL_HINT);
        NonstopParkourData course = _active.remove(player);
        Long startedAt = _startTime.remove(player);
        _lastCheckpoint.remove(player);
        _highestCp.remove(player);
        boolean wasChallenge = _isChallenge.getOrDefault(player, false);
        _isChallenge.remove(player);
        ActionBarService.clear(player, ActionBarChannel.GAME_STATUS);

        BossBar bar = _bossBars.remove(player);
        if (bar != null) bar.removeAll();

        if (restoreLobbyState && _hub.getTransitionCoordinator().isInParkour(player)) {
            _hub.getTransitionCoordinator().exitParkour(player);
        }

        if (finished && course != null) {
            long ms = startedAt != null ? System.currentTimeMillis() - startedAt : 0L;
            if (wasChallenge) {
                _stats.setStatIfLower(player, "Parkour.CP.Finish", ms);
            }
            String time = String.format("%d:%05.2f", ms / 60000, (ms % 60000) / 1000.0);
            boolean isThai = com.houzicore.shared.core.lang.LangManager.get().isThai(player);
            UtilPlayer.message(player, F.main("Parkour", C.cGold + (isThai ? "เสร็จแล้ว! เวลา: " : "Finished! Time: ") + time));
            _donation.RewardEssenceLater("Parkour.Finish", player, 80);
            _stats.incrementStat(player, "Parkour.Finish", 1);
            player.playSound(player.getLocation(), Sound.UI_TOAST_CHALLENGE_COMPLETE, 1f, 1.2f);

            // Broadcast to server
            for (Player p : UtilServer.getPlayers()) {
                boolean pThai = com.houzicore.shared.core.lang.LangManager.get().isThai(p);
                UtilPlayer.message(p, F.main("Parkour",
                    C.cYellow + player.getName() + C.cGray + (pThai ? " ผ่านด่าน Parkour ใน " : " completed Parkour in ")
                    + C.cAqua + time));
            }
        }
    }

    @EventHandler
    public void onTick(UpdateEvent event) {
        if (event.getType() != UpdateType.TICK) return;

        for (Player player : UtilServer.getPlayers()) {
            NonstopParkourData course = _active.get(player);
            if (course == null) continue;

            // Exit if left boundary
            if (!course.inBoundary(player.getLocation())) {
                Location respawn = _lastCheckpoint.getOrDefault(player, course.start);
                boolean isThai = com.houzicore.shared.core.lang.LangManager.get().isThai(player);
                player.teleport(respawn);
                player.sendTitle(C.cRed + "✦", isThai ? C.cGray + "ออกนอกโซน! กลับไปเช็คพอยต์" : C.cGray + "Out of bounds! Back to checkpoint", 5, 30, 10);
                player.playSound(player.getLocation(), Sound.ENTITY_ENDERMAN_TELEPORT, 0.8f, 0.7f);
                continue;
            }

            // Check finish
            if (hasReachedFinish(player, course)) {
                exitCourse(player, true);
                suppressAutoEntry(player);
                continue;
            }

            // Check checkpoint proximity
            if (course.getCheckpointCount() > 0) {
                int cpIdx = course.getCheckpointIndex(player.getLocation(), CHECKPOINT_RADIUS);
                int prevHighest = _highestCp.getOrDefault(player, 0);
                if (cpIdx > prevHighest) {
                    _highestCp.put(player, cpIdx);
                    Location cpLoc = course.getCheckpoints().get(cpIdx - 1);
                    _lastCheckpoint.put(player, cpLoc.clone());

                    boolean challenge = _isChallenge.getOrDefault(player, false);
                    if (challenge) {
                        long elapsedMs = System.currentTimeMillis() - _startTime.getOrDefault(player, System.currentTimeMillis());
                        _stats.setStatIfLower(player, "Parkour.CP." + cpIdx, elapsedMs);
                    }

                    boolean isThai = com.houzicore.shared.core.lang.LangManager.get().isThai(player);
                    UtilPlayer.message(player, F.main("Parkour",
                        C.cGreen + "✓ " + (isThai ? "เช็คพอยต์ " : "Checkpoint ")
                        + C.cYellow + cpIdx + C.cGray + "/" + course.getCheckpointCount()));
                    player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_CHIME, 1f, 1.4f);
                    player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 0.6f, 1.2f);

                    // Update BossBar progress
                    BossBar bar = _bossBars.get(player);
                    if (bar != null) {
                        double progress = (double) cpIdx / (course.getCheckpointCount() + 1);
                        bar.setProgress(Math.min(1.0, progress));
                        bar.setColor(BarColor.GREEN);
                        bar.setTitle(C.cGreen + "▐▐▐▐▐▐ PARKOUR §7[§a" + cpIdx + "§7/§f" + course.getCheckpointCount() + "§7] " + C.cGreen + "▐▐▐▐▐▐");
                    }
                }
            }

            // ActionBar timer
            boolean isChallenge = _isChallenge.getOrDefault(player, false);
            int cpReached = _highestCp.getOrDefault(player, 0);
            int cpTotal = course.getCheckpointCount();
            String cpText = cpTotal > 0 ? "  " + C.cGray + "│  " + C.cGreen + cpReached + "§7/" + cpTotal : "";
            if (isChallenge) {
                long elapsedMs = System.currentTimeMillis() - _startTime.getOrDefault(player, System.currentTimeMillis());
                String time = String.format("%d:%05.2f", elapsedMs / 60000, (elapsedMs % 60000) / 1000.0);
                ActionBarService.display(player, ActionBarChannel.GAME_STATUS, Component.text(C.cGray + "⏱ " + C.cYellow + time + cpText));
            } else {
                ActionBarService.display(player, ActionBarChannel.GAME_STATUS, Component.text(C.cGray + "⚡ " + C.cAqua + "Practice" + cpText));
            }
        }

        // ─── Zone-Entry Auto-Detection ───
        for (Player player : UtilServer.getPlayers()) {
            UUID uuid = player.getUniqueId();

            if (_active.containsKey(player)) {
                _zonePresence.add(uuid);
                continue;
            }

            NonstopParkourData course = getCourseAt(player.getLocation());
            boolean nowInZone = course != null;
            boolean wasInZone = _zonePresence.contains(uuid);

            if (!nowInZone) {
                _zonePresence.remove(uuid);
                _zoneSuppressUntil.remove(uuid);
                continue;
            }

            _zonePresence.add(uuid);

            if (!wasInZone && !isAutoEntrySuppressed(uuid)) {
                if (_hub.isAdminBuilder(player)) continue;
                LobbyTransitionCoordinator transitionCoordinator = _hub.getTransitionCoordinator();
                if (transitionCoordinator.isInAnyLobbyActivity(player) && !transitionCoordinator.isInParkour(player)) {
                    continue;
                }
                boolean isThai = com.houzicore.shared.core.lang.LangManager.get().isThai(player);
                com.houzicore.shared.common.util.UtilTextMiddle.display(
                    C.cGreen + "⚡",
                    isThai ? C.cGray + "โซน Parkour — กระโดดขึ้นไป!" : C.cGray + "Parkour Zone — Jump Up!",
                    10, 40, 20, player);
                enterCourse(player, course, false);
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void preventDamage(EntityDamageEvent event) {
        if (!(event.getEntity() instanceof Player)) return;
        Player player = (Player) event.getEntity();
        if (_active.containsKey(player)) {
            event.setCancelled(true);

            // If fall damage, teleport back to last checkpoint
            if (event.getCause() == EntityDamageEvent.DamageCause.FALL) {
                // Only respawn if they fell significantly (more than 5 blocks below their checkpoint)
                Location checkpoint = _lastCheckpoint.getOrDefault(player, _active.get(player).start);
                if (player.getLocation().getY() < checkpoint.getY() - 3) {
                    player.teleport(checkpoint);
                    boolean isThai = com.houzicore.shared.core.lang.LangManager.get().isThai(player);
                    player.sendTitle("", isThai ? C.cGray + "กลับไปเช็คพอยต์" : C.cGray + "Back to checkpoint", 3, 20, 8);
                    player.playSound(player.getLocation(), Sound.ENTITY_ENDERMAN_TELEPORT, 0.8f, 0.7f);
                }
            }
        }
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        UUID uuid = event.getPlayer().getUniqueId();
        _zonePresence.remove(uuid);
        _zoneSuppressUntil.remove(uuid);
        exitCourse(event.getPlayer(), false, false);
        _hub.getTransitionCoordinator().abandonActivity(event.getPlayer());
    }

    private NonstopParkourData getCourseAt(Location location) {
        for (NonstopParkourData course : _courses) {
            if (course.inBoundary(location)) {
                return course;
            }
        }
        return null;
    }

    private boolean hasReachedFinish(Player player, NonstopParkourData course) {
        Location finish = course.finish;
        Location current = player.getLocation();
        return finish != null
            && finish.getWorld() != null
            && finish.getWorld().equals(current.getWorld())
            && current.distanceSquared(finish) <= FINISH_RADIUS_SQ;
    }

    private void suppressAutoEntry(Player player) {
        UUID uuid = player.getUniqueId();
        _zonePresence.add(uuid);
        _zoneSuppressUntil.put(uuid, System.currentTimeMillis() + AUTO_ENTRY_SUPPRESS_MS);
    }

    private boolean isAutoEntrySuppressed(UUID uuid) {
        Long until = _zoneSuppressUntil.get(uuid);
        if (until == null) {
            return false;
        }
        if (until <= System.currentTimeMillis()) {
            _zoneSuppressUntil.remove(uuid);
            return false;
        }
        return true;
    }

    private Location createBoundaryStart(Location cornerA, Location cornerB) {
        double minX = Math.min(cornerA.getX(), cornerB.getX());
        double minY = Math.min(cornerA.getY(), cornerB.getY());
        double minZ = Math.min(cornerA.getZ(), cornerB.getZ());
        double maxX = Math.max(cornerA.getX(), cornerB.getX());
        double maxZ = Math.max(cornerA.getZ(), cornerB.getZ());
        return new Location(cornerA.getWorld(), (minX + maxX) / 2.0, minY + 1.0, (minZ + maxZ) / 2.0);
    }

    private Location createBoundaryFinish(Location cornerA, Location cornerB) {
        double minX = Math.min(cornerA.getX(), cornerB.getX());
        double maxX = Math.max(cornerA.getX(), cornerB.getX());
        double maxY = Math.max(cornerA.getY(), cornerB.getY());
        double minZ = Math.min(cornerA.getZ(), cornerB.getZ());
        double maxZ = Math.max(cornerA.getZ(), cornerB.getZ());
        return new Location(cornerA.getWorld(), (minX + maxX) / 2.0, maxY, (minZ + maxZ) / 2.0);
    }
}
