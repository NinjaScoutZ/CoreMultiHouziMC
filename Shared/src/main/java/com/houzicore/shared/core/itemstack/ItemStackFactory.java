package com.houzicore.shared.core.itemstack;

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map.Entry;

import com.houzicore.shared.MiniPlugin;
import com.houzicore.shared.common.util.C;
import com.houzicore.shared.common.util.IdUtil;
import com.houzicore.shared.common.util.UtilInv;
import com.houzicore.shared.common.util.UtilMath;
import com.houzicore.shared.common.util.UtilTime;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.Block;
////import org.bukkit.craftbukkit.v1_21_R1.inventory.org.bukkit.inventory.ItemStack;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Monster;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.EntityShootBowEvent;
import org.bukkit.event.entity.ItemSpawnEvent;
import org.bukkit.event.inventory.FurnaceSmeltEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.PrepareItemCraftEvent;
import org.bukkit.event.inventory.InventoryType.SlotType;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.entity.Player;
import org.bukkit.inventory.CraftingInventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;

public class ItemStackFactory extends MiniPlugin {
	public static ItemStackFactory Instance;

	public static void Initialize(JavaPlugin plugin, boolean customNames) {
		Instance = new ItemStackFactory(plugin, customNames);
	}
	private HashMap<Integer, HashMap<Byte, Entry<String, Boolean>>> _names;

	private HashMap<Integer, HashMap<Byte, String[]>> _lores;

	private String _nameFormat = "§r" + C.mItem;

	private final HashSet<Listener> _statListeners = new HashSet<>();

	private boolean _customNames = false;

	protected ItemStackFactory(JavaPlugin plugin, boolean customNames) {
		super("ItemStack Factory", plugin);

		AddDefault();

		if (customNames) {
			SetCustom();
		}
	}

	private void Add(int id, byte data, String name, boolean special) {
		Add(id, data, name, null, special);
	}

	private void Add(int id, byte data, String name, String[] lore, boolean special) {
		if (!_names.containsKey(id)) {
			_names.put(id, new HashMap<Byte, Entry<String, Boolean>>());
		}

		_names.get(id).put(data, new AbstractMap.SimpleEntry<>(name, special));

		if (lore == null)
			return;

		if (!_lores.containsKey(id)) {
			_lores.put(id, new HashMap<Byte, String[]>());
		}

		_lores.get(id).put(data, lore);
	}

	private void Add(Material mat, byte data, String name, boolean special) {
		Add(IdUtil.getTypeId(mat), data, name, null, special);
	}

	private void Add(Material mat, byte data, String name, String[] lore, boolean special) {
		Add(IdUtil.getTypeId(mat), data, name, lore, special);
	}

	private void AddDefault() {
		_names = new HashMap<>();
		_lores = new HashMap<>();

		for (final Material mat : Material.values()) {
			if (mat.isLegacy()) {
				continue;
			}

			final int id = mat.ordinal();

			// Add Item
			final HashMap<Byte, Entry<String, Boolean>> variants = new HashMap<>();
			_names.put(id, variants);

			try {
				final String name = Clean(mat.toString());
				variants.put((byte) 0, new AbstractMap.SimpleEntry<>(name, mat.getMaxStackSize() == 1));
			} catch (final Exception e) {
				// skip
			}
		}
	}

	public void AddStatListener(Listener listener) {
		_statListeners.add(listener);
		registerEvents(listener);
	}

	public List<String> ArrayToList(String[] array) {
		if (array.length == 0)
			return null;

		final List<String> list = new ArrayList<>();

		for (final String cur : array) {
			list.add(cur);
		}

		return list;
	}

	private String Clean(String string) {
		String out = "";
		final String[] words = string.split("_");

		for (final String word : words) {
			if (word.length() < 1)
				return "Unknown";

			out += word.charAt(0) + word.substring(1, word.length()).toLowerCase() + " ";
		}

		return out.substring(0, out.length() - 1);
	}

	private List<String> CombineLore(List<String> A, List<String> B) {
		for (final String b : B) {
			A.add(b);
		}

		return A;
	}

	public ItemStack CreateStack(int id) {
		return CreateStack(id, (byte) 0, 1, (short) 0, null, new String[] {}, null);
	}

