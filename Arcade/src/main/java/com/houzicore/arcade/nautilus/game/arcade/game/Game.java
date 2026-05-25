package com.houzicore.arcade.nautilus.game.arcade.game;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.block.BlockFace;
//import org.bukkit.craftbukkit.v1_7_R4.CraftWorld;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Hanging;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.hanging.HangingBreakEvent;
import org.bukkit.event.hanging.HangingPlaceEvent;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.world.WorldLoadEvent;
import org.bukkit.scoreboard.Objective;
import org.bukkit.util.ChatPaginator;
import org.bukkit.util.Vector;

import com.houzicore.shared.common.util.C;
import com.houzicore.shared.common.util.F;
import com.houzicore.shared.common.util.NautHashMap;
import com.houzicore.shared.common.util.UtilBlock;
import com.houzicore.shared.common.util.UtilPlayer;
import com.houzicore.shared.common.util.UtilServer;
import com.houzicore.shared.common.util.UtilTabTitle;
import com.houzicore.shared.common.util.UtilTime;
import com.houzicore.shared.common.util.UtilTextMiddle;
import com.houzicore.shared.core.lang.LangManager;
import com.houzicore.shared.updater.UpdateType;
import com.houzicore.shared.updater.event.UpdateEvent;
//import com.houzicore.shared.classcombat.event.ClassCombatCreatureAllowSpawnEvent;
//import com.houzicore.shared.combat.DeathMessageType;
import com.houzicore.arcade.ArcadeFormat;
import com.houzicore.arcade.ArcadeManager;
import com.houzicore.arcade.GameType;
import com.houzicore.arcade.bootstrap.ArcadeBootstrap;
import com.houzicore.arcade.nautilus.game.arcade.events.GameStateChangeEvent;
import com.houzicore.arcade.nautilus.game.arcade.events.PlayerGameRespawnEvent;
import com.houzicore.arcade.nautilus.game.arcade.events.PlayerStateChangeEvent;
import com.houzicore.arcade.nautilus.game.arcade.game.GameTeam.PlayerState;
import com.houzicore.arcade.nautilus.game.arcade.kit.Kit;
import com.houzicore.arcade.nautilus.game.arcade.kit.KitAvailability;
import com.houzicore.arcade.nautilus.game.arcade.kit.Perk;
import com.houzicore.arcade.nautilus.game.arcade.managers.GameLobbyManager;
import com.houzicore.arcade.nautilus.game.arcade.scoreboard.GameScoreboard;
import com.houzicore.arcade.nautilus.game.arcade.stats.*;
import com.houzicore.arcade.nautilus.game.arcade.world.WorldData;
import com.houzicore.arcade.nautilus.game.arcade.game.modules.GameModule;
import com.houzicore.arcade.nautilus.game.arcade.game.modules.BloodModule;
import com.houzicore.arcade.nautilus.game.arcade.game.team.selectors.TeamSelector;
import com.houzicore.arcade.nautilus.game.arcade.game.team.selectors.EvenTeamSelector;
import com.houzicore.arcade.nautilus.game.arcade.game.team.GameTeamModule;

public abstract class Game implements Listener, com.houzicore.shared.core.lifecycle.LifecycleOwner
{
	private static final int CHAT_CENTER_PX = 154;
	private static final int ANNOUNCEMENT_WRAP_CHARS = 30;
	private static final String ANNOUNCEMENT_DIVIDER = "\u00A78\u00A7m────────────────────────────";

	public long getGameLiveTime()
	{
		return _gameLiveTime;
	}

	public void setGameLiveTime(long gameLiveTime)
	{
		_gameLiveTime = gameLiveTime;
	}

	public enum GameState
	{
		Loading,
		Vote,
		Recruit,
		Prepare,
		Live,
		End,
		Dead
	}

	public ArcadeManager Manager;

	public <T extends GameModule<?>> T registerModule(T module) {
		_modules.add(module);
		module.register();
		return module;
	}

	public <T extends GameModule<?>> T getModule(Class<T> type) {
		for (GameModule<?> module : _modules) {
			if (type.isInstance(module)) {
				return type.cast(module);
			}
		}
		return null;
	}

	public void unregisterModules() {
		for (GameModule<?> module : _modules) {
			module.unregister();
		}
		_modules.clear();
	}

	//Game
	private GameType _gameType;
	protected String[] _gameDesc;
	protected String[] _gameDescTh;

	//Map
	private HashMap<GameType, ArrayList<String>> _files;

	//State 
	private GameState _gameState = GameState.Loading;
	private long _gameLiveTime;
	private long _gameStateTime = System.currentTimeMillis();

	private boolean _prepareCountdown = false;
	public boolean ForceStart = false;

	private int _countdown = -1;
	private boolean _countdownForce = false;

	private String _customWinLine = "";

	//Kits
	private Kit[] _kits;

	//Teams	
	protected ArrayList<GameTeam> _teamList = new ArrayList<GameTeam>();

	//Modules
	protected List<GameModule<?>> _modules = new ArrayList<>();

	//Player Preferences
	protected NautHashMap<GameTeam, ArrayList<Player>> _teamPreference = new NautHashMap<GameTeam, ArrayList<Player>>();
	protected NautHashMap<Player, Kit> _playerKit = new NautHashMap<Player, Kit>();

	//Player Data
	private NautHashMap<Player, HashMap<String, EssenceData>> _essenceCount = new NautHashMap<Player, HashMap<String, EssenceData>>();
	private NautHashMap<Player, HashMap<String, Integer>> _stats = new NautHashMap<Player, HashMap<String, Integer>>();

	//Player Location Store
	private NautHashMap<String, Location> _playerLocationStore = new NautHashMap<String, Location>();

	//Scoreboard
	protected GameScoreboard Scoreboard;

	//Loaded from Map Config
	public WorldData WorldData = null;

	//Game Help
	private long _helpTimer = 0;
	private int _helpIndex = 0;
	private ChatColor _helpColor = ChatColor.YELLOW;
	protected String[] _help;

	//Gameplay Flags
	public long GameTimeout = 1200000;
	public boolean TeamMode = false;
	protected TeamSelector _teamSelector = new EvenTeamSelector();

	public TeamSelector getTeamSelector()
	{
		return _teamSelector;
	}

	public void setTeamSelector(TeamSelector teamSelector)
	{
		_teamSelector = teamSelector;
	}

	public GameTeamModule getTeamModule()
	{
		return getModule(GameTeamModule.class);
	}

	public boolean Damage = true;
	public boolean DamagePvP = true;
	public boolean DamagePvE = true;
	public boolean DamageEvP = true;
	public boolean DamageSelf = true;
	public boolean DamageTeamSelf = false;
	public boolean DamageTeamOther = true;

