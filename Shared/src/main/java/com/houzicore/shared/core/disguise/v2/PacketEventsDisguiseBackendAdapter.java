package com.houzicore.shared.core.disguise.v2;

import java.util.HashMap;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

import com.houzicore.shared.api.disguise.DisguiseArchetype;
import com.houzicore.shared.api.disguise.DisguiseBackend;
import com.houzicore.shared.api.disguise.DisguiseRequest;
import com.houzicore.shared.core.disguise.v2.engine.NativeDisguiseData;
import com.houzicore.shared.core.disguise.v2.engine.NativeDisguiseEngine;
import org.bukkit.Material;

/**
 * Native packet-based disguise backend adapter.
 * <p>
 * Routes {@link DisguiseRequest} calls to the {@link NativeDisguiseEngine} which
 * handles the actual native teleport and scoreboard logic.
 */
public class PacketEventsDisguiseBackendAdapter implements DisguiseBackendAdapter {

    private final NativeDisguiseEngine _engine;

    public PacketEventsDisguiseBackendAdapter(NativeDisguiseEngine engine) {
        this._engine = engine;
    }

    @Override
    public DisguiseBackend getBackend() {
        return DisguiseBackend.NATIVE;
    }

    @Override
    public boolean isAvailable() {
        return _engine != null;
    }

    @Override
    public boolean supports(DisguiseRequest request) {
        DisguiseArchetype archetype = request.archetype();
        return archetype == DisguiseArchetype.MOB
                || archetype == DisguiseArchetype.NPC
                || archetype == DisguiseArchetype.BLOCK
                || archetype == DisguiseArchetype.PLAYER
                || archetype == DisguiseArchetype.DISPLAY_ONLY;
    }

    @Override
    public void apply(LivingEntity target, DisguiseRequest request) {
        if (!(target instanceof Player player)) {
            return; // NativeDisguiseEngine currently only supports disguising players
        }

        if (request.archetype() == DisguiseArchetype.BLOCK || request.archetype() == DisguiseArchetype.DISPLAY_ONLY) {
            Material mat = resolveMaterial(request);
            _engine.disguiseAsBlock(player, mat);
        } else {
            org.bukkit.entity.EntityType type = resolveEntityType(request);
            _engine.disguiseAsMob(player, type);
        }
    }

    @Override
    public void clear(LivingEntity target) {
        if (target instanceof Player player) {
            _engine.undisguise(player);
        }
    }

    public NativeDisguiseEngine getEngine() {
        return _engine;
    }

    // -----------------------------------------------------------------------
    // Internal helpers
    // -----------------------------------------------------------------------

    private org.bukkit.entity.EntityType resolveEntityType(DisguiseRequest request) {
        if (request.archetype() == DisguiseArchetype.PLAYER) {
            return org.bukkit.entity.EntityType.PLAYER;
        }

        String key = normalizeLegacyMobName(request.variantKey().toUpperCase());
        try {
            return org.bukkit.entity.EntityType.valueOf(key);
        } catch (IllegalArgumentException e) {
            Bukkit.getLogger().warning("[PacketEventsAdapter] Unknown entity type: " + key + ", falling back to ZOMBIE");
            return org.bukkit.entity.EntityType.ZOMBIE;
        }
    }

    private Material resolveMaterial(DisguiseRequest request) {
        String key = request.variantKey().toUpperCase();
        if ((key.equals("BLOCK_DISPLAY") || key.equals("DISPLAY_ONLY")) && request.attributes() != null && request.attributes().containsKey("blockMaterial")) {
            key = request.attributes().get("blockMaterial").toUpperCase();
        }
        try {
            return Material.valueOf(key);
        } catch (IllegalArgumentException e) {
            Bukkit.getLogger().warning("[PacketEventsAdapter] Unknown material type: " + key + ", falling back to STONE");
            return Material.STONE;
        }
    }

    private static String normalizeLegacyMobName(String name) {
        return switch (name) {
            case "PIGZOMBIE", "PIG_ZOMBIE" -> "ZOMBIFIED_PIGLIN";
            case "ZOMBIEVILLAGER" -> "ZOMBIE_VILLAGER";
            case "MAGMACUBE" -> "MAGMA_CUBE";
            case "IRONGOLEM" -> "IRON_GOLEM";
            case "SNOWMAN" -> "SNOW_GOLEM";
            case "MUSHROOMCOW", "MUSHROOM_COW" -> "MOOSHROOM";
            case "OCELOT" -> "CAT";
            case "CAVESPIDER" -> "CAVE_SPIDER";
            case "SILVERFISH" -> "SILVERFISH";
            default -> name;
        };
    }
}
