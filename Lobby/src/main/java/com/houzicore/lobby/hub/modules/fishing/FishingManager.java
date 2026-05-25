package com.houzicore.lobby.hub.modules.fishing;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Fish;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.PlayerFishEvent;
import org.bukkit.event.player.PlayerFishEvent.State;
import org.bukkit.event.player.PlayerQuitEvent;

import com.houzicore.shared.MiniPlugin;
import com.houzicore.shared.account.CoreClientManager;
import com.houzicore.shared.common.actionbar.ActionBarChannel;
import com.houzicore.shared.common.util.C;
import com.houzicore.shared.common.util.F;
import com.houzicore.shared.common.util.UtilTextBottom;
import com.houzicore.shared.common.util.UtilMath;
import com.houzicore.shared.common.util.UtilParticle;
import com.houzicore.shared.common.util.UtilParticle.ParticleType;
import com.houzicore.shared.common.util.UtilParticle.ViewDist;
import com.houzicore.shared.common.util.UtilPlayer;
import com.houzicore.shared.common.util.UtilServer;
import com.houzicore.shared.core.donation.DonationManager;
import com.houzicore.shared.core.stats.StatsManager;
import com.houzicore.shared.recharge.Recharge;
import com.houzicore.shared.updater.UpdateType;
import com.houzicore.shared.updater.event.UpdateEvent;
import com.houzicore.lobby.hub.HubManager;
import com.houzicore.lobby.hub.modules.fishing.FishType.CatchGrade;

public class FishingManager extends MiniPlugin {

    private static final double ZONE_ENTRY_RADIUS_SQ = 20 * 20; // 20 blocks
    private static final double ZONE_EXIT_RADIUS_SQ = 25 * 25;  // 25 blocks (hysteresis)

    private final HubManager _hub;
    private final DonationManager _donation;
    private final StatsManager _stats;
    private final CoreClientManager _clients;

    private final List<FishingPond> _ponds = new ArrayList<>();
    private final Map<UUID, FishingSession> _sessions = new HashMap<>();
    private final java.util.Set<UUID> _inZone = new java.util.HashSet<>();
    private final java.util.WeakHashMap<org.bukkit.entity.Entity, Long> _scaredFishes = new java.util.WeakHashMap<>();
    private com.houzicore.lobby.hub.modules.LobbyNpcManager _npcManager;
    private com.houzicore.shared.core.gadget.GadgetManager _gadgetManager;

    private com.houzicore.shared.core.gadget.GadgetManager getGadgetManager() {
        if (_gadgetManager == null) {
            try {
                _gadgetManager = com.houzicore.shared.core.plugin.PluginRegistry.require(com.houzicore.shared.core.gadget.GadgetManager.class);
            } catch (Exception e) {}
        }
        return _gadgetManager;
    }

    private boolean _scoreboardRegistered = false;
    private long _lastTipMs = System.currentTimeMillis();

    private void registerScoreboard() {
        if (_scoreboardRegistered || com.houzicore.shared.core.scoreboard.ScoreboardManager.getInstance() == null) return;
        _scoreboardRegistered = true;
        com.houzicore.shared.core.scoreboard.ScoreboardData data = com.houzicore.shared.core.scoreboard.ScoreboardManager.getInstance().getData("fishing", true);
        data.addElement(new com.houzicore.shared.core.scoreboard.elements.ScoreboardElement() {
            @Override

            public java.util.ArrayList<String> GetLines(com.houzicore.shared.core.scoreboard.ScoreboardManager manager, org.bukkit.entity.Player player) {
                java.util.ArrayList<String> output = new java.util.ArrayList<>();
                boolean isThai = com.houzicore.shared.core.lang.LangManager.get().isThai(player);
                output.add("");
                output.add(" §f" + (isThai ? "เกมส์" : "Game") + ": §b" + (isThai ? "ตกปลา" : "Fishing"));
                output.add("");
                FishingSession session = _sessions.get(player.getUniqueId());
                if (session != null) {
                    boolean isFrenzy = (session.sessionCatches + 1) % 10 == 0;
                    Map<FishType, Double> chances = FishType.getDropChances(session.combo, isFrenzy);
                    
                    String status = session.phase.equals("FISHING") ? "§eรอปลากินเหยื่อ..." : session.phase.equals("BITE") ? "§c❗ ดึงเบ็ด ❗" : "§aกำลังเตรียม...";
                    String statusEN = session.phase.equals("FISHING") ? "§eWaiting..." : session.phase.equals("BITE") ? "§c❗ REEL ❗" : "§aIdle";
                    
                    output.add(" §7" + (isThai ? "สถานะ" : "Status") + ": " + (isThai ? status : statusEN));
                    output.add(" §d⚡ คอมโบ (Combo): §f" + session.combo);
                    output.add("");
                    output.add(" §f§l" + (isThai ? "ประวัติล่าสุด:" : "Recent:"));
                    if (session.recentCatches.isEmpty()) {
                        output.add(" §7" + (isThai ? "- ยังไม่มี -" : "- None -"));
                    } else {
                        int index = 1;
                        for (FishType type : session.recentCatches) {
                            output.add(" §7" + index + ". " + type.color + type.getName(isThai));
                            index++;
                        }
                    }
                    if (isFrenzy) {
                        output.add("");
                        output.add(" §c§lFRENZY MODE!");
                    }
                } else {
                    output.add(" §7" + (isThai ? "สถานะ" : "Status") + ": §a" + (isThai ? "กำลังพัก" : "Idle"));
                }
                return output;
            }
        });
    }

