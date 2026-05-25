package com.houzicore.shared.core.disguise.v2.packet;

import com.github.retrooper.packetevents.event.PacketListenerAbstract;
import com.github.retrooper.packetevents.event.PacketListenerPriority;
import com.github.retrooper.packetevents.event.PacketReceiveEvent;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientInteractEntity;
import com.houzicore.shared.core.disguise.v2.engine.NativeDisguiseData;
import com.houzicore.shared.core.disguise.v2.engine.NativeDisguiseEngine;
import com.github.retrooper.packetevents.event.PacketSendEvent;
import com.github.retrooper.packetevents.util.Vector3i;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerBlockChange;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerPlayerInfoRemove;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerPlayerInfoUpdate;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerSpawnPlayer;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerSpawnLivingEntity;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerEntityEffect;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerRemoveEntityEffect;
import com.github.retrooper.packetevents.protocol.potion.PotionTypes;
import io.github.retrooper.packetevents.util.SpigotConversionUtil;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerSpawnEntity;
import com.github.retrooper.packetevents.protocol.entity.data.EntityData;
import com.github.retrooper.packetevents.protocol.entity.data.EntityDataTypes;
import org.bukkit.entity.Player;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * PacketEvents listener for combat routing.
 *
 * This is the key Mineplex 2018 innovation for block disguises:
 * When a player attacks/interacts with the fake BlockDisplay entity,
 * we intercept the INTERACT_ENTITY packet and redirect it to the
 * real player entity, so damage flows correctly through the server.
 *
 * Mineplex 2018 did this by rewriting the entity ID field in
 * PacketPlayInUseEntity. We do the same with PacketEvents wrappers.
 */
public class NativeDisguisePacketListener extends PacketListenerAbstract {

    private final NativeDisguiseEngine NativeDisguiseEngine;
    private final org.bukkit.plugin.Plugin plugin;
    private final java.util.logging.Logger log;

    public NativeDisguisePacketListener(NativeDisguiseEngine NativeDisguiseEngine, org.bukkit.plugin.Plugin plugin) {
        super(PacketListenerPriority.HIGH);
        this.NativeDisguiseEngine = NativeDisguiseEngine;
        this.plugin = plugin;
        this.log = plugin.getLogger();

        if (org.bukkit.Bukkit.getPluginManager().isPluginEnabled("ProtocolLib")) {
            com.comphenix.protocol.ProtocolLibrary.getProtocolManager().addPacketListener(
                new com.comphenix.protocol.events.PacketAdapter(plugin, com.comphenix.protocol.PacketType.Play.Server.BUNDLE) {
                    @Override
                    public void onPacketSending(com.comphenix.protocol.events.PacketEvent event) {
                        Object nmsPacket = event.getPacket().getHandle();
                        if (nmsPacket == null) return;
                        
                        Iterable<?> subPackets = getBundleSubPackets(nmsPacket);
                        if (subPackets != null) {
                            boolean containsDisguised = false;
                            for (Object sub : subPackets) {
                                int entityId = getEntityIdFromNMSPacket(sub);
                                if (entityId != -1) {
                                    org.bukkit.entity.Player p = NativeDisguiseEngine.getRealPlayerByEntityId(entityId);
                                    if (p != null && NativeDisguiseEngine.isDisguised(p)) {
                                        containsDisguised = true;
                                        break;
                                    }
                                }
                            }
                            if (containsDisguised) {
                                event.setCancelled(true);
                                org.bukkit.entity.Player viewer = event.getPlayer();
                                log.fine("[Disguise-PktDbg] ProtocolLib unpacking BUNDLE for viewer=" + viewer.getName());
                                for (Object sub : subPackets) {
                                    sendNMSPacket(viewer, sub);
                                }
                            }
                        }
                    }
                }
            );
        }
    }

