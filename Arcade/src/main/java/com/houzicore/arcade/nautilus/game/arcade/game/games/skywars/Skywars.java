package com.houzicore.arcade.nautilus.game.arcade.game.games.skywars;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;

import org.bukkit.boss.BarColor;

import com.houzicore.shared.common.util.C;
import com.houzicore.shared.common.util.F;
import com.houzicore.shared.common.util.MapUtil;
import com.houzicore.shared.common.util.NautHashMap;
import com.houzicore.shared.common.util.UtilAlg;
import com.houzicore.shared.common.util.UtilEnt;
import com.houzicore.shared.common.util.UtilMath;
import com.houzicore.shared.common.util.UtilParticle;
import com.houzicore.shared.common.util.UtilParticle.ParticleType;
import com.houzicore.shared.common.util.UtilParticle.ViewDist;
import com.houzicore.shared.common.util.UtilPlayer;
import com.houzicore.shared.common.util.UtilServer;
import com.houzicore.shared.common.util.UtilTextMiddle;
import com.houzicore.shared.common.util.UtilTextTop;
import com.houzicore.shared.common.util.UtilTextBottom;
import com.houzicore.shared.common.util.UtilTime;
import com.houzicore.shared.common.util.UtilTime.TimeUnit;
import com.houzicore.shared.core.explosion.ExplosionEvent;
import com.houzicore.shared.core.loot.ChestLoot;
import com.houzicore.shared.core.loot.RandomItem;
import com.houzicore.shared.updater.UpdateType;
import com.houzicore.shared.updater.event.UpdateEvent;
import com.houzicore.shared.core.combat.CombatComponent;
import com.houzicore.shared.core.combat.event.CombatDeathEvent;
import org.bukkit.GameMode;
import com.houzicore.arcade.ArcadeManager;
import com.houzicore.arcade.GameType;
import com.houzicore.arcade.nautilus.game.arcade.events.GameStateChangeEvent;
import com.houzicore.arcade.nautilus.game.arcade.game.Game;
import com.houzicore.arcade.nautilus.game.arcade.game.modules.CompassModule;
import com.houzicore.arcade.nautilus.game.arcade.game.GameTeam;
import com.houzicore.arcade.nautilus.game.arcade.game.SoloGame;
import com.houzicore.arcade.nautilus.game.arcade.game.games.skywars.data.RaidBell;
import com.houzicore.arcade.nautilus.game.arcade.game.games.skywars.events.PlayerKillZombieEvent;
import com.houzicore.arcade.nautilus.game.arcade.game.games.skywars.kits.KitEggman;
import com.houzicore.arcade.nautilus.game.arcade.game.games.skywars.kits.KitDestructor;
import com.houzicore.arcade.nautilus.game.arcade.game.games.skywars.kits.KitProspector;
import com.houzicore.arcade.nautilus.game.arcade.game.games.skywars.kits.KitChemist;
import com.houzicore.arcade.nautilus.game.arcade.game.games.skywars.kits.KitArcher;
import com.houzicore.arcade.nautilus.game.arcade.game.games.skywars.kits.KitBuilder;
import com.houzicore.arcade.nautilus.game.arcade.kit.Kit;
import com.houzicore.arcade.nautilus.game.arcade.game.games.skywars.data.OreNode;
import com.houzicore.arcade.nautilus.game.arcade.game.games.skywars.data.EnvironmentalHazardManager;
import com.houzicore.arcade.nautilus.game.arcade.stats.SkywarsKillZombieStatTracker;
import com.houzicore.arcade.nautilus.game.arcade.stats.WinWithoutOpeningChestStatTracker;
import com.houzicore.arcade.nautilus.game.arcade.stats.WinWithoutWearingArmorStatTracker;

import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.ChunkSnapshot;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.attribute.Attribute;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.BlockState;
import org.bukkit.block.Chest;
import org.bukkit.block.DoubleChest;
//import org.bukkit.craftbukkit.v1_7_R4.entity.CraftPlayer;
import org.bukkit.entity.Chicken;
import org.bukkit.entity.Egg;
import org.bukkit.entity.EnderPearl;
import org.bukkit.entity.Item;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.entity.Snowball;
import org.bukkit.entity.Phantom;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockBurnEvent;
import org.bukkit.event.block.BlockFadeEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.block.BlockSpreadEvent;
import org.bukkit.event.block.LeavesDecayEvent;
import org.bukkit.event.entity.EntityCombustEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.EntityTargetLivingEntityEvent;
import org.bukkit.event.entity.ItemDespawnEvent;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

@SuppressWarnings("deprecation")
public abstract class Skywars extends Game
{
	//./parse 19 30 56

	private static final int MAX_ORE_NODES = 48;
	private static final double MIN_ORE_NODE_DISTANCE_SQUARED = 100.0;

	private long _crumbleTime = 720000; // 12 minutes
	private int _initialWorldBlocksCount = 0;
	private int _crumbleBlocksPerTick = 1;
	private long _lastCrumbleExplosion = 0;

	private ArrayList<Block> _worldBlocks = new ArrayList<Block>();
	private HashSet<Location> _lootedBlocks = new HashSet<Location>();
	private boolean _alreadyAnnounced;

	private NautHashMap<Phantom, Location> _phantoms = new NautHashMap<Phantom, Location>();

	// Chest Refill
	private long _lastRefillTime = 0;
	private int _refillCount = 0;
	private static final long REFILL_INTERVAL = 180000; // 3 minutes
	private static final int MAX_REFILLS = 2;

	private ArrayList<Block> _spawnChests = new ArrayList<Block>();
	private ArrayList<Block> _middleChests = new ArrayList<Block>();

	private HashSet<Projectile> _pearls = new HashSet<Projectile>();

	private ArrayList<OreNode> _oreNodes = new ArrayList<>();

	// Environmental Hazard System
	private EnvironmentalHazardManager _hazardManager;
	private com.houzicore.arcade.nautilus.game.arcade.game.modules.objective.GameObjectiveModule _objectiveModule;

	private ChestLoot _playerArmor = new ChestLoot();
	private ChestLoot _playerFood = new ChestLoot();
	private ChestLoot _playerTool = new ChestLoot();
	private ChestLoot _playerProjectile = new ChestLoot();
	private ChestLoot _playerBlock = new ChestLoot();