	public ItemStack CreateStack(int id, byte data) {
		return CreateStack(id, data, 1, (short) 0, null, new String[] {}, null);
	}

	public ItemStack CreateStack(int id, byte data, int amount) {
		return CreateStack(id, data, amount, (short) 0, null, new String[] {}, null);
	}

	@SuppressWarnings("deprecation")
	private static Material idToMaterial(int id) {
		for (Material m : Material.values()) {
			if (!m.isLegacy() && m.ordinal() == id) return m;
		}
		return Material.STONE;
	}

	@SuppressWarnings("deprecation")
	public ItemStack CreateStack(int id, byte data, int amount, short damage, String name, List<String> lore,
			String owner) {
		final Material mat = IdUtil.getMaterial(id, data);
		ItemStack stack = new ItemStack(mat, amount);
		if (damage > 0) {
			stack.setDurability(damage);
		}

		final ItemMeta itemMeta = stack.getItemMeta();

		if (itemMeta == null)
			return null;

		boolean setMeta = false;

		// Set Name
		if (name != null) {
			itemMeta.setDisplayName(name);
			setMeta = true;
		} else if (_customNames) {
			itemMeta.setDisplayName(GetName(stack, true));
			setMeta = true;
		}

		// Default Lore
		if (_lores != null && _lores.containsKey(id) && _lores.get(id).containsKey(data) && lore == null) {
			itemMeta.setLore(ArrayToList(_lores.get(id).get(data)));
			setMeta = true;
		}

		// Owner Lore
		if (owner != null) {
			final String[] tokens = owner.split(" ");

			final String[] ownerLore = new String[tokens.length + 2];

			ownerLore[0] = C.cGray + "Owner: " + C.cAqua + tokens[0];

			if (ownerLore.length >= 3) {
				ownerLore[1] = C.cGray + "Source: " + C.cAqua + tokens[1];
			}

			ownerLore[ownerLore.length - 2] = C.cGray + "Created: " + C.cAqua + UtilTime.date();

			ownerLore[ownerLore.length - 1] = "";

			if (itemMeta.getLore() != null) {
				itemMeta.setLore(CombineLore(itemMeta.getLore(), ArrayToList(ownerLore)));
			} else {
				itemMeta.setLore(ArrayToList(ownerLore));
			}

			setMeta = true;
		}

		// Set Lore
		if (lore != null) {
			if (itemMeta.getLore() != null) {
				itemMeta.setLore(CombineLore(itemMeta.getLore(), lore));
			} else {
				itemMeta.setLore(lore);
			}

			setMeta = true;
		}

		if (setMeta) {
			stack.setItemMeta(itemMeta);
		}

		// Unbreakable
		if (stack.getType().getMaxDurability() > 1) {
			final ItemMeta meta = stack.getItemMeta();
			meta.setUnbreakable(true);
			stack.setItemMeta(meta);
		}

		return stack;
	}

	public ItemStack CreateStack(int id, byte data, int amount, short damage, String name, String[] lore) {
		return CreateStack(id, data, amount, damage, name, ArrayToList(lore), null);
	}

	public ItemStack CreateStack(int id, byte data, int amount, short damage, String name, String[] lore,
			String owner) {
		return CreateStack(id, data, amount, damage, name, ArrayToList(lore), owner);
	}

	public ItemStack CreateStack(int id, byte data, int amount, String name) {
		return CreateStack(id, data, amount, (short) 0, name, new String[] {}, null);
	}

	public ItemStack CreateStack(int id, byte data, int amount, String name, List<String> lore) {
		return CreateStack(id, data, amount, (short) 0, name, lore, null);
	}

	public ItemStack CreateStack(int id, byte data, int amount, String name, List<String> lore, String owner) {
		return CreateStack(id, data, amount, (short) 0, name, lore, owner);
	}

	public ItemStack CreateStack(int id, byte data, int amount, String name, String[] lore) {
		return CreateStack(id, data, amount, (short) 0, name, ArrayToList(lore), null);
	}

	public ItemStack CreateStack(int id, byte data, int amount, String name, String[] lore, String owner) {
		return CreateStack(id, data, amount, (short) 0, name, ArrayToList(lore), owner);
	}

