package com.houzicore.arcade.nautilus.game.arcade.game.games.primalgames;

import java.lang.reflect.Field;
import java.util.*;
import java.util.Map.Entry;

import org.bukkit.*;
import org.bukkit.FireworkEffect.Type;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.BlockState;
import org.bukkit.block.Chest;
import org.bukkit.block.DoubleChest;
import org.bukkit.block.Furnace;
import org.bukkit.entity.Boat;
import org.bukkit.entity.Egg;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Fireball;
import org.bukkit.entity.Firework;
import org.bukkit.entity.ItemFrame;
import org.bukkit.entity.Player;
import org.bukkit.entity.Snowball;
import org.bukkit.entity.TNTPrimed;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockBurnEvent;
import org.bukkit.event.block.BlockFadeEvent;
import org.bukkit.event.block.BlockSpreadEvent;
import org.bukkit.event.block.LeavesDecayEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.entity.EntityShootBowEvent;
import org.bukkit.event.entity.ExplosionPrimeEvent;
import org.bukkit.event.entity.ItemSpawnEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.hanging.HangingBreakEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.PrepareItemCraftEvent;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.vehicle.VehicleCreateEvent;
import org.bukkit.inventory.FurnaceInventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.FireworkMeta;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;
//import org.bukkit.scoreboard.TeamNameTagVisibility;
import org.bukkit.util.Vector;

import com.houzicore.shared.common.util.C;
import com.houzicore.shared.common.util.F;
import com.houzicore.shared.common.util.NautHashMap;
import com.houzicore.shared.common.util.UtilAction;
import com.houzicore.shared.common.util.UtilAlg;
import com.houzicore.shared.common.util.UtilBlock;
import com.houzicore.shared.common.util.UtilEvent;
import com.houzicore.shared.common.util.UtilEvent.ActionType;
import com.houzicore.shared.common.util.UtilFirework;
import com.houzicore.shared.common.util.UtilInv;
import com.houzicore.shared.common.util.UtilMath;
import com.houzicore.shared.common.util.UtilParticle;
import com.houzicore.shared.common.util.UtilPlayer;
import com.houzicore.shared.common.util.UtilTextBottom;
import com.houzicore.shared.common.util.UtilParticle.ParticleType;
import com.houzicore.shared.common.util.UtilParticle.ViewDist;
import com.houzicore.shared.common.util.UtilTime.TimeUnit;
import com.houzicore.shared.common.util.UtilServer;
import com.houzicore.shared.common.util.UtilTime;
import com.houzicore.shared.common.util.UtilWorld;
import com.houzicore.shared.common.actionbar.ActionBarChannel;
import com.houzicore.shared.core.disguise.disguises.DisguisePlayer;
import com.houzicore.shared.core.itemstack.ItemBuilder;
import com.houzicore.shared.core.itemstack.ItemStackFactory;
import com.houzicore.shared.core.loot.*;
import com.houzicore.shared.recharge.Recharge;
import com.houzicore.shared.updater.UpdateType;
import com.houzicore.shared.updater.event.UpdateEvent;
import com.houzicore.shared.core.visibility.VisibilityManager;
import com.houzicore.shared.core.combat.CombatComponent;
import com.houzicore.shared.core.combat.event.CombatDeathEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import com.houzicore.arcade.nautilus.game.arcade.game.games.primalgames.kit.*;
import com.houzicore.arcade.nautilus.game.arcade.game.games.primalgames.lang.PrimalGamesLang;
import com.houzicore.arcade.nautilus.game.arcade.game.games.primalgames.events.*;
import com.houzicore.arcade.nautilus.game.arcade.game.games.primalgames.events.disasters.*;
import org.bukkit.entity.Player;
import com.houzicore.arcade.ArcadeFormat;
import com.houzicore.arcade.ArcadeManager;
import com.houzicore.arcade.GameType;
import com.houzicore.arcade.nautilus.game.arcade.events.GameStateChangeEvent;
import com.houzicore.arcade.nautilus.game.arcade.game.Game;
import com.houzicore.arcade.nautilus.game.arcade.game.GameTeam;
import com.houzicore.arcade.nautilus.game.arcade.game.SoloGame;
import com.houzicore.arcade.nautilus.game.arcade.kit.Kit;
import com.houzicore.arcade.nautilus.game.arcade.stats.FirstSupplyDropOpenStatTracker;
import com.houzicore.arcade.nautilus.game.arcade.stats.KillsWithinTimeLimitStatTracker;
import com.houzicore.arcade.nautilus.game.arcade.stats.SimultaneousSkeletonStatTracker;
import com.houzicore.arcade.nautilus.game.arcade.stats.WinWithoutWearingArmorStatTracker;
//import com.mojang.authlib.GameProfile;
//import com.mojang.authlib.properties.Property;
import org.bukkit.entity.Arrow;


public abstract class PrimalGames extends Game
{
	// SupplyDrop visual state (stays in PrimalGames — AirdropManager owns the drop)
	private ArrayList<Location> _supplyLocations = new ArrayList<Location>();
	private Location _supplyCurrent = null;
	private Location _supplyEffect = null;

	// Loot management delegated to LootTableManager
	private LootTableManager _lootTableManager;

	// Player state
	private HashMap<Player, HashSet<String>> _hiddenNames = new HashMap<Player, HashSet<String>>();

	// Misc
	private HashMap<Entity, Player> _tntMap = new HashMap<Entity, Player>();
	private HashSet<Location> _placedBlocks = new HashSet<Location>();
	private Location _spawn;

	// Border
	private int _secondsSinceStart;
	private HashMap<Integer, Double> _borderPositions = new HashMap<Integer, Double>();
	private double _currentBorder;
	private double _previousBorder;
	private long _borderStartedMoving;

	// Deathmatch
	private boolean _deathMatchTeleported = false;
	private int _deathMatchTime = 10 * 60;
	private int _gameEndTime = 3 * 60;

	private int _deadBodyCount;

	private RuneManager _runeManager;
	private AirdropManager _airdropManager;
	
	private org.bukkit.boss.BossBar _bossBar;
	private ToxicCaveEvent _toxicCaveEvent;
	private WanderingTraderManager _traderManager;
	private DisasterManager _disasterManager;

	// First blood tracking
	private boolean _firstBloodDealt = false;

	public AirdropManager getAirdropManager() { return _airdropManager; }
	public DisasterManager getDisasterManager() { return _disasterManager; }
	public double getCurrentBorder() { return WorldData != null && WorldData.World != null ? WorldData.World.getWorldBorder().getSize() : 400; }
	public int getSecondsSinceStart() { return _secondsSinceStart; }

	private boolean _borderPhase2 = false;
	private boolean _borderPhase3 = false;



