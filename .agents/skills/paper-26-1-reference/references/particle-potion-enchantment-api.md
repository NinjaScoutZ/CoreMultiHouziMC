# Paper 26.1.2 — Particle, Potion & Enchantment API Reference

> **Target version:** Paper 26.1.2 (Minecraft 1.21.5 — "Tiny Takeover")
> **Javadoc root:** `https://jd.papermc.io/paper/26.1.2/`

---

## Table of Contents

1. [Particle System](#1-particle-system)
2. [Potion Effects](#2-potion-effects)
3. [PotionEffectType — All Types](#3-potioneffecttype--all-types)
4. [PotionType — Brewable Potions](#4-potiontype--brewable-potions)
5. [Enchantment API](#5-enchantment-api)
6. [EnchantmentRegistryEntry (Paper)](#6-enchantmentregistryentry-paper)
7. [AreaEffectCloud](#7-areaeffectcloud)
8. [Code Examples](#8-code-examples)

---

## 1. Particle System

### Class: `org.bukkit.Particle` (enum)

Represents all particle types that can be spawned in the world.

### Complete Particle Enum Values

| Particle | Data Type | Description |
|---|---|---|
| `POOF` | — | Small smoke poof (formerly EXPLOSION_NORMAL) |
| `EXPLOSION` | — | Large explosion |
| `EXPLOSION_EMITTER` | — | Huge explosion emitter |
| `FIREWORK` | — | Firework spark |
| `BUBBLE` | — | Water bubble |
| `SPLASH` | — | Water splash |
| `FISHING` | — | Fishing hook trail |
| `UNDERWATER` | — | Underwater ambient particles |
| `CRIT` | — | Critical hit sparkle |
| `ENCHANTED_HIT` | — | Enchanted/magic crit |
| `SMOKE` | — | Small smoke |
| `LARGE_SMOKE` | — | Large smoke |
| `EFFECT` | — | Potion swirl (formerly SPELL) |
| `INSTANT_EFFECT` | — | Instant potion effect |
| `ENTITY_EFFECT` | `Color` | Colored entity effect (mob spell) |
| `WITCH` | — | Witch magic |
| `DRIPPING_WATER` | — | Water drip from blocks |
| `DRIPPING_LAVA` | — | Lava drip from blocks |
| `ANGRY_VILLAGER` | — | Angry villager cloud |
| `HAPPY_VILLAGER` | — | Happy villager green sparkle |
| `MYCELIUM` | — | Mycelium ambient |
| `NOTE` | — | Note block note |
| `PORTAL` | — | Portal swirl |
| `ENCHANT` | — | Enchantment table glyphs |
| `FLAME` | — | Fire flame |
| `SOUL_FIRE_FLAME` | — | Soul fire flame |
| `SMALL_FLAME` | — | Small flame |
| `SNOWFLAKE` | — | Powder snow snowflake |
| `DRIPPING_HONEY` | — | Dripping honey |
| `FALLING_HONEY` | — | Falling honey |
| `LANDING_HONEY` | — | Landing honey |
| `FALLING_NECTAR` | — | Bee nectar falling |
| `FALLING_SPORE_BLOSSOM` | — | Spore blossom falling |
| `SPORE_BLOSSOM_AIR` | — | Spore blossom ambient |
| `ASH` | — | Basalt deltas ash |
| `CRIMSON_SPORE` | — | Crimson forest spore |
| `WARPED_SPORE` | — | Warped forest spore |
| `DRIPPING_OBSIDIAN_TEAR` | — | Crying obsidian tear |
| `FALLING_OBSIDIAN_TEAR` | — | Falling obsidian tear |
| `LANDING_OBSIDIAN_TEAR` | — | Landing obsidian tear |
| `REVERSE_PORTAL` | — | Reverse portal (respawn anchor) |
| `WHITE_ASH` | — | Soul sand valley ash |
| `SMALL_GUST` | — | Small wind gust |
| `GUST_EMITTER_LARGE` | — | Large gust emitter |
| `GUST_EMITTER_SMALL` | — | Small gust emitter |
| `TRIAL_SPAWNER_DETECTED_PLAYER` | — | Trial spawner activation |
| `TRIAL_SPAWNER_DETECTED_PLAYER_OMINOUS` | — | Ominous trial spawner |
| `VAULT_CONNECTION` | — | Vault connection beam |
| `INFESTED` | — | Infested block particles |
| `ITEM_COBWEB` | — | Cobweb item particles |
| `DUST` | `Particle.DustOptions` | Colored dust (redstone) |
| `DUST_COLOR_TRANSITION` | `Particle.DustTransition` | Dust color transition |
| `ITEM` | `ItemStack` | Item crack |
| `BLOCK` | `BlockData` | Block crack/break |
| `BLOCK_MARKER` | `BlockData` | Block marker (barrier, light) |
| `FALLING_DUST` | `BlockData` | Falling dust (sand, gravel) |
| `DUST_PILLAR` | `BlockData` | Dust pillar |
| `SCULK_CHARGE` | `Float` | Sculk charge angle |
| `SHRIEK` | `Integer` | Sculk shrieker delay |
| `VIBRATION` | `Vibration` | Sculk sensor vibration |
| `TRAIL` | `Trail` | Trail particle (breeze) |
| `LAVA` | — | Lava pop |
| `HEART` | — | Love hearts |
| `ITEM_SLIME` | — | Slime ball |
| `ITEM_SNOWBALL` | — | Snowball break |
| `CLOUD` | — | Cloud poof |
| `CAMPFIRE_COSY_SMOKE` | — | Campfire smoke |
| `CAMPFIRE_SIGNAL_SMOKE` | — | Tall campfire smoke |
| `SQUID_INK` | — | Squid ink |
| `GLOW_SQUID_INK` | — | Glow squid ink |
| `GLOW` | — | Glow particle |
| `WAX_ON` | — | Wax honeycomb on copper |
| `WAX_OFF` | — | Wax scrape off copper |
| `ELECTRIC_SPARK` | — | Lightning rod spark |
| `SCRAPE` | — | Axe scrape copper |
| `SONIC_BOOM` | — | Warden sonic boom |
| `CHERRY_LEAVES` | — | Cherry blossom falling |
| `EGG_CRACK` | — | Sniffer egg crack |
| `DUST_PLUME` | — | Brushing sand plume |
| `WHITE_SMOKE` | — | White smoke |
| `RAID_OMEN` | — | Raid omen |
| `TRIAL_OMEN` | — | Trial omen |
| `OMINOUS_SPAWNING` | — | Ominous spawner |
| `PALE_OAK_LEAVES` | — | Pale oak leaves |
| `TINTED_LEAVES` | `Color` | Tinted leaf particles |
| `FIREFLY` | — | Firefly particles (1.21.5+) |

### Particle Data Types

```java
// Particle.DustOptions — colored dust particle
Particle.DustOptions dustOpts = new Particle.DustOptions(Color.RED, 1.5f);
// Parameters: (Color color, float size)  — size 0.01–4.0

// Particle.DustTransition — color-changing dust
Particle.DustTransition transition = new Particle.DustTransition(
    Color.RED,    // fromColor
    Color.BLUE,   // toColor
    1.0f          // size
);

// Vibration — sculk vibration path
Vibration vibration = new Vibration(
    destination,  // Vibration.Destination
    arrivalTicks  // int
);
```

### World.spawnParticle() Method Variants

```java
// === org.bukkit.World particle methods ===

// Basic (no data, all players in range)
void spawnParticle(Particle particle, Location loc, int count);
void spawnParticle(Particle particle, double x, double y, double z, int count);

// With offset/speed (directional control)
void spawnParticle(Particle particle, Location loc, int count,
                   double offsetX, double offsetY, double offsetZ);
void spawnParticle(Particle particle, double x, double y, double z,
                   int count, double offsetX, double offsetY, double offsetZ);

// With offset/speed + extra (speed multiplier)
void spawnParticle(Particle particle, Location loc, int count,
                   double offsetX, double offsetY, double offsetZ, double extra);
void spawnParticle(Particle particle, double x, double y, double z,
                   int count, double offsetX, double offsetY, double offsetZ, double extra);

// With data parameter (for particles that require data)
<T> void spawnParticle(Particle particle, Location loc, int count, T data);
<T> void spawnParticle(Particle particle, double x, double y, double z, int count, T data);

// With data + offset/speed
<T> void spawnParticle(Particle particle, Location loc, int count,
                       double offsetX, double offsetY, double offsetZ, T data);
<T> void spawnParticle(Particle particle, double x, double y, double z,
                       int count, double offsetX, double offsetY, double offsetZ, T data);

// Full signature with data + offset + extra + force
<T> void spawnParticle(Particle particle, Location loc, int count,
                       double offsetX, double offsetY, double offsetZ,
                       double extra, T data);
<T> void spawnParticle(Particle particle, Location loc, int count,
                       double offsetX, double offsetY, double offsetZ,
                       double extra, T data, boolean force);
// force=true → visible beyond 32 blocks and ignores client particle settings

// Player-specific (only visible to one player)
// Use player.spawnParticle() with same signatures
```

### Parameter Guide

| Parameter | Description |
|---|---|
| `count` | Number of particles. If `0`, uses offset as direction vector with `extra` as speed |
| `offsetX/Y/Z` | Random spread range (or direction when count=0) |
| `extra` | Speed multiplier (default 1.0). For count=0, acts as speed along direction |
| `data` | Type-specific data (DustOptions, BlockData, ItemStack, etc.) |
| `force` | If true, sends to players >32 blocks away and bypasses client settings |

---

## 2. Potion Effects

### Class: `org.bukkit.potion.PotionEffect`

Represents a potion effect with type, duration, amplifier, and visual flags.

### Constructors

```java
// Full constructor
PotionEffect(
    PotionEffectType type,  // effect type
    int duration,           // ticks (-1 = infinite, Paper)
    int amplifier,          // level - 1 (0 = level I)
    boolean ambient,        // less visible particles (like beacon)
    boolean particles,      // show particles
    boolean icon            // show icon in HUD
);

// Without icon (defaults to particles value)
PotionEffect(PotionEffectType type, int duration, int amplifier,
             boolean ambient, boolean particles);

// Without visual flags (ambient=true, particles=true)
PotionEffect(PotionEffectType type, int duration, int amplifier, boolean ambient);

// Minimal (ambient=true, particles=true, icon=true)
PotionEffect(PotionEffectType type, int duration, int amplifier);

// From config map
PotionEffect(Map<String, Object> map);
```

### Key Constants

```java
// Infinite duration constant
public static final int INFINITE_DURATION = -1;
```

### Methods

| Return | Method | Description |
|---|---|---|
| `PotionEffectType` | `getType()` | The type of this effect |
| `int` | `getDuration()` | Duration in ticks (-1 = infinite) |
| `boolean` | `isInfinite()` | True if duration == -1 |
| `boolean` | `isShorterThan(int)` | True if duration shorter than given ticks |
| `int` | `getAmplifier()` | Amplifier (0 = level I, 1 = level II, etc.) |
| `boolean` | `isAmbient()` | Whether effect is ambient (beacon-sourced) |
| `boolean` | `hasParticles()` | Whether particles are shown |
| `boolean` | `hasIcon()` | Whether icon is shown in HUD |
| `Color` | `getColor()` | Deprecated — color of particle effect |
| `PotionEffect` | `withType(PotionEffectType)` | New effect with different type |
| `PotionEffect` | `withDuration(int)` | New effect with different duration |
| `PotionEffect` | `withAmplifier(int)` | New effect with different amplifier |
| `PotionEffect` | `withAmbient(boolean)` | New effect with different ambient flag |
| `PotionEffect` | `withParticles(boolean)` | New effect with different particles flag |
| `PotionEffect` | `withIcon(boolean)` | New effect with different icon flag |
| `boolean` | `apply(LivingEntity)` | Apply this effect to a LivingEntity |
| `Map<String,Object>` | `serialize()` | Serialize to Map for config storage |

### Applying Effects to Players

```java
// Via LivingEntity
player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 200, 1));

// Check active effects
boolean hasSpeed = player.hasPotionEffect(PotionEffectType.SPEED);
PotionEffect active = player.getPotionEffect(PotionEffectType.SPEED);
Collection<PotionEffect> all = player.getActivePotionEffects();

// Remove effects
player.removePotionEffect(PotionEffectType.SPEED);
player.clearActivePotionEffects(); // Paper-only: clears all
```

---

## 3. PotionEffectType — All Types

### Class: `org.bukkit.potion.PotionEffectType`

Every potion effect type available. Access via static fields.

| Field | Description | Max Level (Vanilla) |
|---|---|---|
| `SPEED` | Increases movement speed | II |
| `SLOWNESS` | Decreases movement speed | IV |
| `HASTE` | Faster mining speed | II |
| `MINING_FATIGUE` | Slower mining speed | III |
| `STRENGTH` | Increases melee damage | II |
| `INSTANT_HEALTH` | Heals instantly | II |
| `INSTANT_DAMAGE` | Deals instant damage | II |
| `JUMP_BOOST` | Higher jumping | II |
| `NAUSEA` | Screen wobble/portal effect | I |
| `REGENERATION` | Restores health over time | II |
| `RESISTANCE` | Reduces incoming damage | IV |
| `FIRE_RESISTANCE` | Immunity to fire/lava | I |
| `WATER_BREATHING` | Breathe underwater | I |
| `INVISIBILITY` | Invisible to entities | I |
| `BLINDNESS` | Limits vision range | I |
| `NIGHT_VISION` | See in the dark | I |
| `HUNGER` | Depletes food bar faster | III |
| `WEAKNESS` | Reduces melee damage | I |
| `POISON` | Damages over time (no kill) | II |
| `WITHER` | Damages over time (can kill) | II |
| `HEALTH_BOOST` | Additional max health | V |
| `ABSORPTION` | Yellow absorption hearts | IV |
| `SATURATION` | Restores food/saturation | I |
| `GLOWING` | Outline visible through walls | I |
| `LEVITATION` | Float upwards | I |
| `LUCK` | Better loot table rolls | I |
| `UNLUCK` | Worse loot table rolls | I |
| `SLOW_FALLING` | Fall slowly, no fall damage | I |
| `CONDUIT_POWER` | Underwater vision + mining | I |
| `DOLPHINS_GRACE` | Faster swimming | I |
| `BAD_OMEN` | Triggers raids in villages | V |
| `HERO_OF_THE_VILLAGE` | Trading discounts | V |
| `DARKNESS` | Pulsing darkness effect | I |
| `TRIAL_OMEN` | Trial omen (1.21+) | I |
| `RAID_OMEN` | Raid omen (1.21+) | I |
| `WIND_CHARGED` | Wind burst on death (1.21+) | I |
| `WEAVING` | Cobweb on death (1.21+) | I |
| `OOZING` | Slime spawn on death (1.21+) | I |
| `INFESTED` | Silverfish on hit (1.21+) | I |

### PotionEffectType Methods

| Return | Method | Description |
|---|---|---|
| `String` | `getName()` | Deprecated — internal name |
| `int` | `getId()` | Deprecated — numeric ID |
| `boolean` | `isInstant()` | True for INSTANT_HEALTH, INSTANT_DAMAGE |
| `Color` | `getColor()` | Default particle color |
| `double` | `getDurationModifier()` | Duration multiplier |
| `NamespacedKey` | `getKey()` | Namespaced key (e.g., `minecraft:speed`) |
| `String` | `translationKey()` | Client translation key |
| `Map<Attribute, AttributeModifier>` | `getEffectAttributes()` | Paper — attribute modifiers applied by this effect |
| `double` | `getAttributeModifierAmount(Attribute, int)` | Paper — modifier value at amplifier |
| `PotionEffectType.Category` | `getEffectCategory()` | Paper — BENEFICIAL, HARMFUL, or NEUTRAL |

### PotionEffectType.Category (Paper)

```java
PotionEffectType.Category.BENEFICIAL  // green in inventory
PotionEffectType.Category.HARMFUL     // red in inventory
PotionEffectType.Category.NEUTRAL     // blue in inventory
```

---

## 4. PotionType — Brewable Potions

### Enum: `org.bukkit.potion.PotionType`

All brewable potion variants. Each maps to a set of `PotionEffect`s.

| PotionType | Effect | Duration/Level |
|---|---|---|
| `WATER` | None | — |
| `MUNDANE` | None | — |
| `THICK` | None | — |
| `AWKWARD` | None | — |
| `NIGHT_VISION` | Night Vision I | 3:00 |
| `LONG_NIGHT_VISION` | Night Vision I | 8:00 |
| `INVISIBILITY` | Invisibility I | 3:00 |
| `LONG_INVISIBILITY` | Invisibility I | 8:00 |
| `LEAPING` | Jump Boost I | 3:00 |
| `LONG_LEAPING` | Jump Boost I | 8:00 |
| `STRONG_LEAPING` | Jump Boost II | 1:30 |
| `FIRE_RESISTANCE` | Fire Resistance I | 3:00 |
| `LONG_FIRE_RESISTANCE` | Fire Resistance I | 8:00 |
| `SWIFTNESS` | Speed I | 3:00 |
| `LONG_SWIFTNESS` | Speed I | 8:00 |
| `STRONG_SWIFTNESS` | Speed II | 1:30 |
| `SLOWNESS` | Slowness I | 1:30 |
| `LONG_SLOWNESS` | Slowness I | 4:00 |
| `STRONG_SLOWNESS` | Slowness IV | 0:20 |
| `WATER_BREATHING` | Water Breathing I | 3:00 |
| `LONG_WATER_BREATHING` | Water Breathing I | 8:00 |
| `HEALING` | Instant Health I | instant |
| `STRONG_HEALING` | Instant Health II | instant |
| `HARMING` | Instant Damage I | instant |
| `STRONG_HARMING` | Instant Damage II | instant |
| `POISON` | Poison I | 0:45 |
| `LONG_POISON` | Poison I | 1:30 |
| `STRONG_POISON` | Poison II | 0:21 |
| `REGENERATION` | Regeneration I | 0:45 |
| `LONG_REGENERATION` | Regeneration I | 1:30 |
| `STRONG_REGENERATION` | Regeneration II | 0:22 |
| `STRENGTH` | Strength I | 3:00 |
| `LONG_STRENGTH` | Strength I | 8:00 |
| `STRONG_STRENGTH` | Strength II | 1:30 |
| `WEAKNESS` | Weakness I | 1:30 |
| `LONG_WEAKNESS` | Weakness I | 4:00 |
| `LUCK` | Luck I | 5:00 |
| `TURTLE_MASTER` | Slowness IV + Resistance III | 0:20 |
| `LONG_TURTLE_MASTER` | Slowness IV + Resistance III | 0:40 |
| `STRONG_TURTLE_MASTER` | Slowness VI + Resistance IV | 0:20 |
| `SLOW_FALLING` | Slow Falling I | 1:30 |
| `LONG_SLOW_FALLING` | Slow Falling I | 4:00 |
| `WIND_CHARGED` | Wind Charged I | 3:00 |
| `WEAVING` | Weaving I | 3:00 |
| `OOZING` | Oozing I | 3:00 |
| `INFESTED` | Infested I | 3:00 |

### PotionType Methods

| Return | Method | Description |
|---|---|---|
| `PotionEffectType` | `getEffectType()` | Primary effect type (null for WATER, MUNDANE, etc.) |
| `List<PotionEffect>` | `getPotionEffects()` | All effects applied by this potion |
| `boolean` | `isInstant()` | True for instant potions (HEALING, HARMING) |
| `boolean` | `isUpgradeable()` | Deprecated — use specific STRONG_ variant |
| `boolean` | `isExtendable()` | Deprecated — use specific LONG_ variant |
| `int` | `getMaxLevel()` | Deprecated — max amplifier |
| `NamespacedKey` | `getKey()` | Namespaced key |

---

## 5. Enchantment API

### Class: `org.bukkit.enchantments.Enchantment`

Abstract class representing all enchantment types. Implements `Keyed`, `Translatable`.

### All Enchantment Constants

#### Armor Enchantments

| Field | Description | Max Level | Target |
|---|---|---|---|
| `PROTECTION` | Reduces environmental damage | 4 | Armor |
| `FIRE_PROTECTION` | Reduces fire damage | 4 | Armor |
| `FEATHER_FALLING` | Reduces fall damage | 4 | Boots |
| `BLAST_PROTECTION` | Reduces explosion damage | 4 | Armor |
| `PROJECTILE_PROTECTION` | Reduces projectile damage | 4 | Armor |
| `RESPIRATION` | Extends underwater breathing | 3 | Helmet |
| `AQUA_AFFINITY` | Removes underwater mining penalty | 1 | Helmet |
| `THORNS` | Damages attackers | 3 | Armor |
| `DEPTH_STRIDER` | Faster underwater movement | 3 | Boots |
| `FROST_WALKER` | Creates frosted ice | 2 | Boots |
| `SOUL_SPEED` | Faster on soul blocks | 3 | Boots |
| `SWIFT_SNEAK` | Faster sneaking | 3 | Leggings |

#### Weapon Enchantments

| Field | Description | Max Level | Target |
|---|---|---|---|
| `SHARPNESS` | Extra damage (all) | 5 | Sword/Axe |
| `SMITE` | Extra undead damage | 5 | Sword/Axe |
| `BANE_OF_ARTHROPODS` | Extra arthropod damage | 5 | Sword/Axe |
| `KNOCKBACK` | Extra knockback | 2 | Sword |
| `FIRE_ASPECT` | Lights target on fire | 2 | Sword |
| `LOOTING` | Extra mob drops | 3 | Sword |
| `SWEEPING_EDGE` | Extra sweep damage | 3 | Sword |

#### Tool Enchantments

| Field | Description | Max Level | Target |
|---|---|---|---|
| `EFFICIENCY` | Faster mining | 5 | Tools |
| `SILK_TOUCH` | Blocks drop themselves | 1 | Tools |
| `UNBREAKING` | Reduces durability loss | 3 | All |
| `FORTUNE` | Extra block drops | 3 | Tools |

#### Bow/Crossbow Enchantments

| Field | Description | Max Level | Target |
|---|---|---|---|
| `POWER` | Extra arrow damage | 5 | Bow |
| `PUNCH` | Arrow knockback | 2 | Bow |
| `FLAME` | Fire arrows | 1 | Bow |
| `INFINITY` | Infinite arrows | 1 | Bow |
| `MULTISHOT` | Three arrows at once | 1 | Crossbow |
| `QUICK_CHARGE` | Faster crossbow loading | 3 | Crossbow |
| `PIERCING` | Arrows pierce entities | 4 | Crossbow |

#### Trident Enchantments

| Field | Description | Max Level | Target |
|---|---|---|---|
| `LOYALTY` | Trident returns | 3 | Trident |
| `IMPALING` | Extra aquatic damage | 5 | Trident |
| `RIPTIDE` | Launch with trident | 3 | Trident |
| `CHANNELING` | Lightning on hit | 1 | Trident |

#### Mace Enchantments (1.21+)

| Field | Description | Max Level | Target |
|---|---|---|---|
| `DENSITY` | Extra fall damage | 5 | Mace |
| `BREACH` | Reduces armor effectiveness | 4 | Mace |
| `WIND_BURST` | Wind burst on hit | 3 | Mace |

#### Spear Enchantments (1.21.5+)

| Field | Description | Max Level | Target |
|---|---|---|---|
| `LUNGE` | Jab attack propels user into air | ? | Spear |

#### Special Enchantments

| Field | Description | Max Level | Target |
|---|---|---|---|
| `MENDING` | XP repairs items | 1 | All |
| `BINDING_CURSE` | Cannot remove item | 1 | Armor |
| `VANISHING_CURSE` | Item vanishes on death | 1 | All |
| `LUCK_OF_THE_SEA` | Better fishing loot | 3 | Fishing Rod |
| `LURE` | Faster fish biting | 3 | Fishing Rod |

### Enchantment Methods

```java
// === Instance Methods ===
abstract String getName();                      // Deprecated — use getKey()
abstract int getMaxLevel();                     // Max obtainable level
abstract int getStartLevel();                   // Min level (always 1)
abstract boolean isTreasure();                  // Treasure-only (no enchanting table)
abstract boolean isCursed();                    // Is a curse enchantment
abstract boolean conflictsWith(Enchantment);    // Check mutual exclusion
abstract boolean canEnchantItem(ItemStack);     // Can this go on this item?

// Paper-added methods
abstract Component displayName(int level);      // "Sharpness V" etc.
abstract Component description();               // Description component
abstract boolean isTradeable();                 // Obtainable from villager trades
abstract boolean isDiscoverable();              // Can appear in loot/tables
abstract int getAnvilCost();                    // Anvil XP cost multiplier
abstract int getMinModifiedCost(int level);     // Min enchanting table cost at level
abstract int getMaxModifiedCost(int level);     // Max enchanting table cost at level
abstract int getWeight();                       // Rarity weight (higher = more common)
abstract Set<EquipmentSlotGroup> getActiveSlotGroups(); // Where enchantment is active

// Registry key sets
abstract RegistryKeySet<ItemType> getSupportedItems();     // Items this can go on
abstract @Nullable RegistryKeySet<ItemType> getPrimaryItems(); // Items for enchanting table
abstract RegistryKeySet<Enchantment> getExclusiveWith();   // Mutually exclusive enchantments

// Translatable
String translationKey();

// === Deprecated Static Methods ===
static @Nullable Enchantment getByKey(NamespacedKey key);  // Use Registry.get() instead
static @Nullable Enchantment getByName(String name);       // Use getByKey()
static Enchantment[] values();                              // Use Registry iteration

// === Deprecated Instance Methods ===
abstract EnchantmentTarget getItemTarget();                // Use tags instead
Set<EquipmentSlot> getActiveSlots();                       // Use getActiveSlotGroups()
abstract float getDamageIncrease(int, EntityCategory);     // Complex effect system now
abstract float getDamageIncrease(int, EntityType);         // Complex effect system now
```

### Modern Enchantment Lookup (via Registry)

```java
// Preferred way to look up enchantments in 26.1
Registry<Enchantment> registry = RegistryAccess.registryAccess()
    .getRegistry(RegistryKey.ENCHANTMENT);

Enchantment sharpness = registry.get(NamespacedKey.minecraft("sharpness"));
Enchantment lunge = registry.get(NamespacedKey.minecraft("lunge")); // 1.21.5 new!

// Iterate all enchantments
for (Enchantment ench : registry) {
    Bukkit.getLogger().info(ench.getKey() + " max=" + ench.getMaxLevel());
}
```

---

## 6. EnchantmentRegistryEntry (Paper)

### Interface: `io.papermc.paper.registry.data.EnchantmentRegistryEntry`

Data-centric version-specific view of enchantment registration. Used with Paper's
`RegistryFreezeEvent` for modifying or registering enchantments.

**Annotation:** `@Experimental @NonExtendable`

### Methods

| Return | Method | Description |
|---|---|---|
| `Component` | `description()` | Display description (e.g., "Sharpness") |
| `RegistryKeySet<ItemType>` | `supportedItems()` | Items this enchantment supports |
| `@Nullable RegistryKeySet<ItemType>` | `primaryItems()` | Items for enchanting table (null → use supportedItems) |
| `@Range(1..1024) int` | `weight()` | Weighted random selection weight |
| `@Range(1..255) int` | `maxLevel()` | Maximum enchantment level |
| `EnchantmentCost` | `minimumCost()` | Minimum enchanting cost |
| `EnchantmentCost` | `maximumCost()` | Maximum enchanting cost |
| `@NonNegative int` | `anvilCost()` | Anvil application cost (halved for books) |
| `List<EquipmentSlotGroup>` | `activeSlots()` | Slots where enchantment is active |
| `RegistryKeySet<Enchantment>` | `exclusiveWith()` | Mutually exclusive enchantments |

### EnchantmentRegistryEntry.Builder

The mutable builder used in `RegistryFreezeEvent<Enchantment, EnchantmentRegistryEntry.Builder>` to
modify or create enchantments.

### EnchantmentCost Interface

```java
// Represents the cost formula: base + (perLevel * level)
interface EnchantmentRegistryEntry.EnchantmentCost {
    int baseCost();        // Base cost component
    int perLevelCost();    // Cost added per level
}
```

### Modifying Enchantments via Registry Events

```java
@Override
public void bootstrap(BootstrapContext<Enchantment> context) {
    // This uses Paper's lifecycle event system
}

// In plugin bootstrapper:
LifecycleEventManager<BootstrapContext> manager = context.getLifecycleManager();
manager.registerEventHandler(RegistryEvents.ENCHANTMENT.freeze(), event -> {
    // Modify existing enchantment
    event.registry().get(NamespacedKey.minecraft("sharpness"), builder -> {
        builder.maxLevel(10); // Allow Sharpness X
    });
});
```

---

## 7. AreaEffectCloud

### Interface: `org.bukkit.entity.AreaEffectCloud`

Extends `Entity`. Represents a lingering potion cloud that applies effects to entities in its radius.

### All Methods

| Return | Method | Description |
|---|---|---|
| `int` | `getDuration()` | Total lifetime in ticks |
| `void` | `setDuration(int)` | Set total lifetime in ticks |
| `int` | `getWaitTime()` | Ticks before first effect application |
| `void` | `setWaitTime(int)` | Set initial wait time |
| `int` | `getReapplicationDelay()` | Ticks of immunity after application |
| `void` | `setReapplicationDelay(int)` | Set reapplication immunity time |
| `int` | `getDurationOnUse()` | Duration reduction when applied |
| `void` | `setDurationOnUse(int)` | Set duration reduction per use |
| `float` | `getRadius()` | Current radius in blocks |
| `void` | `setRadius(float)` | Set radius |
| `float` | `getRadiusOnUse()` | Radius reduction when applied |
| `void` | `setRadiusOnUse(float)` | Set radius reduction per use |
| `float` | `getRadiusPerTick()` | Radius change per tick |
| `void` | `setRadiusPerTick(float)` | Set per-tick radius change |
| `Particle` | `getParticle()` | Visual particle type |
| `void` | `setParticle(Particle)` | Set particle type |
| `<T> void` | `setParticle(Particle, T data)` | Set particle with data |
| `void` | `setBasePotionType(@Nullable PotionType)` | Set base potion type |
| `@Nullable PotionType` | `getBasePotionType()` | Get base potion type |
| `boolean` | `hasCustomEffects()` | Has any custom effects |
| `List<PotionEffect>` | `getCustomEffects()` | Get all custom effects |
| `boolean` | `addCustomEffect(PotionEffect, boolean overwrite)` | Add custom effect |
| `boolean` | `removeCustomEffect(PotionEffectType)` | Remove by type |
| `boolean` | `hasCustomEffect(PotionEffectType)` | Check for type |
| `void` | `clearCustomEffects()` | Remove all custom effects |
| `Color` | `getColor()` | Cloud particle color |
| `void` | `setColor(@Nullable Color)` | Set cloud color |
| `@Nullable ProjectileSource` | `getSource()` | Original source entity |
| `void` | `setSource(@Nullable ProjectileSource)` | Set source entity |
| `UUID` | `getOwnerUniqueId()` | Owner entity UUID |
| `void` | `setOwnerUniqueId(UUID)` | Set owner UUID |

### Deprecated Methods

| Method | Replacement |
|---|---|
| `getBasePotionData()` | `getBasePotionType()` |
| `setBasePotionData(PotionData)` | `setBasePotionType(PotionType)` |

---

## 8. Code Examples

### Example 1: Particle Trail Behind Player

```java
import org.bukkit.Particle;
import org.bukkit.scheduler.BukkitRunnable;

public class ParticleTrailTask extends BukkitRunnable {
    private final Player player;

    public ParticleTrailTask(Player player) {
        this.player = player;
    }

    @Override
    public void run() {
        if (!player.isOnline()) { cancel(); return; }

        // Rainbow dust trail
        float hue = (System.currentTimeMillis() % 3000) / 3000f;
        java.awt.Color awtColor = java.awt.Color.getHSBColor(hue, 1f, 1f);
        Color color = Color.fromRGB(awtColor.getRed(), awtColor.getGreen(), awtColor.getBlue());

        player.getWorld().spawnParticle(
            Particle.DUST,
            player.getLocation().add(0, 0.5, 0),
            3,                              // count
            0.15, 0.15, 0.15,              // offset
            0,                              // extra/speed
            new Particle.DustOptions(color, 1.2f)
        );
    }
}

// Usage: new ParticleTrailTask(player).runTaskTimer(plugin, 0L, 1L);
```

### Example 2: Spiral Particle Effect

```java
public void spawnSpiral(Location center, Particle particle, int loops, double radius) {
    World world = center.getWorld();
    for (int i = 0; i < loops * 36; i++) {
        double angle = Math.toRadians(i * 10);
        double height = i * 0.05;
        double x = center.getX() + Math.cos(angle) * radius;
        double z = center.getZ() + Math.sin(angle) * radius;
        double y = center.getY() + height;

        world.spawnParticle(particle, x, y, z, 1, 0, 0, 0, 0);
    }
}
```

### Example 3: Custom Potion with Multiple Effects

```java
public ItemStack createSuperPotion() {
    ItemStack potion = new ItemStack(Material.POTION);
    PotionMeta meta = (PotionMeta) potion.getItemMeta();

    // Base type
    meta.setBasePotionType(PotionType.AWKWARD);

    // Add custom effects
    meta.addCustomEffect(new PotionEffect(
        PotionEffectType.SPEED, 600, 2,    // Speed III for 30s
        false, true, true
    ), true);

    meta.addCustomEffect(new PotionEffect(
        PotionEffectType.STRENGTH, 600, 1, // Strength II for 30s
        false, true, true
    ), true);

    meta.addCustomEffect(new PotionEffect(
        PotionEffectType.REGENERATION, 200, 0, // Regen I for 10s
        false, true, true
    ), true);

    // Custom color (gold)
    meta.setColor(Color.fromRGB(255, 200, 0));
    meta.displayName(Component.text("Super Warrior Potion", NamedTextColor.GOLD));

    potion.setItemMeta(meta);
    return potion;
}
```

### Example 4: Lingering Effect Area (Healing Zone)

```java
public void createHealingZone(Location loc, int durationTicks) {
    AreaEffectCloud cloud = loc.getWorld().spawn(loc, AreaEffectCloud.class, aec -> {
        aec.setParticle(Particle.HEART);
        aec.setRadius(4.0f);
        aec.setRadiusOnUse(-0.1f);          // Shrink slightly per use
        aec.setRadiusPerTick(-0.002f);       // Slowly shrink over time
        aec.setDuration(durationTicks);      // Total lifetime
        aec.setWaitTime(10);                 // 0.5s before first effect
        aec.setReapplicationDelay(40);       // 2s between re-applications
        aec.setColor(Color.fromRGB(255, 100, 100));

        aec.addCustomEffect(new PotionEffect(
            PotionEffectType.REGENERATION,
            100,   // 5 seconds per application
            1,     // Level II
            true, true, true
        ), true);
    });
}
```

### Example 5: Poison Gas Cloud (Damaging)

```java
public void createPoisonCloud(Location loc) {
    loc.getWorld().spawn(loc, AreaEffectCloud.class, cloud -> {
        cloud.setBasePotionType(PotionType.POISON);
        cloud.setParticle(Particle.ENTITY_EFFECT);
        cloud.setColor(Color.fromRGB(80, 160, 40));
        cloud.setRadius(3.0f);
        cloud.setDuration(600);              // 30 seconds
        cloud.setRadiusPerTick(-3.0f / 600); // Shrink to 0 over lifetime
        cloud.setReapplicationDelay(20);     // 1s between hits

        // Additional custom damage effect
        cloud.addCustomEffect(new PotionEffect(
            PotionEffectType.WITHER, 60, 0, false, true
        ), false);
    });
}
```

### Example 6: Enchanted Item Programmatically

```java
public ItemStack createBossSword() {
    ItemStack sword = new ItemStack(Material.NETHERITE_SWORD);
    ItemMeta meta = sword.getItemMeta();

    // Add enchantments (unsafe allows above-max levels)
    meta.addEnchant(Enchantment.SHARPNESS, 10, true);     // Sharpness X
    meta.addEnchant(Enchantment.FIRE_ASPECT, 3, true);    // Fire Aspect III
    meta.addEnchant(Enchantment.LOOTING, 5, true);        // Looting V
    meta.addEnchant(Enchantment.SWEEPING_EDGE, 5, true);  // Sweeping Edge V
    meta.addEnchant(Enchantment.UNBREAKING, 5, true);     // Unbreaking V
    meta.addEnchant(Enchantment.MENDING, 1, false);       // Mending

    meta.displayName(Component.text("Blade of the End", NamedTextColor.DARK_PURPLE)
        .decoration(TextDecoration.ITALIC, false));

    sword.setItemMeta(meta);
    return sword;
}
```

### Example 7: Check & Query Enchantments

```java
public void analyzeItem(ItemStack item) {
    if (!item.hasItemMeta()) return;
    ItemMeta meta = item.getItemMeta();

    // Get all enchantments
    Map<Enchantment, Integer> enchants = meta.getEnchants();
    for (Map.Entry<Enchantment, Integer> entry : enchants.entrySet()) {
        Enchantment ench = entry.getKey();
        int level = entry.getValue();

        Component name = ench.displayName(level);
        boolean treasure = ench.isTreasure();
        boolean curse = ench.isCursed();
        int maxLevel = ench.getMaxLevel();
        int weight = ench.getWeight();
        Set<EquipmentSlotGroup> slots = ench.getActiveSlotGroups();

        // Check conflicts
        boolean conflictsWithMending = ench.conflictsWith(Enchantment.MENDING);
        boolean canGoOnDiamond = ench.canEnchantItem(new ItemStack(Material.DIAMOND_SWORD));
    }
}
```

### Example 8: Dust Color Transition Particle

```java
public void spawnColorTransition(Location loc) {
    Particle.DustTransition transition = new Particle.DustTransition(
        Color.fromRGB(255, 0, 0),    // Start: red
        Color.fromRGB(0, 0, 255),    // End: blue
        1.5f                          // Size
    );

    loc.getWorld().spawnParticle(
        Particle.DUST_COLOR_TRANSITION,
        loc,
        20,                           // count
        0.5, 0.5, 0.5,              // offset
        0,                           // speed
        transition
    );
}
```

### Example 9: Block Crack Particles

```java
public void spawnBlockBreakEffect(Location loc, Material material) {
    BlockData blockData = material.createBlockData();
    loc.getWorld().spawnParticle(
        Particle.BLOCK,
        loc,
        50,
        0.3, 0.3, 0.3,
        0.1,
        blockData
    );
}
```

### Example 10: Directional Particle (count=0 Trick)

```java
// When count = 0, offset becomes direction, extra becomes speed
public void spawnDirectionalFlame(Location from, Vector direction) {
    from.getWorld().spawnParticle(
        Particle.FLAME,
        from,
        0,                                    // count=0 = directional mode
        direction.getX(),                     // direction X
        direction.getY(),                     // direction Y
        direction.getZ(),                     // direction Z
        0.1                                   // speed
    );
}
```

### Example 11: Infinite Potion Effect (Paper)

```java
// Paper supports -1 for infinite duration
player.addPotionEffect(new PotionEffect(
    PotionEffectType.NIGHT_VISION,
    PotionEffect.INFINITE_DURATION,   // -1
    0,
    false, false, true                // No particles, show icon
));
```

### Example 12: Particle Ring Around Entity

```java
public void spawnRing(Location center, double radius, Particle particle, int points) {
    World world = center.getWorld();
    for (int i = 0; i < points; i++) {
        double angle = 2 * Math.PI * i / points;
        double x = center.getX() + radius * Math.cos(angle);
        double z = center.getZ() + radius * Math.sin(angle);
        world.spawnParticle(particle, x, center.getY(), z, 1, 0, 0, 0, 0);
    }
}
```

### Example 13: AreaEffectCloud with Custom Particle Data

```java
public void createDustCloud(Location loc) {
    loc.getWorld().spawn(loc, AreaEffectCloud.class, cloud -> {
        cloud.setParticle(Particle.DUST,
            new Particle.DustOptions(Color.PURPLE, 2.0f));
        cloud.setRadius(5.0f);
        cloud.setDuration(200);
        cloud.setWaitTime(0);
        cloud.setRadiusPerTick(0);  // Don't shrink
        cloud.setColor(Color.PURPLE);

        // No potion effects — purely visual
    });
}
```

---

## Quick Reference: Common Patterns

### Particle Spawn Pattern

```java
// Simple spawn
world.spawnParticle(Particle.HEART, location, 5);

// With spread
world.spawnParticle(Particle.FLAME, location, 20, 0.5, 0.5, 0.5, 0.02);

// With data
world.spawnParticle(Particle.DUST, location, 10, 0, 0, 0, 0,
    new Particle.DustOptions(Color.RED, 1.0f));

// Force visibility (>32 blocks)
world.spawnParticle(Particle.EXPLOSION, location, 1, 0, 0, 0, 0, null, true);
```

### Potion Effect Pattern

```java
// Quick effect
player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 200, 1));

// Silent effect (no particles, no icon)
player.addPotionEffect(new PotionEffect(
    PotionEffectType.SPEED, 200, 1, false, false, false));

// Check & remove
if (player.hasPotionEffect(PotionEffectType.POISON)) {
    player.removePotionEffect(PotionEffectType.POISON);
}
```

### Enchantment Pattern

```java
// Add to item
meta.addEnchant(Enchantment.SHARPNESS, 5, false); // respect limits
meta.addEnchant(Enchantment.SHARPNESS, 10, true);  // unsafe (above max)

// Remove
meta.removeEnchant(Enchantment.SHARPNESS);

// Check
boolean has = meta.hasEnchant(Enchantment.SHARPNESS);
int level = meta.getEnchantLevel(Enchantment.SHARPNESS);
```

---

## Import Summary

```java
import org.bukkit.Particle;
import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.Location;
import org.bukkit.block.data.BlockData;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.AreaEffectCloud;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.inventory.EquipmentSlotGroup;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.potion.PotionType;
import io.papermc.paper.registry.data.EnchantmentRegistryEntry;
import io.papermc.paper.registry.set.RegistryKeySet;
import io.papermc.paper.registry.RegistryAccess;
import io.papermc.paper.registry.RegistryKey;
import net.kyori.adventure.text.Component;
```