    @Override
    public void onPacketReceive(PacketReceiveEvent event) {
        if (event.getPacketType() != PacketType.Play.Client.INTERACT_ENTITY) {
            return;
        }

        WrapperPlayClientInteractEntity wrapper = new WrapperPlayClientInteractEntity(event);
        int targetEntityId = wrapper.getEntityId();

        // Check if this entity ID is one of our fake disguise entities or the real player's entity ID
        NativeDisguiseData data = NativeDisguiseEngine.getByFakeEntityId(targetEntityId);
        Player disguisedPlayer = null;
        if (data != null) {
            disguisedPlayer = Bukkit.getPlayer(data.getPlayerUUID());
        } else {
            disguisedPlayer = NativeDisguiseEngine.getRealPlayerByEntityId(targetEntityId);
            if (disguisedPlayer != null) {
                data = NativeDisguiseEngine.getByPlayer(disguisedPlayer);
            }
        }

        if (disguisedPlayer == null || data == null || !disguisedPlayer.isOnline()) {
            return;
        }

        Player attacker = (Player) event.getPlayer();

        // Cancel the packet entirely so Spigot's hidden player check does not drop it
        event.setCancelled(true);

        WrapperPlayClientInteractEntity.InteractAction action = wrapper.getAction();
        final NativeDisguiseData finalData = data;
        final Player finalDisguisedPlayer = disguisedPlayer;

        // Route interaction/damage manually on the main thread
        Bukkit.getScheduler().runTask(plugin, () -> {
            if (!attacker.isOnline() || !finalDisguisedPlayer.isOnline()) {
                return;
            }
            if (action == WrapperPlayClientInteractEntity.InteractAction.ATTACK) {
                if (finalData.getType() == com.houzicore.shared.core.disguise.v2.engine.NativeDisguiseType.BLOCK && finalData.isSolidified()) {
                    NativeDisguiseEngine.breakSolidify(finalDisguisedPlayer, finalData);
                } else {
                    finalDisguisedPlayer.damage(2.0, attacker);
                    // Clear no-damage ticks to allow responsive follow-up hits conforming to custom 250ms cooldown
                    finalDisguisedPlayer.setNoDamageTicks(0);
                }
            }
        });
    }
    /**
     * Intercept block change packets sent to the client to prevent right-click flickering.
     * When Bukkit cancels a PlayerInteractEvent, it sends a BLOCK_CHANGE packet to reset
     * the block to its true server state (AIR). We intercept this and send our fake block instead.
     */
    @Override
    public void onPacketSend(PacketSendEvent event) {
        if (event.getPacketType() == PacketType.Play.Server.BLOCK_CHANGE) {
            WrapperPlayServerBlockChange wrapper = new WrapperPlayServerBlockChange(event);
            Vector3i pos = wrapper.getBlockPosition();
            
            if (pos == null || !(event.getPlayer() instanceof Player viewer)) {
                return;
            }
            
            NativeDisguiseData data = NativeDisguiseEngine.getSolidifiedDisguiseAt(pos.getX(), pos.getY(), pos.getZ(), viewer.getWorld().getName());
            
            if (data != null) {
                // Determine which fake block this viewer should see
                Material mat = viewer.getUniqueId().equals(data.getPlayerUUID()) ? Material.MOVING_PISTON : data.getBlockMaterial();
                wrapper.setBlockID(SpigotConversionUtil.fromBukkitBlockData(mat.createBlockData()).getGlobalId());
            }
        } 
        
        // Intercept MULTI_BLOCK_CHANGE which Paper 1.21.1 uses frequently
        else if (event.getPacketType() == PacketType.Play.Server.MULTI_BLOCK_CHANGE) {
            com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerMultiBlockChange wrapper = 
                    new com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerMultiBlockChange(event);
            
            if (!(event.getPlayer() instanceof Player viewer)) return;
            
            boolean changed = false;
            com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerMultiBlockChange.EncodedBlock[] blocks = wrapper.getBlocks();
            
            for (int i = 0; i < blocks.length; i++) {
                com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerMultiBlockChange.EncodedBlock block = blocks[i];
                int x = wrapper.getChunkPosition().getX() * 16 + block.getX();
                int y = wrapper.getChunkPosition().getY() * 16 + block.getY();
                int z = wrapper.getChunkPosition().getZ() * 16 + block.getZ();
                
                NativeDisguiseData data = NativeDisguiseEngine.getSolidifiedDisguiseAt(x, y, z, viewer.getWorld().getName());
                if (data != null) {
                    Material mat = viewer.getUniqueId().equals(data.getPlayerUUID()) ? Material.MOVING_PISTON : data.getBlockMaterial();
                    blocks[i] = new com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerMultiBlockChange.EncodedBlock(
                            block.getX(), block.getY(), block.getZ(),
                            SpigotConversionUtil.fromBukkitBlockData(mat.createBlockData()).getGlobalId()
                    );
                    changed = true;
                }
            }
            
            if (changed) {
                wrapper.setBlocks(blocks);
            }
        }
        
        // Rewrite player spawn packet to a living mob for CREATURE disguises.
        // This is Lane 1: Packet ID Injection. We keep the real player's entity ID.
        else if (event.getPacketType() == PacketType.Play.Server.SPAWN_PLAYER) {
            WrapperPlayServerSpawnPlayer wrapper = new WrapperPlayServerSpawnPlayer(event);
            Player p = Bukkit.getPlayer(wrapper.getUUID());
            Player viewer = (Player) event.getPlayer();

            log.fine("[Disguise-PktDbg] SPAWN_PLAYER intercepted: eid=" + wrapper.getEntityId()
                    + " uuid=" + wrapper.getUUID() + " viewer=" + (viewer != null ? viewer.getName() : "null")
                    + " isDisguised=" + (p != null && NativeDisguiseEngine.isDisguised(p)));

            if (p != null && !p.equals(viewer) && NativeDisguiseEngine.isDisguised(p)) {
                NativeDisguiseData data = NativeDisguiseEngine.getByPlayer(p);
                if (data.getType() == com.houzicore.shared.core.disguise.v2.engine.NativeDisguiseType.CREATURE) {
                    event.setCancelled(true);

                    log.fine("[Disguise-PktDbg] SPAWN_PLAYER -> rewrite to " + data.getCreatureType().name()
                            + " eid=" + wrapper.getEntityId() + " for viewer=" + viewer.getName());

                    List<EntityData<?>> metadata = new ArrayList<>();
                    metadata.add(new EntityData<>(0, EntityDataTypes.BYTE, (byte) 0));
                    if (data.getCreatureType() != null && isAgeableOrZombie(data.getCreatureType())) {
                        int babyIndex = getBabyIndex(data.getCreatureType());
                        metadata.add(new EntityData<>(babyIndex, EntityDataTypes.BOOLEAN, false));
                        if (data.getCreatureType().name().contains("PIGLIN")) {
                            metadata.add(new EntityData<>(babyIndex + 1, EntityDataTypes.BOOLEAN, false));
                        }
                    }
                    WrapperPlayServerSpawnLivingEntity mobSpawn = new WrapperPlayServerSpawnLivingEntity(
                            wrapper.getEntityId(),
                            wrapper.getUUID(),
                            SpigotConversionUtil.fromBukkitEntityType(data.getCreatureType()),
                            wrapper.getPosition(),
                            wrapper.getYaw(),
                            wrapper.getPitch(),
                            wrapper.getYaw(),
                            new com.github.retrooper.packetevents.util.Vector3d(0, 0, 0),
                            metadata
                    );

                    com.github.retrooper.packetevents.PacketEvents.getAPI()
                            .getPlayerManager()
                            .sendPacket(viewer, mobSpawn);
                }
            }
        }
        
        // Keep the generic SPAWN_ENTITY fallback for protocol paths that do not use SPAWN_PLAYER.
        else if (event.getPacketType() == PacketType.Play.Server.SPAWN_ENTITY) {
            WrapperPlayServerSpawnEntity wrapper = new WrapperPlayServerSpawnEntity(event);

            // Debug: log ALL SPAWN_ENTITY for PLAYER type to diagnose viewer visibility
            if (wrapper.getEntityType() == com.github.retrooper.packetevents.protocol.entity.type.EntityTypes.PLAYER) {
                Player viewer = (Player) event.getPlayer();
                log.fine("[Disguise-PktDbg] SPAWN_ENTITY(PLAYER) intercepted: eid=" + wrapper.getEntityId()
                        + " uuid=" + wrapper.getUUID().orElse(null)
                        + " viewer=" + (viewer != null ? viewer.getName() : "null"));
            }

            if (wrapper.getEntityType() == com.github.retrooper.packetevents.protocol.entity.type.EntityTypes.PLAYER) {
                if (wrapper.getUUID().isPresent()) {
                    Player p = Bukkit.getPlayer(wrapper.getUUID().get());
                    Player viewer = (Player) event.getPlayer();
                    // Don't rewrite for the disguised player themselves (self-view is handled separately)
                    if (p != null && !p.equals(viewer) && NativeDisguiseEngine.isDisguised(p)) {
                        NativeDisguiseData data = NativeDisguiseEngine.getByPlayer(p);
                        if (data.getType() == com.houzicore.shared.core.disguise.v2.engine.NativeDisguiseType.CREATURE) {
                            
                            // Cancel the original player spawn packet
                            event.setCancelled(true);

                            log.fine("[Disguise-PktDbg] SPAWN_ENTITY(PLAYER) -> rewrite to " + data.getCreatureType().name()
                                    + " eid=" + p.getEntityId() + " for viewer=" + viewer.getName());
                            
                            // Build new spawn packet with mob entity type using the SAME Entity ID
                            com.github.retrooper.packetevents.protocol.world.Location peLoc = 
                                new com.github.retrooper.packetevents.protocol.world.Location(
                                    new com.github.retrooper.packetevents.util.Vector3d(p.getLocation().getX(), p.getLocation().getY(), p.getLocation().getZ()),
                                    p.getLocation().getYaw(), p.getLocation().getPitch()
                                );
                            
                            WrapperPlayServerSpawnEntity mobSpawn = new WrapperPlayServerSpawnEntity(
                                    p.getEntityId(), // SAME ID!
                                    wrapper.getUUID().get(), // Original UUID!
                                    io.github.retrooper.packetevents.util.SpigotConversionUtil.fromBukkitEntityType(data.getCreatureType()),
                                    peLoc,
                                    p.getLocation().getYaw(),
                                    0, // data
                                    new com.github.retrooper.packetevents.util.Vector3d(0, 0, 0)
                            );
                            
                            // Send the modified spawn packet
                            var pm = com.github.retrooper.packetevents.PacketEvents.getAPI().getPlayerManager();
                            pm.sendPacket(viewer, mobSpawn);
                        }
                    }
                }
            }
        }
        
        // ═══════════════════════════════════════════════════════════════
        //  BUNDLE packet unpacking
        //  Paper 1.21.1 may wrap spawn packets inside BUNDLE packets.
        //  We unpack, cancel the BUNDLE packet, and send individual sub-packets
        //  so they trigger our SPAWN_PLAYER / SPAWN_ENTITY / METADATA handlers.
        // ═══════════════════════════════════════════════════════════════
//        else if (event.getPacketType() == PacketType.Play.Server.BUNDLE) {
//            log.info("[Disguise-Debug] BUNDLE event class: " + event.getClass().getName());
//            for (java.lang.reflect.Field f : event.getClass().getDeclaredFields()) {
//                f.setAccessible(true);
//                try {
//                    log.info("[Disguise-Debug]   Field " + f.getName() + " = " + f.get(event));
//                } catch (Exception e) {}
//            }
//            Class<?> parent = event.getClass().getSuperclass();
//            while (parent != null) {
//                log.info("[Disguise-Debug] Superclass: " + parent.getName());
//                for (java.lang.reflect.Field f : parent.getDeclaredFields()) {
//                    f.setAccessible(true);
//                    try {
//                        log.info("[Disguise-Debug]     Field " + f.getName() + " = " + f.get(event));
//                    } catch (Exception e) {}
//                }
//                parent = parent.getSuperclass();
//            }
//        }
        
        // Cancel INVISIBILITY ENTITY_EFFECT for disguised players to keep creature/block disguises visible.
        else if (event.getPacketType() == PacketType.Play.Server.ENTITY_EFFECT) {
            WrapperPlayServerEntityEffect wrapper = new WrapperPlayServerEntityEffect(event);
            Player p = NativeDisguiseEngine.getRealPlayerByEntityId(wrapper.getEntityId());
            if (p != null && NativeDisguiseEngine.isDisguised(p)) {
                Player viewer = (Player) event.getPlayer();
                if (!p.equals(viewer)) {
                    if (wrapper.getPotionType() == PotionTypes.INVISIBILITY) {
                        event.setCancelled(true);
                    }
                }
            }
        }
        
        // Cancel INVISIBILITY REMOVE_ENTITY_EFFECT for disguised players.
        else if (event.getPacketType() == PacketType.Play.Server.REMOVE_ENTITY_EFFECT) {
            WrapperPlayServerRemoveEntityEffect wrapper = new WrapperPlayServerRemoveEntityEffect(event);
            Player p = NativeDisguiseEngine.getRealPlayerByEntityId(wrapper.getEntityId());
            if (p != null && NativeDisguiseEngine.isDisguised(p)) {
                Player viewer = (Player) event.getPlayer();
                if (!p.equals(viewer)) {
                    if (wrapper.getPotionType() == PotionTypes.INVISIBILITY) {
                        event.setCancelled(true);
                    }
                }
            }
        }

        // Filter ENTITY_METADATA for CREATURE disguises
        // Strip out the INVISIBILITY flag so the mob is visible to viewers, even though the player has the potion.
        else if (event.getPacketType() == PacketType.Play.Server.ENTITY_METADATA) {
            com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerEntityMetadata wrapper = new com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerEntityMetadata(event);
            Player p = NativeDisguiseEngine.getRealPlayerByEntityId(wrapper.getEntityId());
            if (p != null && NativeDisguiseEngine.isDisguised(p)) {
                NativeDisguiseData data = NativeDisguiseEngine.getByPlayer(p);
                if (data.getType() == com.houzicore.shared.core.disguise.v2.engine.NativeDisguiseType.CREATURE) {
                    Player viewer = (Player) event.getPlayer();
                    if (!p.equals(viewer)) {
                        boolean isNewVersion = com.github.retrooper.packetevents.PacketEvents.getAPI()
                                .getServerManager().getVersion()
                                .isNewerThanOrEquals(com.github.retrooper.packetevents.manager.server.ServerVersion.V_1_20_5);
                        int colorIdx = isNewVersion ? 11 : 10;
                        int ambientIdx = isNewVersion ? 12 : 11;
                        int hideParticlesIdx = isNewVersion ? 13 : 12;

                        List<EntityData<?>> filtered = new ArrayList<>();
                        for (EntityData<?> meta : wrapper.getEntityMetadata()) {
                            if (meta.getIndex() <= 14) {
                                if (meta.getIndex() == 0 && meta.getValue() instanceof Byte b) {
                                    byte cleaned = (byte) (b & ~0x20); // Remove INVISIBILITY (0x20)
                                    filtered.add(new EntityData<>(0, com.github.retrooper.packetevents.protocol.entity.data.EntityDataTypes.BYTE, cleaned));
                                } else if (meta.getIndex() == colorIdx && meta.getValue() instanceof Integer) {
                                    // Clear potion color to remove particle trails
                                    filtered.add(new EntityData(colorIdx, meta.getType(), 0));
                                } else if (meta.getIndex() == ambientIdx && meta.getValue() instanceof Boolean) {
                                    // Set ambient to false
                                    filtered.add(new EntityData(ambientIdx, meta.getType(), false));
                                } else if (meta.getIndex() == hideParticlesIdx && meta.getValue() instanceof Boolean) {
                                    // Set hide particles to true
                                    filtered.add(new EntityData(hideParticlesIdx, meta.getType(), true));
                                } else {
                                    filtered.add(meta);
                                }
                            }
                        }
                        if (data.getCreatureType() != null && isAgeableOrZombie(data.getCreatureType())) {
                            int babyIndex = getBabyIndex(data.getCreatureType());
                            filtered.add(new EntityData<>(babyIndex, com.github.retrooper.packetevents.protocol.entity.data.EntityDataTypes.BOOLEAN, false));
                            if (data.getCreatureType().name().contains("PIGLIN")) {
                                filtered.add(new EntityData<>(babyIndex + 1, com.github.retrooper.packetevents.protocol.entity.data.EntityDataTypes.BOOLEAN, false));
                            }
                        }
                        if (filtered.isEmpty()) {
                            event.setCancelled(true);
                        } else {
                            wrapper.setEntityMetadata(filtered);
                        }
                    }
                }
            }
        }

        // Prevent player armor/held items from being applied to the mob disguise.
        else if (event.getPacketType() == PacketType.Play.Server.ENTITY_EQUIPMENT) {
            com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerEntityEquipment wrapper =
                    new com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerEntityEquipment(event);
            Player p = NativeDisguiseEngine.getRealPlayerByEntityId(wrapper.getEntityId());
            if (p != null && NativeDisguiseEngine.isDisguised(p)) {
                NativeDisguiseData data = NativeDisguiseEngine.getByPlayer(p);
                Player viewer = (Player) event.getPlayer();
                if (!p.equals(viewer)) {
                    event.setCancelled(true);
                }
            }
        }
        
        // Prevent hideEntity() from removing the player from the Tab List
        else if (event.getPacketType() == PacketType.Play.Server.PLAYER_INFO_REMOVE) {
            WrapperPlayServerPlayerInfoRemove wrapper = new WrapperPlayServerPlayerInfoRemove(event);
            List<UUID> toRemove = new ArrayList<>(wrapper.getProfileIds());
            boolean changed = false;

            for (int i = 0; i < toRemove.size(); i++) {
                Player p = Bukkit.getPlayer(toRemove.get(i));
                if (p != null && p.isOnline() && NativeDisguiseEngine.isDisguised(p)) {
                    toRemove.remove(i);
                    i--;
                    changed = true;
                }
            }

            if (changed) {
                if (toRemove.isEmpty()) {
                    event.setCancelled(true);
                } else {
                    wrapper.setProfileIds(toRemove);
                }
            }
        } 
        
        // Also handle 1.19.3+ UPDATE_LISTED mechanic
        else if (event.getPacketType() == PacketType.Play.Server.PLAYER_INFO_UPDATE) {
            WrapperPlayServerPlayerInfoUpdate wrapper = new WrapperPlayServerPlayerInfoUpdate(event);
            if (wrapper.getActions().contains(WrapperPlayServerPlayerInfoUpdate.Action.UPDATE_LISTED)) {
                boolean changed = false;
                List<WrapperPlayServerPlayerInfoUpdate.PlayerInfo> entries = new ArrayList<>(wrapper.getEntries());
                
                for (WrapperPlayServerPlayerInfoUpdate.PlayerInfo info : entries) {
                    Player p = Bukkit.getPlayer(info.getProfileId());
                    if (p != null && p.isOnline() && NativeDisguiseEngine.isDisguised(p)) {
                        if (!info.isListed()) {
                            info.setListed(true); // Force them to stay listed in Tab
                            changed = true;
                        }
                    }
                }
                
                if (changed) {
                    wrapper.setEntries(entries);
                }
            }
        }

        // ═══════════════════════════════════════════════════════════════
        //  ANTI-MOMENTUM-SLIDING: Rewrite relative move → teleport
        //  
        //  The client interpolates REL_ENTITY_MOVE over 3 ticks causing
        //  the mob to "slide" with momentum instead of snapping to the
        //  player's position. By cancelling the relative move and sending
        //  ENTITY_TELEPORT instead, the viewer client snaps the mob
        //  directly to the correct position each tick.
        // ═══════════════════════════════════════════════════════════════

        else if (event.getPacketType() == PacketType.Play.Server.ENTITY_RELATIVE_MOVE
              || event.getPacketType() == PacketType.Play.Server.ENTITY_RELATIVE_MOVE_AND_ROTATION) {

            // Extract entity ID from whichever wrapper applies
            int entityId;
            if (event.getPacketType() == PacketType.Play.Server.ENTITY_RELATIVE_MOVE) {
                entityId = new com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerEntityRelativeMove(event).getEntityId();
            } else {
                entityId = new com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerEntityRelativeMoveAndRotation(event).getEntityId();
            }

            Player disguised = NativeDisguiseEngine.getRealPlayerByEntityId(entityId);
            if (disguised != null && NativeDisguiseEngine.isDisguised(disguised)) {
                NativeDisguiseData data = NativeDisguiseEngine.getByPlayer(disguised);
                if (data != null && data.getType() == com.houzicore.shared.core.disguise.v2.engine.NativeDisguiseType.CREATURE) {
                    Player viewer = (Player) event.getPlayer();
                    if (!viewer.equals(disguised)) {
                        // Cancel the interpolated relative move
                        event.setCancelled(true);

                        var pm = com.github.retrooper.packetevents.PacketEvents.getAPI().getPlayerManager();

                        // Send ENTITY_TELEPORT — client snaps to exact position, no interpolation
                        var teleport = new com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerEntityTeleport(
                                entityId,
                                new com.github.retrooper.packetevents.util.Vector3d(
                                        disguised.getLocation().getX(),
                                        disguised.getLocation().getY(),
                                        disguised.getLocation().getZ()
                                ),
                                disguised.getLocation().getYaw(),
                                disguised.getLocation().getPitch(),
                                disguised.isOnGround()
                        );
                        pm.sendPacket(viewer, teleport);

                        // Also sync head rotation so the mob faces the right direction
                        var headLook = new com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerEntityHeadLook(
                                entityId,
                                disguised.getLocation().getYaw()
                        );
                        pm.sendPacket(viewer, headLook);
                    }
                }
            }
        }

        // Also intercept standalone ENTITY_ROTATION for creature disguises
        // to ensure mob head tracks player facing correctly
        else if (event.getPacketType() == PacketType.Play.Server.ENTITY_ROTATION) {
            var wrapper = new com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerEntityRotation(event);
            int entityId = wrapper.getEntityId();
            Player disguised = NativeDisguiseEngine.getRealPlayerByEntityId(entityId);
            if (disguised != null && NativeDisguiseEngine.isDisguised(disguised)) {
                NativeDisguiseData data = NativeDisguiseEngine.getByPlayer(disguised);
                if (data != null && data.getType() == com.houzicore.shared.core.disguise.v2.engine.NativeDisguiseType.CREATURE) {
                    Player viewer = (Player) event.getPlayer();
                    if (!viewer.equals(disguised)) {
                        // Send head look to keep mob head synced with player yaw
                        var pm = com.github.retrooper.packetevents.PacketEvents.getAPI().getPlayerManager();
                        var headLook = new com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerEntityHeadLook(
                                entityId,
                                disguised.getLocation().getYaw()
                        );
                        pm.sendPacket(viewer, headLook);
                    }
                }
            }
        }
    }