	public PrimalGames(ArcadeManager manager, GameType type, String[] description)
	{
		super(manager, type,

				new Kit[]
					{
							new KitMiner(manager),
							new KitLumberjack(manager),
							new KitAssassin(manager),
							new KitAxeman(manager),
							new KitArcher(manager),
							new KitKnight(manager),
							new KitBrawler(manager),
							new KitHorseman(manager),
							new KitBomber(manager),
							new KitBeastmaster(manager),
							new KitLooter(manager),
							new KitBarbarian(manager),
							new KitNecromancer(manager),
					}, description);

		_help = new String[]
				{
				C.cGreen + "Track survivors with your " + C.cWhite + "Compass" + C.cGreen + " and collapse the map around them.",
				C.cGreen + "Loot fast, craft smart, and pivot when " + C.cPurple + "Disasters" + C.cGreen + " hit.",
				C.cAqua + "Play for tempo, not just gear. The border and Deathmatch will force every fight."
				};

		// Manager.GetAntiStack().SetEnabled(false);

		StrictAntiHack = true;
		
		HideTeamSheep = true;
		
		this.ReplaceTeamsWithKits = true;

		GameTimeout = 1500000;

		QuitDropItems = true;

		WorldTimeSet = 0;
		WorldBoundaryKill = false;
	
		DamageSelf = true;
		DamageTeamSelf = true;

		DeathDropItems = true;

		ItemDrop = true;
		ItemPickup = true;

		InventoryClick = true;
		InventoryOpenBlock = true;
		InventoryOpenChest = true;

		PlaySoundGameStart = false;
		PrepareTime = 15000;
		
		VersionRequire1_8 = true;

		BlockBreakAllow.add(Material.COBWEB.ordinal()); // Web
		BlockPlace = true; // NEW: Allow all block placements freely


		BlockBreakAllow.add(Material.OAK_LEAVES.ordinal()); // Leaves
		BlockBreakAllow.add(Material.ACACIA_LEAVES.ordinal()); // Leaves

		BlockPlaceAllow.add(Material.CAKE.ordinal());
		BlockBreakAllow.add(Material.CAKE.ordinal());

		BlockBreakAllow.add(Material.SHORT_GRASS.ordinal());
		BlockBreakAllow.add(Material.POPPY.ordinal());
		BlockBreakAllow.add(Material.DANDELION.ordinal());
		BlockBreakAllow.add(Material.BROWN_MUSHROOM.ordinal());
		BlockBreakAllow.add(Material.RED_MUSHROOM.ordinal());
		BlockBreakAllow.add(Material.DEAD_BUSH.ordinal());
		BlockBreakAllow.add(Material.CARROT.ordinal());
		BlockBreakAllow.add(Material.POTATO.ordinal());
		BlockBreakAllow.add(Material.SUNFLOWER.ordinal());
		BlockBreakAllow.add(Material.WHEAT.ordinal());
		BlockBreakAllow.add(Material.OAK_SAPLING.ordinal());
		BlockBreakAllow.add(Material.VINE.ordinal());
		BlockBreakAllow.add(Material.LILY_PAD.ordinal());

		// Manager.GetStatsManager().addTable(GetName(), "kills", "deaths", "chestsOpened");


		registerStatTrackers(new WinWithoutWearingArmorStatTracker(this), new KillsWithinTimeLimitStatTracker(this, 3, 60,
				"Bloodlust"), new FirstSupplyDropOpenStatTracker(this), new SimultaneousSkeletonStatTracker(this, 5),
				new com.houzicore.arcade.nautilus.game.arcade.stats.ChestOpenStatTracker(this),
				new com.houzicore.arcade.nautilus.game.arcade.stats.WinWithoutKillsStatTracker(this),
				new com.houzicore.arcade.nautilus.game.arcade.stats.BackstabKillStatTracker(this),
				new com.houzicore.arcade.nautilus.game.arcade.stats.WinWithoutTakingPlayerDamageStatTracker(this),
				new com.houzicore.arcade.nautilus.game.arcade.stats.TheLongestShotStatTracker(this));

		_runeManager = new RuneManager(this);
		org.bukkit.Bukkit.getPluginManager().registerEvents(_runeManager, manager.getPlugin());

		_airdropManager = new AirdropManager(this, _runeManager);
		org.bukkit.Bukkit.getPluginManager().registerEvents(_airdropManager, manager.getPlugin());

		_toxicCaveEvent = new ToxicCaveEvent(this);
		org.bukkit.Bukkit.getPluginManager().registerEvents(_toxicCaveEvent, manager.getPlugin());

		_traderManager = new WanderingTraderManager(this, _runeManager);
		org.bukkit.Bukkit.getPluginManager().registerEvents(_traderManager, manager.getPlugin());

		_lootTableManager = new LootTableManager(this);
		org.bukkit.Bukkit.getPluginManager().registerEvents(_lootTableManager, manager.getPlugin());

		_disasterManager = new DisasterManager(this);
		org.bukkit.Bukkit.getPluginManager().registerEvents(_disasterManager, manager.getPlugin());

		// registerCustomRecipes() moved to GameState.Live to prevent deletion during Prepare
	}

	// ── Public accessors for LootTableManager ──────────────────────────────────

	public ItemStack buildBandageItem(Player viewer)
	{
		PrimalGamesLang lang = PrimalGamesLang.get();
		ItemStack bandage = new ItemBuilder(Material.PAPER)
				.setTitle(lang.get(viewer, "primal_games.item.bandage_name"))
				.addLore(lang.get(viewer, "primal_games.item.bandage_lore_use"))
				.addLore(lang.get(viewer, "primal_games.item.bandage_lore_effect"))
				.build();

		org.bukkit.inventory.meta.ItemMeta meta = bandage.getItemMeta();
		if (meta != null)
		{
			meta.getPersistentDataContainer().set(getBandageRecipeKey(), org.bukkit.persistence.PersistentDataType.BYTE, (byte) 1);
			bandage.setItemMeta(meta);
		}

		return bandage;
	}

	public boolean isBandage(ItemStack item)
	{
		if (item == null || item.getType() != Material.PAPER || !item.hasItemMeta())
			return false;

		org.bukkit.inventory.meta.ItemMeta meta = item.getItemMeta();
		return meta != null && meta.getPersistentDataContainer().has(getBandageRecipeKey(), org.bukkit.persistence.PersistentDataType.BYTE);
	}

	private NamespacedKey getBandageRecipeKey()
	{
		return new NamespacedKey(Manager.getPlugin(), "primal_bandage");
	}

	private void registerCustomRecipes()
	{
		ItemStack bandage = buildBandageItem(null);
		
		NamespacedKey bandageKey = getBandageRecipeKey();
		NamespacedKey gappleKey = new NamespacedKey(Manager.getPlugin(), "primal_gapple");
		
		// Remove existing to avoid duplicate ID issues
		org.bukkit.Bukkit.getServer().removeRecipe(bandageKey);
		org.bukkit.Bukkit.getServer().removeRecipe(gappleKey);

		org.bukkit.inventory.ShapedRecipe bandageRecipe = new org.bukkit.inventory.ShapedRecipe(bandageKey, bandage);
		bandageRecipe.shape("PWP");
		bandageRecipe.setIngredient('P', Material.PAPER);
		bandageRecipe.setIngredient('W', Material.WHITE_WOOL);

		ItemStack gapple = new ItemStack(Material.GOLDEN_APPLE);
		org.bukkit.inventory.ShapelessRecipe gappleRecipe = new org.bukkit.inventory.ShapelessRecipe(gappleKey, gapple);
		gappleRecipe.addIngredient(Material.DIAMOND);
		gappleRecipe.addIngredient(Material.APPLE);

		try {
			org.bukkit.Bukkit.getServer().addRecipe(bandageRecipe);
			org.bukkit.Bukkit.getServer().addRecipe(gappleRecipe);
		} catch (Exception e) {
			// Ignore if somehow still duplicate or locked
		}
	}

	@EventHandler
	public void onBandageConsume(org.bukkit.event.player.PlayerInteractEvent event)
	{
		if (!IsLive() || !IsAlive(event.getPlayer())) return;
		if (event.getAction() != org.bukkit.event.block.Action.RIGHT_CLICK_AIR && event.getAction() != org.bukkit.event.block.Action.RIGHT_CLICK_BLOCK) return;

		Player player = event.getPlayer();
		ItemStack item = player.getInventory().getItemInMainHand();

		if (isBandage(item))
		{
			event.setCancelled(true);

			if (!com.houzicore.shared.recharge.Recharge.Instance.use(player, "PrimalBandage", 500, false, false)) return;

			if (item.getAmount() > 1) {
				item.setAmount(item.getAmount() - 1);
				player.getInventory().setItemInMainHand(item);
			} else {
				player.getInventory().setItemInMainHand(null);
			}

			player.addPotionEffect(new org.bukkit.potion.PotionEffect(org.bukkit.potion.PotionEffectType.REGENERATION, 100, 1));
			player.sendMessage(PrimalGamesLang.get().get(player, "primal_games.item.bandage_used"));
			player.playSound(player.getLocation(), org.bukkit.Sound.ENTITY_PLAYER_BURP, 1f, 1f);
		}
	}

	@EventHandler
	public void setupBorder(GameStateChangeEvent event)
	{
		if (event.GetGame() != this) return;
		
		if (event.GetState() == GameState.Prepare)
		{
			org.bukkit.Bukkit.getServer().removeRecipe(new NamespacedKey(Manager.getPlugin(), "primal_bandage"));
			org.bukkit.Bukkit.getServer().removeRecipe(new NamespacedKey(Manager.getPlugin(), "primal_gapple"));
			
			// Warp Sound FX
			for (Player p : GetPlayers(true))
			{
				if (com.houzicore.shared.core.combat.legacy.LegacyCombatManager.get() != null) {
					com.houzicore.shared.core.combat.legacy.LegacyCombatManager.get().enableFor(p);
				}
				p.playSound(p.getLocation(), Sound.ENTITY_ENDERMAN_TELEPORT, 1f, 1f);
				p.playSound(p.getLocation(), Sound.BLOCK_END_PORTAL_SPAWN, 0.3f, 0.8f);
				UtilParticle.PlayParticle(ParticleType.PORTAL, p.getLocation().add(0, 1, 0), 0.5f, 1.0f, 0.5f, 0.1f, 20, ViewDist.NORMAL, UtilServer.getPlayers());
				UtilParticle.PlayParticle(ParticleType.FIREWORKS_SPARK, p.getLocation().add(0, 1, 0), 0.5f, 1.0f, 0.5f, 0.1f, 20, ViewDist.NORMAL, UtilServer.getPlayers());
				p.sendTitle(
					PrimalGamesLang.get().get(p, "primal_games.title.prepare_go"),
					PrimalGamesLang.get().get(p, "primal_games.title.prepare_subtitle"),
					10, 40, 20);
			}
		}
		
		if (event.GetState() != GameState.Live) return;

		registerCustomRecipes();

		org.bukkit.World world = WorldData.World;
		org.bukkit.WorldBorder border = world.getWorldBorder();
		
		Location center = GetSpectatorLocation() != null ? GetSpectatorLocation() : world.getSpawnLocation();
		border.setCenter(center);
		border.setSize(400);
		border.setDamageAmount(0.0); // No gas damage from border — border = vanilla push only
		border.setDamageBuffer(0);
		
		if (_bossBar == null)
		{
			PrimalGamesLang lang = PrimalGamesLang.get();
			String initialTitle = lang.bossBar(null, "border_safe", "400", UtilTime.MakeStr(600000));
			_bossBar = org.bukkit.Bukkit.createBossBar(initialTitle, org.bukkit.boss.BarColor.GREEN, org.bukkit.boss.BarStyle.SOLID);
		}
		_bossBar.setProgress(1.0);

		PrimalGamesLang lang = PrimalGamesLang.get();
		for (Player p : GetPlayers(true))
		{
			_bossBar.addPlayer(p);
			p.sendTitle(
				lang.get(p, "primal_games.title.game_start"),
				lang.get(p, "primal_games.title.game_start_subtitle"),
				10, 70, 20);
			p.playSound(p.getLocation(), Sound.ENTITY_ENDER_DRAGON_GROWL, 1f, 1f);
			p.sendMessage("");
			p.sendMessage(ArcadeFormat.Line);
			p.sendMessage(lang.announceHeader() + lang.get(p, "primal_games.announce.game_started"));
			p.sendMessage("");
			p.sendMessage(lang.get(p, "primal_games.announce.tip_explore"));
			p.sendMessage(lang.get(p, "primal_games.announce.tip_runes"));
			p.sendMessage(lang.get(p, "primal_games.announce.tip_gas"));
			p.sendMessage(ArcadeFormat.Line);
		}
	}

