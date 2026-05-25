package com.houzicore.lobby.hub.modules.farm;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import com.houzicore.shared.MiniPlugin;
import com.houzicore.shared.common.util.C;
import com.houzicore.shared.common.util.F;
import com.houzicore.shared.common.util.UtilPlayer;
import com.houzicore.shared.common.util.UtilServer;
import com.houzicore.shared.common.util.UtilTime;
import com.houzicore.shared.common.actionbar.ActionBarChannel;
import com.houzicore.shared.common.actionbar.ActionBarService;
import com.houzicore.shared.core.donation.DonationManager;
import com.houzicore.shared.core.stats.StatsManager;
import com.houzicore.shared.recharge.Recharge;
import com.houzicore.shared.updater.UpdateType;
import com.houzicore.shared.updater.event.UpdateEvent;
import com.houzicore.lobby.hub.HubManager;
import com.houzicore.lobby.hub.bootstrap.LobbyTransitionCoordinator;

import net.kyori.adventure.text.Component;

public class FarmSimManager extends MiniPlugin {

    private static final long SESSION_DURATION_S = 60L;
    private static final int ESSENCE_BASE = 30;
    private static final int FAST_BREAK_LIMIT_MS = 80; // minimum ms between breaks

    private final HubManager _hub;
    private final DonationManager _donation;
    private final StatsManager _stats;

    private final FarmTemplate _template;
    private final Map<UUID, FarmSimSession> _sessions = new HashMap<>();
    private final Set<UUID> _firstPlayToday = new HashSet<>(); // loaded from DB
    private final Set<UUID> _zonePresence = new HashSet<>();
    private boolean _scoreboardRegistered = false;

    // Farm zone bounding box — dynamic
    private double ZONE_MIN_X = 100, ZONE_MAX_X = 140;
    private double ZONE_MIN_Y = 60, ZONE_MAX_Y = 80;
    private double ZONE_MIN_Z = 100, ZONE_MAX_Z = 140;

    private void registerScoreboard() {
        if (_scoreboardRegistered || com.houzicore.shared.core.scoreboard.ScoreboardManager.getInstance() == null) return;
        _scoreboardRegistered = true;
        com.houzicore.shared.core.scoreboard.ScoreboardData data = com.houzicore.shared.core.scoreboard.ScoreboardManager.getInstance().getData("farm", true);
        data.addElement(new com.houzicore.shared.core.scoreboard.elements.ScoreboardElement() {
            @Override
            public java.util.ArrayList<String> GetLines(com.houzicore.shared.core.scoreboard.ScoreboardManager manager, Player player) {
                java.util.ArrayList<String> output = new java.util.ArrayList<>();
                boolean isThai = com.houzicore.shared.core.lang.LangManager.get().isThai(player);
                output.add("");
                output.add(" §f" + (isThai ? "เกมส์" : "Game") + ": §a" + (isThai ? "ฟาร์มซิม" : "Farm Sim"));
                output.add("");
                FarmSimSession session = _sessions.get(player.getUniqueId());
                if (session != null) {
                    output.add(" §7" + (isThai ? "คะแนน" : "Score") + ": §f" + session.score);
                    output.add(" §7" + (isThai ? "เวลา" : "Time") + ": §f" + session.remainingSeconds(SESSION_DURATION_S) + "s");
                } else {
                    output.add(" §7" + (isThai ? "สถานะ" : "Status") + ": §e" + (isThai ? "รอเริ่ม" : "Waiting"));
                }
                return output;
            }
        });
    }

    private void setFarmScoreboard(Player player, boolean active) {
        registerScoreboard();
        if (com.houzicore.shared.core.scoreboard.ScoreboardManager.getInstance() == null) return;
        com.houzicore.shared.core.scoreboard.PlayerScoreboard ps = com.houzicore.shared.core.scoreboard.ScoreboardManager.getInstance().getPlayerScoreboard(player);
        if (ps == null) return;
        ps.setHidden(false);
        ps.setScoreboardData(active ? "farm" : "default");
    }

