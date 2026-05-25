package com.houzicore.arcade.nautilus.game.arcade.game.games.primalgames;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Random;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Chest;
import org.bukkit.block.DoubleChest;
import org.bukkit.block.Furnace;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.FurnaceInventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;

import com.houzicore.arcade.ArcadeFormat;
import com.houzicore.arcade.nautilus.game.arcade.events.GameStateChangeEvent;
import com.houzicore.arcade.nautilus.game.arcade.game.Game.GameState;
import com.houzicore.arcade.nautilus.game.arcade.game.games.primalgames.lang.PrimalGamesLang;
import com.houzicore.shared.common.util.C;
import com.houzicore.shared.common.util.F;
import com.houzicore.shared.common.util.UtilBlock;
import com.houzicore.shared.common.util.UtilMath;
import com.houzicore.shared.common.util.UtilServer;
import com.houzicore.shared.core.itemstack.ItemBuilder;
import com.houzicore.shared.core.itemstack.ItemStackFactory;
import com.houzicore.shared.core.loot.*;
import com.houzicore.shared.updater.UpdateType;
import com.houzicore.shared.updater.event.UpdateEvent;

/**
 * Manages all loot tables, chest filling, chest refill timers, and furnace loot
 * for Survival Primal Games.
 *
 * Extracted from PrimalGames.java to reduce monolith size.
 */
public class LootTableManager implements Listener
{
    private final PrimalGames _game;

    // Chest loot tables
    private final ChestLoot _baseLoot = new ChestLoot(true);
    private final ChestLoot _spawnLoot = new ChestLoot(true);
    private final ChestLoot _crateLoot = new ChestLoot(true);
    private final ChestLoot _deathMatchLoot = new ChestLoot(true);

    // Furnace loot
    private final ChestLoot _rawFurnace = new ChestLoot(true);
    private final ChestLoot _cookedFurnace = new ChestLoot(true);

    // Tracking
    private final HashSet<Location> _lootedBlocks = new HashSet<>();
    private final HashSet<Location> _openedChests = new HashSet<>();
    private final HashSet<Location> _landedCrates = new HashSet<>();
    private final ArrayList<Block> _supplyCrates = new ArrayList<>();

    // Chest refill
    private int _chestRefillTime = 60 * 7;

    public LootTableManager(PrimalGames game)
    {
        _game = game;
        setupLoot();
    }

    // ─────────────────────────────────────────────
    // Public API
    // ─────────────────────────────────────────────

    public ArrayList<Block> getSupplyCrates() { return _supplyCrates; }
    public HashSet<Location> getLootedBlocks() { return _lootedBlocks; }
    public HashSet<Location> getLandedCrates() { return _landedCrates; }
    public int getChestRefillTime() { return _chestRefillTime; }

    /**
     * Fill an airdrop crate chest with crate-tier loot.
     */
    public void fillAirdropCrate(Chest chest)
    {
        for (int i = 0; i < 6; i++)
        {
            ItemStack item = _crateLoot.getLoot();
            if (item != null) chest.getInventory().addItem(item);
        }
    }

    /**
     * Fill a chest with appropriate loot based on location and player.
     */
    public void fillChest(Player looter, Block block)
    {
        _lootedBlocks.add(block.getLocation());

        Chest chest = (Chest) block.getState();
        chest.getBlockInventory().clear();

        int items = 2;
        if (Math.random() > 0.50) items++;
        if (Math.random() > 0.65) items++;
        if (Math.random() > 0.80) items++;
        if (Math.random() > 0.95) items++;

        boolean spawnChest = _chestRefillTime > 0 && _game.GetSpectatorLocation() != null
                && UtilMath.offset(chest.getLocation(), _game.GetSpectatorLocation()) < 8;

        if (spawnChest) items += 3;

        if (_game.GetKit(looter) instanceof com.houzicore.arcade.nautilus.game.arcade.game.games.primalgames.kit.KitLooter)
        {
            items += UtilMath.r(3);
        }

        if (_supplyCrates.contains(block))
        {
            items = 2;
            if (Math.random() > 0.75) items++;
            if (Math.random() > 0.95) items++;
        }

        boolean isDeathMatch = _game.isDeathMatchTeleported();

        for (int i = 0; i < items; i++)
        {
            ItemStack item;

            if (spawnChest)
            {
                item = _spawnLoot.getLoot();
            }
            else if (isDeathMatch)
            {
                item = _deathMatchLoot.getLoot();
            }
            else
            {
                item = _supplyCrates.contains(block) ? _crateLoot.getLoot() : _baseLoot.getLoot();
            }

            if (item.getType() == Material.COMPASS)
            {
                item = buildCompass(5, looter);
            }

            chest.getBlockInventory().setItem(UtilMath.r(27), item);
        }

        if (_supplyCrates.contains(block))
        {
            org.bukkit.Bukkit.getPluginManager().callEvent(new SupplyChestOpenEvent(looter, block));
        }

        _supplyCrates.remove(block);
    }