	@EventHandler
	public void borderUpdate(UpdateEvent event)
	{
		if (!IsLive() || event.getType() != UpdateType.SEC) return;

		long elapsed = System.currentTimeMillis() - GetStateTime();
		org.bukkit.WorldBorder border = WorldData.World.getWorldBorder();
		double size = border.getSize();
		int survivors = GetPlayers(true).size();
		PrimalGamesLang lang = PrimalGamesLang.get();

		// ── BossBar: check disaster override first ──
		String bossBarTitle;
		org.bukkit.boss.BarColor bossBarColor;
		double progress;

		Disaster activeDisaster = _disasterManager != null ? _disasterManager.getActiveDisaster() : null;

		if (activeDisaster != null)
		{
			// Disaster overrides bossbar
			if (activeDisaster instanceof ToxicGasDisaster)
				bossBarTitle = lang.bossBar(null, "disaster_toxic_gas", String.format("%.0f", size), null);
			else
				bossBarTitle = lang.bossBar(null, "disaster_generic", activeDisaster.getIcon(), null)
					.replace("{icon}", activeDisaster.getIcon()).replace("{name}", activeDisaster.getNameEn());
			bossBarColor = org.bukkit.boss.BarColor.PURPLE;
			long remaining = activeDisaster.getTimeRemaining();
			progress = Math.max(0.0, Math.min(1.0, remaining / (double) activeDisaster.getDuration()));
		}
		else if (elapsed < 600000) // Phase 1: Border static
		{
			bossBarTitle = lang.bossBar(null, "border_phase1", String.format("%.0f", size), UtilTime.MakeStr(600000 - elapsed));
			bossBarColor = org.bukkit.boss.BarColor.GREEN;
			progress = 1.0 - (elapsed / 600000.0);
		}
		else if (elapsed < 1200000) // Phase 2: Border closing
		{
			bossBarTitle = lang.bossBar(null, "border_phase2", String.format("%.0f", size), UtilTime.MakeStr(1200000 - elapsed));
			bossBarColor = org.bukkit.boss.BarColor.YELLOW;
			progress = size / 400.0;
		}
		else // Phase 3: Deathmatch
		{
			bossBarTitle = lang.bossBar(null, "deathmatch", String.format("%.0f", size), null);
			bossBarColor = org.bukkit.boss.BarColor.RED;
			progress = Math.max(0.0, size / 100.0);
		}

		// ── Phase transitions ──
		if (!_borderPhase2 && elapsed >= 600000)
		{
			_borderPhase2 = true;
			border.setSize(100, 600);
			Announce(lang.get(null, "primal_games.announce.border_phase2"));
			for (Player p : GetPlayers(true))
				p.playSound(p.getLocation(), org.bukkit.Sound.ENTITY_WITHER_SPAWN, 1f, 1f);
		}

		if (!_borderPhase3 && elapsed >= 1200000)
		{
			_borderPhase3 = true;
			border.setSize(30, 120);
			Announce(lang.get(null, "primal_games.announce.border_phase3"));
			for (Player p : GetPlayers(true))
			{
				p.sendTitle(
					lang.get(p, "primal_games.title.deathmatch"),
					lang.get(p, "primal_games.title.deathmatch_subtitle"),
					10, 70, 20);
				p.playSound(p.getLocation(), org.bukkit.Sound.ENTITY_ENDER_DRAGON_GROWL, 1f, 1f);
			}
		}

		if (_bossBar != null)
		{
			_bossBar.setTitle(bossBarTitle);
			_bossBar.setColor(bossBarColor);
			_bossBar.setProgress(Math.max(0.0, Math.min(1.0, progress)));
			for (Player p : GetPlayers(true))
			{
				if (!_bossBar.getPlayers().contains(p))
					_bossBar.addPlayer(p);

				// ActionBar: border-aware (no gas language)
				int kills = getKills(p);
				boolean nearBorder = UtilMath.offset2d(p.getLocation(), border.getCenter()) > (size / 2.0) - 8;
				String actionBar;

				if (elapsed >= 1200000)
					actionBar = lang.actionBar(p, "deathmatch", kills, survivors, null);
				else if (nearBorder)
					actionBar = lang.actionBar(p, "border_warn", kills, survivors, String.format("%.0f", size));
				else if (elapsed >= 600000)
					actionBar = lang.actionBar(p, "border_closing_info", kills, survivors, String.format("%.0f", size));
				else
					actionBar = lang.actionBar(p, "normal", kills, survivors, String.format("%.0f", size));

				UtilTextBottom.display(ActionBarChannel.GAME_STATUS, actionBar, p);
			}
		}
	}

	@EventHandler
	public void onGameEndBossBar(GameStateChangeEvent event)
	{
		if (event.GetGame() != this) return;
		if (event.GetState() == GameState.Dead && _bossBar != null)
		{
			_bossBar.removeAll();
		}
	}

	@EventHandler
	public void borderDamageAndParticles(UpdateEvent event)
	{
		if (!IsLive() || (event.getType() != UpdateType.FAST && event.getType() != UpdateType.SEC)) return;
		
		org.bukkit.WorldBorder border = WorldData.World.getWorldBorder();
		double size = border.getSize();
		double radius = size / 2.0;
		Location center = border.getCenter();
		
		double minX = center.getX() - radius;
		double maxX = center.getX() + radius;
		double minZ = center.getZ() - radius;
		double maxZ = center.getZ() + radius;
		
		for (Player p : GetPlayers(true))
		{
			Location loc = p.getLocation();
			double px = loc.getX();
			double pz = loc.getZ();
			
			boolean outside = (px < minX || px > maxX || pz < minZ || pz > maxZ);
			
			if (outside)
			{
				if (event.getType() == UpdateType.SEC) 
				{
					// Route damage through CombatManager per HouziCore API rules
					Manager.GetDamage().NewDamageEvent(p, null, null,
							DamageCause.CUSTOM, 2.0, false, true, true,
							GetName(), "Toxic Gas");
					p.addPotionEffect(new org.bukkit.potion.PotionEffect(org.bukkit.potion.PotionEffectType.BLINDNESS, 60, 0, false, false, false));
				}
			}
			else if (UtilMath.offset2d(loc, center) > radius - 5) // Warning Zone
			{
				if (event.getType() == UpdateType.SEC) 
				{
					p.playSound(loc, Sound.ENTITY_PLAYER_HURT, 0.5f, 0.5f);
					// Removed ActionBar send; it is now handled cleanly in borderUpdate()
				}
				if (event.getType() == UpdateType.FAST && Math.random() < 0.3)
				{
					UtilParticle.PlayParticle(ParticleType.RED_DUST, loc.clone().add(0, 0.2, 0), 2.0f, 0.1f, 2.0f, 0.1f, 5, ViewDist.NORMAL, UtilServer.getPlayers());
				}
			}

			// Particle Gas Wall Effect (UpdateType.FAST)
			if (event.getType() == UpdateType.FAST)
			{
				if (UtilMath.offset2d(loc, center) > radius - 20)
				{
					int playerY = loc.getBlockY();
					// Create a thick green/purple toxic gas wall
					for (int y = playerY - 3; y < playerY + 8; y++)
					{
						// Use UtilParticle instead of raw spawnParticle
						if (Math.abs(px - minX) < 18)
							UtilParticle.PlayParticle(ParticleType.RED_DUST, new Location(WorldData.World, minX, y, pz + (Math.random() * 8 - 4)), 0f, 0.5f, 0f, 0.1f, 3, ViewDist.NORMAL, p);
						if (Math.abs(px - maxX) < 18)
							UtilParticle.PlayParticle(ParticleType.RED_DUST, new Location(WorldData.World, maxX, y, pz + (Math.random() * 8 - 4)), 0f, 0.5f, 0f, 0.1f, 3, ViewDist.NORMAL, p);
						if (Math.abs(pz - minZ) < 18)
							UtilParticle.PlayParticle(ParticleType.RED_DUST, new Location(WorldData.World, px + (Math.random() * 8 - 4), y, minZ), 0.5f, 0f, 0f, 0.1f, 3, ViewDist.NORMAL, p);
						if (Math.abs(pz - maxZ) < 18)
							UtilParticle.PlayParticle(ParticleType.RED_DUST, new Location(WorldData.World, px + (Math.random() * 8 - 4), y, maxZ), 0.5f, 0f, 0f, 0.1f, 3, ViewDist.NORMAL, p);
					}
				}
			}
		}
	}