    private void setFishingScoreboard(org.bukkit.entity.Player p, boolean active) {
        registerScoreboard();
        if (com.houzicore.shared.core.scoreboard.ScoreboardManager.getInstance() != null) {
            com.houzicore.shared.core.scoreboard.PlayerScoreboard ps = com.houzicore.shared.core.scoreboard.ScoreboardManager.getInstance().getPlayerScoreboard(p);
            if (ps != null) {
                ps.setHidden(false);
                ps.setScoreboardData(active ? "fishing" : "default");
            }
        }
    }

    public FishingManager(HubManager hub, DonationManager donation, StatsManager stats, CoreClientManager clients) {
        this(hub, donation, stats, clients, null);
    }

    public FishingManager(HubManager hub, DonationManager donation, StatsManager stats, CoreClientManager clients,
                          com.houzicore.lobby.hub.modules.LobbyNpcManager npcManager) {
        super("Fishing Manager", hub.getPlugin());
        _hub = hub;
        _donation = donation;
        _stats = stats;
        _clients = clients;

        // Load ponds from MapBuilder
        List<Location> rawZonePoints = hub.getMapData("ZONE_FISHING");
        if (rawZonePoints == null || rawZonePoints.isEmpty()) {
            rawZonePoints = hub.getMapData("DATA_NAME:ZONE_FISHING");
        }
        
        List<Location> zonePoints = new ArrayList<>();
        if (rawZonePoints != null) {
            for (Location loc : rawZonePoints) {
                if (!zonePoints.contains(loc)) zonePoints.add(loc);
            }
        }

        for (Location loc : zonePoints) {
            _ponds.add(new FishingPond(loc));
        }

        // Spawn Fishing NPC — only if at least one explicit point exists in WorldConfig
        _npcManager = npcManager;
        if (npcManager != null) {
            java.util.List<Location> rawNpcLocs = hub.getMapData("DATA_NAME:NPC_FISHING");
            java.util.List<Location> npcLocs = new ArrayList<>();
            for (Location loc : rawNpcLocs) {
                boolean found = false;
                for (Location c : npcLocs) if (c.distanceSquared(loc) < 1) found = true;
                if (!found) npcLocs.add(loc);
            }

            // Guard: skip NPC entirely if no explicit location AND no pond is loaded
            if (npcLocs.isEmpty() && _ponds.isEmpty()) {
                System.out.println("[FishingManager] No NPC_FISHING or ZONE_FISHING in WorldConfig — skipping NPC spawn");
            } else {
            Location npcLoc = npcLocs.isEmpty()
                ? _ponds.get(0).center.clone().add(3, 0, 0)
                : npcLocs.get(0);

            npcManager.spawnNpc(npcLoc,
                C.cAqua + "§l🎣 " + com.houzicore.shared.common.util.UtilText.toSmallCaps("Fishing"),
                C.cGray + "ตกปลาเพื่อรับ Essence",
                C.cYellow + "§o» คลิกเพื่อเข้าโซน «",
                org.bukkit.entity.Villager.Profession.FISHERMAN,
                org.bukkit.Color.fromRGB(60, 140, 200),
                player -> {
                    if (_hub.isAdminBuilder(player)) {
                        UtilPlayer.message(player, F.main("Fishing", C.cRed + "คุณอยู่ในโหมด AdminBuilder"));
                        return;
                    }
                    if (_ponds.isEmpty()) return;

                    com.houzicore.lobby.hub.bootstrap.LobbyTransitionCoordinator tc = _hub.getTransitionCoordinator();
                    if (!tc.isInFishing(player) && !tc.enterFishing(player)) {
                        UtilPlayer.message(player, F.main("Fishing", C.cRed + "คุณกำลังติดอยู่ในกิจกรรมอื่นอยู่ตอนนี้"));
                        return;
                    }

                    _inZone.add(player.getUniqueId());
                    setFishingScoreboard(player, true);
                    // Teleport player to the closest pond
                    Location pondLoc = _ponds.get(0).center.clone().add(2, 1, 0);
                    player.teleport(pondLoc);
                    boolean isThai = com.houzicore.shared.core.lang.LangManager.get().isThai(player);
                    com.houzicore.shared.common.util.UtilTextMiddle.display(
                        C.cAqua + "🎣",
                        isThai ? C.cGray + "เข้าสู่โซนตกปลา" : C.cGray + "Fishing Zone",
                        10, 40, 20, player);
                    UtilTextBottom.display(ActionBarChannel.TOOL_HINT,
                        isThai ? "§b🎣 เหวี่ยงเบ็ดได้เลยทุกทิศ §7(เดินออกโซนเพื่อออก)" : "§b🎣 Cast freely in the pond area §7(leave the zone to exit)",
                        player);
                    UtilPlayer.message(player, F.main("Fishing",
                        isThai ? C.cGreen + "เข้าสู่โซนตกปลา! " + C.cGray + "เหวี่ยงเบ็ดลงน้ำเลย!"
                              : C.cGreen + "Welcome to the Fishing Zone! " + C.cGray + "Cast into the water!"));
                }
            );
            } // end guard
        }
    }