	private ChestLoot _middleArmor = new ChestLoot();
	private ChestLoot _middleFood = new ChestLoot();
	private ChestLoot _middleTool = new ChestLoot();
	private ChestLoot _middleProjectile = new ChestLoot();
	private ChestLoot _middleBlock = new ChestLoot();

	@SuppressWarnings("unchecked")
	public Skywars(ArcadeManager manager, GameType type, String[] description)
	{
		super(manager, type, new Kit[]
				{
				new KitEggman(manager),
				new KitProspector(manager),
				new KitChemist(manager),
				new KitDestructor(manager),
				new KitArcher(manager),
				new KitBuilder(manager)
				}, description);

		PrepareFreeze = true;

		HideTeamSheep = true;

		registerModule(new CompassModule(this));
		_objectiveModule = new com.houzicore.arcade.nautilus.game.arcade.game.modules.objective.GameObjectiveModule(this);
		registerModule(_objectiveModule);
		StrictAntiHack = true;

		GameTimeout = 1500000L;

		DeathDropItems = true;

		QuitDropItems = true;

		WorldTimeSet = 0;
		WorldBoundaryKill = false;

		DamageSelf = true;
		DamageTeamSelf = true;
		DamageEvP = true;
		Damage = true;

		DeathDropItems = true;

		ItemDrop = true;
		ItemPickup = true;

		BlockBreak = true;
		BlockPlace = true;

		InventoryClick = true;
		InventoryOpenBlock = true;
		InventoryOpenChest = true;

		PlaySoundGameStart = true;
		PrepareTime = 10000L;

		DontAllowOverfill = true;



		_help = new String[]
				{

				};

		setupPlayerLoot();
		setupMiddleLoot();

		setAlreadyAnnounced(false);

		registerStatTrackers(
				new SkywarsKillZombieStatTracker(this),
				new WinWithoutOpeningChestStatTracker(this),
				new WinWithoutWearingArmorStatTracker(this));

	}

	public void ParseData()
	{
		parseCreateMiddleChests();
		parseCreatePlayerChests();
		parseCreatePlayerWebs();
		parseCreateOreNodes();

		// Remove Sponge (Holds up Sand)
		for (Location loc : WorldData.GetCustomLocs("19"))
		{
			MapUtil.QuickChangeBlockAt(loc, Material.AIR);
		}

		// Raid Bell (Secondary Islands)
		for (Location loc : WorldData.GetDataLocs("LIME"))
		{
			RaidBell bell = new RaidBell(this, loc);
			_objectiveModule.registerObjective(bell);
		}

		// Register Blocks with ChunkSnapshot Optimization
		int minChunkX = WorldData.MinX >> 4;
		int maxChunkX = (WorldData.MaxX - 1) >> 4;
		int minChunkZ = WorldData.MinZ >> 4;
		int maxChunkZ = (WorldData.MaxZ - 1) >> 4;
		
		int minWorldY = WorldData.World.getMinHeight();
		int maxWorldY = WorldData.World.getMaxHeight();
		
		for (int cx = minChunkX; cx <= maxChunkX; cx++)
		{
			for (int cz = minChunkZ; cz <= maxChunkZ; cz++)
			{
				Chunk chunk = WorldData.World.getChunkAt(cx, cz);
				ChunkSnapshot snapshot = chunk.getChunkSnapshot(false, false, false);
				
				int startX = Math.max(WorldData.MinX, cx << 4);
				int endX = Math.min(WorldData.MaxX - 1, (cx << 4) + 15);
				int startZ = Math.max(WorldData.MinZ, cz << 4);
				int endZ = Math.min(WorldData.MaxZ - 1, (cz << 4) + 15);
				
				for (int y = WorldData.MinY; y < WorldData.MaxY; y++)
				{
					int sectionY = (y - minWorldY) >> 4;
					if (sectionY >= 0 && sectionY < ((maxWorldY - minWorldY) >> 4))
					{
						if (snapshot.isSectionEmpty(sectionY))
						{
							// Skip the rest of this 16-block section
							y = ((sectionY + 1) << 4) + minWorldY - 1;
							continue;
						}
					}
					
					for (int x = startX; x <= endX; x++)
					{
						for (int z = startZ; z <= endZ; z++)
						{
							int rx = x & 15;
							int rz = z & 15;
							Material mat = snapshot.getBlockType(rx, y, rz);
							if (mat != Material.AIR && mat != Material.WATER && mat != Material.LAVA)
							{
								_worldBlocks.add(WorldData.World.getBlockAt(x, y, z));
							}
						}
					}
				}
			}
		}
		_initialWorldBlocksCount = _worldBlocks.size();
	}

	private void parseCreateOreNodes()
	{
		ArrayList<Location> candidates = new ArrayList<Location>(WorldData.GetCustomLocs("56"));
		if (candidates.isEmpty())
			return;

		for (Location oreLoc : candidates)
			MapUtil.QuickChangeBlockAt(oreLoc, Material.STONE);

		java.util.Collections.shuffle(candidates);
		for (Location oreLoc : candidates)
		{
			if (_oreNodes.size() >= MAX_ORE_NODES)
				break;

			if (!isFarEnoughFromOreNodes(oreLoc))
				continue;

			_oreNodes.add(new OreNode(this, oreLoc));
		}
	}

	private boolean isFarEnoughFromOreNodes(Location candidate)
	{
		for (OreNode node : _oreNodes)
		{
			Location existing = node.getLocation();
			if (existing.getWorld() != candidate.getWorld())
				continue;

			if (existing.distanceSquared(candidate) < MIN_ORE_NODE_DISTANCE_SQUARED)
				return false;
		}

		return true;
	}

	private boolean isOreNodeBlock(Block block)
	{
		Location blockLoc = block.getLocation();
		for (OreNode node : _oreNodes)
		{
			if (node.containsBlock(blockLoc))
				return true;
		}

		return false;
	}

