package com.houzicore.shared.core.displayentity.command;

import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import com.houzicore.shared.MiniPlugin;
import com.houzicore.shared.common.Rank;
import com.houzicore.shared.common.util.F;
import com.houzicore.shared.common.util.UtilPlayer;
import com.houzicore.shared.core.command.CommandBase;
import com.houzicore.shared.core.displayentity.DisplayEntityManager;
import com.houzicore.shared.core.displayentity.DisplayModel;
import com.houzicore.shared.core.displayentity.function.BdeFunctionPack;

public class FurnitureBrowserCommand extends CommandBase<MiniPlugin> implements Listener {

    private static final int PAGE_SIZE = 45;

    private final DisplayEntityManager _manager;

    public FurnitureBrowserCommand(DisplayEntityManager manager) {
        super(null, Rank.ADMIN, "furniture", "furnituregui", "modelgui", "models");
        _manager = manager;
        manager.getPlugin().getServer().getPluginManager().registerEvents(this, manager.getPlugin());
    }

    @Override
    public void Execute(Player caller, String[] args) {
        if (args.length > 0 && args[0].equalsIgnoreCase("reload")) {
            _manager.reloadRegistry();
            UtilPlayer.message(caller, F.main("Furniture", "Reloaded display models. Loaded: " + _manager.getRegistry().getModels().size()));
        }

        new BrowserPage(caller, 0).open();
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getInventory().getHolder() instanceof BrowserPage page)) {
            return;
        }

        event.setCancelled(true);
        page.handleClick(event);
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onInventoryDrag(InventoryDragEvent event) {
        if (event.getInventory().getHolder() instanceof BrowserPage) {
            event.setCancelled(true);
        }
    }

    @Override
    public List<String> onTabComplete(org.bukkit.command.CommandSender sender, String commandLabel, String[] args) {
        if (args.length == 1 && "reload".startsWith(args[0].toLowerCase())) {
            return List.of("reload");
        }
        return List.of();
    }

    private final class BrowserPage implements InventoryHolder {
        private final Player _player;
        private final int _page;
        private final Inventory _inventory;
        private final Map<Integer, Entry> _slotEntries = new HashMap<>();

        private BrowserPage(Player player, int page) {
            _player = player;
            _page = Math.max(0, page);
            _inventory = Bukkit.createInventory(this, 54, ChatColor.DARK_AQUA + "Display Models");
            build();
        }

        private void open() {
            _player.openInventory(_inventory);
        }

        private void build() {
            _slotEntries.clear();

            List<Entry> entries = new java.util.ArrayList<>();
            entries.addAll(_manager.getRegistry().getModels().stream()
                    .sorted(Comparator.comparing(DisplayModel::getId))
                    .map(Entry::model)
                    .collect(Collectors.toList()));
            entries.addAll(_manager.getFunctionRuntime().getPacks().stream()
                    .sorted(Comparator.comparing(BdeFunctionPack::getNamespace))
                    .map(Entry::function)
                    .collect(Collectors.toList()));

            int start = _page * PAGE_SIZE;
            for (int slot = 0; slot < PAGE_SIZE && start + slot < entries.size(); slot++) {
                Entry entry = entries.get(start + slot);
                _slotEntries.put(slot, entry);
                _inventory.setItem(slot, makeIcon(entry));
            }

            if (_page > 0) {
                _inventory.setItem(45, named(Material.ARROW, ChatColor.YELLOW + "Previous Page"));
            }
            _inventory.setItem(49, named(Material.COMPASS,
                    ChatColor.AQUA + "Display Models",
                    ChatColor.GRAY + "Loaded: " + ChatColor.YELLOW + entries.size(),
                    ChatColor.GRAY + "Page: " + ChatColor.YELLOW + (_page + 1),
                    "",
                    ChatColor.GREEN + "Click a model to receive a placeable item.",
                    ChatColor.GRAY + "Use /furniture reload after adding files."));

            if (start + PAGE_SIZE < entries.size()) {
                _inventory.setItem(53, named(Material.ARROW, ChatColor.YELLOW + "Next Page"));
            }
        }

        private void handleClick(InventoryClickEvent event) {
            if (!(event.getWhoClicked() instanceof Player player)) {
                return;
            }

            int slot = event.getSlot();
            if (slot == 45 && _page > 0) {
                new BrowserPage(player, _page - 1).open();
                return;
            }
            if (slot == 49) {
                _manager.reloadRegistry();
                new BrowserPage(player, _page).open();
                return;
            }
            if (slot == 53) {
                new BrowserPage(player, _page + 1).open();
                return;
            }

            Entry entry = _slotEntries.get(slot);
            if (entry == null) {
                return;
            }

            ItemStack item = entry.isFunction()
                    ? _manager.getFurnitureManager().createFunctionFurnitureItem(entry.functionPack.getNamespace())
                    : _manager.getFurnitureManager().createFurnitureItem(entry.model.getId());
            if (item == null) {
                UtilPlayer.message(player, F.main("Furniture", ChatColor.RED + "Could not create item for: " + entry.id()));
                return;
            }

            player.getInventory().addItem(item);
            player.playSound(player.getLocation(), Sound.ENTITY_ITEM_PICKUP, 0.8f, 1.3f);
            UtilPlayer.message(player, F.main("Furniture", "Given placeable model item: " + entry.id()));
        }

        private ItemStack makeIcon(Entry entry) {
            ItemStack item = entry.isFunction()
                    ? _manager.getFurnitureManager().createFunctionFurnitureItem(entry.functionPack.getNamespace())
                    : _manager.getFurnitureManager().createFurnitureItem(entry.model.getId());
            if (item == null) {
                item = named(Material.ARMOR_STAND, ChatColor.RED + entry.id());
            }

            ItemMeta meta = item.getItemMeta();
            if (meta != null) {
                if (entry.isFunction()) {
                    meta.setDisplayName(ChatColor.AQUA + entry.functionPack.getNamespace() + ChatColor.GOLD + " [Animated]");
                    meta.setLore(List.of(
                            ChatColor.GRAY + "Functions: " + ChatColor.YELLOW + entry.functionPack.getFunctionIds().size(),
                            ChatColor.GRAY + "Animation: " + ChatColor.GREEN + "BDE Function Loop",
                            ChatColor.GRAY + "Hitbox: " + ChatColor.GREEN + "Auto on place",
                            "",
                            ChatColor.GREEN + "Click to get placeable item."
                    ));
                } else {
                    meta.setDisplayName(ChatColor.AQUA + entry.model.getId());
                    meta.setLore(List.of(
                            ChatColor.GRAY + "Parts: " + ChatColor.YELLOW + entry.model.getPartCount(),
                            ChatColor.GRAY + "Hitbox: " + ChatColor.GREEN + "Auto on place",
                            "",
                            ChatColor.GREEN + "Click to get placeable item."
                    ));
                }
                item.setItemMeta(meta);
            }
            return item;
        }

        @Override
        public Inventory getInventory() {
            return _inventory;
        }
    }

    private static final class Entry {
        private final DisplayModel model;
        private final BdeFunctionPack functionPack;

        private Entry(DisplayModel model, BdeFunctionPack functionPack) {
            this.model = model;
            this.functionPack = functionPack;
        }

        private static Entry model(DisplayModel model) {
            return new Entry(model, null);
        }

        private static Entry function(BdeFunctionPack pack) {
            return new Entry(null, pack);
        }

        private boolean isFunction() {
            return functionPack != null;
        }

        private String id() {
            return isFunction() ? functionPack.getNamespace() : model.getId();
        }
    }

    private static ItemStack named(Material material, String name, String... lore) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(name);
            if (lore.length > 0) {
                meta.setLore(List.of(lore));
            }
            item.setItemMeta(meta);
        }
        return item;
    }
}
