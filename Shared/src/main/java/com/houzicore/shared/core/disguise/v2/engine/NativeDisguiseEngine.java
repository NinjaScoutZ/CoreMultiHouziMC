/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.github.retrooper.packetevents.PacketEvents
 *  com.github.retrooper.packetevents.protocol.player.GameMode
 *  com.github.retrooper.packetevents.protocol.player.UserProfile
 *  com.github.retrooper.packetevents.util.Vector3d
 *  com.github.retrooper.packetevents.wrapper.PacketWrapper
 *  com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerDestroyEntities
 *  com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerPlayerInfoUpdate
 *  com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerPlayerInfoUpdate$Action
 *  com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerPlayerInfoUpdate$PlayerInfo
 *  com.houzicore.shared.common.actionbar.ActionBarChannel
 *  com.houzicore.shared.common.actionbar.ActionBarService
 *  com.houzicore.shared.core.disguise.v2.engine.NativeDisguiseData
 *  com.houzicore.shared.core.disguise.v2.engine.NativeDisguiseType
 *  net.kyori.adventure.text.Component
 *  net.kyori.adventure.text.TextComponent
 *  net.kyori.adventure.text.format.NamedTextColor
 *  net.kyori.adventure.text.format.TextColor
 *  net.kyori.adventure.text.format.TextDecoration
 *  org.bukkit.Bukkit
 *  org.bukkit.Location
 *  org.bukkit.Material
 *  org.bukkit.Particle
 *  org.bukkit.Sound
 *  org.bukkit.attribute.Attribute
 *  org.bukkit.attribute.AttributeInstance
 *  org.bukkit.block.Block
 *  org.bukkit.block.BlockFace
 *  org.bukkit.entity.BlockDisplay
 *  org.bukkit.entity.Display$Billboard
 *  org.bukkit.entity.Entity
 *  org.bukkit.entity.EntityType
 *  org.bukkit.entity.Interaction
 *  org.bukkit.entity.ItemDisplay
 *  org.bukkit.entity.ItemDisplay$ItemDisplayTransform
 *  org.bukkit.entity.LivingEntity
 *  org.bukkit.entity.Player
 *  org.bukkit.event.EventHandler
 *  org.bukkit.event.EventPriority
 *  org.bukkit.event.Listener
 *  org.bukkit.event.block.Action
 *  org.bukkit.event.entity.CreatureSpawnEvent
 *  org.bukkit.event.player.PlayerChangedWorldEvent
 *  org.bukkit.event.player.PlayerInteractEvent
 *  org.bukkit.event.player.PlayerJoinEvent
 *  org.bukkit.event.player.PlayerQuitEvent
 *  org.bukkit.inventory.ItemStack
 *  org.bukkit.inventory.meta.ItemMeta
 *  org.bukkit.inventory.meta.components.CustomModelDataComponent
 *  org.bukkit.plugin.Plugin
 *  org.bukkit.plugin.java.JavaPlugin
 *  org.bukkit.potion.PotionEffect
 *  org.bukkit.potion.PotionEffectType
 *  org.bukkit.scheduler.BukkitTask
 *  org.bukkit.scoreboard.Objective
 *  org.bukkit.scoreboard.Scoreboard
 *  org.bukkit.scoreboard.Team
 *  org.bukkit.scoreboard.Team$Option
 *  org.bukkit.scoreboard.Team$OptionStatus
 *  org.bukkit.util.Transformation
 *  org.bukkit.util.Vector
 *  org.joml.Quaternionf
 *  org.joml.Vector3f
 */
package com.houzicore.shared.core.disguise.v2.engine;

import com.github.retrooper.packetevents.PacketEvents;
import com.github.retrooper.packetevents.protocol.player.GameMode;
import com.github.retrooper.packetevents.protocol.player.UserProfile;
import com.github.retrooper.packetevents.util.Vector3d;
import com.github.retrooper.packetevents.wrapper.PacketWrapper;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerDestroyEntities;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerPlayerInfoUpdate;
import com.houzicore.shared.common.actionbar.ActionBarChannel;
import com.houzicore.shared.common.actionbar.ActionBarService;
import com.houzicore.shared.core.disguise.v2.engine.NativeDisguiseData;
import com.houzicore.shared.core.disguise.v2.engine.NativeDisguiseType;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Logger;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.BlockDisplay;
import org.bukkit.entity.Display;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Interaction;
import org.bukkit.entity.ItemDisplay;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.components.CustomModelDataComponent;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;
import org.bukkit.util.Transformation;
import org.bukkit.util.Vector;
import org.joml.Quaternionf;
import org.joml.Vector3f;