	private void parseCreateVoidPhantoms()
	{
		// Void Phantoms (guards center island)
		for (Location loc : WorldData.GetDataLocs("RED"))
		{
			CreatureAllowOverride = true;
			Phantom phantom = (Phantom) loc.getWorld().spawn(loc, Phantom.class);
			CreatureAllowOverride = false;

			if (phantom != null)
			{
				phantom.setRemoveWhenFarAway(false);
				phantom.setCustomName(C.cDPurple + "Void Phantom");
				phantom.setCustomNameVisible(true);
				phantom.getAttribute(Attribute.MAX_HEALTH).setBaseValue(20);
				phantom.setHealth(20);
				phantom.setSize(2); // Slightly larger than default

				_phantoms.put(phantom, loc);
			}
			else
			{
				System.out.println("[Skywars] Warning: Failed to spawn Void Phantom at " + loc.getBlockX() + "," + loc.getBlockY() + "," + loc.getBlockZ());
			}
		}
	}

	private void parseCreateMiddleChests()
	{
		// Spawn ALL yellow locations as middle chests
		for (Location loc : WorldData.GetDataLocs("YELLOW"))
		{
			loc.getBlock().setType(org.bukkit.Material.CHEST);
			org.bukkit.block.data.type.Chest chestData = (org.bukkit.block.data.type.Chest) loc.getBlock().getBlockData();
			chestData.setFacing(new org.bukkit.block.BlockFace[]{org.bukkit.block.BlockFace.NORTH, org.bukkit.block.BlockFace.SOUTH, org.bukkit.block.BlockFace.EAST, org.bukkit.block.BlockFace.WEST}[com.houzicore.shared.common.util.UtilMath.r(4)]);
			loc.getBlock().setBlockData(chestData);
			_middleChests.add(loc.getBlock());
		}
	}

	private void parseCreatePlayerChests()
	{
		for (Location chestLoc : WorldData.GetDataLocs("BROWN"))
		{
			Location closestSpawn = UtilAlg.findClosest(chestLoc,
					GetTeamList().get(0).GetSpawns());

			if (UtilMath.offset2d(chestLoc, closestSpawn) > 8)
				continue;

			_spawnChests.add(chestLoc.getBlock());
			chestLoc.getBlock().setType(org.bukkit.Material.CHEST);
			org.bukkit.block.data.type.Chest chestData = (org.bukkit.block.data.type.Chest) chestLoc.getBlock().getBlockData();
			chestData.setFacing(new org.bukkit.block.BlockFace[]{org.bukkit.block.BlockFace.NORTH, org.bukkit.block.BlockFace.SOUTH, org.bukkit.block.BlockFace.EAST, org.bukkit.block.BlockFace.WEST}[com.houzicore.shared.common.util.UtilMath.r(4)]);
			chestLoc.getBlock().setBlockData(chestData);
		}
	}

	private void parseCreatePlayerWebs()
	{
		// Store which chests are closest to which spawn
		NautHashMap<Location, ArrayList<Location>> islandWebs = new NautHashMap<Location, ArrayList<Location>>();

		// Allocate chests to their nearest spawn point
		for (Location webLoc : WorldData.GetCustomLocs("30"))
		{
			// Gets the spawn point closest to the current chest
			Location closestSpawn = UtilAlg.findClosest(webLoc,
					GetTeamList().get(0).GetSpawns());

			if (UtilMath.offset2d(webLoc, closestSpawn) > 8)
				continue;

			// Ensure the list exists
			if (!islandWebs.containsKey(closestSpawn))
				islandWebs.put(closestSpawn, new ArrayList<Location>());

			// Add this chest location to the spawn
			islandWebs.get(closestSpawn).add(webLoc);
		}

		// Create 2 Webs
		for (ArrayList<Location> webs : islandWebs.values())
		{
			for (int i = 0; i < 2; i++)
			{
				if (!webs.isEmpty())
				{
					webs.remove(UtilAlg.Random(webs));
				}
			}

			for (Location loc : webs)
			{
				loc.getBlock().setType(Material.AIR);
			}
		}
	}


	@EventHandler
	public void blockUpdate(UpdateEvent event)
	{
		if (event.getType() != UpdateType.TICK)
		{
			return;
		}

		if (!IsLive())
		{
			return;
		}

		if (_worldBlocks.isEmpty())
		{
			return;
		}

		if (!UtilTime.elapsed(GetStateTime(), _crumbleTime))
		{
			return;
		}

		// Suppress Crumble if Void Rising was selected this game
		if (_hazardManager != null && _hazardManager.isVoidRisingActive())
			return;

		if (!alreadyAnnounced())
		{
			for (Player player : UtilServer.getPlayers())
			{
				boolean th = player != null && "THA".equals(com.houzicore.shared.core.lang.LangManager.get().resolveLocaleStr(player));
				player.sendMessage(F.main("Game", th ? "§c§l⚠ แผนที่กำลังถล่ม!" : "§c§l⚠ The map is crumbling!"));
			}
			Player[] arrayOfPlayer;
			int j = (arrayOfPlayer = UtilServer.getPlayers()).length;
			for (int i = 0; i < j; i++)
			{
				Player player = arrayOfPlayer[i];

				player.playSound(player.getLocation(), Sound.ENTITY_ENDER_DRAGON_GROWL,
						3.0F, 1.0F);
			}
			setAlreadyAnnounced(true);

			// Pre-sort blocks from closest/highest to furthest/lowest
			java.util.Collections.sort(_worldBlocks, new java.util.Comparator<Block>() {
				public int compare(Block b1, Block b2) {
					double d1 = UtilMath.offset2d(GetSpectatorLocation(), b1.getLocation());
					double d2 = UtilMath.offset2d(GetSpectatorLocation(), b2.getLocation());
					int distCompare = Double.compare(d1, d2);
					if (distCompare != 0) {
						return distCompare; // Ascending distance (furthest is at the end)
					}
					return Integer.compare(b2.getY(), b1.getY()); // If same column, higher Y first (lowest is at the end)
				}
			});

			// Target 2 minutes (120 seconds = 2400 ticks) to destroy the entire map
			_crumbleBlocksPerTick = Math.min(150, Math.max(1, (int) Math.ceil(_worldBlocks.size() / 2400.0)));
		}

		for (int i = 0; i < _crumbleBlocksPerTick; i++)
		{
			if (_worldBlocks.isEmpty())
				break;

			// Pop from the end of the list (furthest and lowest)
			Block bestBlock = _worldBlocks.remove(_worldBlocks.size() - 1);

			if (bestBlock.getType() != Material.AIR)
			{
				if (Math.random() > 0.98D)
				{
					bestBlock.getWorld().spawnFallingBlock(
							bestBlock.getLocation().add(0.5D, 0.5D, 0.5D),
							bestBlock.getBlockData());
				}
				if (Math.random() > 0.95D)
				{
					UtilParticle.PlayParticle(ParticleType.LARGE_EXPLODE, bestBlock.getLocation().add(0.5D, 0.5D, 0.5D), 0f, 0f, 0f, 0f, 1, ViewDist.MAX, UtilServer.getPlayers());
				}

				// --- VFX Upgrade: Cinematic Explosions ---
				if (com.houzicore.shared.common.util.UtilTime.elapsed(_lastCrumbleExplosion, 1000))
				{
					_lastCrumbleExplosion = System.currentTimeMillis();
					com.houzicore.shared.common.util.effect.AnimatedExplosion.create(bestBlock.getLocation(), 3.5, 2.0)
							.withSound(Sound.ENTITY_DRAGON_FIREBALL_EXPLODE)
							.withParticle(ParticleType.HUGE_EXPLOSION)
							.ignite(Manager.getPlugin());
				}

				MapUtil.QuickChangeBlockAt(bestBlock.getLocation(),
						Material.AIR);
			}
		}
	}