	private NautHashMap<Player, Integer> _killStreaks = new NautHashMap<Player, Integer>();

	public int getKills(Player player)
	{
		return _killStreaks.containsKey(player) ? _killStreaks.get(player) : 0;
	}

	@EventHandler(priority = org.bukkit.event.EventPriority.HIGHEST)
	public void onPlayerDeathPrimal(org.bukkit.event.entity.PlayerDeathEvent event)
	{
		if (!IsLive() || event.getEntity() == null) return;
		Player player = event.getEntity();
		if (GetTeam(player) == null) return; 

		Player killer = player.getKiller();
		if (killer != null && !killer.equals(player))
		{
			int streak = (_killStreaks.containsKey(killer) ? _killStreaks.get(killer) : 0) + 1;
			_killStreaks.put(killer, streak);
			
			if (streak == 1)
			{
				killer.playSound(killer.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1f, 1.2f);
				UtilParticle.PlayParticle(ParticleType.CRIT, killer.getLocation().add(0, 1, 0), 0.5f, 1.0f, 0.5f, 0.1f, 15, ViewDist.NORMAL, UtilServer.getPlayers());
				
				// First Blood?
				if (_killStreaks.size() == 1) // First kill of the game
				{
					killer.playSound(killer.getLocation(), Sound.ENTITY_LIGHTNING_BOLT_THUNDER, 1f, 1.0f);
					UtilServer.broadcast(C.cRed + C.Bold + "🩸 FIRST BLOOD — " + C.cYellow + killer.getName() + C.cWhite + "!");
				}
			}
			else if (streak == 2)
			{
				killer.playSound(killer.getLocation(), Sound.ENTITY_FIREWORK_ROCKET_LAUNCH, 1f, 1.4f);
			}
			else if (streak >= 3)
			{
				killer.playSound(killer.getLocation(), Sound.ENTITY_LIGHTNING_BOLT_THUNDER, 1f, 1.0f);
				killer.playSound(killer.getLocation(), Sound.ENTITY_FIREWORK_ROCKET_TWINKLE, 1f, 1.0f);
				
				for (Player p : UtilServer.getPlayers()) {
					UtilPlayer.message(p, PrimalGamesLang.get().get(p, "primal_games.announce.rampage",
							"killer", killer.getName()));
				}
			}
		}

		// Death Scene FX
		player.playSound(player.getLocation(), Sound.ENTITY_LIGHTNING_BOLT_IMPACT, 1f, 0.8f);
		player.playSound(player.getLocation(), Sound.ENTITY_WITHER_HURT, 0.3f, 0.5f);

		// Death particle effect (Mineplex pattern: red burst firework on death)
		UtilParticle.PlayParticle(ParticleType.LARGE_SMOKE, player.getLocation().add(0, 2, 0), 0.5f, 1.0f, 0.5f, 0.1f, 20, ViewDist.NORMAL, UtilServer.getPlayers());
		UtilFirework.launchFirework(player.getLocation(),
				FireworkEffect.builder().with(FireworkEffect.Type.BALL).withColor(org.bukkit.Color.RED).build(), null, 1);

		// Death title via PrimalGamesLang
		PrimalGamesLang lang = PrimalGamesLang.get();
		player.sendTitle(
			lang.deathTitle(player),
			killer != null ? lang.deathSubtitle(player, killer.getName()) : "",
			10, 60, 20
		);

		Location loc = player.getLocation();
		org.bukkit.block.Block block = loc.getBlock();
		block.setType(Material.CHEST);
		
		if (block.getState() instanceof org.bukkit.block.Chest)
		{
			org.bukkit.block.Chest chest = (org.bukkit.block.Chest) block.getState();
			for (org.bukkit.inventory.ItemStack drop : event.getDrops())
			{
				if (drop != null && drop.getType() != Material.AIR) 
				{
					chest.getInventory().addItem(drop.clone());
				}
			}
			event.getDrops().clear(); 
			
			Announce(C.cYellow + C.Bold + "☠ " + player.getName() + " ตายที่ X:" + loc.getBlockX() + " Z:" + loc.getBlockZ());
		}
	}

	public void Announce(String message)
	{
		for (Player p : GetPlayers(true))
		{
			p.sendMessage("");
			p.sendMessage(ArcadeFormat.Line);
			p.sendMessage(PrimalGamesLang.get().announceHeader() + message);
			p.sendMessage(ArcadeFormat.Line);
			p.sendMessage("");
		}
	}

	/**
	 * Announce with optional chat broadcast toggle.
	 * Legacy callers use Announce(msg, false) to suppress spectator chat.
	 * This overload delegates to the main Announce for now.
	 */
	public void Announce(String message, boolean toAll)
	{
		java.util.Collection<Player> targets = toAll ? java.util.Arrays.asList(UtilServer.getPlayers()) : GetPlayers(true);
		for (Player p : targets)
		{
			p.sendMessage("");
			p.sendMessage(ArcadeFormat.Line);
			p.sendMessage(PrimalGamesLang.get().announceHeader() + message);
			p.sendMessage(ArcadeFormat.Line);
			p.sendMessage("");
		}
	}

	/**
	 * Delegates airdrop chest filling to LootTableManager.
	 */
	public void fillAirdropCrate(org.bukkit.block.Chest chest)
	{
		_lootTableManager.fillAirdropCrate(chest);
	}

	@EventHandler
	public void BlockBreak(BlockBreakEvent event)
	{
		if (!IsLive() || !IsAlive(event.getPlayer())) return;

		Block block = event.getBlock();
		Material type = block.getType();

		// Protect Bedrock and other unbreakable things
		if (type == Material.BEDROCK || type == Material.BARRIER || type == Material.COMMAND_BLOCK)
		{
			event.setCancelled(true);
			return;
		}

		// Protect Spawn Pedestals (assuming they are at spawn)
		if (UtilMath.offset(block.getLocation(), _spawn) < 8)
		{
			if (!_placedBlocks.contains(block.getLocation()))
			{
				event.setCancelled(true);
				return;
			}
		}

		// Allow everything else!
		event.setCancelled(false);
		
		// If it's a map block, we should probably handle its drop manually to ensure it drops?
		// Bukkit handles most block drops automatically in Survival mode.
	}

	@EventHandler
	public void BlockPlace(org.bukkit.event.block.BlockPlaceEvent event)
	{
		if (!IsLive() || !IsAlive(event.getPlayer())) return;

		_placedBlocks.add(event.getBlock().getLocation());
	}

	@EventHandler
	public void BlockBurn(BlockBurnEvent event)
	{
		event.setCancelled(true);
	}

	@EventHandler
	public void BlockDecay(LeavesDecayEvent event)
	{
		event.setCancelled(true);
	}

	@EventHandler
	public void BlockFade(BlockFadeEvent event)
	{
		event.setCancelled(true);
	}

	@EventHandler
	public void BlockSpread(BlockSpreadEvent event)
	{
		event.setCancelled(true);
	}

	private ItemStack buildCompass(int uses, Player viewer)
	{
		return _lootTableManager.buildCompass(uses, viewer);
	}

	@EventHandler
	public void CancelItemFrameBreak(HangingBreakEvent event)
	{
		if (event.getEntity() instanceof ItemFrame)
		{
			event.setCancelled(true);
		}
	}



	public void refillSecond()
	{
		_lootTableManager.refillSecond();
	}

	@EventHandler
	public void chestTickEvent(UpdateEvent event)
	{
		// Tick management delegated to LootTableManager
	}