	public boolean BlockBreak = false;
	public boolean BlockBreakCreative = false;
	public HashSet<Integer> BlockBreakAllow = new HashSet<Integer>();
	public HashSet<Integer> BlockBreakDeny = new HashSet<Integer>();

	public boolean BlockPlace = false;
	public boolean BlockPlaceCreative = false;
	public HashSet<Integer> BlockPlaceAllow = new HashSet<Integer>();
	public HashSet<Integer> BlockPlaceDeny = new HashSet<Integer>();

	public boolean ItemPickup = false;
	public HashSet<Integer> ItemPickupAllow = new HashSet<Integer>();
	public HashSet<Integer> ItemPickupDeny = new HashSet<Integer>();

	public boolean ItemDrop = false;
	public HashSet<Integer> ItemDropAllow = new HashSet<Integer>();
	public HashSet<Integer> ItemDropDeny = new HashSet<Integer>();

	public boolean InventoryOpenBlock = false;
	public boolean InventoryOpenChest = false;
	public boolean InventoryClick = false;

	public boolean PrivateBlocks = false;

	public boolean DeathOut = true;
	public boolean DeathDropItems = false;
	public boolean DeathMessages = true;
	public boolean AutomaticRespawn = true;

	public double DeathSpectateSecs = 0;

	public boolean QuitOut = true;
	public boolean QuitDropItems = false;
	
	public boolean IdleKickz = true;

	public boolean CreatureAllow = false;
	public boolean CreatureAllowOverride = false;

	public int WorldTimeSet = 12000;
	public boolean WorldWeatherEnabled = false;
	public int WorldWaterDamage = 0;
	public boolean WorldBoundaryKill = true;
	public boolean WorldBlockBurn = false;
	public boolean WorldFireSpread = false;
	public boolean WorldLeavesDecay = false;
	public boolean WorldSoilTrample = false;
	public boolean WorldBoneMeal = false;

	public int HungerSet = -1;
	public int HealthSet = -1;

	public boolean PrepareFreeze = true;

	private double _itemMergeRadius = 0;

	public boolean AnnounceStay = true;
	public boolean AnnounceJoinQuit = true;
	public boolean AnnounceSilence = true;

	public boolean DisplayLobbySide = true;

	public GameState KitRegisterState = GameState.Live;

	public boolean JoinInProgress = false;
	
	public int TickPerTeleport = 1;
	
	public int FillTeamsInOrderToCount = -1;
	
	public boolean SpawnNearAllies = false;
	public boolean SpawnNearEnemies = false;
	
	public boolean StrictAntiHack = false;
	
	public boolean DisableKillCommand = true;
	
	public boolean GadgetsDisabled = true;
	
	public boolean TeleportsDisqualify = true;
	
	public boolean DontAllowOverfill = false;

	//Addons
	public boolean SoupEnabled = true;
	public boolean TeamArmor = false;
	public boolean TeamArmorHotbar = false;					

	public boolean GiveClock = true;						
	
	public boolean AllowParticles = true;					

	public double GemMultiplier = 1;
	public boolean GemHunterEnabled = true;
	public boolean GemBoosterEnabled = true;
	public boolean GemDoubleEnabled = true;
	
	public long PrepareTime = 9000;
	public boolean PlaySoundGameStart = true;
	
	//Gameplay Data
	public HashMap<Location, Player> PrivateBlockMap = new HashMap<Location, Player>();
	public HashMap<String, Integer> PrivateBlockCount = new HashMap<String, Integer>();

	public Location SpectatorSpawn = null;

	public boolean FirstKill = true;

	public String Winner = "Nobody";
	public GameTeam WinnerTeam = null;

	public boolean EloRanking = false;
	public int EloStart = 1000;

	public boolean CanAddStats = true;
	public boolean CanGiveLoot = true;
	
	public boolean HideTeamSheep = false;
	public boolean ReplaceTeamsWithKits = false;
	
	public boolean VersionRequire1_8 = false;
	
	public ArrayList<String> GemBoosters = new ArrayList<String>();
	private final Set<StatTracker<? extends Game>> _statTrackers = new HashSet<>();

	public Game(ArcadeManager manager, GameType gameType, Kit[] kits, String[] gameDesc)
	{
		this(manager, gameType, kits, gameDesc, gameDesc);
	}

	public Game(ArcadeManager manager, GameType gameType, Kit[] kits, String[] gameDesc, String[] gameDescTh)
	{
		Manager = manager;

		//Player List
		UtilTabTitle.broadcastHeaderAndFooter(C.cGold + C.Bold + gameType.GetName(), ChatColor.GRAY + com.houzicore.shared.core.common.BrandConfig.networkName());
		
		//Game
		_gameType = gameType;
		_gameDesc = gameDesc;
		_gameDescTh = gameDescTh;

		//Kits
		_kits = kits;

		//Scoreboard
		Scoreboard = new GameScoreboard(this);

		//Default Modules
		registerModule(new BloodModule(this));
		registerModule(new GameTeamModule(this));

		//Map Select
		_files = new HashMap<GameType, ArrayList<String>>();
		for(GameType type : GetWorldHostNames())
		{
            String folderName = type.GetMapFolderName();
            
			_files.put(type, Manager.LoadFiles(folderName));
		}
		if (Manager.GetGameCreationManager().MapPref != null)
		{
			HashMap<GameType, ArrayList<String>> matches = new HashMap<GameType, ArrayList<String>>();
			for (GameType game : _files.keySet())
			{
				ArrayList<String> list = new ArrayList<String>();
				for(String cur : _files.get(game))
				{
					if (cur.toLowerCase().contains(Manager.GetGameCreationManager().MapPref.toLowerCase()))
					{
						if(game.toString().toLowerCase().contains(Manager.GetGameCreationManager().MapSource.toLowerCase()))
						{
							list.add(cur);
							System.out.print("Map Preference: " + cur);
							matches.put(game, list);
						}
					}
				}
			}

			if (matches.size() > 0)
				_files = matches;

			Manager.GetGameCreationManager().MapPref = null;
			Manager.GetGameCreationManager().MapSource = null;
		}
		WorldData = new WorldData(this);

		//Stat Trackers
		registerStatTrackers(
				new KillsStatTracker(this),
				new DeathsStatTracker(this),
				new AssistsStatTracker(this),
				new ExperienceStatTracker(this),
				new WinStatTracker(this),
				new LoseStatTracker(this),
				new DamageDealtStatTracker(this),
				new DamageTakenStatTracker(this),
				new GamesPlayedStatTracker(this)
		);
		
		if (gameType != GameType.Event)
		{
			registerStatTrackers(
					new TeamDeathsStatTracker(this),
					new TeamKillsStatTracker(this)
					);
		}
		
		Manager.setResourcePack(gameType.getResourcePackUrl(), gameType.isEnforceResourcePack());

	}