	@EventHandler
	public void phantomCombust(EntityCombustEvent event)
	{
		if (event.getEntity() instanceof Phantom && _phantoms.containsKey((Phantom)event.getEntity()))
		{
			event.setCancelled(true);
		}
	}


	@EventHandler
	public void initRefillTimer(GameStateChangeEvent event)
	{
		if (event.GetState() != Game.GameState.Live)
			return;

		parseCreateVoidPhantoms();

		_lastRefillTime = System.currentTimeMillis();
		_refillCount = 0;

		// Initialize environmental hazard manager
		_hazardManager = new EnvironmentalHazardManager(this, Manager.getPlugin());
	}

	@EventHandler
	public void sandMapWarning(GameStateChangeEvent event)
	{
		if (event.GetState() != Game.GameState.Live)
			return;

		if (WorldData.MapName.equals("Sahara"))
		{
			for (Player p : UtilServer.getPlayers())
			{
				boolean th = p != null && "THA".equals(com.houzicore.shared.core.lang.LangManager.get().resolveLocaleStr(p));
				UtilTextBottom.display(com.houzicore.shared.common.actionbar.ActionBarChannel.GAME_STATUS,
					th ? "§c⚠ คำเตือน: ทรายแดงไม่เสถียร!" : "§c⚠ Warning: Red sand is unstable!", p);
			}
		}
	}





	@EventHandler
	public void chestOpen(PlayerInteractEvent event)
	{
		if (event.getClickedBlock() == null) return;
		if (event.getClickedBlock().getType() != Material.CHEST) return;

		Block block = event.getClickedBlock();
		BlockState state = block.getState();
		if ((state instanceof DoubleChest))
		{
			DoubleChest doubleChest = (DoubleChest) state;
			fillChest(event.getPlayer(), ((Chest) doubleChest.getLeftSide()).getBlock());
			fillChest(event.getPlayer(), ((Chest) doubleChest.getRightSide()).getBlock());
		}
		else if ((state instanceof Chest))
		{
			fillChest(event.getPlayer(), block);
		}
	}

	@EventHandler
	public void chestRefill(UpdateEvent event)
	{
		if (event.getType() != UpdateType.SEC)
			return;

		if (!IsLive())
			return;

		if (_refillCount >= MAX_REFILLS)
			return;

		if (!UtilTime.elapsed(_lastRefillTime, REFILL_INTERVAL))
			return;

		_refillCount++;
		_lastRefillTime = System.currentTimeMillis();

		// Clear looted tracking so all chests can be re-looted
		_lootedBlocks.clear();

		// Announce
		for (Player player : UtilServer.getPlayers())
		{
			boolean th = player != null && "THA".equals(com.houzicore.shared.core.lang.LangManager.get().resolveLocaleStr(player));
			player.sendMessage(F.main("Game", th 
				? "§e§l📦 หีบทั้งหมดถูกเติมใหม่! §7(" + _refillCount + "/" + MAX_REFILLS + ")"
				: "§e§l📦 All chests have been refilled! §7(" + _refillCount + "/" + MAX_REFILLS + ")"));
		}

		for (Player p : UtilServer.getPlayers())
		{
			p.playSound(p.getLocation(), org.bukkit.Sound.BLOCK_CHEST_OPEN, 1.5f, 1.2f);
		}
	}

