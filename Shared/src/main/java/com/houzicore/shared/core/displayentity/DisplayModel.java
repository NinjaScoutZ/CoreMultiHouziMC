package com.houzicore.shared.core.displayentity;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Location;
import org.bukkit.entity.Display;
import org.bukkit.entity.Entity;
import org.joml.Vector3f;

/**
 * A "model" composed of one or more {@link DisplayPart} elements.
 * When spawned, each part becomes a real Display entity in the world.
 * Supports animations, entity-following, and bulk lifecycle management.
 */
public class DisplayModel {

    private final String _id;
    private final List<DisplayPart> _parts;
    private final List<Display> _spawnedEntities = new ArrayList<>();
    private final List<Entity> _auxiliaryEntities = new ArrayList<>(); // Shulkers and Interaction entities
    private final List<Vector3f> _baseScales = new ArrayList<>();

    private com.houzicore.shared.core.lifecycle.LifecycleOwner _owner;

    // Hitbox & seat settings
    private final List<HitboxConfig> _hitboxes = new ArrayList<>();
    private final List<SeatConfig> _seats = new ArrayList<>();
    private final List<InteractConfig> _interactions = new ArrayList<>();

    private Location _origin;
    private Entity _followTarget;
    private org.bukkit.util.Vector _followOffset = new org.bukkit.util.Vector(0, 0, 0);
    private ModelAnimation _animation = ModelAnimation.none();
    private long _tickCounter = 0;
    private int _teleportDuration = 3; // Client-side smooth teleport ticks

    /**
     * @param id    Unique identifier (e.g. "hub_trophy", "lobby_chair")
     * @param parts The display elements composing this model
     */
    public DisplayModel(String id, List<DisplayPart> parts) {
        _id = id;
        _parts = new ArrayList<>(parts);
    }

    /**
     * Create a model from a single part (convenience).
     */
    public DisplayModel(String id, DisplayPart singlePart) {
        _id = id;
        _parts = new ArrayList<>();
        _parts.add(singlePart);
    }

    // ── Hitboxes & Seating ───────────────────────────────

    /**
     * Adds a physical solid hitbox using an invisible Shulker.
     */
    public DisplayModel addSolidHitbox(double offsetX, double offsetY, double offsetZ, double scale) {
        _hitboxes.add(new HitboxConfig(offsetX, offsetY, offsetZ, scale));
        return this;
    }

    /**
     * Adds an interactive seat zone.
     * When players right-click this area, they will sit down.
     */
    public DisplayModel addSeat(double offsetX, double offsetY, double offsetZ, float width, float height) {
        _seats.add(new SeatConfig(offsetX, offsetY, offsetZ, width, height));
        return this;
    }

    /**
     * Adds a clickable interaction zone (no seating — just detects right-click).
     * Tagged with "bde_interact" in PersistentDataContainer.
     */
    public DisplayModel addInteractionBox(double offsetX, double offsetY, double offsetZ, float width, float height) {
        _interactions.add(new InteractConfig(offsetX, offsetY, offsetZ, width, height));
        return this;
    }

    public List<HitboxConfig> getHitboxes() { return _hitboxes; }
    public List<SeatConfig> getSeats() { return _seats; }
    public List<InteractConfig> getInteractions() { return _interactions; }

    // ── Spawn / Remove ───────────────────────────────