	@EventHandler
	public void CreateRandomChests(GameStateChangeEvent event)
	{
		if (event.GetState() != GameState.Recruit)
			return;

		HashSet<Material> ignore = new HashSet<Material>();

		ignore.add(Material.OAK_LEAVES);

		int xDiff = WorldData.MaxX - WorldData.MinX;
		int zDiff = WorldData.MaxZ - WorldData.MinZ;
		
		if (xDiff <= 0 || zDiff <= 0) return;

		int done = 0;

		while (done < 40)
		{

			Block block = UtilBlock.getHighest(WorldData.World, WorldData.MinX + UtilMath.r(xDiff),
					WorldData.MinZ + UtilMath.r(zDiff), ignore);

			if (!UtilBlock.airFoliage(block) || !UtilBlock.solid(block.getRelative(BlockFace.DOWN)))
				continue;

			block.setType(org.bukkit.Material.CHEST);
			org.bukkit.block.data.type.Chest chestData = (org.bukkit.block.data.type.Chest) block.getBlockData();
			chestData.setFacing(new org.bukkit.block.BlockFace[]{org.bukkit.block.BlockFace.NORTH, org.bukkit.block.BlockFace.SOUTH, org.bukkit.block.BlockFace.EAST, org.bukkit.block.BlockFace.WEST}[com.houzicore.shared.common.util.UtilMath.r(4)]);
			block.setBlockData(chestData);
			done++;
		}
	}

	@EventHandler
	public void DayNightCycle(UpdateEvent event)
	{
		if (!IsLive())
			return;

		if (event.getType() != UpdateType.TICK)
			return;

		if (_deathMatchTeleported)
			return;

		long time = WorldData.World.getTime();

		if (time > 22000 || time < 14000)
		{
			WorldTimeSet = (WorldTimeSet + 4) % 24000;
		}
		else
		{
			WorldTimeSet = (WorldTimeSet + 16) % 24000;
		}

		WorldData.World.setTime(WorldTimeSet);
	}

	@EventHandler
	public void onVechilePlace(org.bukkit.event.vehicle.VehicleCreateEvent event)
	{
		if (event.getVehicle() instanceof org.bukkit.entity.Boat)
		{
			for (int x = -1; x <= 1; x++)
			{
				for (int y = -1; y <= 1; y++)
				{
					for (int z = -1; z <= 1; z++)
					{
						Block b = event.getVehicle().getLocation().add(x, y, z).getBlock();

						if (b.isLiquid())
						{
							return;
						}
					}
				}
			}

			event.getVehicle().remove();
		}
	}

	@EventHandler
	public void deathmatchBowShoot(EntityShootBowEvent event)
	{
		if (!_deathMatchTeleported)
			return;

		if (_deathMatchTime <= 0)
			return;

		event.getProjectile().remove();
	}

	@EventHandler(priority = EventPriority.LOWEST)
	public void DeathmatchDamage(EntityDamageByEntityEvent event)
	{
		if (!_deathMatchTeleported)
			return;

		if (_deathMatchTime <= 0)
			return;

		event.setCancelled(true);
	}

	@EventHandler
	public void DeathmatchMoveCancel(PlayerMoveEvent event)
	{
		if (_deathMatchTime <= 0)
			return;

		if (!_deathMatchTeleported)
			return;

		if (UtilMath.offset2d(event.getFrom(), event.getTo()) == 0)
			return;

		if (!IsAlive(event.getPlayer()))
			return;

		event.setTo(event.getFrom());
	}

	public void deathmatchSecond()
	{
		if (_deathMatchTime <= 0)
		{
			_gameEndTime--;

			if (_gameEndTime <= 0)
			{
				for (Player player : GetPlayers(true))
				{
					Manager.GetDamage().NewDamageEvent(player, null, null, DamageCause.CUSTOM, 1, false, true, true, "Game End",
							"Game End Damage");
				}
			}

			return;
		}

		_deathMatchTime--;

		if (_deathMatchTime > 0 && _deathMatchTime <= 60)
		{
			if (_deathMatchTime % 30 == 0 || _deathMatchTime == 15 || _deathMatchTime == 10
					|| (_deathMatchTeleported ? _deathMatchTime <= 10 : _deathMatchTime <= 5))
			{
				if (_deathMatchTeleported && _deathMatchTime <= 10)
				{
					for (Player p : GetPlayers(true)) {
						com.houzicore.shared.common.util.UtilTextMiddle.display(
							"",
							PrimalGamesLang.get().get(p, "primal_games.deathmatch.countdown_live", "seconds", String.valueOf(_deathMatchTime)),
							0, 40, 0,
							p
						);
					}
				}
				else
				{
					for (Player p : GetPlayers(true)) {
						com.houzicore.shared.common.util.UtilTextMiddle.display(
							"",
							PrimalGamesLang.get().get(p, "primal_games.deathmatch.countdown_pre", "seconds", String.valueOf(_deathMatchTime)),
							0, 40, 0,
							p
						);
					}
				}
			}
		}

		if (_deathMatchTeleported)
		{
			if (_deathMatchTime == 5)
			{
				for (Player player : GetPlayers(true))
				{
					VisibilityManager.Instance.refreshPlayerToAll(player);
				}
			}
			else if (_deathMatchTime == 0)
			{
				Announce(PrimalGamesLang.get().get(null, "primal_games.deathmatch.started"), false);

				_spawn.getWorld().playSound(_spawn, Sound.ENTITY_WITHER_DEATH, 1000, 0);

				refillChests();
			}
		}
		else if (_deathMatchTime == 0)
		{
			_deathMatchTeleported = true;

			WorldTimeSet = 0;
			WorldData.World.setTime(15000);

			for (GameTeam team : GetTeamList())
				team.SpawnTeleport(false);

			_borderPositions.clear();

			_currentBorder = 30.5;
			_previousBorder = 30.5;
			int i = 0;

			for (double border : buildBorders((2 * 30) - 15, 30.5, 7.5))
			{
				_borderPositions.put(_secondsSinceStart + 60 + (i++ * 2), border);
			}

			// Border now handled by native WorldBorder

			_deathMatchTime = 11;
		}
		else
		{
			if (_deathMatchTime <= 60)
				return;

			if (_secondsSinceStart < 5 * 60)
				return;

			if (GetPlayers(true).size() > 4)
				return;
		}
	}

	private BlockFace getFace(Location loc)
	{
		Block block = loc.getBlock();

		while (block.getY() > 0 && !UtilBlock.fullSolid(block.getRelative(BlockFace.DOWN))
				&& !UtilBlock.solid(block.getRelative(BlockFace.DOWN)))
		{
			block = block.getRelative(BlockFace.DOWN);
		}

		BlockFace proper = BlockFace.values()[Math.round(loc.getYaw() / 90F) & 0x3].getOppositeFace();

		// A complicated way to get the face the dead body should be towards.
		for (HashSet<Byte> validBlocks : new HashSet[]
				{
				UtilBlock.blockAirFoliageSet, UtilBlock.blockPassSet
				})
		{

				if (validBlocks.contains((byte) com.houzicore.shared.common.util.IdUtil.getTypeId(block.getRelative(proper))))
			{
				return proper;
			}

			for (BlockFace face : new BlockFace[]
					{
					BlockFace.EAST, BlockFace.SOUTH, BlockFace.NORTH, BlockFace.WEST
					})
			{
			if (validBlocks.contains((byte) com.houzicore.shared.common.util.IdUtil.getTypeId(block.getRelative(face))))
				{
					return face;
				}
			}
		}

		return proper;
	}

	private void deathOrQuit(Player player)
	{
		String name = "";

		for (char c : ("" + _deadBodyCount++).toCharArray())
		{
			name += "§" + c;
		}

		try
		{

			Team team = Scoreboard.GetScoreboard().getTeam(name);
			if (team == null)
				team = Scoreboard.GetScoreboard().registerNewTeam(name);

			team.setOption(Team.Option.NAME_TAG_VISIBILITY, Team.OptionStatus.NEVER);
			team.addEntry(name);


		}
		catch (Exception ex)
		{
			ex.printStackTrace();
		}

		/*
		GameProfile newProfile = new GameProfile(UUID.randomUUID(), name);
		// Note: CraftPlayer.getHandle().getProfile() is NMS but GameProfile is okay to use.
		// However, we should try to avoid even this if possible.
		// For now, I'll keep it but fix the CraftPlayer casting if it's problematic.
		// In 1.21.1, the profile is accessible via player.getPlayerProfile()
		org.bukkit.profile.PlayerProfile bukkitProfile = player.getPlayerProfile();
		for (org.bukkit.profile.ProfileProperty prop : bukkitProfile.getProperties())
		{
			newProfile.getProperties().put(prop.getName(), new Property(prop.getName(), prop.getValue(), prop.getSignature()));
		}

		DisguisePlayer disguise = new DisguisePlayer(null, newProfile);

		// disguise.setSleeping(getFace(player.getLocation()));

		getArcadeManager().GetDisguise().addFutureDisguise(disguise);
		*/

		Entity entity = player.getWorld().spawnEntity(player.getLocation(), EntityType.ARROW);

		if (entity instanceof Arrow)
		{
			// ((Arrow) entity).setDespawnTimer(Integer.MIN_VALUE);
		}

	}

