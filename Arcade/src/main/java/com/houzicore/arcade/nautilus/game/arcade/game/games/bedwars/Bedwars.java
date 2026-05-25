package com.houzicore.arcade.nautilus.game.arcade.game.games.bedwars;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.Sound;
import org.bukkit.entity.EnderDragon;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityChangeBlockEvent;
import org.bukkit.event.entity.EntityRegainHealthEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;

import com.houzicore.shared.common.util.C;
import com.houzicore.shared.common.util.UtilAlg;
import com.houzicore.shared.common.util.UtilMath;
import com.houzicore.shared.common.util.UtilPlayer;
import com.houzicore.shared.common.util.UtilServer;
import com.houzicore.shared.common.util.UtilTime;
import com.houzicore.shared.core.itemstack.ItemBuilder;
import com.houzicore.shared.updater.UpdateType;
import com.houzicore.shared.updater.event.UpdateEvent;
import com.houzicore.shared.core.combat.DeathMessageType;
import com.houzicore.shared.core.damage.CustomDamageEvent;

import com.houzicore.arcade.ArcadeManager;
import com.houzicore.arcade.GameType;
import com.houzicore.arcade.nautilus.game.arcade.events.GameStateChangeEvent;
import com.houzicore.arcade.nautilus.game.arcade.events.PlayerKitGiveEvent;
import com.houzicore.arcade.nautilus.game.arcade.game.GameTeam;
import com.houzicore.arcade.nautilus.game.arcade.game.TeamGame;
import com.houzicore.arcade.nautilus.game.arcade.game.games.bedwars.capturepoint.BedwarsPointModule;
import com.houzicore.arcade.nautilus.game.arcade.game.games.bedwars.general.BedwarsBatModule;
import com.houzicore.arcade.nautilus.game.arcade.game.games.bedwars.general.BedwarsPlayerModule;
import com.houzicore.arcade.nautilus.game.arcade.game.games.bedwars.general.BedwarsSpawnerModule;
import com.houzicore.arcade.nautilus.game.arcade.game.games.bedwars.island.BedwarsIslandModule;
import com.houzicore.arcade.nautilus.game.arcade.game.games.bedwars.item.BedwarsItemModule;
import com.houzicore.arcade.nautilus.game.arcade.game.games.bedwars.item.BedwarsSpecialItem;
import com.houzicore.arcade.nautilus.game.arcade.game.games.bedwars.item.items.BedwarsDeployPlatform;
import com.houzicore.arcade.nautilus.game.arcade.game.games.bedwars.item.items.BedwarsSheep;
import com.houzicore.arcade.nautilus.game.arcade.game.games.bedwars.item.items.BedwarsWall;
import com.houzicore.arcade.nautilus.game.arcade.game.games.bedwars.kits.KitBedwarsArcher;
import com.houzicore.arcade.nautilus.game.arcade.game.games.bedwars.kits.KitBedwarsBuilder;
import com.houzicore.arcade.nautilus.game.arcade.game.games.bedwars.kits.KitBedwarsFrosting;
import com.houzicore.arcade.nautilus.game.arcade.game.games.bedwars.kits.KitBedwarsWarrior;
import com.houzicore.arcade.nautilus.game.arcade.game.games.bedwars.shop.BedwarsItem;
import com.houzicore.arcade.nautilus.game.arcade.game.games.bedwars.shop.BedwarsNetherItem;
import com.houzicore.arcade.nautilus.game.arcade.game.games.bedwars.shop.BedwarsResource;
import com.houzicore.arcade.nautilus.game.arcade.game.games.bedwars.shop.BedwarsShopItem;
import com.houzicore.arcade.nautilus.game.arcade.game.games.bedwars.shop.BedwarsShopItemType;
import com.houzicore.arcade.nautilus.game.arcade.game.games.bedwars.shop.BedwarsShopModule;
import com.houzicore.arcade.nautilus.game.arcade.game.games.bedwars.shop.trap.BedwarsBearTrap;
import com.houzicore.arcade.nautilus.game.arcade.game.games.bedwars.shop.trap.BedwarsTNTTrap;
import com.houzicore.arcade.nautilus.game.arcade.game.games.bedwars.team.BedwarsTeam;
import com.houzicore.arcade.nautilus.game.arcade.game.games.bedwars.team.BedwarsTeamModule;
import com.houzicore.arcade.nautilus.game.arcade.game.games.bedwars.trackers.BreakAllBedsTracker;
import com.houzicore.arcade.nautilus.game.arcade.game.games.bedwars.trackers.BreakFirstMinuteTracker;
import com.houzicore.arcade.nautilus.game.arcade.game.games.bedwars.trackers.FirstBloodStatTracker;
import com.houzicore.arcade.nautilus.game.arcade.game.games.bedwars.trackers.FloorIsLavaTracker;
import com.houzicore.arcade.nautilus.game.arcade.game.games.bedwars.trackers.GetGoodStatTracker;
import com.houzicore.arcade.nautilus.game.arcade.game.games.bedwars.trackers.OwnAllBeaconsTracker;
import com.houzicore.arcade.nautilus.game.arcade.game.games.bedwars.trackers.Survive10Tracker;
import com.houzicore.arcade.nautilus.game.arcade.game.games.bedwars.trackers.WinWithBedIntactTracker;
import com.houzicore.arcade.nautilus.game.arcade.game.games.bedwars.trackers.WinWithinTimeTracker;
import com.houzicore.arcade.nautilus.game.arcade.game.games.bedwars.trackers.WinWithoutKillingTracker;
import com.houzicore.arcade.nautilus.game.arcade.game.games.bedwars.ui.BedwarsResourcePage;
import com.houzicore.arcade.nautilus.game.arcade.game.games.bedwars.ui.BedwarsResourceStarPage;
import com.houzicore.arcade.nautilus.game.arcade.game.modules.CompassModule;
import com.houzicore.arcade.nautilus.game.arcade.game.modules.capturepoint.CapturePointModule;
import com.houzicore.arcade.nautilus.game.arcade.game.modules.chest.ChestLootModule;
import com.houzicore.arcade.nautilus.game.arcade.game.modules.chest.ChestLootPool;
import com.houzicore.arcade.nautilus.game.arcade.kit.Kit;
import com.houzicore.arcade.nautilus.game.arcade.scoreboard.GameScoreboard;
import com.houzicore.arcade.nautilus.game.arcade.stats.WinWithoutDyingStatTracker;