    private Object getFieldValue(Object obj, String fieldName) {
        Class<?> clazz = obj.getClass();
        while (clazz != null) {
            try {
                java.lang.reflect.Field field = clazz.getDeclaredField(fieldName);
                field.setAccessible(true);
                return field.get(obj);
            } catch (NoSuchFieldException e) {
                clazz = clazz.getSuperclass();
            } catch (Exception e) {
                break;
            }
        }
        try {
            java.lang.reflect.Field field = obj.getClass().getField(fieldName);
            field.setAccessible(true);
            return field.get(obj);
        } catch (Exception ignored) {}
        return null;
    }

    private Iterable<?> getBundleSubPackets(Object nmsPacket) {
        try {
            java.lang.reflect.Method method = nmsPacket.getClass().getMethod("subPackets");
            method.setAccessible(true);
            return (Iterable<?>) method.invoke(nmsPacket);
        } catch (Exception e) {
            try {
                java.lang.reflect.Method getPacketsMethod = nmsPacket.getClass().getMethod("getPackets");
                getPacketsMethod.setAccessible(true);
                return (Iterable<?>) getPacketsMethod.invoke(nmsPacket);
            } catch (Exception ex) {
                for (java.lang.reflect.Field field : nmsPacket.getClass().getDeclaredFields()) {
                    if (Iterable.class.isAssignableFrom(field.getType())) {
                        field.setAccessible(true);
                        try {
                            return (Iterable<?>) field.get(nmsPacket);
                        } catch (Exception ignored) {}
                    }
                }
            }
        }
        return null;
    }