    private FishingPond getNearestPond(Location loc, double maxRadiusSquared) {
        if (_ponds.isEmpty()) return null;
        for (FishingPond pond : _ponds) {
            if (pond.center.getWorld() != loc.getWorld()) continue;
            if (pond.isNear(loc, maxRadiusSquared)) return pond;
        }
        return null;
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onFish(PlayerFishEvent event) {
        Player player = event.getPlayer();
        UUID uuid = player.getUniqueId();

        if (event.getState() == State.FISHING) {

            if (!_inZone.contains(uuid)) return;
            if (event.getHook() == null) return;
            Location hookLoc = event.getHook().getLocation();
            
            // Validate: Player must be within playerRadius (60)
            FishingPond playerPond = getNearestPond(player.getLocation(), 60 * 60);
            if (playerPond == null) {
                cancelSession(uuid, player, "You are too far from the fishing area.");
                return;
            }

            FishingSession session = _sessions.computeIfAbsent(uuid, k -> new FishingSession());
            session.activePond = playerPond;
            session.phase = "FISHING";
            session.biteTimeMs = 0;

            // AFK Checking
            if (session.lastCastLoc != null) {
                double dist = hookLoc.distanceSquared(session.lastCastLoc);
                long elapsed = System.currentTimeMillis() - session.lastCastTime;
                
                // Suspicion rules: very small shift and fast recast
                if (dist < 0.04 && elapsed < 3500) {
                    session.suspectScore++;
                    if (session.suspectScore >= 3) {
                        UtilPlayer.message(player, F.main("Fishing", C.cRed + "โปรดขยับทุ่นเบ็ดของคุณบ้าง ไม่ใช่ตกซ้ำจุดเดิมเป๊ะๆ!"));
                        event.setCancelled(true);
                        cancelSession(uuid, player, null);
                        return;
                    }
                } else {
                    session.suspectScore = Math.max(0, session.suspectScore - 1);
                }
            }

            session.lastCastLoc = event.getHook().getLocation();
            session.lastCastTime = System.currentTimeMillis();
            
            session.hook = event.getHook();
            if (session.visualFish != null) {
                if (session.visualFish.getPassengers().contains(session.hook)) {
                    session.visualFish.removePassenger(session.hook);
                }
                session.visualFish = null;
            }
            session.fishSpawned = false;
            
            session.hook.setWaitTime(Integer.MAX_VALUE);
            session.hook.setWaitTime(Integer.MAX_VALUE, Integer.MAX_VALUE); 
            session.hook.setApplyLure(false); 
            session.hook.setLureTime(0, 0); 
            session.hook.setLureAngle(0, 0);

            session.customWaitTimer = 140 + UtilMath.random.nextInt(60);
            
            UtilTextBottom.display(ActionBarChannel.GAME_EVENT, "§b§lตกปลา §8» §7กำลังรอจังหวะ...", player);
            return;
        }

        FishingSession session = _sessions.get(uuid);

        // Vanilla BITE is disabled. All BITE states are manually managed now.
        
        if (event.getState() == State.FAILED_ATTEMPT || event.getState() == State.REEL_IN || event.getState() == State.CAUGHT_FISH) {
            if (session != null && session.phase.equals("BITE")) {
                if (session.visualFish != null) {
                    if (session.hook != null && session.visualFish.getPassengers().contains(session.hook)) {
                        session.visualFish.removePassenger(session.hook);
                    }
                    session.visualFish.remove();
                    session.visualFish = null;
                }
                if (event.getHook() != null) {
                    event.getHook().remove();
                }

                CatchGrade grade = CatchGrade.GOOD;
                long reactionMs = System.currentTimeMillis() - session.biteTimeMs;
                if (reactionMs <= 1000) {
                    grade = CatchGrade.PERFECT;
                } else if (reactionMs <= 2000) {
                    grade = CatchGrade.GOOD;
                } else {
                    grade = CatchGrade.SLOW;
                }

                if (event.getCaught() != null) event.getCaught().remove();
                event.setExpToDrop(0);
                boolean isThai = com.houzicore.shared.core.lang.LangManager.get().isThai(player);

                if (grade == CatchGrade.PERFECT) {
                    player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1f, 2f);
                    UtilTextBottom.display(ActionBarChannel.GAME_EVENT, "§e§l✨ PERFECT CATCH! ✨ §8(" + reactionMs + "ms)", player);
                } else if (grade == CatchGrade.GOOD) {
                    player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1f, 1f);
                    UtilTextBottom.display(ActionBarChannel.GAME_EVENT, isThai ? "§a§l✔ ยอดเยี่ยม! §8(" + reactionMs + "ms)" : "§a§l✔ GREAT! §8(" + reactionMs + "ms)", player);
                } else {
                    player.playSound(player.getLocation(), Sound.BLOCK_ANVIL_LAND, 0.5f, 0.5f);
                    UtilTextBottom.display(ActionBarChannel.GAME_EVENT, isThai ? "§c§l🐢 ช้าไปนิด! §8(" + reactionMs + "ms)" : "§c§l🐢 Too slow! §8(" + reactionMs + "ms)", player);
                }

                boolean isFrenzy = (session.sessionCatches + 1) % 10 == 0;
                FishType caught = FishType.draw(grade, session.combo, isFrenzy);
                org.bukkit.inventory.ItemStack offHand = player.getInventory().getItemInOffHand();
                boolean isOffhandBait = offHand != null && offHand.getType() == Material.BREAD && offHand.getItemMeta() != null && (offHand.getItemMeta().getDisplayName().contains("เหยื่อพรีเมียม") || offHand.getItemMeta().getDisplayName().contains("Premium Bait"));
                
                boolean isBaitEquipped = false;
                try {
                    com.houzicore.shared.core.gadget.GadgetManager gman = com.houzicore.shared.core.plugin.PluginRegistry.require(com.houzicore.shared.core.gadget.GadgetManager.class);
                    if (gman != null) isBaitEquipped = gman.getActive(player, com.houzicore.shared.core.gadget.types.GadgetType.Bait) instanceof com.houzicore.shared.core.gadget.types.BaitGadget;
                } catch (Exception ignored) {}
                
                boolean isBait = isOffhandBait || isBaitEquipped;

                if (!isBait && caught == FishType.JUNK && session.sessionCatches == 1) {
                    org.bukkit.inventory.ItemStack bait = new org.bukkit.inventory.ItemStack(Material.BREAD, 5);
                    org.bukkit.inventory.meta.ItemMeta meta = bait.getItemMeta();
                    if (meta != null) {
                        meta.setDisplayName(isThai ? "§eเหยื่อพรีเมียม" : "§ePremium Bait");
                        meta.setLore(java.util.Arrays.asList(isThai ? "§7ถือไว้ในมือซ้ายตอนตกปลาเพื่อการันตีปลากินเหยื่อ 100%" : "§7Hold in offhand while fishing to guarantee 100% bite chance."));
                        bait.setItemMeta(meta);
                    }
                    player.getInventory().addItem(bait);
                    UtilTextBottom.display(ActionBarChannel.GAME_EVENT, isThai ? "§e§l💡 เคล็ดลับ: §fลองถือเหยื่อพรีเมียมมือซ้ายสิ! หรือสวมใส่ใน Cosmetic Box!" : "§e§l💡 TIP: §fHold Premium Bait offhand OR equip it from Cosmetic Box!", player);
                }

                // Tension Pulling
                if (caught == FishType.EPIC || caught == FishType.LEGENDARY) {
                    org.bukkit.Location hookLoc = session.lastCastLoc; 
                    if (hookLoc != null) {
                        org.bukkit.util.Vector pullDir = hookLoc.toVector().subtract(player.getLocation().toVector()).normalize();
                        com.houzicore.shared.common.util.UtilAction.velocity(player, pullDir, 1.2, false, 0, 0.4, 2.0, true);
                        player.playSound(player.getLocation(), Sound.ENTITY_IRON_GOLEM_ATTACK, 1f, 0.5f);
                        player.playSound(player.getLocation(), Sound.ENTITY_FISHING_BOBBER_SPLASH, 2f, 0.5f);
                        UtilTextBottom.display(ActionBarChannel.GAME_EVENT, isThai ? "§c§lเย่อ!! ปลายักษ์กระชากเบ็ด!" : "§c§lHEAVY PULL!! Big catch!", player);
                    }
                }

                session.pendingRollResult = caught;
                session.lastGrade = grade;
                session.isFrenzy = isFrenzy;
                session.phase = "IDLE";
                giveReward(player, session);
                
                event.setCancelled(true);
                return;
            } else {
                cancelSession(uuid, player, null);
                return;
            }
        }
        