    public FarmSimManager(HubManager hub, DonationManager donation, StatsManager stats) {
        this(hub, donation, stats, null);
    }

    public FarmSimManager(HubManager hub, DonationManager donation, StatsManager stats,
            com.houzicore.lobby.hub.modules.LobbyNpcManager npcManager) {
        super("Farm Sim", hub.getPlugin());
        _hub = hub;
        _donation = donation;
        _stats = stats;

        java.util.List<Location> npcPoints = hub.getMapData("DATA_NAME:NPC_FARM");
        Location origin = npcPoints.isEmpty() ? new Location(hub.GetSpawn().getWorld(), 120, 65, 120) : npcPoints.get(0);
        _template = new FarmTemplate(origin);

        java.util.List<Location> zonePoints = hub.getMapData("DATA_NAME:ZONE_FARM");
        if (zonePoints.size() >= 2) {
            ZONE_MIN_X = Math.min(zonePoints.get(0).getX(), zonePoints.get(1).getX());
            ZONE_MAX_X = Math.max(zonePoints.get(0).getX(), zonePoints.get(1).getX());
            ZONE_MIN_Y = Math.min(zonePoints.get(0).getY(), zonePoints.get(1).getY()) - 5;
            ZONE_MAX_Y = Math.max(zonePoints.get(0).getY(), zonePoints.get(1).getY()) + 20;
            ZONE_MIN_Z = Math.min(zonePoints.get(0).getZ(), zonePoints.get(1).getZ());
            ZONE_MAX_Z = Math.max(zonePoints.get(0).getZ(), zonePoints.get(1).getZ());
        }

        // Spawn Farm NPC — only if MapBuilder data exists
        if (npcManager != null) {
            // Guard: skip if no explicit NPC point AND no ZONE_FARM is defined
            if (npcPoints.isEmpty() && zonePoints.size() < 2) {
                System.out.println("[FarmSimManager] No NPC_FARM or ZONE_FARM in WorldConfig — skipping NPC spawn");
            } else {
                npcManager.spawnNpc(origin.clone().add(3, 0, 0),
                    C.cGreen + "§l🌾 " + com.houzicore.shared.common.util.UtilText.toSmallCaps("Farm Sim"),
                    C.cGray + "เก็บเกี่ยวให้ทันเวลา",
                    C.cYellow + "§o» คลิกเพื่อเริ่ม «",
                    org.bukkit.entity.Villager.Profession.FARMER,
                    org.bukkit.Color.fromRGB(90, 190, 90),
                    player -> {
                        if (_hub.isAdminBuilder(player)) {
                            UtilPlayer.message(player, com.houzicore.shared.common.util.F.main("Farm", C.cRed + "คุณอยู่ในโหมด AdminBuilder"));
                            return;
                        }
                        if (_sessions.containsKey(player.getUniqueId())) {
                            UtilPlayer.message(player, F.main("Farm", C.cRed + "คุณกำลังเล่นอยู่แล้ว!"));
                            return;
                        }

                        if (startSession(player)) {
                            setFarmScoreboard(player, true);
                            com.houzicore.shared.common.util.UtilTextMiddle.display(
                                C.cGreen + "🌾",
                                com.houzicore.shared.core.lang.LangManager.get().isThai(player) ? C.cGray + "เข้าสู่ Farm Sim" : C.cGray + "Farm Sim",
                                10, 40, 20, player);
                            com.houzicore.shared.common.util.UtilTextBottom.display(ActionBarChannel.TOOL_HINT,
                                com.houzicore.shared.core.lang.LangManager.get().isThai(player) ? "§a🌾 ตัดให้ไว เก็บให้คุ้มใน 60 วินาที" : "§a🌾 Harvest fast and score big in 60 seconds",
                                player);
                            player.teleport(_template.getOrigin().clone().add(0.5, 1.0, 0.5));
                        }
                    }
                );
            }
        }
    }