    /**
     * Spawn all display entities at the given world location.
     * Each part's translation acts as an offset from this origin.
     */
    public void spawn(Location origin) {
        boolean isRespawn = isSpawned();
        if (isRespawn) {
            remove();
        }

        _origin = origin.clone();
        _spawnedEntities.clear();
        _auxiliaryEntities.clear();
        _baseScales.clear();
        if (!isRespawn) {
            _tickCounter = 0;
        }

        // Spawn visual parts
        for (DisplayPart part : _parts) {
            Display entity = part.spawn(_origin);
            entity.setTeleportDuration(_teleportDuration);
            _spawnedEntities.add(entity);
            _baseScales.add(part.getScale());
        }

        // Spawn solid hitboxes (Shulkers)
        for (HitboxConfig hb : _hitboxes) {
            Location loc = _origin.clone().add(hb.offsetX, hb.offsetY, hb.offsetZ);
            org.bukkit.entity.Shulker shulker = (org.bukkit.entity.Shulker) loc.getWorld().spawnEntity(loc, org.bukkit.entity.EntityType.SHULKER);
            shulker.setInvisible(true);
            shulker.setSilent(true);
            shulker.setAI(false);
            shulker.setGravity(false);
            shulker.setInvulnerable(true);
            shulker.setPersistent(false);
            shulker.customName(null);
            shulker.setCustomNameVisible(false);
            org.bukkit.NamespacedKey hitboxKey = new org.bukkit.NamespacedKey("houzicore", "bde_hitbox");
            shulker.getPersistentDataContainer().set(hitboxKey, org.bukkit.persistence.PersistentDataType.STRING, _id);
            if (shulker.getAttribute(org.bukkit.attribute.Attribute.SCALE) != null) {
                shulker.getAttribute(org.bukkit.attribute.Attribute.SCALE).setBaseValue(hb.scale);
            }
            _auxiliaryEntities.add(shulker);
        }

        // Spawn interaction entities for seating
        for (SeatConfig seat : _seats) {
            Location loc = _origin.clone().add(seat.offsetX, seat.offsetY, seat.offsetZ);
            org.bukkit.entity.Interaction interaction = (org.bukkit.entity.Interaction) loc.getWorld().spawnEntity(loc, org.bukkit.entity.EntityType.INTERACTION);
            interaction.setInteractionWidth(seat.width);
            interaction.setInteractionHeight(seat.height);
            interaction.setResponsive(true);
            interaction.setPersistent(false);
            org.bukkit.NamespacedKey seatKey = new org.bukkit.NamespacedKey("houzicore", "bde_seat");
            interaction.getPersistentDataContainer().set(seatKey, org.bukkit.persistence.PersistentDataType.STRING, _id);
            _auxiliaryEntities.add(interaction);
        }

        // Spawn interaction entities for clicking (non-seating)
        for (InteractConfig ic : _interactions) {
            Location loc = _origin.clone().add(ic.offsetX, ic.offsetY, ic.offsetZ);
            org.bukkit.entity.Interaction interaction = (org.bukkit.entity.Interaction) loc.getWorld().spawnEntity(loc, org.bukkit.entity.EntityType.INTERACTION);
            interaction.setInteractionWidth(ic.width);
            interaction.setInteractionHeight(ic.height);
            interaction.setResponsive(true);
            interaction.setPersistent(false);
            org.bukkit.NamespacedKey interactKey = new org.bukkit.NamespacedKey("houzicore", "bde_interact");
            interaction.getPersistentDataContainer().set(interactKey, org.bukkit.persistence.PersistentDataType.STRING, _id);
            _auxiliaryEntities.add(interaction);
        }
    }

    /**
     * Spawn this model using raw Minecraft /summon commands.
     * Used for BDEngine file models — lets Minecraft's NBT parser handle all
     * transformation math instead of the Java Transformation API (which is bug-prone).
     *
     * @param origin   The spawn origin
     * @param commands List of raw summon command strings (without leading slash)
     *                 e.g. "summon block_display ~ ~ ~ {transformation:[...]}"
     */
    public void spawnWithCommands(Location origin, List<String> commands) {
        boolean isRespawn = isSpawned();
        if (isRespawn) {
            remove();
        }

        _origin = origin.clone();
        _spawnedEntities.clear();
        _auxiliaryEntities.clear();
        _baseScales.clear();

        // Track entity count before spawning so we can identify newly-spawned entities
        final double x = origin.getX();
        final double y = origin.getY();
        final double z = origin.getZ();
        final String worldName = origin.getWorld().getName();

        // Execute each summon command via Paper's server command sender
        // Prefix coordinates with absolute values by replacing ~ with actual coords
        org.bukkit.command.CommandSender console = org.bukkit.Bukkit.getConsoleSender();
        for (String cmd : commands) {
            // Replace relative coords if any (the command should use absolute coords ideally)
            // Commands from BDEngine API use ~ ~ ~ which we must replace with actual coords
            String resolvedCmd = cmd
                .replaceFirst("~ ~ ~", String.format("%.4f %.4f %.4f", x, y, z));

            // Execute the command on the next tick to ensure the entity is tracked
            final String finalCmd = resolvedCmd;
            org.bukkit.Bukkit.getScheduler().runTask(
                org.bukkit.Bukkit.getPluginManager().getPlugin("HouziCore-Shared") != null
                    ? org.bukkit.Bukkit.getPluginManager().getPlugin("HouziCore-Shared")
                    : org.bukkit.Bukkit.getPluginManager().getPlugins()[0],
                () -> org.bukkit.Bukkit.dispatchCommand(console, finalCmd)
            );
        }

        // Note: spawnedEntities list will NOT be tracked when using command-based spawning.
        // For proper entity tracking and cleanup, use spawn() with Java Transformation API instead.
    }


