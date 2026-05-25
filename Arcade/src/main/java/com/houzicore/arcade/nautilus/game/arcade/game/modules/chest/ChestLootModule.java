package com.houzicore.arcade.nautilus.game.arcade.game.modules.chest;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Chest;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import com.houzicore.shared.common.util.MapUtil;
import com.houzicore.shared.common.util.UtilAlg;
import com.houzicore.shared.common.util.UtilBlock;
import com.houzicore.shared.common.util.UtilEvent;
import com.houzicore.shared.common.util.UtilEvent.ActionType;
import com.houzicore.shared.common.util.UtilServer;
import com.houzicore.shared.common.util.UtilTime;
import com.houzicore.shared.updater.UpdateType;
import com.houzicore.shared.updater.event.UpdateEvent;

import com.houzicore.arcade.nautilus.game.arcade.events.ChestRefillEvent;
import com.houzicore.arcade.nautilus.game.arcade.events.GameStateChangeEvent;
import com.houzicore.arcade.nautilus.game.arcade.game.Game;
import com.houzicore.arcade.nautilus.game.arcade.game.Game.GameState;
import com.houzicore.arcade.nautilus.game.arcade.game.modules.GameModule;

public class ChestLootModule extends GameModule<Game>
{

	private final Map<ChestType, Set<ChestMetadata>> _chests;

	private long _destroyAfterOpened;
	private boolean _autoRotateChests = true, _spawnNearby, _preGenerateLoot;
	private int _spawnNearbyRadius = 8;

	public ChestLootModule()
	{
		super(null); // Compatibility with legacy instantiation: new ChestLootModule()
		_chests = new HashMap<>();
	}

	public ChestLootModule(Game game)
	{
		super(game);
		_chests = new HashMap<>();
	}