    public boolean inZone(Location loc) {
        return loc.getX() >= ZONE_MIN_X && loc.getX() <= ZONE_MAX_X
            && loc.getY() >= ZONE_MIN_Y && loc.getY() <= ZONE_MAX_Y
            && loc.getZ() >= ZONE_MIN_Z && loc.getZ() <= ZONE_MAX_Z;
    }

    // Called by NPC click
    public boolean startSession(Player player) {
        UUID uuid = player.getUniqueId();
        LobbyTransitionCoordinator transitionCoordinator = _hub.getTransitionCoordinator();

        if (_sessions.containsKey(uuid)) {
            UtilPlayer.message(player, F.main("Farm", C.cRed + "คุณกำลังเล่นอยู่แล้ว!"));
            return false;
        }
        if (transitionCoordinator.isInAnyLobbyActivity(player) && !transitionCoordinator.isInFarm(player)) {
            UtilPlayer.message(player, F.main("Farm", C.cRed + "คุณกำลังติดอยู่ในกิจกรรมอื่นอยู่ตอนนี้"));
            return false;
        }
        if (!Recharge.Instance.use(player, "FarmSim.Start", 30000, true, false)) return false;
        if (!transitionCoordinator.isInFarm(player) && !transitionCoordinator.enterFarm(player)) {
            UtilPlayer.message(player, F.main("Farm", C.cRed + "ยังไม่สามารถเริ่ม Farm ได้ตอนนี้"));
            return false;
        }

        boolean firstToday = !_firstPlayToday.contains(uuid);
        _firstPlayToday.add(uuid);

        _template.snapshot(uuid);
        _sessions.put(uuid, new FarmSimSession(player, firstToday));

        UtilPlayer.message(player, F.main("Farm", C.cGreen + "เริ่ม! " + C.cGray + "เก็บให้มากที่สุดใน " + SESSION_DURATION_S + " วินาที!"));
        player.playSound(player.getLocation(), Sound.ENTITY_ENDERMAN_TELEPORT, 1f, 1.2f);
        return true;
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();
        UUID uuid = player.getUniqueId();
        FarmSimSession session = _sessions.get(uuid);
        if (session == null) return;

        Block block = event.getBlock();
        if (!inZone(block.getLocation())) return;

        int score = FarmTemplate.scoreFor(block.getType());
        if (score <= 0) {
            event.setCancelled(true);
            return;
        }

        // Fast-break anti-abuse
        long now = System.currentTimeMillis();
        if (now - session.lastBlockBreakTime < FAST_BREAK_LIMIT_MS) {
            event.setCancelled(true);
            UtilPlayer.message(player, F.main("Farm", C.cRed + "ตัดช้าลงหน่อยนะ!"));
            return;
        }
        session.lastBlockBreakTime = now;
        session.score += score;

        // Prevent drop
        event.setDropItems(false);
        player.playSound(block.getLocation(), Sound.BLOCK_GRASS_BREAK, 1f, 1f);
        ActionBarService.display(player, ActionBarChannel.GAME_STATUS, Component.text(
            "🌾 " + C.cYellow + session.score + " แต้ม  " + C.cGray + "│  " + C.cGreen + "เวลาเหลือ: " + C.cYellow + session.remainingSeconds(SESSION_DURATION_S) + "s"));
    }

