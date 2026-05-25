package com.houzicore.shared.core.treasure;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import com.houzicore.shared.MiniPlugin;
import com.houzicore.shared.account.CoreClientManager;
import com.houzicore.shared.core.blockrestore.BlockRestore;
import com.houzicore.shared.core.donation.DonationManager;
import com.houzicore.shared.core.hologram.HologramManager;
import com.houzicore.shared.core.inventory.InventoryManager;
import com.houzicore.shared.core.pet.PetManager;
import com.houzicore.shared.core.reward.Reward;
import com.houzicore.shared.core.reward.RewardManager;
import com.houzicore.shared.core.reward.RewardType;
import com.houzicore.shared.core.displayentity.DisplayEntityManager;

/**
 * Created by Shaun on 8/27/2014.
 */
public class TreasureManager extends MiniPlugin {
	private final RewardManager _rewardManager;
	private final InventoryManager _inventoryManager;
	private final TreasureInventoryService _treasureInventoryService;
	private final CoreClientManager _clientManager;
	private final DonationManager _donationManager;
	private final BlockRestore _blockRestore;
	private final HologramManager _hologramManager;
	private final List<TreasureLocation> _treasureLocations;
	private final com.houzicore.shared.core.preferences.PreferencesManager _preferencesManager;
	private final DisplayEntityManager _displayEntityManager;

	public TreasureManager(JavaPlugin plugin, CoreClientManager clientManager, DonationManager donationManager,
			InventoryManager inventoryManager, PetManager petManager, BlockRestore blockRestore,
			HologramManager hologramManager, com.houzicore.shared.core.preferences.PreferencesManager preferencesManager, DisplayEntityManager displayEntityManager) {
		super("Treasure", plugin);
		_preferencesManager = preferencesManager;
		_clientManager = clientManager;
		_donationManager = donationManager;

		_inventoryManager = inventoryManager;
		_treasureInventoryService = new TreasureInventoryService(inventoryManager);
		_blockRestore = blockRestore;
		_hologramManager = hologramManager;
		_displayEntityManager = displayEntityManager;
		_rewardManager = new RewardManager(clientManager, donationManager, inventoryManager, petManager, 100, 250, 500,
				1000, 4000, 6000, 12000, 32000, true);

		_treasureLocations = new ArrayList<>();
	}

	/**
	 * Registers a new TreasureLocation from a WorldConfig.dat coordinate.
	 * The chestBlock is the block at the given location.
	 * ChestSpawns are auto-generated at 4 cardinal directions around the center.
	 * ResetLocation is 3 blocks away from center (player teleports here after opening).
	 */
	public void addLocation(Location location) {
		World world = location.getWorld();
		Block chestBlock = world.getBlockAt(location);

		// Prevent duplicate treasure locations from identical coordinates in WorldConfig.dat
		for (TreasureLocation existing : _treasureLocations) {
			if (existing.getHologram() != null && existing.getHologram().getLocation().getWorld().equals(world)) {
				if (existing.getHologram().getLocation().distanceSquared(location) < 16) {
					System.out.println("[TreasureManager] Skipped duplicate TreasureLocation at " + location.getX() + "," + location.getY() + "," + location.getZ());
					return;
				}
			}
		}

		// Scan for existing physical chest blocks within radius 6 that the MapBuilder may have placed
		java.util.List<Block> foundChests = new java.util.ArrayList<>();
		for (int x = -6; x <= 6; x++) {
			for (int y = -2; y <= 2; y++) {
				for (int z = -6; z <= 6; z++) {
					if (x == 0 && y == 0 && z == 0) continue; // Skip center block itself
					Block b = world.getBlockAt(location.getBlockX() + x, location.getBlockY() + y, location.getBlockZ() + z);
					if (b.getType() == org.bukkit.Material.CHEST || b.getType() == org.bukkit.Material.ENDER_CHEST || 
						b.getType() == org.bukkit.Material.TRAPPED_CHEST || b.getType() == org.bukkit.Material.WAXED_OXIDIZED_COPPER) {
						foundChests.add(b);
					}
				}
			}
		}

		Block[] chestSpawns;
		if (foundChests.size() > 0) {
			chestSpawns = foundChests.toArray(new Block[0]);
		} else {
			// Fallback: Generate 8 chest spawn positions around center (N, E, S, W at distance 3, plus NE, SE, SW, NW at distance 2,2)
			chestSpawns = new Block[] {
				world.getBlockAt(location.clone().add(3, 0, 0)),
				world.getBlockAt(location.clone().add(-3, 0, 0)),
				world.getBlockAt(location.clone().add(0, 0, 3)),
				world.getBlockAt(location.clone().add(0, 0, -3)),
				world.getBlockAt(location.clone().add(2, 0, 2)),
				world.getBlockAt(location.clone().add(2, 0, -2)),
				world.getBlockAt(location.clone().add(-2, 0, 2)),
				world.getBlockAt(location.clone().add(-2, 0, -2))
			};
		}

		// Reset location: 5 blocks in the +X direction from center
		Location resetLocation = location.clone().add(5, 0, 0);

		TreasureLocation treasureLocation = new TreasureLocation(
			this, _treasureInventoryService, _clientManager, _donationManager,
			chestBlock, chestSpawns, resetLocation, _hologramManager, _displayEntityManager
		);

		_treasureLocations.add(treasureLocation);
		_plugin.getServer().getPluginManager().registerEvents(treasureLocation, _plugin);

		System.out.println("[TreasureManager] Registered treasure at " + location.getBlockX() + "," + location.getBlockY() + "," + location.getBlockZ());
	}