public class Bedwars extends TeamGame
{

	private static final String[] DESCRIPTION =
			{
					C.cAqua + "Defend your Bed" + C.cWhite + " from the enemy teams.",
					C.cRed + "Destroy the enemy's Bed" + C.cWhite + "!",
					"Control the " + C.cAqua + "Beacons" + C.cWhite + " to get more resources.",
					"Last team standing wins!"
			};
	private static final String[] TIPS =
			{
					"Controlling the beacons is essential to victory.",
					"Watch out for other teams in case they try to rush your Bed!",
					"Controlling the center beacon will spawn Diamonds at your base, these can be used to buy team upgrades.",
					"Controlling the outer beacons will spawn Emeralds at your base, these can be used to buy strong weapons and armor.",
					"Players will respawn as long as their bed hasn't been broken.",
					"Deploy Platforms can be used to cross large gaps quickly. Faster but more expensive than wool blocks.",
					"Purchasing the Resource Generator upgrade in the Diamond shop increases the number of resources your generator creates.",
					"Balance attacking and defending.",
					"All players standing on the Resource Generator get the items generated.",
					"Don't want to see hologram and chat tips? Turn them off in /prefs.",
					"Watch out for Polly The Sheep, if you see her, kill her quick. Otherwise you might just lose your bed."
			};
	private static final int RESPAWN_TIME = 6;
	private static final double GAME_KNOCKBACK = 0.845;

	private final Map<GameTeam, Location> _averages;
	private final Cache<Long, Player> _deathsInLastMinute;