    private int getEntityIdFromNMSPacket(Object nmsPacket) {
        if (nmsPacket == null) return -1;
        String[] methodNames = {"getId", "id", "getEntityId", "entityId"};
        for (String mName : methodNames) {
            try {
                java.lang.reflect.Method method = nmsPacket.getClass().getMethod(mName);
                method.setAccessible(true);
                Object val = method.invoke(nmsPacket);
                if (val instanceof Integer) {
                    return (Integer) val;
                }
            } catch (Exception ignored) {}
        }
        String[] fieldNames = {"id", "entityId"};
        for (String fName : fieldNames) {
            Object val = getFieldValue(nmsPacket, fName);
            if (val instanceof Integer) {
                return (Integer) val;
            }
        }
        return -1;
    }

    private void sendNMSPacket(Player player, Object nmsPacket) {
        try {
            Class<?> craftPlayerClass = Class.forName("org.bukkit.craftbukkit.entity.CraftPlayer");
            Object craftPlayer = craftPlayerClass.cast(player);
            java.lang.reflect.Method getHandleMethod = craftPlayerClass.getMethod("getHandle");
            Object entityPlayer = getHandleMethod.invoke(craftPlayer);
            
            Object connection = getFieldValue(entityPlayer, "connection");
            if (connection == null) {
                log.warning("[Disguise] connection field is null on player handle.");
                return;
            }
            
            java.lang.reflect.Method sendMethod = null;
            for (java.lang.reflect.Method method : connection.getClass().getMethods()) {
                if (method.getName().equals("send") && method.getParameterCount() == 1) {
                    sendMethod = method;
                    break;
                }
            }
            if (sendMethod == null) {
                for (java.lang.reflect.Method method : connection.getClass().getDeclaredMethods()) {
                    if (method.getName().equals("send") && method.getParameterCount() == 1) {
                        sendMethod = method;
                        break;
                    }
                }
            }
            
            if (sendMethod != null) {
                sendMethod.setAccessible(true);
                sendMethod.invoke(connection, nmsPacket);
            } else {
                log.warning("[Disguise] Could not find 'send' method on player connection via reflection.");
            }
        } catch (Exception e) {
            log.warning("[Disguise] Failed to send NMS packet via reflection: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private boolean isAgeableOrZombie(org.bukkit.entity.EntityType type) {
        if (type == null) return false;
        String name = type.name();
        return name.contains("ZOMBIE") || name.contains("PIG") || name.contains("COW") || 
               name.contains("SHEEP") || name.contains("CHICKEN") || name.contains("VILLAGER") || 
               name.contains("FOX") || name.contains("WOLF") || name.contains("CAT") || 
               name.contains("OCELOT") || name.contains("RABBIT") || name.contains("DONKEY") || 
               name.contains("HORSE") || name.contains("MULE") || name.contains("LLAMA") || 
               name.contains("GOAT") || name.contains("PANDA") || name.contains("POLAR_BEAR") || 
               name.contains("FROG") || name.contains("CAMEL") || name.contains("SNIFFER") ||
               name.contains("HUSK") || name.contains("DROWNED") || name.contains("HOGLIN") ||
               name.contains("ZOGLIN");
    }

    private int getBabyIndex(org.bukkit.entity.EntityType type) {
        com.github.retrooper.packetevents.manager.server.ServerVersion version = 
            com.github.retrooper.packetevents.PacketEvents.getAPI().getServerManager().getVersion();
        
        String name = type.name();
        if (version.isNewerThanOrEquals(com.github.retrooper.packetevents.manager.server.ServerVersion.V_1_19_4)) {
            if (name.contains("PIGLIN")) {
                return 17;
            }
            return 16;
        } else if (version.isNewerThanOrEquals(com.github.retrooper.packetevents.manager.server.ServerVersion.V_1_14)) {
            if (name.contains("PIGLIN")) {
                return 16;
            }
            return 15;
        } else {
            return 12; // Legacy versions
        }
    }
}