	private int _hologramTick = 0;
	private static final String[] HOLOGRAM_GRADIENTS = {
		"#005500,#55FF55,#005500",
		"#007700,#55FF55,#007700",
		"#009900,#55FF55,#009900",
		"#00BB00,#55FF55,#00BB00",
		"#00DD00,#55FF55,#00DD00",
		"#00FF00,#55FF55,#00FF00",
		"#00DD00,#55FF55,#00DD00",
		"#00BB00,#55FF55,#00BB00",
		"#009900,#55FF55,#009900",
		"#007700,#55FF55,#007700"
	};

	@org.bukkit.event.EventHandler
	public void onUpdateHologram(com.houzicore.shared.updater.event.UpdateEvent event) {
		if (event.getType() != com.houzicore.shared.updater.UpdateType.FASTER) return;
		
		String label = TreasureLang.getEnglish("ui.hologram.open", "OPEN TREASURE");
		String frame = "<GRADIENT:" + HOLOGRAM_GRADIENTS[_hologramTick % HOLOGRAM_GRADIENTS.length] + "> " + label + " </GRADIENT>";
		String parsedFrame = com.houzicore.shared.common.util.HouziColorParser.parse(frame);
		_hologramTick++;
		
		for (TreasureLocation loc : _treasureLocations) {
			if (loc.getHologram() != null && loc.getHologram().isInUse()) {
				loc.getHologram().setText(parsedFrame);
			}
		}
	}

	@Override
	public void disable() {
		for (final TreasureLocation treasureLocation : _treasureLocations) {
			treasureLocation.cleanup();
		}
	}

	public BlockRestore getBlockRestore() {
		return _blockRestore;
	}

	public TreasureInventoryService getTreasureInventoryService() {
		return _treasureInventoryService;
	}

	public Reward[] getRewards(Player player, RewardType rewardType) {
		return _rewardManager.getRewards(player, rewardType);
	}

	public com.houzicore.shared.core.preferences.PreferencesManager getPreferencesManager() {
		return _preferencesManager;
	}

	public boolean isOpening(Player player) {
		for (final TreasureLocation treasureLocation : _treasureLocations) {
			final Treasure treasure = treasureLocation.getCurrentTreasure();

			if (treasure == null) {
				continue;
			}

			if (treasure.getPlayer().equals(player))
				return true;
		}

		return false;
	}
}
