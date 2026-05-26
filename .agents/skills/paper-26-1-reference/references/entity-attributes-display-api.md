# Paper 26.1.2 — Entity, Attributes & Display API Reference

> **API Version**: `paper-api 26.1.2.build.64-stable`
> **Javadoc**: https://jd.papermc.io/paper/26.1.2/

---

## Table of Contents
1. [Entity Base API](#1-entity-base-api)
2. [Spawning Entities](#2-spawning-entities)
3. [LivingEntity API](#3-livingentity-api)
4. [Attribute System](#4-attribute-system)
5. [Display Entities (Base)](#5-display-entities-base)
6. [TextDisplay](#6-textdisplay)
7. [BlockDisplay](#7-blockdisplay)
8. [ItemDisplay](#8-itemdisplay)
9. [Interaction Entity](#9-interaction-entity)
10. [TeleportFlag (Paper)](#10-teleportflag-paper)
11. [Comprehensive Code Examples](#11-comprehensive-code-examples)

---

## 1. Entity Base API

**Interface**: `org.bukkit.entity.Entity`
**Extends**: `Metadatable`, `CommandSender`, `Nameable`, `PersistentDataHolder`, `HoverEventSource<HoverEvent.ShowEntity>`, `Sound.Emitter`, `DataComponentView`

### 1.1 Identity & Type

| Method | Return | Description |
|--------|--------|-------------|
| `getEntityId()` | `int` | Server-side entity ID (changes each session) |
| `getUniqueId()` | `UUID` | Persistent UUID |
| `getType()` | `EntityType` | Entity type enum |
| `getName()` | `String` | Entity name |
| `name()` | `Component` | Adventure component name |
| `customName()` | `Component` | Custom display name (nullable) |
| `customName(Component)` | `void` | Set custom name |
| `isCustomNameVisible()` | `boolean` | Name plate visible? |
| `setCustomNameVisible(boolean)` | `void` | Toggle name plate |
| `getScoreboardEntryName()` | `String` | Paper — scoreboard entry name |
| `teamDisplayName()` | `Component` | Paper — team-colored display name |

### 1.2 Position & Movement

| Method | Return | Description |
|--------|--------|-------------|
| `getLocation()` | `Location` | Current location (creates new object) |
| `getLocation(Location)` | `Location` | Reuse existing Location (no alloc) |
| `getX()` / `getY()` / `getZ()` | `double` | Coordinate getters (Paper) |
| `getYaw()` / `getPitch()` | `float` | Rotation getters (Paper) |
| `getWorld()` | `World` | Current world |
| `getVelocity()` | `Vector` | Current velocity |
| `setVelocity(Vector)` | `void` | Set velocity |
| `isOnGround()` | `boolean` | Standing on solid block |
| `getFacing()` | `BlockFace` | Cardinal direction facing |
| `getPose()` | `Pose` | Current pose (STANDING, SNEAKING, etc.) |
| `setPose(Pose)` | `void` | Force pose |
| `setPose(Pose, boolean)` | `void` | Force pose, fixed flag |
| `hasFixedPose()` | `boolean` | Whether pose is locked |
| `setRotation(float yaw, float pitch)` | `void` | Set rotation without teleporting |

### 1.3 State Flags

| Method | Return | Description |
|--------|--------|-------------|
| `isDead()` | `boolean` | Entity has been removed |
| `isValid()` | `boolean` | Entity exists and is valid |
| `isInWorld()` | `boolean` | Paper — currently in a world |
| `isTicking()` | `boolean` | Paper — currently ticking |
| `isPersistent()` | `boolean` | Persists across chunk unloads |
| `setPersistent(boolean)` | `void` | Toggle persistence |
| `isInvisible()` | `boolean` | Invisibility state |
| `setInvisible(boolean)` | `void` | Toggle invisibility |
| `isInvulnerable()` | `boolean` | Invulnerable state |
| `setInvulnerable(boolean)` | `void` | Toggle invulnerability |
| `isSilent()` | `boolean` | Suppresses sounds |
| `setSilent(boolean)` | `void` | Toggle silent |
| `isGlowing()` | `boolean` | Glowing outline |
| `setGlowing(boolean)` | `void` | Toggle glow |
| `hasGravity()` | `boolean` | Affected by gravity |
| `setGravity(boolean)` | `void` | Toggle gravity |
| `hasNoPhysics()` | `boolean` | No collision/physics |
| `setNoPhysics(boolean)` | `void` | Toggle no-physics |
| `isSneaking()` | `boolean` | Sneaking state |
| `setSneaking(boolean)` | `void` | Toggle sneaking |
| `isVisibleByDefault()` | `boolean` | Paper — visible to all by default |
| `setVisibleByDefault(boolean)` | `void` | Paper — toggle default visibility |

### 1.4 Fire & Freeze

```java
int getFireTicks();
void setFireTicks(int ticks);
int getMaxFireTicks();
boolean isVisualFire();        // Paper
void setVisualFire(boolean);   // Paper — visual only, no damage
void setVisualFire(TriState);  // Paper

int getFreezeTicks();
void setFreezeTicks(int ticks);
int getMaxFreezeTicks();
boolean isFrozen();
boolean isFreezeTickingLocked();  // Paper
void lockFreezeTicks(boolean);    // Paper
```

### 1.5 Passengers & Vehicles

```java
Entity getVehicle();
boolean isInsideVehicle();
boolean leaveVehicle();
List<Entity> getPassengers();
boolean addPassenger(Entity passenger);
boolean removePassenger(Entity passenger);
boolean eject();                     // Remove all passengers
boolean isEmpty();                   // No passengers
```

### 1.6 Teleportation

```java
boolean teleport(Location loc);
boolean teleport(Location loc, TeleportCause cause);
boolean teleport(Entity destination);
boolean teleport(Entity destination, TeleportCause cause);

// Paper extensions
boolean teleport(Location loc, TeleportFlag... flags);
boolean teleport(Location loc, TeleportCause cause, TeleportFlag... flags);
CompletableFuture<Boolean> teleportAsync(Location loc);
CompletableFuture<Boolean> teleportAsync(Location loc, TeleportFlag... flags);
CompletableFuture<Boolean> teleportAsync(Location loc, TeleportCause cause);
CompletableFuture<Boolean> teleportAsync(Location loc, TeleportCause cause, TeleportFlag... flags);
```

### 1.7 Tags & Metadata

```java
// Scoreboard tags (vanilla)
Set<String> getScoreboardTags();
boolean addScoreboardTag(String tag);
boolean removeScoreboardTag(String tag);

// Bukkit metadata
List<MetadataValue> getMetadata(String key);
void setMetadata(String key, MetadataValue value);
boolean hasMetadata(String key);
void removeMetadata(String key, Plugin plugin);

// Paper PDC (Persistent Data Container)
PersistentDataContainer getPersistentDataContainer();
```

### 1.8 Paper-Specific Entity Methods

```java
// Spawn reason
CreatureSpawnEvent.SpawnReason getEntitySpawnReason();

// Tracking
Set<Player> getTrackedPlayers();     // Players tracking this entity
Set<Player> getTrackedBy();         // Alias
boolean isTrackedBy(Player player);

// Origin
Location getOrigin();               // Original spawn location

// Scheduler (Folia-compatible)
EntityScheduler getScheduler();

// Copying/Snapshots
Entity copy();                      // Copy entity (unspawned)
Entity copy(Location loc);          // Copy and place at location
EntitySnapshot createSnapshot();    // Serializable snapshot

// Look
void lookAt(double x, double y, double z, LookAnchor anchor);
void lookAt(Position pos, LookAnchor anchor);

// Bounding box
BoundingBox getBoundingBox();
boolean collidesAt(Location loc);
boolean wouldCollideUsing(BoundingBox bb);

// Serialization
String getAsString();               // SNBT representation
float getWidth();
float getHeight();

// Effects
void broadcastHurtAnimation(Collection<Player> players);
void playEffect(EntityEffect effect);

// Environment
boolean isInWater();
boolean isInLava();
boolean isInRain();
boolean isInBubbleColumn();
boolean isInPowderedSnow();
boolean isUnderWater();
boolean isInWaterOrRain();
boolean isInWaterOrBubbleColumn();
boolean isInWaterOrRainOrBubbleColumn();

// Misc
void remove();
float getFallDistance();
void setFallDistance(float distance);
int getTicksLived();
void setTicksLived(int ticks);
int getPortalCooldown();
void setPortalCooldown(int ticks);
SpawnCategory getSpawnCategory();
```

---

## 2. Spawning Entities

### 2.1 Basic Spawning

```java
World world = player.getWorld();
Location loc = player.getLocation();

// Simple spawn
Zombie zombie = (Zombie) world.spawnEntity(loc, EntityType.ZOMBIE);

// Typed spawn
Zombie zombie = world.spawn(loc, Zombie.class);

// Spawn with pre-configuration (entity is configured BEFORE adding to world)
Zombie zombie = world.spawn(loc, Zombie.class, z -> {
    z.customName(Component.text("Boss Zombie", NamedTextColor.RED));
    z.setCustomNameVisible(true);
    z.setHealth(100);
    z.getAttribute(Attribute.MAX_HEALTH).setBaseValue(100);
    z.setGlowing(true);
});

// Paper: Spawn with reason
Zombie zombie = world.spawn(loc, Zombie.class,
    CreatureSpawnEvent.SpawnReason.CUSTOM, z -> {
        z.setPersistent(true);
    });
```

### 2.2 Spawning Display Entities

```java
// TextDisplay
TextDisplay text = world.spawn(loc, TextDisplay.class, display -> {
    display.text(Component.text("Hello World!", NamedTextColor.GOLD));
    display.setBillboard(Display.Billboard.CENTER);
    display.setBackgroundColor(Color.fromARGB(128, 0, 0, 0));
    display.setShadowed(true);
});

// BlockDisplay
BlockDisplay block = world.spawn(loc, BlockDisplay.class, display -> {
    display.setBlock(Material.DIAMOND_BLOCK.createBlockData());
    display.setBillboard(Display.Billboard.FIXED);
});

// ItemDisplay
ItemDisplay item = world.spawn(loc, ItemDisplay.class, display -> {
    display.setItemStack(new ItemStack(Material.DIAMOND_SWORD));
    display.setItemDisplayTransform(ItemDisplay.ItemDisplayTransform.GROUND);
});

// Interaction
Interaction interaction = world.spawn(loc, Interaction.class, e -> {
    e.setInteractionWidth(2.0f);
    e.setInteractionHeight(2.0f);
    e.setResponsive(true);
});
```

---

## 3. LivingEntity API

**Interface**: `org.bukkit.entity.LivingEntity`
**Extends**: `Entity`, `Attributable`, `Damageable`, `ProjectileSource`

### 3.1 Health & Damage

```java
double getHealth();
void setHealth(double health);
double getAbsorptionAmount();
void setAbsorptionAmount(double amount);
double getMaxHealth();                     // Deprecated — use attributes
void setMaxHealth(double health);          // Deprecated — use attributes
void resetMaxHealth();                     // Deprecated
void damage(double amount);
void damage(double amount, Entity source);
EntityDamageEvent getLastDamageCause();
void setLastDamageCause(EntityDamageEvent event);
int getNoDamageTicks();
void setNoDamageTicks(int ticks);
int getMaximumNoDamageTicks();
void setMaximumNoDamageTicks(int ticks);
```

### 3.2 Combat

```java
void setAI(boolean ai);
boolean hasAI();
Entity getTargetEntity(int maxDistance);         // Paper
boolean attack(Entity target);                   // Paper — swing at entity
void swingMainHand();                            // Paper
void swingOffHand();                             // Paper
void knockback(double strength, double dx, double dz); // Paper
int getArrowCooldown();
void setArrowCooldown(int ticks);
int getArrowsInBody();
void setArrowsInBody(int count);
int getBeeStingerCooldown();
void setBeeStingerCooldown(int ticks);
int getBeeStingersInBody();
void setBeeStingersInBody(int count);
int getMaximumAir();
void setMaximumAir(int ticks);
int getRemainingAir();
void setRemainingAir(int ticks);
```

### 3.3 Equipment

```java
EntityEquipment getEquipment();

// EntityEquipment methods:
ItemStack getItemInMainHand();
void setItemInMainHand(ItemStack item);
ItemStack getItemInOffHand();
void setItemInOffHand(ItemStack item);
ItemStack getHelmet();
void setHelmet(ItemStack item);
ItemStack getChestplate();
void setChestplate(ItemStack item);
ItemStack getLeggings();
void setLeggings(ItemStack item);
ItemStack getBoots();
void setBoots(ItemStack item);
ItemStack[] getArmorContents();
void setArmorContents(ItemStack[] items);

// Drop chances (0.0 = never, 1.0 = always)
float getItemInMainHandDropChance();
void setItemInMainHandDropChance(float chance);
float getHelmetDropChance();
void setHelmetDropChance(float chance);
// ... same for all slots
```

### 3.4 Potion Effects

```java
boolean addPotionEffect(PotionEffect effect);
boolean addPotionEffect(PotionEffect effect, boolean force);
boolean addPotionEffects(Collection<PotionEffect> effects);
boolean hasPotionEffect(PotionEffectType type);
PotionEffect getPotionEffect(PotionEffectType type);
void removePotionEffect(PotionEffectType type);
Collection<PotionEffect> getActivePotionEffects();
boolean clearActivePotionEffects();  // Paper
```

### 3.5 Vision & Pathfinding

```java
boolean hasLineOfSight(Entity other);
boolean hasLineOfSight(Location loc);      // Paper

// Paper — entity AI
Mob mob = (Mob) entity;
mob.getPathfinder().moveTo(target);
mob.getPathfinder().moveTo(location);
mob.getPathfinder().moveTo(location, speed);
mob.getPathfinder().stopPathfinding();
boolean mob.getPathfinder().hasPath();
PathResult mob.getPathfinder().getCurrentPath();

mob.setTarget(LivingEntity target);
LivingEntity mob.getTarget();
```

### 3.6 Location Helpers

```java
Location getEyeLocation();
double getEyeHeight();
double getEyeHeight(boolean ignorePose);
Block getTargetBlock(Set<Material> transparent, int maxDistance);
List<Block> getLastTwoTargetBlocks(Set<Material> transparent, int maxDistance);
Block getTargetBlockExact(int maxDistance);
Block getTargetBlockExact(int maxDistance, FluidCollisionMode fluidMode);
RayTraceResult rayTraceBlocks(double maxDistance);
RayTraceResult rayTraceBlocks(double maxDistance, FluidCollisionMode fluidMode);
```

### 3.7 Paper LivingEntity Extensions

```java
// Memory
<T> void setMemory(MemoryKey<T> key, T value);
<T> T getMemory(MemoryKey<T> key);

// Sounds
Sound getHurtSound();
Sound getDeathSound();
Sound getFallDamageSound(int fallHeight);
Sound getFallDamageSoundBig();
Sound getFallDamageSoundSmall();
Sound getDrinkingSound(ItemStack item);
Sound getEatingSound(ItemStack item);

// Misc
boolean isClimbing();
boolean isLeashed();
Entity getLeashHolder();
void setLeashHolder(Entity holder);
boolean isJumping();        // Paper
void setJumping(boolean);   // Paper
```

---

## 4. Attribute System

**Interface**: `org.bukkit.attribute.Attributable` (implemented by `LivingEntity`)
**Class**: `org.bukkit.attribute.Attribute`
**Class**: `org.bukkit.attribute.AttributeInstance`
**Class**: `org.bukkit.attribute.AttributeModifier`

### 4.1 All Attribute Constants (Paper 26.1.2)

| Attribute | Description | Default |
|-----------|-------------|---------|
| `MAX_HEALTH` | Maximum health of an Entity | 20.0 |
| `FOLLOW_RANGE` | Range at which an Entity will follow others | 32.0 |
| `KNOCKBACK_RESISTANCE` | Resistance to knockback (0.0–1.0) | 0.0 |
| `MOVEMENT_SPEED` | Movement speed | 0.7 (player) |
| `FLYING_SPEED` | Flying speed | 0.4 |
| `ATTACK_DAMAGE` | Attack damage | 2.0 |
| `ATTACK_KNOCKBACK` | Attack knockback force | 0.0 |
| `ATTACK_SPEED` | Attack speed (hits per second) | 4.0 |
| `ARMOR` | Armor bonus | 0.0 |
| `ARMOR_TOUGHNESS` | Armor durability bonus | 0.0 |
| `FALL_DAMAGE_MULTIPLIER` | Fall damage multiplier | 1.0 |
| `LUCK` | Luck bonus (loot tables) | 0.0 |
| `MAX_ABSORPTION` | Maximum absorption hearts | 0.0 |
| `SAFE_FALL_DISTANCE` | Safe fall distance (blocks) | 3.0 |
| `SCALE` | Relative scale of entity | 1.0 |
| `STEP_HEIGHT` | Step-up height (blocks) | 0.6 |
| `GRAVITY` | Gravity applied to entity | 0.08 |
| `JUMP_STRENGTH` | Jump strength | 0.42 |
| `BURNING_TIME` | How long entity burns after ignition | 1.0 |
| `CAMERA_DISTANCE` | Camera distance (3rd person) | 1.0 |
| `EXPLOSION_KNOCKBACK_RESISTANCE` | Explosion knockback resistance | 0.0 |
| `MOVEMENT_EFFICIENCY` | Speed through difficult terrain | 0.0 |
| `OXYGEN_BONUS` | Oxygen use underwater | 0.0 |
| `WATER_MOVEMENT_EFFICIENCY` | Speed through water | 0.0 |
| `TEMPT_RANGE` | Range mobs are tempted by items | 10.0 |
| `BLOCK_INTERACTION_RANGE` | Block reach distance (Player) | 4.5 |
| `ENTITY_INTERACTION_RANGE` | Entity reach distance (Player) | 3.0 |
| `BLOCK_BREAK_SPEED` | Block break speed (Player) | 1.0 |
| `MINING_EFFICIENCY` | Mining speed for correct tools | 0.0 |
| `SNEAKING_SPEED` | Sneaking speed | 0.3 |
| `SUBMERGED_MINING_SPEED` | Underwater mining speed | 0.2 |
| `SWEEPING_DAMAGE_RATIO` | Sweeping damage ratio | 0.0 |
| `SPAWN_REINFORCEMENTS` | Zombie reinforcement chance | 0.0 |
| `WAYPOINT_TRANSMIT_RANGE` | Range entity transmits as waypoint | 0.0 |
| `WAYPOINT_RECEIVE_RANGE` | Range entity receives waypoints | 0.0 |

### 4.2 Attribute.Sentiment

```java
enum Attribute.Sentiment {
    POSITIVE,   // Higher is better (MAX_HEALTH, ARMOR)
    NEUTRAL,    // Context-dependent (GRAVITY, SCALE)
    NEGATIVE    // Higher is worse (none currently)
}

Attribute.Sentiment sentiment = Attribute.MAX_HEALTH.getSentiment(); // POSITIVE
```

### 4.3 AttributeInstance API

```java
AttributeInstance attr = entity.getAttribute(Attribute.MAX_HEALTH);
if (attr == null) return; // Entity doesn't have this attribute

// Read values
double baseValue  = attr.getBaseValue();       // Base (unmodified)
double value      = attr.getValue();           // Final (with modifiers)
double defaultVal = attr.getDefaultValue();    // Vanilla default

// Set base
attr.setBaseValue(40.0); // 20 hearts

// Modifiers
Collection<AttributeModifier> mods = attr.getModifiers();
attr.addModifier(modifier);
attr.removeModifier(modifier);
attr.removeModifier(NamespacedKey key);        // Paper
```

### 4.4 AttributeModifier

```java
// Constructor
AttributeModifier modifier = new AttributeModifier(
    NamespacedKey.fromString("myplugin:speed_boost"),  // Unique key
    0.5,                                                // Amount
    AttributeModifier.Operation.ADD_NUMBER              // Operation
);

// With equipment slot restriction
AttributeModifier modifier = new AttributeModifier(
    NamespacedKey.fromString("myplugin:chest_armor"),
    8.0,
    AttributeModifier.Operation.ADD_NUMBER,
    EquipmentSlotGroup.CHEST
);
```

### 4.5 AttributeModifier.Operation

| Operation | Formula | Description |
|-----------|---------|-------------|
| `ADD_NUMBER` | `base + Σ(amount)` | Flat addition |
| `ADD_SCALAR` | `result * (1 + Σ(amount))` | Percentage of base after ADD_NUMBER |
| `MULTIPLY_SCALAR_1` | `result * Π(1 + amount)` | Multiplicative stacking |

### 4.6 Complete Attribute Example

```java
public void makeSuper(LivingEntity entity) {
    // Double health
    AttributeInstance maxHealth = entity.getAttribute(Attribute.MAX_HEALTH);
    if (maxHealth != null) {
        maxHealth.addModifier(new AttributeModifier(
            NamespacedKey.fromString("myplugin:super_health"),
            20.0, // +20 health (10 hearts)
            AttributeModifier.Operation.ADD_NUMBER
        ));
        entity.setHealth(maxHealth.getValue()); // Heal to new max
    }

    // +50% speed
    AttributeInstance speed = entity.getAttribute(Attribute.MOVEMENT_SPEED);
    if (speed != null) {
        speed.addModifier(new AttributeModifier(
            NamespacedKey.fromString("myplugin:super_speed"),
            0.5,
            AttributeModifier.Operation.ADD_SCALAR
        ));
    }

    // 2x scale
    AttributeInstance scale = entity.getAttribute(Attribute.SCALE);
    if (scale != null) {
        scale.setBaseValue(2.0);
    }

    // Extended reach
    if (entity instanceof Player) {
        AttributeInstance reach = entity.getAttribute(Attribute.BLOCK_INTERACTION_RANGE);
        if (reach != null) {
            reach.addModifier(new AttributeModifier(
                NamespacedKey.fromString("myplugin:super_reach"),
                3.0, // +3 blocks reach
                AttributeModifier.Operation.ADD_NUMBER
            ));
        }
    }
}

public void removeSuper(LivingEntity entity) {
    for (Attribute attr : new Attribute[]{
        Attribute.MAX_HEALTH, Attribute.MOVEMENT_SPEED,
        Attribute.BLOCK_INTERACTION_RANGE
    }) {
        AttributeInstance inst = entity.getAttribute(attr);
        if (inst != null) {
            // Remove by key (Paper API)
            inst.removeModifier(NamespacedKey.fromString("myplugin:super_health"));
            inst.removeModifier(NamespacedKey.fromString("myplugin:super_speed"));
            inst.removeModifier(NamespacedKey.fromString("myplugin:super_reach"));
        }
    }
    AttributeInstance scale = entity.getAttribute(Attribute.SCALE);
    if (scale != null) scale.setBaseValue(1.0);
}
```

---

## 5. Display Entities (Base)

**Interface**: `org.bukkit.entity.Display`
**Extends**: `Entity`
**Subinterfaces**: `TextDisplay`, `BlockDisplay`, `ItemDisplay`

> Display entities are client-rendered visual elements with no hitbox, no AI, and no physics. They are the modern replacement for armor stands for visual effects.

### 5.1 Transformation

```java
Transformation getTransformation();
void setTransformation(Transformation transformation);
void setTransformationMatrix(Matrix4f matrix);  // Raw JOML matrix

// Transformation class (org.bukkit.util.Transformation)
// Components: translation, leftRotation, scale, rightRotation
Transformation t = new Transformation(
    new Vector3f(0, 0, 0),        // translation
    new AxisAngle4f(0, 0, 0, 1),  // left rotation
    new Vector3f(1, 1, 1),        // scale
    new AxisAngle4f(0, 0, 0, 1)   // right rotation
);
```

### 5.2 Interpolation (Smooth Animation)

```java
int getInterpolationDuration();
void setInterpolationDuration(int ticks);   // How long animation takes
int getInterpolationDelay();
void setInterpolationDelay(int ticks);      // Ticks before animation starts
int getTeleportDuration();
void setTeleportDuration(int ticks);        // Smooth teleport (0–59 ticks)
```

### 5.3 Rendering

```java
float getViewRange();
void setViewRange(float range);        // Multiplier of default view distance

float getShadowRadius();
void setShadowRadius(float radius);    // Shadow circle radius
float getShadowStrength();
void setShadowStrength(float strength);// Shadow darkness

float getDisplayWidth();
void setDisplayWidth(float width);     // Culling width
float getDisplayHeight();
void setDisplayHeight(float height);   // Culling height

// Billboard (how entity faces player)
Display.Billboard getBillboard();
void setBillboard(Display.Billboard billboard);

// Glow
Color getGlowColorOverride();
void setGlowColorOverride(Color color);

// Brightness
Display.Brightness getBrightness();
void setBrightness(Display.Brightness brightness);
```

### 5.4 Display.Billboard Enum

| Value | Description |
|-------|-------------|
| `FIXED` | No rotation — stays in placed orientation |
| `VERTICAL` | Rotates around Y axis to face player |
| `HORIZONTAL` | Rotates around X axis to face player |
| `CENTER` | Full rotation to always face player (like nametags) |

### 5.5 Display.Brightness

```java
// Record class
Display.Brightness brightness = new Display.Brightness(15, 15);
// (blockLight: 0-15, skyLight: 0-15)
int block = brightness.getBlockLight();
int sky   = brightness.getSkyLight();
```

---

## 6. TextDisplay

**Interface**: `org.bukkit.entity.TextDisplay`
**Extends**: `Display`

### 6.1 All Methods

| Method | Return | Description |
|--------|--------|-------------|
| `text()` | `Component` | Get displayed text (Adventure) |
| `text(Component)` | `void` | Set displayed text |
| `getText()` | `String` | **Deprecated** — use `text()` |
| `setText(String)` | `void` | **Deprecated** — use `text(Component)` |
| `getLineWidth()` | `int` | Max line width before wrapping (default 200) |
| `setLineWidth(int)` | `void` | Set line wrap width |
| `getBackgroundColor()` | `Color` | Background color (nullable) |
| `setBackgroundColor(Color)` | `void` | Set background color (null = transparent) |
| `getTextOpacity()` | `byte` | Text opacity (-128 to 127, -1 = opaque) |
| `setTextOpacity(byte)` | `void` | Set opacity |
| `isShadowed()` | `boolean` | Text has shadow |
| `setShadowed(boolean)` | `void` | Toggle text shadow |
| `isSeeThrough()` | `boolean` | Visible through blocks |
| `setSeeThrough(boolean)` | `void` | Toggle see-through |
| `isDefaultBackground()` | `boolean` | Using default chat background |
| `setDefaultBackground(boolean)` | `void` | Toggle default background |
| `getAlignment()` | `TextAlignment` | Text alignment |
| `setAlignment(TextAlignment)` | `void` | Set alignment |

### 6.2 TextDisplay.TextAlignment Enum

| Value | Description |
|-------|-------------|
| `CENTER` | Centered text (default) |
| `LEFT` | Left-aligned |
| `RIGHT` | Right-aligned |

### 6.3 Complete TextDisplay Example

```java
public TextDisplay spawnFloatingLabel(Location loc, String text, Color bgColor) {
    return loc.getWorld().spawn(loc, TextDisplay.class, display -> {
        display.text(MiniMessage.miniMessage().deserialize(text));
        display.setBillboard(Display.Billboard.CENTER);
        display.setBackgroundColor(bgColor != null ? bgColor : Color.fromARGB(100, 0, 0, 0));
        display.setShadowed(true);
        display.setSeeThrough(false);
        display.setDefaultBackground(false);
        display.setAlignment(TextDisplay.TextAlignment.CENTER);
        display.setLineWidth(300);
        display.setViewRange(100f);
        display.setBrightness(new Display.Brightness(15, 15));

        // Animation: scale up from 0
        display.setTransformation(new Transformation(
            new Vector3f(0, 0, 0),
            new AxisAngle4f(0, 0, 0, 1),
            new Vector3f(0, 0, 0),         // Start at scale 0
            new AxisAngle4f(0, 0, 0, 1)
        ));
        display.setInterpolationDelay(0);
        display.setInterpolationDuration(10);  // 0.5 seconds
    });
}

// After spawning, set target transformation for animation:
public void animateIn(TextDisplay display) {
    Bukkit.getScheduler().runTaskLater(plugin, () -> {
        display.setTransformation(new Transformation(
            new Vector3f(0, 0, 0),
            new AxisAngle4f(0, 0, 0, 1),
            new Vector3f(1, 1, 1),         // Scale to full size
            new AxisAngle4f(0, 0, 0, 1)
        ));
    }, 1L); // 1 tick delay so interpolation triggers
}
```

---

## 7. BlockDisplay

**Interface**: `org.bukkit.entity.BlockDisplay`
**Extends**: `Display`

### 7.1 Methods

| Method | Return | Description |
|--------|--------|-------------|
| `getBlock()` | `BlockData` | Get displayed block data |
| `setBlock(BlockData)` | `void` | Set displayed block data |

### 7.2 Examples

```java
// Floating diamond block
BlockDisplay diamond = world.spawn(loc, BlockDisplay.class, d -> {
    d.setBlock(Material.DIAMOND_BLOCK.createBlockData());
    d.setBillboard(Display.Billboard.FIXED);
    d.setShadowRadius(1.0f);
    d.setShadowStrength(0.5f);
    d.setGlowing(true);
    d.setGlowColorOverride(Color.AQUA);
});

// Scaled block (2x size)
BlockDisplay big = world.spawn(loc, BlockDisplay.class, d -> {
    d.setBlock(Material.TNT.createBlockData());
    d.setTransformation(new Transformation(
        new Vector3f(-0.5f, 0, -0.5f),   // Center the scaled block
        new AxisAngle4f(0, 0, 0, 1),
        new Vector3f(2, 2, 2),            // 2x scale
        new AxisAngle4f(0, 0, 0, 1)
    ));
});

// Rotated block
BlockDisplay rotated = world.spawn(loc, BlockDisplay.class, d -> {
    d.setBlock(Material.OAK_STAIRS.createBlockData(bd -> {
        ((Stairs) bd).setFacing(BlockFace.NORTH);
    }));
    d.setTransformation(new Transformation(
        new Vector3f(0, 0, 0),
        new AxisAngle4f((float) Math.toRadians(45), 0, 1, 0), // 45° Y rotation
        new Vector3f(1, 1, 1),
        new AxisAngle4f(0, 0, 0, 1)
    ));
});
```

---

## 8. ItemDisplay

**Interface**: `org.bukkit.entity.ItemDisplay`
**Extends**: `Display`

### 8.1 Methods

| Method | Return | Description |
|--------|--------|-------------|
| `getItemStack()` | `ItemStack` | Get displayed item |
| `setItemStack(ItemStack)` | `void` | Set displayed item |
| `getItemDisplayTransform()` | `ItemDisplayTransform` | Rendering context |
| `setItemDisplayTransform(ItemDisplayTransform)` | `void` | Set render context |

### 8.2 ItemDisplay.ItemDisplayTransform Enum

| Value | Description |
|-------|-------------|
| `NONE` | Raw model |
| `THIRDPERSON_LEFTHAND` | 3rd person left hand |
| `THIRDPERSON_RIGHTHAND` | 3rd person right hand |
| `FIRSTPERSON_LEFTHAND` | 1st person left hand |
| `FIRSTPERSON_RIGHTHAND` | 1st person right hand |
| `HEAD` | Worn on head |
| `GUI` | In inventory/GUI |
| `GROUND` | Dropped on ground |
| `FIXED` | In item frame |

### 8.3 Examples

```java
// Floating sword
ItemDisplay sword = world.spawn(loc, ItemDisplay.class, d -> {
    d.setItemStack(new ItemStack(Material.DIAMOND_SWORD));
    d.setItemDisplayTransform(ItemDisplay.ItemDisplayTransform.FIXED);
    d.setBillboard(Display.Billboard.CENTER);
    d.setBrightness(new Display.Brightness(15, 15));
});

// Custom head display
ItemStack head = new ItemStack(Material.PLAYER_HEAD);
SkullMeta meta = (SkullMeta) head.getItemMeta();
// ... set skull owner/profile ...
head.setItemMeta(meta);

ItemDisplay headDisplay = world.spawn(loc, ItemDisplay.class, d -> {
    d.setItemStack(head);
    d.setItemDisplayTransform(ItemDisplay.ItemDisplayTransform.HEAD);
});
```

---

## 9. Interaction Entity

**Interface**: `org.bukkit.entity.Interaction`
**Extends**: `Entity`

> An invisible entity that records player interactions (clicks/attacks) but has no visual representation. Designed to be paired with Display entities for clickable UI elements in the world.

### 9.1 All Methods

| Method | Return | Description |
|--------|--------|-------------|
| `getInteractionWidth()` | `float` | Hitbox width |
| `setInteractionWidth(float)` | `void` | Set hitbox width |
| `getInteractionHeight()` | `float` | Hitbox height |
| `setInteractionHeight(float)` | `void` | Set hitbox height |
| `isResponsive()` | `boolean` | Triggers arm swing animation |
| `setResponsive(boolean)` | `void` | Toggle response |
| `getLastAttack()` | `PreviousInteraction` | Last attack (nullable) |
| `getLastInteraction()` | `PreviousInteraction` | Last right-click (nullable) |

### 9.2 Interaction.PreviousInteraction

```java
interface Interaction.PreviousInteraction {
    UUID getPlayer();        // UUID of interacting player
    long getTimestamp();     // Timestamp in millis
}
```

### 9.3 Clickable Display + Interaction Pattern

```java
public void spawnClickableSign(Location loc, String text, Consumer<Player> onClick) {
    // Visual component
    TextDisplay display = loc.getWorld().spawn(loc, TextDisplay.class, d -> {
        d.text(Component.text(text, NamedTextColor.YELLOW));
        d.setBillboard(Display.Billboard.CENTER);
        d.setBackgroundColor(Color.fromARGB(200, 0, 0, 0));
        d.setShadowed(true);
    });

    // Clickable hitbox
    Interaction hitbox = loc.getWorld().spawn(loc, Interaction.class, i -> {
        i.setInteractionWidth(2.0f);
        i.setInteractionHeight(1.0f);
        i.setResponsive(true);
    });

    // Store reference for click handling
    hitbox.getPersistentDataContainer().set(
        NamespacedKey.fromString("myplugin:callback_id"),
        PersistentDataType.STRING,
        UUID.randomUUID().toString()
    );

    // Mount display on interaction (optional)
    hitbox.addPassenger(display);
}

// Handle clicks via EntityInteractEvent / PlayerInteractAtEntityEvent
@EventHandler
public void onInteract(PlayerInteractEntityEvent event) {
    if (event.getRightClicked() instanceof Interaction interaction) {
        PersistentDataContainer pdc = interaction.getPersistentDataContainer();
        NamespacedKey key = NamespacedKey.fromString("myplugin:callback_id");
        if (pdc.has(key)) {
            String callbackId = pdc.get(key, PersistentDataType.STRING);
            // Look up and execute callback
            handleCallback(event.getPlayer(), callbackId);
        }
    }
}
```

---

## 10. TeleportFlag (Paper)

**Interface**: `io.papermc.paper.entity.TeleportFlag`
**Sealed**: permits `TeleportFlag.Relative`, `TeleportFlag.EntityState`

### 10.1 TeleportFlag.Relative (Velocity Preservation)

Keeps entity velocity in the specified axis during teleport.

| Enum Constant | Description |
|---------------|-------------|
| `VELOCITY_X` | Keep X-axis velocity |
| `VELOCITY_Y` | Keep Y-axis velocity |
| `VELOCITY_Z` | Keep Z-axis velocity |
| `VELOCITY_ROTATION` | Keep rotational velocity |

**Deprecated fields** (use VELOCITY_* instead):
- `X` → use `VELOCITY_X`
- `Y` → use `VELOCITY_Y`
- `Z` → use `VELOCITY_Z`
- `YAW` → use `VELOCITY_ROTATION`
- `PITCH` → use `VELOCITY_ROTATION`

### 10.2 TeleportFlag.EntityState (Deprecated)

> **Deprecated for removal** as of 1.21.10. Default teleport behavior now aligns with vanilla.

### 10.3 Usage Examples

```java
// Teleport while preserving momentum
player.teleport(targetLoc,
    TeleportFlag.Relative.VELOCITY_X,
    TeleportFlag.Relative.VELOCITY_Y,
    TeleportFlag.Relative.VELOCITY_Z
);

// Async teleport with velocity preservation
player.teleportAsync(targetLoc,
    PlayerTeleportEvent.TeleportCause.PLUGIN,
    TeleportFlag.Relative.VELOCITY_X,
    TeleportFlag.Relative.VELOCITY_Y,
    TeleportFlag.Relative.VELOCITY_Z,
    TeleportFlag.Relative.VELOCITY_ROTATION
);

// Useful for minigames: teleport without killing momentum
public void launchAndTeleport(Player player, Location target) {
    player.setVelocity(new Vector(0, 2, 0)); // Launch up
    Bukkit.getScheduler().runTaskLater(plugin, () -> {
        player.teleport(target,
            TeleportFlag.Relative.VELOCITY_X,
            TeleportFlag.Relative.VELOCITY_Y,
            TeleportFlag.Relative.VELOCITY_Z
        );
    }, 10L);
}
```

---

## 11. Comprehensive Code Examples

### 11.1 Boss Entity with Custom Attributes

```java
public Zombie spawnBoss(Location loc) {
    return loc.getWorld().spawn(loc, Zombie.class, boss -> {
        // Name
        boss.customName(MiniMessage.miniMessage().deserialize(
            "<gradient:red:gold><bold>ZOMBIE KING</bold></gradient>"
        ));
        boss.setCustomNameVisible(true);

        // Attributes
        boss.getAttribute(Attribute.MAX_HEALTH).setBaseValue(200);
        boss.setHealth(200);
        boss.getAttribute(Attribute.MOVEMENT_SPEED).setBaseValue(0.35);
        boss.getAttribute(Attribute.ATTACK_DAMAGE).setBaseValue(12);
        boss.getAttribute(Attribute.KNOCKBACK_RESISTANCE).setBaseValue(0.8);
        boss.getAttribute(Attribute.ARMOR).setBaseValue(10);
        boss.getAttribute(Attribute.SCALE).setBaseValue(1.5);
        boss.getAttribute(Attribute.STEP_HEIGHT).setBaseValue(1.5);
        boss.getAttribute(Attribute.FOLLOW_RANGE).setBaseValue(64);

        // Equipment
        boss.getEquipment().setHelmet(new ItemStack(Material.NETHERITE_HELMET));
        boss.getEquipment().setChestplate(new ItemStack(Material.NETHERITE_CHESTPLATE));
        boss.getEquipment().setItemInMainHand(new ItemStack(Material.NETHERITE_SWORD));
        boss.getEquipment().setHelmetDropChance(0f);
        boss.getEquipment().setChestplateDropChance(0f);
        boss.getEquipment().setItemInMainHandDropChance(0.1f);

        // Effects
        boss.addPotionEffect(new PotionEffect(PotionEffectType.FIRE_RESISTANCE, -1, 0, false, false));
        boss.addPotionEffect(new PotionEffect(PotionEffectType.GLOWING, -1, 0, false, false));

        // State
        boss.setPersistent(true);
        boss.setRemoveWhenFarAway(false);
        boss.setGlowing(true);

        // PDC tag
        boss.getPersistentDataContainer().set(
            NamespacedKey.fromString("myplugin:boss"),
            PersistentDataType.BOOLEAN, true
        );
    });
}
```

### 11.2 Animated Hologram with TextDisplay

```java
public class AnimatedHologram {
    private final TextDisplay display;
    private int taskId;

    public AnimatedHologram(Location loc, JavaPlugin plugin) {
        this.display = loc.getWorld().spawn(loc, TextDisplay.class, d -> {
            d.text(Component.text("Loading...", NamedTextColor.GRAY));
            d.setBillboard(Display.Billboard.CENTER);
            d.setBackgroundColor(Color.fromARGB(0, 0, 0, 0)); // Transparent
            d.setShadowed(true);
            d.setViewRange(50f);
            d.setBrightness(new Display.Brightness(15, 15));
        });

        // Rainbow animation
        final int[] frame = {0};
        this.taskId = Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            frame[0]++;
            float hue = (frame[0] % 360) / 360f;
            java.awt.Color awtColor = java.awt.Color.getHSBColor(hue, 1f, 1f);
            TextColor color = TextColor.color(awtColor.getRed(), awtColor.getGreen(), awtColor.getBlue());

            display.text(Component.text("★ Welcome! ★", color, TextDecoration.BOLD));

            // Gentle bobbing
            Location base = display.getLocation();
            double yOffset = Math.sin(frame[0] * 0.1) * 0.1;
            display.setTransformation(new Transformation(
                new Vector3f(0, (float) yOffset, 0),
                new AxisAngle4f(0, 0, 0, 1),
                new Vector3f(1, 1, 1),
                new AxisAngle4f(0, 0, 0, 1)
            ));
            display.setInterpolationDuration(2);
            display.setInterpolationDelay(0);
        }, 0L, 2L).getTaskId();
    }

    public void remove() {
        Bukkit.getScheduler().cancelTask(taskId);
        display.remove();
    }
}
```

### 11.3 Display Entity Progress Bar

```java
public class ProgressBar {
    private final TextDisplay display;

    public ProgressBar(Location loc) {
        this.display = loc.getWorld().spawn(loc, TextDisplay.class, d -> {
            d.setBillboard(Display.Billboard.CENTER);
            d.setBackgroundColor(Color.fromARGB(180, 20, 20, 20));
            d.setShadowed(false);
            d.setLineWidth(400);
        });
        setProgress(0);
    }

    public void setProgress(double percent) {
        percent = Math.max(0, Math.min(100, percent));
        int filled = (int) (percent / 5);  // 20 chars total
        int empty = 20 - filled;

        Component bar = Component.text("█".repeat(filled), NamedTextColor.GREEN)
            .append(Component.text("█".repeat(empty), NamedTextColor.DARK_GRAY));
        Component label = Component.text(String.format(" %.0f%%", percent), NamedTextColor.WHITE);

        display.text(bar.append(label));
    }

    public void remove() {
        display.remove();
    }
}
```

### 11.4 Interaction-Based World Button

```java
public void spawnButton(Location loc, Component label, Runnable action) {
    // Block display as button face
    BlockDisplay face = loc.getWorld().spawn(loc, BlockDisplay.class, d -> {
        d.setBlock(Material.STONE_BUTTON.createBlockData());
        d.setTransformation(new Transformation(
            new Vector3f(-0.25f, -0.25f, 0),
            new AxisAngle4f(0, 0, 0, 1),
            new Vector3f(0.5f, 0.5f, 0.1f),
            new AxisAngle4f(0, 0, 0, 1)
        ));
    });

    // Label above
    TextDisplay text = loc.getWorld().spawn(
        loc.clone().add(0, 0.5, 0), TextDisplay.class, d -> {
            d.text(label);
            d.setBillboard(Display.Billboard.CENTER);
            d.setBackgroundColor(Color.fromARGB(0, 0, 0, 0));
            d.setShadowed(true);
        }
    );

    // Clickable area
    Interaction hitbox = loc.getWorld().spawn(loc, Interaction.class, i -> {
        i.setInteractionWidth(1.0f);
        i.setInteractionHeight(1.0f);
        i.setResponsive(true);
    });
}
```

### 11.5 Entity Snapshot & Copy

```java
// Create a snapshot (serializable, can be stored)
Entity original = world.spawn(loc, Zombie.class, z -> {
    z.customName(Component.text("Template Zombie"));
    z.getAttribute(Attribute.MAX_HEALTH).setBaseValue(50);
    z.setHealth(50);
});
EntitySnapshot snapshot = original.createSnapshot();
original.remove();

// Later: restore from snapshot
Entity restored = snapshot.createEntity(newLocation);
// Or just copy directly
Entity clone = original.copy(newLocation);
```

### 11.6 Scale + Gravity Attribute Combo

```java
// Create a giant floating mob
public void spawnGiantFloater(Location loc) {
    Slime slime = loc.getWorld().spawn(loc, Slime.class, s -> {
        s.setSize(1); // Base size
        s.getAttribute(Attribute.SCALE).setBaseValue(5.0);   // 5x visual scale
        s.getAttribute(Attribute.GRAVITY).setBaseValue(0.01); // Near-zero gravity
        s.getAttribute(Attribute.MAX_HEALTH).setBaseValue(500);
        s.setHealth(500);
        s.getAttribute(Attribute.JUMP_STRENGTH).setBaseValue(0); // No jumping
        s.setAI(false);
        s.setGlowing(true);
        s.customName(Component.text("Sky Slime", NamedTextColor.AQUA));
        s.setCustomNameVisible(true);
    });
}
```

---

## Quick Reference: EntityType (Common)

| Type | Class | Description |
|------|-------|-------------|
| `PLAYER` | `Player` | Human player |
| `ZOMBIE` | `Zombie` | Zombie mob |
| `SKELETON` | `Skeleton` | Skeleton mob |
| `CREEPER` | `Creeper` | Creeper mob |
| `ARMOR_STAND` | `ArmorStand` | Armor stand entity |
| `TEXT_DISPLAY` | `TextDisplay` | Text display entity |
| `BLOCK_DISPLAY` | `BlockDisplay` | Block display entity |
| `ITEM_DISPLAY` | `ItemDisplay` | Item display entity |
| `INTERACTION` | `Interaction` | Interaction entity |
| `ITEM` | `Item` | Dropped item |
| `EXPERIENCE_ORB` | `ExperienceOrb` | XP orb |
| `ARROW` | `Arrow` | Arrow projectile |
| `FIREBALL` | `Fireball` | Fireball projectile |
| `MINECART` | `Minecart` | Rideable minecart |
| `BOAT` | `Boat` | Rideable boat |

## Quick Reference: Pose Enum

| Value | Description |
|-------|-------------|
| `STANDING` | Normal standing |
| `FALL_FLYING` | Elytra flying |
| `SLEEPING` | In bed |
| `SWIMMING` | Swimming |
| `SPIN_ATTACK` | Riptide spinning |
| `SNEAKING` | Crouching |
| `LONG_JUMPING` | Goat long jump |
| `DYING` | Death animation |
| `CROAKING` | Frog croaking |
| `USING_TONGUE` | Frog tongue |
| `SITTING` | Sitting |
| `ROARING` | Warden roaring |
| `SNIFFING` | Warden/Sniffer sniffing |
| `EMERGING` | Warden emerging |
| `DIGGING` | Warden digging |
| `SLIDING` | Sliding |
| `SHOOTING` | Shooting |
| `INHALING` | Breeze inhaling |
