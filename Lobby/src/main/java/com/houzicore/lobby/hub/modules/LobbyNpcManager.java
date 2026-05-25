package com.houzicore.lobby.hub.modules;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Villager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.player.PlayerInteractAtEntityEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.LeatherArmorMeta;
import org.bukkit.Color;
import org.bukkit.util.Vector;

import com.houzicore.shared.common.util.C;
import com.houzicore.shared.common.util.UtilText;
import com.houzicore.shared.core.hologram.Hologram;
import com.houzicore.shared.core.hologram.HologramManager;
import com.houzicore.lobby.hub.HubManager;

/**
 * Central NPC manager for Lobby mini-game NPCs.
 * Spawns themed Villager NPCs with floating Holograms and handles click events.
 */
public class LobbyNpcManager implements Listener {

    private static final double PLAYER_LOOK_RADIUS_SQUARED = 64.0D;
    private static final double POSITION_DRIFT_THRESHOLD_SQUARED = 0.04D;
    public static final String NPC_TAG = "houzicore_lobby_npc";
    public static final String LABEL_TAG = "houzicore_npc_label";

    public interface NpcClickHandler {
        void onInteract(Player player);
    }

    private final HubManager _hub;
    private final HologramManager _hologramManager;
    private final Map<Entity, NpcClickHandler> _npcHandlers = new HashMap<>();
    private final Map<Entity, Location> _npcAnchors = new HashMap<>();
    private final List<Entity> _npcs = new ArrayList<>();
    private final Map<UUID, Entity> _npcUuids = new HashMap<>();
    private final List<Hologram> _holograms = new ArrayList<>();
    private final Map<Entity, Hologram> _npcHolograms = new HashMap<>();
    private final Map<UUID, Long> _recentNpcInteractions = new HashMap<>();