	public void setKits(Kit[] kits)
	{
		_kits = kits;
	}

	public void loadWorld(String mapName)
	{
		if (WorldData != null && WorldData.World == null)
		{
			WorldData.Initialize(mapName);
		}
	}

	public HashMap<GameType, ArrayList<String>> GetFiles()
	{
		return _files;
	}

	public String GetName()
	{
		return _gameType.GetName();
	}
	
	public GameType[] GetWorldHostNames()
	{
		GameType[] mapSource = new GameType[]{GetType()};
		if(GetType().getMapSource() != null)
		{
			if(GetType().ownMaps())
			{
				int i = 1;
				mapSource = new GameType[GetType().getMapSource().length + 1];
				for(GameType type : GetType().getMapSource())
				{
					mapSource[i] = type;
					i++;
				}
				mapSource[0] = GetType();
			} 
			else
			{
				mapSource = GetType().getMapSource();
			}
		}
		return mapSource;
	}
	
	public String GetGameNamebyMap(String game, String map)
	{
		for(GameType type : _files.keySet())
		{
			if(type.GetName().toLowerCase().contains(game.toLowerCase()))
			{
				for(String string : _files.get(type))
				{
					if(string.toLowerCase().contains(map.toLowerCase()))
					{
						return type.GetName();
					}
				}
			}
		}
		return null;
	}
	
	public GameType GetGameByMapList(ArrayList<String> maps) 
	{
		for(GameType game : _files.keySet())
		{
			if(maps.equals(_files.get(game)))
			{
				return game;
			}
		}
		return null;
	}

	public String GetMode()
	{
		return null;
	}

	public GameType GetType()
	{
		return _gameType;
	}

	public String[] GetDesc()
	{
		return _gameDesc;
	}

	public String[] GetDesc(org.bukkit.entity.Player player)
	{
		if (Manager.getPreferences() != null && Manager.getPreferences().Get(player) != null)
		{
			String lang = Manager.getPreferences().Get(player).Language;
			if (lang != null && (lang.equalsIgnoreCase("TH") || lang.equalsIgnoreCase("THA")))
			{
				if (_gameDescTh != null)
					return _gameDescTh;
			}
		}
		return _gameDesc;
	}

	public void SetCustomWinLine(String line)
	{
		_customWinLine = line;
	}

	public GameScoreboard GetScoreboard()
	{
		return Scoreboard;
	}


	public ArrayList<GameTeam> GetTeamList()
	{
		return _teamList;
	}

	public int GetCountdown()
	{
		return _countdown;
	}

	public void SetCountdown(int time)
	{
		_countdown = time;
	}

	public boolean GetCountdownForce()
	{
		return _countdownForce;
	}

	public void SetCountdownForce(boolean value)
	{
		_countdownForce = value;
	}

	public NautHashMap<GameTeam, ArrayList<Player>> GetTeamPreferences()
	{
		return _teamPreference;
	}

	public NautHashMap<Player, Kit> GetPlayerKits()
	{
		return _playerKit;
	}

	public NautHashMap<Player, HashMap<String, EssenceData>> GetEssence()
	{
		return _essenceCount;
	}

	public NautHashMap<String, Location> GetLocationStore()
	{
		return _playerLocationStore;
	}

	public GameState GetState()
	{
		return _gameState;
	}

	protected boolean contextRuntime = false;
	public boolean isContextRuntime() { return contextRuntime; }

	protected GameRuleSet ruleset = null;
	public GameRuleSet getRules() { 
		if (contextRuntime && ruleset == null) {
			throw new IllegalStateException("Context-runtime game " + GetName() + " MUST define a GameRuleSet.");
		}
		return ruleset; 
	}

	public void SetState(GameState state)
	{
		_gameState = state;
		_gameStateTime = System.currentTimeMillis();

		if (_gameState == GameState.Live)
			setGameLiveTime(_gameStateTime);

		for (Player player : UtilServer.getPlayers())
			player.leaveVehicle();

		//Event
		GameStateChangeEvent stateEvent = new GameStateChangeEvent(this, state);
		UtilServer.getServer().getPluginManager().callEvent(stateEvent);

		if (state == GameState.Recruit && ForceStart)
		{
			org.bukkit.Bukkit.getScheduler().runTaskLater(Manager.getPlugin(), () -> {
				SetState(GameState.Prepare);
			}, 1L);
		}
	}

	public void SetStateTime(long time)
	{
		_gameStateTime = time;
	}

	public long GetStateTime()
	{
		return _gameStateTime;
	}

	public boolean InProgress()
	{
		return GetState() == GameState.Prepare || GetState() == GameState.Live;
	}

	public boolean IsLive()
	{
		return _gameState == GameState.Live;
	}

	public void AddTeam(GameTeam team)
	{
		//Add
		GetTeamList().add(team);

	}

	public void RemoveTeam(GameTeam team)
	{
		GetTeamList().remove(team);
	}

	public boolean HasTeam(GameTeam team)
	{
		for (GameTeam cur : GetTeamList())
			if (cur.equals(team))
				return true;

		return false;
	}

	public void RestrictKits()
	{
		//Null Default
	}

	public void RegisterKits()
	{
		for (Kit kit : _kits)
		{
			UtilServer.getServer().getPluginManager().registerEvents(kit, Manager.getPlugin());

			for (Perk perk : kit.GetPerks())
			{
				UtilServer.getServer().getPluginManager()
						.registerEvents(perk, Manager.getPlugin());
				perk.registeredEvents();
			}
		}
	}

	public void DeregisterKits()
	{
		for (Kit kit : _kits)
		{
			HandlerList.unregisterAll(kit);

			for (Perk perk : kit.GetPerks())
				HandlerList.unregisterAll(perk);
		}
	}

	public void ParseData()
	{
		//Nothing by default,
		//Use this to parse in extra location data from maps
	}

	public void SetPlayerTeam(Player player, GameTeam team, boolean in)
	{
		//Clean Old Team
		GameTeam pastTeam = this.GetTeam(player);
		if (pastTeam != null)
		{
			pastTeam.RemovePlayer(player);
		}

		team.AddPlayer(player, in);

		//Ensure Valid Kit
		ValidateKit(player, team);

		//Game Scoreboard
		Scoreboard.SetPlayerTeam(player, team.GetName().toUpperCase());

		//Lobby Scoreboard
		Manager.GetLobby().AddPlayerToScoreboards(player, team.GetName().toUpperCase());

		//Save Tournament Team
		Manager.GetGameTournamentManager().setTournamentTeam(player, team);
	}

