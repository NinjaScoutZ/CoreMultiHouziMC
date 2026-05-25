package com.houzicore.shared.core.disguise.v2.engine;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.EntityType;

import java.util.UUID;

/**
 * Holds the runtime state of a single active disguise.
 *
 * Mirrors Mineplex 2018's approach where each disguised entity has:
 * - a real entity ID (the player)
 * - a fake entity ID (the client-side visual — BlockDisplay or mob)
 * - a self-view entity ID (what the disguised player sees of themselves)
 * - state tracking for solidify/movement
 */
public class NativeDisguiseData {

    // ─── Identity ───────────────────────────────────────────────
    private final UUID playerUUID;
    private final int realEntityId;
    private final int fakeEntityId;
    private final int selfViewEntityId;
    private final UUID fakeEntityUUID;
    private final UUID selfViewEntityUUID;
    private UUID displayEntityUUID;
    private UUID interactionEntityUUID;
    private int displayEntityId = -1;
    private int interactionEntityId = -1;
    
    private org.bukkit.entity.Entity realSelfViewEntity = null;

    // ─── Disguise Config ────────────────────────────────────────
    private final NativeDisguiseType type;
    private Material blockMaterial;    // for BLOCK type
    private EntityType creatureType;   // for CREATURE type
    private float blockWidth = 1.0f;
    private float blockHeight = 1.0f;

    // ─── Block Form State ───────────────────────────────────────
    private boolean solidified = false;
    private Location solidLocation = null;
    private Location lastLocation = null;
    private int stillTicks = 0;          // ticks without moving
    private static final int SOLIDIFY_THRESHOLD = 40; // 2 seconds

    // ─── Lifecycle ──────────────────────────────────────────────
    private boolean active = true;

    public NativeDisguiseData(UUID playerUUID, int realEntityId, int fakeEntityId,
                        int selfViewEntityId, NativeDisguiseType type) {
        this.playerUUID = playerUUID;
        this.realEntityId = realEntityId;
        this.fakeEntityId = fakeEntityId;
        this.selfViewEntityId = selfViewEntityId;
        this.fakeEntityUUID = UUID.randomUUID();
        this.selfViewEntityUUID = UUID.randomUUID();
        this.type = type;
    }

    // ─── Getters ────────────────────────────────────────────────

    public UUID getPlayerUUID() { return playerUUID; }
    public int getRealEntityId() { return realEntityId; }
    public int getFakeEntityId() { return fakeEntityId; }
    public int getSelfViewEntityId() { return selfViewEntityId; }
    public UUID getFakeEntityUUID() { return fakeEntityUUID; }
    public UUID getSelfViewEntityUUID() { return selfViewEntityUUID; }
    public UUID getDisplayEntityUUID() { return displayEntityUUID; }
    public UUID getInteractionEntityUUID() { return interactionEntityUUID; }
    public int getDisplayEntityId() { return displayEntityId; }
    public int getInteractionEntityId() { return interactionEntityId; }
    public NativeDisguiseType getType() { return type; }

    public org.bukkit.entity.Entity getRealSelfViewEntity() { return realSelfViewEntity; }
    public void setRealSelfViewEntity(org.bukkit.entity.Entity entity) { this.realSelfViewEntity = entity; }

    public void setDisplayEntity(UUID displayEntityUUID, int displayEntityId) {
        this.displayEntityUUID = displayEntityUUID;
        this.displayEntityId = displayEntityId;
    }

    public void setInteractionEntity(UUID interactionEntityUUID, int interactionEntityId) {
        this.interactionEntityUUID = interactionEntityUUID;
        this.interactionEntityId = interactionEntityId;
    }

    public Material getBlockMaterial() { return blockMaterial; }
    public void setBlockMaterial(Material blockMaterial) {
        this.blockMaterial = blockMaterial;
        this.calculateBlockDimensions();
    }

    public float getBlockWidth() { return blockWidth; }
    public float getBlockHeight() { return blockHeight; }