	public ItemStack CreateStack(int id, int amount) {
		return CreateStack(id, (byte) 0, amount, (short) 0, null, new String[] {}, null);
	}

	public ItemStack CreateStack(Material type) {
		return CreateStack(IdUtil.getTypeId(type), IdUtil.getData(type), 1, (short) 0, null, new String[] {}, null);
	}

	public ItemStack CreateStack(Material type, byte data) {
		return CreateStack(IdUtil.getTypeId(type), data, 1, (short) 0, null, new String[] {}, null);
	}

	public ItemStack CreateStack(Material type, byte data, int amount) {
		return CreateStack(IdUtil.getTypeId(type), data, amount, (short) 0, null, new String[] {}, null);
	}

	public ItemStack CreateStack(Material type, byte data, int amount, short damage, String name, List<String> lore) {
		return CreateStack(IdUtil.getTypeId(type), data, amount, damage, name, lore, null);
	}

	public ItemStack CreateStack(Material type, byte data, int amount, short damage, String name, List<String> lore,
			String owner) {
		return CreateStack(IdUtil.getTypeId(type), data, amount, damage, name, lore, owner);
	}

	public ItemStack CreateStack(Material type, byte data, int amount, short damage, String name, String[] lore) {
		return CreateStack(IdUtil.getTypeId(type), data, amount, damage, name, ArrayToList(lore), null);
	}

	public ItemStack CreateStack(Material type, byte data, int amount, short damage, String name, String[] lore,
			String owner) {
		return CreateStack(IdUtil.getTypeId(type), data, amount, damage, name, ArrayToList(lore), owner);
	}

	public ItemStack CreateStack(Material type, byte data, int amount, String name) {
		return CreateStack(IdUtil.getTypeId(type), data, amount, (short) 0, name, new String[] {}, null);
	}

	public ItemStack CreateStack(Material type, byte data, int amount, String name, List<String> lore) {
		return CreateStack(IdUtil.getTypeId(type), data, amount, (short) 0, name, lore, null);
	}

	public ItemStack CreateStack(Material type, byte data, int amount, String name, List<String> lore, String owner) {
		return CreateStack(IdUtil.getTypeId(type), data, amount, (short) 0, name, lore, owner);
	}

	public ItemStack CreateStack(Material type, byte data, int amount, String name, String[] lore) {
		return CreateStack(IdUtil.getTypeId(type), data, amount, (short) 0, name, ArrayToList(lore), null);
	}

	public ItemStack CreateStack(Material type, byte data, int amount, String name, String[] lore, String owner) {
		return CreateStack(IdUtil.getTypeId(type), data, amount, (short) 0, name, ArrayToList(lore), owner);
	}

	public ItemStack CreateStack(Material type, int amount) {
		return CreateStack(IdUtil.getTypeId(type), IdUtil.getData(type), amount, (short) 0, null, new String[] {}, null);
	}

	public String GetItemStackName(ItemStack stack) {
		if (stack == null || stack.getItemMeta() == null) return "Unknown";
		String displayName = stack.getItemMeta().getDisplayName();
		return displayName != null && !displayName.isEmpty() ? displayName : Clean(stack.getType().toString());
	}

	public String GetLoreVar(ItemStack stack, String var) {
		if (stack == null)
			return null;

		final ItemMeta meta = stack.getItemMeta();

		if (meta == null)
			return null;

		if (meta.getLore() == null)
			return null;

		for (final String cur : meta.getLore())
			if (cur.contains(var)) {
				final int index = var.split(" ").length;

				final String[] tokens = cur.split(" ");

				String out = "";
				for (int i = index; i < tokens.length; i++) {
					out += tokens[i] + " ";
				}

				if (out.length() > 0) {
					out = out.substring(0, out.length() - 1);
				}

				return out;
			}

		return null;
	}

	public int GetLoreVar(ItemStack stack, String var, int empty) {
		if (stack == null)
			return empty;

		final ItemMeta meta = stack.getItemMeta();

		if (meta == null)
			return 0;

		if (meta.getLore() == null)
			return 0;

		for (final String cur : meta.getLore())
			if (cur.contains(var)) {
				final String[] tokens = cur.split(" ");

				try {
					return Integer.parseInt(tokens[tokens.length - 1]);
				} catch (final Exception e) {
					return empty;
				}

			}

		return 0;
	}