	public GameTeam ChooseTeam(Player player)
	{
		if (FillTeamsInOrderToCount != -1)
		{
			for (int i = 0; i < _teamList.size(); i++)
			{
				if (_teamList.get(i).GetSize() < FillTeamsInOrderToCount)
				{
					return _teamList.get(i);
				}
			}
		}
		
		GameTeam team = null;

		//Random Team
		for (int i = 0; i < _teamList.size(); i++)
		{
			if (team == null || _teamList.get(i).GetSize() < team.GetSize())
			{
				team = _teamList.get(i);
			}
		}

		return team;
	}

	public double GetKillsGems(Player killer, Player killed, boolean assist)
	{
		if (!DeathOut)
		{
			return 0.5;
		}

		if (!assist)
		{
			return 4;
		}
		else
		{
			return 1;
		}
	}

	public HashMap<String, EssenceData> GetEssence(Player player)
	{
		if (!_essenceCount.containsKey(player))
			_essenceCount.put(player, new HashMap<String, EssenceData>());

		return _essenceCount.get(player);
	}

	public void AddGems(Player player, double gems, String reason, boolean countAmount, boolean multipleAllowed)
	{
		if (!countAmount && gems < 1)
			gems = 1;

		if (GetEssence(player).containsKey(reason) && multipleAllowed)
		{
			GetEssence(player).get(reason).AddGems(gems);
		}
		else
		{
			GetEssence(player).put(reason, new EssenceData(gems, countAmount));
		}
		
		if (Manager.getBattlePassManager() != null && gems > 0)
		{
			Manager.getBattlePassManager().addXp(player.getName(), (int) (gems * 10));
		}
	}

	public void ValidateKit(Player player, GameTeam team)
	{
		//Kit
		if (GetKit(player) == null || !team.KitAllowed(GetKit(player)))
		{
			for (Kit kit : _kits)
			{
				if (kit.GetAvailability() == KitAvailability.Hide ||
						kit.GetAvailability() == KitAvailability.Null)
					continue;

				if (team.KitAllowed(kit))
				{
					SetKit(player, kit, false);
					break;
				}
			}
		}
	}

	public void SetKit(Player player, Kit kit, boolean announce)
	{
		GameTeam team = GetTeam(player);
		if (team != null)
		{
			if (!team.KitAllowed(kit))
			{
				player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASS, 2f, 0.5f);
				UtilPlayer.message(player, F.main("Kit", F.elem(player.getName()) + (LangManager.get().isThai(player) ? " \u00A7c\u0e44\u0e21\u0e48\u0e2a\u0e32\u0e21\u0e32\u0e23\u0e16\u0e43\u0e0a\u0e49\u0e07\u0e32\u0e19\u0e04\u0e34\u0e17 " + F.elem(kit.GetFormattedName()) + " \u00A7c\u0e44\u0e14\u0e49." : " \u00A7ccannot use kit " + F.elem(kit.GetFormattedName()) + ".")));
				return;
			}
		}

		if (_playerKit.get(player) != null)
		{
			_playerKit.get(player).Deselected(player);
		}

		_playerKit.put(player, kit);

		kit.Selected(player);

		if (announce)
		{
			player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 2f, 1f);
			UtilPlayer.message(player, F.main("Kit", LangManager.get().isThai(player) ? "\u00A77\u0e04\u0e38\u0e13\u0e2a\u0e27\u0e21\u0e43\u0e2a\u0e48\u0e04\u0e34\u0e17 " + F.elem(kit.GetFormattedName()) + "." : "\u00A77You equipped kit " + F.elem(kit.GetFormattedName()) + "."));
		}

		if (InProgress())
		{
			kit.ApplyKit(player);
		}
		else if ((GetState() == GameState.Recruit || GetState() == GameState.Vote) && Manager.GetLobby() != null)
		{
			Manager.GetLobby().resendAllKitGlowStates(player);
		}
	}

	public Kit GetKit(Player player)
	{
		return _playerKit.get(player);
	}

	public Kit[] GetKits()
	{
		return _kits;
	}

	public boolean HasKit(Kit kit)
	{
		for (Kit cur : GetKits())
			if (cur.equals(kit))
				return true;

		return false;
	}

	public boolean HasKit(Player player, Kit kit)
	{
		if (!IsAlive(player))
			return false;

		if (GetKit(player) == null)
			return false;

		return GetKit(player).equals(kit);
	}

	public boolean SetPlayerState(Player player, PlayerState state)
	{
		GetScoreboard().ResetScore(player.getName());

		GameTeam team = GetTeam(player);

		if (team == null)
			return false;

		team.SetPlayerState(player, state);

		//Event
		PlayerStateChangeEvent playerStateEvent = new PlayerStateChangeEvent(this, player, PlayerState.OUT);
		UtilServer.getServer().getPluginManager().callEvent(playerStateEvent);

		// Flush game-state Player references once eliminated to prevent stale Memory
		if (state == PlayerState.OUT)
		{
			cleanupEliminatedPlayer(player);
		}

		return true;
	}

	public abstract void EndCheck();

	public void RespawnPlayer(final Player player)
	{
		player.eject();
		player.teleport(GetTeam(player).GetSpawn());

		ArcadeBootstrap.getInstance().getPlayerStateApplier().cleanState(player);

		//Event
		PlayerGameRespawnEvent event = new PlayerGameRespawnEvent(this, player);
		UtilServer.getServer().getPluginManager().callEvent(event);

		//Re-Give Kit
		UtilServer.getServer().getScheduler().runTask(Manager.getPlugin(), new Runnable()
		{
			public void run()
			{
				GetKit(player).ApplyKit(player);
			}
		});
	}

	public boolean IsPlaying(Player player)
	{
		return GetTeam(player) != null;
	}

	public void PlayerQuit(PlayerQuitEvent event)
	{
		Player player = event.getPlayer();
		
		if (GetScoreboard() != null)
		{
			GetScoreboard().removeBoard(player);
		}

		if (GetState() == GameState.Recruit)
		{
			GameTeam team = GetTeam(player);
			if (team != null)
				team.RemovePlayer(player);
		}

		// Always flush stale Player references regardless of game state
		cleanupPlayer(player);
	}

	public boolean IsAlive(Entity entity)
	{
		if (entity instanceof Player)
		{
			Player player = (Player)entity;
			
			GameTeam team = GetTeam(player);

			if (team == null)
				return false;

			return team.IsAlive(player);
		}
		
		return false;
	}

	public ArrayList<Player> GetPlayers(boolean aliveOnly)
	{
		ArrayList<Player> players = new ArrayList<Player>();

		for (GameTeam team : _teamList)
			players.addAll(team.GetPlayers(aliveOnly));

		return players;
	}

	public GameTeam GetTeam(String player, boolean aliveOnly)
	{
		for (GameTeam team : _teamList)
			if (team.HasPlayer(player, aliveOnly))
				return team;

		return null;
	}

	public GameTeam GetTeam(Player player)
	{
		if (player == null)
			return null;

		for (GameTeam team : _teamList)
			if (team.HasPlayer(player))
				return team;

		return null;
	}

	public GameTeam GetTeam(ChatColor color)
	{
		for (GameTeam team : _teamList)
			if (team.GetColor() == color)
				return team;

		return null;
	}

	public Location GetSpectatorLocation()
	{
		if (SpectatorSpawn != null && SpectatorSpawn.getWorld() != null)
			return SpectatorSpawn;

		if (this.WorldData == null || this.WorldData.World == null)
		{
			// Fallback to primary world spawn if completely unloaded
			return UtilServer.getServer().getWorlds().get(0).getSpawnLocation();
		}

		Vector vec = new Vector(0, 0, 0);
		double count = 0;

		for (GameTeam team : this.GetTeamList())
		{
			for (Location spawn : team.GetSpawns())
			{
				count++;
				vec.add(spawn.toVector());
			}
		}

		SpectatorSpawn = new Location(this.WorldData.World, 0, 0, 0);

		if (count > 0)
		{
			vec.multiply(1d / count);

			SpectatorSpawn.setX(vec.getX());
			SpectatorSpawn.setY(vec.getY());
			SpectatorSpawn.setZ(vec.getZ());
		}
		else
		{
			SpectatorSpawn.setX(0);
			SpectatorSpawn.setY(100);
			SpectatorSpawn.setZ(0);
		}

		//Move Up - Out Of Blocks
		while (!UtilBlock.airFoliage(SpectatorSpawn.getBlock()) || !UtilBlock.airFoliage(SpectatorSpawn.getBlock().getRelative(BlockFace.UP)))
		{
			SpectatorSpawn.add(0, 1, 0);
		}

		int Up = 0;

		//Move Up - Through Air
		for (int i = 0; i < 15; i++)
		{
			if (UtilBlock.airFoliage(SpectatorSpawn.getBlock().getRelative(BlockFace.UP)))
			{
				SpectatorSpawn.add(0, 1, 0);
				Up++;
			}
			else
			{
				break;
			}
		}

		//Move Down - Out Of Blocks
		while (Up > 0 && !UtilBlock.airFoliage(SpectatorSpawn.getBlock()) || !UtilBlock.airFoliage(SpectatorSpawn.getBlock().getRelative(BlockFace.UP)))
		{
			SpectatorSpawn.subtract(0, 1, 0);
			Up--;
		}

		SpectatorSpawn = SpectatorSpawn.getBlock().getLocation().add(0.5, 0.1, 0.5);

		while (!SpectatorSpawn.getBlock().getType().isAir() || !SpectatorSpawn.getBlock().getRelative(BlockFace.UP).getType().isAir())
			SpectatorSpawn.add(0, 1, 0);

		return SpectatorSpawn;
	}

	@EventHandler
	public void eloStart(PlayerLoginEvent event)
	{
		if (EloRanking)
		{
			if (Manager.getEloManager().getElo(event.getPlayer().getUniqueId(), GetName()) == -1)
			{
				Manager.getEloManager().saveElo(event.getPlayer().getUniqueId(), GetName(), EloStart);
			}
		}
	}

	@EventHandler
	public abstract void ScoreboardUpdate(UpdateEvent event);

