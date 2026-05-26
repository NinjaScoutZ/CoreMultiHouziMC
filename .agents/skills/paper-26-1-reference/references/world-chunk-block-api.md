# Paper 26.1.2 — World, Chunk & Block API Reference

> **API Version**: `paper-api 26.1.2.build.64-stable`
> **Javadoc**: https://jd.papermc.io/paper/26.1.2/

---

## Table of Contents
1. [World Management](#1-world-management)
2. [Chunk API](#2-chunk-api)
3. [Block API](#3-block-api)
4. [BlockData System](#4-blockdata-system)
5. [Location Utilities](#5-location-utilities)
6. [WorldCreator](#6-worldcreator)
7. [ChunkGenerator](#7-chunkgenerator)
8. [Paper World Events](#8-paper-world-events)
9. [Comprehensive Code Examples](#9-comprehensive-code-examples)

---

## 1. World Management

**Class**: `org.bukkit.World`
**Extends**: `RegionAccessor`, `WorldInfo`, `PluginMessageRecipient`, `Metadatable`, `PersistentDataHolder`, `Keyed`, `Audience`

### 1.1 Getting Worlds

```java
// From Bukkit server
List<World> worlds = Bukkit.getWorlds();
World overworld = Bukkit.getWorld("world");
World byKey    = Bukkit.getWorld(NamespacedKey.minecraft("the_nether"));
World byUUID   = Bukkit.getWorld(someUUID);
```

### 1.2 Key World Methods

| Method | Return | Description |
|--------|--------|-------------|
| `getName()` | `String` | World folder name |
| `getUID()` | `UUID` | Unique world identifier |
| `getKey()` | `NamespacedKey` | Namespaced key (Paper) |
| `getEnvironment()` | `World.Environment` | NORMAL, NETHER, THE_END, CUSTOM |
| `getSeed()` | `long` | World seed |
| `getDifficulty()` | `Difficulty` | PEACEFUL, EASY, NORMAL, HARD |
| `setDifficulty(Difficulty)` | `void` | Change difficulty |
| `getWorldType()` | `WorldType` | NORMAL, FLAT, LARGE_BIOMES, AMPLIFIED |
| `getWorldBorder()` | `WorldBorder` | World border accessor |
| `getWorldFolder()` | `File` | Filesystem path |
| `isHardcore()` | `boolean` | Hardcore mode flag |
| `setHardcore(boolean)` | `void` | Toggle hardcore |
| `getMinHeight()` | `int` | Lowest Y (typically -64) |
| `getMaxHeight()` | `int` | Max build height (typically 320) |
| `getLogicalHeight()` | `int` | Max logical height for gameplay |
| `getSeaLevel()` | `int` | Sea level Y |
| `isUltraWarm()` | `boolean` | Nether-like water evaporation |
| `isNatural()` | `boolean` | Compass/clock work normally |
| `hasCeiling()` | `boolean` | Has bedrock ceiling |
| `hasSkyLight()` | `boolean` | Has sky light |

### 1.3 Entity Methods

```java
// Spawning
Entity entity = world.spawnEntity(location, EntityType.ZOMBIE);
<T extends Entity> T spawn(Location loc, Class<T> clazz);
<T extends Entity> T spawn(Location loc, Class<T> clazz, Consumer<T> function);
<T extends Entity> T spawn(Location loc, Class<T> clazz, CreatureSpawnEvent.SpawnReason reason, Consumer<T> function);

// Querying
List<Entity> getEntities();
List<LivingEntity> getLivingEntities();
<T extends Entity> Collection<T> getEntitiesByClass(Class<T> cls);
Collection<Entity> getEntitiesByClasses(Class<?>... classes);
List<Entity> getNearbyEntities(Location loc, double x, double y, double z);
Collection<Entity> getNearbyEntities(BoundingBox bb);
Collection<Entity> getNearbyEntities(BoundingBox bb, Predicate<Entity> filter);
Collection<Entity> getNearbyEntities(Location loc, double dx, double dy, double dz, Predicate<Entity> filter);
Entity getEntity(UUID uuid);

// Players
List<Player> getPlayers();
int getPlayerCount(); // Paper - efficient player count
```

### 1.4 Block & Region Methods

```java
Block getBlockAt(int x, int y, int z);
Block getBlockAt(Location loc);
BlockState getBlockState(int x, int y, int z);   // Paper
BlockState getBlockState(Location loc);           // Paper
BlockData getBlockData(int x, int y, int z);      // Paper
BlockData getBlockData(Location loc);             // Paper
void setBlockData(int x, int y, int z, BlockData data);  // Paper
void setType(int x, int y, int z, Material type);        // Paper

int getHighestBlockYAt(int x, int z);
int getHighestBlockYAt(Location loc);
int getHighestBlockYAt(int x, int z, HeightMap heightMap);
Block getHighestBlockAt(int x, int z);
Block getHighestBlockAt(int x, int z, HeightMap heightMap);

Biome getBiome(int x, int y, int z);
void setBiome(int x, int y, int z, Biome biome);
```

### 1.5 Time & Weather

```java
// Time (ticks: 0=sunrise, 6000=noon, 12000=sunset, 18000=midnight)
long getTime();                    // Relative time (0–24000)
void setTime(long time);
long getFullTime();                // Absolute total time
void setFullTime(long time);
long getGameTime();                // Non-resettable game time
boolean isDayTime();               // Paper
boolean hasStorm();
void setStorm(boolean hasStorm);
int getWeatherDuration();
void setWeatherDuration(int ticks);
boolean isThundering();
void setThundering(boolean thundering);
int getThunderDuration();
void setThunderDuration(int ticks);
boolean isClearWeather();          // Paper
void setClearWeatherDuration(int ticks); // Paper
```

### 1.6 Explosions & Effects

```java
boolean createExplosion(double x, double y, double z, float power);
boolean createExplosion(double x, double y, double z, float power, boolean setFire);
boolean createExplosion(double x, double y, double z, float power, boolean setFire, boolean breakBlocks);
boolean createExplosion(double x, double y, double z, float power, boolean setFire, boolean breakBlocks, Entity source);
boolean createExplosion(Location loc, float power);
boolean createExplosion(Location loc, float power, boolean setFire);
boolean createExplosion(Location loc, float power, boolean setFire, boolean breakBlocks);

void playSound(Location loc, Sound sound, float volume, float pitch);
void playSound(Location loc, Sound sound, SoundCategory category, float volume, float pitch);
void playSound(Entity entity, Sound sound, float volume, float pitch);
void playEffect(Location loc, Effect effect, int data);
<T> void playEffect(Location loc, Effect effect, T data);
void spawnParticle(Particle particle, Location loc, int count);
<T> void spawnParticle(Particle particle, Location loc, int count, T data);
void spawnParticle(Particle particle, double x, double y, double z, int count, double offX, double offY, double offZ, double extra);

void strikeLightning(Location loc);
void strikeLightningEffect(Location loc);
```

### 1.7 Raycasting (Paper)

```java
RayTraceResult rayTraceEntities(Location start, Vector direction, double maxDistance);
RayTraceResult rayTraceEntities(Location start, Vector direction, double maxDistance, double raySize);
RayTraceResult rayTraceEntities(Location start, Vector direction, double maxDistance, double raySize, Predicate<Entity> filter);
RayTraceResult rayTraceBlocks(Location start, Vector direction, double maxDistance);
RayTraceResult rayTraceBlocks(Location start, Vector direction, double maxDistance, FluidCollisionMode fluidMode);
RayTraceResult rayTraceBlocks(Location start, Vector direction, double maxDistance, FluidCollisionMode fluidMode, boolean ignorePassableBlocks);
RayTraceResult rayTrace(Location start, Vector direction, double maxDistance, FluidCollisionMode fluidMode, boolean ignorePassableBlocks, double raySize, Predicate<Entity> filter);
```

### 1.8 Game Rules

```java
// Typed game rule API
<T> T getGameRuleValue(GameRule<T> rule);
<T> T getGameRuleDefault(GameRule<T> rule);
<T> boolean setGameRule(GameRule<T> rule, T value);
boolean isGameRule(String rule);
String[] getGameRules();

// Common game rules
GameRule<Boolean> DO_MOB_SPAWNING
GameRule<Boolean> KEEP_INVENTORY
GameRule<Boolean> MOB_GRIEFING
GameRule<Boolean> DO_FIRE_TICK
GameRule<Integer> RANDOM_TICK_SPEED
GameRule<Integer> MAX_COMMAND_CHAIN_LENGTH
GameRule<Integer> SPAWN_CHUNK_RADIUS
GameRule<Boolean> DO_IMMEDIATE_RESPAWN
```

### 1.9 Saving & Unloading

```java
void save();
boolean isAutoSave();
void setAutoSave(boolean value);
```

---

## 2. Chunk API

**Class**: `org.bukkit.Chunk`
**Implements**: `PersistentDataHolder`

### 2.1 Core Properties

| Method | Return | Description |
|--------|--------|-------------|
| `getX()` | `int` | Chunk X coordinate (block X >> 4) |
| `getZ()` | `int` | Chunk Z coordinate (block Z >> 4) |
| `getWorld()` | `World` | Owning world |
| `getChunkKey()` | `long` | Unique 64-bit packed key |
| `getChunkKey(int x, int z)` | `long` | **Static** helper to pack X/Z |

### 2.2 Loading & Unloading

```java
boolean isLoaded();
boolean load();
boolean load(boolean generate);
boolean unload();
boolean unload(boolean save);

// Force-loading (keeps chunk loaded across ticks)
boolean isForceLoaded();
void setForceLoaded(boolean forced);

// Ticket-based loading (Paper)
boolean addPluginChunkTicket(Plugin plugin);
boolean removePluginChunkTicket(Plugin plugin);
Collection<Plugin> getPluginChunkTickets();
```

### 2.3 Block Access

```java
Block getBlock(int x, int y, int z);  // x,z: 0–15 relative
BlockState[] getTileEntities();        // Block entities (chests, signs, etc.)
boolean contains(BlockData block);     // Check if chunk has this block data
```

### 2.4 Entity Access

```java
Entity[] getEntities();
```

### 2.5 Chunk Loading Helpers (World)

```java
// On World object
Chunk getChunkAt(int x, int z);
Chunk getChunkAt(Location loc);
Chunk getChunkAt(Block block);
boolean isChunkLoaded(Chunk chunk);
boolean isChunkLoaded(int x, int z);
boolean isChunkGenerated(int x, int z);
boolean isChunkInUse(int x, int z);  // Any players nearby
void loadChunk(int x, int z);
void loadChunk(int x, int z, boolean generate);
boolean unloadChunk(int x, int z);
boolean unloadChunk(int x, int z, boolean save);
boolean unloadChunkRequest(int x, int z);
Chunk[] getLoadedChunks();
Collection<Chunk> getForceLoadedChunks();

// Async loading (Paper)
CompletableFuture<Chunk> getChunkAtAsync(int x, int z);
CompletableFuture<Chunk> getChunkAtAsync(int x, int z, boolean gen);
CompletableFuture<Chunk> getChunkAtAsync(int x, int z, boolean gen, boolean urgent);
CompletableFuture<Chunk> getChunkAtAsync(Location loc);
CompletableFuture<Chunk> getChunkAtAsync(Block block);
void getChunkAtAsync(int x, int z, Consumer<Chunk> callback);
void getChunkAtAsync(int x, int z, boolean gen, Consumer<Chunk> callback);
```

### 2.6 Chunk Key Utilities

```java
// Static utility on Chunk
long chunkKey = Chunk.getChunkKey(chunkX, chunkZ);

// Extract back
int chunkX = (int) chunkKey;
int chunkZ = (int) (chunkKey >> 32);
```

---

## 3. Block API

**Class**: `org.bukkit.block.Block`
**Implements**: `Metadatable`, `Translatable`

### 3.1 Core Properties

| Method | Return | Description |
|--------|--------|-------------|
| `getType()` | `Material` | Block material |
| `getBlockData()` | `BlockData` | Full block data |
| `getState()` | `BlockState` | Snapshot of block state (chest, sign, etc.) |
| `getLocation()` | `Location` | World location |
| `getLocation(Location store)` | `Location` | Reuse existing Location (no alloc) |
| `getX()` | `int` | Block X |
| `getY()` | `int` | Block Y |
| `getZ()` | `int` | Block Z |
| `getWorld()` | `World` | Containing world |
| `getChunk()` | `Chunk` | Containing chunk |

### 3.2 Block Modification

```java
void setType(Material type);
void setType(Material type, boolean applyPhysics);  // false = no block update cascade
void setBlockData(BlockData data);
void setBlockData(BlockData data, boolean applyPhysics);

// Quick check
boolean isEmpty();
boolean isLiquid();
boolean isSolid();                        // Paper
boolean isCollidable();                   // Paper
boolean isPassable();
boolean isReplaceable();                  // Paper
boolean isBuildable();                    // Paper
```

### 3.3 Light Levels

```java
byte getLightLevel();            // Combined light
byte getLightFromSky();          // Sky light (0–15)
byte getLightFromBlocks();       // Block-emitted light
```

### 3.4 Relative Blocks & Faces

```java
Block getRelative(int modX, int modY, int modZ);
Block getRelative(BlockFace face);
Block getRelative(BlockFace face, int distance);
BlockFace getFace(Block other);     // Which face touches 'other'
```

**BlockFace enum values:**
`NORTH`, `SOUTH`, `EAST`, `WEST`, `UP`, `DOWN`,
`NORTH_EAST`, `NORTH_WEST`, `SOUTH_EAST`, `SOUTH_WEST`,
`WEST_NORTH_WEST`, `NORTH_NORTH_WEST`, `NORTH_NORTH_EAST`, `EAST_NORTH_EAST`,
`EAST_SOUTH_EAST`, `SOUTH_SOUTH_EAST`, `SOUTH_SOUTH_WEST`, `WEST_SOUTH_WEST`,
`SELF`

### 3.5 Physics & Interaction

```java
boolean breakNaturally();                              // Drop items, remove block
boolean breakNaturally(ItemStack tool);                // Use specific tool for drops
boolean breakNaturally(ItemStack tool, boolean triggerEffect);
boolean breakNaturally(ItemStack tool, boolean triggerEffect, boolean dropExperience);
Collection<ItemStack> getDrops();
Collection<ItemStack> getDrops(ItemStack tool);
Collection<ItemStack> getDrops(ItemStack tool, Entity entity);
boolean isPreferredTool(ItemStack tool);

boolean applyBoneMeal(BlockFace face);                 // Paper
boolean isBlockPowered();
boolean isBlockIndirectlyPowered();
boolean isBlockFacePowered(BlockFace face);
boolean isBlockFaceIndirectlyPowered(BlockFace face);
int getBlockPower();
int getBlockPower(BlockFace face);

double getDestroySpeed(ItemStack itemStack);           // Paper
double getDestroySpeed(ItemStack itemStack, boolean considerEnchants); // Paper
```

### 3.6 Biome & Temperature

```java
Biome getBiome();                         // Deprecated - use 3D biome
Biome getComputedBiome();                 // Paper - actual biome
void setBiome(Biome biome);
double getTemperature();
double getHumidity();
```

### 3.7 Bounding & Collision

```java
BoundingBox getBoundingBox();              // Paper
VoxelShape getCollisionShape();            // Paper
```

---

## 4. BlockData System

**Interface**: `org.bukkit.block.data.BlockData`
**Implements**: `Cloneable`

### 4.1 Creating & Parsing BlockData

```java
// From Material
BlockData data = Material.OAK_STAIRS.createBlockData();

// From string (Minecraft block state notation)
BlockData data = Bukkit.createBlockData("minecraft:oak_stairs[facing=north,half=top]");

// With consumer
BlockData data = Material.OAK_STAIRS.createBlockData(bd -> {
    ((Stairs) bd).setFacing(BlockFace.NORTH);
    ((Stairs) bd).setHalf(Bisected.Half.TOP);
});

// From server
BlockData data = Bukkit.createBlockData(Material.OAK_STAIRS);
BlockData data = Bukkit.createBlockData(Material.OAK_STAIRS, "[facing=north]");
```

### 4.2 Core Methods

| Method | Return | Description |
|--------|--------|-------------|
| `getMaterial()` | `Material` | Base material type |
| `getAsString()` | `String` | Full serialized form `minecraft:oak_stairs[facing=north,half=bottom,shape=straight,waterlogged=false]` |
| `getAsString(boolean hideUnspecified)` | `String` | Omit default properties |
| `merge(BlockData other)` | `BlockData` | Merge properties from another |
| `matches(BlockData other)` | `boolean` | Match specified properties |
| `clone()` | `BlockData` | Deep copy |
| `getSoundGroup()` | `SoundGroup` | Sound group for placing/breaking |
| `getLightEmission()` | `int` | Light emitted (0–15) |
| `isOccluding()` | `boolean` | Occludes light |
| `requiresCorrectToolForDrops()` | `boolean` | Needs correct tool to drop |
| `getPistonMoveReaction()` | `PistonMoveReaction` | MOVE, BREAK, BLOCK, IGNORE, PUSH_ONLY |
| `isSupported(Block block)` | `boolean` | Can exist at this location |
| `isSupported(Location loc)` | `boolean` | Can exist at this location |
| `isFaceSturdy(BlockFace face, BlockSupport support)` | `boolean` | Face supports given type |
| `getPlacementMaterial()` | `Material` | Material used to place this block |
| `isRandomlyTicked()` | `boolean` | Subject to random ticks |
| `isPreferredTool(ItemStack tool)` | `boolean` | Is this the correct tool? |
| `getDestroySpeed(ItemStack tool, boolean enchants)` | `float` | Mining speed |
| `copy()` | `BlockData` | Paper — immutable copy |
| `copyTo(int x, int y, int z, World world)` | `void` | Paper — place at coords |

### 4.3 Type Hierarchy (Specialized BlockData Interfaces)

BlockData has many sub-interfaces for blocks with special properties:

#### Directional
```java
// org.bukkit.block.data.Directional
BlockFace getFacing();
void setFacing(BlockFace facing);
Set<BlockFace> getFaces();     // Valid facing options
```

#### Rotatable
```java
// org.bukkit.block.data.Rotatable (signs, heads)
BlockFace getRotation();
void setRotation(BlockFace rotation);
```

#### Bisected
```java
// org.bukkit.block.data.Bisected (doors, tall plants)
enum Half { TOP, BOTTOM }
Half getHalf();
void setHalf(Half half);
```

#### Waterlogged
```java
// org.bukkit.block.data.Waterlogged
boolean isWaterlogged();
void setWaterlogged(boolean waterlogged);
```

#### MultipleFacing
```java
// org.bukkit.block.data.MultipleFacing (fences, glass panes, vines)
boolean hasFace(BlockFace face);
void setFace(BlockFace face, boolean has);
Set<BlockFace> getFaces();
Set<BlockFace> getAllowedFaces();
```

#### Powerable
```java
// org.bukkit.block.data.type.Switch, etc.
boolean isPowered();
void setPowered(boolean powered);
```

#### Ageable
```java
// org.bukkit.block.data.Ageable (crops, saplings)
int getAge();
void setAge(int age);
int getMaximumAge();
```

#### Levelled
```java
// org.bukkit.block.data.Levelled (water, cauldron, composter)
int getLevel();
void setLevel(int level);
int getMaximumLevel();
```

#### Lightable
```java
// org.bukkit.block.data.Lightable (candles, redstone ore)
boolean isLit();
void setLit(boolean lit);
```

#### Openable
```java
// org.bukkit.block.data.Openable (doors, trapdoors, fence gates)
boolean isOpen();
void setOpen(boolean open);
```

#### Snowable
```java
// org.bukkit.block.data.Snowable (grass block, podzol, mycelium)
boolean isSnowy();
void setSnowy(boolean snowy);
```

#### Stairs
```java
// org.bukkit.block.data.type.Stairs (extends Bisected, Directional, Waterlogged)
enum Shape { STRAIGHT, INNER_LEFT, INNER_RIGHT, OUTER_LEFT, OUTER_RIGHT }
Shape getShape();
void setShape(Shape shape);
```

#### Slab
```java
// org.bukkit.block.data.type.Slab (extends Waterlogged)
enum Type { TOP, BOTTOM, DOUBLE }
Type getSlabType();
void setType(Type type);
```

#### Bed
```java
// org.bukkit.block.data.type.Bed (extends Directional)
enum Part { HEAD, FOOT }
Part getPart();
void setPart(Part part);
boolean isOccupied();
```

---

## 5. Location Utilities

**Class**: `org.bukkit.Location`
**Implements**: `Cloneable`, `ConfigurationSerializable`, `io.papermc.paper.math.FinePosition`

### 5.1 Constructors

```java
Location(World world, double x, double y, double z);
Location(World world, double x, double y, double z, float yaw, float pitch);
```

### 5.2 Properties

| Method | Return | Description |
|--------|--------|-------------|
| `getWorld()` | `World` | World reference (nullable) |
| `setWorld(World)` | `void` | Change world |
| `isWorldLoaded()` | `boolean` | Is world loaded? |
| `getX()` / `getY()` / `getZ()` | `double` | Coordinates |
| `setX(double)` / `setY(double)` / `setZ(double)` | `void` | Set coords |
| `getBlockX()` / `getBlockY()` / `getBlockZ()` | `int` | Floor coordinates |
| `getYaw()` | `float` | Left-right rotation (0=south, 90=west) |
| `getPitch()` | `float` | Up-down rotation (-90=up, 90=down) |
| `setYaw(float)` / `setPitch(float)` | `void` | Set rotation |

### 5.3 Derived Values

```java
Vector getDirection();                     // Unit vector from yaw/pitch
Location setDirection(Vector direction);   // Yaw/pitch from vector
float length();                            // Distance from origin
float lengthSquared();                     // Squared (faster)
double distance(Location other);           // Euclidean distance
double distanceSquared(Location other);    // Squared (faster, no sqrt)
```

### 5.4 Arithmetic

```java
Location add(double x, double y, double z);
Location add(Location other);
Location add(Vector vector);
Location subtract(double x, double y, double z);
Location subtract(Location other);
Location subtract(Vector vector);
Location multiply(double factor);
Location zero();                           // Set all to 0

// Convert
Vector toVector();
Block getBlock();
Chunk getChunk();

// Paper position types
BlockPosition toBlockPosition();     // Paper
FinePosition toFinePosition();       // Paper
```

### 5.5 Clamping & Validation

```java
void checkFinite();  // Throws if NaN/Infinity
Location clone();
Location toHighestLocation();             // Paper - sets Y to highest block
Location toHighestLocation(HeightMap hm); // Paper - with height map
```

### 5.6 Serialization

```java
Map<String, Object> serialize();
static Location deserialize(Map<String, Object> args);
```

### 5.7 Useful Static Patterns

```java
// Calculate center of block
Location center = block.getLocation().add(0.5, 0.5, 0.5);

// Check same world
boolean sameWorld = loc1.getWorld().equals(loc2.getWorld());

// Normalized 2D distance (ignoring Y)
double dist2D = Math.sqrt(
    Math.pow(loc1.getX() - loc2.getX(), 2) +
    Math.pow(loc1.getZ() - loc2.getZ(), 2)
);
```

---

## 6. WorldCreator

**Class**: `org.bukkit.WorldCreator`

### 6.1 Constructor & Factory

```java
WorldCreator creator = new WorldCreator("my_world");
WorldCreator creator = WorldCreator.name("my_world");
WorldCreator copy = new WorldCreator("copy_world").copy(existingWorld);
```

### 6.2 Configuration Methods (Builder Pattern)

| Method | Parameter | Description |
|--------|-----------|-------------|
| `environment(Environment)` | `NORMAL`, `NETHER`, `THE_END`, `CUSTOM` | Dimension type |
| `seed(long)` | seed value | World seed |
| `type(WorldType)` | `NORMAL`, `FLAT`, `LARGE_BIOMES`, `AMPLIFIED` | World generator type |
| `generator(ChunkGenerator)` | custom generator | Custom chunk generation |
| `generator(String)` | plugin generator name | Named generator from plugin |
| `generatorSettings(String)` | JSON | Flat world preset or custom settings |
| `generateStructures(boolean)` | true/false | Enable structure generation |
| `hardcore(boolean)` | true/false | Hardcore mode |
| `keepSpawnLoaded(TriState)` | TRUE/FALSE/NOT_SET | Paper: keep spawn chunks loaded |
| `biomeProvider(BiomeProvider)` | custom provider | Paper: custom biome placement |
| `biomeProvider(String)` | plugin provider name | Paper: named biome provider |

### 6.3 Creating the World

```java
World world = creator.createWorld();  // Loads or generates
// Returns null if creation cancelled by WorldInitEvent
```

### 6.4 Complete Example

```java
World voidWorld = new WorldCreator("arena_void")
    .environment(World.Environment.NORMAL)
    .type(WorldType.FLAT)
    .generatorSettings("{\"layers\":[],\"biome\":\"minecraft:plains\"}")
    .generateStructures(false)
    .keepSpawnLoaded(TriState.FALSE)
    .createWorld();

// Set game rules
voidWorld.setGameRule(GameRule.DO_MOB_SPAWNING, false);
voidWorld.setGameRule(GameRule.DO_DAYLIGHT_CYCLE, false);
voidWorld.setGameRule(GameRule.DO_WEATHER_CYCLE, false);
voidWorld.setDifficulty(Difficulty.PEACEFUL);
voidWorld.setTime(6000); // Noon
```

---

## 7. ChunkGenerator

**Class**: `org.bukkit.generator.ChunkGenerator`

### 7.1 Key Methods to Override

```java
public class VoidGenerator extends ChunkGenerator {

    // Called for each chunk during generation
    @Override
    public void generateNoise(@NotNull WorldInfo worldInfo,
                               @NotNull Random random,
                               int chunkX, int chunkZ,
                               @NotNull ChunkData chunkData) {
        // Don't generate anything = void world
    }

    // Surface modifications after noise
    @Override
    public void generateSurface(@NotNull WorldInfo worldInfo,
                                 @NotNull Random random,
                                 int chunkX, int chunkZ,
                                 @NotNull ChunkData chunkData) {
    }

    // Bedrock generation
    @Override
    public void generateBedrock(@NotNull WorldInfo worldInfo,
                                 @NotNull Random random,
                                 int chunkX, int chunkZ,
                                 @NotNull ChunkData chunkData) {
    }

    // Carve caves
    @Override
    public void generateCaves(@NotNull WorldInfo worldInfo,
                               @NotNull Random random,
                               int chunkX, int chunkZ,
                               @NotNull ChunkData chunkData) {
    }

    // Fixed spawn location
    @Override
    public Location getFixedSpawnLocation(@NotNull World world,
                                           @NotNull Random random) {
        return new Location(world, 0, 65, 0);
    }

    // Whether vanilla decoration runs
    @Override
    public boolean shouldGenerateNoise() { return false; }

    @Override
    public boolean shouldGenerateSurface() { return false; }

    @Override
    public boolean shouldGenerateBedrock() { return false; }

    @Override
    public boolean shouldGenerateCaves() { return false; }

    @Override
    public boolean shouldGenerateDecorations() { return false; }

    @Override
    public boolean shouldGenerateMobs() { return false; }

    @Override
    public boolean shouldGenerateStructures() { return false; }
}
```

### 7.2 ChunkData API

```java
// Available inside generate methods
int getMinHeight();
int getMaxHeight();
Biome getBiome(int x, int y, int z);
void setBlock(int x, int y, int z, Material material);
void setBlock(int x, int y, int z, BlockData blockData);
Material getType(int x, int y, int z);
BlockData getBlockData(int x, int y, int z);
void setRegion(int xMin, int yMin, int zMin, int xMax, int yMax, int zMax, Material material);
void setRegion(int xMin, int yMin, int zMin, int xMax, int yMax, int zMax, BlockData data);
```

### 7.3 Platform Generator Example

```java
public class PlatformGenerator extends ChunkGenerator {
    @Override
    public void generateNoise(WorldInfo worldInfo, Random random,
                               int chunkX, int chunkZ, ChunkData chunkData) {
        if (chunkX == 0 && chunkZ == 0) {
            // 16x16 stone platform at Y=64
            chunkData.setRegion(0, 64, 0, 16, 65, 16, Material.STONE);
            // Grass on top
            chunkData.setRegion(0, 65, 0, 16, 66, 16, Material.GRASS_BLOCK);
        }
    }

    @Override
    public Location getFixedSpawnLocation(World world, Random random) {
        return new Location(world, 8, 66, 8);
    }

    @Override
    public boolean shouldGenerateNoise() { return false; }
    @Override
    public boolean shouldGenerateSurface() { return false; }
    @Override
    public boolean shouldGenerateBedrock() { return false; }
    @Override
    public boolean shouldGenerateCaves() { return false; }
    @Override
    public boolean shouldGenerateDecorations() { return false; }
    @Override
    public boolean shouldGenerateMobs() { return false; }
    @Override
    public boolean shouldGenerateStructures() { return false; }
}
```

---

## 8. Paper World Events

**Package**: `io.papermc.paper.event.world`

### 8.1 Event List

| Event | Description | Cancellable |
|-------|-------------|-------------|
| `StructuresLocateEvent` | Fired **before** `/locate structure` or eye of ender search | ✅ Yes |
| `StructuresLocateEvent.Result` | Record holding result position and structure | — |
| `WorldDifficultyChangeEvent` | Difficulty changed by command or API | ✅ Yes |
| `WorldGameRuleChangeEvent` | Game rule changed by command, menu, or API | ✅ Yes |

### 8.2 Paper World Border Events

**Package**: `io.papermc.paper.event.world.border`

| Event | Description | Cancellable |
|-------|-------------|-------------|
| `WorldBorderBoundsChangeEvent` | Border size changing | ✅ Yes |
| `WorldBorderBoundsChangeFinishEvent` | Border finished changing | ❌ No |
| `WorldBorderCenterChangeEvent` | Border center moved | ✅ Yes |

### 8.3 Standard Bukkit World Events

| Event | Description | Cancellable |
|-------|-------------|-------------|
| `WorldInitEvent` | World initialized (before chunks loaded) | ❌ |
| `WorldLoadEvent` | World fully loaded | ❌ |
| `WorldUnloadEvent` | World unloading | ✅ |
| `WorldSaveEvent` | World saving | ❌ |
| `ChunkLoadEvent` | Chunk loaded | ❌ |
| `ChunkUnloadEvent` | Chunk unloading | ❌ |
| `ChunkPopulateEvent` | Chunk populated with ores/trees | ❌ |
| `SpawnChangeEvent` | World spawn location changed | ❌ |
| `PortalCreateEvent` | Portal created | ✅ |
| `BlockPhysicsEvent` | Block physics update | ✅ |
| `BlockFromToEvent` | Liquid flow / dragon egg teleport | ✅ |

### 8.4 Usage Examples

```java
@EventHandler
public void onStructureLocate(StructuresLocateEvent event) {
    // Redirect all structure searches to a fixed location
    event.setResult(new StructuresLocateEvent.Result(
        new BlockPosition(100, 64, 100),
        event.getConfiguredStructures().iterator().next()
    ));
}

@EventHandler
public void onGameRuleChange(WorldGameRuleChangeEvent event) {
    if (event.getGameRule() == GameRule.KEEP_INVENTORY) {
        event.setCancelled(true); // Prevent changing keepInventory
    }
}

@EventHandler
public void onDifficultyChange(WorldDifficultyChangeEvent event) {
    Difficulty oldDiff = event.getOldDifficulty();
    Difficulty newDiff = event.getNewDifficulty();
    Bukkit.getLogger().info("Difficulty: " + oldDiff + " -> " + newDiff);
}
```

---

## 9. Comprehensive Code Examples

### 9.1 Arena World Setup (Full Pattern)

```java
public class ArenaWorldManager {

    private final JavaPlugin plugin;

    public ArenaWorldManager(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    public World createArenaWorld(String name) {
        // Delete existing if present
        World existing = Bukkit.getWorld(name);
        if (existing != null) {
            Bukkit.unloadWorld(existing, false);
            deleteWorldFolder(existing.getWorldFolder());
        }

        // Create void world
        World arena = new WorldCreator(name)
            .environment(World.Environment.NORMAL)
            .generator(new VoidChunkGenerator())
            .generateStructures(false)
            .keepSpawnLoaded(TriState.FALSE)
            .createWorld();

        if (arena == null) return null;

        // Configure
        arena.setGameRule(GameRule.DO_MOB_SPAWNING, false);
        arena.setGameRule(GameRule.DO_DAYLIGHT_CYCLE, false);
        arena.setGameRule(GameRule.DO_WEATHER_CYCLE, false);
        arena.setGameRule(GameRule.ANNOUNCE_ADVANCEMENTS, false);
        arena.setGameRule(GameRule.DO_FIRE_TICK, false);
        arena.setDifficulty(Difficulty.NORMAL);
        arena.setTime(6000);
        arena.setPVP(true);
        arena.setAutoSave(false);

        return arena;
    }

    public void deleteArenaWorld(String name) {
        World world = Bukkit.getWorld(name);
        if (world != null) {
            // Evacuate players
            World fallback = Bukkit.getWorlds().get(0);
            for (Player p : world.getPlayers()) {
                p.teleport(fallback.getSpawnLocation());
            }
            Bukkit.unloadWorld(world, false);
            deleteWorldFolder(world.getWorldFolder());
        }
    }

    private void deleteWorldFolder(File folder) {
        if (folder.exists()) {
            File[] files = folder.listFiles();
            if (files != null) {
                for (File f : files) {
                    if (f.isDirectory()) deleteWorldFolder(f);
                    else f.delete();
                }
            }
            folder.delete();
        }
    }
}
```

### 9.2 Schematic-Style Block Paste (Batch Blocks)

```java
public void pasteBlocks(Location origin, Map<BlockVector, BlockData> blocks) {
    World world = origin.getWorld();

    for (Map.Entry<BlockVector, BlockData> entry : blocks.entrySet()) {
        BlockVector offset = entry.getKey();
        BlockData data = entry.getValue();

        int x = origin.getBlockX() + offset.getBlockX();
        int y = origin.getBlockY() + offset.getBlockY();
        int z = origin.getBlockZ() + offset.getBlockZ();

        // Use setBlockData with applyPhysics=false for performance
        Block block = world.getBlockAt(x, y, z);
        block.setBlockData(data, false);
    }
}
```

### 9.3 BlockData Manipulation (Stairs + Waterlogging)

```java
public void placeWaterloggedStairs(Location loc, BlockFace facing) {
    BlockData data = Material.DARK_OAK_STAIRS.createBlockData(bd -> {
        Stairs stairs = (Stairs) bd;
        stairs.setFacing(facing);
        stairs.setHalf(Bisected.Half.BOTTOM);
        stairs.setShape(Stairs.Shape.STRAIGHT);
        stairs.setWaterlogged(true);
    });
    loc.getBlock().setBlockData(data);
}
```

### 9.4 Async Chunk Loading (Paper)

```java
public void teleportSafely(Player player, Location target) {
    target.getWorld().getChunkAtAsync(target).thenAccept(chunk -> {
        // Chunk is now loaded — safe to teleport on main thread
        Bukkit.getScheduler().runTask(plugin, () -> {
            Location safe = target.toHighestLocation().add(0, 1, 0);
            player.teleport(safe);
        });
    });
}
```

### 9.5 Raycasting (Line of Sight Check)

```java
public boolean hasLineOfSight(Location from, Location to, double maxDist) {
    World world = from.getWorld();
    Vector direction = to.toVector().subtract(from.toVector()).normalize();
    double distance = Math.min(from.distance(to), maxDist);

    RayTraceResult result = world.rayTraceBlocks(
        from, direction, distance,
        FluidCollisionMode.NEVER,
        true  // ignore passable blocks
    );
    return result == null; // null = no block hit = clear line
}
```

### 9.6 Custom Flat World via WorldCreator

```java
// Classic flat world: 1 layer bedrock, 3 layers dirt, 1 grass
World flat = new WorldCreator("flat_arena")
    .type(WorldType.FLAT)
    .generatorSettings("""
        {
            "layers": [
                {"block": "minecraft:bedrock", "height": 1},
                {"block": "minecraft:dirt", "height": 3},
                {"block": "minecraft:grass_block", "height": 1}
            ],
            "biome": "minecraft:plains",
            "features": false,
            "structures": { "structures": {} }
        }
        """)
    .generateStructures(false)
    .createWorld();
```

### 9.7 Force-Load Chunks for Redstone Contraption

```java
public void forceLoadArea(Location center, int radiusChunks) {
    World world = center.getWorld();
    int cx = center.getBlockX() >> 4;
    int cz = center.getBlockZ() >> 4;

    for (int x = cx - radiusChunks; x <= cx + radiusChunks; x++) {
        for (int z = cz - radiusChunks; z <= cz + radiusChunks; z++) {
            world.getChunkAt(x, z).setForceLoaded(true);
            world.getChunkAt(x, z).addPluginChunkTicket(plugin);
        }
    }
}

public void unforceLoadArea(Location center, int radiusChunks) {
    World world = center.getWorld();
    int cx = center.getBlockX() >> 4;
    int cz = center.getBlockZ() >> 4;

    for (int x = cx - radiusChunks; x <= cx + radiusChunks; x++) {
        for (int z = cz - radiusChunks; z <= cz + radiusChunks; z++) {
            world.getChunkAt(x, z).setForceLoaded(false);
            world.getChunkAt(x, z).removePluginChunkTicket(plugin);
        }
    }
}
```

### 9.8 Fill Region with BlockData

```java
public void fillRegion(Location corner1, Location corner2, BlockData data) {
    World world = corner1.getWorld();
    int minX = Math.min(corner1.getBlockX(), corner2.getBlockX());
    int minY = Math.min(corner1.getBlockY(), corner2.getBlockY());
    int minZ = Math.min(corner1.getBlockZ(), corner2.getBlockZ());
    int maxX = Math.max(corner1.getBlockX(), corner2.getBlockX());
    int maxY = Math.max(corner1.getBlockY(), corner2.getBlockY());
    int maxZ = Math.max(corner1.getBlockZ(), corner2.getBlockZ());

    for (int x = minX; x <= maxX; x++) {
        for (int y = minY; y <= maxY; y++) {
            for (int z = minZ; z <= maxZ; z++) {
                world.getBlockAt(x, y, z).setBlockData(data, false);
            }
        }
    }
}
```

---

## Quick Reference: HeightMap enum

| Value | Description |
|-------|-------------|
| `MOTION_BLOCKING` | Highest block that blocks movement or fluid |
| `MOTION_BLOCKING_NO_LEAVES` | Same but ignores leaves |
| `OCEAN_FLOOR` | Highest non-fluid solid block |
| `OCEAN_FLOOR_WG` | World generation version |
| `WORLD_SURFACE` | Highest non-air block |
| `WORLD_SURFACE_WG` | World generation version |

## Quick Reference: World.Environment

| Value | Description |
|-------|-------------|
| `NORMAL` | Overworld |
| `NETHER` | The Nether |
| `THE_END` | The End |
| `CUSTOM` | Custom dimension |

## Quick Reference: FluidCollisionMode

| Value | Description |
|-------|-------------|
| `NEVER` | Ignore all fluids |
| `SOURCE_ONLY` | Only collide with source blocks |
| `ALWAYS` | Collide with all fluid blocks |