    /**
     * Refill all previously opened chests.
     */
    public void refillChests()
    {
        ArrayList<Location> list = new ArrayList<>(_lootedBlocks);
        _lootedBlocks.clear();

        for (Location loc : list)
        {
            if (loc.getChunk().isLoaded())
            {
                Block block = loc.getBlock();

                if (block.getState() instanceof InventoryHolder)
                {
                    InventoryHolder holder = (InventoryHolder) block.getState();

                    if (!holder.getInventory().getViewers().isEmpty())
                    {
                        if (_landedCrates.contains(loc)) continue;

                        fillChest((Player) holder.getInventory().getViewers().get(0), block);
                    }
                }
            }
        }
    }

    // ─────────────────────────────────────────────
    // Event Handlers
    // ─────────────────────────────────────────────

    @EventHandler
    public void onOpenChest(PlayerInteractEvent event)
    {
        if (event.isCancelled()) return;

        Block block = event.getClickedBlock();
        if (block == null) return;

        if (!_game.IsLive()) return;

        if (_lootedBlocks.contains(block.getLocation())) return;

        org.bukkit.block.BlockState state = block.getState();

        if (state instanceof DoubleChest)
        {
            DoubleChest doubleChest = (DoubleChest) state;
            fillChest(event.getPlayer(), ((Chest) doubleChest.getLeftSide()).getBlock());
            fillChest(event.getPlayer(), ((Chest) doubleChest.getRightSide()).getBlock());
        }
        else if (state instanceof Chest)
        {
            fillChest(event.getPlayer(), block);
        }
        else if (state instanceof Furnace)
        {
            Furnace furnace = (Furnace) state;

            if (furnace.getCookTime() == 0)
            {
                FurnaceInventory inv = furnace.getInventory();

                if (UtilMath.r(3) == 0)
                {
                    int random = UtilMath.r(9);

                    if (random == 0)
                    {
                        inv.setFuel(new ItemStack(Material.STICK, new Random().nextInt(2) + 1));
                    }
                    else if (random <= 3)
                    {
                        inv.setSmelting(_rawFurnace.getLoot());
                    }
                    else
                    {
                        inv.setResult(_cookedFurnace.getLoot());
                    }
                }

                _lootedBlocks.add(block.getLocation());
            }
        }
    }

    @EventHandler
    public void onChestClose(InventoryCloseEvent event)
    {
        InventoryHolder holder = event.getInventory().getHolder();

        if (holder instanceof DoubleChest)
        {
            holder = (Chest) ((DoubleChest) holder).getLeftSide();
        }

        if (holder instanceof Chest)
        {
            Block block = ((Chest) holder).getBlock();
            _openedChests.add(block.getLocation());
        }
    }