	private final BedwarsTeamModule _bedTeamModule;
	private final BedwarsPlayerModule _bedPlayerModule;
	private final BedwarsSpawnerModule _bedSpawnerModule;
	private final BedwarsShopModule _bedShopModule;
	protected final ChestLootModule _chestLootModule;
	private final CapturePointModule _capturePointModule;
	private final BedwarsPointModule _bedPointModule;

	private String _gameLengthString;
	private boolean _colourTick;
	private EnderDragon _enderDragon;
	private boolean _dragonSpawned;

	public Bedwars(ArcadeManager manager)
	{
		this(manager, GameType.Bedwars);
	}

	@SuppressWarnings("unchecked")
	public Bedwars(ArcadeManager manager, GameType gameType)
	{
		super(manager, gameType, new Kit[]
				{
						new KitBedwarsWarrior(manager),
						new KitBedwarsArcher(manager),
						new KitBedwarsBuilder(manager),
						new KitBedwarsFrosting(manager)
				}, DESCRIPTION);

		_averages = new HashMap<>(4);
		_deathsInLastMinute = CacheBuilder.newBuilder()
				.expireAfterWrite(60, TimeUnit.SECONDS)
				.build();

		AnnounceStay = false;
		BlockPlace = true;
		BlockBreak = true;
		DeathSpectateSecs = RESPAWN_TIME;
		DeathDropItems = true;
		StrictAntiHack = true;
		HungerSet = 20;
		InventoryClick = true;
		InventoryOpenChest = true;
		InventoryOpenBlock = true;
		ItemDrop = true;
		ItemPickup = true;
		GameTimeout = TimeUnit.HOURS.toMillis(1);
		WorldBoundaryKill = false;
		_help = TIPS;

		registerStatTrackers(
				new BreakAllBedsTracker(this),
				new BreakFirstMinuteTracker(this),
				new FirstBloodStatTracker(this),
				new FloorIsLavaTracker(this),
				new GetGoodStatTracker(this),
				new OwnAllBeaconsTracker(this),
				new Survive10Tracker(this),
				new WinWithinTimeTracker(this, "WinIn10", TimeUnit.MINUTES.toMillis(10)),
				new WinWithoutDyingStatTracker(this, "NoDeaths"),
				new WinWithBedIntactTracker(this),
				new WinWithoutKillingTracker(this, "NoKills")
		);

		// registerChatStats(
		// 		Kills,
		// 		Assists,
		// 		Deaths,
		// 		KDRatio,
		// 		BlankLine,
		// 		new ChatStatData("Bites", "Bed Bites", true),
		// 		new ChatStatData("EatWholeBed", "Whole Beds", true)
		// );

		// manager.GetDamage().setConstantKnockback(GAME_KNOCKBACK);
		manager.GetCreature().SetDisableCustomDrops(true);

		new CompassModule(this)
				.register();

		_bedTeamModule = new BedwarsTeamModule(this);
		_bedTeamModule.register();

		new BedwarsIslandModule(this)
				.register();

		_bedPlayerModule = new BedwarsPlayerModule(this);
		_bedPlayerModule.register();

		_bedSpawnerModule = new BedwarsSpawnerModule(this);
		_bedSpawnerModule.register();

		_bedShopModule = new BedwarsShopModule(this);
		_bedShopModule.register();

		_bedPointModule = new BedwarsPointModule(this);
		_bedPointModule.register();

		new BedwarsBatModule(this)
				.register();

		new BedwarsItemModule(this)
				.register();

		_capturePointModule = new CapturePointModule();
		_capturePointModule.register(this);

		_chestLootModule = new ChestLootModule();
	}