	public void register(Game game)
	{
		// Since parent GameModule uses final T _game, and we want compatibility with legacy instantiation:
		try
		{
			java.lang.reflect.Field field = GameModule.class.getDeclaredField("_game");
			field.setAccessible(true);
			field.set(this, game);
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		register();
	}

	public ChestLootModule registerChestType(String name, List<Location> chestLocations, ChestLootPool... pools)
	{
		return registerChestType(name, chestLocations, 1, pools);
	}

	public ChestLootModule registerChestType(String name, List<Location> chestLocations, double spawnChance, ChestLootPool... pools)
	{
		_chests.put(new ChestType(name, chestLocations, spawnChance, pools), new HashSet<>());
		return this;
	}

	public ChestLootModule destroyAfterOpened(int seconds)
	{
		_destroyAfterOpened = TimeUnit.SECONDS.toMillis(seconds);
		return this;
	}

	public ChestLootModule autoRotateChests(boolean autoRotate)
	{
		_autoRotateChests = autoRotate;
		return this;
	}

	public ChestLootModule spawnNearbyDataPoints()
	{
		_spawnNearby = true;
		return this;
	}

	public ChestLootModule spawnNearbyDataPoints(int radius)
	{
		_spawnNearby = true;
		_spawnNearbyRadius = radius;
		return this;
	}

	public ChestLootModule setPreGenerateLoot(boolean preGenerateLoot)
	{
		_preGenerateLoot = preGenerateLoot;
		return this;
	}

	public void addChestLocation(String typeName, Location location)
	{
		for (Entry<ChestType, Set<ChestMetadata>> entry : _chests.entrySet())
		{
			if (!entry.getKey().Name.equals(typeName))
			{
				continue;
			}

			entry.getValue().add(new ChestMetadata(location.getBlock(), entry.getKey()));
			return;
		}
	}

	public void refill()
	{
		_chests.forEach((type, metadataSet) -> metadataSet.forEach(metadata -> metadata.Opened = false));

		List<Location> chests = new ArrayList<>();
		_chests.values().forEach(set -> set.forEach(chestMetadata -> chests.add(chestMetadata.Chest.getLocation())));

		org.bukkit.Bukkit.getPluginManager().callEvent(new ChestRefillEvent(chests));
	}

	public void refill(String typeName)
	{
		_chests.forEach((type, metadataSet) ->
		{
			if (!type.Name.equals(typeName))
			{
				return;
			}

			metadataSet.forEach(metadata -> metadata.Opened = false);
			org.bukkit.Bukkit.getPluginManager().callEvent(new ChestRefillEvent(metadataSet.stream()
					.map(chestMetadata -> chestMetadata.Chest.getLocation())
					.collect(Collectors.toList())));
		});
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	public void populateChests(GameStateChangeEvent event)
	{
		if (event.GetState() != GameState.Prepare)
		{
			return;
		}

		for (Entry<ChestType, Set<ChestMetadata>> entry : _chests.entrySet())
		{
			ChestType chestType = entry.getKey();

			if (chestType.ChestSpawns == null)
			{
				continue;
			}

			Set<ChestMetadata> metadataSet = entry.getValue();

			for (Location location : chestType.ChestSpawns)
			{
				if (chestType.SpawnChance == 1 || Math.random() < chestType.SpawnChance)
				{
					Block block = location.getBlock();

					if (_spawnNearby)
					{
						Location nearby = getNearbyLocation(location);

						if (nearby == null)
						{
							continue;
						}

						block = nearby.getBlock();
					}

					block.setType(Material.CHEST);

					ChestMetadata metadata = new ChestMetadata(block, chestType);
					metadataSet.add(metadata);

					if (_preGenerateLoot)
					{
						metadata.populateChest((Chest) block.getState());
					}
				}
				else
				{
					MapUtil.QuickChangeBlockAt(location, Material.AIR);
				}
			}

			_chests.put(chestType, metadataSet);
		}
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	public void openChest(PlayerInteractEvent event)
	{
		Block block = event.getClickedBlock();

		if (event.isCancelled() || !UtilEvent.isAction(event, ActionType.R_BLOCK) || block == null || !(block.getState() instanceof Chest))
		{
			return;
		}

		ChestMetadata metadata = getFromBlock(block);

		if (metadata == null || metadata.Opened)
		{
			return;
		}

		Chest chest = (Chest) block.getState();
		Player player = event.getPlayer();
		getGame().AddStat(player, "ChestsOpened", 1, false, false);

		metadata.Opened = true;
		metadata.OpenedAt = System.currentTimeMillis();

		if (!_preGenerateLoot)
		{
			metadata.populateChest(chest);
		}
	}

	@EventHandler
	public void destroyOpenedChests(UpdateEvent event)
	{
		if (event.getType() != UpdateType.SEC || _destroyAfterOpened == 0)
		{
			return;
		}

		for (Set<ChestMetadata> metadataSet : _chests.values())
		{
			metadataSet.removeIf(metadata ->
			{
				if (metadata.Opened && UtilTime.elapsed(metadata.OpenedAt, _destroyAfterOpened))
				{
					Block block = metadata.Chest;
					Location location = block.getLocation();
					location.getWorld().playEffect(location.add(0.5, 0.5, 0.5), Effect.STEP_SOUND, block.getType());
					if (block.getType() == Material.CHEST)
					{
						((Chest) block.getState()).getBlockInventory().clear();
					}
					MapUtil.QuickChangeBlockAt(location, Material.AIR);
					return true;
				}

				return false;
			});
		}
	}

	public ItemStack getRandomItem(String chestTypeName)
	{
		for (ChestType chestType1 : _chests.keySet())
		{
			if (!chestType1.Name.equals(chestTypeName))
			{
				continue;
			}

			ChestLootPool pool = UtilAlg.Random(chestType1.Pools);

			if (pool == null)
			{
				return null;
			}

			return pool.getRandomItem();
		}

		return null;
	}

	private ChestMetadata getFromBlock(Block block)
	{
		for (Set<ChestMetadata> metadataSet : _chests.values())
		{
			for (ChestMetadata metadata : metadataSet)
			{
				if (metadata.Chest.getLocation().getBlock().equals(block))
				{
					return metadata;
				}
			}
		}

		return null;
	}

	private Location getNearbyLocation(Location center)
	{
		int attempts = 0;
		while (attempts++ < 20)
		{
			Location newLocation = center.clone().add(
					com.houzicore.shared.common.util.UtilMath.r(2 * _spawnNearbyRadius + 1) - _spawnNearbyRadius,
					com.houzicore.shared.common.util.UtilMath.r(3) - 1,
					com.houzicore.shared.common.util.UtilMath.r(2 * _spawnNearbyRadius + 1) - _spawnNearbyRadius
			);

			if (isSuitable(newLocation.getBlock()))
			{
				return newLocation;
			}
		}

		return null;
	}

	private boolean isSuitable(Block block)
	{
		Block up = block.getRelative(BlockFace.UP);
		Block down = block.getRelative(BlockFace.DOWN);

		return block.getType() == Material.AIR && up.getType() == Material.AIR && down.getType() != Material.AIR && !down.isLiquid() && !up.isLiquid() && !block.isLiquid();
	}

	private class ChestMetadata
	{

		Block Chest;
		ChestType Type;
		long OpenedAt;
		boolean Opened;

		ChestMetadata(Block chest, ChestType type)
		{
			Chest = chest;
			Type = type;
		}

		void populateChest(Chest chest)
		{
			Inventory inventory = chest.getBlockInventory();
			inventory.clear();
			List<Integer> slots = new ArrayList<>(chest.getBlockInventory().getSize());

			for (int i = 0; i < inventory.getSize(); i++)
			{
				slots.add(i);
			}

			for (ChestLootPool pool : Type.Pools)
			{
				if (pool.getProbability() == 1 || Math.random() < pool.getProbability())
				{
					pool.populateChest(chest, slots);
				}
			}
		}
	}

	private class ChestType
	{
		String Name;
		double SpawnChance;
		List<ChestLootPool> Pools;
		List<Location> ChestSpawns;

		ChestType(String name, List<Location> chestLocations, double spawnChance, ChestLootPool... pools)
		{
			Name = name;
			SpawnChance = spawnChance;
			Pools = Arrays.asList(pools);
			ChestSpawns = chestLocations;
		}
	}
}