	@EventHandler
	public void bossBarUpdate(UpdateEvent event)
	{
		if (event.getType() != UpdateType.FASTER)
			return;

		if (!IsLive())
			return;

		Player[] players = UtilServer.getPlayers();
		if (players.length == 0) return;

		long elapsed = System.currentTimeMillis() - GetStateTime();

		if (_hazardManager != null && _hazardManager.isVoidRisingActive())
			return;

		String text = "";
		double progress = 1.0;
		BarColor color = BarColor.YELLOW;

		if (elapsed >= _crumbleTime)
		{
			// CRUMBLE ACTIVE phase
			progress = _initialWorldBlocksCount == 0 ? 0f : (float)_worldBlocks.size() / (float)_initialWorldBlocksCount;
			progress = Math.max(0f, Math.min(1f, progress));

			text = "§5§l☠ MAP CRUMBLE ☠";
			color = BarColor.PURPLE;
		}
		else if (elapsed >= _crumbleTime - 60000)
		{
			// Crumble warning phase (last 1 minute before crumble)
			long remaining = _crumbleTime - elapsed;
			int seconds = (int)(remaining / 1000);
			String timeStr = String.format("%02d:%02d", seconds / 60, seconds % 60);

			text = "§c⚠ Map Crumble — §f" + timeStr;
			color = BarColor.RED;
			progress = Math.max(0f, (float)remaining / 60000f);
		}
		else if (_refillCount < MAX_REFILLS)
		{
			// Chest Refill phase
			long refillElapsed = System.currentTimeMillis() - _lastRefillTime;
			long remaining = REFILL_INTERVAL - refillElapsed;
			if (remaining < 0) remaining = 0;
			int seconds = (int)(remaining / 1000);
			String timeStr = String.format("%02d:%02d", seconds / 60, seconds % 60);

			color = remaining <= 60000 ? BarColor.RED : BarColor.YELLOW;
			String prefix = color == BarColor.RED ? "§c" : "§e";

			text = prefix + "📦 Chest Refill — §f" + timeStr;
			progress = Math.max(0f, Math.min(1f, 1f - (float)refillElapsed / (float)REFILL_INTERVAL));
		}
		else
		{
			// After all refills, show crumble countdown
			long remaining = _crumbleTime - elapsed;
			if (remaining < 0) remaining = 0;
			int seconds = (int)(remaining / 1000);
			String timeStr = String.format("%02d:%02d", seconds / 60, seconds % 60);

			color = remaining <= 60000 ? BarColor.RED : BarColor.YELLOW;
			String prefix = color == BarColor.RED ? "§c" : "§e";

			text = prefix + "💥 Map Crumble — §f" + timeStr;
			progress = Math.max(0f, Math.min(1f, (float)remaining / (float)_crumbleTime));
		}

		UtilTextTop.displayProgress(text, progress, color, players);
	}



	@EventHandler
	public void raidBellUpdate(UpdateEvent event)
	{
		if (event.getType() != UpdateType.TICK)
			return;

		if (!IsLive())
			return;


		for (OreNode node : _oreNodes)
		{
			node.update();
		}

		// Tick hazard manager
		if (_hazardManager != null)
			_hazardManager.tick();
	}

	@EventHandler
	public void cleanUpBells(GameStateChangeEvent event)
	{
		if (event.GetState() != Game.GameState.Dead)
			return;


		for (OreNode node : _oreNodes)
		{
			node.cleanUp();
		}

		if (_hazardManager != null)
		{
			_hazardManager.cleanUp();
			_hazardManager = null;
		}

		// Remove BossBar
		UtilTextTop.displayProgress("", 0, BarColor.YELLOW, UtilServer.getPlayers());
	}




	@EventHandler
	public void ItemDespawn(ItemDespawnEvent event)
	{
		event.setCancelled(true);
	}



	@EventHandler
	public void phantomUpdate(UpdateEvent event)
	{
		if (event.getType() != UpdateType.TICK)
			return;

		Iterator<Phantom> phantomIter = _phantoms.keySet().iterator();

		while (phantomIter.hasNext())
		{
			Phantom phantom = phantomIter.next();

			if (!phantom.isValid())
			{
				phantomIter.remove();
				continue;
			}

			Location loc = _phantoms.get(phantom);

			// Keep phantom near its spawn point
			if (UtilMath.offset(phantom.getLocation(), loc) > 12)
			{
				phantom.teleport(loc.clone().add(0, 3, 0));
			}

			// Force target nearest alive player within range
			if (phantom.getTarget() == null || !phantom.getTarget().isValid() || UtilMath.offset(phantom.getTarget().getLocation(), loc) > 12)
			{
				Player closest = null;
				double closestDist = 12.0;
				for (Player p : GetPlayers(true))
				{
					if (!IsAlive(p)) continue;
					double d = UtilMath.offset(p.getLocation(), loc);
					if (d <= closestDist)
					{
						closestDist = d;
						closest = p;
					}
				}

				if (closest != null)
				{
					phantom.setTarget(closest);
				}
				else
				{
					phantom.setTarget(null);
				}
			}
		}
	}


	@EventHandler
	public void phantomTarget(EntityTargetLivingEntityEvent event)
	{
		if (event.getEntity() instanceof Phantom && _phantoms.containsKey((Phantom)event.getEntity()))
		{
			Phantom phantom = (Phantom)event.getEntity();
			Location loc = _phantoms.get(phantom);

			if (event.getTarget() != null && UtilMath.offset(event.getTarget().getLocation(), loc) > 12)
			{
				event.setCancelled(true);
			}
		}
	}

	@EventHandler
	public void phantomKnockback(EntityDamageByEntityEvent event)
	{
		if (event.getDamager() instanceof Phantom && _phantoms.containsKey((Phantom)event.getDamager()))
		{
			if (event.getEntity() instanceof Player)
			{
				// Extra knockback - launch upward to risk falling off edges
				org.bukkit.util.Vector kb = event.getEntity().getLocation().toVector()
					.subtract(event.getDamager().getLocation().toVector())
					.normalize().multiply(1.2).setY(0.6);
				event.getEntity().setVelocity(kb);
			}
		}
	}

	@EventHandler
	public void blockBurn(BlockBurnEvent event)
	{
		event.setCancelled(true);
	}

	@EventHandler
	public void blockDecay(LeavesDecayEvent event)
	{
		event.setCancelled(true);
	}

	@EventHandler
	public void blockFade(BlockFadeEvent event)
	{
		event.setCancelled(true);
	}

	@EventHandler
	public void blockSpread(BlockSpreadEvent event)
	{
		event.setCancelled(true);
	}

	@EventHandler
	public void onBlockPlaceAdd(BlockPlaceEvent e)
	{
		_worldBlocks.add(e.getBlock());

		if (e.getBlock().getType() == Material.CHEST ||
			e.getBlock().getType() == Material.PISTON ||
			e.getBlock().getType() == Material.STICKY_PISTON ||
			e.getBlock().getType() == Material.HOPPER)
		{
			e.setCancelled(true);
		}
	}