//	public DeathMessageType GetDeathMessageType()
//	{
//		if (!DeathMessages)
//			return DeathMessageType.None;
//
//		if (this.DeathOut)
//			return DeathMessageType.Detailed;
//
//		return DeathMessageType.Simple;
//	}

	public boolean CanJoinTeam(GameTeam team)
	{
		return Manager.IsTeamBalance() ? team.GetSize() < Math.max(1, UtilServer.getPlayers().length / GetTeamList().size()) : true;
	}

	@EventHandler
	public final void onFoodLevelChangeEvent(FoodLevelChangeEvent event)
	{
		if (!(event.getEntity() instanceof Player)) return;
	    ((Player) event.getEntity()).setSaturation(3.8F);
	}

	public GameTeam GetTeamPreference(Player player)
	{
		for (GameTeam team : _teamPreference.keySet())
		{
			if (_teamPreference.get(team).contains(player))
				return team;
		}
		return null;
	}

	public void RemoveTeamPreference(Player player)
	{
		for (ArrayList<Player> queue : _teamPreference.values())
			queue.remove(player);
	}

	public void cleanupEliminatedPlayer(Player player)
	{
		this._playerKit.remove(player);
		RemoveTeamPreference(player);

		if (this.Scoreboard != null)
		{
			this.Scoreboard.removeBoard(player);
		}
	}

	public void cleanupPlayer(Player player)
	{
		cleanupEliminatedPlayer(player);
		this._essenceCount.remove(player);
		this._stats.remove(player);
	}

	public String GetTeamQueuePosition(Player player)
	{
		for (ArrayList<Player> queue : _teamPreference.values())
		{
			for (int i = 0; i < queue.size(); i++)
			{
				if (queue.get(i).equals(player))
					return (i + 1) + "/" + queue.size();
			}
		}
		return "Unknown";
	}

	public void InformQueuePositions()
	{
		for (GameTeam team : _teamPreference.keySet())
		{
			for (Player player : _teamPreference.get(team))
			{
				UtilPlayer.message(player, F.main("Team", LangManager.get().isThai(player) ? "\u00A77\u0e04\u0e38\u0e13\u0e2d\u0e22\u0e39\u0e48\u0e04\u0e34\u0e27\u0e17\u0e35\u0e48 \u00A7f" + F.elem(GetTeamQueuePosition(player)) + " \u00A77\u0e2a\u0e33\u0e2b\u0e23\u0e31\u0e1a\u0e17\u0e35\u0e21 " + F.elem(team.GetFormattedName() + " Team") + "." : "\u00A77You are " + F.elem(GetTeamQueuePosition(player)) + " in queue for " + F.elem(team.GetFormattedName() + " Team") + "."));
			}
		}
	}

	public void AnnounceGame()
	{
		for (Player player : UtilServer.getPlayers())
			AnnounceGame(player);
		if (AnnounceSilence)
			Manager.GetChat().Silence(PrepareTime, false);
	}

	public void AnnounceGame(Player player)
	{
		player.playSound(player.getLocation(), org.bukkit.Sound.ENTITY_PLAYER_LEVELUP, 2f, 1f);

		List<String> descriptionLines = buildAnnouncementDescriptionLines(player);
		UtilTextMiddle.display(
				"\u00A76\u00A7l\u2726 \u00A7f\u00A7l" + com.houzicore.shared.common.util.UtilText.toSmallCaps("game start") + " \u00A76\u00A7l\u2726",
				"\u00A77" + this.GetName(),
				10,
				70,
				10,
				player);

		for (int i = 0; i < Math.max(0, 6 - descriptionLines.size()); i++)
			com.houzicore.shared.common.util.UtilPlayer.message(player, "");

		net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer leg = net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer.legacySection();

		player.sendMessage(leg.deserialize(centerText(ANNOUNCEMENT_DIVIDER)));
		player.sendMessage(net.kyori.adventure.text.Component.text(""));

		player.sendMessage(leg.deserialize(centerText("\u00A76\u00A7l\u2726 \u00A7f\u00A7l" + com.houzicore.shared.common.util.UtilText.toSmallCaps("game start") + " \u00A76\u00A7l\u2726")));
		player.sendMessage(leg.deserialize(centerText("\u00A78\u00A7o" + com.houzicore.shared.core.common.BrandConfig.networkName() + " \u00A78\u2022 \u00A77" + this.GetName())));
		player.sendMessage(net.kyori.adventure.text.Component.text(""));

		for (String line : descriptionLines)
		{
			if (ChatColor.stripColor(line).trim().isEmpty())
			{
				player.sendMessage(net.kyori.adventure.text.Component.text(""));
				continue;
			}

			player.sendMessage(leg.deserialize(centerText(line)));
		}

		player.sendMessage(net.kyori.adventure.text.Component.text(""));
		for (String footerLine : wrapAnnouncementLine(getAnnouncementFooter(player)))
		{
			player.sendMessage(leg.deserialize(centerText(footerLine)));
		}

		player.sendMessage(leg.deserialize(centerText(ANNOUNCEMENT_DIVIDER)));
	}

	public void AnnounceEnd(GameTeam team)
	{
		if (!IsLive())
			return;

		String winnerText = ChatColor.WHITE + "Nobody";
		ChatColor subColor = ChatColor.WHITE;
		
		if (team != null)
		{
			WinnerTeam = team;
			Winner = team.GetName() + " Team";
			winnerText = team.GetColor() + team.GetName();
			subColor = team.GetColor();
		}

		sendInteractiveSummary(team != null ? team.GetColor() + C.Bold + team.GetName() + " \u00A77\u0e0a\u0e19\u0e30\u0e40\u0e01\u0e21\u0e19\u0e35\u0e49!" : "\u00A7c\u00A7l\u0e44\u0e21\u0e48\u0e21\u0e35\u0e1c\u0e39\u0e49\u0e0a\u0e19\u0e30!", null);
		UtilTextMiddle.display("\u00A76\u00A7l\u2726 " + winnerText + " \u00A76\u00A7l\u2726", subColor + "\u0e0a\u0e19\u0e30\u0e40\u0e01\u0e21\u0e19\u0e35\u0e49", 20, 120, 20);

		if (AnnounceSilence)
			Manager.GetChat().Silence(5000, false);
	}

	public void AnnounceEnd(List<Player> places)
	{
		if (!IsLive())
			return;

		String winnerText = ChatColor.RED + "\u00A7l\u0e44\u0e21\u0e48\u0e21\u0e35\u0e1c\u0e39\u0e49\u0e0a\u0e19\u0e30...";
		ChatColor subColor = ChatColor.WHITE;

		if (places != null && !places.isEmpty())
		{
			Winner = places.get(0).getName();
			winnerText = C.cYellow + places.get(0).getName();
			subColor = ChatColor.YELLOW;
		}

		sendInteractiveSummary(null, places);
		UtilTextMiddle.display("\u00A76\u00A7l\u2726 " + winnerText + " \u00A76\u00A7l\u2726", subColor + "\u0e0a\u0e19\u0e30\u0e40\u0e01\u0e21\u0e19\u0e35\u0e49", 20, 120, 20);

		if (AnnounceSilence)
			Manager.GetChat().Silence(5000, false);
	}

	private void sendInteractiveSummary(String teamWinText, List<Player> places)
	{
		net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer leg = net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer.legacySection();

		String sep = ArcadeFormat.Line;

		// ── Header ──
		net.kyori.adventure.text.Component header = net.kyori.adventure.text.Component.text()
			.append(leg.deserialize("\n" + sep))
			.append(leg.deserialize("\n"))
			.append(leg.deserialize(centerText("\u00A76\u00A7l\u2726 \u00A7f\u00A7l" + com.houzicore.shared.common.util.UtilText.toSmallCaps("game over") + " \u00A76\u00A7l\u2726")))
			.append(leg.deserialize("\n"))
			.build();

		// ── Game name ──
		net.kyori.adventure.text.Component gameInfo = leg.deserialize(centerText("\u00A77\u00A7o" + this.GetName()) + "\n\n");

		// ── Winner / Placement rows ──
		net.kyori.adventure.text.Component winners = net.kyori.adventure.text.Component.empty();

		if (teamWinText != null)
		{
			winners = winners.append(leg.deserialize(centerText(teamWinText) + "\n"));
		}
		else
		{
			if (places == null || places.isEmpty())
			{
				winners = winners.append(leg.deserialize(centerText("\u00A7c\u00A7l\u0e44\u0e21\u0e48\u0e21\u0e35\u0e1c\u0e39\u0e49\u0e0a\u0e19\u0e30...") + "\n"));
			}
			else
			{
				if (places.size() >= 1)
					winners = winners.append(getPlayerSummaryLine("\u00A76\u00A7l\uD83C\uDFC6 \u00A7e\u00A7l\u0e2d\u0e31\u0e19\u0e14\u0e31\u0e1a 1", places.get(0), "\u00A7e"));
				if (places.size() >= 1)
					winners = winners.append(leg.deserialize("\n"));
				if (places.size() >= 2)
					winners = winners.append(getPlayerSummaryLine("\u00A7f\u00A7l\u25C9 \u00A77\u00A7l\u0e2d\u0e31\u0e19\u0e14\u0e31\u0e1a 2", places.get(1), "\u00A77"));
				if (places.size() >= 3)
					winners = winners.append(getPlayerSummaryLine("\u00A76\u25C9 \u00A76\u0e2d\u0e31\u0e19\u0e14\u0e31\u0e1a 3", places.get(2), "\u00A76"));
			}
		}

		if (_customWinLine != null && !_customWinLine.isEmpty())
		{
			winners = winners.append(leg.deserialize("\n" + centerText(_customWinLine)));
		}

		// ── Map info ──
		net.kyori.adventure.text.Component mapInfo = leg.deserialize(
			"\n" + centerText("\u00A78\u0e41\u0e21\u0e1e \u00A77" + WorldData.MapName + " \u00A78\u2022 \u00A77\u0e42\u0e14\u0e22 " + WorldData.MapAuthor) + "\n");

		// ── Footer ──
		net.kyori.adventure.text.Component footer = leg.deserialize(sep + "\n");

		net.kyori.adventure.text.Component fullMessage = net.kyori.adventure.text.Component.text()
			.append(header)
			.append(gameInfo)
			.append(winners)
			.append(mapInfo)
			.append(footer)
			.build();

		for (Player player : UtilServer.getPlayers())
		{
			player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 2f, 1f);
			player.sendMessage(fullMessage);
		}
	}

	/**
	 * Center a legacy-formatted string in Minecraft chat using approximate font widths.
	 */
	private String centerText(String text)
	{
		if (text == null || text.isEmpty())
			return "";

		int messagePxSize = getLegacyTextPixelWidth(text);
		int halvedMessageSize = messagePxSize / 2;
		int toCompensate = CHAT_CENTER_PX - halvedMessageSize;

		if (toCompensate <= 0)
			return text;

		int spaceWidth = getChatCharacterWidth(' ') + 1;
		StringBuilder sb = new StringBuilder();
		int compensated = 0;

		while (compensated < toCompensate)
		{
			sb.append(' ');
			compensated += spaceWidth;
		}

		return sb.toString() + text;
	}

	private List<String> buildAnnouncementDescriptionLines(Player player)
	{
		ArrayList<String> lines = new ArrayList<>();

		for (String line : this.GetDesc(player))
		{
			if (line == null || line.isEmpty())
			{
				lines.add("");
				continue;
			}

			lines.addAll(wrapAnnouncementLine("\u00A7a" + line));
		}

		return lines;
	}



	private List<String> wrapAnnouncementLine(String legacyLine)
	{
		ArrayList<String> lines = new ArrayList<>();

		if (legacyLine == null || legacyLine.isEmpty())
		{
			lines.add("");
			return lines;
		}

		String stripped = ChatColor.stripColor(legacyLine);
		if (stripped == null || stripped.trim().isEmpty())
		{
			lines.add("");
			return lines;
		}

		for (String line : ChatPaginator.wordWrap(legacyLine, ANNOUNCEMENT_WRAP_CHARS))
		{
			lines.add(line);
		}

		return lines;
	}

	private String getAnnouncementFooter(Player player)
	{
		boolean isThai = LangManager.get().isThai(player);
		return isThai
			? "\u00A78\u0e41\u0e21\u0e1e \u00A77" + WorldData.MapName + " \u00A78\u2022 \u00A77\u0e42\u0e14\u0e22 " + WorldData.MapAuthor
			: "\u00A78Map \u00A77" + WorldData.MapName + " \u00A78\u2022 \u00A77by " + WorldData.MapAuthor;
	}

	private int getLegacyTextPixelWidth(String text)
	{
		int width = 0;
		boolean bold = false;

		for (int i = 0; i < text.length(); i++)
		{
			char character = text.charAt(i);

			if (character == '\u00A7' && i + 1 < text.length())
			{
				char formatCode = Character.toLowerCase(text.charAt(++i));

				if (formatCode == 'l')
				{
					bold = true;
				}
				else if ((formatCode >= '0' && formatCode <= '9') || (formatCode >= 'a' && formatCode <= 'f') || formatCode == 'r')
				{
					bold = false;
				}

				continue;
			}

			width += getChatCharacterWidth(character);

			if (bold && character != ' ')
			{
				width++;
			}

			width++;
		}

		return width;
	}

	private int getChatCharacterWidth(char character)
	{
		switch (character)
		{
			case ' ':
				return 3;
			case '!':
			case '\'':
			case ',':
			case '.':
			case ':':
			case ';':
			case 'i':
			case 'l':
			case '|':
				return 1;
			case '"':
			case '(':
			case ')':
			case '*':
			case '[':
			case ']':
			case 'I':
			case 't':
				return 3;
			case '<':
			case '>':
			case 'f':
			case 'k':
				return 4;
			case '@':
			case '~':
				return 6;
			default:
				return 5;
		}
	}

	private net.kyori.adventure.text.Component getPlayerSummaryLine(String rankTitle, Player p, String nameColor)
	{
		int kills = 0;
		int deaths = 0;
		if (GetStats().containsKey(p))
		{
			java.util.HashMap<String, Integer> pStats = GetStats().get(p);
			if (pStats.containsKey("Kills")) kills = pStats.get("Kills");
			if (pStats.containsKey("Deaths")) deaths = pStats.get("Deaths");
		}

		String hoverText = "\u00A77Kills: \u00A7a" + kills + "\n\u00A77Deaths: \u00A7c" + deaths + "\n\u00A78Click to message";

		return net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer.legacySection().deserialize(centerText(rankTitle + " \u00A78\u2503 " + nameColor + p.getName()) + "\n")
			.hoverEvent(net.kyori.adventure.text.event.HoverEvent.showText(net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer.legacySection().deserialize(hoverText)))
			.clickEvent(net.kyori.adventure.text.event.ClickEvent.suggestCommand("/tell " + p.getName() + " "));
	}

	public void Announce(String message)
	{
		if (message == null)
			return;
		
		Announce(message, true);
	}

	public void Announce(String message, boolean playSound)
	{
		for (Player player : UtilServer.getPlayers())
		{
			if (playSound)
				player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 1f, 1f);

			UtilPlayer.message(player, message);
		}

	}

	public boolean AdvertiseText(GameLobbyManager gameLobbyManager, int _advertiseStage)
	{
		return false;
	}

	public boolean CanThrowTNT(Location location)
	{
		return true;
	}

	@EventHandler
	public void HelpUpdate(UpdateEvent event)
	{
		if (_help == null || _help.length == 0)
			return;

		if (event.getType() != UpdateType.SEC)
			return;

		if (this.GetState() != GameState.Recruit)
			return;

		if (!UtilTime.elapsed(_helpTimer, 8000))
			return;

		if (_helpColor == ChatColor.YELLOW)
			_helpColor = ChatColor.GOLD;
		else
			_helpColor = ChatColor.YELLOW;

		_helpTimer = System.currentTimeMillis();

		String msg = "\u00A78\u00A7l\u00BB \u00A77" + com.houzicore.shared.common.util.UtilText.toSmallCaps("tip") + " \u00A78| " + _helpColor + _help[_helpIndex];

		for (Player player : UtilServer.getPlayers())
		{
			player.playSound(player.getLocation(), Sound.ENTITY_CHICKEN_EGG, 1f, 1f);

			UtilPlayer.message(player, msg);
		}

		_helpIndex = (_helpIndex + 1) % _help.length;
	}

	public void StartPrepareCountdown()
	{
		_prepareCountdown = true;
	}

	public boolean CanStartPrepareCountdown()
	{
		return _prepareCountdown;
	}

	@EventHandler
	public void TeamPlayerPlacement(PlayerStateChangeEvent event)
	{
		GameTeam team = GetTeam(event.GetPlayer());

		if (team != null)
			team.SetPlacement(event.GetPlayer(), event.GetState());
	}

	public void HandleTimeout()
	{
		SetState(GameState.End);
	}

	public void AddGemBooster(Player player)
	{
		if (!GemBoosterEnabled)
		{
			UtilPlayer.message(player, F.main("Game", "You cannot use " + F.elem("Gem Boosters")) + " for this game.");
			return;
		}
		
		if (GemBoosters.size() >= 4)
		{
			UtilPlayer.message(player, F.main("Game", "Games cannot have more than " + F.elem("4 Gem Boosters")) + ".");
			return;
		}

		if (GemBoosters.contains(player.getName()))
		{
			UtilPlayer.message(player, F.main("Game", "You can only use " + F.elem("1 Gem Booster")) + " per game.");
			return;
		}

		Announce(F.elem(player.getName()) + " used a " + F.elem(C.cGreen + "Gem Booster") + " for " + F.elem("+" + (100 - (GemBoosters.size() * 25)) + "% Gems") + "!");

		GemBoosters.add(player.getName());
	}

	public double GetGemBoostAmount()
	{
		if (GemBoosters.size() == 1) return 1;
		if (GemBoosters.size() == 2) return 1.75;
		if (GemBoosters.size() == 3) return 2.25;
		if (GemBoosters.size() == 4) return 2.5;

		return 0;
	}

	public void AddStat(Player player, String stat, int amount, boolean limitTo1, boolean global)
	{
		if (!Manager.IsRewardStats())
			return;

		if (!_stats.containsKey(player))
			_stats.put(player, new HashMap<String, Integer>());

		if (global)
			stat = "Global." + stat;
		else
			stat = GetName() + "." + stat;

		if (Manager.IsTournamentServer())
			stat += ".Tournament";

		int past = 0;
		if (_stats.get(player).containsKey(stat))
			past = _stats.get(player).get(stat);

		_stats.get(player).put(stat, limitTo1 ? Math.min(1, past + amount) : past + amount);
	}

	public abstract List<Player> getWinners();

	public abstract List<Player> getLosers();

	public NautHashMap<Player, HashMap<String, Integer>> GetStats()
	{
		return _stats;
	}

	public void registerStatTrackers(StatTracker<? extends Game>... statTrackers)
	{
		for (StatTracker<? extends Game> tracker : statTrackers)
		{
			if (_statTrackers.add(tracker))
				Bukkit.getPluginManager().registerEvents(tracker, Manager.getPlugin());
		}
	}

	

	public Collection<StatTracker<? extends Game>> getStatTrackers()
	{
		return _statTrackers;
	}

	@EventHandler
	public void onHangingBreak(HangingBreakEvent event)
	{
		event.setCancelled(true);
	}

	@EventHandler
	public void onHangingPlace(HangingPlaceEvent event)
	{
		event.setCancelled(true);
	}

	@EventHandler
	public void onDamageHanging(EntityDamageEvent event)
	{
		if (event.getEntity() instanceof Hanging)
		{
			event.setCancelled(true);
		}
	}

	public void deRegisterStats()
	{
		for (StatTracker<? extends Game> tracker : _statTrackers)
			HandlerList.unregisterAll(tracker);

		_statTrackers.clear();
	}

	public ArcadeManager getArcadeManager()
	{
		return Manager;
	}
	
