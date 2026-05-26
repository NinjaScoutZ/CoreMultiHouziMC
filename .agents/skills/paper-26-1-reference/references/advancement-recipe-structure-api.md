# Paper 26.1.2 — Advancement, Recipe & Structure API Reference

> **API Version:** Paper 26.1.2 (Minecraft 1.21.5 "Tiny Takeover")
> **Javadoc Base:** `https://jd.papermc.io/paper/26.1.2/`

---

## Table of Contents

1. [Advancement System](#1-advancement-system)
   - [Advancement Interface](#11-advancement-interface)
   - [AdvancementProgress Interface](#12-advancementprogress-interface)
   - [AdvancementDisplay (Paper)](#13-advancementdisplay-paper)
   - [Player Advancement Methods](#14-player-advancement-methods)
2. [Recipe System](#2-recipe-system)
   - [Recipe Interface Hierarchy](#21-recipe-interface-hierarchy)
   - [RecipeChoice](#22-recipechoice)
   - [ShapedRecipe](#23-shapedrecipe)
   - [ShapelessRecipe](#24-shapelessrecipe)
   - [FurnaceRecipe](#25-furnacerecipe)
   - [BlastingRecipe](#26-blastingrecipe)
   - [SmokingRecipe](#27-smokingrecipe)
   - [CampfireRecipe](#28-campfirerecipe)
   - [SmithingTransformRecipe](#29-smithingtransformrecipe)
   - [StonecuttingRecipe](#210-stonecuttingrecipe)
   - [CraftingRecipe (Base Class)](#211-craftingrecipe-base-class)
   - [Registering & Removing Recipes at Runtime](#212-registering--removing-recipes-at-runtime)
3. [Structure System](#3-structure-system)
   - [Structure Class](#31-structure-class)
   - [StructureType Class](#32-structuretype-class)
   - [Locating Structures](#33-locating-structures)
   - [StructuresLocateEvent (Paper)](#34-structureslocateevent-paper)
4. [Code Examples](#4-code-examples)

---

## 1. Advancement System

### 1.1 Advancement Interface

**Package:** `org.bukkit.advancement`
**Extends:** `org.bukkit.Keyed`

Represents an advancement that may be awarded to a player. **Not reference-safe** — the underlying advancement may be reloaded.

#### Methods

| Return Type | Method | Description |
|---|---|---|
| `@NotNull Collection<String>` | `getCriteria()` | Get all criteria present in this advancement (unmodifiable copy) |
| `@NotNull AdvancementRequirements` | `getRequirements()` | Returns the requirements for this advancement |
| `@Nullable AdvancementDisplay` | `getDisplay()` | Get the display info (null when hidden, e.g. crafting recipes) |
| `@NotNull Component` | `displayName()` | Gets the formatted display name (chat component for completion messages) |
| `@Nullable Advancement` | `getParent()` | Gets the parent advancement, if any |
| `@NotNull @Unmodifiable Collection<Advancement>` | `getChildren()` | Gets all direct children advancements |
| `@NotNull Advancement` | `getRoot()` | Gets the root advancement of the tree |
| `@NotNull NamespacedKey` | `getKey()` | *(Inherited from Keyed)* Returns the namespaced key |
| `@NotNull Key` | `key()` | *(Inherited from Keyed)* Returns the Adventure key |

#### Retrieving Advancements

```java
// By NamespacedKey
Advancement adv = Bukkit.getAdvancement(NamespacedKey.minecraft("story/mine_stone"));

// Iterate all advancements
Iterator<Advancement> it = Bukkit.advancementIterator();
while (it.hasNext()) {
    Advancement a = it.next();
    getLogger().info("Advancement: " + a.getKey());
}
```

---

### 1.2 AdvancementProgress Interface

**Package:** `org.bukkit.advancement`

The individual status of an advancement for a player. **Not reference-safe.**

#### Methods

| Return Type | Method | Description |
|---|---|---|
| `@NotNull Advancement` | `getAdvancement()` | The advancement this progress is concerning |
| `boolean` | `isDone()` | Check if all criteria have been met |
| `boolean` | `awardCriteria(@NotNull String criteria)` | Mark the specified criteria as awarded at current time. Returns `true` if awarded, `false` if criteria doesn't exist or already awarded |
| `boolean` | `revokeCriteria(@NotNull String criteria)` | Mark the specified criteria as uncompleted. Returns `true` if removed, `false` if criteria doesn't exist or not awarded |
| `@Nullable Date` | `getDateAwarded(@NotNull String criteria)` | Get the date the specified criteria was awarded. Returns `null` if unawarded or criteria doesn't exist |
| `@NotNull Collection<String>` | `getRemainingCriteria()` | Get criteria which have not been awarded (unmodifiable copy) |
| `@NotNull Collection<String>` | `getAwardedCriteria()` | Gets criteria which have been awarded (unmodifiable copy) |

---

### 1.3 AdvancementDisplay (Paper)

**Package:** `io.papermc.paper.advancement`

Paper's interface for reading advancement display properties (read-only).

#### Key Methods

| Return Type | Method | Description |
|---|---|---|
| `@NotNull Component` | `title()` | The title component |
| `@NotNull Component` | `description()` | The description component |
| `@NotNull Component` | `displayName()` | The formatted display name (used in chat) |
| `@NotNull ItemStack` | `icon()` | The icon item |
| `@NotNull Frame` | `frame()` | The frame type (`TASK`, `CHALLENGE`, `GOAL`) |
| `boolean` | `doesShowToast()` | Whether a toast notification shows |
| `boolean` | `doesAnnounceToChat()` | Whether completion is announced in chat |
| `boolean` | `isHidden()` | Whether this advancement is hidden until achieved |
| `@Nullable String` | `backgroundPath()` | The background texture path for root advancements |

#### Frame Enum (`AdvancementDisplay.Frame`)

| Value | Description |
|---|---|
| `TASK` | Standard advancement (square frame) |
| `CHALLENGE` | Challenge advancement (spiked frame) |
| `GOAL` | Goal advancement (round frame) |

---

### 1.4 Player Advancement Methods

**On `Player` (extends `Entity` → `OfflinePlayer`):**

```java
// Get progress for an advancement
AdvancementProgress progress = player.getAdvancementProgress(advancement);

// Check if completed
boolean done = progress.isDone();

// Award all criteria at once
for (String criteria : progress.getRemainingCriteria()) {
    progress.awardCriteria(criteria);
}

// Revoke all criteria
for (String criteria : progress.getAwardedCriteria()) {
    progress.revokeCriteria(criteria);
}
```

---

## 2. Recipe System

### 2.1 Recipe Interface Hierarchy

```
Recipe (interface)
├── CraftingRecipe (abstract class)
│   ├── ShapedRecipe
│   └── ShapelessRecipe
├── CookingRecipe<T> (abstract class)
│   ├── FurnaceRecipe
│   ├── BlastingRecipe
│   ├── SmokingRecipe
│   └── CampfireRecipe
├── SmithingRecipe (abstract class)
│   ├── SmithingTransformRecipe
│   └── SmithingTrimRecipe
├── StonecuttingRecipe
├── ComplexRecipe (server-internal, e.g. map cloning)
└── TransmuteRecipe
```

All custom recipes require a `NamespacedKey` for unique identification.

---

### 2.2 RecipeChoice

**Package:** `org.bukkit.inventory`

`RecipeChoice` is the modern way to specify recipe ingredients. There are several implementations:

#### RecipeChoice.MaterialChoice

Accepts any of a list of `Material` types (ignores item metadata).

```java
// Single material
RecipeChoice choice = new RecipeChoice.MaterialChoice(Material.IRON_INGOT);

// Multiple materials (tag-like)
RecipeChoice choice = new RecipeChoice.MaterialChoice(
    Material.OAK_PLANKS, Material.SPRUCE_PLANKS,
    Material.BIRCH_PLANKS, Material.JUNGLE_PLANKS
);

// From a Tag
RecipeChoice choice = new RecipeChoice.MaterialChoice(Tag.PLANKS);
```

#### RecipeChoice.ExactChoice

Matches an exact `ItemStack` (including metadata, enchantments, display name).

```java
ItemStack customItem = new ItemStack(Material.DIAMOND);
ItemMeta meta = customItem.getItemMeta();
meta.displayName(Component.text("Magic Diamond", NamedTextColor.AQUA));
customItem.setItemMeta(meta);

RecipeChoice choice = new RecipeChoice.ExactChoice(customItem);
```

---

### 2.3 ShapedRecipe

**Package:** `org.bukkit.inventory`
**Extends:** `CraftingRecipe`

Represents a shaped (positional) crafting recipe.

#### Constructors

| Constructor | Description |
|---|---|
| `ShapedRecipe(@NotNull NamespacedKey key, @NotNull ItemStack result)` | Create a shaped recipe with the given key and result |
| ~~`ShapedRecipe(@NotNull ItemStack result)`~~ | **Deprecated** — Recipes must have keys |

#### Methods

| Return Type | Method | Description |
|---|---|---|
| `@NotNull ShapedRecipe` | `shape(@NotNull String... shape)` | Set the shape (up to 3 rows, each up to 3 chars). Spaces = empty slots |
| `@NotNull ShapedRecipe` | `setIngredient(char key, @NotNull Material ingredient)` | Set a material for a shape character |
| `@NotNull ShapedRecipe` | `setIngredient(char key, @NotNull RecipeChoice ingredient)` | Set a RecipeChoice for a shape character |
| `@NotNull ShapedRecipe` | `setIngredient(char key, @NotNull ItemStack item)` | Set an ItemStack for a shape character |
| `@NotNull Map<Character, RecipeChoice>` | `getChoiceMap()` | Get a copy of the choice map |
| `@NotNull String[]` | `getShape()` | Get the shape rows |
| ~~`@NotNull Map<Character, ItemStack>`~~ | ~~`getIngredientMap()`~~ | **Deprecated** — Use `getChoiceMap()` |

#### Inherited from CraftingRecipe

| Return Type | Method | Description |
|---|---|---|
| `@NotNull ItemStack` | `getResult()` | Get the result ItemStack |
| `@NotNull NamespacedKey` | `getKey()` | Get the recipe key |
| `@NotNull String` | `getGroup()` | Get the recipe group (for recipe book grouping) |
| `void` | `setGroup(@NotNull String group)` | Set the recipe group |
| `@NotNull CraftingBookCategory` | `getCategory()` | Get the crafting book category |
| `void` | `setCategory(@NotNull CraftingBookCategory category)` | Set the crafting book category |

#### CraftingBookCategory Enum

| Value | Description |
|---|---|
| `BUILDING` | Building materials |
| `REDSTONE` | Redstone components |
| `EQUIPMENT` | Tools, weapons, armor |
| `MISC` | Miscellaneous items |

---

### 2.4 ShapelessRecipe

**Package:** `org.bukkit.inventory`
**Extends:** `CraftingRecipe`

Represents a shapeless crafting recipe (ingredient order doesn't matter).

#### Constructors

| Constructor | Description |
|---|---|
| `ShapelessRecipe(@NotNull NamespacedKey key, @NotNull ItemStack result)` | Create a shapeless recipe with key and result |

#### Methods

| Return Type | Method | Description |
|---|---|---|
| `@NotNull ShapelessRecipe` | `addIngredient(@NotNull Material ingredient)` | Add a single material ingredient |
| `@NotNull ShapelessRecipe` | `addIngredient(int count, @NotNull Material ingredient)` | Add multiple of the same material |
| `@NotNull ShapelessRecipe` | `addIngredient(@NotNull RecipeChoice ingredient)` | Add a RecipeChoice ingredient |
| `@NotNull ShapelessRecipe` | `addIngredient(@NotNull ItemStack item)` | Add an ItemStack ingredient |
| `@NotNull ShapelessRecipe` | `removeIngredient(@NotNull Material ingredient)` | Remove a material ingredient |
| `@NotNull ShapelessRecipe` | `removeIngredient(int count, @NotNull Material ingredient)` | Remove multiple of a material |
| `@NotNull ShapelessRecipe` | `removeIngredient(@NotNull RecipeChoice ingredient)` | Remove a RecipeChoice ingredient |
| `@NotNull List<RecipeChoice>` | `getChoiceList()` | Get an unmodifiable list of all ingredient choices |
| ~~`@NotNull List<ItemStack>`~~ | ~~`getIngredientList()`~~ | **Deprecated** — Use `getChoiceList()` |

---

### 2.5 FurnaceRecipe

**Package:** `org.bukkit.inventory`
**Extends:** `CookingRecipe<FurnaceRecipe>`

Standard furnace smelting recipe. Default cook time: 200 ticks (10 seconds).

#### Constructors

| Constructor | Description |
|---|---|
| `FurnaceRecipe(@NotNull NamespacedKey key, @NotNull ItemStack result, @NotNull Material source, float experience, int cookingTime)` | Full constructor with XP and cook time |
| `FurnaceRecipe(@NotNull NamespacedKey key, @NotNull ItemStack result, @NotNull RecipeChoice input, float experience, int cookingTime)` | Full constructor with RecipeChoice input |

#### Inherited CookingRecipe Methods

| Return Type | Method | Description |
|---|---|---|
| `@NotNull ItemStack` | `getResult()` | Get the result item |
| `@NotNull RecipeChoice` | `getInputChoice()` | Get the input RecipeChoice |
| `void` | `setInputChoice(@NotNull RecipeChoice input)` | Set the input RecipeChoice |
| `float` | `getExperience()` | Get XP granted when smelting |
| `void` | `setExperience(float experience)` | Set XP granted |
| `int` | `getCookingTime()` | Get cook time in ticks |
| `void` | `setCookingTime(int cookingTime)` | Set cook time in ticks |
| `@NotNull CookingBookCategory` | `getCategory()` | Get the recipe book category |
| `void` | `setCategory(@NotNull CookingBookCategory category)` | Set the recipe book category |
| `@NotNull String` | `getGroup()` | Get recipe group |
| `void` | `setGroup(@NotNull String group)` | Set recipe group |

#### CookingBookCategory Enum

| Value | Description |
|---|---|
| `FOOD` | Food items |
| `BLOCKS` | Block smelting (ores, glass, etc.) |
| `MISC` | Miscellaneous |

---

### 2.6 BlastingRecipe

**Package:** `org.bukkit.inventory`
**Extends:** `CookingRecipe<BlastingRecipe>`

Blast furnace recipe. Default cook time: 100 ticks (5 seconds, 2x faster than furnace).

#### Constructor

```java
BlastingRecipe(@NotNull NamespacedKey key, @NotNull ItemStack result,
               @NotNull RecipeChoice input, float experience, int cookingTime)
```

Same methods as `CookingRecipe` (see FurnaceRecipe above).

---

### 2.7 SmokingRecipe

**Package:** `org.bukkit.inventory`
**Extends:** `CookingRecipe<SmokingRecipe>`

Smoker recipe. Default cook time: 100 ticks (5 seconds, 2x faster than furnace).

#### Constructor

```java
SmokingRecipe(@NotNull NamespacedKey key, @NotNull ItemStack result,
              @NotNull RecipeChoice input, float experience, int cookingTime)
```

Same methods as `CookingRecipe` (see FurnaceRecipe above).

---

### 2.8 CampfireRecipe

**Package:** `org.bukkit.inventory`
**Extends:** `CookingRecipe<CampfireRecipe>`

Campfire cooking recipe. Default cook time: 600 ticks (30 seconds).

#### Constructor

```java
CampfireRecipe(@NotNull NamespacedKey key, @NotNull ItemStack result,
               @NotNull RecipeChoice input, float experience, int cookingTime)
```

Same methods as `CookingRecipe` (see FurnaceRecipe above).

---

### 2.9 SmithingTransformRecipe

**Package:** `org.bukkit.inventory`
**Extends:** `SmithingRecipe`

Transforms an item using a template, base, and addition on a smithing table (e.g. Netherite upgrade).

#### Constructor

```java
SmithingTransformRecipe(
    @NotNull NamespacedKey key,
    @NotNull ItemStack result,
    @Nullable RecipeChoice template,    // e.g. Netherite Upgrade template
    @Nullable RecipeChoice base,        // e.g. Diamond Sword
    @Nullable RecipeChoice addition     // e.g. Netherite Ingot
)
```

#### Methods

| Return Type | Method | Description |
|---|---|---|
| `@NotNull ItemStack` | `getResult()` | Get the result item |
| `@Nullable RecipeChoice` | `getTemplate()` | Get the template RecipeChoice |
| `@Nullable RecipeChoice` | `getBase()` | Get the base item RecipeChoice |
| `@Nullable RecipeChoice` | `getAddition()` | Get the addition RecipeChoice |

---

### 2.10 StonecuttingRecipe

**Package:** `org.bukkit.inventory`

Stonecutter recipe (single input → single output, no shape).

#### Constructor

```java
StonecuttingRecipe(@NotNull NamespacedKey key, @NotNull ItemStack result,
                   @NotNull RecipeChoice input)
```

#### Methods

| Return Type | Method | Description |
|---|---|---|
| `@NotNull ItemStack` | `getResult()` | Get the result item |
| `@NotNull RecipeChoice` | `getInputChoice()` | Get the input RecipeChoice |
| `void` | `setInputChoice(@NotNull RecipeChoice input)` | Set the input |
| `@NotNull String` | `getGroup()` | Get recipe group |
| `void` | `setGroup(@NotNull String group)` | Set recipe group |

---

### 2.11 CraftingRecipe (Base Class)

**Package:** `org.bukkit.inventory`

Abstract base for `ShapedRecipe` and `ShapelessRecipe`.

#### Key Method: `checkResult`

```java
// Paper-exclusive: Validate result before crafting completes
public void checkResult(@NotNull ItemStack result)
```

This method is called by Paper to allow plugins to modify or validate the result item before it's placed in the crafting output slot.

---

### 2.12 Registering & Removing Recipes at Runtime

#### Adding Recipes

```java
// Register with the server
Bukkit.addRecipe(recipe);

// In onEnable:
@Override
public void onEnable() {
    ShapedRecipe recipe = createMyRecipe();
    Bukkit.addRecipe(recipe);
}
```

#### Removing Recipes

```java
// Remove a specific recipe by key
Bukkit.removeRecipe(NamespacedKey.minecraft("iron_sword"));

// Remove a custom recipe
Bukkit.removeRecipe(new NamespacedKey(this, "my_recipe"));

// Clear ALL recipes (use with caution!)
Bukkit.clearRecipes();

// Reset to vanilla defaults
Bukkit.resetRecipes();
```

#### Querying Recipes

```java
// Get a recipe by key
Recipe recipe = Bukkit.getRecipe(NamespacedKey.minecraft("diamond_sword"));

// Get all recipes for a result
List<Recipe> recipes = Bukkit.getRecipesFor(new ItemStack(Material.DIAMOND_SWORD));

// Iterate all recipes
Iterator<Recipe> it = Bukkit.recipeIterator();
while (it.hasNext()) {
    Recipe r = it.next();
    // ...
}
```

#### Discovering Recipes (Player)

```java
// Make a recipe visible in the player's recipe book
player.discoverRecipe(new NamespacedKey(plugin, "custom_sword"));

// Bulk discover
player.discoverRecipes(List.of(
    new NamespacedKey(plugin, "custom_sword"),
    new NamespacedKey(plugin, "custom_shield")
));

// Un-discover (hide from recipe book)
player.undiscoverRecipe(new NamespacedKey(plugin, "custom_sword"));

// Check if discovered
boolean known = player.hasDiscoveredRecipe(new NamespacedKey(plugin, "custom_sword"));

// Get all discovered recipe keys
Set<NamespacedKey> discovered = player.getDiscoveredRecipes();
```

---

## 3. Structure System

### 3.1 Structure Class

**Package:** `org.bukkit.generator.structure`
**Implements:** `org.bukkit.Keyed`

Represents a specific structure variant in the world. Additional structures can be added via data packs and accessed through `RegistryAccess.getRegistry(RegistryKey.STRUCTURE)`.

#### Static Constants (All Default Structures)

| Constant | Description |
|---|---|
| `Structure.ANCIENT_CITY` | Deep Dark ancient city |
| `Structure.BASTION_REMNANT` | Nether bastion remnant |
| `Structure.BURIED_TREASURE` | Beach buried treasure |
| `Structure.DESERT_PYRAMID` | Desert temple |
| `Structure.END_CITY` | End city with optional ship |
| `Structure.FORTRESS` | Nether fortress |
| `Structure.IGLOO` | Snowy igloo |
| `Structure.JUNGLE_PYRAMID` | Jungle temple |
| `Structure.MANSION` | Woodland mansion |
| `Structure.MINESHAFT` | Standard abandoned mineshaft |
| `Structure.MINESHAFT_MESA` | Badlands mineshaft (exposed at surface) |
| `Structure.MONUMENT` | Ocean monument |
| `Structure.NETHER_FOSSIL` | Soul Sand Valley fossil |
| `Structure.OCEAN_RUIN_COLD` | Cold ocean ruins |
| `Structure.OCEAN_RUIN_WARM` | Warm ocean ruins |
| `Structure.PILLAGER_OUTPOST` | Pillager outpost |
| `Structure.RUINED_PORTAL` | Standard ruined portal |
| `Structure.RUINED_PORTAL_DESERT` | Desert ruined portal variant |
| `Structure.RUINED_PORTAL_JUNGLE` | Jungle ruined portal variant |
| `Structure.RUINED_PORTAL_MOUNTAIN` | Mountain ruined portal variant |
| `Structure.RUINED_PORTAL_NETHER` | Nether ruined portal variant |
| `Structure.RUINED_PORTAL_OCEAN` | Ocean ruined portal variant |
| `Structure.RUINED_PORTAL_SWAMP` | Swamp ruined portal variant |
| `Structure.SHIPWRECK` | Ocean shipwreck |
| `Structure.SHIPWRECK_BEACHED` | Beached shipwreck variant |
| `Structure.STRONGHOLD` | End portal stronghold |
| `Structure.SWAMP_HUT` | Witch hut |
| `Structure.TRAIL_RUINS` | Trail ruins (1.20+) |
| `Structure.TRIAL_CHAMBERS` | Trial chambers (1.21+) |
| `Structure.VILLAGE_DESERT` | Desert village |
| `Structure.VILLAGE_PLAINS` | Plains village |
| `Structure.VILLAGE_SAVANNA` | Savanna village |
| `Structure.VILLAGE_SNOWY` | Snowy village |
| `Structure.VILLAGE_TAIGA` | Taiga village |

#### Methods

| Return Type | Method | Description |
|---|---|---|
| `abstract @NotNull StructureType` | `getStructureType()` | Returns the type of the structure |
| ~~`abstract @NotNull NamespacedKey`~~ | ~~`getKey()`~~ | **Deprecated for removal** — use `Registry.getKey(Keyed)` with `RegistryKey.STRUCTURE` |
| ~~`@NotNull Key`~~ | ~~`key()`~~ | **Deprecated for removal** — use `Registry.getKey(Keyed)` with `RegistryKey.STRUCTURE` |

#### Modern Registry Access Pattern

```java
// Get Structure registry
Registry<Structure> structureRegistry = RegistryAccess.registryAccess()
    .getRegistry(RegistryKey.STRUCTURE);

// Lookup by key
Structure fortress = structureRegistry.getOrThrow(
    TypedKey.create(RegistryKey.STRUCTURE, Key.key("minecraft:fortress"))
);

// Get the key of a structure
Key key = structureRegistry.getKey(Structure.ANCIENT_CITY);
```

---

### 3.2 StructureType Class

**Package:** `org.bukkit.generator.structure`
**Implements:** `org.bukkit.Keyed`

Represents a category/type that groups related `Structure` variants. Additional types can be added by data packs and accessed via `Registry.STRUCTURE_TYPE`.

#### Static Constants

| Constant | Description | Example Structures |
|---|---|---|
| `StructureType.BURIED_TREASURE` | Buried treasure | `BURIED_TREASURE` |
| `StructureType.DESERT_PYRAMID` | Desert temple | `DESERT_PYRAMID` |
| `StructureType.END_CITY` | End city | `END_CITY` |
| `StructureType.FORTRESS` | Nether fortress | `FORTRESS` |
| `StructureType.IGLOO` | Igloo | `IGLOO` |
| `StructureType.JIGSAW` | Jigsaw-generated | Villages, Bastions, Pillager Outposts, Trial Chambers |
| `StructureType.JUNGLE_TEMPLE` | Jungle temple | `JUNGLE_PYRAMID` |
| `StructureType.MINESHAFT` | Mineshaft | `MINESHAFT`, `MINESHAFT_MESA` |
| `StructureType.NETHER_FOSSIL` | Nether fossil | `NETHER_FOSSIL` |
| `StructureType.OCEAN_MONUMENT` | Ocean monument | `MONUMENT` |
| `StructureType.OCEAN_RUIN` | Ocean ruins | `OCEAN_RUIN_COLD`, `OCEAN_RUIN_WARM` |
| `StructureType.RUINED_PORTAL` | Ruined portal | All `RUINED_PORTAL_*` variants |
| `StructureType.SHIPWRECK` | Shipwreck | `SHIPWRECK`, `SHIPWRECK_BEACHED` |
| `StructureType.STRONGHOLD` | Stronghold | `STRONGHOLD` |
| `StructureType.SWAMP_HUT` | Witch hut | `SWAMP_HUT` |
| `StructureType.WOODLAND_MANSION` | Woodland mansion | `MANSION` |

> **Note:** `JIGSAW` is a meta-type for all jigsaw-based structures (villages, bastions, pillager outposts, trial chambers, trail ruins).

---

### 3.3 Locating Structures

**On `World`:**

```java
// Locate nearest structure of a specific type
// Returns StructureSearchResult or null
StructureSearchResult result = world.locateNearestStructure(
    location,               // Origin location
    Structure.STRONGHOLD,   // Target structure
    100,                    // Search radius (in chunks)
    false                   // false = include unexplored chunks
);

// locateNearestStructure also supports StructureType
StructureSearchResult result = world.locateNearestStructure(
    location,
    StructureType.FORTRESS,
    100,
    false
);
```

#### StructureSearchResult

| Return Type | Method | Description |
|---|---|---|
| `@NotNull Location` | `getLocation()` | The location of the nearest structure |
| `@NotNull Structure` | `getStructure()` | The structure that was found |

---

### 3.4 StructuresLocateEvent (Paper)

**Package:** `io.papermc.paper.event.world`
**Extends:** `ServerEvent`, `Cancellable`

> **Note:** The old `StructureLocateEvent` is **deprecated and removed**. Use `StructuresLocateEvent` (plural) instead.

Fired when the server searches for structures (e.g., `/locate`, Eye of Ender, explorer maps, mob spawning logic).

#### Methods

| Return Type | Method | Description |
|---|---|---|
| `@NotNull Location` | `getOrigin()` | The origin location of the search |
| `@NotNull List<Structure>` | `getStructures()` | Get the list of structures being searched for |
| `void` | `setStructures(@NotNull List<Structure> structures)` | Modify which structures are being searched for |
| `@Nullable StructureSearchResult` | `getResult()` | Get the result (null if search hasn't completed or was cancelled) |
| `void` | `setResult(@Nullable StructureSearchResult result)` | Override the search result |
| `int` | `getRadius()` | Get the search radius in chunks |
| `void` | `setRadius(int radius)` | Set the search radius |
| `boolean` | `shouldFindUnexplored()` | Whether the search includes unexplored chunks |
| `void` | `setShouldFindUnexplored(boolean findUnexplored)` | Set whether to search unexplored chunks |
| `boolean` | `isCancelled()` | Whether the event is cancelled |
| `void` | `setCancelled(boolean cancel)` | Cancel the event |

#### Handler List

```java
public static @NotNull HandlerList getHandlerList()
public @NotNull HandlerList getHandlers()
```

---

## 4. Code Examples

### Example 1: Grant All Advancements to a Player

```java
public void grantAllAdvancements(Player player) {
    Iterator<Advancement> it = Bukkit.advancementIterator();
    int count = 0;

    while (it.hasNext()) {
        Advancement adv = it.next();
        AdvancementProgress progress = player.getAdvancementProgress(adv);

        if (!progress.isDone()) {
            for (String criteria : progress.getRemainingCriteria()) {
                progress.awardCriteria(criteria);
            }
            count++;
        }
    }

    player.sendMessage(Component.text("Granted " + count + " advancements!", NamedTextColor.GREEN));
}
```

### Example 2: Revoke a Specific Advancement

```java
public void revokeAdvancement(Player player, String key) {
    Advancement adv = Bukkit.getAdvancement(NamespacedKey.minecraft(key));
    if (adv == null) return;

    AdvancementProgress progress = player.getAdvancementProgress(adv);
    for (String criteria : progress.getAwardedCriteria()) {
        progress.revokeCriteria(criteria);
    }

    player.sendMessage(Component.text("Revoked: " + adv.displayName()));
}
```

### Example 3: Check Advancement Progress

```java
public void showAdvancementInfo(Player player, String key) {
    Advancement adv = Bukkit.getAdvancement(NamespacedKey.minecraft(key));
    if (adv == null) {
        player.sendMessage(Component.text("Unknown advancement!", NamedTextColor.RED));
        return;
    }

    AdvancementProgress progress = player.getAdvancementProgress(adv);
    AdvancementDisplay display = adv.getDisplay();

    player.sendMessage(Component.text("=== Advancement Info ===", NamedTextColor.GOLD));
    player.sendMessage(Component.text("Name: ").append(adv.displayName()));

    if (display != null) {
        player.sendMessage(Component.text("Frame: " + display.frame().name()));
        player.sendMessage(Component.text("Description: ").append(display.description()));
    }

    player.sendMessage(Component.text("Completed: " + progress.isDone(), 
        progress.isDone() ? NamedTextColor.GREEN : NamedTextColor.RED));

    int awarded = progress.getAwardedCriteria().size();
    int total = adv.getCriteria().size();
    player.sendMessage(Component.text("Progress: " + awarded + "/" + total));

    // Show remaining criteria
    for (String remaining : progress.getRemainingCriteria()) {
        player.sendMessage(Component.text("  ✗ " + remaining, NamedTextColor.RED));
    }
    for (String done : progress.getAwardedCriteria()) {
        Date date = progress.getDateAwarded(done);
        String dateStr = date != null ? " (awarded " + date.toString() + ")" : "";
        player.sendMessage(Component.text("  ✓ " + done + dateStr, NamedTextColor.GREEN));
    }
}
```

### Example 4: Custom Shaped Recipe — Diamond Hammer

```java
public void registerDiamondHammer(JavaPlugin plugin) {
    // Create the result item
    ItemStack hammer = new ItemStack(Material.DIAMOND_AXE);
    ItemMeta meta = hammer.getItemMeta();
    meta.displayName(Component.text("Diamond Hammer", NamedTextColor.AQUA)
        .decoration(TextDecoration.ITALIC, false));
    meta.lore(List.of(
        Component.text("A powerful mining tool", NamedTextColor.GRAY)
    ));
    hammer.setItemMeta(meta);

    // Create the recipe
    NamespacedKey key = new NamespacedKey(plugin, "diamond_hammer");
    ShapedRecipe recipe = new ShapedRecipe(key, hammer);

    // Define shape
    recipe.shape(
        "DDD",
        " S ",
        " S "
    );

    // Set ingredients
    recipe.setIngredient('D', Material.DIAMOND_BLOCK);
    recipe.setIngredient('S', Material.STICK);

    // Set category for recipe book
    recipe.setCategory(CraftingBookCategory.EQUIPMENT);
    recipe.setGroup("custom_tools");

    // Register
    Bukkit.addRecipe(recipe);
}
```

### Example 5: Custom Shapeless Recipe — Ore Blending

```java
public void registerOreBlend(JavaPlugin plugin) {
    ItemStack result = new ItemStack(Material.RAW_GOLD, 2);

    NamespacedKey key = new NamespacedKey(plugin, "ore_blend");
    ShapelessRecipe recipe = new ShapelessRecipe(key, result);

    // Add ingredients (order doesn't matter)
    recipe.addIngredient(Material.RAW_IRON);
    recipe.addIngredient(Material.RAW_COPPER);
    recipe.addIngredient(Material.GOLD_NUGGET);

    recipe.setCategory(CraftingBookCategory.MISC);
    Bukkit.addRecipe(recipe);
}
```

### Example 6: Recipe with RecipeChoice (Multiple Valid Inputs)

```java
public void registerTagRecipe(JavaPlugin plugin) {
    ItemStack result = new ItemStack(Material.CHEST, 4);

    NamespacedKey key = new NamespacedKey(plugin, "any_plank_chest");
    ShapedRecipe recipe = new ShapedRecipe(key, result);

    recipe.shape(
        "PPP",
        "P P",
        "PPP"
    );

    // Accept ANY type of planks using a Tag
    recipe.setIngredient('P', new RecipeChoice.MaterialChoice(Tag.PLANKS));

    Bukkit.addRecipe(recipe);
}
```

### Example 7: Custom Furnace Smelting Recipe

```java
public void registerCustomSmelting(JavaPlugin plugin) {
    ItemStack result = new ItemStack(Material.EMERALD);

    NamespacedKey key = new NamespacedKey(plugin, "smelt_mossy_cobble");
    FurnaceRecipe recipe = new FurnaceRecipe(
        key,
        result,
        Material.MOSSY_COBBLESTONE,
        1.5f,   // XP
        200     // Cook time in ticks (10 seconds)
    );

    recipe.setCategory(CookingBookCategory.MISC);
    Bukkit.addRecipe(recipe);
}
```

### Example 8: Blast Furnace & Smoker Recipes

```java
// Blast furnace — 2x faster than regular furnace
public void registerBlastRecipe(JavaPlugin plugin) {
    NamespacedKey key = new NamespacedKey(plugin, "blast_raw_ancient");
    BlastingRecipe recipe = new BlastingRecipe(
        key,
        new ItemStack(Material.NETHERITE_SCRAP),
        new RecipeChoice.MaterialChoice(Material.ANCIENT_DEBRIS),
        2.0f,   // XP
        100     // 5 seconds (half of furnace)
    );
    Bukkit.addRecipe(recipe);
}

// Smoker — fast food cooking
public void registerSmokerRecipe(JavaPlugin plugin) {
    NamespacedKey key = new NamespacedKey(plugin, "smoke_apple");
    SmokingRecipe recipe = new SmokingRecipe(
        key,
        new ItemStack(Material.GOLDEN_APPLE),
        new RecipeChoice.MaterialChoice(Material.APPLE),
        0.5f,   // XP
        100     // 5 seconds
    );
    Bukkit.addRecipe(recipe);
}
```

### Example 9: Smithing Transform Recipe (Custom Upgrade)

```java
public void registerSmithingUpgrade(JavaPlugin plugin) {
    ItemStack result = new ItemStack(Material.NETHERITE_SWORD);
    ItemMeta meta = result.getItemMeta();
    meta.displayName(Component.text("Infernal Blade", NamedTextColor.DARK_RED));
    result.setItemMeta(meta);

    NamespacedKey key = new NamespacedKey(plugin, "infernal_blade");
    SmithingTransformRecipe recipe = new SmithingTransformRecipe(
        key,
        result,
        // Template: Netherite Upgrade Smithing Template
        new RecipeChoice.MaterialChoice(Material.NETHERITE_UPGRADE_SMITHING_TEMPLATE),
        // Base: Diamond Sword
        new RecipeChoice.MaterialChoice(Material.DIAMOND_SWORD),
        // Addition: Blaze Rod
        new RecipeChoice.MaterialChoice(Material.BLAZE_ROD)
    );

    Bukkit.addRecipe(recipe);
}
```

### Example 10: Stonecutter Recipe

```java
public void registerStonecutterRecipe(JavaPlugin plugin) {
    NamespacedKey key = new NamespacedKey(plugin, "cut_deepslate_bricks");
    StonecuttingRecipe recipe = new StonecuttingRecipe(
        key,
        new ItemStack(Material.DEEPSLATE_BRICKS, 4),
        new RecipeChoice.MaterialChoice(Material.DEEPSLATE)
    );
    Bukkit.addRecipe(recipe);
}
```

### Example 11: Find Nearest Structure (with Compass)

```java
public void findNearestStronghold(Player player) {
    World world = player.getWorld();
    Location origin = player.getLocation();

    // Async-safe: this can be slow, consider running async for large radii
    StructureSearchResult result = world.locateNearestStructure(
        origin,
        Structure.STRONGHOLD,
        100,    // 100 chunks = 1600 blocks radius
        false   // Include unexplored chunks
    );

    if (result != null) {
        Location loc = result.getLocation();
        double distance = origin.distance(loc);

        player.sendMessage(Component.text("Nearest Stronghold: ", NamedTextColor.GOLD)
            .append(Component.text(
                String.format("X=%d, Z=%d (%.0f blocks away)",
                    loc.getBlockX(), loc.getBlockZ(), distance),
                NamedTextColor.YELLOW
            )));

        // Give compass pointing to structure
        ItemStack compass = new ItemStack(Material.COMPASS);
        CompassMeta compassMeta = (CompassMeta) compass.getItemMeta();
        compassMeta.setLodestone(loc);
        compassMeta.setLodestoneTracked(false); // Don't require actual lodestone block
        compassMeta.displayName(Component.text("Stronghold Tracker", NamedTextColor.LIGHT_PURPLE));
        compass.setItemMeta(compassMeta);

        player.getInventory().addItem(compass);
    } else {
        player.sendMessage(Component.text("No Stronghold found nearby!", NamedTextColor.RED));
    }
}
```

### Example 12: StructuresLocateEvent Listener

```java
public class StructureListener implements Listener {

    @EventHandler
    public void onStructuresLocate(StructuresLocateEvent event) {
        // Redirect all stronghold searches to ancient cities
        List<Structure> structures = new ArrayList<>(event.getStructures());
        boolean modified = false;

        for (int i = 0; i < structures.size(); i++) {
            if (structures.get(i).equals(Structure.STRONGHOLD)) {
                structures.set(i, Structure.ANCIENT_CITY);
                modified = true;
            }
        }

        if (modified) {
            event.setStructures(structures);
            Bukkit.getLogger().info("Redirected stronghold search to ancient city at " +
                event.getOrigin());
        }
    }

    // Prevent locating buried treasure (anti-x-ray for treasure maps)
    @EventHandler
    public void onTreasureLocate(StructuresLocateEvent event) {
        event.getStructures().removeIf(s -> s.equals(Structure.BURIED_TREASURE));
        if (event.getStructures().isEmpty()) {
            event.setCancelled(true);
        }
    }
}
```

### Example 13: Complete Recipe Manager Plugin

```java
public class RecipeManager extends JavaPlugin {

    private final List<NamespacedKey> registeredRecipes = new ArrayList<>();

    @Override
    public void onEnable() {
        registerAllRecipes();

        // Auto-discover recipes for joining players
        getServer().getPluginManager().registerEvents(new Listener() {
            @EventHandler
            public void onJoin(PlayerJoinEvent event) {
                event.getPlayer().discoverRecipes(registeredRecipes);
            }
        }, this);
    }

    @Override
    public void onDisable() {
        // Clean up custom recipes
        for (NamespacedKey key : registeredRecipes) {
            Bukkit.removeRecipe(key);
        }
    }

    private void registerAllRecipes() {
        // Register shaped recipe
        registerRecipe(createEnchantedGoldenAppleRecipe());

        // Register shapeless recipe
        registerRecipe(createDyeBlendRecipe());

        // Register furnace recipe
        registerRecipe(createCustomSmeltRecipe());

        getLogger().info("Registered " + registeredRecipes.size() + " custom recipes");
    }

    private void registerRecipe(Recipe recipe) {
        if (recipe instanceof Keyed keyed) {
            registeredRecipes.add(keyed.getKey());
        }
        Bukkit.addRecipe(recipe);
    }

    private ShapedRecipe createEnchantedGoldenAppleRecipe() {
        NamespacedKey key = new NamespacedKey(this, "enchanted_golden_apple");
        ShapedRecipe recipe = new ShapedRecipe(key, new ItemStack(Material.ENCHANTED_GOLDEN_APPLE));
        recipe.shape("GGG", "GAG", "GGG");
        recipe.setIngredient('G', Material.GOLD_BLOCK);
        recipe.setIngredient('A', Material.APPLE);
        recipe.setCategory(CraftingBookCategory.MISC);
        return recipe;
    }

    private ShapelessRecipe createDyeBlendRecipe() {
        NamespacedKey key = new NamespacedKey(this, "magenta_dye_blend");
        ShapelessRecipe recipe = new ShapelessRecipe(key, new ItemStack(Material.MAGENTA_DYE, 3));
        recipe.addIngredient(Material.RED_DYE);
        recipe.addIngredient(Material.BLUE_DYE);
        recipe.addIngredient(Material.WHITE_DYE);
        recipe.setCategory(CraftingBookCategory.MISC);
        return recipe;
    }

    private FurnaceRecipe createCustomSmeltRecipe() {
        NamespacedKey key = new NamespacedKey(this, "smelt_gravel_to_flint");
        return new FurnaceRecipe(key, new ItemStack(Material.FLINT),
            Material.GRAVEL, 0.1f, 200);
    }
}
```

### Example 14: Structure Finder GUI Command

```java
public class StructureFinderCommand implements CommandExecutor {

    private static final Map<String, Structure> FINDABLE = Map.ofEntries(
        Map.entry("stronghold", Structure.STRONGHOLD),
        Map.entry("fortress", Structure.FORTRESS),
        Map.entry("monument", Structure.MONUMENT),
        Map.entry("mansion", Structure.MANSION),
        Map.entry("end_city", Structure.END_CITY),
        Map.entry("village", Structure.VILLAGE_PLAINS),
        Map.entry("desert_temple", Structure.DESERT_PYRAMID),
        Map.entry("jungle_temple", Structure.JUNGLE_PYRAMID),
        Map.entry("outpost", Structure.PILLAGER_OUTPOST),
        Map.entry("ancient_city", Structure.ANCIENT_CITY),
        Map.entry("trail_ruins", Structure.TRAIL_RUINS),
        Map.entry("trial_chambers", Structure.TRIAL_CHAMBERS),
        Map.entry("buried_treasure", Structure.BURIED_TREASURE),
        Map.entry("shipwreck", Structure.SHIPWRECK),
        Map.entry("igloo", Structure.IGLOO),
        Map.entry("swamp_hut", Structure.SWAMP_HUT),
        Map.entry("bastion", Structure.BASTION_REMNANT)
    );

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("Players only!");
            return true;
        }

        if (args.length == 0) {
            player.sendMessage(Component.text("Available: " + String.join(", ", FINDABLE.keySet()),
                NamedTextColor.YELLOW));
            return true;
        }

        Structure target = FINDABLE.get(args[0].toLowerCase());
        if (target == null) {
            player.sendMessage(Component.text("Unknown structure: " + args[0], NamedTextColor.RED));
            return true;
        }

        player.sendMessage(Component.text("Searching for " + args[0] + "...", NamedTextColor.GRAY));

        // Run async to avoid blocking main thread
        Bukkit.getScheduler().runTaskAsynchronously(
            JavaPlugin.getProvidingPlugin(getClass()), () -> {
                StructureSearchResult result = player.getWorld().locateNearestStructure(
                    player.getLocation(), target, 150, false
                );

                // Switch back to main thread for player interaction
                Bukkit.getScheduler().runTask(
                    JavaPlugin.getProvidingPlugin(getClass()), () -> {
                        if (result != null) {
                            Location loc = result.getLocation();
                            player.sendMessage(Component.text(
                                String.format("Found %s at X=%d, Z=%d",
                                    args[0], loc.getBlockX(), loc.getBlockZ()),
                                NamedTextColor.GREEN
                            ));
                        } else {
                            player.sendMessage(Component.text(
                                "Could not find " + args[0] + " within range!",
                                NamedTextColor.RED
                            ));
                        }
                    }
                );
            }
        );

        return true;
    }
}
```

### Example 15: Campfire & ExactChoice Recipe

```java
public void registerCampfireRecipe(JavaPlugin plugin) {
    // Campfire recipe with longer cook time (30 seconds default)
    NamespacedKey key = new NamespacedKey(plugin, "roast_spider_eye");
    CampfireRecipe recipe = new CampfireRecipe(
        key,
        new ItemStack(Material.FERMENTED_SPIDER_EYE),
        new RecipeChoice.MaterialChoice(Material.SPIDER_EYE),
        0.35f,  // XP
        600     // 30 seconds
    );
    recipe.setCategory(CookingBookCategory.MISC);
    Bukkit.addRecipe(recipe);
}

// ExactChoice recipe — requires SPECIFIC named item
public void registerExactRecipe(JavaPlugin plugin) {
    // Create the required input (must match exactly)
    ItemStack requiredInput = new ItemStack(Material.PAPER);
    ItemMeta inputMeta = requiredInput.getItemMeta();
    inputMeta.displayName(Component.text("Magic Scroll", NamedTextColor.LIGHT_PURPLE));
    requiredInput.setItemMeta(inputMeta);

    // Result
    ItemStack result = new ItemStack(Material.ENCHANTED_BOOK);

    NamespacedKey key = new NamespacedKey(plugin, "magic_scroll_recipe");
    ShapelessRecipe recipe = new ShapelessRecipe(key, result);
    recipe.addIngredient(new RecipeChoice.ExactChoice(requiredInput));
    recipe.addIngredient(Material.LAPIS_LAZULI);

    Bukkit.addRecipe(recipe);
}
```

---

## Quick Reference: Key Classes & Packages

| Class | Package | Purpose |
|---|---|---|
| `Advancement` | `org.bukkit.advancement` | Advancement definition with criteria & display |
| `AdvancementProgress` | `org.bukkit.advancement` | Per-player advancement progress tracking |
| `AdvancementDisplay` | `io.papermc.paper.advancement` | Paper: advancement display info (title, icon, frame) |
| `ShapedRecipe` | `org.bukkit.inventory` | Positional crafting recipe |
| `ShapelessRecipe` | `org.bukkit.inventory` | Order-independent crafting recipe |
| `FurnaceRecipe` | `org.bukkit.inventory` | Furnace smelting recipe (200t default) |
| `BlastingRecipe` | `org.bukkit.inventory` | Blast furnace recipe (100t default) |
| `SmokingRecipe` | `org.bukkit.inventory` | Smoker recipe (100t default) |
| `CampfireRecipe` | `org.bukkit.inventory` | Campfire recipe (600t default) |
| `SmithingTransformRecipe` | `org.bukkit.inventory` | Smithing table transform (template + base + addition) |
| `StonecuttingRecipe` | `org.bukkit.inventory` | Stonecutter recipe |
| `RecipeChoice.MaterialChoice` | `org.bukkit.inventory` | Match any of listed Materials or a Tag |
| `RecipeChoice.ExactChoice` | `org.bukkit.inventory` | Match exact ItemStack (with metadata) |
| `Structure` | `org.bukkit.generator.structure` | Specific structure variant (33 constants) |
| `StructureType` | `org.bukkit.generator.structure` | Structure category (16 constants) |
| `StructuresLocateEvent` | `io.papermc.paper.event.world` | Paper: intercept structure searches |
| `StructureSearchResult` | `org.bukkit.util` | Location + Structure of a search result |

---

## Javadoc Links

- [Advancement](https://jd.papermc.io/paper/26.1.2/org/bukkit/advancement/Advancement.html)
- [AdvancementProgress](https://jd.papermc.io/paper/26.1.2/org/bukkit/advancement/AdvancementProgress.html)
- [ShapedRecipe](https://jd.papermc.io/paper/26.1.2/org/bukkit/inventory/ShapedRecipe.html)
- [ShapelessRecipe](https://jd.papermc.io/paper/26.1.2/org/bukkit/inventory/ShapelessRecipe.html)
- [FurnaceRecipe](https://jd.papermc.io/paper/26.1.2/org/bukkit/inventory/FurnaceRecipe.html)
- [SmithingTransformRecipe](https://jd.papermc.io/paper/26.1.2/org/bukkit/inventory/SmithingTransformRecipe.html)
- [Structure](https://jd.papermc.io/paper/26.1.2/org/bukkit/generator/structure/Structure.html)
- [StructureType](https://jd.papermc.io/paper/26.1.2/org/bukkit/generator/structure/StructureType.html)
- [StructuresLocateEvent](https://jd.papermc.io/paper/26.1.2/io/papermc/paper/event/world/StructuresLocateEvent.html)