public class NativeDisguiseEngine
implements Listener {
    public static final ThreadLocal<Boolean> SPAWNING_SELF_VIEW = ThreadLocal.withInitial(() -> false);
    private final JavaPlugin plugin;
    private final Logger log;
    private final Map<UUID, NativeDisguiseData> activeDisguises = new ConcurrentHashMap<UUID, NativeDisguiseData>();
    private final Map<Integer, NativeDisguiseData> fakeEntityIndex = new ConcurrentHashMap<Integer, NativeDisguiseData>();
    private final Map<Integer, NativeDisguiseData> realEntityIndex = new ConcurrentHashMap<Integer, NativeDisguiseData>();
    private final AtomicInteger nextFakeId = new AtomicInteger(500000);
    private BukkitTask tickTask;
    private int tickCounter = 0;
    private static final int SOLID_VISUAL_REFRESH_INTERVAL_TICKS = 10;
    private static final String DPOC_ID_OBJECTIVE = "dpoc.id";
    private static final String DPOC_TEMP_OBJECTIVE = "dpoc.temp";
    private SelfViewMode selfViewMode = SelfViewMode.FOLLOWER;

    public NativeDisguiseEngine(JavaPlugin plugin) {
        this.plugin = plugin;
        this.log = plugin.getLogger();
        this.tickTask = Bukkit.getScheduler().runTaskTimer((Plugin)plugin, this::tick, 1L, 1L);
    }

    public NativeDisguiseData getDisguise(Player player) {
        return this.activeDisguises.get(player.getUniqueId());
    }

    public boolean isSolidified(Player player) {
        NativeDisguiseData data = this.getDisguise(player);
        return data != null && data.isSolidified();
    }

    public Block getSolidBlock(Player player) {
        NativeDisguiseData data = this.getDisguise(player);
        if (data != null && data.isSolidified() && data.getSolidLocation() != null) {
            return data.getSolidLocation().getBlock();
        }
        return null;
    }

    public void disguiseAsBlock(Player player, Material material) {
        if (this.activeDisguises.containsKey(player.getUniqueId())) {
            this.undisguise(player);
        }
        int fakeId = this.nextFakeId.getAndIncrement();
        int selfViewId = this.nextFakeId.getAndIncrement();
        NativeDisguiseData data = new NativeDisguiseData(player.getUniqueId(), player.getEntityId(), fakeId, selfViewId, NativeDisguiseType.BLOCK);
        data.setBlockMaterial(material);
        data.setLastLocation(player.getLocation().clone());
        this.activeDisguises.put(player.getUniqueId(), data);
        this.realEntityIndex.put(player.getEntityId(), data);
        this.spawnServerSideVisuals(player, data, player.getLocation());
        this.hideRealPlayerFromViewers(player);
        player.addPotionEffect(new PotionEffect(PotionEffectType.INVISIBILITY, Integer.MAX_VALUE, 0, false, false, false));
        player.setCollidable(false);
        this.log.info("[Disguise] " + player.getName() + " -> " + material.name() + " (block ID: " + data.getDisplayEntityId() + ")");
    }

    public void replaceDisguise(Player player, Material newMaterial) {
        NativeDisguiseData oldData = this.activeDisguises.get(player.getUniqueId());
        if (oldData != null) {
            oldData.setActive(false);
            this.unregisterVisualEntityIds(oldData);
            if (oldData.isSolidified()) {
                this.clearSolidBlockVisuals(oldData);
            }
            this.removeServerSideVisuals(oldData);
            if (oldData.getType() == NativeDisguiseType.CREATURE) {
                this.removeCreatureSelfView(oldData);
            } else if (oldData.getDisplayEntityId() > 0) {
                WrapperPlayServerDestroyEntities destroy = new WrapperPlayServerDestroyEntities(oldData.getDisplayEntityId());
                PacketEvents.getAPI().getPlayerManager().sendPacket((Object)player, (PacketWrapper)destroy);
            }
            this.activeDisguises.remove(player.getUniqueId());
            this.realEntityIndex.remove(player.getEntityId());
        }
        this.disguiseAsBlock(player, newMaterial);
    }

    public void disguiseAsMob(Player player, EntityType entityType) {
        if (this.activeDisguises.containsKey(player.getUniqueId())) {
            this.undisguise(player);
        }
        int selfViewId = this.nextFakeId.getAndIncrement();
        NativeDisguiseData data = new NativeDisguiseData(player.getUniqueId(), player.getEntityId(), -1, selfViewId, NativeDisguiseType.CREATURE);
        data.setCreatureType(entityType);
        this.activeDisguises.put(player.getUniqueId(), data);
        this.realEntityIndex.put(player.getEntityId(), data);
        player.addPotionEffect(new PotionEffect(PotionEffectType.INVISIBILITY, Integer.MAX_VALUE, 0, false, false, false));
        player.setCollidable(false);
        this.applyMobAttributes(player, entityType, data);
        player.getWorld().spawnParticle(Particle.POOF, player.getLocation().add(0.0, 1.0, 0.0), 30, 0.5, 0.5, 0.5, 0.1);
        player.playSound(player.getLocation(), Sound.ENTITY_ILLUSIONER_MIRROR_MOVE, 1.0f, 1.0f);
        this.applyCreatureSelfView(player, data);
        //this.log.info("[Disguise-Debug] Starting hide/show cycle for " + player.getName() + " (" + entityType.name() + ") to " + (Bukkit.getOnlinePlayers().size() - 1) + " viewers");
        for (Player viewer : Bukkit.getOnlinePlayers()) {
            if (viewer.equals((Object)player)) continue;
            viewer.hidePlayer((Plugin)this.plugin, player);
            viewer.showPlayer((Plugin)this.plugin, player);
        }
        //this.log.info("[Disguise-Debug] Hide/show cycle complete for " + player.getName());
        this.forceRelistInTablist(player);
        //this.log.info("[Disguise-PacketID] " + player.getName() + " -> " + entityType.name());

        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            if (!player.isOnline() || !isDisguised(player)) return;
            for (Player viewer : Bukkit.getOnlinePlayers()) {
                if (!viewer.equals(player)) {
                    viewer.hidePlayer(plugin, player);
                    viewer.showPlayer(plugin, player);
                }
            }
            forceRelistInTablist(player);
        }, 10L);
    }

    public void replaceDisguise(Player player, EntityType newEntityType) {
        NativeDisguiseData oldData = this.activeDisguises.get(player.getUniqueId());
        if (oldData != null) {
            oldData.setActive(false);
            this.unregisterVisualEntityIds(oldData);
            if (oldData.isSolidified()) {
                this.clearSolidBlockVisuals(oldData);
            }
            this.removeServerSideVisuals(oldData);
            if (oldData.getType() == NativeDisguiseType.CREATURE) {
                this.removeCreatureSelfView(oldData);
            } else if (oldData.getDisplayEntityId() > 0) {
                WrapperPlayServerDestroyEntities destroy = new WrapperPlayServerDestroyEntities(oldData.getDisplayEntityId());
                PacketEvents.getAPI().getPlayerManager().sendPacket((Object)player, (PacketWrapper)destroy);
            }
            this.activeDisguises.remove(player.getUniqueId());
            this.realEntityIndex.remove(player.getEntityId());
        }
        this.disguiseAsMob(player, newEntityType);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private void spawnCreatureSelfView(Player player, NativeDisguiseData data) {
        Entity spawned;
        Location loc = this.toCreatureSelfViewLocation(player, data);
        if (data.getCreatureType() == null || !data.getCreatureType().isSpawnable()) {
            return;
        }
        SPAWNING_SELF_VIEW.set(true);
        try {
            spawned = player.getWorld().spawnEntity(loc, data.getCreatureType());
        }
        finally {
            SPAWNING_SELF_VIEW.set(false);
        }
        if (!(spawned instanceof LivingEntity)) {
            spawned.remove();
            return;
        }
        LivingEntity living = (LivingEntity)spawned;
        living.setPersistent(false);
        living.setRemoveWhenFarAway(false);
        living.setInvulnerable(true);
        living.setSilent(true);
        living.setGravity(false);
        living.setAI(false);
        living.setCanPickupItems(false);
        living.setCollidable(false);
        living.setVisibleByDefault(false);
        living.setCustomNameVisible(false);
        living.addScoreboardTag("disguise-poc");
        living.addScoreboardTag("disguise-poc-self-view");
        living.addScoreboardTag("owner-" + String.valueOf(player.getUniqueId()));
        Scoreboard board = Bukkit.getScoreboardManager().getMainScoreboard();
        Team team = board.getTeam("dpoc_nocollide");
        if (team == null) {
            team = board.registerNewTeam("dpoc_nocollide");
            team.setOption(Team.Option.COLLISION_RULE, Team.OptionStatus.NEVER);
        }
        team.addEntry(living.getUniqueId().toString());
        data.setRealSelfViewEntity((Entity)living);
        data.setSelfViewLastLoc(loc.clone());
        this.refreshCreatureSelfViewVisibility(player, data);
    }

    private void applyCreatureSelfView(Player player, NativeDisguiseData data) {
        this.removeCreatureSelfView(data);
        if (this.selfViewMode == SelfViewMode.REAL) {
            this.spawnRealCreatureSelfView(player, data);
        } else if (this.selfViewMode == SelfViewMode.FOLLOWER) {
            this.spawnCreatureSelfView(player, data);
        } else if (this.selfViewMode == SelfViewMode.DISPLAY) {
            this.spawnDisplayCreatureSelfView(player, data);
        }
    }

    private void spawnDisplayCreatureSelfView(Player player, NativeDisguiseData data) {
        if (data.getCreatureType() == null) {
            return;
        }
        Location loc = this.toDisplayCreatureSelfViewLocation(player, data);
        ItemDisplay display = (ItemDisplay)player.getWorld().spawn(loc, ItemDisplay.class, entity -> {
            entity.setPersistent(false);
            entity.setInvulnerable(true);
            entity.setGravity(false);
            entity.setSilent(true);
            entity.setVisibleByDefault(false);
            entity.setCustomNameVisible(false);
            entity.addScoreboardTag("disguise-poc");
            entity.addScoreboardTag("disguise-poc-self-view-display");
            entity.addScoreboardTag("owner-" + String.valueOf(player.getUniqueId()));
            entity.setItemStack(this.createDisplayModelItem(data.getCreatureType()));
            entity.setItemDisplayTransform(ItemDisplay.ItemDisplayTransform.FIXED);
            entity.setBillboard(Display.Billboard.FIXED);
            entity.setTeleportDuration(0);
            entity.setInterpolationDelay(0);
            entity.setInterpolationDuration(0);
            double scale = this.getMobVisualScale(data.getCreatureType());
            entity.setTransformation(new Transformation(new Vector3f(0.0f, 0.0f, 0.0f), new Quaternionf(), new Vector3f((float)scale, (float)scale, (float)scale), new Quaternionf()));
        });
        data.setRealSelfViewEntity((Entity)display);
        data.setSelfViewLastLoc(loc.clone());
        this.refreshCreatureSelfViewVisibility(player, data);
        this.syncDisplayCreatureSelfView(player, data);
    }

    private void syncDisplayCreatureSelfView(Player player, NativeDisguiseData data) {
        ItemDisplay display;
        Entity entity = data.getRealSelfViewEntity();
        if (!(entity instanceof ItemDisplay) || (display = (ItemDisplay)entity).isDead()) {
            if (this.selfViewMode == SelfViewMode.DISPLAY && data.isActive()) {
                this.spawnDisplayCreatureSelfView(player, data);
            }
            return;
        }
        if (!display.getWorld().equals((Object)player.getWorld())) {
            display.remove();
            data.setRealSelfViewEntity(null);
            this.spawnDisplayCreatureSelfView(player, data);
            return;
        }
        Location loc = this.toDisplayCreatureSelfViewLocation(player, data);
        display.setTeleportDuration(0);
        display.setInterpolationDelay(0);
        display.setInterpolationDuration(0);
        display.teleport(loc);
        data.setSelfViewLastLoc(loc.clone());
        this.refreshCreatureSelfViewVisibility(player, data);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private void spawnRealCreatureSelfView(Player player, NativeDisguiseData data) {
        Entity spawned;
        if (data.getCreatureType() == null || !data.getCreatureType().isSpawnable()) {
            this.log.warning("[Disguise] Cannot spawn real self-view for " + String.valueOf(data.getCreatureType()));
            return;
        }
        SPAWNING_SELF_VIEW.set(true);
        try {
            spawned = player.getWorld().spawnEntity(this.toRealSelfViewLocation(player, data), data.getCreatureType());
        }
        finally {
            SPAWNING_SELF_VIEW.set(false);
        }
        if (!(spawned instanceof LivingEntity)) {
            spawned.remove();
            this.log.warning("[Disguise] Self-view entity is not a living entity.");
            return;
        }
        LivingEntity living = (LivingEntity)spawned;
        living.setPersistent(false);
        living.setRemoveWhenFarAway(false);
        living.setInvulnerable(true);
        living.setSilent(true);
        living.setGravity(false);
        living.setAI(false);
        living.setCanPickupItems(false);
        living.setCollidable(false);
        living.setVisibleByDefault(false);
        living.setCustomNameVisible(false);
        living.addScoreboardTag("disguise-poc");
        living.addScoreboardTag("disguise-poc-self-view");
        living.addScoreboardTag("dpoc.self_view");
        living.addScoreboardTag("owner-" + String.valueOf(player.getUniqueId()));
        if (this.isTallCreature(data.getCreatureType())) {
            living.addScoreboardTag("dpoc.tall");
        }
        data.setRealSelfViewEntity((Entity)living);
        data.setSelfViewLastLoc(player.getLocation().clone());
        this.setupDatapackSync(player, data, (Entity)living);
        this.refreshCreatureSelfViewVisibility(player, data);
    }

    @EventHandler(priority=EventPriority.HIGHEST)
    public void onSelfViewSpawn(CreatureSpawnEvent event) {
        if (SPAWNING_SELF_VIEW.get().booleanValue()) {
            event.setCancelled(false);
        }
    }

    private void syncRealCreatureSelfView(Player player, NativeDisguiseData data) {
        LivingEntity living;
        Entity entity = data.getRealSelfViewEntity();
        if (!(entity instanceof LivingEntity) || (living = (LivingEntity)entity).isDead()) {
            if (this.selfViewMode == SelfViewMode.REAL && data.isActive()) {
                this.spawnRealCreatureSelfView(player, data);
            }
            return;
        }
        if (!living.getWorld().equals((Object)player.getWorld())) {
            living.remove();
            data.setRealSelfViewEntity(null);
            this.spawnRealCreatureSelfView(player, data);
            return;
        }
        this.setupDatapackSync(player, data, (Entity)living);
        data.setSelfViewLastLoc(player.getLocation().clone());
        this.refreshCreatureSelfViewVisibility(player, data);
    }

    private void setupDatapackSync(Player player, NativeDisguiseData data, Entity entity) {
        this.ensureDatapackObjectives();
        Scoreboard scoreboard = Bukkit.getScoreboardManager().getMainScoreboard();
        Objective idObjective = scoreboard.getObjective(DPOC_ID_OBJECTIVE);
        if (idObjective == null) {
            return;
        }
        int ownerId = data.getRealEntityId();
        idObjective.getScore(player.getName()).setScore(ownerId);
        idObjective.getScore(entity.getUniqueId().toString()).setScore(ownerId);
        this.updateDatapackTotalScore();
    }

    private void ensureDatapackObjectives() {
        if (Bukkit.getScoreboardManager() == null) {
            return;
        }
        Scoreboard scoreboard = Bukkit.getScoreboardManager().getMainScoreboard();
        if (scoreboard.getObjective(DPOC_ID_OBJECTIVE) == null) {
            scoreboard.registerNewObjective(DPOC_ID_OBJECTIVE, "dummy");
        }
        if (scoreboard.getObjective(DPOC_TEMP_OBJECTIVE) == null) {
            scoreboard.registerNewObjective(DPOC_TEMP_OBJECTIVE, "dummy");
        }
    }

    private void updateDatapackTotalScore() {
        if (Bukkit.getScoreboardManager() == null) {
            return;
        }
        Objective idObjective = Bukkit.getScoreboardManager().getMainScoreboard().getObjective(DPOC_ID_OBJECTIVE);
        if (idObjective != null) {
            idObjective.getScore("#total").setScore(Math.max(1, Bukkit.getOnlinePlayers().size()));
        }
    }

    private void removeCreatureSelfView(NativeDisguiseData data) {
        Entity realSelfView = data.getRealSelfViewEntity();
        if (realSelfView != null) {
            this.resetDatapackScore(realSelfView.getUniqueId().toString());
            realSelfView.remove();
            data.setRealSelfViewEntity(null);
        }
        data.setSelfViewLastLoc(null);
    }

    private void refreshCreatureSelfViewVisibility(Player owner, NativeDisguiseData data) {
        Entity entity = data.getRealSelfViewEntity();
        if (entity == null) {
            return;
        }
        for (Player viewer : Bukkit.getOnlinePlayers()) {
            if (viewer.equals((Object)owner) && viewer.getWorld().equals((Object)entity.getWorld())) {
                viewer.showEntity((Plugin)this.plugin, entity);
                continue;
            }
            viewer.hideEntity((Plugin)this.plugin, entity);
        }
    }

    private void syncCreatureSelfView(Player player, NativeDisguiseData data) {
        if (!player.isOnline() || data == null || !data.isActive()) {
            return;
        }
        Entity entity = data.getRealSelfViewEntity();
        if (entity == null || entity.isDead()) {
            if (data.isActive()) {
                this.spawnCreatureSelfView(player, data);
            }
            return;
        }
        if (!entity.getWorld().equals((Object)player.getWorld())) {
            entity.remove();
            data.setRealSelfViewEntity(null);
            if (data.isActive()) {
                this.spawnCreatureSelfView(player, data);
            }
            return;
        }
        Location now = this.toCreatureSelfViewLocation(player, data);
        entity.setVelocity(new Vector(0, 0, 0));
        entity.teleport(now);
        data.setSelfViewLastLoc(now.clone());
        this.refreshCreatureSelfViewVisibility(player, data);
    }

    public void undisguise(Player player) {
        NativeDisguiseData data = this.activeDisguises.remove(player.getUniqueId());
        if (data == null) {
            return;
        }
        data.setActive(false);
        this.unregisterVisualEntityIds(data);
        this.realEntityIndex.remove(player.getEntityId());
        if (data.isSolidified()) {
            this.clearSolidBlockVisuals(data);
        }
        this.removeServerSideVisuals(data);
        if (data.getType() == NativeDisguiseType.BLOCK) {
            this.showRealPlayerToViewers(player);
        }
        player.removePotionEffect(PotionEffectType.INVISIBILITY);
        player.setCollidable(true);
        this.restorePlayerAttributes(player, data);
        if (data.getType() == NativeDisguiseType.CREATURE) {
            this.removeCreatureSelfView(data);
        } else if (data.getDisplayEntityId() > 0) {
            WrapperPlayServerDestroyEntities destroy = new WrapperPlayServerDestroyEntities(data.getDisplayEntityId());
            PacketEvents.getAPI().getPlayerManager().sendPacket((Object)player, (PacketWrapper)destroy);
        }
        for (Player viewer : Bukkit.getOnlinePlayers()) {
            if (viewer.equals((Object)player)) continue;
            viewer.hidePlayer((Plugin)this.plugin, player);
            viewer.showPlayer((Plugin)this.plugin, player);
        }
        this.forceRelistInTablist(player);
        player.getWorld().spawnParticle(Particle.POOF, player.getLocation().add(0.0, 1.0, 0.0), 30, 0.5, 0.5, 0.5, 0.1);
        player.playSound(player.getLocation(), Sound.ENTITY_ILLUSIONER_MIRROR_MOVE, 1.0f, 1.0f);
        this.log.info("[Undisguise] " + player.getName() + " returned to normal.");
    }

    private void applyMobAttributes(Player player, EntityType type, NativeDisguiseData data) {
        AttributeInstance scaleAttr = player.getAttribute(Attribute.SCALE);
        AttributeInstance healthAttr = player.getAttribute(Attribute.MAX_HEALTH);
        AttributeInstance stepAttr = player.getAttribute(Attribute.STEP_HEIGHT);
        if (scaleAttr != null) {
            data.setOriginalScale(scaleAttr.getBaseValue());
        }
        if (healthAttr != null) {
            data.setOriginalMaxHealth(healthAttr.getBaseValue());
        }
        if (stepAttr != null) {
            data.setOriginalStepHeight(stepAttr.getBaseValue());
        }
        double targetScale = 1.0;
        double targetHealth = 20.0;
        double targetStep = 0.6;
        switch (type) {
            case PIG: {
                targetScale = 0.75;
                targetHealth = 10.0;
                break;
            }
            case CHICKEN: 
            case PARROT: 
            case BAT: {
                targetScale = 0.65;
                targetHealth = 4.0;
                break;
            }
            case COW: {
                targetScale = 1.15;
                targetHealth = 10.0;
                break;
            }
            case SPIDER: {
                targetScale = 0.85;
                targetHealth = 16.0;
                break;
            }
            case IRON_GOLEM: {
                targetScale = 1.5;
                targetHealth = 100.0;
                targetStep = 1.0;
                break;
            }
            case WOLF: 
            case CAT: {
                targetScale = 0.5;
                targetHealth = 8.0;
                break;
            }
            case HORSE: 
            case DONKEY: 
            case MULE: {
                targetScale = 1.5;
                targetHealth = 26.0;
                targetStep = 1.0;
                break;
            }
            case ENDERMAN: {
                targetScale = 1.7;
                targetHealth = 40.0;
                break;
            }
        }
        if (scaleAttr != null) {
            scaleAttr.setBaseValue(targetScale);
        }
        if (healthAttr != null) {
            healthAttr.setBaseValue(targetHealth);
            if (player.getHealth() > targetHealth) {
                player.setHealth(targetHealth);
            }
        }
        if (stepAttr != null) {
            stepAttr.setBaseValue(targetStep);
        }
    }

    private void restorePlayerAttributes(Player player, NativeDisguiseData data) {
        AttributeInstance scaleAttr = player.getAttribute(Attribute.SCALE);
        AttributeInstance healthAttr = player.getAttribute(Attribute.MAX_HEALTH);
        AttributeInstance stepAttr = player.getAttribute(Attribute.STEP_HEIGHT);
        if (scaleAttr != null) {
            scaleAttr.setBaseValue(data.getOriginalScale());
        }
        if (healthAttr != null) {
            healthAttr.setBaseValue(data.getOriginalMaxHealth());
        }
        if (stepAttr != null) {
            stepAttr.setBaseValue(data.getOriginalStepHeight());
        }
    }

    public boolean solidify(Player player) {
        NativeDisguiseData data = this.activeDisguises.get(player.getUniqueId());
        if (data == null || data.getType() != NativeDisguiseType.BLOCK || data.isSolidified()) {
            return false;
        }
        Location loc = player.getLocation();
        Block block = loc.getBlock();
        if (block.getType().isSolid() || !block.getRelative(BlockFace.DOWN).getType().isSolid()) {
            return false;
        }
        Location solidLoc = block.getLocation().clone();
        data.setSolidified(true, solidLoc);
        Location center = solidLoc.clone().add(0.5, 0.0, 0.5);
        center.setYaw(player.getLocation().getYaw());
        center.setPitch(player.getLocation().getPitch());
        player.teleport(center);
        this.syncServerSideVisuals(player, data, center);
        for (Player viewer : player.getWorld().getPlayers()) {
            this.sendSolidBlockVisual(player, data, viewer);
        }
        this.hideVisualEntitiesDuringSolidify(player, data);
        this.hideRealPlayerFromViewers(player);
        player.getWorld().spawnParticle(Particle.POOF, center, 15, 0.3, 0.3, 0.3, 0.05);
        player.getWorld().playSound(center, Sound.BLOCK_STONE_PLACE, 1.0f, 0.8f);
        this.log.info("[Solidify] " + player.getName() + " solidified at " + this.formatLoc(loc));
        return true;
    }

    public void breakSolidify(Player player, NativeDisguiseData data) {
        if (data == null) {
            data = this.activeDisguises.get(player.getUniqueId());
        }
        if (data == null || !data.isSolidified()) {
            return;
        }
        data.setGraceHit(data.getSolidLocation().clone(), System.currentTimeMillis() + 750L);
        data.setSolidBreakImmunityUntil(System.currentTimeMillis() + 1500L);
        this.clearSolidBlockVisuals(data);
        data.setSolidified(false, null);
        data.resetStillTicks();
        this.showVisualEntitiesToViewers(player, data);
        player.getWorld().spawnParticle(Particle.POOF, player.getLocation().add(0.0, 0.5, 0.0), 10, 0.3, 0.3, 0.3, 0.05);
        player.getWorld().playSound(player.getLocation(), Sound.ENTITY_CHICKEN_EGG, 1.0f, 1.2f);
        this.syncServerSideVisuals(player, data, player.getLocation());
        this.hideRealPlayerFromViewers(player);
        this.log.info("[Solidify] " + player.getName() + " broke solidify");
    }

    public void undisguiseAll() {
        for (UUID uuid : new HashSet<UUID>(this.activeDisguises.keySet())) {
            Player player = Bukkit.getPlayer((UUID)uuid);
            if (player == null || !player.isOnline()) continue;
            this.undisguise(player);
        }
    }

    public NativeDisguiseData getByFakeEntityId(int fakeEntityId) {
        return this.fakeEntityIndex.get(fakeEntityId);
    }

    public Player getRealPlayerByEntityId(int entityId) {
        NativeDisguiseData data = this.realEntityIndex.get(entityId);
        return data != null ? Bukkit.getPlayer((UUID)data.getPlayerUUID()) : null;
    }

    public NativeDisguiseData getByPlayer(Player player) {
        return this.activeDisguises.get(player.getUniqueId());
    }

    public boolean isDisguised(Player player) {
        return this.activeDisguises.containsKey(player.getUniqueId());
    }

    public boolean isSolidBreakImmune(Player player) {
        NativeDisguiseData data = this.activeDisguises.get(player.getUniqueId());
        return data != null && System.currentTimeMillis() < data.getSolidBreakImmunityUntil();
    }

    public void forceRelistInTablist(Player player) {
        Bukkit.getScheduler().runTaskLater((Plugin)this.plugin, () -> {
            if (!player.isOnline()) {
                return;
            }
            try {
                WrapperPlayServerPlayerInfoUpdate.PlayerInfo info = new WrapperPlayServerPlayerInfoUpdate.PlayerInfo(new UserProfile(player.getUniqueId(), player.getName()), true, 0, GameMode.SURVIVAL, null, null);
                WrapperPlayServerPlayerInfoUpdate packet = new WrapperPlayServerPlayerInfoUpdate(EnumSet.of(WrapperPlayServerPlayerInfoUpdate.Action.UPDATE_LISTED), new WrapperPlayServerPlayerInfoUpdate.PlayerInfo[]{info});
                for (Player viewer : Bukkit.getOnlinePlayers()) {
                    PacketEvents.getAPI().getPlayerManager().sendPacket((Object)viewer, (PacketWrapper)packet);
                }
            }
            catch (Exception e) {
                this.log.warning("[Disguise] Failed to force re-list tablist for " + player.getName() + ": " + e.getMessage());
            }
        }, 2L);
    }

    public SelfViewMode getSelfViewMode() {
        return this.selfViewMode;
    }

    public void setSelfViewMode(SelfViewMode selfViewMode) {
        SelfViewMode selfViewMode2 = this.selfViewMode = selfViewMode == null ? SelfViewMode.DISPLAY : selfViewMode;
        if (this.selfViewMode == SelfViewMode.REAL) {
            this.selfViewMode = SelfViewMode.FOLLOWER;
        }
        this.refreshCreatureSelfViews();
    }

    private void resetDatapackScore(String scoreHolder) {
        if (Bukkit.getScoreboardManager() == null || scoreHolder == null) {
            return;
        }
        Bukkit.getScoreboardManager().getMainScoreboard().resetScores(scoreHolder);
    }

    private void refreshCreatureSelfViews() {
        for (NativeDisguiseData data : this.activeDisguises.values()) {
            Player player;
            if (data.getType() != NativeDisguiseType.CREATURE || !data.isActive() || (player = Bukkit.getPlayer((UUID)data.getPlayerUUID())) == null || !player.isOnline()) continue;
            this.applyCreatureSelfView(player, data);
        }
    }

    public NativeDisguiseData getSolidifiedDisguiseAt(int x, int y, int z, String worldName) {
        for (NativeDisguiseData data : this.activeDisguises.values()) {
            Location loc;
            if (!data.isSolidified() || data.getSolidLocation() == null || (loc = data.getSolidLocation()).getBlockX() != x || loc.getBlockY() != y || loc.getBlockZ() != z || !loc.getWorld().getName().equals(worldName)) continue;
            return data;
        }
        return null;
    }

    public NativeDisguiseData getGraceHitDisguiseAt(int x, int y, int z, String worldName) {
        long now = System.currentTimeMillis();
        for (NativeDisguiseData data : this.activeDisguises.values()) {
            Location loc;
            if (data.isSolidified() || data.getGraceHitLocation() == null || data.getGraceHitExpiry() <= now || (loc = data.getGraceHitLocation()).getBlockX() != x || loc.getBlockY() != y || loc.getBlockZ() != z || !loc.getWorld().getName().equals(worldName)) continue;
            return data;
        }
        return null;
    }

    private void tick() {
        ++this.tickCounter;
        if (this.tickCounter % 20 == 0) {
            this.updateDatapackTotalScore();
        }
        for (Map.Entry<UUID, NativeDisguiseData> entry : this.activeDisguises.entrySet()) {
            Player player = Bukkit.getPlayer((UUID)entry.getKey());
            NativeDisguiseData data = entry.getValue();
            if (player == null || !player.isOnline() || !data.isActive()) continue;
            if (data.isSolidified()) {
                Location last = data.getLastLocation();
                Location now = player.getLocation();
                if (last != null && !this.sameBlock(last, now)) {
                    this.breakSolidify(player, data);
                }
                if (data.isSolidified() && this.tickCounter % 10 == 0) {
                    this.refreshSolidBlockVisual(player, data);
                    this.hideRealPlayerFromViewers(player);
                }
                data.setLastLocation(now.clone());
                continue;
            }
            Location now = player.getLocation();
            Location last = data.getLastLocation();
            if (last != null && this.sameBlock(last, now)) {
                data.incrementStillTicks();
            } else {
                data.resetStillTicks();
            }
            if (data.shouldAutoSolidify()) {
                this.solidify(player);
                data.setLastLocation(now.clone());
                continue;
            }
            if (data.getType() == NativeDisguiseType.BLOCK) {
                this.syncServerSideVisuals(player, data, now);
            } else if (data.getType() == NativeDisguiseType.CREATURE && this.selfViewMode != SelfViewMode.OFF) {
                if (this.selfViewMode == SelfViewMode.FOLLOWER) {
                    this.syncCreatureSelfView(player, data);
                } else if (this.selfViewMode == SelfViewMode.DISPLAY) {
                    this.syncDisplayCreatureSelfView(player, data);
                }
            }
            data.setLastLocation(now.clone());
        }
        if (this.tickCounter % 20 == 0) {
            for (Map.Entry<UUID, NativeDisguiseData> entry : this.activeDisguises.entrySet()) {
                Player player;
                NativeDisguiseData data = entry.getValue();
                if (data.getType() != NativeDisguiseType.CREATURE || !data.isActive() || (player = Bukkit.getPlayer((UUID)entry.getKey())) == null || !player.isOnline()) continue;
                String mobName = data.getCreatureType().name().replace("_", " ");
                ActionBarService.display((Player)player, (ActionBarChannel)ActionBarChannel.GAME_STATUS, (Component)((TextComponent)((TextComponent)Component.text((String)"\ud83c\udfad ").color((TextColor)NamedTextColor.GOLD)).append(Component.text((String)"Disguised as ").color((TextColor)NamedTextColor.GRAY))).append(((TextComponent)Component.text((String)mobName).color((TextColor)NamedTextColor.GREEN)).decorate(TextDecoration.BOLD)));
            }
        }
    }

    private void spawnServerSideVisuals(Player player, NativeDisguiseData data, Location loc) {
        this.removeServerSideVisuals(data);
        if (data.getType() == NativeDisguiseType.BLOCK) {
            Location displayLoc = this.toDisplayLocation(loc);
            Location interactionLoc = this.toInteractionLocation(loc);
            BlockDisplay display = (BlockDisplay)loc.getWorld().spawn(displayLoc, BlockDisplay.class, entity -> {
                entity.setPersistent(false);
                entity.setInvulnerable(true);
                entity.setSilent(true);
                entity.setGravity(false);
                entity.setBlock(data.getBlockMaterial().createBlockData());
                entity.setBillboard(Display.Billboard.FIXED);
                entity.setViewRange(64.0f);
                entity.setShadowRadius(0.0f);
                entity.setShadowStrength(0.0f);
                entity.setTeleportDuration(1);
                entity.setInterpolationDuration(1);
                entity.setTransformation(new Transformation(new Vector3f(0.0f, 0.0f, 0.0f), new Quaternionf(), new Vector3f(1.0f, 1.0f, 1.0f), new Quaternionf()));
                entity.setRotation(0.0f, 0.0f);
                entity.addScoreboardTag("disguise-poc");
            });
            final float width = data.getBlockWidth();
            final float height = data.getBlockHeight();
            Interaction interaction = (Interaction)loc.getWorld().spawn(interactionLoc, Interaction.class, entity -> {
                entity.setPersistent(false);
                entity.setInvulnerable(true);
                entity.setSilent(true);
                entity.setGravity(false);
                entity.setInteractionWidth(width);
                entity.setInteractionHeight(height);
                entity.setResponsive(true);
                entity.setRotation(0.0f, 0.0f);
                entity.addScoreboardTag("disguise-poc");
            });
            data.setDisplayEntity(display.getUniqueId(), display.getEntityId());
            data.setInteractionEntity(interaction.getUniqueId(), interaction.getEntityId());
            this.fakeEntityIndex.put(display.getEntityId(), data);
            this.fakeEntityIndex.put(interaction.getEntityId(), data);
            player.hideEntity((Plugin)this.plugin, (Entity)interaction);
        }
        this.syncServerSideVisuals(player, data, loc);
    }

    private void syncServerSideVisuals(Player player, NativeDisguiseData data, Location loc) {
        if (loc == null) {
            return;
        }
        if (data.getType() == NativeDisguiseType.BLOCK) {
            BlockDisplay display = this.getDisplayEntity(data);
            Interaction interaction = this.getInteractionEntity(data);
            if (display == null || display.isDead() || interaction == null || interaction.isDead() || !display.getWorld().equals((Object)loc.getWorld()) || !interaction.getWorld().equals((Object)loc.getWorld())) {
                this.spawnServerSideVisuals(player, data, loc);
                return;
            }
            Location displayLoc = this.toDisplayLocation(loc);
            Location interactionLoc = this.toInteractionLocation(loc);
            display.teleport(displayLoc);
            interaction.teleport(interactionLoc);
            player.hideEntity((Plugin)this.plugin, (Entity)interaction);
        }
    }

    private void removeServerSideVisuals(NativeDisguiseData data) {
        Entity realSelfView;
        Entity interaction;
        this.unregisterVisualEntityIds(data);
        Entity display = this.getEntity(data.getDisplayEntityUUID());
        if (display != null) {
            display.remove();
        }
        if ((interaction = this.getEntity(data.getInteractionEntityUUID())) != null) {
            interaction.remove();
        }
        if ((realSelfView = data.getRealSelfViewEntity()) != null) {
            realSelfView.remove();
            data.setRealSelfViewEntity(null);
        }
    }

    private void unregisterVisualEntityIds(NativeDisguiseData data) {
        if (data.getDisplayEntityId() > 0) {
            this.fakeEntityIndex.remove(data.getDisplayEntityId());
        }
        if (data.getInteractionEntityId() > 0) {
            this.fakeEntityIndex.remove(data.getInteractionEntityId());
        }
        this.fakeEntityIndex.remove(data.getFakeEntityId());
        this.fakeEntityIndex.remove(data.getSelfViewEntityId());
    }

    private void hideVisualEntitiesDuringSolidify(Player owner, NativeDisguiseData data) {
        BlockDisplay display = this.getDisplayEntity(data);
        Interaction interaction = this.getInteractionEntity(data);
        for (Player viewer : Bukkit.getOnlinePlayers()) {
            if (viewer.equals((Object)owner)) {
                if (interaction == null) continue;
                viewer.hideEntity((Plugin)this.plugin, (Entity)interaction);
                continue;
            }
            if (display != null) {
                viewer.hideEntity((Plugin)this.plugin, (Entity)display);
            }
            if (interaction == null) continue;
            viewer.hideEntity((Plugin)this.plugin, (Entity)interaction);
        }
    }

    private void showVisualEntitiesToViewers(Player owner, NativeDisguiseData data) {
        BlockDisplay display = this.getDisplayEntity(data);
        Interaction interaction = this.getInteractionEntity(data);
        for (Player viewer : Bukkit.getOnlinePlayers()) {
            if (display != null) {
                viewer.showEntity((Plugin)this.plugin, (Entity)display);
            }
            if (interaction == null || viewer.equals((Object)owner)) continue;
            viewer.showEntity((Plugin)this.plugin, (Entity)interaction);
        }
    }

    private void clearSolidBlockVisuals(NativeDisguiseData data) {
        Location solidLoc = data.getSolidLocation();
        if (solidLoc != null) {
            Location cloned = solidLoc.clone();
            data.setSolidified(false, null);
            this.clearSolidBlockVisuals(cloned);
        }
    }

    private void clearSolidBlockVisuals(Location solidLoc) {
        if (solidLoc == null || solidLoc.getWorld() == null) {
            return;
        }
        final Location locToReset = solidLoc.clone();
        final org.bukkit.block.data.BlockData airData = org.bukkit.Bukkit.createBlockData(Material.AIR);
        for (Player viewer : locToReset.getWorld().getPlayers()) {
            viewer.sendBlockChange(locToReset, airData);
        }
        Bukkit.getScheduler().runTaskLater((Plugin)this.plugin, () -> {
            if (locToReset.getWorld() == null) return;
            for (Player viewer : locToReset.getWorld().getPlayers()) {
                viewer.sendBlockChange(locToReset, airData);
            }
        }, 3L);
    }

    private void sendSolidBlockVisual(Player disguisedPlayer, NativeDisguiseData data, Player viewer) {
        Location solidLoc = data.getSolidLocation();
        if (viewer.equals((Object)disguisedPlayer)) {
            return;
        }
        if (solidLoc == null || !viewer.getWorld().equals((Object)solidLoc.getWorld())) {
            return;
        }
        viewer.sendBlockChange(solidLoc.getBlock().getLocation(), data.getBlockMaterial().createBlockData());
    }

    private void refreshSolidBlockVisual(Player disguisedPlayer, NativeDisguiseData data) {
        if (data == null || !data.isSolidified() || data.getSolidLocation() == null) {
            return;
        }
        for (Player viewer : data.getSolidLocation().getWorld().getPlayers()) {
            this.sendSolidBlockVisual(disguisedPlayer, data, viewer);
        }
    }

    private void refreshSolidBlockVisualsFor(Player viewer) {
        for (NativeDisguiseData data : this.activeDisguises.values()) {
            Player disguisedPlayer;
            if (!data.isSolidified() || data.getSolidLocation() == null || !viewer.getWorld().equals((Object)data.getSolidLocation().getWorld()) || (disguisedPlayer = Bukkit.getPlayer((UUID)data.getPlayerUUID())) == null || !disguisedPlayer.isOnline()) continue;
            this.sendSolidBlockVisual(disguisedPlayer, data, viewer);
        }
    }

    private BlockDisplay getDisplayEntity(NativeDisguiseData data) {
        BlockDisplay display;
        Entity entity = this.getEntity(data.getDisplayEntityUUID());
        return entity instanceof BlockDisplay ? (display = (BlockDisplay)entity) : null;
    }

    private Interaction getInteractionEntity(NativeDisguiseData data) {
        Interaction interaction;
        Entity entity = this.getEntity(data.getInteractionEntityUUID());
        return entity instanceof Interaction ? (interaction = (Interaction)entity) : null;
    }

    private Entity getEntity(UUID uuid) {
        return uuid == null ? null : Bukkit.getEntity((UUID)uuid);
    }

    private Location toDisplayLocation(Location source) {
        return new Location(source.getWorld(), source.getX() - 0.5, source.getY(), source.getZ() - 0.5, 0.0f, 0.0f);
    }

    private Location toInteractionLocation(Location source) {
        return new Location(source.getWorld(), source.getX(), source.getY(), source.getZ(), source.getYaw(), source.getPitch());
    }

    private Vector3d toSelfViewPosition(Location loc) {
        return new Vector3d(loc.getX(), loc.getY(), loc.getZ());
    }

    private Location toCreatureSelfViewLocation(Player player, NativeDisguiseData data) {
        Location loc = player.getLocation().clone();
        Vector dir = loc.getDirection().setY(0).normalize();
        if (dir.lengthSquared() > 0.0) {
            loc.subtract(dir.multiply(0.5));
        }
        loc.setPitch(0.0f);
        return loc;
    }

    private Location toDisplayCreatureSelfViewLocation(Player player, NativeDisguiseData data) {
        Location loc = player.getLocation().clone();
        loc.setPitch(0.0f);
        return loc;
    }

    private ItemStack createDisplayModelItem(EntityType type) {
        ItemStack stack = new ItemStack(Material.PAPER);
        ItemMeta meta = stack.getItemMeta();
        if (meta != null) {
            int modelData = this.getDisplayModelData(type);
            meta.setCustomModelData(Integer.valueOf(modelData));
            CustomModelDataComponent customModelData = meta.getCustomModelDataComponent();
            customModelData.setFloats(List.of(Float.valueOf(modelData)));
            meta.setCustomModelDataComponent(customModelData);
            meta.setDisplayName("Disguise " + type.name());
            stack.setItemMeta(meta);
        }
        return stack;
    }

    private int getDisplayModelData(EntityType type) {
        return switch (type) {
            case EntityType.ZOMBIE -> 700001;
            case EntityType.SKELETON -> 700002;
            case EntityType.CREEPER -> 700003;
            case EntityType.SPIDER -> 700004;
            case EntityType.ENDERMAN -> 700005;
            case EntityType.PIG -> 700006;
            case EntityType.COW -> 700007;
            case EntityType.SHEEP -> 700008;
            case EntityType.CHICKEN -> 700009;
            case EntityType.VILLAGER -> 700010;
            case EntityType.IRON_GOLEM -> 700011;
            case EntityType.WOLF -> 700012;
            case EntityType.OCELOT -> 700013;
            case EntityType.SLIME -> 700014;
            case EntityType.MAGMA_CUBE -> 700015;
            case EntityType.BLAZE -> 700016;
            case EntityType.ZOMBIFIED_PIGLIN -> 700017;
            case EntityType.WITCH -> 700018;
            case EntityType.BAT -> 700019;
            default -> 790000 + type.ordinal();
        };
    }

    private double getMobVisualScale(EntityType type) {
        return switch (type) {
            case EntityType.PIG -> 0.75;
            case EntityType.CHICKEN, EntityType.PARROT, EntityType.BAT -> 0.65;
            case EntityType.COW -> 1.15;
            case EntityType.SPIDER -> 0.85;
            case EntityType.IRON_GOLEM -> 1.5;
            case EntityType.WOLF, EntityType.CAT -> 0.5;
            case EntityType.HORSE, EntityType.DONKEY, EntityType.MULE -> 1.5;
            case EntityType.ENDERMAN -> 1.7;
            default -> 1.0;
        };
    }

    private void hideRealPlayerFromViewers(Player hidden) {
        for (Player viewer : Bukkit.getOnlinePlayers()) {
            if (viewer.equals((Object)hidden)) continue;
            viewer.hideEntity((Plugin)this.plugin, (Entity)hidden);
        }
    }

    private void showRealPlayerToViewers(Player shown) {
        for (Player viewer : Bukkit.getOnlinePlayers()) {
            if (viewer.equals((Object)shown)) continue;
            viewer.showEntity((Plugin)this.plugin, (Entity)shown);
        }
    }

    private void refreshAllViewerHides() {
        for (UUID uuid : this.activeDisguises.keySet()) {
            Player hidden = Bukkit.getPlayer((UUID)uuid);
            if (hidden == null || !hidden.isOnline()) continue;
            NativeDisguiseData data = this.activeDisguises.get(uuid);
            if (data.getType() == NativeDisguiseType.BLOCK) {
                this.hideRealPlayerFromViewers(hidden);
            }
            if (data.getType() != NativeDisguiseType.CREATURE) continue;
            this.refreshCreatureSelfViewVisibility(hidden, data);
        }
    }

    @EventHandler(priority=EventPriority.MONITOR)
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        if (this.activeDisguises.containsKey(player.getUniqueId())) {
            this.undisguise(player);
        }
    }

    @EventHandler(priority=EventPriority.HIGH)
    public void onSolidBlockAttack(PlayerInteractEvent event) {
        if (event.getAction() != Action.LEFT_CLICK_BLOCK) {
            return;
        }
        Block clicked = event.getClickedBlock();
        if (clicked == null) {
            return;
        }
        NativeDisguiseData data = this.getSolidifiedDisguiseAt(clicked.getX(), clicked.getY(), clicked.getZ(), clicked.getWorld().getName());
        if (data == null) {
            data = this.getGraceHitDisguiseAt(clicked.getX(), clicked.getY(), clicked.getZ(), clicked.getWorld().getName());
        }
        if (data != null && data.isActive()) {
            Player target = Bukkit.getPlayer((UUID)data.getPlayerUUID());
            Player attacker = event.getPlayer();
            if (target != null && target.isOnline() && !target.equals((Object)attacker)) {
                event.setCancelled(true);
                if (data.isSolidified()) {
                    this.breakSolidify(target, data);
                }
            }
        }
    }

    @EventHandler(priority=EventPriority.MONITOR)
    public void onPlayerJoin(PlayerJoinEvent event) {
        this.updateDatapackTotalScore();
        Bukkit.getScheduler().runTaskLater((Plugin)this.plugin, () -> {
            this.updateDatapackTotalScore();
            this.refreshAllViewerHides();
            this.refreshSolidBlockVisualsFor(event.getPlayer());
            for (NativeDisguiseData data : this.activeDisguises.values()) {
                Player owner;
                if (data.isSolidified() && (owner = Bukkit.getPlayer((UUID)data.getPlayerUUID())) != null) {
                    this.hideVisualEntitiesDuringSolidify(owner, data);
                }
                if (data.getType() != NativeDisguiseType.CREATURE || (owner = Bukkit.getPlayer((UUID)data.getPlayerUUID())) == null || !owner.isOnline()) continue;
                this.refreshCreatureSelfViewVisibility(owner, data);
            }
        }, 2L);
    }

    @EventHandler(priority=EventPriority.MONITOR)
    public void onPlayerQuitUpdateScores(PlayerQuitEvent event) {
        Bukkit.getScheduler().runTask((Plugin)this.plugin, this::updateDatapackTotalScore);
    }

    @EventHandler(priority=EventPriority.MONITOR)
    public void onPlayerChangedWorld(PlayerChangedWorldEvent event) {
        Bukkit.getScheduler().runTask((Plugin)this.plugin, () -> {
            this.refreshAllViewerHides();
            NativeDisguiseData data = this.activeDisguises.get(event.getPlayer().getUniqueId());
            if (data != null && !data.isSolidified()) {
                if (data.getType() == NativeDisguiseType.CREATURE) {
                    this.applyCreatureSelfView(event.getPlayer(), data);
                } else {
                    this.syncServerSideVisuals(event.getPlayer(), data, event.getPlayer().getLocation());
                }
            }
        });
    }

    private boolean sameBlock(Location a, Location b) {
        return a.getBlockX() == b.getBlockX() && a.getBlockY() == b.getBlockY() && a.getBlockZ() == b.getBlockZ();
    }

    private Location toRealSelfViewLocation(Player player, NativeDisguiseData data) {
        Location loc = player.getLocation().clone();
        loc.setPitch(0.0f);
        if (this.isTallCreature(data.getCreatureType())) {
            Vector backward = loc.getDirection();
            backward.setY(0);
            if (backward.lengthSquared() > 1.0E-4) {
                loc.add(backward.normalize().multiply(-0.5));
                loc.setY(player.getLocation().getY());
            }
        }
        return loc;
    }

    private boolean isTallCreature(EntityType type) {
        return switch (type) {
            case EntityType.ENDERMAN, EntityType.ZOMBIE, EntityType.SKELETON, EntityType.CREEPER, EntityType.VILLAGER, EntityType.BLAZE, EntityType.ZOMBIFIED_PIGLIN, EntityType.BREEZE, EntityType.EVOKER, EntityType.HUSK, EntityType.PIGLIN, EntityType.PIGLIN_BRUTE, EntityType.PILLAGER, EntityType.STRAY, EntityType.VINDICATOR, EntityType.WANDERING_TRADER, EntityType.WITHER_SKELETON, EntityType.ZOMBIE_VILLAGER -> true;
            default -> false;
        };
    }

    private String formatLoc(Location loc) {
        return String.format("(%d, %d, %d)", loc.getBlockX(), loc.getBlockY(), loc.getBlockZ());
    }

    public static enum SelfViewMode {
        OFF,
        REAL,
        FOLLOWER,
        DISPLAY;

    }
}