    /**
     * Gets the spawned auxiliary entities (hitboxes, seats, interactions).
     */
    public List<Entity> getAuxiliaryEntities() {
        return _auxiliaryEntities;
    }

    /**
     * Remove all spawned entities from the world.
     */
    public void remove() {
        for (Display entity : _spawnedEntities) {
            if (entity != null && entity.isValid()) {
                entity.remove();
            }
        }
        for (Entity entity : _auxiliaryEntities) {
            if (entity != null && entity.isValid()) {
                // If there are passengers (players sitting), eject them first
                entity.eject();
                entity.remove();
            }
        }
        _spawnedEntities.clear();
        _auxiliaryEntities.clear();
        _baseScales.clear();
        _followTarget = null;
        _origin = null;
    }

    /**
     * Teleport the entire model to a new location (client-side smooth).
     */
    public void teleport(Location newOrigin) {
        if (!isSpawned()) return;

        _origin = newOrigin.clone();

        for (int i = 0; i < _spawnedEntities.size(); i++) {
            Display entity = _spawnedEntities.get(i);
            if (entity == null || !entity.isValid()) continue;

            // DisplayPart stores its local offset inside transformation.translation.
            // Teleport only the shared origin, otherwise the local translation is applied twice.
            entity.teleport(_origin);
        }

        // We don't smoothly teleport hitboxes (they don't support it well), but we update their positions
        int auxIdx = 0;
        for (HitboxConfig hb : _hitboxes) {
            if (auxIdx < _auxiliaryEntities.size()) {
                Entity aux = _auxiliaryEntities.get(auxIdx++);
                if (aux != null && aux.isValid()) {
                    aux.teleport(_origin.clone().add(hb.offsetX, hb.offsetY, hb.offsetZ));
                }
            }
        }
        for (SeatConfig seat : _seats) {
            if (auxIdx < _auxiliaryEntities.size()) {
                Entity aux = _auxiliaryEntities.get(auxIdx++);
                if (aux != null && aux.isValid()) {
                    aux.teleport(_origin.clone().add(seat.offsetX, seat.offsetY, seat.offsetZ));
                }
            }
        }
        for (InteractConfig ic : _interactions) {
            if (auxIdx < _auxiliaryEntities.size()) {
                Entity aux = _auxiliaryEntities.get(auxIdx++);
                if (aux != null && aux.isValid()) {
                    aux.teleport(_origin.clone().add(ic.offsetX, ic.offsetY, ic.offsetZ));
                }
            }
        }
    }

    // ── Internal Classes ─────────────────────────────
    
    public static class HitboxConfig {
        double offsetX, offsetY, offsetZ, scale;
        HitboxConfig(double ox, double oy, double oz, double s) {
            offsetX = ox; offsetY = oy; offsetZ = oz; scale = s;
        }
    }
    
    public static class SeatConfig {
        double offsetX, offsetY, offsetZ;
        float width, height;
        SeatConfig(double ox, double oy, double oz, float w, float h) {
            offsetX = ox; offsetY = oy; offsetZ = oz; width = w; height = h;
        }
    }
    
    public static class InteractConfig {
        public double offsetX, offsetY, offsetZ;
        public float width, height;
        InteractConfig(double ox, double oy, double oz, float w, float h) {
            offsetX = ox; offsetY = oy; offsetZ = oz; width = w; height = h;
        }
    }

    // ── Following ────────────────────────────────────

    /**
     * Make this model follow an entity (player, NPC, etc.) with an offset.
     * The model will be teleported every tick to stay on the target.
     */
    public DisplayModel setFollowEntity(Entity target, double offsetX, double offsetY, double offsetZ) {
        _followTarget = target;
        _followOffset = new org.bukkit.util.Vector(offsetX, offsetY, offsetZ);
        return this;
    }

    /**
     * Make this model follow an entity at its location (no offset).
     */
    public DisplayModel setFollowEntity(Entity target) {
        return setFollowEntity(target, 0, 0, 0);
    }

    /**
     * Stop following any entity.
     */
    public DisplayModel clearFollowEntity() {
        _followTarget = null;
        return this;
    }

    // ── Animation ────────────────────────────────────