	@Override
	@org.bukkit.event.EventHandler
	public void ScoreboardUpdate(com.houzicore.shared.updater.event.UpdateEvent event)
	{
		if (event != null && event.getType() != com.houzicore.shared.updater.UpdateType.FAST)
			return;

		Scoreboard.Reset();

		switch (GetState())
		{
			case Prepare:
				Scoreboard.WriteBlank();
				Scoreboard.Write(C.cYellow + C.Bold + "Bed Wars");
				Scoreboard.WriteBlank();
				Scoreboard.Write(C.cWhite + "Game starting...");
				Scoreboard.WriteBlank();
				break;
			case Live:
				Scoreboard.WriteBlank();
				
				// ⏳ Countdown
				Scoreboard.Write("⏳ §eᴄᴏᴜɴᴛᴅᴏᴡɴ");
				
				long elapsed = System.currentTimeMillis() - GetStateTime();
				String countdownLine = "§r";
				if (elapsed < TimeUnit.MINUTES.toMillis(5))
				{
					long remaining = TimeUnit.MINUTES.toMillis(5) - elapsed;
					long secs = (remaining + 999) / 1000;
					countdownLine += String.format("§7 Diamond §bII §a%d:%02d", secs / 60, secs % 60);
				}
				else if (elapsed < TimeUnit.MINUTES.toMillis(10))
				{
					long remaining = TimeUnit.MINUTES.toMillis(10) - elapsed;
					long secs = (remaining + 999) / 1000;
					countdownLine += String.format("§7 Emerald §aIII §a%d:%02d", secs / 60, secs % 60);
				}
				else if (elapsed < TimeUnit.MINUTES.toMillis(15))
				{
					long remaining = TimeUnit.MINUTES.toMillis(15) - elapsed;
					long secs = (remaining + 999) / 1000;
					countdownLine += String.format("§7 Spawners §eIV §a%d:%02d", secs / 60, secs % 60);
				}
				else if (elapsed < TimeUnit.MINUTES.toMillis(20))
				{
					long remaining = TimeUnit.MINUTES.toMillis(20) - elapsed;
					long secs = (remaining + 999) / 1000;
					countdownLine += String.format("§7 Bed Rot §a%d:%02d", secs / 60, secs % 60);
				}
				else
				{
					countdownLine += "§7 Bed Rot §cRotted";
				}
				Scoreboard.Write(countdownLine);
				
				Scoreboard.WriteBlank();
				
				// Team Left
				Scoreboard.Write("§6ᴛᴇᴀᴍ ʟᴇғᴛ");
				for (GameTeam team : GetTeamList())
				{
					BedwarsTeam bedTeam = _bedTeamModule.getBedwarsTeam(team);
					boolean hasBed = bedTeam != null && bedTeam.canRespawn();
					String teamLine = "§r"; // Bypass automatic bullet formatting in GameScoreboard
					
					if (!team.IsTeamAlive())
					{
						teamLine += "§8§o§m■ Team " + team.GetName();
					}
					else
					{
						if (hasBed)
						{
							teamLine += team.GetColor() + "■ §fTeam " + team.GetName();
						}
						else
						{
							teamLine += team.GetColor() + "■ §fTeam " + team.GetName() + " §e" + team.GetPlayers(true).size();
						}
					}
					Scoreboard.Write(teamLine);
				}
				
				Scoreboard.WriteBlank();
				
				// Stats
				Scoreboard.Write("§esᴛᴀᴛs");
				Scoreboard.Write("§r§f\u2694 Kills §a%KILLS%");
				Scoreboard.Write("§r§f\u2620 Assit §c%ASSISTS%");
				Scoreboard.Write("§r§f\uD83D\uDECF Beds §e%BEDS%");
				
				Scoreboard.WriteBlank();
				break;
			case End:
				Scoreboard.WriteBlank();

				if (WinnerTeam == null)
				{
					Scoreboard.Write("No winner");
				}
				else
				{
					Scoreboard.Write((_colourTick ? C.cYellow : C.cGold) + "WINNER");
					Scoreboard.Write(WinnerTeam.GetColor() + C.Bold + WinnerTeam.GetName());
					Scoreboard.WriteBlank();
					Scoreboard.Write(C.cYellow + C.Bold + "Time");
					Scoreboard.Write(_gameLengthString);
				}

				Scoreboard.WriteBlank();
				break;
			default:
				break;
		}

		Scoreboard.Draw();
	}