    /**
     * Tick the chest refill timer every second.
     */
    public void refillSecond()
    {
        if (_game.getDeathMatchTime() <= 60) return;
        if (_chestRefillTime <= 0) return;

        _chestRefillTime--;

        PrimalGamesLang lang = PrimalGamesLang.get();

        switch (_chestRefillTime)
        {
            case 0:
                for (Player player : UtilServer.getPlayers())
                {
                    player.sendMessage("");
                    player.sendMessage(ArcadeFormat.Line);
                    player.sendMessage(lang.announceHeader() + lang.get(player, "primal_games.announce.chest_refilled"));
                    player.sendMessage(ArcadeFormat.Line);
                    player.sendMessage("");
                    player.playSound(player.getEyeLocation(), org.bukkit.Sound.ENTITY_IRON_GOLEM_DEATH, 1000, 0);
                }

                refillChests();
                _chestRefillTime--;
                break;
            case 300:
            case 180:
            case 120:
            case 60:
            case 30:
            case 15:
            case 10:
            case 5:
            case 4:
            case 3:
            case 2:
            case 1:
                String time;
                if (_chestRefillTime >= 60)
                {
                    time = (_chestRefillTime / 60) + " minute" + (_chestRefillTime > 60 ? "s" : "");
                }
                else
                {
                    time = _chestRefillTime + " second" + (_chestRefillTime != 1 ? "s" : "");
                }

                for (Player player : UtilServer.getPlayers())
                {
                    player.sendMessage("");
                    player.sendMessage(ArcadeFormat.Line);
                    player.sendMessage(lang.announceHeader() + lang.get(player, "primal_games.announce.chest_refill_timer", "time", time));
                    player.sendMessage(ArcadeFormat.Line);
                    player.sendMessage("");
                }
                break;
            default:
                break;
        }
    }

    // ─────────────────────────────────────────────
    // Internal
    // ─────────────────────────────────────────────

    public ItemStack buildCompass(int uses, Player viewer)
    {
        PrimalGamesLang lang = PrimalGamesLang.get();
        ItemBuilder item = new ItemBuilder(Material.COMPASS);
        item.setTitle(lang.get(viewer, "primal_games.item.compass_name") + buildTime());
        item.addLore(lang.get(viewer, "primal_games.item.compass_uses", "uses", String.valueOf(uses)));
        item.addLore(lang.get(viewer, "primal_games.item.compass_lore1"));
        item.addLore(lang.get(viewer, "primal_games.item.compass_lore2"));
        return item.build();
    }

    private String buildTime()
    {
        String s = "";
        for (char c : ("" + System.nanoTime()).toCharArray())
        {
            s += "§" + c;
        }
        return s;
    }