	public String GetName(Block block, boolean formatted) {
		return GetName(IdUtil.getTypeId(block), IdUtil.getData(block), formatted);
	}

	public String GetName(int id, byte data, boolean formatted) {
		String out = "";
		if (formatted) {
			out = _nameFormat;
		}

		if (!_names.containsKey(id))
			return out + "Unknown";

		if (!_names.get(id).containsKey(data)) {
			if (_names.get(id).containsKey(0))
				return out + _names.get(id).get(0).getKey();

			for (final Entry<String, Boolean> cur : _names.get(id).values())
				return cur.getKey();

			return out + "Unknown";
		}

		return out + _names.get(id).get(data).getKey();
	}

	public String GetName(ItemStack stack, boolean formatted) {
		if (stack == null)
			return "Unarmed";

		return GetName(IdUtil.getTypeId(stack), IdUtil.getData(stack), formatted);
	}

	public String GetName(Material mat, byte data, boolean formatted) {
		return GetName(IdUtil.getTypeId(mat), data, formatted);
	}

	public boolean IsSpecial(int id, byte data) {
		if (!_names.containsKey(id))
			return false;

		if (!_names.get(id).containsKey(data))
			if (_names.get(id).containsKey(0))
				return _names.get(id).get(0).getValue();
			else
				return false;

		return _names.get(id).get(data).getValue();
	}

	public boolean IsSpecial(ItemStack stack) {
		if (stack == null)
			return false;

		return IsSpecial(IdUtil.getTypeId(stack), IdUtil.getData(stack));
	}

	public boolean IsSpecial(Material mat, byte data) {
		return IsSpecial(IdUtil.getTypeId(mat), data);
	}

	@EventHandler
	public void RenameArrow(EntityPickupItemEvent event) {
		if (!_customNames)
			return;

		if (event.isCancelled())
			return;

		if (!(event.getEntity() instanceof Player))
			return;
			
		Player player = (Player) event.getEntity();

		final ItemStack stack = event.getItem().getItemStack();

		if (stack.getType() != Material.ARROW)
			return;

		// Ignore Named Items
		final String color = ChatColor.getLastColors(GetItemStackName(stack));
		if (color != null && color.length() >= 2 && color.charAt(1) != 'f')
			return;

		// Data
		byte data = 0;
		data = IdUtil.getData(stack);

		// Remove
		event.setCancelled(true);
		event.getItem().remove();

		// Fletched
		if (data == 1)
			return;

		player.getInventory().addItem(CreateStack(IdUtil.getTypeId(stack), data, stack.getAmount()));
	}

	@EventHandler
	public void RenameCraft(PrepareItemCraftEvent event) {
		if (!_customNames)
			return;

		final ItemStack stack = event.getInventory().getResult();

		byte data = IdUtil.getData(stack);

		String crafter = null;
		if (event.getViewers().size() == 1 && stack.getMaxStackSize() == 1) {
			crafter = event.getViewers().get(0).getName() + " Crafting";
		}

		final ItemStack result = CreateStack(IdUtil.getTypeId(stack), data, stack.getAmount(), null, new String[] {}, crafter);

		event.getInventory().setResult(result);
	}