    /**
     * Set the animation for this model.
     */
    public DisplayModel setAnimation(ModelAnimation animation) {
        _animation = animation != null ? animation : ModelAnimation.none();
        return this;
    }

    /**
     * Set how many ticks the client uses to smooth teleport movement.
     * Lower = snappier, higher = smoother but slightly behind.
     * Default: 3.
     */
    public DisplayModel setTeleportDuration(int ticks) {
        _teleportDuration = Math.max(0, Math.min(ticks, 59));
        for (Display entity : _spawnedEntities) {
            if (entity != null && entity.isValid()) {
                entity.setTeleportDuration(_teleportDuration);
            }
        }
        return this;
    }

    // ── Tick (called by DisplayEntityManager) ────────

    /**
     * Called every relevant tick by {@link DisplayEntityManager}.
     * Handles entity following and animation updates.
     */
    void tick() {
        if (!isSpawned()) return;

        _tickCounter++;

        // Following
        if (_followTarget != null) {
            if (!_followTarget.isValid()) {
                _followTarget = null;
            } else {
                Location targetLoc = _followTarget.getLocation().add(_followOffset);
                teleport(targetLoc);
            }
        }

        // Animation (only apply on the animation's interpolation interval)
        if (_animation.isActive() && _tickCounter % _animation.getInterpolationTicks() == 0) {
            for (int i = 0; i < _spawnedEntities.size(); i++) {
                Display entity = _spawnedEntities.get(i);
                if (entity != null && entity.isValid()) {
                    Vector3f baseScale = i < _baseScales.size() ? _baseScales.get(i) : new Vector3f(1, 1, 1);
                    _animation.apply(entity, _tickCounter, baseScale);
                }
            }
        }
    }

    // ── Getters ──────────────────────────────────────

    public String getId() { return _id; }
    public boolean isSpawned() { return !_spawnedEntities.isEmpty(); }
    public Location getOrigin() { return _origin != null ? _origin.clone() : null; }
    public List<Display> getEntities() { return new ArrayList<>(_spawnedEntities); }
    public List<DisplayPart> getParts() { return new ArrayList<>(_parts); }
    public Entity getFollowTarget() { return _followTarget; }
    public ModelAnimation getAnimation() { return _animation; }

    /**
     * @return The number of constituent Display entities in this model
     */
    public int getPartCount() { return _parts.size(); }

    public com.houzicore.shared.core.lifecycle.LifecycleOwner getOwner() { return _owner; }

    public DisplayModel setOwner(com.houzicore.shared.core.lifecycle.LifecycleOwner owner) {
        _owner = owner;
        return this;
    }

    /**
     * Create a deep copy of this model definition (unspawned).
     */
    public DisplayModel copy(String newId) {
        return copyRotated(newId, 0);
    }

    /**
     * Create a deep copy and rotate its local offsets around the model origin.
     */
    public DisplayModel copyRotated(String newId, float yawDegrees) {
        List<DisplayPart> copiedParts = new ArrayList<>();
        for (DisplayPart p : _parts) {
            copiedParts.add(p.copy().rotateAroundY(yawDegrees));
        }
        DisplayModel copy = new DisplayModel(newId, copiedParts);
        copy._animation = _animation;
        copy._teleportDuration = _teleportDuration;
        for (HitboxConfig hb : _hitboxes) {
            double[] rotated = rotateOffset(hb.offsetX, hb.offsetZ, yawDegrees);
            copy._hitboxes.add(new HitboxConfig(rotated[0], hb.offsetY, rotated[1], hb.scale));
        }
        for (SeatConfig seat : _seats) {
            double[] rotated = rotateOffset(seat.offsetX, seat.offsetZ, yawDegrees);
            copy._seats.add(new SeatConfig(rotated[0], seat.offsetY, rotated[1], seat.width, seat.height));
        }
        for (InteractConfig ic : _interactions) {
            double[] rotated = rotateOffset(ic.offsetX, ic.offsetZ, yawDegrees);
            copy._interactions.add(new InteractConfig(rotated[0], ic.offsetY, rotated[1], ic.width, ic.height));
        }
        return copy;
    }

    private static double[] rotateOffset(double x, double z, float yawDegrees) {
        if (Math.abs(yawDegrees) < 0.0001f) {
            return new double[] { x, z };
        }
        double radians = Math.toRadians(-yawDegrees);
        double cos = Math.cos(radians);
        double sin = Math.sin(radians);
        return new double[] {
                x * cos + z * sin,
                -x * sin + z * cos
        };
    }
}