	@EventHandler
	public void DisableDamageLevel(EntityDamageByEntityEvent event)
	{
		// event.SetDamageToLevel(false); // not migrated
	}

	@EventHandler
	public void SnowballEggsDamage(EntityDamageByEntityEvent event)
	{
		if (event.getDamager() instanceof Snowball || event.getDamager() instanceof Egg)
		{
   // /* event.AddMod(...) */;
		}
	}

	@EventHandler
	public void DisallowBrewingStand(PlayerInteractEvent event)
	{
		if (event.getClickedBlock() == null)
			return;

		if (event.getClickedBlock().getType() == Material.BREWING_STAND)
		{
			event.setCancelled(true);
		}
	}

	@EventHandler(priority = EventPriority.LOWEST)
	public void ExplosionDamageRemove(EntityExplodeEvent event)
	{
		event.blockList().clear();
	}


	@Override
	public double GetKillsGems(Player killer, Player killed, boolean assist)
	{
		if (assist)
			return 3;
		else
			return 12;
	}


	// If an item spawns and no one is there to see it, does it really spawn? No.
	@EventHandler
	public void ItemSpawn(ItemSpawnEvent event)
	{
		Material mat = event.getEntity().getItemStack().getType();

		switch (mat)
		{
		case WHEAT_SEEDS:
		case OAK_SAPLING:
		case VINE:
		case OAK_LEAVES:
		case TALL_GRASS:
		case POPPY:
		case DANDELION:
		case DEAD_BUSH:
		case LILY_PAD:
			// event.setCancelled(true);
			return;
		case CARROT:
// 			if (UtilMath.r(10) != 0)
// 			{
// 				event.setCancelled(true);
// 			}
// 			return;
		case POTATO:
// 			if (UtilMath.r(10) != 0)
// 			{
// 				event.setCancelled(true);
// 			}
// 			return;
		case WHEAT:
			if (UtilMath.r(6) != 0)
			{
				event.setCancelled(true);
			}
			return;
		case OAK_LOG:
// 			event.setCancelled(true);
// 			return;

		default:
			break;
		}

		for (Player player : GetPlayers(true))
			if (UtilMath.offset(player, event.getEntity()) < 6)
				return;

		event.setCancelled(true);
	}

	@EventHandler
	public void onGameEnd(GameStateChangeEvent event)
	{
		if (event.GetState() != GameState.End)
			return;
		// Game end fireworks are now handled by the UpdateEvent handler below
	}

	private long _gameEndFireworkStart = -1;

	@EventHandler
	public void onGameEndFireworks(UpdateEvent event)
	{
		if (event.getType() != UpdateType.FASTER)
			return;
		if (GetState() != GameState.End)
			return;
		if (GetTeamList().isEmpty())
			return;
		if (_gameEndFireworkStart < 0)
			_gameEndFireworkStart = System.currentTimeMillis();
		// Stop after 30 seconds
		if (System.currentTimeMillis() - _gameEndFireworkStart > 30000)
			return;
		for (Location loc : GetTeamList().get(0).GetSpawns())
		{
			Firework firework = (Firework) loc.getWorld().spawnEntity(loc, EntityType.FIREWORK_ROCKET);
			FireworkMeta meta = firework.getFireworkMeta();
			meta.addEffect(FireworkEffect.builder().withColor(Color.AQUA).with(Type.BALL).withTrail().build());
			firework.setFireworkMeta(meta);
		}
	}

	// onGameState / onJoin: legacy border packet setup removed (now native WorldBorder)

	@EventHandler
	public void onSecond(UpdateEvent event)
	{
		if (event.getType() != UpdateType.SEC)
		{
			return;
		}


		if (!IsLive())
		{
			return;
		}

		_previousBorder = _currentBorder;

		// We half the number so this only activates every 2nd second.
		if (_borderPositions.containsKey(_secondsSinceStart))
		{
			_currentBorder = _borderPositions.get(_secondsSinceStart);

			// Border now handled by native WorldBorder
		}

		_secondsSinceStart++;

		refillSecond();
		deathmatchSecond();
	}

