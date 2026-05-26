# Inventory, Menu & ItemStack API — Paper 26.1.2

> **Packages:** `org.bukkit.inventory`, `org.bukkit.event.inventory`, `io.papermc.paper.datacomponent`
> **Javadoc:** https://jd.papermc.io/paper/26.1.2/org/bukkit/inventory/package-summary.html

---

## Table of Contents

1. [Overview & Architecture](#overview--architecture)
1.5 [HouziCore Integration Rules (CRITICAL)](#houzicore-integration-rules-critical)
2. [MenuType](#menutype)
3. [Inventory](#inventory)
4. [InventoryView](#inventoryview)
5. [ItemStack](#itemstack)
6. [ItemMeta](#itemmeta)
7. [DataComponentTypes (Paper)](#datacomponenttypes-paper)
8. [InventoryHolder & Custom GUIs](#inventoryholder--custom-guis)
9. [Inventory Events](#inventory-events)
10. [Code Examples](#code-examples)

---

## HouziCore Integration Rules (CRITICAL)

> [!IMPORTANT]
> **HouziCore Standard:** Never create raw inventories using `Bukkit.createInventory()` + `InventoryClickEvent` handling. Bypassing the core framework leaves open slots, breaks click sound feedback, and leads to item desync issues.
> 
> Follow these strict integration rules:
> 1. **Custom GUIs & Menus**: All menus, shops, page lists, and confirmation dialogs MUST extend the `ShopBase` and `ShopPageBase` framework.
> 2. **Item Stack Construction**: Always use `ItemStackFactory.Instance.CreateStack(...)` to build item stacks. This ensures consistent formatting, display name formatting, and lore parsing.
> 3. **Paper 26.1 DataComponents Integration**:
>    - You can use Paper 26.1 `DataComponentTypes` (like `MAX_STACK_SIZE`, `UNBREAKABLE`, `RARITY`, `ENCHANTMENT_GLINT_OVERRIDE`) directly on item stacks returned by `ItemStackFactory`.
>    - Use `itemStack.setData(DataComponentTypes.UNBREAKABLE, Unbreakable.unbreakable(false))` to override components on created stacks.
> 4. **Border Padding**: All empty slots in a shop menu MUST be filled with blue glass pane border padding (`Material.STAINED_GLASS_PANE` byte 3) with a title displaying the player's currency balance.
> 5. **Sound Feedback**: Use `playAcceptSound(player)` for successful actions/purchases and `playDenySound(player)` for locked/failed actions inside button callbacks.

---

## Overview & Architecture

```
MenuType (defines GUI layout)
  └── create(Player, Component title) → InventoryView

Inventory (raw slot storage)
  ├── created via Bukkit.createInventory(holder, size, title)
  ├── or via MenuType.GENERIC_9X6.create(player, title)
  └── contains ItemStack[]

ItemStack (single stack)
  ├── ItemMeta (legacy metadata API)
  │     ├── displayName(), lore(), enchants, flags, PDC
  │     └── Specialized: BookMeta, PotionMeta, SkullMeta, etc.
  └── DataComponentTypes (modern Paper 26.1 API)
        ├── setData(DataComponentTypes.CUSTOM_NAME, Component)
        ├── setData(DataComponentTypes.LORE, ItemLore)
        ├── setData(DataComponentTypes.MAX_STACK_SIZE, 99)
        └── ...hundreds of typed components

InventoryHolder (marker interface)
  └── Implement to associate your GUI class with an Inventory
```

---

## MenuType

```java
// Interface: org.bukkit.inventory.MenuType
// Javadoc: https://jd.papermc.io/paper/26.1.2/org/bukkit/inventory/MenuType.html
```

### All MenuType Constants

| Constant | Slots | Description |
|----------|-------|-------------|
| `MenuType.GENERIC_9X1` | 9 | 1-row chest |
| `MenuType.GENERIC_9X2` | 18 | 2-row chest |
| `MenuType.GENERIC_9X3` | 27 | 3-row chest (small chest) |
| `MenuType.GENERIC_9X4` | 36 | 4-row chest |
| `MenuType.GENERIC_9X5` | 45 | 5-row chest |
| `MenuType.GENERIC_9X6` | 54 | 6-row chest (double chest) |
| `MenuType.GENERIC_3X3` | 9 | 3×3 grid (dropper/dispenser style) |
| `MenuType.CRAFTER_3X3` | 9 | Crafter menu |
| `MenuType.ANVIL` | 3 | Anvil (2 input + 1 output) |
| `MenuType.BEACON` | 1 | Beacon configuration |
| `MenuType.BLAST_FURNACE` | 3 | Blast furnace |
| `MenuType.BREWING` | 5 | Brewing stand |
| `MenuType.CRAFTING` | 10 | Crafting table (9 grid + 1 output) |
| `MenuType.ENCHANTMENT` | 2 | Enchanting table |
| `MenuType.FURNACE` | 3 | Furnace |
| `MenuType.GRINDSTONE` | 3 | Grindstone |
| `MenuType.HOPPER` | 5 | Hopper |
| `MenuType.LECTERN` | 1 | Lectern (book reading) |
| `MenuType.LOOM` | 4 | Loom (banner crafting) |
| `MenuType.MERCHANT` | 3 | Villager/wandering trader |
| `MenuType.SHULKER_BOX` | 27 | Shulker box |
| `MenuType.SMITHING` | 4 | Smithing table |
| `MenuType.SMOKER` | 3 | Smoker |
| `MenuType.CARTOGRAPHY` | 3 | Cartography table |
| `MenuType.STONECUTTER` | 2 | Stonecutter |

### MenuType Methods

| Method | Return | Description |
|--------|--------|-------------|
| `create(@NotNull HumanEntity player, @NotNull Component title)` | `@NotNull InventoryView` | Open a GUI for the player |
| `typed()` | `@NotNull MenuType.Typed<V>` | Get the typed version |
| `typed(@NotNull Class<V> clazz)` | `@NotNull MenuType.Typed<V>` | Typed with explicit view class |
| `getInventoryViewClass()` | `@NotNull Class<?>` | The InventoryView subclass |
| `getKey()` | `@NotNull NamespacedKey` | Registry key |

### Usage

```java
// Modern way to open a GUI (Paper 26.1 preferred)
InventoryView view = MenuType.GENERIC_9X3.create(player,
    Component.text("My Menu", NamedTextColor.DARK_PURPLE));

// Then populate via the inventory
Inventory inv = view.getTopInventory();
inv.setItem(13, myItem);
```

---

## Inventory

```java
// Interface: org.bukkit.inventory.Inventory
// Extends: Iterable<ItemStack>
```

### Core Methods

| Method | Return | Description |
|--------|--------|-------------|
| `getSize()` | `int` | Total number of slots |
| `getMaxStackSize()` | `int` | Max stack size for this inventory |
| `setMaxStackSize(int size)` | `void` | Override max stack size |
| `getType()` | `@NotNull InventoryType` | Type of inventory |
| `getHolder()` | `@Nullable InventoryHolder` | The holder (chest, player, or custom) |
| `getHolder(boolean useSnapshot)` | `@Nullable InventoryHolder` | Paper: optionally get snapshot holder |
| `getViewers()` | `@NotNull List<HumanEntity>` | Currently viewing players |
| `isEmpty()` | `boolean` | True if no items |

### Item Access

| Method | Return | Description |
|--------|--------|-------------|
| `getItem(int index)` | `@Nullable ItemStack` | Get item at slot |
| `setItem(int index, @Nullable ItemStack item)` | `void` | Set item at slot |
| `getContents()` | `@NotNull ItemStack @NotNull []` | All items |
| `setContents(@NotNull ItemStack[] items)` | `void` | Replace all items |
| `getStorageContents()` | `@NotNull ItemStack @NotNull []` | Storage-only items |
| `setStorageContents(@NotNull ItemStack[] items)` | `void` | Set storage items |

### Item Management

| Method | Return | Description |
|--------|--------|-------------|
| `addItem(@NotNull ItemStack... items)` | `@NotNull HashMap<Integer, ItemStack>` | Add items, returns overflow |
| `removeItem(@NotNull ItemStack... items)` | `@NotNull HashMap<Integer, ItemStack>` | Remove items, returns unremoved |
| `removeItemAnySlot(@NotNull ItemStack... items)` | `@NotNull HashMap<Integer, ItemStack>` | Paper: remove from any slot |
| `clear()` | `void` | Clear all items |
| `clear(int index)` | `void` | Clear specific slot |
| `close()` | `int` | Paper: close for all viewers, returns count |

### Search

| Method | Return | Description |
|--------|--------|-------------|
| `contains(@NotNull Material material)` | `boolean` | Has any of this material? |
| `contains(@Nullable ItemStack item)` | `boolean` | Has matching item? |
| `contains(@NotNull Material material, int amount)` | `boolean` | Has at least N of this material |
| `contains(@Nullable ItemStack item, int amount)` | `boolean` | Has at least N matching stacks |
| `containsAtLeast(@Nullable ItemStack item, int amount)` | `boolean` | Has at least N total across stacks |
| `first(@NotNull Material material)` | `int` | First slot index with material |
| `first(@NotNull ItemStack item)` | `int` | First slot with matching stack |
| `firstEmpty()` | `int` | First empty slot (-1 if full) |
| `all(@NotNull Material material)` | `@NotNull HashMap<Integer, ? extends ItemStack>` | All matching slots |
| `all(@Nullable ItemStack item)` | `@NotNull HashMap<Integer, ? extends ItemStack>` | All matching stacks |

### Creation Methods (Bukkit static)

```java
// Classic creation (still works, widely used)
Inventory inv = Bukkit.createInventory(holder, 27, Component.text("Title"));
Inventory inv = Bukkit.createInventory(holder, InventoryType.HOPPER, Component.text("Title"));

// Deprecated string-based
Inventory inv = Bukkit.createInventory(holder, 27, "Title"); // ⚠️ Deprecated
```

---

## InventoryView

```java
// Abstract class: org.bukkit.inventory.InventoryView
```

### Key Methods

| Method | Return | Description |
|--------|--------|-------------|
| `getTopInventory()` | `@NotNull Inventory` | The upper inventory (chest, furnace, etc.) |
| `getBottomInventory()` | `@NotNull Inventory` | The lower inventory (player's) |
| `getPlayer()` | `@NotNull HumanEntity` | The viewing player |
| `getType()` | `@NotNull InventoryType` | Type of this view |
| `title()` | `@NotNull Component` | Title (Adventure) |
| `getTitle()` | `@NotNull String` | ⚠️ Deprecated — use `title()` |
| `setTitle(@NotNull String title)` | `void` | ⚠️ Deprecated |
| `getItem(int rawSlot)` | `@Nullable ItemStack` | Get item at raw slot |
| `setItem(int rawSlot, @Nullable ItemStack item)` | `void` | Set item at raw slot |
| `setCursor(@Nullable ItemStack item)` | `void` | Set cursor item |
| `getCursor()` | `@Nullable ItemStack` | Get cursor item |
| `convertSlot(int rawSlot)` | `int` | Convert raw slot to inventory-local slot |
| `getSlotType(int rawSlot)` | `@NotNull InventoryType.SlotType` | Slot classification |
| `close()` | `void` | Close the view |
| `countSlots()` | `int` | Total slots across both inventories |
| `setProperty(@NotNull Property prop, int value)` | `boolean` | Set a GUI property |

### InventoryView.Property Enum

| Property | Used In | Description |
|----------|---------|-------------|
| `BREW_TIME` | Brewing Stand | Remaining brew time |
| `FUEL_TIME` | Brewing Stand | Fuel remaining |
| `BURN_TIME` | Furnace | Remaining burn time |
| `COOK_TIME` | Furnace | Cook progress |
| `TICKS_FOR_CURRENT_FUEL` | Furnace | Ticks current fuel provides |
| `TICKS_FOR_CURRENT_SMELTING` | Furnace | Total cook time for current item |
| `ENCHANT_BUTTON1` | Enchantment | Enchant cost slot 1 |
| `ENCHANT_BUTTON2` | Enchantment | Enchant cost slot 2 |
| `ENCHANT_BUTTON3` | Enchantment | Enchant cost slot 3 |
| `ENCHANT_LEVEL1` | Enchantment | Enchant level slot 1 |
| `ENCHANT_LEVEL2` | Enchantment | Enchant level slot 2 |
| `ENCHANT_LEVEL3` | Enchantment | Enchant level slot 3 |
| `ENCHANT_ID1` | Enchantment | Enchant ID slot 1 |
| `ENCHANT_ID2` | Enchantment | Enchant ID slot 2 |
| `ENCHANT_ID3` | Enchantment | Enchant ID slot 3 |
| `LEVELS` | Beacon | Level of beacon pyramid |
| `PRIMARY_EFFECT` | Beacon | Primary power |
| `SECONDARY_EFFECT` | Beacon | Secondary power |
| `REPAIR_COST` | Anvil | XP level cost |
| `BOOK_PAGE` | Lectern | Current page |

---

## ItemStack

```java
// Class: org.bukkit.inventory.ItemStack
// Javadoc: https://jd.papermc.io/paper/26.1.2/org/bukkit/inventory/ItemStack.html
```

### Constructors

```java
new ItemStack(Material.DIAMOND_SWORD)
new ItemStack(Material.STONE, 64)
ItemStack.of(Material.DIAMOND_SWORD) // Paper: immutable shorthand
ItemStack.of(Material.DIAMOND_SWORD, 5) // Paper: with amount
```

### Core Methods

| Method | Return | Description |
|--------|--------|-------------|
| `getType()` | `@NotNull Material` | The material type |
| `setType(@NotNull Material type)` | `void` | Change material |
| `getAmount()` | `int` | Stack count |
| `setAmount(int amount)` | `void` | Set stack count (0 = remove) |
| `getMaxStackSize()` | `int` | Max this can stack to |
| `isEmpty()` | `boolean` | Is this air or amount 0? |
| `isSimilar(@Nullable ItemStack stack)` | `boolean` | Same type and meta (ignores amount) |
| `clone()` | `@NotNull ItemStack` | Deep copy |

### ItemMeta (Legacy)

| Method | Return | Description |
|--------|--------|-------------|
| `getItemMeta()` | `@Nullable ItemMeta` | Get metadata copy |
| `setItemMeta(@Nullable ItemMeta meta)` | `boolean` | Apply metadata |
| `hasItemMeta()` | `boolean` | Has non-empty metadata |
| `editMeta(Consumer<? super ItemMeta> consumer)` | `void` | Paper: edit meta inline |
| `editMeta(Class<M> metaClass, Consumer<? super M> consumer)` | `boolean` | Paper: typed edit |

### Data Components (Paper 26.1)

| Method | Return | Description |
|--------|--------|-------------|
| `getData(DataComponentType.Valued<V> type)` | `@Nullable V` | Get component value |
| `setData(DataComponentType.Valued<V> type, V value)` | `void` | Set component value |
| `hasData(DataComponentType type)` | `boolean` | Check if component present |
| `unsetData(DataComponentType type)` | `void` | Remove component |
| `getDataTypes()` | `@NotNull Set<DataComponentType>` | All set components |
| `matchesWithoutData(@NotNull ItemStack other, @NotNull Set<DataComponentType> exclude)` | `boolean` | Compare ignoring certain components |
| `isDataOverridden(DataComponentType.Valued<V> type)` | `boolean` | Has been overridden from default? |
| `resetData(DataComponentType type)` | `void` | Reset to default value |

### Enchantments

| Method | Return | Description |
|--------|--------|-------------|
| `addEnchantment(@NotNull Enchantment ench, int level)` | `void` | Add (must respect limits) |
| `addUnsafeEnchantment(@NotNull Enchantment ench, int level)` | `void` | Add (bypass limits) |
| `removeEnchantment(@NotNull Enchantment ench)` | `int` | Remove, returns old level |
| `removeEnchantments()` | `void` | Paper: remove all enchantments |
| `getEnchantments()` | `@NotNull Map<Enchantment, Integer>` | All enchantments |
| `containsEnchantment(@NotNull Enchantment ench)` | `boolean` | Has this enchantment? |
| `getEnchantmentLevel(@NotNull Enchantment ench)` | `int` | Level (0 if absent) |

### Serialization

| Method | Return | Description |
|--------|--------|-------------|
| `serialize()` | `@NotNull Map<String, Object>` | Serialize to map |
| `deserialize(Map<String, Object>)` | `static ItemStack` | Deserialize |
| `serializeAsBytes()` | `byte[]` | Paper: binary serialization |
| `deserializeBytes(byte[] bytes)` | `static ItemStack` | Paper: from bytes |
| `ensureServerConversions()` | `@NotNull ItemStack` | Paper: normalize for server |

### Utility

| Method | Return | Description |
|--------|--------|-------------|
| `empty()` | `static @NotNull ItemStack` | An empty stack (not null) |
| `asHoverEvent()` | `@NotNull HoverEvent<HoverEvent.ShowItem>` | Adventure hover event |
| `displayName()` | `@NotNull Component` | Computed display name |
| `translationKey()` | `@NotNull String` | Translation key |
| `getI18NDisplayName()` | `@Nullable String` | Paper: localized display name |
| `asOne()` | `@NotNull ItemStack` | Paper: copy with amount=1 |
| `asQuantity(int qty)` | `@NotNull ItemStack` | Paper: copy with specific amount |
| `add()` | `@NotNull ItemStack` | Paper: increment amount by 1 |
| `add(int qty)` | `@NotNull ItemStack` | Paper: increment amount |
| `subtract()` | `@NotNull ItemStack` | Paper: decrement by 1 |
| `subtract(int qty)` | `@NotNull ItemStack` | Paper: decrement |
| `damage(int amount, LivingEntity entity)` | `@NotNull ItemStack` | Paper: apply durability damage |
| `withType(@NotNull Material type)` | `@NotNull ItemStack` | Paper: copy with new type |
| `enchantWithLevels(int levels, RegistryKeySet<@NotNull Enchantment> allowed, Random random)` | `void` | Paper: enchant at table levels |
| `getMaxItemUseDuration(@NotNull LivingEntity entity)` | `int` | Max use duration |
| `getLootTable()` | `@Nullable LootTable` | Paper: container loot table |
| `setLootTable(@Nullable LootTable table)` | `void` | Paper: set loot table |

---

## ItemMeta

```java
// Interface: org.bukkit.inventory.meta.ItemMeta
// Javadoc: https://jd.papermc.io/paper/26.1.2/org/bukkit/inventory/meta/ItemMeta.html
```

### Display

| Method | Return | Description |
|--------|--------|-------------|
| `displayName()` | `@Nullable Component` | Display name (Adventure) |
| `displayName(@Nullable Component name)` | `void` | Set display name |
| `hasDisplayName()` | `boolean` | Has custom display name |
| `getDisplayName()` | `@NotNull String` | ⚠️ Deprecated |
| `setDisplayName(@Nullable String name)` | `void` | ⚠️ Deprecated |
| `itemName()` | `@Nullable Component` | Paper: item name (not italic) |
| `itemName(@Nullable Component name)` | `void` | Paper: set item name |
| `hasItemName()` | `boolean` | Has item name |
| `lore()` | `@Nullable List<Component>` | Lore lines (Adventure) |
| `lore(@Nullable List<? extends Component> lore)` | `void` | Set lore |
| `hasLore()` | `boolean` | Has lore |
| `getLore()` | `@Nullable List<String>` | ⚠️ Deprecated |
| `setLore(@Nullable List<String> lore)` | `void` | ⚠️ Deprecated |

### Model Data

| Method | Return | Description |
|--------|--------|-------------|
| `hasCustomModelData()` | `boolean` | Has custom model data |
| `getCustomModelData()` | `int` | Get custom model data |
| `setCustomModelData(@Nullable Integer data)` | `void` | Set custom model data |
| `hasCustomModelDataComponent()` | `boolean` | Paper: has model data component |
| `getCustomModelDataComponent()` | `@NotNull CustomModelData` | Paper: get model data component |
| `setCustomModelDataComponent(@Nullable CustomModelData data)` | `void` | Paper: set model data component |

### Enchantments & Flags

| Method | Return | Description |
|--------|--------|-------------|
| `addEnchant(@NotNull Enchantment ench, int level, boolean ignoreLevelRestriction)` | `boolean` | Add enchantment |
| `removeEnchant(@NotNull Enchantment ench)` | `boolean` | Remove enchantment |
| `removeEnchantments()` | `void` | Remove all |
| `hasEnchant(@NotNull Enchantment ench)` | `boolean` | Has enchantment |
| `getEnchantLevel(@NotNull Enchantment ench)` | `int` | Get level |
| `getEnchants()` | `@NotNull Map<Enchantment, Integer>` | All enchantments |
| `hasEnchants()` | `boolean` | Has any enchantments |
| `hasConflictingEnchant(@NotNull Enchantment ench)` | `boolean` | Conflicts with existing |
| `addItemFlags(@NotNull ItemFlag... flags)` | `void` | Add display flags |
| `removeItemFlags(@NotNull ItemFlag... flags)` | `void` | Remove display flags |
| `getItemFlags()` | `@NotNull Set<ItemFlag>` | All flags |
| `hasItemFlag(@NotNull ItemFlag flag)` | `boolean` | Has flag |

### Durability & Modifiers

| Method | Return | Description |
|--------|--------|-------------|
| `isUnbreakable()` | `boolean` | Is unbreakable |
| `setUnbreakable(boolean unbreakable)` | `void` | Set unbreakable |
| `hasDamage()` | `boolean` | Has durability damage |
| `getDamage()` | `int` | Current damage |
| `setDamage(int damage)` | `void` | Set damage |
| `hasMaxDamage()` | `boolean` | Has custom max damage |
| `getMaxDamage()` | `int` | Max damage |
| `setMaxDamage(@Nullable Integer maxDamage)` | `void` | Set max damage |
| `hasAttributeModifiers()` | `boolean` | Has attribute mods |
| `getAttributeModifiers()` | `@Nullable Multimap<Attribute, AttributeModifier>` | All modifiers |
| `setAttributeModifiers(@Nullable Multimap<Attribute, AttributeModifier>)` | `void` | Set modifiers |
| `addAttributeModifier(@NotNull Attribute attr, @NotNull AttributeModifier mod)` | `boolean` | Add modifier |
| `removeAttributeModifier(@NotNull Attribute attr)` | `boolean` | Remove all mods for attribute |
| `removeAttributeModifier(@NotNull Attribute attr, @NotNull AttributeModifier mod)` | `boolean` | Remove specific modifier |

### Persistent Data Container (PDC)

| Method | Return | Description |
|--------|--------|-------------|
| `getPersistentDataContainer()` | `@NotNull PersistentDataContainer` | Access the PDC |

### Misc

| Method | Return | Description |
|--------|--------|-------------|
| `hasEnchantmentGlintOverride()` | `boolean` | Has glint override |
| `getEnchantmentGlintOverride()` | `@NotNull Boolean` | Get glint override |
| `setEnchantmentGlintOverride(@Nullable Boolean override)` | `void` | Set glint override |
| `isHideTooltip()` | `boolean` | Hide entire tooltip |
| `setHideTooltip(boolean hideTooltip)` | `void` | Set tooltip hiding |
| `hasFood()` | `boolean` | Has food component |
| `getFood()` | `@NotNull FoodComponent` | Get food data |
| `setFood(@Nullable FoodComponent food)` | `void` | Set food data |
| `hasTool()` | `boolean` | Has tool component |
| `getTool()` | `@NotNull ToolComponent` | Get tool data |
| `setTool(@Nullable ToolComponent tool)` | `void` | Set tool data |
| `hasJukeboxPlayable()` | `boolean` | Has jukebox data |
| `getJukeboxPlayable()` | `@NotNull JukeboxPlayableComponent` | Get jukebox data |
| `setJukeboxPlayable(@Nullable JukeboxPlayableComponent comp)` | `void` | Set jukebox data |
| `isFireResistant()` | `boolean` | Fire resistant |
| `setFireResistant(boolean resistant)` | `void` | Set fire resistant |
| `hasMaxStackSize()` | `boolean` | Has custom max stack size |
| `getMaxStackSize()` | `int` | Get max stack size |
| `setMaxStackSize(@Nullable Integer max)` | `void` | Set max stack size |
| `hasRarity()` | `boolean` | Has custom rarity |
| `getRarity()` | `@NotNull ItemRarity` | Get rarity |
| `setRarity(@Nullable ItemRarity rarity)` | `void` | Set rarity |
| `hasUseRemainder()` | `boolean` | Has use remainder |
| `getUseRemainder()` | `@NotNull ItemStack` | Get remainder after use |
| `setUseRemainder(@Nullable ItemStack remainder)` | `void` | Set remainder |
| `hasUseCooldown()` | `boolean` | Has use cooldown |
| `getUseCooldown()` | `@NotNull UseCooldownComponent` | Get cooldown |
| `setUseCooldown(@Nullable UseCooldownComponent cooldown)` | `void` | Set cooldown |
| `hasEquippable()` | `boolean` | Has equippable component |
| `getEquippable()` | `@NotNull EquippableComponent` | Get equippable data |
| `setEquippable(@Nullable EquippableComponent equippable)` | `void` | Set equippable |
| `clone()` | `@NotNull ItemMeta` | Deep copy |

### Specialized ItemMeta Subinterfaces

| Interface | Used For |
|-----------|---------|
| `BookMeta` | Written books / writable books |
| `EnchantmentStorageMeta` | Enchanted books |
| `PotionMeta` | Potions, tipped arrows |
| `SkullMeta` | Player heads |
| `LeatherArmorMeta` | Leather armor coloring |
| `MapMeta` | Filled maps |
| `FireworkMeta` | Fireworks |
| `FireworkEffectMeta` | Firework stars |
| `BannerMeta` | Banners |
| `BlockStateMeta` | Blocks with stored state |
| `BundleMeta` | Bundles |
| `ArmorMeta` | Armor trim |
| `ColorableArmorMeta` | Armor with color + trim |
| `MusicInstrumentMeta` | Goat horns |
| `ShieldMeta` | Shields |
| `OminousBottleMeta` | Ominous bottles |
| `SpawnEggMeta` | Spawn eggs |
| `TropicalFishBucketMeta` | Tropical fish buckets |
| `SuspiciousStewMeta` | Suspicious stew |
| `AxolotlBucketMeta` | Axolotl buckets |
| `CrossbowMeta` | Crossbows |
| `KnowledgeBookMeta` | Knowledge books |
| `WritableBookMeta` | Book and quill |

---

## DataComponentTypes (Paper)

```java
// Class: io.papermc.paper.datacomponent.DataComponentTypes
// Paper-exclusive modern API for item components
```

> [!IMPORTANT]
> DataComponentTypes is Paper 26.1's preferred way to manipulate item data. It replaces many ItemMeta methods with strongly-typed, composable data components.

### Common DataComponentTypes

| Type Constant | Value Type | Description |
|---------------|-----------|-------------|
| `DataComponentTypes.CUSTOM_NAME` | `Component` | Custom display name (italic by default) |
| `DataComponentTypes.ITEM_NAME` | `Component` | Item name (NOT italic, like vanilla) |
| `DataComponentTypes.LORE` | `ItemLore` | Lore lines |
| `DataComponentTypes.MAX_STACK_SIZE` | `Integer` | Max stack size (default 64) |
| `DataComponentTypes.MAX_DAMAGE` | `Integer` | Max durability |
| `DataComponentTypes.DAMAGE` | `Integer` | Current damage |
| `DataComponentTypes.UNBREAKABLE` | `Unbreakable` | Unbreakable marker |
| `DataComponentTypes.CUSTOM_MODEL_DATA` | `CustomModelData` | Custom model data |
| `DataComponentTypes.HIDE_TOOLTIP` | `Unit` | Hide entire tooltip |
| `DataComponentTypes.HIDE_ADDITIONAL_TOOLTIP` | `Unit` | Hide extra tooltip info |
| `DataComponentTypes.REPAIR_COST` | `Integer` | Anvil repair cost |
| `DataComponentTypes.ENCHANTMENTS` | `ItemEnchantments` | Enchantments map |
| `DataComponentTypes.STORED_ENCHANTMENTS` | `ItemEnchantments` | Stored enchantments (books) |
| `DataComponentTypes.DYED_COLOR` | `DyedItemColor` | Dyed color (leather armor) |
| `DataComponentTypes.ATTRIBUTE_MODIFIERS` | `ItemAttributeModifiers` | Attribute modifiers |
| `DataComponentTypes.FOOD` | `FoodProperties` | Food component |
| `DataComponentTypes.TOOL` | `Tool` | Tool component |
| `DataComponentTypes.ENCHANTMENT_GLINT_OVERRIDE` | `Boolean` | Force glint on/off |
| `DataComponentTypes.RARITY` | `ItemRarity` | Item rarity |
| `DataComponentTypes.FIRE_RESISTANT` | `Unit` | Fire resistant |
| `DataComponentTypes.TRIM` | `ItemArmorTrim` | Armor trim |
| `DataComponentTypes.CHARGED_PROJECTILES` | `ChargedProjectiles` | Crossbow ammo |
| `DataComponentTypes.BUNDLE_CONTENTS` | `BundleContents` | Bundle items |
| `DataComponentTypes.POTION_CONTENTS` | `PotionContents` | Potion data |
| `DataComponentTypes.MAP_COLOR` | `MapItemColor` | Map background color |
| `DataComponentTypes.MAP_DECORATIONS` | `MapDecorations` | Map icons |
| `DataComponentTypes.MAP_ID` | `MapId` | Map ID number |
| `DataComponentTypes.WRITABLE_BOOK_CONTENT` | `WritableBookContent` | Book & quill pages |
| `DataComponentTypes.WRITTEN_BOOK_CONTENT` | `WrittenBookContent` | Written book data |
| `DataComponentTypes.CONTAINER` | `ItemContainerContents` | Container items |
| `DataComponentTypes.CONTAINER_LOOT` | `SeededContainerLoot` | Container loot table |
| `DataComponentTypes.PROFILE` | `ResolvableProfile` | Player head profile |
| `DataComponentTypes.NOTE_BLOCK_SOUND` | `Key` | Note block sound |
| `DataComponentTypes.BANNER_PATTERNS` | `BannerPatternLayers` | Banner patterns |
| `DataComponentTypes.BASE_COLOR` | `DyeColor` | Base banner color |
| `DataComponentTypes.POT_DECORATIONS` | `PotDecorations` | Decorated pot shards |
| `DataComponentTypes.FIREWORKS` | `Fireworks` | Firework rockets |
| `DataComponentTypes.FIREWORK_EXPLOSION` | `FireworkEffect` | Firework star |
| `DataComponentTypes.LODESTONE_TRACKER` | `LodestoneTracker` | Lodestone compass |
| `DataComponentTypes.SUSPICIOUS_STEW_EFFECTS` | `SuspiciousStewEffects` | Stew effects |
| `DataComponentTypes.CREATIVE_SLOT_LOCK` | `Unit` | Lock in creative |
| `DataComponentTypes.INTANGIBLE_PROJECTILE` | `Unit` | Non-pickup projectile |
| `DataComponentTypes.RECIPES` | `List<Key>` | Unlockable recipes |
| `DataComponentTypes.INSTRUMENT` | `MusicInstrument` | Goat horn |
| `DataComponentTypes.OMINOUS_BOTTLE_AMPLIFIER` | `Integer` | Bad omen amplifier |
| `DataComponentTypes.JUKEBOX_PLAYABLE` | `JukeboxPlayable` | Jukebox disc |
| `DataComponentTypes.EQUIPPABLE` | `Equippable` | Equippable config |
| `DataComponentTypes.GLIDER` | `Unit` | Elytra glider marker |
| `DataComponentTypes.TOOLTIP_STYLE` | `Key` | Custom tooltip style |
| `DataComponentTypes.DEATH_PROTECTION` | `DeathProtection` | Totem of Undying |
| `DataComponentTypes.CONSUMABLE` | `Consumable` | Consumable behavior |
| `DataComponentTypes.USE_COOLDOWN` | `UseCooldown` | Cooldown after use |
| `DataComponentTypes.USE_REMAINDER` | `ItemStack` | Item left after use |
| `DataComponentTypes.DAMAGE_RESISTANT` | `DamageResistant` | Damage type resistance |
| `DataComponentTypes.ENCHANTABLE` | `Integer` | Enchantability value |
| `DataComponentTypes.REPAIRABLE` | `Repairable` | Repair material set |
| `DataComponentTypes.BLOCKS_ATTACKS` | `BlocksAttacks` | Shield blocking |

### Usage Pattern

```java
import io.papermc.paper.datacomponent.DataComponentTypes;
import io.papermc.paper.datacomponent.item.ItemLore;

ItemStack item = new ItemStack(Material.DIAMOND_SWORD);

// Set custom name
item.setData(DataComponentTypes.CUSTOM_NAME,
    Component.text("Excalibur", NamedTextColor.GOLD, TextDecoration.BOLD));

// Set lore
item.setData(DataComponentTypes.LORE, ItemLore.lore(List.of(
    Component.text("A legendary blade", NamedTextColor.GRAY),
    Component.text("Forged in dragon fire", NamedTextColor.DARK_PURPLE)
)));

// Set max stack size
item.setData(DataComponentTypes.MAX_STACK_SIZE, 1);

// Add enchantment glint without enchantments
item.setData(DataComponentTypes.ENCHANTMENT_GLINT_OVERRIDE, true);

// Read data
Component name = item.getData(DataComponentTypes.CUSTOM_NAME);
boolean hasGlint = item.hasData(DataComponentTypes.ENCHANTMENT_GLINT_OVERRIDE);

// Remove data
item.unsetData(DataComponentTypes.CUSTOM_NAME);
```

---

## InventoryHolder & Custom GUIs

```java
// Interface: org.bukkit.inventory.InventoryHolder
// Single method: Inventory getInventory()
```

### Pattern: Custom GUI Holder

```java
public class ShopMenuHolder implements InventoryHolder {
    private final Inventory inventory;
    private final String shopId;

    public ShopMenuHolder(String shopId, Component title) {
        this.shopId = shopId;
        this.inventory = Bukkit.createInventory(this, 54, title);
    }

    @Override
    public @NotNull Inventory getInventory() {
        return inventory;
    }

    public String getShopId() {
        return shopId;
    }
}
```

---

## Inventory Events

```java
// Package: org.bukkit.event.inventory
```

### Event Hierarchy

| Event | When Fired |
|-------|------------|
| `InventoryOpenEvent` | Player opens an inventory |
| `InventoryCloseEvent` | Player closes an inventory |
| `InventoryClickEvent` | Player clicks a slot |
| `InventoryDragEvent` | Player drags items across slots |
| `InventoryMoveItemEvent` | Hopper/dropper moves item between inventories |
| `InventoryCreativeEvent` | Player sets item in creative mode |
| `InventoryInteractEvent` | Base class for click/drag |
| `PrepareItemCraftEvent` | Crafting grid updated |
| `CraftItemEvent` | Player crafts an item |
| `PrepareAnvilEvent` | Anvil output preview |
| `PrepareSmithingEvent` | Smithing output preview |
| `PrepareGrindstoneEvent` | Grindstone output preview |
| `SmithItemEvent` | Player takes smithing output |
| `FurnaceBurnEvent` | Fuel starts burning |
| `FurnaceSmeltEvent` | Item finishes smelting |
| `FurnaceExtractEvent` | Player takes from furnace output |
| `FurnaceStartSmeltEvent` | Smelting begins |
| `BrewEvent` | Brewing completes |
| `BrewingStandFuelEvent` | Fuel added to brewing stand |
| `TradeSelectEvent` | Player selects villager trade |
| `HopperInventorySearchEvent` | Hopper searches for inventory (Paper) |

### InventoryClickEvent — Key Methods

| Method | Return | Description |
|--------|--------|-------------|
| `getSlot()` | `int` | Clicked slot index (inventory-local) |
| `getRawSlot()` | `int` | Raw slot index (across both inventories) |
| `getSlotType()` | `@NotNull InventoryType.SlotType` | CONTAINER, QUICKBAR, ARMOR, etc. |
| `getClick()` | `@NotNull ClickType` | Type of click performed |
| `getAction()` | `@NotNull InventoryAction` | What the click does |
| `getCurrentItem()` | `@Nullable ItemStack` | Item in clicked slot |
| `setCurrentItem(@Nullable ItemStack item)` | `void` | Replace item in clicked slot |
| `getCursor()` | `@NotNull ItemStack` | Item on cursor |
| `setCursor(@Nullable ItemStack item)` | `void` | Set cursor item |
| `getClickedInventory()` | `@Nullable Inventory` | Which inventory was clicked |
| `getHotbarButton()` | `int` | Hotbar key pressed (-1 if none) |
| `isLeftClick()` | `boolean` | Was left click? |
| `isRightClick()` | `boolean` | Was right click? |
| `isShiftClick()` | `boolean` | Was shift held? |
| `getView()` | `@NotNull InventoryView` | The inventory view |
| `getWhoClicked()` | `@NotNull HumanEntity` | The clicker |
| `getInventory()` | `@NotNull Inventory` | Top inventory |
| `setCancelled(boolean cancel)` | `void` | Cancel the click |
| `isCancelled()` | `boolean` | Is cancelled? |

### ClickType Enum

| Constant | Description |
|----------|-------------|
| `LEFT` | Left click |
| `RIGHT` | Right click |
| `SHIFT_LEFT` | Shift + left click |
| `SHIFT_RIGHT` | Shift + right click |
| `MIDDLE` | Middle click (creative pick) |
| `NUMBER_KEY` | Number key press (1–9) |
| `DOUBLE_CLICK` | Double left click (collect) |
| `DROP` | Q key (drop single) |
| `CONTROL_DROP` | Ctrl+Q (drop stack) |
| `CREATIVE` | Creative mode action |
| `SWAP_OFFHAND` | F key (swap offhand) |
| `WINDOW_BORDER_LEFT` | Left click outside window |
| `WINDOW_BORDER_RIGHT` | Right click outside window |
| `UNKNOWN` | Unknown click type |

### InventoryAction Enum

| Constant | Description |
|----------|-------------|
| `NOTHING` | No action |
| `PICKUP_ALL` | Pick up entire stack |
| `PICKUP_SOME` | Pick up partial stack |
| `PICKUP_HALF` | Pick up half |
| `PICKUP_ONE` | Pick up one |
| `PLACE_ALL` | Place entire cursor stack |
| `PLACE_SOME` | Place as many as possible |
| `PLACE_ONE` | Place one from cursor |
| `SWAP_WITH_CURSOR` | Swap slot and cursor |
| `DROP_ALL_CURSOR` | Drop entire cursor stack |
| `DROP_ONE_CURSOR` | Drop one from cursor |
| `DROP_ALL_SLOT` | Drop entire slot stack |
| `DROP_ONE_SLOT` | Drop one from slot |
| `MOVE_TO_OTHER_INVENTORY` | Shift-click transfer |
| `HOTBAR_MOVE_AND_READD` | Hotbar swap (move old to hotbar, old hotbar to inv) |
| `HOTBAR_SWAP` | Simple hotbar swap |
| `CLONE_STACK` | Creative middle-click clone |
| `COLLECT_TO_CURSOR` | Double-click collect |
| `UNKNOWN` | Unknown |

### InventoryCloseEvent.Reason Enum (Paper)

| Constant | Description |
|----------|-------------|
| `PLAYER` | Player closed it (Escape/inventory key) |
| `PLUGIN` | Plugin called `close()` |
| `CANT_USE` | Player can't use (e.g. enchant table destroyed) |
| `DISCONNECT` | Player disconnected |
| `DEATH` | Player died |
| `OPEN_NEW` | Another inventory was opened |## Code Examples

### Example 1: Standard Shop GUI (HouziCore Standard)

This demonstrates the correct way to construct a GUI in HouziCore using `ShopBase` and `ShopPageBase` with Paper 26.1 `DataComponentTypes` styling:

```java
package com.houzicore.lobby.hub.modules.myfeature;

import com.houzicore.shared.account.CoreClientManager;
import com.houzicore.shared.core.donation.DonationManager;
import com.houzicore.shared.core.itemstack.ItemStackFactory;
import com.houzicore.shared.core.shop.ShopBase;
import com.houzicore.shared.core.shop.page.ShopPageBase;
import io.papermc.paper.datacomponent.DataComponentTypes;
import io.papermc.paper.datacomponent.item.Unbreakable;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class MyFeatureShop extends ShopBase<MyFeatureManager> {
    public MyFeatureShop(MyFeatureManager plugin, CoreClientManager clientManager, DonationManager donationManager) {
        super(plugin, clientManager, donationManager, "✦ Feature Shop ✦");
    }

    @Override
    protected ShopPageBase<MyFeatureManager, MyFeatureShop> buildPagesFor(Player player) {
        return new MyFeatureShopPage(getPlugin(), this, getClientManager(), getDonationManager(), player);
    }
}
```

```java
package com.houzicore.lobby.hub.modules.myfeature;

import com.houzicore.shared.account.CoreClientManager;
import com.houzicore.shared.core.donation.DonationManager;
import com.houzicore.shared.core.itemstack.ItemStackFactory;
import com.houzicore.shared.core.shop.page.ShopPageBase;
import io.papermc.paper.datacomponent.DataComponentTypes;
import io.papermc.paper.datacomponent.item.Unbreakable;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class MyFeatureShopPage extends ShopPageBase<MyFeatureManager, MyFeatureShop> {

    public MyFeatureShopPage(MyFeatureManager plugin, MyFeatureShop shop, CoreClientManager clientManager,
                             DonationManager donationManager, Player player) {
        super(plugin, shop, clientManager, donationManager, "My Page", player, 54);
        buildPage();
    }

    @Override
    protected void buildPage() {
        // 1. Mandatory blue glass border (slot padding)
        long currency = getDonationManager().Get(getPlayer()).getGems();
        ItemStack glass = ItemStackFactory.Instance.CreateStack(
            Material.STAINED_GLASS_PANE, (byte) 3, 1,
            "§bEssence: §a" + currency
        );
        for (int i = 0; i < getSize(); i++) {
            getInventory().setItem(i, glass);
        }

        // 2. Interactive action button
        ItemStack buySword = ItemStackFactory.Instance.CreateStack(
            Material.DIAMOND_SWORD, (byte) 0, 1, "§a§lVoid Reaver",
            new String[]{"§7A legendary blade from the abyss.", "§8───────────", "§eClick to purchase!"}
        );
        // Integrate Paper 26.1 components
        buySword.setData(DataComponentTypes.UNBREAKABLE, Unbreakable.unbreakable(true));
        buySword.setData(DataComponentTypes.ENCHANTMENT_GLINT_OVERRIDE, true);

        addButton(13, buySword, (player, clickType) -> {
            if (getDonationManager().Get(player).getGems() >= 500) {
                // Perform transaction...
                playAcceptSound(player);
                player.sendMessage("§a✓ You purchased Void Reaver!");
                refresh();
            } else {
                playDenySound(player);
                player.sendMessage("§c✗ Insufficient gems!");
            }
        });
    }
}
```

### Example 2: Confirmation Dialog GUI (HouziCore Standard)

Confirmation dialogs should also use the button lambda mechanism of `ShopPageBase`:

```java
package com.houzicore.lobby.hub.modules.myfeature;

import com.houzicore.shared.account.CoreClientManager;
import com.houzicore.shared.core.donation.DonationManager;
import com.houzicore.shared.core.itemstack.ItemStackFactory;
import com.houzicore.shared.core.shop.page.ShopPageBase;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class ConfirmPage extends ShopPageBase<MyFeatureManager, MyFeatureShop> {

    private final Runnable _onConfirm;

    public ConfirmPage(MyFeatureManager plugin, MyFeatureShop shop, CoreClientManager clientManager,
                       DonationManager donationManager, Player player, Runnable onConfirm) {
        super(plugin, shop, clientManager, donationManager, "Confirm Purchase?", player, 27);
        this._onConfirm = onConfirm;
        buildPage();
    }

    @Override
    protected void buildPage() {
        // Fill border with dark gray glass
        ItemStack glass = ItemStackFactory.Instance.CreateStack(
            Material.STAINED_GLASS_PANE, (byte) 7, 1, "§r"
        );
        for (int i = 0; i < getSize(); i++) {
            getInventory().setItem(i, glass);
        }

        // Green confirm button (slot 11)
        ItemStack confirm = ItemStackFactory.Instance.CreateStack(
            Material.LIME_WOOL, (byte) 0, 1, "§a§l✔ CONFIRM"
        );
        addButton(11, confirm, (player, clickType) -> {
            playAcceptSound(player);
            player.closeInventory();
            _onConfirm.run();
        });

        // Red cancel button (slot 15)
        ItemStack cancel = ItemStackFactory.Instance.CreateStack(
            Material.RED_WOOL, (byte) 0, 1, "§c§l✘ CANCEL"
        );
        addButton(15, cancel, (player, clickType) -> {
            playDenySound(player);
            player.closeInventory();
        });
    }
}
```

### Example 3: Applying Advanced Paper 26.1 DataComponentTypes

Shows how to apply various modern Paper data component overrides to custom item stacks returned by `ItemStackFactory`:

```java
import com.houzicore.shared.core.itemstack.ItemStackFactory;
import io.papermc.paper.datacomponent.DataComponentTypes;
import io.papermc.paper.datacomponent.item.ItemRarity;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

public ItemStack buildLegendaryItem() {
    // 1. Create base stack
    ItemStack item = ItemStackFactory.Instance.CreateStack(
        Material.FEATHER, (byte) 0, 1, "§b§lZephyr Wing",
        new String[]{"§7Grants incredible speed."}
    );

    // 2. Apply Paper 26.1-exclusive data component overrides
    item.setData(DataComponentTypes.MAX_STACK_SIZE, 1);
    item.setData(DataComponentTypes.RARITY, ItemRarity.EPIC);
    item.setData(DataComponentTypes.ENCHANTMENT_GLINT_OVERRIDE, true);
    
    return item;
}
```ain inventory
    // Slot 36: Boots
    // Slot 37: Leggings
    // Slot 38: Chestplate
    // Slot 39: Helmet
    // Slot 40: Off-hand
}
```

---

## Slot Reference

### Chest (9×N) Layout

```
Slots 0-8:   Row 1 (top)
Slots 9-17:  Row 2
Slots 18-26: Row 3
Slots 27-35: Row 4
Slots 36-44: Row 5
Slots 45-53: Row 6 (bottom)
```

### Common Slot Patterns

```java
// Center slot formulas
int centerSlot(int rows) {
    return (rows * 9) / 2;  // e.g., row=3 → slot 13
}

// Row of slots
int[] getRow(int row) { // 0-indexed
    int[] slots = new int[9];
    for (int i = 0; i < 9; i++) slots[i] = row * 9 + i;
    return slots;
}

// Column of slots
int[] getColumn(int col, int rows) { // 0-indexed
    int[] slots = new int[rows];
    for (int i = 0; i < rows; i++) slots[i] = i * 9 + col;
    return slots;
}

// Border slots for any size
Set<Integer> getBorderSlots(int rows) {
    Set<Integer> border = new HashSet<>();
    for (int i = 0; i < rows * 9; i++) {
        int row = i / 9;
        int col = i % 9;
        if (row == 0 || row == rows - 1 || col == 0 || col == 8) {
            border.add(i);
        }
    }
    return border;
}
```

---

## Common Pitfalls

| Issue | Solution |
|-------|----------|
| Items movable in custom GUI | Cancel `InventoryClickEvent` + `InventoryDragEvent` |
| Player can shift-click into GUI | Check `event.getClickedInventory()` — handle both top and bottom clicks |
| Number key bypasses cancel | Also check `ClickType.NUMBER_KEY` and `SWAP_OFFHAND` |
| Items disappear on close | Don't use shared Inventory instances if items change per-player |
| `getHolder()` returns wrong type | Use `instanceof` checks, not class equality |
| editMeta has no effect | `editMeta` applies changes immediately — no need to call `setItemMeta` |
| Display name is italic | `CUSTOM_NAME` is italic by default; use `ITEM_NAME` or `.decoration(TextDecoration.ITALIC, false)` |
| DataComponent vs ItemMeta | Both work; don't mix on the same item property |
| MenuType.create doesn't open | It DOES open the inventory; it returns the view for further manipulation |

---

## Important Notes for Paper 26.1

- **`MenuType.create()`** is the modern way to open typed inventory views
- **`editMeta()`** is Paper-exclusive and avoids the `getItemMeta()`/`setItemMeta()` boilerplate
- **`DataComponentTypes`** is the modern replacement for much of ItemMeta — strongly typed, composable
- **`Inventory.close()`** is Paper-exclusive — closes the inventory for all viewers
- **`Inventory.getHolder(boolean useSnapshot)`** is Paper — get snapshot vs. live holder
- **`removeItemAnySlot()`** is Paper-exclusive
- **`InventoryCloseEvent.Reason`** is Paper-exclusive — know WHY the inventory closed
- **Adventure Components** are native — always use `Component` for display names/titles
- **`ItemStack.of(Material)`** creates an immutable snapshot (Paper)
- **`serializeAsBytes()` / `deserializeBytes()`** are Paper-exclusive efficient serialization