	@EventHandler
	public void RenameCraftAlg(InventoryClickEvent event) {
		if (!_customNames)
			return;

		if (!event.isShiftClick())
			return;

		if (event.getSlotType() != SlotType.RESULT)
			return;

		if (!(event.getInventory() instanceof CraftingInventory))
			return;

		final CraftingInventory inv = (CraftingInventory) event.getInventory();

		int make = 64;

		// Find Lowest Amount
		for (final ItemStack item : inv.getMatrix())
			if (item != null && item.getType() != Material.AIR)
				if (item.getAmount() < make) {
					make = item.getAmount();
				}

		make = make - 1;

		// Lower Amounts
		for (int i = 0; i < inv.getMatrix().length; i++)
			if (inv.getMatrix()[i] != null && inv.getMatrix()[i].getType() != Material.AIR) {
				if (inv.getMatrix()[i].getAmount() > make) {
					inv.getMatrix()[i].setAmount(inv.getMatrix()[i].getAmount() - make);
				} else {
					inv.getMatrix()[i].setAmount(1);
				}
			}

		// Get Result Data
		final int id = IdUtil.getTypeId(event.getCurrentItem());
		byte data = IdUtil.getData(event.getCurrentItem());
		final int amount = event.getCurrentItem().getAmount();

		// Crafter
		String crafter = null;
		if (event.getViewers().size() == 1 && event.getCurrentItem().getMaxStackSize() == 1) {
			crafter = event.getViewers().get(0).getName() + " Crafting";
		}

		// Give Result
		for (int i = 0; i < make; i++) {
			final ItemStack result = CreateStack(id, data, amount, null, new String[] {}, crafter);

			if (result != null) {
				event.getWhoClicked().getInventory().addItem(result);
			}
		}

		// Shedule Update
		if (event.getWhoClicked() instanceof Player) {
			final Player player = (Player) event.getWhoClicked();
			_plugin.getServer().getScheduler().scheduleSyncDelayedTask(_plugin, new Runnable() {
				@Override
				public void run() {
					UtilInv.Update(player);
				}
			}, 0);
		}
	}

	@EventHandler
	public void RenameSmelt(FurnaceSmeltEvent event) {
		if (!_customNames)
			return;

		final ItemStack stack = event.getResult();

		byte data = IdUtil.getData(stack);

		final ItemStack result = CreateStack(IdUtil.getTypeId(stack), data, stack.getAmount());

		event.setResult(result);
	}

	@EventHandler
	public void RenameSpawn(ItemSpawnEvent event) {
		if (!_customNames)
			return;

		if (event.isCancelled())
			return;

		// Ignore Named Items
		final String color = ChatColor.getLastColors(GetItemStackName(event.getEntity().getItemStack()));
		if (color != null && color.length() >= 2 && color.charAt(1) != 'f')
			return;

		final int id = IdUtil.getTypeId(event.getEntity().getItemStack());
		byte data = IdUtil.getData(event.getEntity().getItemStack());

		// NMS removed: item name set via NMS
		// ((org.bukkit.inventory.ItemStack) event.getEntity().getItemStack()).getHandle().c(GetName(id, data, true));
	}

	// XXX Owner Variant End