	@Override
	public void ParseData()
	{
		for (GameTeam team : GetTeamList())
		{
			_averages.put(team, UtilAlg.getAverageLocation(team.GetSpawns()));
		}

		generateChests();
		_chestLootModule.register(this);

		// Backwards compatibility with old bed wars maps
		int i = 1;
		for (Location location : WorldData.GetDataLocs("SILVER"))
		{
			WorldData.GetAllCustomLocs().computeIfAbsent("POINT Outer-" + i++ + " GREEN", k -> new ArrayList<>()).add(location);
		}
		for (Location location : WorldData.GetDataLocs("WHITE"))
		{
			WorldData.GetAllCustomLocs().computeIfAbsent("POINT Center GOLD", k -> new ArrayList<>()).add(location);
		}

		// if (Manager.IsRewardStats() && Manager.GetLobby() instanceof NewGameLobbyManager)
		// {
		//    ... legacy leaderboard registration commented out
		// }
	}

	@EventHandler
	public void entityChangeBlock(EntityChangeBlockEvent event)
	{
		if (IsLive())
		{
			if (event.getEntity() instanceof EnderDragon)
			{
				Block block = event.getBlock();
				if (_bedPlayerModule.getPlacedBlocks().contains(block))
				{
					Material type = block.getType();
					_bedPlayerModule.getPlacedBlocks().remove(block);
					block.setType(Material.AIR);
					block.getWorld().playEffect(block.getLocation(), org.bukkit.Effect.STEP_SOUND, type);
				}
			}
			event.setCancelled(true);
		}
	}

	@EventHandler
	public void alternateColourTick(UpdateEvent event)
	{
		if (event.getType() != UpdateType.SEC)
		{
			return;
		}

		_colourTick = !_colourTick;
	}

	@EventHandler
	public void onStateChange(GameStateChangeEvent event)
	{
		if (event.GetGame() != this) return;

		if (event.GetState() == GameState.Live)
		{
			_dragonSpawned = false;
			_enderDragon = null;
		}
		else if (event.GetState() == GameState.End || event.GetState() == GameState.Dead)
		{
			if (_enderDragon != null && _enderDragon.isValid())
			{
				_enderDragon.remove();
			}
			_enderDragon = null;
		}
	}

	@EventHandler
	public void updateEndgame(UpdateEvent event)
	{
		if (event.getType() != UpdateType.SEC || !IsLive())
		{
			return;
		}

		long elapsed = System.currentTimeMillis() - GetStateTime();
		int aliveTeams = 0;
		for (GameTeam team : GetTeamList())
		{
			if (team.IsTeamAlive())
			{
				aliveTeams++;
			}
		}

		if ((elapsed >= TimeUnit.MINUTES.toMillis(20) || aliveTeams <= 2) && !_dragonSpawned)
		{
			spawnEnderDragon();
		}

		if (_dragonSpawned && _enderDragon != null && _enderDragon.isValid())
		{
			updateDragonTarget();
		}
	}

	private void spawnEnderDragon()
	{
		_dragonSpawned = true;
		
		// Calculate center location
		double sumX = 0, sumY = 0, sumZ = 0;
		int count = 0;
		org.bukkit.World world = null;
		for (Location loc : _averages.values())
		{
			if (loc != null)
			{
				sumX += loc.getX();
				sumY += loc.getY();
				sumZ += loc.getZ();
				world = loc.getWorld();
				count++;
			}
		}
		
		Location spawnLoc = null;
		if (count > 0 && world != null)
		{
			spawnLoc = new Location(world, sumX / count, (sumY / count) + 25, sumZ / count);
		}
		else
		{
			spawnLoc = WorldData.GetSpawn();
			if (spawnLoc != null)
			{
				spawnLoc = spawnLoc.clone().add(0, 25, 0);
			}
		}
		
		if (spawnLoc == null)
		{
			return;
		}
		
		_enderDragon = spawnLoc.getWorld().spawn(spawnLoc, EnderDragon.class);
		_enderDragon.setPhase(EnderDragon.Phase.STRAFING);
		_enderDragon.setCustomName("§c§lENDGAME DRAGON");
		_enderDragon.setCustomNameVisible(true);
		
		Announce(C.cRed + C.Bold + "THE ENDGAME DRAGON HAS SPAWNED!");
		for (Player player : UtilServer.getPlayers())
		{
			player.playSound(player.getLocation(), Sound.ENTITY_ENDER_DRAGON_GROWL, 1.0f, 1.0f);
		}
	}

