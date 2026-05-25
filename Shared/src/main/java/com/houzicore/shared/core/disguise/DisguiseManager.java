package com.houzicore.shared.core.disguise;

import java.util.List;

import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import com.houzicore.shared.MiniPlugin;
import com.houzicore.shared.api.disguise.DisguiseArchetype;
import com.houzicore.shared.api.disguise.DisguiseRequest;
import com.houzicore.shared.api.disguise.DisguiseService;
import com.houzicore.shared.core.disguise.disguises.DisguiseBase;
import com.houzicore.shared.core.disguise.disguises.DisguisePlayer;

import com.houzicore.shared.core.disguise.v2.PacketEventsDisguiseBackendAdapter;
import com.houzicore.shared.core.disguise.v2.RoutingDisguiseService;
import com.houzicore.shared.core.disguise.v2.engine.NativeDisguiseEngine;
import com.houzicore.shared.core.disguise.v2.packet.NativeDisguisePacketListener;
import com.houzicore.shared.core.packethandler.IPacketHandler;
import com.houzicore.shared.core.packethandler.PacketHandler;
import com.houzicore.shared.core.packethandler.PacketInfo;

/**
 * DisguiseManager — bridges legacy callers to the unified DisguiseService.
 * <p>
 * All write-paths route through {@link DisguiseService}, which delegates to
 * the native {@link NativeDisguiseEngine} via {@link PacketEventsDisguiseBackendAdapter}.
 */
public class DisguiseManager extends MiniPlugin implements IPacketHandler {

    /** Native disguise engine. */
    private final NativeDisguiseEngine _engine;

    /** Unified service — all disguise write-paths route through here. */
    private final DisguiseService disguiseService;

    public DisguiseManager(JavaPlugin plugin, PacketHandler packetHandler) {
        super("Disguise Manager", plugin);

        _engine = new NativeDisguiseEngine(plugin);
        
        // Register NativeDisguisePacketListener
        com.github.retrooper.packetevents.PacketEvents.getAPI().getEventManager().registerListener(
                new NativeDisguisePacketListener(_engine, plugin)
        );

        this.disguiseService = new RoutingDisguiseService(List.of(
                new PacketEventsDisguiseBackendAdapter(_engine)
        ));
    }

    /** Expose the service so future modules can bypass this legacy manager directly. */
    public DisguiseService getService() {
        return disguiseService;
    }

    /** Expose the native engine so Arcade can manipulate solidify states directly. */
    public NativeDisguiseEngine getEngine() {
        return _engine;
    }

    // -------------------------------------------------------------------------
    // Internal routing — archetype derived from disguise type, single path
    // -------------------------------------------------------------------------

    private void applyDisguise(DisguiseBase disguise) {
        if (disguise.getEntity() == null) return;

        // Derive the correct archetype from the runtime type.
        DisguiseArchetype archetype =
                (disguise instanceof DisguisePlayer) ? DisguiseArchetype.PLAYER : DisguiseArchetype.MOB;

        String name = disguise.getClass().getSimpleName().replace("Disguise", "").toUpperCase();

        // Legacy mob name normalisations (preserved for MOB requests)
        if (archetype == DisguiseArchetype.MOB) {
            name = normaliseLegacyMobName(name);
        }

        DisguiseRequest request = new DisguiseRequest(
                disguise.getEntity().getUniqueId(),
                archetype,
                name,
                true, false, false,
                disguise.getName(), disguise.isCustomNameVisible());

        disguiseService.apply((LivingEntity) disguise.getEntity(), request);
    }

    private static String normaliseLegacyMobName(String name) {
        switch (name) {
            case "PIGZOMBIE":      return "ZOMBIFIED_PIGLIN";
            case "ZOMBIEVILLAGER": return "ZOMBIE_VILLAGER";
            case "MAGMACUBE":      return "MAGMA_CUBE";
            case "IRONGOLEM":      return "IRON_GOLEM";
            case "SNOWMAN":        return "SNOW_GOLEM";
            case "MUSHROOMCOW":    return "MUSHROOM_COW";
            case "OCELOT":         return "CAT";
            default:               return name;
        }
    }

    // -------------------------------------------------------------------------
    // Public API — forwarded from the original DisguiseManager surface
    // -------------------------------------------------------------------------

    public void disguise(DisguiseBase disguise, boolean refreshTrackers, Player... players) {
        applyDisguise(disguise);
    }

    public void disguise(DisguiseBase disguise, Player... players) {
        applyDisguise(disguise);
    }

    public void addViewerToDisguise(DisguiseBase disguise, Player player, boolean reapply) {}
    public void clearDisguises() {
        _engine.undisguiseAll();
    }

    public DisguiseBase getDisguise(LivingEntity entity) {
        if (entity == null) return null;

        return disguiseService.getActiveSession(entity.getUniqueId())
                .map(session -> toLegacyDisguise(entity, session.request()))
                .orElse(null);
    }

    private DisguiseBase toLegacyDisguise(LivingEntity entity, DisguiseRequest request) {
        EntityType type = resolveEntityType(request);
        return type == null ? null : DisguiseFactory.createDisguise(entity, type);
    }

    private EntityType resolveEntityType(DisguiseRequest request) {
        if (request.archetype() == DisguiseArchetype.PLAYER) {
            return EntityType.PLAYER;
        }

        if (request.archetype() != DisguiseArchetype.MOB && request.archetype() != DisguiseArchetype.NPC) {
            return null;
        }

        String name = normaliseLegacyMobName(request.variantKey().toUpperCase());
        try {
            return EntityType.valueOf(name);
        } catch (IllegalArgumentException ignored) {
            return null;
        }
    }

    /** Checks if the entity has an active disguise via the active service/backend. */
    public boolean isDisguised(LivingEntity entity) {
        return entity != null && disguiseService.getActiveSession(entity.getUniqueId()).isPresent();
    }

    public void addFutureDisguise(DisguiseBase disguise, Player... players) {}

    @Override public void handle(PacketInfo packetInfo) {}

    public void updateDisguise(DisguiseBase disguise) {
        applyDisguise(disguise);
    }

    public void undisguise(Player p) {
        disguiseService.clear(p);
    }
}