	@EventHandler
	public void blockBonusDrops(BlockBreakEvent e)
	{
		e.setExpToDrop(0);

		final Block block = e.getBlock();

		if (isOreNodeBlock(block))
			return;

		if (e.getBlock().getType() == Material.COBWEB)
		{
			Bukkit.getScheduler().runTaskLater(Manager.getPlugin(), new Runnable()
			{
				@Override
				public void run()
				{
					for (int i=0 ; i<1 + UtilMath.r(2) ; i++)
						block.getWorld().dropItem(block.getLocation().add(0.5, 0.2, 0.5), new ItemStack(Material.STRING));

				}}, 1);

		}

		if (e.getBlock().getType() == Material.GRAVEL)
		{
			e.setCancelled(true);
			e.getBlock().setType(Material.AIR);

			Bukkit.getScheduler().runTaskLater(Manager.getPlugin(), new Runnable()
			{
				@Override
				public void run()
				{
					for (int i=0 ; i<1 + UtilMath.r(3) ; i++)
						block.getWorld().dropItem(block.getLocation().add(0.5, 0.2, 0.5), new ItemStack(Material.FLINT));

				}}, 1);
		}

		if (e.getBlock().getType() == Material.IRON_ORE)
		{
			e.setCancelled(true);
			e.getBlock().setType(Material.AIR);

			Bukkit.getScheduler().runTaskLater(Manager.getPlugin(), new Runnable()
			{
				@Override
				public void run()
				{
					block.getWorld().dropItem(block.getLocation().add(0.5, 0.2, 0.5), new ItemStack(Material.IRON_INGOT));

				}}, 1);
		}
	}

	@EventHandler
	public void onKillZombie(EntityDeathEvent e)
	{
		if (e.getEntity() instanceof Phantom)
		{
			Phantom ent = (Phantom) e.getEntity();

			if (_phantoms.containsKey(ent))
			{
				if (ent.getKiller() instanceof Player)
				{
					Player p = ent.getKiller();

					Bukkit.getPluginManager().callEvent(
							new PlayerKillZombieEvent(p, ent));
				}
			}
		}
		else
		{
			return;
		}
	}

	@EventHandler
	public void blockBreak(BlockBreakEvent e)
	{
		// Protect Bell blocks (Raid Bell)
		if (e.getBlock().getType() == Material.BELL)
		{
			e.setCancelled(true);
		}
	}

	@EventHandler
	public void disableDamageToLevel(EntityDamageByEntityEvent event)
	{
		// event.SetDamageToLevel(false); // not migrated
	}

	@EventHandler
	public void mobLoot(EntityDeathEvent event)
	{
		//Phantom Loot
		if (event.getEntity() instanceof Phantom && _phantoms.containsKey((Phantom)event.getEntity()))
		{
			event.getDrops().clear();

			// Always drop 1 Golden Apple
			event.getDrops().add(new ItemStack(Material.GOLDEN_APPLE));

			// 10% chance to also drop an Ender Pearl
			if (Math.random() < 0.10)
				event.getDrops().add(new ItemStack(Material.ENDER_PEARL));
		}
		//Chicken Loot
		else if (event.getEntity() instanceof Chicken)
		{
			event.getDrops().clear();

			event.getDrops().add(new ItemStack(Material.FEATHER, 1 + UtilMath.r(4)));
		}
	}

	@EventHandler
	public void eggHit(EntityDamageByEntityEvent event)
	{
		if (event.getDamager() == null)
			return;

		if (event.getDamage() >= 1)
			return;

		Player damagerPlayer = null;
		if (event.getDamager() instanceof Player)
		{
			damagerPlayer = (Player) event.getDamager();
		}
		else if (event.getDamager() instanceof Projectile)
		{
			Projectile proj = (Projectile) event.getDamager();
			if (proj.getShooter() instanceof Player)
			{
				damagerPlayer = (Player) proj.getShooter();
			}
		}

		Player damageePlayer = null;
		if (event.getEntity() instanceof Player)
		{
			damageePlayer = (Player) event.getEntity();
		}

		if (damagerPlayer != null && damageePlayer != null)
		{
			if (GetTeam(damagerPlayer) == GetTeam(damageePlayer))
			{
				return;
			}
		}

		if (event.getDamager() instanceof Egg || event.getDamager() instanceof Snowball)
		{
			event.setCancelled(true);

			if (!(event.getEntity() instanceof LivingEntity)) return;
			LivingEntity damagee = (LivingEntity) event.getEntity();
			Projectile projectile = (Projectile) event.getDamager();
			LivingEntity shooter = projectile.getShooter() instanceof LivingEntity ? (LivingEntity) projectile.getShooter() : null;

			// Damage Event
			Manager.GetDamage().NewDamageEvent(damagee, shooter, projectile,
					DamageCause.PROJECTILE, 1, false, true, false,
					UtilEnt.getName(shooter),
					(event.getDamager() instanceof Egg ? "Egg" : "Snowball"));


			Vector vel = event.getDamager().getVelocity().multiply(0.2);

			if (vel.getY() < 0.1)
				vel.setY(0.1);

			event.getEntity().setVelocity(vel);
		}
	}

	@EventHandler
	public void projectile(EntityDamageByEntityEvent event)
	{
		if(event.getDamager() instanceof Snowball || event.getDamager() instanceof Egg || event.getDamager() instanceof EnderPearl)
		{
			Projectile prj = (Projectile) event.getDamager();
			if(prj.getShooter() instanceof Player)
			{
				if(event.getEntity() instanceof Player)
				{
					if(GetTeam((Player) prj.getShooter()) == GetTeam((Player) event.getEntity()))
					{
						event.setCancelled(true);
					}
				}
			}
		}
	}