	private void SetCustom() {
		Add(0, (byte) 0, "Unarmed", false);

		Add(Material.DIAMOND_SWORD, (byte) 0, ChatColor.GOLD + "Diamond Sword",
				new String[] { C.cGray + "Damage: " + C.cYellow + "6", "" }, true);

		Add(Material.IRON_SWORD, (byte) 0, "Iron Sword", new String[] { C.cGray + "Damage: " + C.cYellow + "6", "" },
				true);

		Add(Material.GOLDEN_SWORD, (byte) 0, ChatColor.GOLD + "Power Sword",
				new String[] { C.cGray + "Damage: " + C.cYellow + "7", "" }, true);

		Add(Material.DIAMOND_AXE, (byte) 0, ChatColor.GOLD + "Diamond Axe",
				new String[] { C.cGray + "Damage: " + C.cYellow + "6", "" }, true);

		Add(Material.IRON_AXE, (byte) 0, "Iron Axe", new String[] { C.cGray + "Damage: " + C.cYellow + "6", "" }, true);

		Add(Material.GOLDEN_AXE, (byte) 0, ChatColor.GOLD + "Power Axe",
				new String[] { C.cGray + "Damage: " + C.cYellow + "7", "" }, true);

		Add(Material.MUSIC_DISC_5, (byte) 0, "50,000 Coin Token", true);

		Add(Material.IRON_HELMET, (byte) 0, "Knights Helm", true);
		Add(Material.IRON_CHESTPLATE, (byte) 0, "Knights Chestplate", true);
		Add(Material.IRON_LEGGINGS, (byte) 0, "Knights Leggings", true);
		Add(Material.IRON_BOOTS, (byte) 0, "Knights Boots", true);

		Add(Material.CHAINMAIL_HELMET, (byte) 0, "Rangers Cap", true);
		Add(Material.CHAINMAIL_CHESTPLATE, (byte) 0, "Rangers Vest", true);
		Add(Material.CHAINMAIL_LEGGINGS, (byte) 0, "Rangers Leggings", true);
		Add(Material.CHAINMAIL_BOOTS, (byte) 0, "Rangers Boots", true);

		Add(Material.LEATHER_HELMET, (byte) 0, "Assassins Cap", true);
		Add(Material.LEATHER_CHESTPLATE, (byte) 0, "Assassins Vest", true);
		Add(Material.LEATHER_LEGGINGS, (byte) 0, "Assassins Chaps", true);
		Add(Material.LEATHER_BOOTS, (byte) 0, "Assassins Boots", true);

		Add(Material.DIAMOND_HELMET, (byte) 0, "Brutes Helm", true);
		Add(Material.DIAMOND_CHESTPLATE, (byte) 0, "Brutes Chestplate", true);
		Add(Material.DIAMOND_LEGGINGS, (byte) 0, "Brutes Leggings", true);
		Add(Material.DIAMOND_BOOTS, (byte) 0, "Brutes Boots", true);

		Add(Material.GOLDEN_HELMET, (byte) 0, "Mages Helm", true);
		Add(Material.GOLDEN_CHESTPLATE, (byte) 0, "Mages Chestplate", true);
		Add(Material.GOLDEN_LEGGINGS, (byte) 0, "Mages Leggings", true);
		Add(Material.GOLDEN_BOOTS, (byte) 0, "Mages Boots", true);

		Add(Material.ENDER_CHEST, (byte) 0, "Class Unlock Shop", true);
		Add(Material.ENCHANTING_TABLE, (byte) 0, "Class Setup Table", true);
		Add(Material.BREWING_STAND, (byte) 0, "TNT Generator", true);
		Add(Material.BEACON, (byte) 0, "Clan Outpost", true);

		Add(Material.GOLD_NUGGET, (byte) 0, ChatColor.YELLOW + "Power Charge", true);

		// CONSUMABLE ITEMS
		Add(Material.MUSHROOM_STEW, (byte) 0, ChatColor.YELLOW + "Mushroom Soup",
				new String[] { C.cGray + "Right-Click: " + C.cYellow + "Consume",
						C.cGray + "  " + "Regeneration I for 4 Seconds", C.cGray + "  " + "4 Food", "" },
				true);

		// THROWABLES
		Add(Material.POTION, (byte) 0, ChatColor.YELLOW + "Water Bottle",
				new String[] { C.cGray + "Left-Click: " + C.cYellow + "Throw", C.cGray + "  " + "Douses Players",
						C.cGray + "  " + "Douses Fires", "", C.cGray + "Right-Click: " + C.cYellow + "Drink",
						C.cGray + "  " + "Douse Self", C.cGray + "  " + "Fire Resistance I for 4 Seconds" },
				true);

		Add(Material.SLIME_BALL, (byte) 0, ChatColor.YELLOW + "Poison Ball",
				new String[] { C.cGray + "Left-Click: " + C.cYellow + "Throw",
						C.cGray + "  " + "Poison I for 6 Seconds", C.cGray + "  " + "Returns to Thrower" },
				true);

		Add(Material.ENDER_PEARL, (byte) 0, ChatColor.YELLOW + "Ender Pearl",
				new String[] { C.cGray + "Left-Click: " + C.cYellow + "Throw", C.cGray + "  " + "Ride Ender Pearl", "",
						C.cGray + "Right-Click: " + C.cYellow + "Consume", C.cGray + "  " + "Removes Negative Effects",
						C.cGray + "  " + "4 Food" },
				true);

		Add(Material.NOTE_BLOCK, (byte) 0, ChatColor.YELLOW + "Proximity Incendiary",
				new String[] { C.cGray + "Left-Click: " + C.cYellow + "Throw",
						C.cGray + "  " + "Activates after 4 Seconds", C.cGray + "  " + "Detonates on player proximity;",
						C.cGray + "    " + "30 Fires spew out", C.cGray + "    " + "Fires ignite for 3 Seconds",
						C.cGray + "    " + "Fires remains for 15 Seconds" },
				true);

		Add(Material.REDSTONE_LAMP, (byte) 0, ChatColor.YELLOW + "Proximity Zapper",
				new String[] { C.cGray + "Left-Click: " + C.cYellow + "Throw",
						C.cGray + "  " + "Activates after 4 Seconds", C.cGray + "  " + "Detonates on player proximity;",
						C.cGray + "    " + "Lightning strikes the Zapper", C.cGray + "    " + "Silence for 6 seconds",
						C.cGray + "    " + "Shock for 6 seconds", C.cGray + "    " + "Slow IV for 6 seconds" },
				true);

		Add(Material.COMMAND_BLOCK, (byte) 0, ChatColor.YELLOW + "Proximity Explosive",
				new String[] { C.cGray + "Left-Click: " + C.cYellow + "Throw",
						C.cGray + "  " + "Activates after 4 Seconds", C.cGray + "  " + "Detonates on player proximity;",
						C.cGray + "    " + "8 Range", C.cGray + "    " + "Strong Knockback" },
				true);

		// TOOLS
		Add(Material.SHEARS, (byte) 0, ChatColor.YELLOW + "Scanner VR-9000",
				new String[] { C.cGray + "Right-Click: " + C.cYellow + "Scan Player",
						C.cGray + "  " + "100 Blocks Range", C.cGray + "  " + "Shows Targets Skills", "" },
				true);
	}