	private void updateDragonTarget()
	{
		if (_enderDragon == null || !_enderDragon.isValid())
		{
			return;
		}

		Player closestPlayer = null;
		double closestDistSq = Double.MAX_VALUE;

		for (GameTeam team : GetTeamList())
		{
			for (Player player : team.GetPlayers(true))
			{
				if (player.isOnline() && !UtilPlayer.isSpectator(player))
				{
					double distSq = player.getLocation().distanceSquared(_enderDragon.getLocation());
					if (distSq < closestDistSq)
					{
						closestDistSq = distSq;
						closestPlayer = player;
					}
				}
			}
		}

		if (closestPlayer != null)
		{
			_enderDragon.setTarget(closestPlayer);
		}
	}

	@Override
	public void AnnounceEnd(GameTeam team)
	{
		_gameLengthString = UtilTime.MakeStr(System.currentTimeMillis() - getGameLiveTime());

		super.AnnounceEnd(team);
	}

	@Override
	public double GetKillsGems(Player killer, Player killed, boolean assist)
	{
		if (getDeathsInLastMinute(killed) > 1)
		{
			return 0;
		}

		return assist ? 1 : 3;
	}

	@EventHandler(priority = EventPriority.MONITOR)
	public void playerDeath(PlayerDeathEvent event)
	{
		if (!IsLive())
		{
			return;
		}

		Player player = event.getEntity();
		_deathsInLastMinute.put(System.currentTimeMillis(), player);
	}

	@EventHandler
	public void playerQuit(PlayerQuitEvent event)
	{
		_deathsInLastMinute.asMap().values().removeIf(player -> player.equals(event.getPlayer()));
	}

	public int getDeathsInLastMinute(Player player)
	{
		return (int) _deathsInLastMinute.asMap().values().stream()
				.filter(player::equals)
				.count();
	}


	@Override
	public String GetMode()
	{
		return "Standard";
	}

	public int getGeneratorRate(BedwarsResource resource, int current)
	{
		return current;
	}