    private void calculateBlockDimensions() {
        if (blockMaterial == null) {
            this.blockWidth = 1.0f;
            this.blockHeight = 1.0f;
            return;
        }
        String name = blockMaterial.name();
        if (name.contains("CARPET")) {
            this.blockHeight = 0.0625f;
            this.blockWidth = 1.0f;
        } else if (name.contains("SLAB")) {
            this.blockHeight = 0.5f;
            this.blockWidth = 1.0f;
        } else if (name.contains("PLATE")) {
            this.blockHeight = 0.05f;
            this.blockWidth = 1.0f;
        } else if (blockMaterial == Material.FLOWER_POT) {
            this.blockHeight = 0.375f;
            this.blockWidth = 0.375f;
        } else if (blockMaterial == Material.CAKE) {
            this.blockHeight = 0.5f;
            this.blockWidth = 0.875f;
        } else if (name.contains("SKULL") || name.contains("HEAD")) {
            this.blockHeight = 0.5f;
            this.blockWidth = 0.5f;
        } else {
            this.blockHeight = 1.0f;
            this.blockWidth = 1.0f;
        }
    }

    public EntityType getCreatureType() { return creatureType; }
    public void setCreatureType(EntityType creatureType) { this.creatureType = creatureType; }

    // ─── Solidify State ─────────────────────────────────────────

    public boolean isSolidified() { return solidified; }

    public void setSolidified(boolean solidified, Location location) {
        this.solidified = solidified;
        this.solidLocation = solidified ? location : null;
    }

    public Location getSolidLocation() { return solidLocation; }

    // ─── Grace Hit State ────────────────────────────────────────

    private Location graceHitLocation = null;
    private long graceHitExpiry = 0;

    public Location getGraceHitLocation() { return graceHitLocation; }
    public long getGraceHitExpiry() { return graceHitExpiry; }

    public void setGraceHit(Location location, long expiryTimeMs) {
        this.graceHitLocation = location;
        this.graceHitExpiry = expiryTimeMs;
    }

    // ─── Solid Break Immunity ───────────────────────────────────

    private long solidBreakImmunityUntil = 0;

    public long getSolidBreakImmunityUntil() { return solidBreakImmunityUntil; }
    public void setSolidBreakImmunityUntil(long time) { this.solidBreakImmunityUntil = time; }

    // ─── Movement Tracking ──────────────────────────────────────

    public Location getLastLocation() { return lastLocation; }
    public void setLastLocation(Location lastLocation) { this.lastLocation = lastLocation; }

    public int getStillTicks() { return stillTicks; }
    public void incrementStillTicks() { this.stillTicks++; }
    public void resetStillTicks() { this.stillTicks = 0; }

    public boolean shouldAutoSolidify() {
        return type == NativeDisguiseType.BLOCK && !solidified && stillTicks >= SOLIDIFY_THRESHOLD;
    }

    // ─── Self-View Position Tracking ────────────────────────────

    private Location selfViewLastLoc;
    public Location getSelfViewLastLoc() { return selfViewLastLoc; }
    public void setSelfViewLastLoc(Location loc) { this.selfViewLastLoc = loc; }

    // ─── Original Attributes ────────────────────────────────────

    private double originalScale = 1.0;
    private double originalMaxHealth = 20.0;
    private double originalStepHeight = 0.6;

    public double getOriginalScale() { return originalScale; }
    public void setOriginalScale(double originalScale) { this.originalScale = originalScale; }

    public double getOriginalMaxHealth() { return originalMaxHealth; }
    public void setOriginalMaxHealth(double originalMaxHealth) { this.originalMaxHealth = originalMaxHealth; }

    public double getOriginalStepHeight() { return originalStepHeight; }
    public void setOriginalStepHeight(double originalStepHeight) { this.originalStepHeight = originalStepHeight; }

    // ─── Lifecycle ──────────────────────────────────────────────

    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }
}