	public void SetCustomNameFormat(String format) {
		_nameFormat = format;
	}

	public void SetLoreVar(ItemStack stack, String var, String value) {
		if (stack == null)
			return;

		final ItemMeta meta = stack.getItemMeta();

		if (meta == null)
			return;

		final ArrayList<String> newLore = new ArrayList<>();

		boolean inserted = false;

		if (meta.getLore() != null) {
			for (final String lore : meta.getLore()) {
				if (!lore.contains(var)) {
					newLore.add(lore);
				} else {
					newLore.add(C.cGray + var + ":" + C.cGreen + " " + value);
					inserted = true;
				}
			}
		}

		if (!inserted) {
			newLore.add(C.cGray + var + ":" + C.cGreen + " " + value);
		}

		meta.setLore(newLore);

		stack.setItemMeta(meta);
	}

	public void SetUseCustomNames(boolean var) {
		_customNames = var;
	}

	public void StatsArmorRename(ItemStack item, int damage) {
		if (!_customNames)
			return;

		if (item == null)
			return;

		if (item.getMaxStackSize() > 1)
			return;

		damage += GetLoreVar(item, "Damage Tanked", 0);

		SetLoreVar(item, "Damage Tanked", "" + damage);

		if (damage >= 10000) {
			item.addEnchantment(Enchantment.UNBREAKING, 1);
		}
	}

	@EventHandler(priority = EventPriority.MONITOR)
	public void StatsBlockMined(BlockBreakEvent event) {
		if (!_customNames)
			return;

		if (event.isCancelled())
			return;

		final ItemStack item = event.getPlayer().getItemInHand();

		if (item == null)
			return;

		if (item.getMaxStackSize() > 1)
			return;

		final int blocks = 1 + GetLoreVar(item, "Blocks Mined", 0);
		SetLoreVar(item, "Blocks Mined", blocks + "");
	}

	@EventHandler(priority = EventPriority.MONITOR)
	public void StatsBowShoot(EntityShootBowEvent event) {
		if (!_customNames)
			return;

		if (event.isCancelled())
			return;

		final int shots = 1 + GetLoreVar(event.getBow(), "Arrows Shot", 0);

		SetLoreVar(event.getBow(), "Arrows Shot", "" + shots);

		final int hits = GetLoreVar(event.getBow(), "Arrows Hit", 0);

		final double acc = UtilMath.trim(1, (double) hits / (double) shots * 100);

		SetLoreVar(event.getBow(), "Accuracy", acc + "%");
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	public void StatsKillMob(EntityDeathEvent event) {
		if (!_customNames)
			return;

		if (!(event.getEntity() instanceof Monster))
			return;

		final Monster ent = (Monster) event.getEntity();

		if (ent.getKiller() == null)
			return;

		if (ent.getKiller().isBlocking())
			return;

		final ItemStack item = ent.getKiller().getItemInHand();

		if (item == null)
			return;

		if (item.getMaxStackSize() > 1)
			return;

		final int kills = 1 + GetLoreVar(item, "Monster Kills", 0);

		SetLoreVar(item, "Monster Kills", "" + kills);
	}
}