	public List<BedwarsItem> generateItems(BedwarsResource resource)
	{
		switch (resource)
		{
			case BRICK:
				return Arrays.asList
						(
								// Iron Set
								new BedwarsShopItem(BedwarsShopItemType.HELMET, new ItemStack(Material.IRON_HELMET), 5),
								new BedwarsShopItem(BedwarsShopItemType.CHESTPLATE, new ItemStack(Material.IRON_CHESTPLATE), 8),
								new BedwarsShopItem(BedwarsShopItemType.LEGGINGS, new ItemStack(Material.IRON_LEGGINGS), 6),
								new BedwarsShopItem(BedwarsShopItemType.BOOTS, new ItemStack(Material.IRON_BOOTS), 5),

								// Sword
								new BedwarsShopItem(BedwarsShopItemType.SWORD, new ItemStack(Material.IRON_SWORD), 5),

								// Bow
								new BedwarsShopItem(BedwarsShopItemType.BOW, new ItemStack(Material.BOW), 12),

								// Pickaxe
								new BedwarsShopItem(BedwarsShopItemType.PICKAXE, new ItemStack(Material.IRON_PICKAXE), 8),

								// Axe
								new BedwarsShopItem(BedwarsShopItemType.AXE, new ItemStack(Material.IRON_AXE), 3),

								// Arrow
								new BedwarsShopItem(BedwarsShopItemType.OTHER, new ItemStack(Material.ARROW, 3), 12),

								// Blocks
								// Wool
								new BedwarsShopItem(BedwarsShopItemType.BLOCK, new ItemStack(Material.WHITE_WOOL, 16), 3),

								// Coloured Clay
								new BedwarsShopItem(BedwarsShopItemType.BLOCK, new ItemStack(Material.TERRACOTTA, 8), 8),

								// Wood
								new BedwarsShopItem(BedwarsShopItemType.BLOCK, new ItemStack(Material.OAK_PLANKS, 8), 8),

								// End Stone
								new BedwarsShopItem(BedwarsShopItemType.BLOCK, new ItemStack(Material.END_STONE, 8), 12),

								// Deploy Platform
								new BedwarsShopItem(BedwarsShopItemType.OTHER, BedwarsDeployPlatform.ITEM_STACK, 5),

								// Emerald
								new BedwarsShopItem(BedwarsShopItemType.OTHER, new ItemStack(Material.EMERALD), 20)
						);
			case EMERALD:
				return Arrays.asList
						(

								// Diamond Set
								new BedwarsShopItem(BedwarsShopItemType.HELMET, new ItemStack(Material.DIAMOND_HELMET), 10),
								new BedwarsShopItem(BedwarsShopItemType.CHESTPLATE, new ItemStack(Material.DIAMOND_CHESTPLATE), 24),
								new BedwarsShopItem(BedwarsShopItemType.LEGGINGS, new ItemStack(Material.DIAMOND_LEGGINGS), 16),
								new BedwarsShopItem(BedwarsShopItemType.BOOTS, new ItemStack(Material.DIAMOND_BOOTS), 10),

								// Sword
								new BedwarsShopItem(BedwarsShopItemType.SWORD, new ItemStack(Material.DIAMOND_SWORD), 5),

								// Pickaxe
								new BedwarsShopItem(BedwarsShopItemType.PICKAXE, new ItemStack(Material.DIAMOND_PICKAXE), 10),

								// Axe
								new BedwarsShopItem(BedwarsShopItemType.AXE, new ItemStack(Material.DIAMOND_AXE), 4),

								// Obsidian
								new BedwarsShopItem(BedwarsShopItemType.BLOCK, new ItemStack(Material.OBSIDIAN), 8),

								// Shears
								new BedwarsShopItem(BedwarsShopItemType.SHEARS, new ItemStack(Material.SHEARS), 5),

								// Golden Apple
								new BedwarsShopItem(BedwarsShopItemType.OTHER, new ItemStack(Material.GOLDEN_APPLE), 8),

								// Ender pearl
								new BedwarsShopItem(BedwarsShopItemType.OTHER, BedwarsShopModule.ENDER_PEARL, 7),

								// Rune of Holding
								new BedwarsShopItem(BedwarsShopItemType.OTHER, BedwarsPlayerModule.RUNE_OF_HOLDING, 20),

								// Insta-Wall
								new BedwarsShopItem(BedwarsShopItemType.OTHER, BedwarsWall.ITEM_STACK, 2),

								new BedwarsShopItem(BedwarsShopItemType.OTHER, BedwarsSheep.ITEM_STACK, 8),

								// Traps
								new BedwarsTNTTrap(8),
								new BedwarsBearTrap(8)
						);
			case STAR:
				return Arrays.asList(BedwarsNetherItem.values());
			default:
				return Collections.emptyList();
		}
	}

	public List<BedwarsSpecialItem> generateSpecialItems()
	{
		List<BedwarsSpecialItem> items = new ArrayList<>();
		items.add(new BedwarsDeployPlatform(this));
		items.add(new BedwarsWall(this));
		items.add(new BedwarsSheep(this));
		return items;
	}