        if (event.getState() == State.IN_GROUND || event.getState() == State.CAUGHT_ENTITY) {
            cancelSession(uuid, player, null);
        }
    }

    @EventHandler
    public void updateActiveFishing(UpdateEvent event) {
        if (event.getType() != UpdateType.TICK) return;
        
        for (Player player : UtilServer.getPlayers()) {
            FishingSession session = _sessions.get(player.getUniqueId());
            if (session == null) continue;

            if (System.currentTimeMillis() - session.lastPlaytimeTick >= 60000) {
                if (!session.phase.equals("IDLE")) {
                    _stats.incrementStat(player, "Fishing.PlayTime", 1);
                }
                session.lastPlaytimeTick = System.currentTimeMillis();
            }

            if (System.currentTimeMillis() - _lastTipMs > 90000) {
                _lastTipMs = System.currentTimeMillis();
                for (UUID uid : _inZone) {
                    Player p = org.bukkit.Bukkit.getPlayer(uid);
                    if (p != null) {
                        boolean isThai = com.houzicore.shared.core.lang.LangManager.get().isThai(p);
                        FishingSession s = _sessions.get(uid);
                        int c = s != null ? s.combo : 0;
                        boolean f = s != null && s.isFrenzy;
                        Map<FishType, Double> cx = FishType.getDropChances(c, f);
                        
                        UtilPlayer.message(p, F.main("Fishing", isThai ? "§e§lTIPS> §7อัตราดรอปเป้าหมายของคุณ:" : "§e§lTIPS> §7Your estimated drop chance:"));
                        StringBuilder sb = new StringBuilder("§r");
                        for (FishType type : FishType.values()) {
                            double chance = cx.getOrDefault(type, 0.0);
                            if (chance <= 0) continue;
                            sb.append(type.color).append(type.getName(isThai)).append(" §8(").append(String.format("%.1f", chance)).append("%) ");
                        }
                        p.sendMessage(sb.toString());
                    }
                }
            }

            UUID uuid = player.getUniqueId();
            
            com.houzicore.shared.core.gadget.GadgetManager gm = getGadgetManager();
            
            com.houzicore.shared.core.gadget.types.BaitGadget bg = null;
            boolean hasBaitGadget = false;
            
            if (gm != null) {
                com.houzicore.shared.core.gadget.types.Gadget equippedBait = gm.getActive(player, com.houzicore.shared.core.gadget.types.GadgetType.Bait);
                if (equippedBait instanceof com.houzicore.shared.core.gadget.types.BaitGadget) {
                    bg = (com.houzicore.shared.core.gadget.types.BaitGadget) equippedBait;
                    if (bg.hasAmmo(player)) {
                        hasBaitGadget = true;
                    } else {
                        bg.Disable(player);
                    }
                }
            }
            
            if (hasBaitGadget && session.hook != null && session.hook.isValid() && (session.phase.equals("FISHING") || session.phase.equals("BITE"))) {
                bg.playBobberEffect(session.hook);
            }
            
            if (session.phase.equals("BITE")) {
                if (session.hook != null && session.hook.isValid() && session.visualFish != null && session.visualFish.isValid()) {
                    boolean isThai = com.houzicore.shared.core.lang.LangManager.get().isThai(player);
                    org.bukkit.Location hLoc = session.hook.getLocation();
                    
                    if (session.bobbingTicksLeft % 10 == 0) {
                        session.bobbingDown = !session.bobbingDown;
                        if (session.bobbingDown) {
                            hLoc.subtract(0, 0.4, 0); // Bob down
                            player.playSound(hLoc, Sound.ENTITY_FISHING_BOBBER_SPLASH, 0.6f, 1.2f);
                            UtilParticle.PlayParticle(ParticleType.SPLASH, hLoc, 0.2f, 0f, 0.2f, 0.05f, 15, ViewDist.SHORT, UtilServer.getPlayers());
                        } else {
                            hLoc.add(0, 0.4, 0); // Bob up
                        }
                    }
                    session.bobbingTicksLeft--;
                    if (session.bobbingTicksLeft <= 0) {
                        if (session.hook != null && session.visualFish.getPassengers().contains(session.hook)) {
                            session.visualFish.removePassenger(session.hook);
                        }
                        org.bukkit.util.Vector escapeDir = session.visualFish.getLocation().getDirection().multiply(0.3);
                        session.visualFish.setVelocity(escapeDir);
                        session.visualFish = null;
                        
                        player.playSound(player.getLocation(), Sound.ENTITY_FISHING_BOBBER_RETRIEVE, 0.8f, 0.5f);
                        cancelSession(uuid, player, isThai ? "ปลาหลุดไปแล้ว! ดึงเร็วกว่านี้นะ!" : "The fish got away! Reel faster next time!");
                        continue;
                    }
                }
            }

            if (session.phase.equals("FISHING")) {
                if (session.hook != null && session.hook.isValid()) {
                    org.bukkit.Location hookLoc = session.hook.getLocation();
                    session.customWaitTimer--;
                    int wait = session.customWaitTimer;
                    
                    if (wait > 0 && wait <= 100 && !session.fishSpawned) {
                        session.fishSpawned = true;
                        
                        org.bukkit.entity.Entity chosenFish = null;
                        if (session.activePond != null && !session.activePond.ambientFishes.isEmpty()) {
                            java.util.List<org.bukkit.entity.Entity> validFishes = new java.util.ArrayList<>();
                            for (org.bukkit.entity.Entity f : session.activePond.ambientFishes) {
                                if (f != null && f.isValid() && f.getLocation().distanceSquared(hookLoc) < 144) { 
                                    if (_scaredFishes.containsKey(f) && System.currentTimeMillis() - _scaredFishes.get(f) < 15000) {
                                        continue; 
                                    }
                                    boolean lured = false;
                                    for (FishingSession other : _sessions.values()) {
                                        if (other.visualFish == f) { lured = true; break; }
                                    }
                                    if (!lured) validFishes.add(f);
                                }
                            }
                            if (!validFishes.isEmpty()) {
                                chosenFish = validFishes.get(UtilMath.random.nextInt(validFishes.size()));
                            }
                        }
                        
                        if (chosenFish == null) {
                            double angle = UtilMath.random.nextDouble() * 2 * Math.PI;
                            double dx = Math.cos(angle) * 4;
                            double dz = Math.sin(angle) * 4;
                            org.bukkit.Location startLoc = hookLoc.clone().add(dx, -0.5, dz);
                            org.bukkit.entity.EntityType type = UtilMath.random.nextBoolean() ? org.bukkit.entity.EntityType.COD : org.bukkit.entity.EntityType.SALMON;
                            chosenFish = hookLoc.getWorld().spawnEntity(startLoc, type);
                            chosenFish.setPersistent(false);
                            chosenFish.setInvulnerable(true);
                            if (chosenFish instanceof org.bukkit.entity.LivingEntity) ((org.bukkit.entity.LivingEntity) chosenFish).setRemoveWhenFarAway(false);
                            if (session.activePond != null) session.activePond.ambientFishes.add(chosenFish);
                        }
                        session.visualFish = chosenFish;
                    }
                    
                    if (session.visualFish != null && session.visualFish.isValid()) {
                        org.bukkit.util.Vector fishPos = session.visualFish.getLocation().toVector();
                        org.bukkit.util.Vector hookPos = hookLoc.clone().subtract(0, 0.3, 0).toVector();
                        org.bukkit.util.Vector direction = hookPos.clone().subtract(fishPos);
                        
                        if (direction.lengthSquared() > 0.15) { 
                            org.bukkit.util.Vector swimDir = direction.clone().normalize();
                            session.visualFish.setVelocity(swimDir.clone().multiply(0.05)); 
                            org.bukkit.Location currentLoc = session.visualFish.getLocation();
                            currentLoc.setDirection(swimDir);
                            session.visualFish.setRotation(currentLoc.getYaw(), currentLoc.getPitch());
                        } else {
                            org.bukkit.inventory.ItemStack offHand = player.getInventory().getItemInOffHand();
                            boolean hasOffhandBait = offHand != null && offHand.getType() == Material.BREAD && offHand.getItemMeta() != null && (offHand.getItemMeta().getDisplayName().contains("พรีเมียม") || offHand.getItemMeta().getDisplayName().contains("Premium"));
                            
                            boolean hasBait = hasBaitGadget || hasOffhandBait;
                            double biteChance = hasBait ? 1.0 : 0.5;

                            if (UtilMath.random.nextDouble() < biteChance) {
                                if (hasBait) {
                                    if (hasBaitGadget && bg != null) {
                                        gm.getInventoryManager().addItemToInventory(player, com.houzicore.shared.core.gadget.types.GadgetType.Bait.name(), bg.GetName(), -1);
                                        player.playSound(player.getLocation(), Sound.ENTITY_GENERIC_EAT, 0.5f, 1f);
                                        // Update Gadget UI
                                        gm.redisplayActiveItem(player);
                                    } else if (hasOffhandBait) {
                                        offHand.setAmount(offHand.getAmount() - 1);
                                        player.getInventory().setItemInOffHand(offHand.getAmount() > 0 ? offHand : null);
                                        player.playSound(player.getLocation(), Sound.ENTITY_GENERIC_EAT, 0.5f, 1f);
                                    }
                                }
                                session.phase = "BITE";
                                session.biteTimeMs = System.currentTimeMillis();
                                player.playSound(session.hook.getLocation(), Sound.ENTITY_FISHING_BOBBER_SPLASH, 1f, 1f);
                                session.visualFish.addPassenger(session.hook);
                                session.bobbingDown = false;
                                session.bobbingTicksLeft = 60 + UtilMath.random.nextInt(40);
                                player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BELL, 1f, 1f);
                                
                                boolean isThai = com.houzicore.shared.core.lang.LangManager.get().isThai(player);
                                UtilTextBottom.display(ActionBarChannel.GAME_EVENT, isThai ? "§a§lฮุบแล้ว! §fดึงเบ็ดเลย!" : "§a§lBITED! §fReel it in!", player);
                            } else {
                                org.bukkit.util.Vector ignoreDir = session.visualFish.getLocation().getDirection().multiply(0.2);
                                session.visualFish.setVelocity(ignoreDir);
                                session.visualFish = null;
                                session.fishSpawned = false;
                                session.customWaitTimer = 60 + UtilMath.random.nextInt(80);
                                boolean isThai = com.houzicore.shared.core.lang.LangManager.get().isThai(player);
                                UtilTextBottom.display(ActionBarChannel.GAME_EVENT, isThai ? "§eปลาว่ายมาดูแล้วก็เมิน... รอตัวต่อไปนะ" : "§eThe fish ignored it... wait for another one", player);
                            }
                        }
                    }
                }

                if (session.lastCastLoc != null) {
                    if (UtilMath.random.nextInt(10) == 0) {
                        UtilParticle.PlayParticle(ParticleType.SPLASH, session.lastCastLoc.clone().add(0, 0.1, 0), 0.5f, 0f, 0.5f, 0.05f, 3, ViewDist.SHORT, UtilServer.getPlayers());
                    }
                }
                if (session.combo > 0) {
                    FishingPond pond = getNearestPond(player.getLocation(), 60 * 60);
                    if (pond == null) {
                        cancelSession(player.getUniqueId(), player, "คุณเดินออกนอกเขตบ่อปลา Combo หายไปแล้ว!");
                    }
                }
            }
        }
    }

    @EventHandler
    public void ecosystemTick(UpdateEvent event) {
        if (event.getType() != UpdateType.TICK) return; // fastest
        if (_ponds.isEmpty() || _inZone.isEmpty()) return;
        
        long now = System.currentTimeMillis();
        for (UUID uuid : _inZone) {
            Player player = org.bukkit.Bukkit.getPlayer(uuid);
            if (player == null || !player.isOnline()) continue;
            
            if ((player.isSprinting() || player.getVelocity().lengthSquared() > 0.08) && player.getLocation().getBlock().getType() == Material.WATER) {
                FishingPond nearest = getNearestPond(player.getLocation(), 15 * 15);
                if (nearest != null) {
                    for (org.bukkit.entity.Entity fish : nearest.ambientFishes) {
                        if (fish != null && fish.isValid() && fish.getLocation().distanceSquared(player.getLocation()) < 49) {
                            if (!_scaredFishes.containsKey(fish) || now - _scaredFishes.get(fish) > 15000) {
                                _scaredFishes.put(fish, now);
                                org.bukkit.util.Vector away = fish.getLocation().toVector().subtract(player.getLocation().toVector()).normalize().multiply(0.5).setY(0.1);
                                fish.setVelocity(away);
                                fish.setRotation(fish.getLocation().setDirection(away).getYaw(), 0);
                            }
                        }
                    }
                }
            }
        }
    }

    // ─── Zone-Entry Auto-Detection ───
    @EventHandler
    public void zoneEntryCheck(UpdateEvent event) {
        if (event.getType() != UpdateType.FAST) return;
        if (_ponds.isEmpty()) return;

        for (Player player : UtilServer.getPlayers()) {
            UUID uuid = player.getUniqueId();
            boolean wasInZone = _inZone.contains(uuid);
            FishingPond nearest = getNearestPond(player.getLocation(), wasInZone ? ZONE_EXIT_RADIUS_SQ : ZONE_ENTRY_RADIUS_SQ);
            boolean nowInZone = nearest != null;

            if (nowInZone && !wasInZone) {
                if (_hub.isAdminBuilder(player)) continue;
                com.houzicore.lobby.hub.bootstrap.LobbyTransitionCoordinator tc = _hub.getTransitionCoordinator();
                if (tc.isInFishing(player)) {
                    _inZone.add(uuid);
                    continue;
                }
                if (tc.isInAnyLobbyActivity(player)) continue;

                if (!tc.enterFishing(player)) continue;
                _inZone.add(uuid);
                setFishingScoreboard(player, true);

                boolean isThai = com.houzicore.shared.core.lang.LangManager.get().isThai(player);
                com.houzicore.shared.common.util.UtilTextMiddle.display(
                    C.cAqua + "🎣",
                    isThai ? C.cGray + "เข้าสู่โซนตกปลา" : C.cGray + "Fishing Zone",
                    10, 40, 20, player);
                UtilTextBottom.display(ActionBarChannel.TOOL_HINT,
                    isThai ? "§b🎣 เหวี่ยงเบ็ดได้เลยทุกทิศ §7(เดินออกโซนเพื่อออก)" : "§b🎣 Cast freely in the pond area §7(leave the zone to exit)",
                    player);
                UtilPlayer.message(player, F.main("Fishing", isThai ? C.cGray + "เหวี่ยงเบ็ดได้เลย ไม่ต้องยืนในน้ำ" : C.cGray + "You can cast freely here, no need to stand in water"));
                player.playSound(player.getLocation(), Sound.AMBIENT_UNDERWATER_ENTER, 0.6f, 1f);
            } else if (!nowInZone && wasInZone) {
                // Player left fishing zone → restore Lobby items
                _inZone.remove(uuid);
                cancelSession(uuid, player, null);
                setFishingScoreboard(player, false);
                com.houzicore.shared.common.actionbar.ActionBarService.clear(player, ActionBarChannel.TOOL_HINT);

                com.houzicore.lobby.hub.bootstrap.LobbyTransitionCoordinator tc = _hub.getTransitionCoordinator();
                if (tc.isInFishing(player)) {
                    tc.exitFishing(player);
                }
            }
        }
    }

    private void cancelSession(UUID uuid, Player player, String message) {
        FishingSession session = _sessions.get(uuid);
        if (session != null) {
            if (session.visualFish != null) {
                if (session.hook != null && session.visualFish.getPassengers().contains(session.hook)) {
                    session.visualFish.removePassenger(session.hook);
                }
                session.visualFish = null;
            }
            session.phase = "IDLE";
            session.combo = 0;
            session.biteTimeMs = 0;
            session.bobbingTicksLeft = 0;
            session.customWaitTimer = 0;
            session.fishSpawned = false;
        }
        if (message != null && player != null) {
            UtilPlayer.message(player, F.main("Fishing", C.cRed + message));
        }
    }

    private void giveReward(Player player, FishingSession session) {
        FishType caught = session.pendingRollResult;
        CatchGrade grade = session.lastGrade;
        boolean isThai = com.houzicore.shared.core.lang.LangManager.get().isThai(player);
        session.sessionCatches++;
        
        session.recentCatches.addFirst(caught);
        if (session.recentCatches.size() > 3) session.recentCatches.removeLast();
        
        if (caught == FishType.JUNK || grade == CatchGrade.SLOW) {
            session.combo = 0;
        } else if (grade == CatchGrade.PERFECT) {
            session.combo++;
        }
        
        boolean hasKeyCooldown = !Recharge.Instance.use(player, "Fishing.KeyDrop", 30000, false, false);
        if (caught.isSpecial() && hasKeyCooldown) {
            caught = FishType.RARE; // Downgrade if on cooldown
        }
        
        int finalEssence = (grade == CatchGrade.PERFECT) ? (caught.essence * 2) : caught.essence;
        
        String title = caught.color + "✦ " + caught.getName(isThai) + "!";
        String sub = C.cGray + "Essence: " + C.cGreen + finalEssence;
        
        if (caught == FishType.JUNK) {
            player.playSound(player.getLocation(), Sound.BLOCK_MUD_BREAK, 1f, 0.5f);
            player.sendTitle(isThai ? "§8[ขยะ]" : "§8[Trash]", isThai ? "§8ได้ของไร้ค่า... Combo หาย" : "§8Trash item... Combo broken", 5, 20, 10);
            UtilTextBottom.display(ActionBarChannel.GAME_EVENT, isThai ? "§8§l🗑 ได้ขยะ... §c(Combo ถูกรีเซ็ต)" : "§8§l🗑 Got trash... §c(Combo zeroed)", player);
        } else if (caught.isSpecial()) {
            sub = caught.color + "✦ " + (isThai ? "ได้รับ " : "Obtained ") + caught.salesPackage + "!";
            player.sendTitle(title, sub, 10, 50, 20);
            playEpicEffects(player);
            int accountId = _clients.Get(player).getAccountId();
            _donation.PurchaseUnknownSalesPackage(null, player.getName(), accountId, caught.salesPackage, false, 1, false);
        } else {
            if (grade == CatchGrade.PERFECT) player.sendTitle("§e✨ PERFECT ✨", title, 5, 20, 10);
            else player.sendTitle("", title, 5, 20, 10);
            playNormalEffects(player, caught);
        }
        
        if (finalEssence > 0) {
            _donation.RewardEssenceLater("Fishing." + caught.name(), player, finalEssence);
        }

        _stats.incrementStat(player, "Fishing.Catches", 1);
        if (caught.ordinal() >= FishType.EPIC.ordinal()) {
            _stats.incrementStat(player, "Fishing.Jackpots", 1);
        }

        UtilPlayer.message(player, F.main("Fishing", C.cGray + (isThai ? "ได้ " : "Caught ") + caught.color + caught.getName(isThai) + C.cGray + "! " + (finalEssence>0 ? "+"+C.cGreen+finalEssence+" Essence" : "")));
    }

    @EventHandler
    public void onPluginDisable(org.bukkit.event.server.PluginDisableEvent event) {
        if (event.getPlugin().equals(getPlugin())) {
            for (FishingPond pond : _ponds) {
                for (org.bukkit.entity.Entity e : pond.ambientFishes) {
                    if (e != null && e.isValid()) {
                        e.remove();
                    }
                }
            }
        }
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        UUID uuid = event.getPlayer().getUniqueId();
        _sessions.remove(uuid);
        _inZone.remove(uuid);
        setFishingScoreboard(event.getPlayer(), false);
    }

    private void playNormalEffects(Player player, FishType fish) {
        Location loc = player.getLocation();
        player.playSound(loc, Sound.ENTITY_FISHING_BOBBER_SPLASH, 1f, 1f);
        player.playSound(loc, Sound.BLOCK_NOTE_BLOCK_PLING, 0.8f, fish.ordinal() >= FishType.RARE.ordinal() ? 1.8f : 1.2f);
        UtilParticle.PlayParticle(ParticleType.SPLASH, loc, 0.5f, 0.5f, 0.5f, 0.1f, 15, ViewDist.NORMAL, UtilServer.getPlayers());
    }

    private void playEpicEffects(Player player) {
        Location loc = player.getLocation();
        player.playSound(loc, Sound.ENTITY_FISHING_BOBBER_SPLASH, 1f, 0.8f);
        player.playSound(loc, Sound.UI_TOAST_CHALLENGE_COMPLETE, 1f, 1f);
        player.playSound(loc, Sound.ENTITY_FIREWORK_ROCKET_LAUNCH, 1f, 1.2f);
        UtilParticle.PlayParticle(ParticleType.FIREWORKS_SPARK, loc.add(0, 1, 0), 0.5f, 1.0f, 0.5f, 0.2f, 60, ViewDist.LONG, UtilServer.getPlayers());
    }

    private static class FishingPond {
        public final Location center;
        public final java.util.Set<org.bukkit.block.Block> waterBlocks = new java.util.HashSet<>();
        public final java.util.List<org.bukkit.entity.Entity> ambientFishes = new java.util.ArrayList<>();
        private int minX, maxX, minY, maxY, minZ, maxZ;

        public FishingPond(Location center) {
            this.center = center;
            minX = maxX = center.getBlockX();
            minY = maxY = center.getBlockY();
            minZ = maxZ = center.getBlockZ();
            
            java.util.Queue<org.bukkit.block.Block> queue = new java.util.LinkedList<>();
            org.bukkit.block.Block startBlock = center.getBlock();
            if (startBlock.getType() != Material.WATER) {
                for (int y = 0; y > -5; y--) {
                    if (startBlock.getRelative(0, y, 0).getType() == Material.WATER) {
                        startBlock = startBlock.getRelative(0, y, 0);
                        break;
                    }
                }
            }
            if (startBlock.getType() == Material.WATER) {
                queue.add(startBlock);
                waterBlocks.add(startBlock);
            }
            
            int maxBlocks = 8000;
            org.bukkit.block.BlockFace[] faces = {
                org.bukkit.block.BlockFace.NORTH, org.bukkit.block.BlockFace.SOUTH, org.bukkit.block.BlockFace.EAST, org.bukkit.block.BlockFace.WEST,
                org.bukkit.block.BlockFace.UP, org.bukkit.block.BlockFace.DOWN
            };
            
            while (!queue.isEmpty() && waterBlocks.size() < maxBlocks) {
                org.bukkit.block.Block b = queue.poll();
                
                if (b.getX() < minX) minX = b.getX();
                if (b.getX() > maxX) maxX = b.getX();
                if (b.getY() < minY) minY = b.getY();
                if (b.getY() > maxY) maxY = b.getY();
                if (b.getZ() < minZ) minZ = b.getZ();
                if (b.getZ() > maxZ) maxZ = b.getZ();

                for (org.bukkit.block.BlockFace face : faces) {
                    org.bukkit.block.Block adj = b.getRelative(face);
                    if (adj.getType() == Material.WATER && !waterBlocks.contains(adj)) {
                        waterBlocks.add(adj);
                        queue.add(adj);
                    }
                }
            }
            
            int fishCount = Math.min(15, Math.max(1, waterBlocks.size() / 10));
            if (!waterBlocks.isEmpty()) {
                org.bukkit.block.Block[] blocksArray = waterBlocks.toArray(new org.bukkit.block.Block[0]);
                for (int i=0; i<fishCount; i++) {
                    org.bukkit.block.Block spawnBlock = blocksArray[UtilMath.random.nextInt(blocksArray.length)];
                    Location spawnLoc = spawnBlock.getLocation().add(0.5, 0.5, 0.5);
                    org.bukkit.entity.EntityType type = UtilMath.random.nextBoolean() ? org.bukkit.entity.EntityType.COD : org.bukkit.entity.EntityType.SALMON;
                    org.bukkit.entity.Entity fish = center.getWorld().spawnEntity(spawnLoc, type);
                    fish.setPersistent(false);
                    fish.setInvulnerable(true);
                    if (fish instanceof org.bukkit.entity.LivingEntity) {
                        ((org.bukkit.entity.LivingEntity) fish).setRemoveWhenFarAway(false);
                    }
                    ambientFishes.add(fish);
                }
            }
        }

        public boolean isNear(Location loc, double maxDistSq) {
            double pad = Math.sqrt(maxDistSq);
            if (loc.getX() < minX - pad || loc.getX() > maxX + pad) return false;
            if (loc.getY() < minY - pad || loc.getY() > maxY + pad) return false;
            if (loc.getZ() < minZ - pad || loc.getZ() > maxZ + pad) return false;
            
            for (org.bukkit.block.Block b : waterBlocks) {
                if (b.getLocation().add(0.5, 0.5, 0.5).distanceSquared(loc) <= maxDistSq) {
                    return true;
                }
            }
            return center.distanceSquared(loc) <= maxDistSq;
        }
    }

    private static class FishingSession {
        public String phase = "IDLE";
        public int combo = 0;
        public int sessionCatches = 0;
        public boolean isFrenzy = false;
        public FishType pendingRollResult = null;
        public FishType.CatchGrade lastGrade = null;
        public long rollStartTime = 0;
        public long biteTimeMs = 0;
        public Location lastCastLoc = null;
        public long lastCastTime = 0;
        public int suspectScore = 0;
        public FishingPond activePond = null;
        public long lastPlaytimeTick = System.currentTimeMillis();
        public java.util.LinkedList<FishType> recentCatches = new java.util.LinkedList<>();
        
        public org.bukkit.entity.FishHook hook = null;
        public org.bukkit.entity.Entity visualFish = null;
        public boolean fishSpawned = false;
        public boolean bobbingDown = false;
        public int bobbingTicksLeft = 0;
        public int customWaitTimer = 0;
    }
}