//	@EventHandler
//	public void classCombatCreatureAllow(ClassCombatCreatureAllowSpawnEvent event)
//	{
//		CreatureAllowOverride = event.getAllowed();
//	}

	public boolean isInsideMap(Player player)
	{
		return isInsideMap(player.getLocation());
	}

	public boolean isInsideMap(Location loc)
	{
		return !(
			loc.getX() >= WorldData.MaxX + 1 ||
			loc.getX() <= WorldData.MinX ||
			loc.getZ() >= WorldData.MaxZ + 1 ||
			loc.getZ() <= WorldData.MinZ ||
			loc.getY() >= WorldData.MaxY + 1 ||
			loc.getY() <= WorldData.MinY);
	}

	public void setItemMerge(boolean itemMerge)
	{
		setItemMergeRadius(itemMerge ? 3.5 : 0);
	}

	public void setItemMergeRadius(double mergeRadius)
	{
		_itemMergeRadius = mergeRadius;
	}

	public double getItemMergeRadius()
	{
		return _itemMergeRadius;
	}

	@EventHandler
	public void applyItemMerge(WorldLoadEvent event)
	{
		// NMS removed in 1.21
	}

	public void setGame(GameType gameType, Player caller, boolean inform)
	{
		Manager.GetGameCreationManager().SetNextGameType(gameType);

		//End Current
		if (GetState() == GameState.Recruit)
		{
			SetState(GameState.Dead);

			if (gameType != null)
				Announce(C.cAqua + caller.getName() + " \u00A77\u0e40\u0e1b\u0e25\u0e35\u0e48\u0e22\u0e19\u0e40\u0e01\u0e21\u0e40\u0e1b\u0e47\u0e19 " + C.cYellow + gameType.GetName() + ".");
		}
		else
		{
			if (gameType != null)
				Announce(C.cAqua + caller.getName() + " \u00A77\u0e15\u0e31\u0e49\u0e07\u0e04\u0e48\u0e32\u0e40\u0e01\u0e21\u0e16\u0e31\u0e14\u0e44\u0e1b\u0e40\u0e1b\u0e47\u0e19 " + C.cYellow + gameType.GetName() + ".");
		}
	}
	
	public void endGame(GameTeam winningTeam) 
	{
	    AnnounceEnd(winningTeam);

        for (GameTeam team : GetTeamList())
        {
            if (WinnerTeam != null && team.equals(WinnerTeam))
            {
                for (Player player : team.GetPlayers(false))
                    AddGems(player, 10, "Winning Team", false, false);
            }

            for (Player player : team.GetPlayers(false))
                if (player.isOnline())
                    AddGems(player, 10, "Participation", false, false);
        }

        //End
        SetState(GameState.End);
	}

    public String GetBossBarText() { return null; }
    public double GetBossBarHealth() { return -1; }
    public org.bukkit.boss.BarColor GetBossBarColor() { return org.bukkit.boss.BarColor.BLUE; }
	
	@Override
	public String getLifecycleId() {
		return "GAME_" + GetName() + "_" + GetStateTime();
	}
}