	public void generateChests()
	{
		_chestLootModule.registerChestType(BedwarsIslandModule.CHEST_TYPE, new ArrayList<>(),

				new ChestLootPool()
						.addItem(new ItemBuilder(Material.DIAMOND_SWORD)
								.addEnchantment(Enchantment.KNOCKBACK, 1)
								.setUnbreakable(true)
								.build())
						.addItem(new ItemBuilder(Material.BOW)
								.addEnchantment(Enchantment.PUNCH, 1)
								.setUnbreakable(true)
								.build())
						.addItem(new ItemBuilder(Material.BOW, (short) (Material.BOW.getMaxDurability() - 9))
								.addEnchantment(Enchantment.POWER, 2)
								.addEnchantment(Enchantment.INFINITY, 1)
								.build())
						.addItem(new ItemBuilder(Material.BOW, (short) (Material.BOW.getMaxDurability() - 9))
								.addEnchantment(Enchantment.POWER, 4)
								.build())
						.addItem(new ItemBuilder(Material.GOLDEN_PICKAXE)
								.setTitle(C.cGold + C.Bold + "The Golden Pickaxe")
								.setUnbreakable(true)
								.build())
						.addItem(new ItemBuilder(Material.DIAMOND_CHESTPLATE)
								.setUnbreakable(true)
								.build())
						.addItem(new ItemStack(Material.DIAMOND), 3, 4)
						.addItem(BedwarsShopModule.ENDER_PEARL, 1, 2)
						.addItem(new ItemStack(Material.GOLDEN_APPLE), 2, 3)
						.addItem(getSpeedPotion()),

				new ChestLootPool()
						.addItem(new ItemStack(Material.EMERALD), 4, 8),

				new ChestLootPool()
						.addItem(new ItemStack(Material.IRON_INGOT), 8, 16)

		).destroyAfterOpened(30);
	}

	public BedwarsResourcePage getShopPage(BedwarsResource resource, Player player)
	{
		switch (resource)
		{
			case BRICK:
			case EMERALD:
				return new BedwarsResourcePage(getArcadeManager(), getBedwarsShopModule().getShop(), player, resource, getBedwarsShopModule().getItems().get(resource));
			case STAR:
				return new BedwarsResourceStarPage(getArcadeManager(), getBedwarsShopModule().getShop(), player, Arrays.asList(BedwarsNetherItem.values()));
			default:
				return null;
		}
	}

	public Location getAverageLocation(GameTeam team)
	{
		return _averages.get(team);
	}

	public boolean isNearSpawn(Block block)
	{
		return isNearSpawn(block.getLocation().add(0.5, 0, 0.5));
	}

	public boolean isNearSpawn(Location location)
	{
		for (List<Location> locations : WorldData.SpawnLocs.values())
		{
			for (Location spawn : locations)
			{
				if (UtilMath.offsetSquared(location, spawn) < 9)
				{
					return true;
				}
			}
		}

		return false;
	}

	public ChestLootModule getChestLootModule()
	{
		return _chestLootModule;
	}

	public CapturePointModule getCapturePointModule()
	{
		return _capturePointModule;
	}

	public BedwarsPointModule getBedwarsPointModule()
	{
		return _bedPointModule;
	}

	public BedwarsTeamModule getBedwarsTeamModule()
	{
		return _bedTeamModule;
	}

	public BedwarsPlayerModule getBedwarsPlayerModule()
	{
		return _bedPlayerModule;
	}

	public BedwarsShopModule getBedwarsShopModule()
	{
		return _bedShopModule;
	}

	public BedwarsSpawnerModule getBedwarsSpawnerModule()
	{
		return _bedSpawnerModule;
	}

	private ItemStack getSpeedPotion()
	{
		ItemStack potion = new ItemStack(Material.POTION);
		org.bukkit.inventory.meta.PotionMeta meta = (org.bukkit.inventory.meta.PotionMeta) potion.getItemMeta();
		if (meta != null)
		{
			meta.setDisplayName(C.cAqua + "Speed Potion");
			meta.addCustomEffect(new PotionEffect(PotionEffectType.SPEED, 30 * 20, 0), true);
			potion.setItemMeta(meta);
		}
		return potion;
	}
}