    @EventHandler
    public void onTick(UpdateEvent event) {
        if (event.getType() == UpdateType.FAST) {
            for (Player player : UtilServer.getPlayers()) {
                UUID uuid = player.getUniqueId();
                boolean nowInZone = inZone(player.getLocation());
                boolean wasInZone = _zonePresence.contains(uuid);

                if (nowInZone && !wasInZone) {
                    _zonePresence.add(uuid);
                    if (!_hub.isAdminBuilder(player) && !_sessions.containsKey(uuid) && !_hub.getTransitionCoordinator().isInAnyLobbyActivity(player)) {
                        boolean isThai = com.houzicore.shared.core.lang.LangManager.get().isThai(player);
                        com.houzicore.shared.common.util.UtilTextMiddle.display(
                            C.cGreen + "🌾",
                            isThai ? C.cGray + "โซน Farm Sim" : C.cGray + "Farm Sim Zone",
                            10, 35, 15, player);
                        com.houzicore.shared.common.util.UtilTextBottom.display(ActionBarChannel.TOOL_HINT,
                            isThai ? "§a🌾 คลิก NPC เพื่อเริ่มเก็บเกี่ยว" : "§a🌾 Click the NPC to start harvesting",
                            player);
                    }
                } else if (!nowInZone && wasInZone) {
                    _zonePresence.remove(uuid);
                    if (!_sessions.containsKey(uuid)) {
                        com.houzicore.shared.common.actionbar.ActionBarService.clear(player, ActionBarChannel.TOOL_HINT);
                    }
                }
            }
            return;
        }

        if (event.getType() != UpdateType.SEC) return;

        for (FarmSimSession session : new java.util.ArrayList<>(_sessions.values())) {
            Player player = session.player;
            if (!player.isOnline()) { cleanupSession(session, false, false); continue; }
            if (!inZone(player.getLocation())) {
                UtilPlayer.message(player, F.main("Farm", C.cRed + "คุณเดินออกจากโซน Farm แล้ว"));
                cleanupSession(session, false, true);
                continue;
            }

            long remaining = session.remainingSeconds(SESSION_DURATION_S);
            if (remaining <= 0) {
                cleanupSession(session, true, true);
                continue;
            }

            // BossBar progress
            ActionBarService.display(player, ActionBarChannel.GAME_STATUS, Component.text(
                "🌾 " + C.cYellow + session.score + " แต้ม  " + C.cGray + "│  " + C.cGreen + remaining + "s เหลือ"));
        }
    }

    private void cleanupSession(FarmSimSession session, boolean timeUp, boolean restoreLobbyState) {
        Player player = session.player;
        UUID uuid = player.getUniqueId();
        _sessions.remove(uuid);
        _template.restore(uuid);
        setFarmScoreboard(player, false);
        com.houzicore.shared.common.actionbar.ActionBarService.clear(player, ActionBarChannel.TOOL_HINT);

        if (restoreLobbyState && _hub.getTransitionCoordinator().isInFarm(player)) {
            _hub.getTransitionCoordinator().exitFarm(player);
        }

        if (!timeUp) return;

        int score = session.score;
        int essence = ESSENCE_BASE + score;

        // Daily first-time bonus
        if (session.firstPlayToday) essence = (int) (essence * 1.25);

        _donation.RewardEssenceLater("FarmSim.Score", player, essence);
        _stats.incrementStat(player, "Farm.Score", score);

        player.sendTitle(C.cGold + "§l🌾 เสร็จแล้ว!", C.cGray + "คะแนน: " + C.cYellow + score + " แต้ม", 10, 60, 20);
        player.playSound(player.getLocation(), Sound.UI_TOAST_CHALLENGE_COMPLETE, 1f, 1f);
        UtilPlayer.message(player, F.main("Farm",
            C.cGray + "คะแนน: " + C.cYellow + score
            + C.cGray + "  |  Essence: " + C.cGreen + "+" + essence));

        // Check personal best and broadcast if top 10  
        saveToDB(player, score);
    }

    private void saveToDB(Player player, int score) {
        // Async save to farm_leaderboard table
        getPlugin().getServer().getScheduler().runTaskAsynchronously(getPlugin(), () -> {
            try {
                // TODO: wire to DBPool when real coordinates are set
                // DBPool.getConnection().executeUpdate("INSERT INTO farm_leaderboard ...")
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        UUID uuid = event.getPlayer().getUniqueId();
        _zonePresence.remove(uuid);
        FarmSimSession session = _sessions.remove(uuid);
        if (session != null) {
            _template.restore(uuid);
        }
        setFarmScoreboard(event.getPlayer(), false);
    }
}