	private void fillChest(Player looter, Block block)
	{
		if (_lootedBlocks.contains(block.getLocation()))
			return;

		_lootedBlocks.add(block.getLocation());
		Chest chest = (Chest) block.getState();

		chest.getBlockInventory().clear();

		//Prevents same inventory spot being used twice
		HashSet<Integer> used = new HashSet<Integer>();

		//Player Island
		if (_spawnChests.contains(block))
		{
			//Armor
			for (int i=0 ; i<1 + UtilMath.r(2) ; i++)
				chest.getBlockInventory().setItem(getIndex(used), _playerArmor.getLoot());

			//Food
			for (int i=0 ; i<1 + UtilMath.r(3) ; i++)
				chest.getBlockInventory().setItem(getIndex(used), _playerFood.getLoot());

			//Tool
			for (int i=0 ; i<1 + UtilMath.r(2) ; i++)
				chest.getBlockInventory().setItem(getIndex(used), _playerTool.getLoot());

			//Projectile
			for (int i=0 ; i<1 + UtilMath.r(2) ; i++)
				chest.getBlockInventory().setItem(getIndex(used), _playerProjectile.getLoot());

			//Block
			for (int i=0 ; i<1 + UtilMath.r(2) ; i++)
				chest.getBlockInventory().setItem(getIndex(used), _playerBlock.getLoot());
		}
		//Other
		else if (_middleChests.contains(block))
		{
			//Armor
			for (int i=0 ; i<1 + UtilMath.r(2) ; i++)
				chest.getBlockInventory().setItem(getIndex(used), _middleArmor.getLoot());

			//Food
			for (int i=0 ; i<1 + UtilMath.r(3) ; i++)
				chest.getBlockInventory().setItem(getIndex(used), _middleFood.getLoot());

			//Tool
			for (int i=0 ; i<1 + UtilMath.r(2) ; i++)
				chest.getBlockInventory().setItem(getIndex(used), _middleTool.getLoot());

			//Projectile
			for (int i=0 ; i<1 + UtilMath.r(2) ; i++)
				chest.getBlockInventory().setItem(getIndex(used), _middleProjectile.getLoot());

			//Block
			for (int i=0 ; i<1 + UtilMath.r(2) ; i++)
				chest.getBlockInventory().setItem(getIndex(used), _middleBlock.getLoot());
		}
		else
		{
			//Armor
			for (int i=0 ; i<UtilMath.r(2) ; i++)
				chest.getBlockInventory().setItem(getIndex(used), _middleArmor.getLoot());

			//Food
			for (int i=0 ; i<UtilMath.r(3) ; i++)
				chest.getBlockInventory().setItem(getIndex(used), _middleFood.getLoot());

			//Tool
			for (int i=0 ; i<UtilMath.r(2) ; i++)
				chest.getBlockInventory().setItem(getIndex(used), _middleTool.getLoot());

			//Projectile
			for (int i=0 ; i<UtilMath.r(2) ; i++)
				chest.getBlockInventory().setItem(getIndex(used), _middleProjectile.getLoot());

			//Block
			for (int i=0 ; i<UtilMath.r(2) ; i++)
				chest.getBlockInventory().setItem(getIndex(used), _middleBlock.getLoot());
		}
	}

	private int getIndex(HashSet<Integer> used)
	{
		int i = UtilMath.r(27);

		while (used.contains(i))
		{
			i = UtilMath.r(27);
		}

		used.add(i);

		return i;
	}


	private void setupPlayerLoot()
	{
		// Tier 1 Armor (Leather 60%, Chain 25%, Iron 15%)
		_playerArmor.addLoot(new RandomItem(Material.LEATHER_HELMET, 15));
		_playerArmor.addLoot(new RandomItem(Material.LEATHER_CHESTPLATE, 24));
		_playerArmor.addLoot(new RandomItem(Material.LEATHER_LEGGINGS, 21));
		_playerArmor.addLoot(new RandomItem(Material.LEATHER_BOOTS, 12));

		_playerArmor.addLoot(new RandomItem(Material.CHAINMAIL_HELMET, 6));
		_playerArmor.addLoot(new RandomItem(Material.CHAINMAIL_CHESTPLATE, 10));
		_playerArmor.addLoot(new RandomItem(Material.CHAINMAIL_LEGGINGS, 9));
		_playerArmor.addLoot(new RandomItem(Material.CHAINMAIL_BOOTS, 5));

		_playerArmor.addLoot(new RandomItem(Material.IRON_HELMET, 4));
		_playerArmor.addLoot(new RandomItem(Material.IRON_CHESTPLATE, 6));
		_playerArmor.addLoot(new RandomItem(Material.IRON_LEGGINGS, 5));
		_playerArmor.addLoot(new RandomItem(Material.IRON_BOOTS, 3));

		//Food
		_playerFood.addLoot(new RandomItem(Material.BAKED_POTATO, 1, 1, 4));
		_playerFood.addLoot(new RandomItem(Material.COOKED_BEEF, 1, 1, 2));
		_playerFood.addLoot(new RandomItem(Material.COOKED_CHICKEN, 1, 1, 2));
		_playerFood.addLoot(new RandomItem(Material.CARROT, 1, 1, 3));
		_playerFood.addLoot(new RandomItem(Material.BREAD, 1, 1, 3));
		_playerFood.addLoot(new RandomItem(Material.APPLE, 1, 1, 4));
		_playerFood.addLoot(new RandomItem(Material.PORKCHOP, 1, 1, 4));
		_playerFood.addLoot(new RandomItem(Material.ROTTEN_FLESH, 1, 1, 6));

		//Tools
		_playerTool.addLoot(new RandomItem(Material.WOODEN_SWORD, 2));
		_playerTool.addLoot(new RandomItem(Material.STONE_SWORD, 1));
		_playerTool.addLoot(new RandomItem(Material.FISHING_ROD, 2));

		_playerTool.addLoot(new RandomItem(Material.STONE_AXE, 2));
		_playerTool.addLoot(new RandomItem(Material.STONE_PICKAXE, 3));

		// No iron tools in Tier 1


		//Projectile
		_playerProjectile.addLoot(new RandomItem(Material.ARROW, 18, 2, 8));
		_playerProjectile.addLoot(new RandomItem(Material.SNOWBALL, 60, 2, 5));
		_playerProjectile.addLoot(new RandomItem(Material.EGG, 60, 2, 5));

		//Block
		_playerBlock.addLoot(new RandomItem(Material.COBBLESTONE, 30, 8, 16));
		_playerBlock.addLoot(new RandomItem(Material.DIRT, 30, 8, 16));
		_playerBlock.addLoot(new RandomItem(Material.OAK_PLANKS, 30, 8, 16));
	}