	@EventHandler
	public void onUse(PlayerInteractEvent event)
	{
		if (!IsLive())
			return;

		Player player = event.getPlayer();

		if (!IsAlive(player))
			return;

		if (!event.getAction().name().contains("RIGHT"))
			return;

		ItemStack item = event.getItem();

		if (item == null || item.getType() != Material.COMPASS)
			return;

		int uses = Integer.parseInt(ChatColor.stripColor(item.getItemMeta().getLore().get(0)).replaceAll("\\D+", ""));

		if (uses > 0)
		{
			uses--;

			Player closestPlayer = null;
			double closestDistance = 0;

			for (Player alive : GetPlayers(true))
			{
				if (alive != player)
				{
					double distance = alive.getLocation().distance(player.getLocation());

					if (distance > 10 && (closestPlayer == null || distance < closestDistance))
					{
						closestDistance = distance;
						closestPlayer = alive;
					}
				}
			}

			if (closestPlayer != null)
			{
				player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1, 0);

				player.setCompassTarget(closestPlayer.getLocation());
				player.setItemInHand(buildCompass(uses, player));

				player.sendMessage(F.main("Compass", PrimalGamesLang.get().get(player,
						"primal_games.item.compass_located",
						"player", closestPlayer.getName(),
						"distance", String.valueOf((int) closestDistance))));

				if (uses >= 1)
				{
					player.sendMessage(F.main("Compass", PrimalGamesLang.get().get(player,
							"primal_games.item.compass_uses_remaining",
							"uses", String.valueOf(uses))));
				}
				else
				{
					player.sendMessage(F.main("Compass", PrimalGamesLang.get().get(player, "primal_games.item.compass_no_uses")));
				}
			}
			else
			{
				player.sendMessage(F.main("Compass", PrimalGamesLang.get().get(player, "primal_games.item.compass_no_target")));
				player.setCompassTarget(_spawn);
			}
		}
		else
		{
			player.sendMessage(F.main("Compass", PrimalGamesLang.get().get(player, "primal_games.item.compass_broken")));

			player.getWorld().playSound(player.getLocation(), Sound.ENTITY_ITEM_BREAK, 1, 5);

			player.setItemInHand(new ItemStack(Material.AIR));
		}
	}

	@EventHandler
	public void outsideBorder(UpdateEvent event)
	{
		if (!IsLive())
		{
			return;
		}

		if (event.getType() != UpdateType.FAST)
		{
			return;
		}

		// The distance between the old border and the new
		double distanceMovedSince = _currentBorder - _previousBorder;

		// Multiply that distance depending on how long its been since it moved.
		long timeSinceMoved = System.currentTimeMillis() - _borderStartedMoving;
		double percentageBorderMoved = Math.min(timeSinceMoved, 1000D) / 1000D;

		distanceMovedSince *= percentageBorderMoved;

		double border = (_previousBorder - 0.3D) + distanceMovedSince;

		//24 @ 100+    reduced to    0 at 32-
		double borderAttackDist = Math.max(8, (Math.min(100, border) - 28d) / 3d);
		double borderCheckDist = borderAttackDist + 6;
		
		for (Player player : UtilServer.getPlayers())
		{
			Location loc = player.getLocation();

			//Bump Players Back In
			if (loc.getX() > _spawn.getX() + border || 
					loc.getX() < _spawn.getX() - border || 
					loc.getZ() > _spawn.getZ() + border	|| 
					loc.getZ() < _spawn.getZ() - border)
			{
				if (Recharge.Instance.use(player, "Hit by Border", 1000, false, false))
				{
					Entity bottom = player;
					while (bottom.getVehicle() != null)
						bottom = bottom.getVehicle();
					
					UtilAction.velocity(bottom, UtilAlg.getTrajectory2d(loc, GetSpectatorLocation()), 1.2, true, 0.4, 0, 10, true);

					if (Manager.IsAlive(player))
					{
						Manager.GetDamage().NewDamageEvent(player, null, null, DamageCause.CUSTOM, 10, false, false, false, "Nether Field",
								"Vaporize");

						player.getWorld().playSound(loc, Sound.BLOCK_NOTE_BLOCK_BASS, 2f, 1f);
						player.getWorld().playSound(loc, Sound.BLOCK_NOTE_BLOCK_BASS, 2f, 1f);
					}
				}
			}

			if (border < 32)
				continue;
			
			//Attack Players who are nearby
			boolean isX = true;
			Location attackSource = null;
			if (loc.getX() > _spawn.getX() + (border-borderCheckDist))
			{
				attackSource = player.getLocation();
				attackSource.setX(_spawn.getX() + border);
			}
			else if (loc.getX() < _spawn.getX() - (border-borderCheckDist))
			{
				attackSource = player.getLocation();
				attackSource.setX(_spawn.getX() - border);
			}
			else if (loc.getZ() > _spawn.getZ() + (border-borderCheckDist))
			{
				attackSource = player.getLocation();
				attackSource.setZ(_spawn.getZ() + border);
				isX = false;
			}
			else if (loc.getZ() < _spawn.getZ() - (border-borderCheckDist))
			{
				attackSource = player.getLocation();
				attackSource.setZ(_spawn.getZ() - border);
				isX = false;
			}

			if (attackSource != null)
			{
				double dist = UtilMath.offset(player.getLocation(), attackSource);
				
				double scale = 1 - (dist / borderAttackDist);
				
				player.playSound(player.getLocation().add(UtilAlg.getTrajectory(player.getLocation(), attackSource).multiply(8)), 
						Sound.BLOCK_PORTAL_AMBIENT, (float)(1 - (dist / borderCheckDist)) * 2, 2f);

				if (!Manager.IsAlive(player))
					continue;
				
				//Shoot more frequently when they get closer
				if (dist < borderAttackDist && Math.random() < scale)
				{			 
					//Spawn Fireball
					Location spawn = attackSource.clone();
					spawn.add(isX ? 0 : (Math.random()-0.5)*12, 4 + Math.random() * 2 + (Math.random() * 12 * scale), isX ? (Math.random()-0.5)*12 : 0);
					
					//Raytrace back
					double maxBack = 8; 
					double back = 0;
					while (spawn.getBlock().getType() == Material.AIR && back < maxBack)
					{
						spawn.subtract(UtilAlg.getTrajectory(spawn, player.getLocation()).multiply(0.2));
						back += 0.1;
					}
					
					//Move out of block
					spawn.add(UtilAlg.getTrajectory(spawn, player.getLocation()).multiply(Math.min(back, 1)));
					
					
					Fireball ball = player.getWorld().spawn(spawn, Fireball.class);	

					//Trajectory
					Vector traj = UtilAlg.getTrajectory(spawn, player.getLocation());
					traj.add(new Vector((Math.random()-0.5)*0.2,(Math.random()-0.5)*0.2,(Math.random()-0.5)*0.2));

					ball.setDirection(traj.multiply(0.1));


					UtilParticle.PlayParticle(ParticleType.LARGE_EXPLODE, spawn, 0, 0, 0, 0, 1,
							ViewDist.MAX, UtilServer.getPlayers());
					player.getWorld().playSound(attackSource, Sound.ENTITY_GHAST_SHOOT, 2f, 2f);
				}	
			}
		}
	}

	@EventHandler
	public void borderBlockDamage(ProjectileHitEvent event)
	{
		if (!IsLive())
			return;
		
		if (!(event.getEntity() instanceof Fireball))
			return; 

		Collection<Block> blocks = UtilBlock.getInRadius(event.getEntity().getLocation(), 2.4).keySet();

		Manager.GetExplosion().BlockExplosion(blocks, event.getEntity().getLocation(), false);	
	}

	public boolean isStableBlock(Block block)
	{
		int sides = 0;
		if (UtilBlock.solid(block.getRelative(BlockFace.NORTH)))	sides++;
		if (UtilBlock.solid(block.getRelative(BlockFace.EAST)))		sides++;
		if (UtilBlock.solid(block.getRelative(BlockFace.SOUTH)))	sides++;
		if (UtilBlock.solid(block.getRelative(BlockFace.WEST)))		sides++;
		
		return sides >= 3;
	}
	
	@EventHandler
	public void borderDamage(EntityDamageByEntityEvent event)
	{
		if (event.getDamager() == null)
			return;

		if (!(event.getDamager() instanceof Fireball))
			return;

		// event.AddMult("Fireball", "Nether Field", 1, true); // not migrated

  // /* event.AddKnockback(...) */;
	}

	private ArrayList<Double> buildBorders(int seconds, double border, double leaveRemaining)
	{

		double totalNumber = Math.pow(seconds, 1.9D) + (seconds * 50);

		ArrayList<Double> borders = new ArrayList<Double>();

		for (int i = 0; i <= seconds; i++)
		{
			borders.add(border - ((border - leaveRemaining) * (((Math.pow(i, 1.9D) + (i * 50))) / totalNumber)));
		}

		return borders;
	}

	@Override
	public void ParseData()
	{
		_spawn = UtilWorld.averageLocation(GetTeamList().get(0).GetSpawns());

		ArrayList<Double> borders = new ArrayList<Double>();

		borders.add(WorldData.MaxX - _spawn.getX());
		borders.add(_spawn.getX() - WorldData.MinX);
		borders.add(WorldData.MaxZ - _spawn.getZ());
		borders.add(_spawn.getZ() - WorldData.MinZ);

		Collections.sort(borders);

		double largestBorder = borders.get(3);
		int i = 0;

		for (double border : buildBorders(10 * 30, largestBorder, 30.5))
		{
			_borderPositions.put(i++ * 2, border);
		}

		_currentBorder = _borderPositions.get(0);
		_previousBorder = _currentBorder;

		for (Location loc : GetTeamList().get(0).GetSpawns())
			loc.setYaw(UtilAlg.GetYaw(UtilAlg.getTrajectory(loc, _spawn)));

		setupChestsEnchantingCrafting();

		_supplyLocations = WorldData.GetDataLocs("WHITE");
		for (Location loc : _supplyLocations)
			loc.getBlock().setType(Material.GLASS);
	}

	@EventHandler
	public void PlayerKill(PlayerDeathEvent event)
	{
		if (!(event.getEntity() instanceof Player))
			return;

		Player player = (Player) event.getEntity();

		deathOrQuit(player);

		FireworkEffect effect = FireworkEffect.builder().flicker(false).withColor(Color.RED).with(Type.BALL_LARGE).trail(false)
				.build();
		for (int i = 0; i < 3; i++)
			UtilFirework.launchFirework(player.getLocation(), effect, null, 3);
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

	@EventHandler
	public void allowCrafting(PrepareItemCraftEvent event)
	{
		// We want players to be able to craft everything in Primal Games!
		// No restrictions like the old Survival Games.
	}

	public void refillChests()
	{
		_lootTableManager.refillChests();
	}

	@EventHandler
	public void RemoveNametagInfo(PlayerQuitEvent event)
	{
		if (!IsLive())
		{
			return;
		}

		if (!_hiddenNames.containsKey(event.getPlayer()))
		{
			return;
		}

		deathOrQuit(event.getPlayer());

	}




	private void setupChestsEnchantingCrafting()
	{
		ArrayList<Location> chests = WorldData.GetCustomLocs("54");


		// Enchants
		for (int i = 0; i < 5 && !chests.isEmpty(); i++)
		{
			Location loc = chests.remove(UtilMath.r(chests.size()));
			loc.getBlock().setType(Material.ENCHANTING_TABLE);
		}

		// Crafting
		for (int i = 0; i < 10 && !chests.isEmpty(); i++)
		{
			Location loc = chests.remove(UtilMath.r(chests.size()));
			loc.getBlock().setType(Material.CRAFTING_TABLE);
		}

		int spawn = 0;

		// Chests
		for (int i = 0; i < 250 && !chests.isEmpty(); i++)
		{
			Location loc = chests.remove(UtilMath.r(chests.size()));

			if (UtilMath.offset2d(loc, _spawn) < 8)
				spawn++;
		}

		for (Location loc : chests)
		{
			if (spawn < 10 && UtilMath.offset(loc, _spawn) < 8)
			{
				spawn++;
				continue;
			}

			loc.getBlock().setType(Material.AIR);
		}
	}


	@EventHandler
	public void SpeedRemove(EntityDamageByEntityEvent event)
	{
		if (!(event.getDamager() instanceof Player))
			return;
			
		Player damager = (Player) event.getDamager();
		if (damager != null) {
			Manager.GetCondition().EndCondition(damager, null, "Start Speed");
		}
	}

	@EventHandler
	public void StartEffectApply(GameStateChangeEvent event)
	{
		if (event.GetState() != GameState.Live)
			return;

		Scoreboard board = GetScoreboard().GetScoreboard();

		for (Player player : GetPlayers(true))
		{
			player.playSound(player.getLocation(), Sound.ENTITY_DONKEY_DEATH, 0.8F, 0);

			Manager.GetCondition().Factory().Speed("Start Speed", player, player, 30, 1, false, false, false);
			Manager.GetCondition().Factory().HealthBoost("Start Health", player, player, 30, 1, false, false, false);

			player.setHealth(player.getMaxHealth());

			Team team = board.registerNewTeam(player.getName());

			team.setPrefix(board.getPlayerTeam(player).getPrefix());

			team.addPlayer(player);

			_hiddenNames.put(player, new HashSet<String>());
		}
	}

	@EventHandler
	public void SupplyDrop(UpdateEvent event)
	{
		if (!IsLive())
			return;

		if (event.getType() != UpdateType.FASTEST)
			return;

		long time = WorldData.World.getTime();

		if (time > 14000 && time < 23000)
		{
			if (_supplyCurrent == null && !_deathMatchTeleported)
			{
				if (_supplyLocations.isEmpty())
					return;

				_supplyCurrent = _supplyLocations.remove(UtilMath.r(_supplyLocations.size()));

				// Remove Prior
				_lootTableManager.getSupplyCrates().remove(_supplyCurrent.getBlock().getRelative(BlockFace.UP));
				_supplyCurrent.getBlock().getRelative(BlockFace.UP).setType(Material.AIR);

				// Create New
				_supplyCurrent.getBlock().setType(Material.BEACON);
				for (int x = -1; x <= 1; x++)
					for (int z = -1; z <= 1; z++)
						_supplyCurrent.getBlock().getRelative(x, -1, z).setType(Material.IRON_BLOCK);

				// Announce
				Announce(C.cYellow + C.Bold + "📦 ของส่งจากฟ้า! (" + ChatColor.RESET
						+ UtilWorld.locToStrClean(_supplyCurrent) + C.cYellow + C.Bold + ")");
			}
		}
		else
		{
			if (_supplyCurrent != null)
			{
				if (_supplyEffect == null)
				{
					_supplyEffect = _supplyCurrent.clone();
					_supplyEffect.setY(250);
				}

				FireworkEffect effect = FireworkEffect.builder().flicker(false).withColor(Color.YELLOW).with(Type.BURST)
						.trail(false).build();
				UtilFirework.playFirework(_supplyEffect, effect);

				_supplyEffect.setY(_supplyEffect.getY() - 2);

				if (UtilMath.offset(_supplyEffect, _supplyCurrent) < 2)
				{
					effect = FireworkEffect.builder().flicker(false).withColor(Color.YELLOW).with(Type.BALL_LARGE).trail(true)
							.build();
					UtilFirework.playFirework(_supplyEffect, effect);

					// Create Chest
					_supplyCurrent.getBlock().setType(Material.GLASS);

					Block block = _supplyCurrent.getBlock().getRelative(BlockFace.UP);
					block.setType(Material.CHEST);
					_lootTableManager.getLandedCrates().add(block.getLocation());
					_lootTableManager.getSupplyCrates().add(block);
					_lootTableManager.getLootedBlocks().remove(_supplyCurrent);

					// Reset
					_supplyEffect = null;
					_supplyCurrent = null;
				}
			}
		}
	}

	@EventHandler
	public void SupplyGlow(UpdateEvent event)
	{
		if (!IsLive())
			return;

		if (event.getType() != UpdateType.TICK)
			return;

		if (_lootTableManager.getSupplyCrates().isEmpty())
			return;

		Iterator<Block> chestIterator = _lootTableManager.getSupplyCrates().iterator();

		while (chestIterator.hasNext())
		{
			Block block = chestIterator.next();

			if (block.getType() != Material.CHEST)
			{
				chestIterator.remove();
				continue;
			}

			UtilParticle.PlayParticle(ParticleType.SPELL, block.getLocation().add(0.5, 0.5, 0.5), 0.3f, 0.3f, 0.3f, 0, 1,
					ViewDist.LONG, UtilServer.getPlayers());
		}
	}

	@EventHandler
	public void TNTDelay(GameStateChangeEvent event)
	{
		if (event.GetState() != GameState.Live)
			return;

		for (Player player : UtilServer.getPlayers())
			Recharge.Instance.useForce(player, "Throw TNT", 30000);
	}

	@EventHandler
	public void TNTExplosion(ExplosionPrimeEvent event)
	{
		if (!_tntMap.containsKey(event.getEntity()))
			return;

		Player player = _tntMap.remove(event.getEntity());

		/*Explosion explosion = new Explosion(getArcadeManager().GetDamage(), event.getEntity().getLocation(),
				((TNTPrimed) event.getEntity()).getYield(), "Throwing TNT");

		explosion.setPlayer(player, true);*/

		for (Player other : UtilPlayer.getNearby(event.getEntity().getLocation(), 14)) {
			Manager.GetCondition().Factory().Explosion("Throwing TNT", other, player, 50, 0.1, false, false);
		}
	}

	@EventHandler
	public void TNTThrow(PlayerInteractEvent event)
	{
		if (!IsLive())
			return;

		if (!UtilEvent.isAction(event, ActionType.L))
			return;

		Player player = event.getPlayer();

		if (!UtilInv.IsItem(player.getItemInHand(), Material.TNT, (byte) 0))
			return;

		event.setCancelled(true);

		if (!Recharge.Instance.use(player, "Throw TNT", 0, true, false))
		{
			UtilPlayer.message(event.getPlayer(), F.main(GetName(), "You cannot use " + F.item("Throw TNT") + " yet."));
			return;
		}

		if (!Manager.GetGame().CanThrowTNT(player.getLocation()))
		{
			// Inform
			UtilPlayer.message(event.getPlayer(), F.main(GetName(), "You cannot use " + F.item("Throw TNT") + " here."));
			return;
		}

		UtilInv.remove(player, Material.TNT, (byte) 0, 1);
		UtilInv.Update(player);

		TNTPrimed tnt = player.getWorld()
				.spawn(player.getEyeLocation().add(player.getLocation().getDirection()), TNTPrimed.class);

		tnt.setFuseTicks(60);

		UtilAction.velocity(tnt, player.getLocation().getDirection(), 0.5, false, 0, 0.1, 10, false);

		_tntMap.put(tnt, player);
	}

	@EventHandler
	public void TourneyKills(PlayerDeathEvent event)
	{
		if (!(event.getEntity() instanceof Player))
			return;

		Player killed = (Player) event.getEntity();

		if (event.getEntity().getKiller() != null)
		{
			Player killer = event.getEntity().getKiller();

			if (killer != null && !killer.equals(killed))
			{
				// /* Manager.GetStatsManager().addStat(killer, GetName(), "kills", 1); */
			}
		}

		if (event.getEntity() != null)
		{
			if (killed != null)
			{
				// Manager.GetStatsManager().addStat(killed, GetName(), "deaths", 1);
			}
		}
	}

	@EventHandler
	public void UpdateNametagVisibility(UpdateEvent event)
	{
		if (event.getType() != UpdateType.TICK)
			return;

		if (!IsLive())
			return;

		ArrayList<Player> alivePlayers = new ArrayList<Player>(_hiddenNames.keySet());
		HashMap<Player, HashMap<Player, Boolean>> checkedPlayers = new HashMap<Player, HashMap<Player, Boolean>>();

		for (Player target : alivePlayers)
		{

			// NMS packet

			try
			{
				// NMS team

// 				packet = new PacketPlayOutScoreboardTeam(nmsTeam, 2);
			}
			catch (Exception ex)
			{
				ex.printStackTrace();
			}

			for (Player player : alivePlayers)
			{
				if (target != player)
				{
					boolean hideName = false;

					if (!checkedPlayers.containsKey(target) || !checkedPlayers.get(target).containsKey(player))
					{
						if (player.getLocation().distance(target.getLocation()) > (GetKit(target) instanceof KitAssassin ? 8 : 24))
						{
							hideName = true;
						}
						else if (!player.hasLineOfSight(target))
						{
							// no los
							hideName = true;
						}

						Player[] players = new Player[]
								{
								target, player
								};

						if (!(GetKit(player) instanceof KitAssassin || GetKit(target) instanceof KitAssassin))
						{
							for (int i = 0; i <= 1; i++)
							{
								Player p1 = players[i];
								Player p2 = players[1 - i];

								if (!checkedPlayers.containsKey(p1))
								{
									checkedPlayers.put(p1, new HashMap<Player, Boolean>());
								}

								checkedPlayers.get(p1).put(p2, hideName);
							}
						}
					}
					else
					{
						hideName = checkedPlayers.get(target).get(player);
					}

					// If hiddenNames conta
					if (hideName != _hiddenNames.get(player).contains(target.getName()))
					{
						if (!hideName)
						{
							_hiddenNames.get(player).remove(target.getName());
						}
						else
						{
							_hiddenNames.get(player).add(target.getName());
						}

						try
						{
							// _nameTagVisibility.set(packet, hideName ? "never" : "always");
						}
						catch (Exception ex)
						{
							ex.printStackTrace();
						}

// 						UtilPlayer.sendPacket(player, packet);
					}
				}
			}
		}
	}
	

	public int getChestRefillTime() 
	{
		return _lootTableManager.getChestRefillTime();
	}
	
	public int getDeathMatchTime() 
	{
		return this._deathMatchTime;
	}
	
	public boolean isDeathMatchTeleported() 
	{
		return this._deathMatchTeleported;
	}
	
	public int getGameEndTime() 
	{
		return this._gameEndTime;
	}
	
}