    public LobbyNpcManager(HubManager hub, HologramManager hologramManager) {
        _hub = hub;
        _hologramManager = hologramManager;

        Location spawn = hub.GetSpawn();
        if (spawn != null && spawn.getWorld() != null) {
            spawn.getWorld().setSpawnLocation(spawn.getBlockX(), spawn.getBlockY(), spawn.getBlockZ());
        }
        
        // Cleanup old persistent NPCs from previous reloads
        try {
            org.bukkit.World world = org.bukkit.Bukkit.getWorlds().get(0);
            for (Entity e : world.getEntities()) {
                if (e.getScoreboardTags().contains(NPC_TAG) || e.getScoreboardTags().contains(LABEL_TAG)) {
                    e.remove();
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        hub.getPlugin().getServer().getPluginManager().registerEvents(this, hub.getPlugin());
        hub.getPlugin().getServer().getScheduler().scheduleSyncRepeatingTask(hub.getPlugin(), this::stabilizeNpcs, 1L, 5L);

        // Register PacketEvents listener for our PacketNpcs
        com.github.retrooper.packetevents.PacketEvents.getAPI().getEventManager().registerListener(
            new com.github.retrooper.packetevents.event.PacketListenerAbstract() {
                @Override
                public void onPacketReceive(com.github.retrooper.packetevents.event.PacketReceiveEvent event) {
                    if (event.getPacketType() == com.github.retrooper.packetevents.protocol.packettype.PacketType.Play.Client.INTERACT_ENTITY) {
                        com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientInteractEntity interactPacket = 
                            new com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientInteractEntity(event);
                        
                        // We care about INTERACT (right click) and INTERACT_AT (right click on specific body part)
                        if (interactPacket.getAction() == com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientInteractEntity.InteractAction.INTERACT ||
                            interactPacket.getAction() == com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientInteractEntity.InteractAction.INTERACT_AT) {
                            int targetId = interactPacket.getEntityId();
                            if (_packetNpcHandlers.containsKey(targetId)) {
                                Player player = (Player) event.getPlayer();
                                
                                Long lastInteract = _recentNpcInteractions.get(player.getUniqueId());
                                if (lastInteract != null && System.currentTimeMillis() - lastInteract < 1000) {
                                    return; // Cooldown 1s
                                }
                                _recentNpcInteractions.put(player.getUniqueId(), System.currentTimeMillis());
                                
                                Bukkit.getScheduler().runTask(_hub.getPlugin(), () -> {
                                    _packetNpcHandlers.get(targetId).onInteract(player);
                                });
                            }
                        }
                    }
                }
            }
        );
    }

    /**
     * Spawn a themed NPC Villager at the given location.
     *
     * @param loc         Spawn location
     * @param displayName Top hologram line (colored)
     * @param subtitle    Second hologram line
     * @param clickHint   Third hologram line (click instruction)
     * @param profession  Villager profession for visual theme
     * @param armorColor  Leather armor color (null = no armor)
     * @param handler     Click callback
     * @return The spawned entity
     */
    public Entity spawnNpc(Location loc, String displayName, String subtitle, String clickHint,
                           Villager.Profession profession, Color armorColor, NpcClickHandler handler) {

        Location anchor = findSafeStandLocation(loc).clone();
        anchor.getChunk().load();
        Villager villager = (Villager) anchor.getWorld().spawnEntity(anchor, EntityType.VILLAGER);
        villager.setProfession(profession);
        villager.setVillagerLevel(5); // Diamond badge
        villager.setAI(false);
        villager.setInvulnerable(true);
        villager.setSilent(true);
        villager.setCollidable(false);
        villager.setCustomNameVisible(false);
        villager.setPersistent(true);
        villager.setRemoveWhenFarAway(false);
        villager.setRecipes(new ArrayList<>()); // Prevent trade gui
        villager.addScoreboardTag(NPC_TAG);
        villager.setRotation(anchor.getYaw(), anchor.getPitch());
        com.houzicore.shared.common.util.UtilEnt.Vegetate(villager);

        // Leather armor for color coding
        if (armorColor != null) {
            villager.getEquipment().setBoots(coloredLeather(Material.LEATHER_BOOTS, armorColor));
            villager.getEquipment().setLeggings(coloredLeather(Material.LEATHER_LEGGINGS, armorColor));
            villager.getEquipment().setChestplate(coloredLeather(Material.LEATHER_CHESTPLATE, armorColor));
        }

        // Hologram above head
        Location holoLoc = anchor.clone().add(0, 2.6, 0);
        Hologram holo = new Hologram(_hologramManager, holoLoc,
            displayName,
            subtitle,
            clickHint
        );
        holo.start();

        _npcs.add(villager);
        _npcUuids.put(villager.getUniqueId(), villager);
        _holograms.add(holo);
        _npcHolograms.put(villager, holo);
        _npcHandlers.put(villager, handler);
        _npcAnchors.put(villager, anchor);

        return villager;
    }

    private final Map<String, NpcClickHandler> _fancyNpcHandlers = new HashMap<>();

    private final Map<Integer, NpcClickHandler> _packetNpcHandlers = new HashMap<>();
    private final List<PacketNpc> _packetNpcs = new ArrayList<>();
    private final Map<PacketNpc, org.bukkit.entity.TextDisplay> _packetNpcLabels = new HashMap<>();
    private final Map<PacketNpc, String> _packetNpcServerKeys = new HashMap<>();
    private final Map<PacketNpc, String> _packetNpcGameNames = new HashMap<>();
    private final Map<PacketNpc, String> _packetNpcClickHints = new HashMap<>();

    public PacketNpc spawnPlayerSkinNpc(Location loc, String gameName, String serverKey, String clickHint,
                                     String skinName, String skinValue, String skinSignature, NpcClickHandler handler) {

        Location anchor = loc.clone();
        anchor.getChunk().load();

        try {
            com.github.retrooper.packetevents.protocol.world.Location peLoc = 
                new com.github.retrooper.packetevents.protocol.world.Location(
                    anchor.getX(), anchor.getY(), anchor.getZ(), anchor.getYaw(), anchor.getPitch()
                );
            
            // Build a clean profile name from gameName (max 16 chars, alphanumeric only)
            String profileName = org.bukkit.ChatColor.stripColor(gameName).replaceAll("[^a-zA-Z0-9]", "");
            if (profileName.length() > 16) {
                profileName = profileName.substring(0, 16);
            }
            
            PacketNpc npc = new PacketNpc(_hub.getPlugin(), profileName, skinName, peLoc);
            if (skinValue != null && !skinValue.isEmpty()) {
                npc.setRawSkin(skinValue, skinSignature);
            }
            
            _packetNpcs.add(npc);
            _packetNpcHandlers.put(npc.getEntityId(), handler);

            npc.fetchSkinAndSpawn();

            // Spawn direct TextDisplay label above NPC head
            Location holoLoc = anchor.clone().add(0, 2.3, 0);
            org.bukkit.entity.TextDisplay label = (org.bukkit.entity.TextDisplay) 
                anchor.getWorld().spawnEntity(holoLoc, org.bukkit.entity.EntityType.TEXT_DISPLAY);
            label.setPersistent(false);
            label.setBillboard(org.bukkit.entity.Display.Billboard.CENTER);
            label.setShadowed(true);
            label.setBackgroundColor(org.bukkit.Color.fromARGB(0, 0, 0, 0));
            label.setAlignment(org.bukkit.entity.TextDisplay.TextAlignment.CENTER);
            label.addScoreboardTag(LABEL_TAG); // Use LABEL_TAG, not NPC_TAG!
            
            // Build initial premium label text
            String initText = gameName + "\n" +
                "\u00a78\u00a7o\u2588 \u00a77Loading... \u00a78\u00a7o\u2588" + "\n" +
                (clickHint != null ? clickHint : "");
            label.text(net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer
                .legacySection().deserialize(initText));
            
            _packetNpcLabels.put(npc, label);
            _packetNpcServerKeys.put(npc, serverKey);
            _packetNpcGameNames.put(npc, gameName);
            _packetNpcClickHints.put(npc, clickHint);

            return npc;
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    public PacketNpc spawnPlayerSkinNpc(Location loc, String gameName, String serverKey, String clickHint,
                                     String skinName, NpcClickHandler handler) {
        return spawnPlayerSkinNpc(loc, gameName, serverKey, clickHint, skinName, null, null, handler);
    }

    public void removePacketNpc(PacketNpc npc) {
        if (npc == null) return;
        
        // Hide from all players
        for (Player p : Bukkit.getOnlinePlayers()) {
            npc.hideFrom(p);
        }
        
        _packetNpcs.remove(npc);
        _packetNpcHandlers.remove(npc.getEntityId());
        
        org.bukkit.entity.TextDisplay label = _packetNpcLabels.remove(npc);
        if (label != null && label.isValid()) {
            label.remove();
        }
        
        _packetNpcServerKeys.remove(npc);
        _packetNpcGameNames.remove(npc);
        _packetNpcClickHints.remove(npc);
    }

    // Since PacketNpcs are not sent to new players automatically by Bukkit,
    // we need to handle PlayerJoinEvent to show them.
    @EventHandler
    public void onPlayerJoinPacketNpcs(org.bukkit.event.player.PlayerJoinEvent event) {
        sendPacketNpcsSafely(event.getPlayer());
    }

    @EventHandler
    public void onPlayerChangeWorldPacketNpcs(org.bukkit.event.player.PlayerChangedWorldEvent event) {
        if (_hub.GetSpawn() != null && _hub.GetSpawn().getWorld() != null && 
            event.getPlayer().getWorld().getName().equals(_hub.GetSpawn().getWorld().getName())) {
            sendPacketNpcsSafely(event.getPlayer());
        }
    }

    private void sendPacketNpcsSafely(Player player) {
        // We MUST delay sending the spawn packets. If sent immediately on Join/WorldChange,
        // the client is still in the "Downloading Terrain" screen and will drop the entity packets.
        Bukkit.getScheduler().runTaskLater(_hub.getPlugin(), () -> {
            if (player != null && player.isOnline() && 
                _hub.GetSpawn() != null && _hub.GetSpawn().getWorld() != null &&
                player.getWorld().getName().equals(_hub.GetSpawn().getWorld().getName())) {
                for (PacketNpc npc : _packetNpcs) {
                    npc.showTo(player);
                }
            }
        }, 20L); // 1 second delay
    }

    @EventHandler
    public void onUpdateTick(com.houzicore.shared.updater.event.UpdateEvent event) {
        if (event.getType() != com.houzicore.shared.updater.UpdateType.TICK) return;

        // Look at nearest player
        for (PacketNpc npc : _packetNpcs) {
            Player nearest = null;
            double nearestDistSq = PLAYER_LOOK_RADIUS_SQUARED;

            for (Player p : Bukkit.getOnlinePlayers()) {
                if (p.getWorld() != _hub.GetSpawn().getWorld()) continue;
                double distSq = p.getLocation().distanceSquared(new Location(p.getWorld(), npc.getLocation().getX(), npc.getLocation().getY(), npc.getLocation().getZ()));
                if (distSq < nearestDistSq) {
                    nearestDistSq = distSq;
                    nearest = p;
                }
            }

            if (nearest != null) {
                npc.lookAt(nearest.getEyeLocation());
            }
        }
    }

    @EventHandler
    public void onUpdateSec(com.houzicore.shared.updater.event.UpdateEvent event) {
        if (event.getType() != com.houzicore.shared.updater.UpdateType.SEC) return;

        // Recreate holograms that died due to chunk unload
        for (Hologram holo : _holograms) {
            if (!holo.isInUse() && holo.getLocation().getChunk().isLoaded()) {
                holo.start();
            }
        }

        com.houzicore.lobby.hub.server.ServerManager sm = null;
        try {
            sm = com.houzicore.shared.core.plugin.PluginRegistry.require(com.houzicore.lobby.hub.server.ServerManager.class);
        } catch (Exception e) {}

        for (PacketNpc npc : _packetNpcs) {
            org.bukkit.entity.TextDisplay label = _packetNpcLabels.get(npc);
            String serverKey = _packetNpcServerKeys.get(npc);
            String gameName = _packetNpcGameNames.get(npc);
            String clickHint = _packetNpcClickHints.get(npc);

            // Ensure Hologram label exists if chunk is loaded (fixes labels disappearing on chunk unload)
            com.github.retrooper.packetevents.protocol.world.Location peLoc = npc.getLocation();
            Location holoLoc = new Location(_hub.GetSpawn().getWorld(), peLoc.getX(), peLoc.getY() + 2.3, peLoc.getZ());
            
            if ((label == null || !label.isValid() || label.isDead()) && holoLoc.getChunk().isLoaded()) {
                label = (org.bukkit.entity.TextDisplay) holoLoc.getWorld().spawnEntity(holoLoc, org.bukkit.entity.EntityType.TEXT_DISPLAY);
                label.setPersistent(false);
                label.setBillboard(org.bukkit.entity.Display.Billboard.CENTER);
                label.setShadowed(true);
                label.setBackgroundColor(org.bukkit.Color.fromARGB(0, 0, 0, 0));
                label.setAlignment(org.bukkit.entity.TextDisplay.TextAlignment.CENTER);
                label.addScoreboardTag(LABEL_TAG);
                _packetNpcLabels.put(npc, label);
            }

            if (label != null && label.isValid() && serverKey != null) {
                String statusLine = "§7Loading...";
                int currentPlayers = 0;
                if (sm != null) {
                    statusLine = sm.getNpcStatusLine(serverKey);
                    currentPlayers = sm.getTotalPlayers(serverKey);
                }
                
                // Build premium status text
                String statusText;
                if (currentPlayers > 0) {
                    statusText = "\u00a7a\u25cf Online \u00a78\u2502 \u00a7f" + currentPlayers + " \u00a77Players";
                } else {
                    statusText = sm.getNpcStatusLine(serverKey); // e.g. §c§lOFFLINE
                }
                
                String labelText = gameName + "\n" +
                    statusText + "\n" +
                    (clickHint != null ? clickHint : "");
                label.text(net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer
                    .legacySection().deserialize(labelText));

                if (currentPlayers >= 50) {
                    com.houzicore.shared.common.util.UtilParticle.PlayParticle(
                        com.houzicore.shared.common.util.UtilParticle.ParticleType.FLAME, 
                        new Location(_hub.GetSpawn().getWorld(), npc.getLocation().getX(), npc.getLocation().getY() + 1.0, npc.getLocation().getZ()), 
                        0.4f, 0.4f, 0.4f, 0.05f, 2, 
                        com.houzicore.shared.common.util.UtilParticle.ViewDist.NORMAL,
                        com.houzicore.shared.common.util.UtilServer.getPlayers()
                    );
                } else if (currentPlayers > 0) {
                    com.houzicore.shared.common.util.UtilParticle.PlayParticle(
                        com.houzicore.shared.common.util.UtilParticle.ParticleType.HAPPY_VILLAGER, 
                        new Location(_hub.GetSpawn().getWorld(), npc.getLocation().getX(), npc.getLocation().getY() + 1.0, npc.getLocation().getZ()), 
                        0.4f, 0.4f, 0.4f, 0.01f, 1, 
                        com.houzicore.shared.common.util.UtilParticle.ViewDist.NORMAL,
                        com.houzicore.shared.common.util.UtilServer.getPlayers()
                    );
                }
            }

            // Idle animation
            if (Math.random() < 0.1) {
                npc.playAnimation(com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerEntityAnimation.EntityAnimationType.SWING_MAIN_ARM);
            }
        }
    }


    public Entity spawnEntityNpc(Location loc, String displayName, String subtitle, String clickHint,
                           EntityType entityType, double hologramOffset, NpcClickHandler handler) {

        Location anchor = findSafeStandLocation(loc).clone();
        anchor.getChunk().load();
        LivingEntity entity = (LivingEntity) anchor.getWorld().spawnEntity(anchor, entityType);
        entity.setAI(false);
        entity.setInvulnerable(true);
        entity.setSilent(true);
        entity.setCollidable(false);
        entity.setCustomNameVisible(false);
        entity.setPersistent(true);
        entity.setRemoveWhenFarAway(false);
        entity.addScoreboardTag(NPC_TAG);
        entity.setRotation(anchor.getYaw(), anchor.getPitch());
        com.houzicore.shared.common.util.UtilEnt.Vegetate(entity);

        // Hologram above head
        Location holoLoc = anchor.clone().add(0, hologramOffset, 0);
        Hologram holo = new Hologram(_hologramManager, holoLoc,
            displayName,
            subtitle,
            clickHint
        );
        holo.start();

        _npcs.add(entity);
        _npcUuids.put(entity.getUniqueId(), entity);
        _holograms.add(holo);
        _npcHolograms.put(entity, holo);
        _npcHandlers.put(entity, handler);
        _npcAnchors.put(entity, anchor);

        return entity;
    }

    private Location findSafeStandLocation(Location source) {
        Location centered = new Location(source.getWorld(), source.getBlockX() + 0.5, source.getBlockY(), source.getBlockZ() + 0.5, source.getYaw(), source.getPitch());

        for (int offsetY = 0; offsetY <= 4; offsetY++) {
            Location probe = centered.clone().add(0, offsetY, 0);
            if (isStandable(probe)) {
                return probe;
            }
        }

        return centered;
    }

    private boolean isStandable(Location location) {
        Block feet = location.getBlock();
        Block head = feet.getLocation().add(0, 1, 0).getBlock();
        return feet.isPassable() && head.isPassable();
    }

    public static boolean isLobbyNpc(Entity entity) {
        return entity != null && entity.getScoreboardTags().contains(NPC_TAG);
    }

    private void stabilizeNpcs() {
        for (Entity npc : new ArrayList<Entity>(_npcs)) {
            if (npc == null || npc.isDead()) {
                _npcs.remove(npc);
                _npcUuids.remove(npc.getUniqueId());
                _npcHandlers.remove(npc);
                _npcAnchors.remove(npc);
                continue;
            }
            if (!npc.isValid()) {
                continue; // Unloaded chunk, keep in list but skip stabilization
            }

            Location anchor = _npcAnchors.get(npc);
            if (anchor == null || anchor.getWorld() == null) {
                continue;
            }

            Player target = findNearestPlayer(anchor);
            Location desired = buildDesiredFacing(anchor, target);
            npc.setVelocity(new Vector(0, 0, 0));

            if (needsReanchor(npc, desired)) {
                npc.teleport(desired);
            } else {
                npc.setRotation(desired.getYaw(), desired.getPitch());
            }
        }
    }

    private Player findNearestPlayer(Location anchor) {
        Player closest = null;
        double closestDistance = PLAYER_LOOK_RADIUS_SQUARED;

        for (Player player : anchor.getWorld().getPlayers()) {
            if (player == null || !player.isOnline()) {
                continue;
            }

            double distance = player.getLocation().distanceSquared(anchor);
            if (distance > closestDistance) {
                continue;
            }

            closest = player;
            closestDistance = distance;
        }

        return closest;
    }

    private Location buildDesiredFacing(Location anchor, Player target) {
        Location desired = anchor.clone();

        if (target == null) {
            return desired;
        }

        Vector direction = target.getLocation().toVector().subtract(desired.toVector());
        direction.setY(0);

        if (direction.lengthSquared() > 0.0001D) {
            desired.setDirection(direction);
            desired.setPitch(0f);
        }

        return desired;
    }

    private boolean needsReanchor(Entity npc, Location desired) {
        if (!npc.getWorld().equals(desired.getWorld())) {
            return true;
        }

        return npc.getLocation().distanceSquared(desired) > POSITION_DRIFT_THRESHOLD_SQUARED;
    }

    private ItemStack coloredLeather(Material mat, Color color) {
        ItemStack item = new ItemStack(mat);
        LeatherArmorMeta meta = (LeatherArmorMeta) item.getItemMeta();
        meta.setColor(color);
        item.setItemMeta(meta);
        return item;
    }

    // Removed onUpdate rotation logic to keep NPCs completely stationary

    @EventHandler
    public void onChunkLoad(org.bukkit.event.world.ChunkLoadEvent event) {
        for (Entity e : event.getChunk().getEntities()) {
            // Restore persistent NPCs that were loaded back into memory
            if (e.getScoreboardTags().contains(NPC_TAG)) {
                if (_npcUuids.containsKey(e.getUniqueId())) {
                    Entity oldRef = _npcUuids.get(e.getUniqueId());
                    if (oldRef != e) {
                        _npcs.remove(oldRef);
                        _npcs.add(e);
                        _npcUuids.put(e.getUniqueId(), e);
                        
                        NpcClickHandler handler = _npcHandlers.remove(oldRef);
                        if (handler != null) _npcHandlers.put(e, handler);
                        
                        Location anchor = _npcAnchors.remove(oldRef);
                        if (anchor != null) _npcAnchors.put(e, anchor);
                    }
                } else if (!_npcs.contains(e)) {
                    e.remove(); // Truly orphaned
                }
            }
            // Clean orphaned labels that aren't tracked
            if (e.getScoreboardTags().contains(LABEL_TAG)) {
                boolean tracked = false;
                for (org.bukkit.entity.TextDisplay td : _packetNpcLabels.values()) {
                    if (td != null && td.getEntityId() == e.getEntityId()) {
                        tracked = true;
                        break;
                    }
                }
                if (!tracked) {
                    e.remove();
                }
            }
        }
    }

    // LOWEST priority to cancel before Vanilla trade gui opens
    @EventHandler(priority = org.bukkit.event.EventPriority.LOWEST)
    public void onInteract(PlayerInteractEntityEvent event) {
        Entity clicked = event.getRightClicked();
        NpcClickHandler handler = _npcHandlers.get(clicked);
        if (handler == null) return;

        event.setCancelled(true);
        triggerInteraction(event.getPlayer(), handler);
    }

    @EventHandler(priority = org.bukkit.event.EventPriority.LOWEST)
    public void onInteractAt(PlayerInteractAtEntityEvent event) {
        Entity clicked = event.getRightClicked();
        NpcClickHandler handler = _npcHandlers.get(clicked);
        if (handler == null) return;

        event.setCancelled(true);
        triggerInteraction(event.getPlayer(), handler);
    }

    @EventHandler
    public void onDamage(EntityDamageByEntityEvent event) {
        if (_npcHandlers.containsKey(event.getEntity())) {
            event.setCancelled(true);
            if (event.getDamager() instanceof Player) {
                Player player = (Player) event.getDamager();
                NpcClickHandler handler = _npcHandlers.get(event.getEntity());
                if (handler != null) {
                    triggerInteraction(player, handler);
                }
            }
        }
    }

    @EventHandler
    public void preventNpcDamage(EntityDamageEvent event) {
        if (_npcHandlers.containsKey(event.getEntity())) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = org.bukkit.event.EventPriority.LOWEST)
    public void onInventoryOpen(InventoryOpenEvent event) {
        if (!(event.getPlayer() instanceof Player player)) {
            return;
        }

        Long lastNpcInteraction = _recentNpcInteractions.get(player.getUniqueId());
        if (lastNpcInteraction == null) {
            return;
        }

        if (System.currentTimeMillis() - lastNpcInteraction > 1500L) {
            _recentNpcInteractions.remove(player.getUniqueId());
            return;
        }

        if (event.getInventory().getType() == org.bukkit.event.inventory.InventoryType.MERCHANT) {
            event.setCancelled(true);
            _hub.getPlugin().getServer().getScheduler().runTask(_hub.getPlugin(), new Runnable() {
                @Override
                public void run() {
                    player.closeInventory();
                }
            });
        }
    }

    private void triggerInteraction(Player player, NpcClickHandler handler) {
        _recentNpcInteractions.put(player.getUniqueId(), System.currentTimeMillis());
        player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 0.8f, 1.2f);
        com.houzicore.shared.common.util.UtilParticle.PlayParticle(
            com.houzicore.shared.common.util.UtilParticle.ParticleType.HAPPY_VILLAGER, 
            player.getLocation().add(0, 1, 0), 
            0.5f, 0.5f, 0.5f, 0.1f, 10, 
            com.houzicore.shared.common.util.UtilParticle.ViewDist.NORMAL,
            com.houzicore.shared.common.util.UtilServer.getPlayers()
        );
        handler.onInteract(player);
    }

    public void despawnNpc(Entity entity) {
        if (entity == null) return;
        entity.remove();
        _npcs.remove(entity);
        _npcUuids.remove(entity.getUniqueId());
        Hologram holo = _npcHolograms.remove(entity);
        if (holo != null) {
            holo.stop();
            _holograms.remove(holo);
        }
        _npcHandlers.remove(entity);
        _npcAnchors.remove(entity);
    }

    public void cleanup() {
        for (Entity e : _npcs) {
            if (e != null && !e.isDead()) e.remove();
        }
        for (Hologram h : _holograms) {
            if (h != null && h.isInUse()) h.stop();
        }
        // Cleanup FancyNpcs (FancyNpcs plugin usually handles its own on server shutdown/reload, but we can clear our handlers)
        _fancyNpcHandlers.clear();
        _npcs.clear();
        _holograms.clear();
        _npcHolograms.clear();
        _packetNpcs.clear();
        for (org.bukkit.entity.TextDisplay td : _packetNpcLabels.values()) {
            if (td != null && td.isValid()) td.remove();
        }
        _packetNpcLabels.clear();
        _packetNpcServerKeys.clear();
        _packetNpcGameNames.clear();
        _packetNpcClickHints.clear();
        _npcHandlers.clear();
        _npcAnchors.clear();
    }
}