	private void setupMiddleLoot()
	{
		//Armor
		_middleArmor.addLoot(new RandomItem(Material.GOLDEN_HELMET, 20));
		_middleArmor.addLoot(new RandomItem(Material.GOLDEN_CHESTPLATE, 32));
		_middleArmor.addLoot(new RandomItem(Material.GOLDEN_LEGGINGS, 28));
		_middleArmor.addLoot(new RandomItem(Material.GOLDEN_BOOTS, 16));

		_middleArmor.addLoot(new RandomItem(Material.IRON_HELMET, 20));
		_middleArmor.addLoot(new RandomItem(Material.IRON_CHESTPLATE, 32));
		_middleArmor.addLoot(new RandomItem(Material.IRON_LEGGINGS, 28));
		_middleArmor.addLoot(new RandomItem(Material.IRON_BOOTS, 16));

		_middleArmor.addLoot(new RandomItem(Material.DIAMOND_HELMET, 5));
		_middleArmor.addLoot(new RandomItem(Material.DIAMOND_CHESTPLATE, 8));
		_middleArmor.addLoot(new RandomItem(Material.DIAMOND_LEGGINGS, 7));
		_middleArmor.addLoot(new RandomItem(Material.DIAMOND_BOOTS, 4));

		//Food (Tier 2)
		_middleFood.addLoot(new RandomItem(Material.COOKED_BEEF, 1, 1, 3));
		_middleFood.addLoot(new RandomItem(Material.COOKED_CHICKEN, 1, 1, 3));
		_middleFood.addLoot(new RandomItem(Material.MUSHROOM_STEW, 1));
		_middleFood.addLoot(new RandomItem(Material.COOKED_PORKCHOP, 1, 1, 3));
		_middleFood.addLoot(new RandomItem(Material.GOLDEN_APPLE, 1, 1, 1));

		//Tools
		_middleTool.addLoot(new RandomItem(Material.IRON_SWORD, 1));
		_middleTool.addLoot(new RandomItem(Material.DIAMOND_SWORD, 1));
		_middleTool.addLoot(new RandomItem(Material.FISHING_ROD, 1));

		_middleTool.addLoot(new RandomItem(Material.DIAMOND_AXE, 1));
		_middleTool.addLoot(new RandomItem(Material.DIAMOND_PICKAXE, 1));

		//Projectile
		_middleTool.addLoot(new RandomItem(Material.BOW, 1));
		_middleProjectile.addLoot(new RandomItem(Material.ARROW, 2, 4, 12));
		_middleProjectile.addLoot(new RandomItem(Material.ENDER_PEARL, 1, 1, 2));

		//Block
		_middleBlock.addLoot(new RandomItem(Material.BRICKS, 30, 12, 24));
		_middleBlock.addLoot(new RandomItem(Material.GLASS, 30, 12, 24));
		_middleBlock.addLoot(new RandomItem(Material.SOUL_SAND, 30, 12, 24));
	}

	public boolean alreadyAnnounced()
	{
		return _alreadyAnnounced;
	}

	public boolean setAlreadyAnnounced(boolean _already)
	{
		_alreadyAnnounced = _already;
		return _already;
	}

	@EventHandler
	public void handleExplosion(ExplosionEvent event)
	{
		// No longer needed
	}

	@EventHandler(priority = EventPriority.MONITOR)
	public void onOreMine(BlockBreakEvent event)
	{
		if (event.isCancelled())
			return;

		Location blockLoc = event.getBlock().getLocation();

		for (OreNode node : _oreNodes)
		{
			if (node.isOreBlock(blockLoc))
			{
				if (node.isReady())
				{
					// Mine this specific ore block
					node.depleteBlock(blockLoc);
				}
				else
				{
					event.setCancelled(true);
					event.getPlayer().sendMessage(C.cRed + "This ore is currently regenerating.");
				}
				return;
			}
		}
	}



	@EventHandler
	public void pearlRide(ProjectileLaunchEvent event)
	{
		if (!IsLive())
			return;

		if (!(event.getEntity() instanceof EnderPearl))
			return;

		if (event.getEntity().getShooter() == null)
			return;

		if (!(event.getEntity().getShooter() instanceof Player))
			return;

		Player shooter = (Player)event.getEntity().getShooter();

		if (GetKit(shooter) instanceof KitDestructor)
			return;

		event.getEntity().setPassenger(shooter);

		_pearls.add(event.getEntity());

		shooter.setGameMode(GameMode.SPECTATOR);
		shooter.setCollidable(false);
	}

	@EventHandler
	public void pearlUpdate(UpdateEvent event)
	{
		if (event.getType() != UpdateType.TICK)
			return;

		//Disable Spec

		Iterator<Projectile> pearlIter = _pearls.iterator();

		while (pearlIter.hasNext())
		{
			Projectile proj = pearlIter.next();

			if (!proj.isValid())
			{
				if (proj.getPassenger() instanceof Player) {
					Player shooter = (Player)proj.getPassenger();
					shooter.setGameMode(GameMode.SURVIVAL);
					shooter.setCollidable(true);
				}

				proj.remove();
				pearlIter.remove();
				continue;
			}

			UtilParticle.PlayParticle(ParticleType.WITCH_MAGIC, proj.getLocation(), 0f, 0f, 0f, 0f, 1, ViewDist.MAX, UtilServer.getPlayers());
		}
	}

	@EventHandler
	public void killLevelReward(CombatDeathEvent event)
	{
		Game game = Manager.GetGame();
		if (game == null)	return;

		if (!(event.GetEvent().getEntity() instanceof Player))
			return;

		Player killed = (Player)event.GetEvent().getEntity();

		if (event.GetEvent().getEntity().getKiller() != null)
		{
			Player killer = event.GetEvent().getEntity().getKiller();

			if (killer != null && !killer.equals(killed))
			{
				//Kill
				killer.giveExpLevels(2);

				killer.playSound(killer.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1f, 1f);
			}
		}

		for (CombatComponent log : event.GetLog().GetAttackers())
		{
		 	if (event.GetEvent().getEntity().getKiller() != null && log.GetName().equals(event.GetEvent().getEntity().getKiller().getName()))
				continue;

		 	Player assist = UtilPlayer.searchExact(log.GetName());

		 	//Assist
		 	if (assist != null)
		 	{
		 		assist.giveExpLevels(1);
		 		assist.playSound(assist.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1f, 1f);
		 	}
		}
	}

	@Override
	public double GetKillsGems(Player killer, Player killed, boolean assist)
	{
		if (assist)
			return 3;
		else
			return 12;
	}

	public long getCrumbleTime()
	{
		return this._crumbleTime;
	}

}