    private void setupLoot()
    {
        // ====== BASE LOOT (Basic Ingredients, Raw Food, Wooden Tools) ======
        _baseLoot.addLoot(new RandomItem(Material.APPLE, 30, 1, 3));
        _baseLoot.addLoot(new RandomItem(Material.CARROT, 30, 1, 3));
        _baseLoot.addLoot(new RandomItem(Material.POTATO, 30, 1, 3));
        _baseLoot.addLoot(new RandomItem(Material.ROTTEN_FLESH, 40, 1, 5));
        _baseLoot.addLoot(new RandomItem(Material.MUSHROOM_STEW, 15, 1, 1));
        _baseLoot.addLoot(new RandomItem(Material.PORKCHOP, 30, 1, 3));

        _baseLoot.addLoot(new RandomItem(Material.STICK, 40, 2, 4));
        _baseLoot.addLoot(new RandomItem(Material.FLINT, 35, 2, 3));
        _baseLoot.addLoot(new RandomItem(Material.FEATHER, 30, 1, 2));
        _baseLoot.addLoot(new RandomItem(Material.STRING, 30, 1, 2));
        _baseLoot.addLoot(new RandomItem(Material.COAL, 35, 1, 3));
        _baseLoot.addLoot(new RandomItem(Material.IRON_NUGGET, 40, 1, 3));
        _baseLoot.addLoot(new RandomItem(Material.LEATHER, 25, 1, 2));

        _baseLoot.addLoot(new RandomItem(Material.WOODEN_AXE, 50));
        _baseLoot.addLoot(new RandomItem(Material.WOODEN_PICKAXE, 50));
        _baseLoot.addLoot(new RandomItem(Material.WOODEN_SWORD, 40));

        _baseLoot.addLoot(new RandomItem(Material.COMPASS, 10));
        _baseLoot.addLoot(new RandomItem(Material.FLINT_AND_STEEL, 10));
        _baseLoot.addLoot(new RandomItem(ItemStackFactory.Instance.CreateStack(Material.TNT, (byte)0, 1, F.item("Throwing TNT")), 15));

        // ====== SPAWN/MID LOOT ======
        _spawnLoot.cloneLoot(_baseLoot);
        _spawnLoot.addLoot(new RandomItem(Material.COOKED_BEEF, 25, 1, 2));
        _spawnLoot.addLoot(new RandomItem(Material.COOKED_CHICKEN, 25, 1, 2));
        _spawnLoot.addLoot(new RandomItem(Material.CAKE, 15));
        _spawnLoot.addLoot(new RandomItem(Material.RAW_IRON, 30, 1, 2));
        _spawnLoot.addLoot(new RandomItem(Material.COAL, 30, 2, 4));
        _spawnLoot.addLoot(new RandomItem(Material.WHITE_WOOL, 25, 1, 3));
        _spawnLoot.addLoot(new RandomItem(Material.STONE_SWORD, 30));
        _spawnLoot.addLoot(new RandomItem(Material.STONE_AXE, 40));
        _spawnLoot.addLoot(new RandomItem(Material.STONE_PICKAXE, 20));
        _spawnLoot.addLoot(new RandomItem(Material.BOW, 15));
        _spawnLoot.addLoot(new RandomItem(Material.ARROW, 20, 3, 6));
        _spawnLoot.addLoot(new RandomItem(Material.LEATHER_HELMET, 20));
        _spawnLoot.addLoot(new RandomItem(Material.LEATHER_BOOTS, 20));
        _spawnLoot.addLoot(new RandomItem(Material.PAPER, 10, 1, 2));

        // ====== SUPPLY CRATE LOOT ======
        _crateLoot.addLoot(new RandomItem(Material.IRON_HELMET, 30));
        _crateLoot.addLoot(new RandomItem(Material.IRON_CHESTPLATE, 25));
        _crateLoot.addLoot(new RandomItem(Material.IRON_LEGGINGS, 25));
        _crateLoot.addLoot(new RandomItem(Material.IRON_BOOTS, 30));
        _crateLoot.addLoot(new RandomItem(Material.DIAMOND_CHESTPLATE, 5));
        _crateLoot.addLoot(new RandomItem(Material.IRON_SWORD, 30));
        _crateLoot.addLoot(new RandomItem(Material.DIAMOND_SWORD, 10));
        _crateLoot.addLoot(new RandomItem(Material.BOW, 30));
        _crateLoot.addLoot(new RandomItem(Material.ARROW, 30, 5, 12));
        _crateLoot.addLoot(new RandomItem(Material.GOLDEN_APPLE, 25, 1, 2));

        // ====== FURNACES ======
        _cookedFurnace.addLoot(new RandomItem(Material.COOKED_BEEF, 3, 1, 2));
        _cookedFurnace.addLoot(new RandomItem(Material.COOKED_CHICKEN, 3, 1, 2));
        _cookedFurnace.addLoot(new RandomItem(Material.IRON_INGOT, 2, 1, 2));
        _rawFurnace.addLoot(new RandomItem(Material.RAW_IRON, 1, 1, 2));
        _rawFurnace.addLoot(new RandomItem(Material.RAW_GOLD, 1, 1, 2));
        _rawFurnace.addLoot(new RandomItem(Material.PORKCHOP, 1, 1, 3));

        // ====== DEATHMATCH LOOT ======
        _deathMatchLoot.addLoot(new RandomItem(Material.PUMPKIN_PIE, 4));
        _deathMatchLoot.addLoot(new RandomItem(Material.BAKED_POTATO, 4));
        _deathMatchLoot.addLoot(new RandomItem(Material.GOLDEN_APPLE, 2));
        _deathMatchLoot.addLoot(new RandomItem(Material.IRON_SWORD, 1));
    }
}
