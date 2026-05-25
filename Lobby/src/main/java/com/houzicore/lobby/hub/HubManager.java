package com.houzicore.lobby.hub;

import java.util.ArrayList;
import java.util.HashMap;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;

import org.bukkit.entity.Egg;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.entity.Snowball;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.BlockDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.entity.ItemSpawnEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import io.papermc.paper.event.player.AsyncChatEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.event.player.PlayerVelocityEvent;
import org.bukkit.event.server.ServerListPingEvent;
import org.bukkit.event.world.ChunkLoadEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;
import org.bukkit.plugin.java.JavaPlugin;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.title.Title;
import java.time.Duration;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Scoreboard;

import com.houzicore.shared.core.lang.LangManager;
import com.houzicore.shared.MiniClientPlugin;
import com.houzicore.shared.account.CoreClient;
import com.houzicore.shared.account.CoreClientManager;
import com.houzicore.shared.core.achievement.AchievementManager;
import com.houzicore.shared.core.aprilfools.AprilFoolsManager;
import com.houzicore.shared.core.benefit.BenefitManager;
import com.houzicore.shared.core.blockrestore.BlockRestore;
import com.houzicore.shared.common.Rank;
import com.houzicore.shared.common.util.C;
import com.houzicore.shared.common.util.F;
import com.houzicore.shared.common.util.UtilAction;
import com.houzicore.shared.common.util.UtilAlg;
import com.houzicore.shared.common.util.UtilMath;
import com.houzicore.shared.common.util.UtilPlayer;
import com.houzicore.shared.common.util.UtilServer;
import com.houzicore.shared.common.util.UtilTime;
import com.houzicore.shared.common.util.UtilWorld;
import com.houzicore.shared.common.actionbar.ActionBarChannel;
import com.houzicore.shared.common.actionbar.ActionBarService;
import com.houzicore.shared.core.cosmetic.CosmeticManager;
import com.houzicore.shared.core.disguise.DisguiseManager;
import com.houzicore.shared.core.disguise.disguises.DisguiseSlime;
import com.houzicore.shared.core.donation.DonationManager;
import com.houzicore.shared.core.gadget.GadgetManager;
import com.houzicore.shared.core.gadget.event.GadgetActivateEvent;
import com.houzicore.shared.core.gadget.event.GadgetCollideEntityEvent;
import com.houzicore.shared.core.hologram.HologramManager;
import com.houzicore.shared.core.inventory.InventoryManager;
import com.houzicore.shared.core.itemstack.ItemStackFactory;
import com.houzicore.shared.core.message.PrivateMessageEvent;
import com.houzicore.shared.core.mount.MountManager;
import com.houzicore.shared.core.mount.event.MountActivateEvent;
import com.houzicore.shared.core.notifier.NotificationManager;
import com.houzicore.shared.core.party.Party;
import com.houzicore.shared.core.party.PartyManager;
import com.houzicore.shared.core.pet.PetManager;
import com.houzicore.shared.core.portal.Portal;
import com.houzicore.shared.core.preferences.PreferencesManager;
import com.houzicore.shared.core.projectile.ProjectileManager;
import com.houzicore.shared.core.stats.StatsManager;
import com.houzicore.shared.core.task.TaskManager;
import com.houzicore.shared.core.treasure.TreasureManager;
import com.houzicore.shared.core.displayentity.DisplayEntityManager;
import com.houzicore.shared.updater.UpdateType;
import com.houzicore.shared.updater.event.UpdateEvent;
import com.houzicore.lobby.hub.commands.ForcefieldRadius;
import com.houzicore.lobby.hub.commands.GadgetToggle;
import com.houzicore.lobby.hub.commands.GameModeCommand;
import com.houzicore.lobby.hub.commands.HelpCommand;
import com.houzicore.lobby.hub.commands.AdminPunchCommand;

import com.houzicore.lobby.hub.modules.ForcefieldManager;
import com.houzicore.lobby.hub.modules.AdminPunchManager;
import com.houzicore.lobby.hub.modules.HubVisibilityManager;
import com.houzicore.lobby.hub.modules.JumpManager;
import com.houzicore.lobby.hub.modules.NewsManager;
import com.houzicore.lobby.hub.modules.ParkourManager;
import com.houzicore.lobby.hub.modules.TabHeaderManager;
import com.houzicore.lobby.hub.modules.TextManager;
import com.houzicore.lobby.hub.modules.WorldManager;
import com.houzicore.shared.core.nametag.SubnameManager;
import com.houzicore.lobby.hub.modules.LeaderboardManager;
import com.houzicore.lobby.hub.modules.LeaderboardCommand;
import com.houzicore.lobby.hub.poll.PollManager;
import com.houzicore.lobby.hub.tutorial.TutorialManager;
// import com.houzicore.shared.core.classcombat.Skill.event.SkillTriggerEvent; // Not in Shared JAR
// import com.houzicore.shared.core.classcombat.item.event.ItemTriggerEvent; // Not in Shared JAR
// import com.houzicore.shared.core.condition.ConditionManager; // Not in Shared JAR
import com.houzicore.shared.core.damage.CustomDamageEvent;

public class HubManager extends MiniClientPlugin<HubClient>
{
	// ☃❅ Snowman!
	public HubType Type = HubType.Normal;
  
	private BlockRestore _blockRestore;
	private CoreClientManager _clientManager;
	private Object _conditionManager; // Was ConditionManager, not in Shared JAR
	private DonationManager _donationManager;
	private DisguiseManager _disguiseManager;
	private PartyManager _partyManager;   
	private ForcefieldManager _forcefieldManager; 
	private AdminPunchManager _adminPunchManager;
	private Portal _portal;  
	private StatsManager _statsManager; 
	private GadgetManager _gadgetManager;
	private MountManager _mountManager;
	private HubVisibilityManager _visibilityManager; 
	private TutorialManager _tutorialManager;  
	private TextManager _textCreator;
	private ParkourManager _parkour;   
	private PreferencesManager _preferences; 
	private InventoryManager _inventoryManager;
	private NewsManager _news;
	private AchievementManager _achievementManager;
	private TreasureManager _treasureManager;
	private PetManager _petManager;
	private com.houzicore.shared.core.music.RadioManager _radioManager;
	private com.houzicore.lobby.hub.bootstrap.LobbyTransitionCoordinator _transitionCoordinator;
	private com.houzicore.lobby.hub.modules.arena.ArenaManager _arenaManager;

	public com.houzicore.lobby.hub.modules.arena.ArenaManager getArenaManager() {
		return _arenaManager;
	}

	private Location _spawn;

	private String _pigStacker = "0 - Nobody"; 
	private String _serverName = "";
	 
	private ItemStack _ruleBook = null;

	private boolean _shuttingDown;

	private final java.util.Map<String, java.util.List<Location>> _mapData = new HashMap<>();

	public java.util.List<Location> getMapData(String key) {
		java.util.List<Location> result = _mapData.get(key);
		if (result != null && !result.isEmpty()) {
			return result;
		}
		if (key != null && key.startsWith("DATA_NAME:")) {
			String customKey = "CUSTOM_NAME:" + key.substring(10);
			return _mapData.getOrDefault(customKey, new ArrayList<>());
		}
		return new ArrayList<>();
	}

	public java.util.Map<String, java.util.List<Location>> getMapDataMap() {
		return _mapData;
	}

	public void reloadMapBuilderData() {
		_mapData.clear();
		loadMapBuilderData();
	}


	private void loadMapBuilderData() {
		try {
			java.io.File file = new java.io.File(org.bukkit.Bukkit.getWorldContainer(), "world/WorldConfig.dat");
			if (!file.exists()) {
                file = new java.io.File(getPlugin().getDataFolder().getParentFile(), "MapBuilderPlugin/Hub/WorldConfig.dat");
            }
			if (!file.exists()) {
				System.out.println("[HubManager] No WorldConfig.dat found for minigames setup.");
				return;
			}
			System.out.println("[HubManager] Parsing WorldConfig.dat for MiniGame Zones...");

			try (java.io.BufferedReader br = new java.io.BufferedReader(new java.io.FileReader(file))) {
				String line;
				String currentType = null;
				while ((line = br.readLine()) != null) {
					if (line.trim().isEmpty()) continue;
					String[] parts = line.split(":", 2);
					if (parts.length < 2) continue;
					String key = parts[0];
					String value = parts[1];
					
					if (key.startsWith("DATA_NAME") || key.startsWith("CUSTOM_NAME") || key.startsWith("TEAM_NAME")) {
						currentType = key + ":" + value;
					} else if (key.equals("DATA_LOCS") || key.equals("CUSTOM_LOCS") || key.equals("TEAM_SPAWNS")) {
						if (currentType != null) {
							java.util.List<Location> locs = _mapData.computeIfAbsent(currentType, k -> new ArrayList<>());
							for (String locStr : value.split(":")) {
								String[] coords = locStr.split(",");
								if (coords.length >= 3) {
									locs.add(new Location(org.bukkit.Bukkit.getWorlds().get(0), Integer.parseInt(coords[0]), Integer.parseInt(coords[1]), Integer.parseInt(coords[2])));
								}
							}
						}
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private HashMap<String, Long> _portalTime = new HashMap<String, Long>();
	
	private HashMap<String, ArrayList<String>> _creativeAdmin = new HashMap<String, ArrayList<String>>();

	//Admin
	private LeaderboardManager _leaderboardManager;
	private HologramManager _hologramManager;
	private boolean _gadgetsEnabled = true;

	private com.houzicore.shared.core.level.LvlManager _lvlManager;

	public HubManager(JavaPlugin plugin, BlockRestore blockRestore, CoreClientManager clientManager, DonationManager donationManager, Object conditionManager, DisguiseManager disguiseManager, TaskManager taskManager, Portal portal, PartyManager partyManager, PreferencesManager preferences, PetManager petManager, PollManager pollManager, StatsManager statsManager, AchievementManager achievementManager, com.houzicore.shared.core.level.LvlManager lvlManager, HologramManager hologramManager, DisplayEntityManager displayEntityManager)
	{
		super("Hub Manager", plugin);

		_lvlManager = lvlManager;
		_blockRestore = blockRestore;
		_clientManager = clientManager;
		_conditionManager = conditionManager;
		_donationManager = donationManager;
		_disguiseManager = disguiseManager;
		_hologramManager = hologramManager;
		
		_portal = portal;

		org.bukkit.World world = UtilWorld.getWorld("world");
		_spawn = world.getSpawnLocation().add(0.5, 0.1, 0.5);
		// Disable item merging
		// ((CraftWorld) _spawn.getWorld()).getHandle().spigotConfig.itemMerge = 0; // Removed NMS dependency

		_textCreator = new TextManager(this, hologramManager);
		_parkour = new ParkourManager(this, donationManager, taskManager);

		new WorldManager(this);
		new JumpManager(this);
		//new TournamentInviter(this);
		    
		_news = new NewsManager(this);
        new com.houzicore.lobby.hub.modules.HubFurniture(this, displayEntityManager);

		_mountManager = new MountManager(_plugin, clientManager, donationManager, blockRestore, _disguiseManager, com.houzicore.lobby.hub.bootstrap.LobbyBootstrap.getInstance().getFeatureGate());
		_inventoryManager = new InventoryManager(plugin, clientManager);
		new BenefitManager(plugin, clientManager, _inventoryManager);
		_gadgetManager = new GadgetManager(_plugin, clientManager, donationManager, _inventoryManager, _mountManager, petManager, preferences, disguiseManager, blockRestore, new ProjectileManager(plugin), com.houzicore.lobby.hub.bootstrap.LobbyBootstrap.getInstance().getFeatureGate());
		_treasureManager = new TreasureManager(_plugin, clientManager, donationManager, _inventoryManager, petManager, _blockRestore, hologramManager, preferences, displayEntityManager);
		new CosmeticManager(_plugin, clientManager, donationManager, _inventoryManager, _gadgetManager, _mountManager, petManager, _treasureManager);
		SubnameManager subnameManager = new SubnameManager(_plugin, clientManager);
		subnameManager.setSubnameProvider(target -> {
			if (this._queueManager != null && this._queueManager.isQueued(target)) {
				return net.kyori.adventure.text.Component.text("§aกำลังหาห้องอยู่...");
			}
			com.houzicore.shared.core.party.Party party = partyManager != null ? partyManager.getPartyByPlayer(target) : null;
			if (party != null) {
				return net.kyori.adventure.text.Component.text("§dParty ของ " + party.getLeaderName());
			}
			
			try {
				if (com.houzicore.shared.core.title.TitleManager.Instance != null) {
					com.houzicore.shared.core.title.TitleType equippedTitle = com.houzicore.shared.core.title.TitleManager.Instance.getEquippedTitle(target);
					if (equippedTitle != null) {
						String lang = com.houzicore.shared.core.lang.LangManager.get().isThai(target) ? "TH" : "EN";
						String formatted = com.houzicore.shared.core.title.TitleManager.Instance.getFormattedTitle(target, lang);
						if (formatted != null && !formatted.isEmpty()) {
							return net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer.legacySection().deserialize(formatted);
						}
					}
				}
			} catch (Exception ignored) {}
			
			return net.kyori.adventure.text.Component.empty();
		});
		// Initialization moved to before DailyLoginNpcModule
		new com.houzicore.lobby.hub.modules.AfkManager(this);
		new com.houzicore.lobby.hub.modules.ParkourTimerManager(this);
		new com.houzicore.lobby.hub.modules.PlayerProfileManager(this, statsManager, clientManager, _lvlManager);
		new com.houzicore.lobby.hub.modules.NpcQueueManager(this, hologramManager);

		_petManager = petManager;
		_partyManager = partyManager;
		_preferences = preferences;
		_tutorialManager = new TutorialManager(this, donationManager, taskManager, _textCreator);
		_visibilityManager = new HubVisibilityManager(this);
		
		_forcefieldManager = new ForcefieldManager(this);
		addCommand(new ForcefieldRadius(_forcefieldManager));
		
		_adminPunchManager = new AdminPunchManager(this);
		addCommand(new AdminPunchCommand(_adminPunchManager));
		
		_leaderboardManager = new LeaderboardManager(this, getMapData("DATA_NAME:LEADERBOARD"));
		addCommand(new LeaderboardCommand(_leaderboardManager));
		addCommand(new com.houzicore.lobby.hub.command.SpawnModelCommand(this, displayEntityManager));
		addCommand(new com.houzicore.lobby.hub.command.HaloCommand(this, displayEntityManager));
		addCommand(new com.houzicore.shared.core.displayentity.command.GiveFurnitureCommand(displayEntityManager));
		addCommand(new com.houzicore.shared.core.displayentity.command.FurnitureBrowserCommand(displayEntityManager));
		
		_statsManager = statsManager;
		_achievementManager = achievementManager;
		/*
		 * Omit default GUI items in hotbar
		//_achievementManager.setGiveInterfaceItem(true);
		*/
		
		new NotificationManager(getPlugin(), clientManager);
		// ChatTagManager removed to allow unified chat prefix (T-C69) to take over
		// HubScoreboardManager is created in Hub.java (with friendManager) — do NOT duplicate here

		// Tab Header/Footer — animated gradient brand with per-player rank footer
		new TabHeaderManager(plugin, clientManager);

		_radioManager = new com.houzicore.shared.core.music.RadioManager(plugin, _preferences);
		java.io.File musicDir = new java.io.File(plugin.getDataFolder(), "music");
		_radioManager.loadSongs(musicDir);
		com.houzicore.lobby.hub.ui.radio.RadioShop radioShop = new com.houzicore.lobby.hub.ui.radio.RadioShop(this, _clientManager, _donationManager, _radioManager);
		addCommand(new com.houzicore.lobby.hub.commands.RadioCommand(this, _radioManager, radioShop));

		// ── Load MapBuilder Data ──
		loadMapBuilderData();

		// ── Leaderboard Holograms (MapBuilder: DATA_NAME:HOLO_LEADER) ──
		// Disabled: Holograms hardcoded with Location in the code have been removed as per request.


		// ── Treasure Locations (MapBuilder: DATA_NAME:TREASURE) ──
		java.util.List<Location> treasureLocs = getMapData("DATA_NAME:TREASURE");
		if (!treasureLocs.isEmpty()) {
			for (Location loc : treasureLocs) {
				_treasureManager.addLocation(loc);
			}
			System.out.println("[HubManager] Loaded " + treasureLocs.size() + " treasure locations from WorldConfig.dat");
		} else {
			System.out.println("[HubManager] No TREASURE points in WorldConfig.dat");
		}

		// ── Lobby Spawn Location (MapBuilder: DATA_NAME:SPAWN or DATA_NAME:LOBBY_SPAWN) ──
		java.util.List<Location> spawnLocs = getMapData("DATA_NAME:SPAWN");
		if (spawnLocs.isEmpty()) {
			spawnLocs = getMapData("DATA_NAME:LOBBY_SPAWN");
		}
		if (!spawnLocs.isEmpty()) {
			_spawn = spawnLocs.get(0);
			System.out.println("[HubManager] Loaded Lobby Spawn location from WorldConfig.dat: " + _spawn);
		} else {
			// Try to set to highest block at world spawn X, Z to prevent underground spawning
			org.bukkit.World w = _spawn.getWorld();
			int highY = w.getHighestBlockYAt(_spawn.getBlockX(), _spawn.getBlockZ());
			if (highY > org.bukkit.Bukkit.getWorlds().get(0).getMinHeight() + 10) {
				_spawn = new Location(w, _spawn.getBlockX() + 0.5, highY + 1.1, _spawn.getBlockZ() + 0.5, _spawn.getYaw(), _spawn.getPitch());
				world.setSpawnLocation(_spawn.getBlockX(), _spawn.getBlockY(), _spawn.getBlockZ());
				System.out.println("[HubManager] Adjusted hardcoded spawn to highest block: " + _spawn);
			}
		}

		// ── Lobby NPCs ──
		com.houzicore.lobby.hub.modules.LobbyNpcManager npcManager = new com.houzicore.lobby.hub.modules.LobbyNpcManager(this, hologramManager);
		new com.houzicore.lobby.hub.modules.KeeperOfRewardModule(this, npcManager, getMapData("DATA_NAME:NPC_KEEPER"));
		com.houzicore.lobby.hub.modules.DailyLoginManager dailyLoginManager = new com.houzicore.lobby.hub.modules.DailyLoginManager(this, statsManager);
		new com.houzicore.lobby.hub.modules.DailyLoginNpcModule(this, npcManager, getMapData("DATA_NAME:NPC_DAILY_LOGIN"), dailyLoginManager);

		// ── Lobby Mini-Games ──
		new com.houzicore.lobby.hub.modules.fishing.FishingManager(this, donationManager, statsManager, clientManager, npcManager);
		new com.houzicore.lobby.hub.modules.nonstop.NonstopParkourManager(this, donationManager, statsManager, npcManager, hologramManager);
		_arenaManager = new com.houzicore.lobby.hub.modules.arena.ArenaManager(this, donationManager, statsManager, npcManager);
		new com.houzicore.lobby.hub.modules.farm.FarmSimManager(this, donationManager, statsManager, npcManager);
		new com.houzicore.lobby.hub.modules.WaterfallParticleManager(this);

		// ── Dynamic 15 Slot Avatar System ──
		org.bukkit.World npcWorld = _spawn.getWorld();
		com.houzicore.lobby.hub.modules.AvatarNpcManager avatarNpcManager = new com.houzicore.lobby.hub.modules.AvatarNpcManager(this.getPlugin(), hologramManager, npcManager);
		
		java.util.List<Location> avatarLocs = getMapData("DATA_NAME:NPC_AVATAR");
		if (!avatarLocs.isEmpty()) {
			int slot = 1;
			for (Location loc : avatarLocs) {
				avatarNpcManager.registerSlotLocation(slot++, loc);
			}
			System.out.println("[HubManager] Loaded " + avatarLocs.size() + " Avatar NPC slots from WorldConfig.dat");
		} else {
			System.out.println("[HubManager] No NPC_AVATAR points in WorldConfig.dat");
		}


		// ── Context-Driven Transition Coordinator ──
		_transitionCoordinator = new com.houzicore.lobby.hub.bootstrap.LobbyTransitionCoordinator(
			com.houzicore.lobby.hub.bootstrap.LobbyBootstrap.getInstance().getContextService(),
			com.houzicore.lobby.hub.bootstrap.LobbyBootstrap.getInstance().getSnapshotService(),
			com.houzicore.lobby.hub.bootstrap.LobbyBootstrap.getInstance().getPlayerStateApplier(),
			_gadgetManager, _petManager, _mountManager, clientManager
		);
		org.bukkit.Bukkit.getPluginManager().registerEvents(_transitionCoordinator, plugin);
		
		_ruleBook = ItemStackFactory.Instance.CreateStack(Material.WRITTEN_BOOK, (byte)0, 1, ChatColor.GREEN + "Rule Book", new String[] { });
		BookMeta meta = (BookMeta)_ruleBook.getItemMeta();
		_serverName = getPlugin().getConfig().getString("serverstatus.name");
		_serverName = _serverName.substring(0, Math.min(16,  _serverName.length()));

		meta.addPage("§m-------------------§r\n"
				+ "Welcome to §6§l" + com.houzicore.shared.core.common.BrandConfig.mainServerName() + "§r\n"
				+ "§r§0§l§r§m§0§m-------------------§r§0\n"
				+ "\n"
				+ "§2Please §0take a moment to read through this book!\n"
				+ "\n"
				+ "\n"
				+ "Part 1 - Rules\n"
				+ "\n"
				+ "Part 2 - FAQ\n");

		meta.addPage("§m-------------------\n"
				+ "§r          §2§lRules§r§0\n"
				+ "§m-------------------\n"
				+ "§r\n"
				+ "§l1.§r §4No§r spamming.\n"
				+ "\n"
				+ "§0This is sending too many messages and/or repeating the same message in a short period of time.\n");

		meta.addPage("§m-------------------\n"
				+ "§r          §2§lRules§r§0\n"
				+ "§m-------------------\n"
				+ "§r\n"
				+ "§l2.§m§r §4No§0 use of excessive caps.\n"
				+ "\n"
				+ "This is sending messages with an excessive amount of capital letters.\n");

		meta.addPage("§m-------------------\n"
				+ "§r          §2§lRules§r§0\n"
				+ "§m-------------------\n"
				+ "§r\n"
				+ "§l3.§r §4No§0 hacking or use of any unapproved mods.\n"
				+ "\n"
				+ "This means we do not tolerate any sort of hacked client or any unapproved mods, such as fly hacks.\n");

		meta.addPage("§m-------------------\n"
				+ "§r          §2§lRules§r§0\n"
				+ "§m-------------------\n"
				+ "§r\n"
				+ "§l4.§r §4No§0 advertising non-" + com.houzicore.shared.core.common.BrandConfig.mainServerName() + " related links.\n"
				+ "\n"
				+ "This is when a link is sent in chat which directs others to non-" + com.houzicore.shared.core.common.BrandConfig.mainServerName() + " related content.\n");

		meta.addPage("§m-------------------\n"
				+ "§r          §2§lRules§r§0\n"
				+ "§m-------------------\n"
				+ "§r\n"
				+ "§l5.§r §4No§0 trolling or use of any exploits.\n"
				+ "\n"
				+ "This means that abuse of bugs/glitches is not tolerated. You also may not do things such as teamkilling and/or blocking spawns.\n");

		meta.addPage("§m-------------------\n"
				+ "§r          §2§lRules§r§0\n"
				+ "§m-------------------\n"
				+ "§r\n"
				+ "§l6.§r §2Be§0 respectful to others, yourself, and the environment around you.\n");

		meta.addPage("§m-------------------\n"
				+ "§r          §2§lRules§r§0\n"
				+ "§m-------------------\n"
				+ "§r\n"
				+ "§rPlease report any bugs, exploits, and/or rule breakers to a staff member with evidence.\n");

		meta.addPage("§m-------------------\n"
				+ "§r          §2§lFAQ§r§0\n"
				+ "§m-------------------\n"
				+ "§r\n"
				+ "§lWhat is stacker and how do you play it?\n"
				+ "\n"
				+ "§rStacker is a hub game where you can stack & throw players/mobs.\n"
				+ "\n"
				+ "§9Right-Click: pick up\n"
				+ "Left-Click: throw\n");

		meta.addPage("§m-------------------\n"
				+ "§r          §2§lFAQ§r§0\n"
				+ "§m-------------------\n"
				+ "§r\n"
				+ "§lHow do I get §bUltra§l, §5Hero§l, or §aLegend§l?\n"
				+ "\n"
				+ "§r§0You are able to obtain these ranks from a server administrator.\n");

		meta.addPage("§m-------------------\n"
				+ "§r          §2§lFAQ§r§0\n"
				+ "§m-------------------\n"
				+ "§r\n"
				+ "§lWhy hasn't my rank been applied yet?\n"
				+ "\n"
				+ "§m§rYour rank may take a while to be applied. Please contact an administrator for assistance.\n");

		meta.addPage("§m-------------------\n"
				+ "§r          §2§lFAQ§r§0\n"
				+ "§m-------------------\n"
				+ "§r\n"
				+ "§lWhat do I do if I was wrongfully punished?\n"
				+ "\n"
				+ "§0If you believe you were wrongfully punished, please contact an administrator for an appeal.\n");

		meta.addPage("§m-------------------\n"
				+ "§r   §6§lThank you for \n"
				+ "      reading!§r§0\n"
				+ "§m-------------------\n"
				+ "§r\n"
				+ "We hope you enjoy your time on §6§l" + com.houzicore.shared.core.common.BrandConfig.mainServerName() + "§0!\n"
				+ "\n"
				+ "\n"
				+ "§c§lH§6§lA§a§lV§9§lE §c§lF§6§lU§a§lN§9§l!\n");

		// These are needed or 1.8 clients will not show book correctly
		meta.setTitle("Rule Book");
		meta.setAuthor(com.houzicore.shared.core.common.BrandConfig.mainServerName().toUpperCase());

		_ruleBook.setItemMeta(meta);
	}

	@Override
	public void addCommands()
	{
		addCommand(new com.houzicore.lobby.hub.commands.GadgetToggle(this));
		addCommand(new com.houzicore.lobby.hub.commands.GameModeCommand(this));
		addCommand(new com.houzicore.lobby.hub.commands.HelpCommand(this));
		addCommand(new com.houzicore.lobby.hub.commands.CtxCommand(this));
	}

	public com.houzicore.lobby.hub.bootstrap.LobbyTransitionCoordinator getTransitionCoordinator() {
		return _transitionCoordinator;
	}

	public com.houzicore.shared.core.level.LvlManager getLevelManager() {
		return _lvlManager;
	}

	public PartyManager getPartyManager() {
		return _partyManager;
	}

	public boolean isAdminBuilder(Player player) {
		if (player.getGameMode() == org.bukkit.GameMode.CREATIVE) return true;

		try {
			com.houzicore.shared.api.context.PlayerContextId ctx = com.houzicore.lobby.hub.bootstrap.LobbyBootstrap.getInstance().getContextService().getCurrentContextId(player);
			if (ctx == com.houzicore.shared.api.context.PlayerContextId.MAP_EDIT || ctx == com.houzicore.shared.api.context.PlayerContextId.MAP_PREVIEW) {
				return true;
			}
		} catch (Exception e) {}

		try {
			com.houzicore.shared.core.npc.NpcManager npcManager = com.houzicore.shared.core.plugin.PluginRegistry.require(com.houzicore.shared.core.npc.NpcManager.class);
			if (npcManager != null && npcManager.isBuilder(player)) {
				return true;
			}
		} catch (Exception e) {}

		return false;
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	public void reflectMotd(ServerListPingEvent event)
	{
		if (_shuttingDown)
		{
			event.setMotd("§c§lRestarting soon...");
		}
		else
		{
			event.setMotd("§6§l" + com.houzicore.shared.core.common.BrandConfig.mainServerName() + " §r§7- §fMini-Games Network\n§aJoin now and have fun!");
		}
	}
	
	public boolean BumpDisabled(Entity ent)
	{
		if (ent == null)
			return false;

		if (ent instanceof Player)
		{
			return !_preferences.Get((Player)ent).HubGames;
		}
		
		return true;
	}

	
	@EventHandler
	public void SnowballPickup(BlockDamageEvent event)
	{
		if (Type != HubType.Christmas)
			return;
		
		if (event.getBlock().getType() != Material.SNOW)
			return;
		
		Player player = event.getPlayer();
		
		_gadgetManager.RemoveItem(player);
		
		player.getInventory().setItem(3, new ItemStack(Material.SNOWBALL, 16));
	}
	
	@EventHandler
	public void SnowballHit(CustomDamageEvent event)
	{
		if (Type != HubType.Christmas)
			return;
		
		org.bukkit.entity.Projectile proj = event.GetProjectile();
		if (proj == null)	return;

		if (!(proj instanceof org.bukkit.entity.Snowball))
			return;
		
		event.SetCancelled("Snowball Cancel");
		
		if (BumpDisabled(event.GetDamageeEntity()))
			return;
		
		if (BumpDisabled(event.GetDamagerEntity(true)))
			return;
		
		com.houzicore.shared.common.util.UtilAction.velocity(event.GetDamageeEntity(), com.houzicore.shared.common.util.UtilAlg.getTrajectory2d(event.GetDamagerEntity(true), event.GetDamageeEntity()), 
				0.4, false, 0, 0.2, 1, false);
		
		//No Portal
		SetPortalDelay(event.GetDamageeEntity());
	}
	
	@EventHandler
	public void redirectStopCommand(PlayerCommandPreprocessEvent event)
	{
		if (event.getPlayer().isOp() && event.getMessage().equalsIgnoreCase("/stop"))
		{
			_shuttingDown = true;

			Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(_plugin, new Runnable()
			{
				public void run()
				{
					_portal.sendAllPlayers("Lobby");

					Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(_plugin, new Runnable()
					{
						public void run()
						{
							Bukkit.shutdown();
						}
					}, 40L);
				}
			}, 60L);

			event.setCancelled(true);
		}
	}

	@EventHandler
	public void preventEggSpawn(ItemSpawnEvent event)
	{
		if (event.getEntity() instanceof Egg)
		{
			event.setCancelled(true);
		}
	}

	@EventHandler(priority = EventPriority.MONITOR)
	public void OnChunkLoad(ChunkLoadEvent event)
	{
		for (Entity entity : event.getChunk().getEntities())
		{
			if (entity instanceof LivingEntity)
			{
				if (((LivingEntity)entity).isCustomNameVisible() && ((LivingEntity)entity).getCustomName() != null)
				{
					if (ChatColor.stripColor(((LivingEntity)entity).getCustomName()).equalsIgnoreCase("Prop Rush"))
					{
						DisguiseSlime disguise = new DisguiseSlime(entity);
						// disguise.setCustomNameVisible(true); // Method not in DisguiseSlime
//						disguise.setName(((LivingEntity)entity).getCustomName(), null);
						// disguise.SetSize(2);
						_disguiseManager.disguise(disguise);
					}
				}
			}
		}
	}

	@EventHandler(priority = EventPriority.LOW)
	public void login(final PlayerLoginEvent event)
	{
        CoreClient client = _clientManager.Get(event.getPlayer().getName());

        // Reserved Slot Check
		if (Bukkit.getOnlinePlayers().size() - Bukkit.getServer().getMaxPlayers() >= 20)
		{
			if (!client.GetRank().Has(Rank.WARRIOR))
			{
				Bukkit.getScheduler().scheduleSyncDelayedTask(getPlugin(), new Runnable()
				{
					public void run()
					{
						_portal.sendPlayerToServer(event.getPlayer(), "Lobby");
					}
				});

				event.allow();
			}
		}
		else
			event.allow();
	}

	@EventHandler(priority = EventPriority.LOW)
	public void AdminOP(PlayerJoinEvent event)
	{
		// Give developers operator on their servers
		boolean testServer = _plugin.getConfig().getString("serverstatus.group").equalsIgnoreCase("Testing");

		if (_clientManager.Get(event.getPlayer()).GetRank().Has(Rank.OWNER) || (testServer && (_clientManager.Get(event.getPlayer()).GetRank().Has(Rank.DEVELOPER) || _clientManager.Get(event.getPlayer()).GetRank() == Rank.JNR_DEV)))
			event.getPlayer().setOp(true);
		else
			event.getPlayer().setOp(false);
	}
	
	@EventHandler
	public void PlayerRespawn(PlayerRespawnEvent event)
	{
		event.setRespawnLocation(GetSpawn());
	}

	@EventHandler(priority = EventPriority.LOW)
	public void PlayerJoin(PlayerJoinEvent event)
	{
		Player player = event.getPlayer();
		String playerName = player.getName();

		// April Fools
		if (AprilFoolsManager.Instance.isActive())
			playerName = AprilFoolsManager.Instance.getName(player);

		//Public Message
		event.setJoinMessage(null);
		
		com.houzicore.shared.common.Rank rank = _clientManager.Get(player).GetRank();
		if (rank != com.houzicore.shared.common.Rank.ALL)
		{
			for (Player p : org.bukkit.Bukkit.getOnlinePlayers())
			{
				p.sendMessage(LangManager.get().get(p, "hub.join.announce", rank.GetTag(true, true), player.getName()));
			}
		}
		
		//Teleport
		player.teleport(GetSpawn());
		
		//Survival
		player.setGameMode(GameMode.SURVIVAL);
		
		// Route the join inventory through the Lobby runtime state instead of a raw wipe.
		com.houzicore.lobby.hub.bootstrap.LobbyBootstrap.getInstance()
			.getPlayerStateApplier()
			.applyContextState(player, com.houzicore.shared.api.context.PlayerContextId.LOBBY_FREE);
		
		//Allow Double Jump
		player.setAllowFlight(true);
		
		//Health
		player.setHealth(20);

		//Rules
		//player.getInventory().setItem(2, _ruleBook);

		//Game Menu (Compass)
		player.getInventory().setItem(0, ItemStackFactory.Instance.CreateStack(Material.COMPASS, (byte)0, 1, 
			com.houzicore.shared.common.util.HouziColorParser.parse(LangManager.get().get(player, "hub.item.game_menu")), 
			new String[]{LangManager.get().get(player, "hub.item.game_menu.lore1"), LangManager.get().get(player, "hub.item.game_menu.lore2")}));
		
		//My Profile (Player Head)
		org.bukkit.inventory.ItemStack profileHead = new com.houzicore.shared.core.itemstack.ItemBuilder(Material.PLAYER_HEAD)
			.setTitle(com.houzicore.shared.common.util.HouziColorParser.parse(LangManager.get().get(player, "hub.item.profile")))
			.setPlayerHead(playerName)
			.build();
		player.getInventory().setItem(1, profileHead);
		
		startJoinLoadingSequence(player);

		// Welcome Experience
		String displayServerName = org.bukkit.ChatColor.stripColor(com.houzicore.shared.core.common.BrandConfig.mainServerName());
		String rawTitle = com.houzicore.shared.common.util.HouziColorParser.parse(LangManager.get().get(player, "hub.join.title", playerName, displayServerName));
		String rawSubtitle = com.houzicore.shared.common.util.HouziColorParser.parse(LangManager.get().get(player, "hub.join.subtitle", playerName, displayServerName));
		Component compTitle = net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer.legacySection().deserialize(rawTitle);
		Component compSub = net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer.legacySection().deserialize(rawSubtitle);
		
		net.kyori.adventure.title.Title.Times times = net.kyori.adventure.title.Title.Times.times(
			java.time.Duration.ofMillis(500), 
			java.time.Duration.ofMillis(4000), 
			java.time.Duration.ofMillis(1000)
		);
		player.showTitle(net.kyori.adventure.title.Title.title(compTitle, compSub, times));

		// Premium Entrance Effects
		player.playSound(player.getLocation(), org.bukkit.Sound.UI_TOAST_CHALLENGE_COMPLETE, 1.0f, 1.4f);
		player.playSound(player.getLocation(), org.bukkit.Sound.ENTITY_PLAYER_LEVELUP, 1.0f, 1.1f);
		
		Location particleLoc = player.getLocation().add(0, 1.0, 0);
		com.houzicore.shared.common.util.UtilParticle.PlayParticleToAll(
			com.houzicore.shared.common.util.UtilParticle.ParticleType.HAPPY_VILLAGER,
			particleLoc, 0.5f, 0.5f, 0.5f, 0.1f, 30,
			com.houzicore.shared.common.util.UtilParticle.ViewDist.NORMAL
		);
		com.houzicore.shared.common.util.UtilParticle.PlayParticleToAll(
			com.houzicore.shared.common.util.UtilParticle.ParticleType.FIREWORKS_SPARK,
			particleLoc, 0.5f, 0.5f, 0.5f, 0.1f, 30,
			com.houzicore.shared.common.util.UtilParticle.ViewDist.NORMAL
		);

		final String finalPlayerName = playerName;
		final String finalDisplayServerName = displayServerName;
		org.bukkit.Bukkit.getScheduler().runTaskLater(getPlugin(), () -> {
			if (!player.isOnline()) return;
			for (int i = 1; i <= 13; i++) {
				String chatKey = "hub.welcome_chat.line" + i;
				String lang = com.houzicore.shared.core.lang.LangManager.get().resolveLocaleStr(player);
				java.util.Map<String, String> flatMap = com.houzicore.shared.core.lang.LangManager.get().flat(lang);
				String rawChat = flatMap != null ? flatMap.get(chatKey) : null;
				if (rawChat == null) rawChat = com.houzicore.shared.core.lang.LangManager.get().flat("ENG").get(chatKey);
				
				if (rawChat == null || rawChat.isEmpty() || rawChat.equals(chatKey)) {
					player.sendMessage("");
				} else {
					String finalChat = rawChat.replace("{0}", finalPlayerName).replace("{1}", finalDisplayServerName).replace("{2}", com.houzicore.shared.core.common.BrandConfig.website());
					// Center the message using pixel-width calculation
					String centered = com.houzicore.shared.common.util.UtilCenterChat.centerMiniMessage(finalChat);
					player.sendMessage(com.houzicore.shared.core.chat.Chat.replaceObjectTags(
						net.kyori.adventure.text.minimessage.MiniMessage.miniMessage().deserialize(centered)
					));
				}
			}
		}, 20L);
	}

	private void startJoinLoadingSequence(Player player)
	{
		String[] loadingKeys = new String[] {
				"loading.profile",
				"loading.stats",
				"loading.achievements",
				"loading.cosmetics",
				"loading.game_data"
		};

		for (int i = 0; i < loadingKeys.length; i++)
		{
			final int delay = 10 + (i * 12);
			final String key = loadingKeys[i];
			Bukkit.getScheduler().runTaskLater(_plugin, () ->
			{
				if (player.isOnline())
				{
					ActionBarService.display(
							player,
							ActionBarChannel.SYSTEM_ALERT,
							Component.text("• ", NamedTextColor.AQUA)
									.append(Component.text(LangManager.get().get(player, key), NamedTextColor.WHITE)),
							1800L);
				}
			}, delay);
		}

		Bukkit.getScheduler().runTaskLater(_plugin, () ->
		{
			if (player.isOnline())
			{
				ActionBarService.display(
						player,
						ActionBarChannel.SYSTEM_ALERT,
						Component.text(LangManager.get().get(player, "loading.ready"), NamedTextColor.GREEN, TextDecoration.BOLD),
						2200L);
			}
		}, 74L);
	}

	@EventHandler
	public void PlayerDeath(PlayerDeathEvent event)
	{
		// Safety net — should rarely fire since all damage is cancelled
		event.setDeathMessage(null);
		event.getDrops().clear();
		event.setKeepInventory(true);
	}

	@EventHandler
	public void PlayerQuit(PlayerQuitEvent event)
	{
		event.setQuitMessage(null);
		
		Player player = event.getPlayer();
		com.houzicore.shared.common.Rank rank = _clientManager.Get(player).GetRank();
		if (rank != com.houzicore.shared.common.Rank.ALL)
		{
			for (Player p : org.bukkit.Bukkit.getOnlinePlayers())
			{
				p.sendMessage(LangManager.get().get(p, "hub.quit.announce", rank.GetTag(true, true), player.getName()));
			}
		}

		player.leaveVehicle();
		player.eject();

		for (Player p : UtilServer.getPlayers())
			p.getScoreboard().resetScores(player.getName());

		_portalTime.remove(event.getPlayer().getName());
	}
	
	@EventHandler
	public void playerPrivateMessage(PrivateMessageEvent event)
	{
		//Dont Let PM Near Spawn!
		if (UtilMath.offset2d(GetSpawn(), event.getSender().getLocation()) == 0 && !_clientManager.Get(event.getSender()).GetRank().Has(Rank.HELPER))
		{
			UtilPlayer.message(event.getSender(), F.main("Chat", LangManager.get().get(event.getSender(), "hub.chat.spawn_lock_pm")));
			event.setCancelled(true);
		}
	}

	@EventHandler(priority = org.bukkit.event.EventPriority.LOW)
	public void PlayerChatLock(AsyncPlayerChatEvent event)
	{
		if (event.isCancelled())
			return;
		
		//Dont Let Chat Near Spawn!
		if (UtilMath.offset2d(GetSpawn(), event.getPlayer().getLocation()) == 0 && !_clientManager.Get(event.getPlayer()).GetRank().Has(Rank.HELPER))
		{
			UtilPlayer.message(event.getPlayer(), F.main("Chat", LangManager.get().get(event.getPlayer(), "hub.chat.spawn_lock")));
			event.setCancelled(true);
			return;
		}
	}

	@EventHandler(priority = org.bukkit.event.EventPriority.HIGH, ignoreCancelled = true)
	public void PlayerChatRender(AsyncChatEvent event)
	{
		Player player = event.getPlayer();
		String playerName = player.getName();
		
		// April Fools
		if (AprilFoolsManager.Instance.isActive())
			playerName = AprilFoolsManager.Instance.getName(player);

		String unifiedPrefix = com.houzicore.shared.core.chat.Chat.getExtChatPrefix(player);
		final String rawMessage = net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer.legacySection().serialize(event.message());

		//Party Chat
		if (rawMessage.length() > 0 && rawMessage.charAt(0) == '@')
		{
			Party party = _partyManager.getPartyByPlayer(player);
			if (party != null)
			{
				String finalMessage = rawMessage.substring(1);
				event.viewers().clear();

				for (String name : party.GetPlayers())
				{
					Player other = UtilPlayer.searchExact(name);
					if (other != null)
						event.viewers().add(other);
				}

				// Render Party Format
				final String finalName = playerName;
				event.renderer((source, sourceDisplayName, message, viewer) -> {
					net.kyori.adventure.text.Component prefixComp = com.houzicore.shared.core.chat.Chat.getChatPrefixComponent(source);
					net.kyori.adventure.text.Component partyPrefix = net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer.legacySection().deserialize(C.cDPurple + C.Bold + "Party " + C.cWhite + C.Bold + finalName + " " + C.cPurple);
					return prefixComp.append(partyPrefix).append(net.kyori.adventure.text.Component.text(finalMessage));
				});
			}
			else
			{
				UtilPlayer.message(player, F.main("Party", "You are not in a Party."));
				event.setCancelled(true);
			}

			return;
		} 
		else
		{
			// Remove tutorial players
			for (Player other : UtilServer.getPlayers())
			{
				if (_tutorialManager.InTutorial(other))
				{
					event.viewers().remove(other);
				}
			}

			final String finalName = playerName;
			event.renderer((source, sourceDisplayName, message, viewer) -> {
				return com.houzicore.shared.core.chat.Chat.formatChat(source, finalName, message);
			});
		}
	}



	@EventHandler
	public void Damage(CustomDamageEvent event)
	{
		// Cancel all damage in the Hub
		event.SetCancelled("Hub Damage");

		// For VOID, additionally teleport the player back to spawn (or remove non-player entities)
		if (event.GetCause() == DamageCause.VOID)
		{
			if (event.GetDamageeEntity() instanceof Player)
			{
				event.GetDamageeEntity().eject();
				event.GetDamageeEntity().leaveVehicle();
				event.GetDamageeEntity().teleport(GetSpawn().add(0, 10, 0));
			}
			else
			{
				event.GetDamageeEntity().remove();
			}
			return;
		}
	}


	@EventHandler
	public void FoodHealthUpdate(UpdateEvent event)
	{
		if (event.getType() != UpdateType.SEC)
			return;

		for (Player player : UtilServer.getPlayers())
		{
			player.setFoodLevel(20);
			player.setExhaustion(0f);
			player.setSaturation(3f);
		}
	}

	@EventHandler
	public void LevelUpdate(UpdateEvent event)
	{
		if (event.getType() != UpdateType.SEC)
			return;

		for (Player player : UtilServer.getPlayers())
		{
			if (_clientManager.Get(player) == null || _clientManager.Get(player).GetRank() == null) continue;
			
			int level = _achievementManager.getHouziLevelNumber(player, _clientManager.Get(player).GetRank());
			
			if (player.getLevel() != level)
			{
				player.setLevel(level);
				player.setExp(0f);
			}
		}
	}

	@EventHandler
	public void InventoryCancel(InventoryClickEvent event)
	{
		if (event.getWhoClicked() instanceof Player && ((Player)event.getWhoClicked()).getGameMode() != GameMode.CREATIVE)
			event.setCancelled(true);
	}




	@Override
	protected HubClient AddPlayer(String player)
	{
		return new HubClient(player);
	}

	public BlockRestore GetBlockRestore()
	{
		return _blockRestore;
	}

	public CoreClientManager GetClients()
	{
		return _clientManager;
	}

	public Object GetCondition() // Was ConditionManager
	{
		return _conditionManager;
	}

	public DonationManager GetDonation()
	{
		return _donationManager;
	}

	public com.houzicore.shared.core.inventory.InventoryManager getInventoryManager()
	{
		return _inventoryManager;
	}

	public DisguiseManager GetDisguise()
	{
		return _disguiseManager;
	}

	public GadgetManager GetGadget()
	{
		return _gadgetManager;
	}

	public TreasureManager GetTreasure()
	{
		return _treasureManager;
	}

	public MountManager GetMount()
	{
		return _mountManager;
	}

	public ParkourManager GetParkour()
	{
		return _parkour;
	}

	public PreferencesManager getPreferences()
	{
		return _preferences;
	}
	
	public Location GetSpawn()
	{
		return _spawn.clone();
	}
	
	public PetManager getPetManager()
	{
	    return _petManager;
	}

	public TutorialManager GetTutorial()
	{
		return _tutorialManager;
	}

	public StatsManager GetStats()
	{
		return _statsManager;
	}

	public HubVisibilityManager GetVisibility()
	{
		return _visibilityManager;
	}
	
	@EventHandler(priority = EventPriority.MONITOR)
	public void gadgetCollide(GadgetCollideEntityEvent event)
	{
		if (!event.isCancelled())
			SetPortalDelay(event.getOther());
	}

	public void SetPortalDelay(Entity ent)
	{
		if (ent instanceof Player)
			_portalTime.put(((Player)ent).getName(), System.currentTimeMillis());
	}

	public boolean CanPortal(Player player)
	{
		//Riding
		if (player.getVehicle() != null || player.getPassenger() != null)
			return false;

		//Portal Delay
		if (!_portalTime.containsKey(player.getName()))
			return true;

		return UtilTime.elapsed(_portalTime.get(player.getName()), 5000);
	}

	public boolean CanBump(LivingEntity ent)
	{
		if (!(ent instanceof Player))
			return true;

		if (BumpDisabled(ent))
			return false;

		if (!getPreferences().Get((Player)ent).ShowPlayers)
			return false;

		return true;
	}

	// @EventHandler - SkillTriggerEvent not in Shared JAR
	// public void SkillTrigger(SkillTriggerEvent event)
	// {
	// 	event.SetCancelled(true);
	// }

	// @EventHandler - ItemTriggerEvent not in Shared JAR
	// public void ItemTrigger(ItemTriggerEvent event)
	// {
	// 	event.SetCancelled(true);
	// }

	public boolean IsGadgetEnabled()
	{
		return _gadgetsEnabled;
	}

	
	public NewsManager GetNewsManager()
	{
		return _news;
	}

	private com.houzicore.lobby.hub.queue.QueueManager _queueManager;
	public void setQueueManager(com.houzicore.lobby.hub.queue.QueueManager queueManager) {
		this._queueManager = queueManager;
	}
	public com.houzicore.lobby.hub.queue.QueueManager getQueueManager() {
		return _queueManager;
	}

	public HologramManager getHologramManager()
	{
		return _hologramManager;
	}

	@EventHandler
	public void ignoreVelocity(PlayerVelocityEvent event)
	{
		if (_clientManager.Get(event.getPlayer()).GetRank().Has(Rank.MODERATOR) && _preferences.Get(event.getPlayer()).IgnoreVelocity)
		{
			event.setCancelled(true);
		}
	}

	public void ToggleGadget(Player caller)
	{
		_gadgetsEnabled = !_gadgetsEnabled;
		
		if (!_gadgetsEnabled)
		{
			GetMount().DisableAll();
			GetGadget().DisableAll();
		}
		
		for (Player player : UtilServer.getPlayers()) {
			boolean isThai = com.houzicore.shared.core.lang.LangManager.get().isThai(player);
			String message = isThai ? "§f§lGadgets/Mounts §7ตอนนี้ " + F.elem(_gadgetsEnabled ? "§aเปิดใช้งาน" : "§cปิดการใช้งาน") : "§f§lGadgets/Mounts §7are now " + F.elem(_gadgetsEnabled ? "§aEnabled" : "§cDisabled");
			player.sendMessage(message);
		}
	}
	
	@EventHandler
	public void GadgetActivate(GadgetActivateEvent event)
	{
		if (!_gadgetsEnabled)
			event.setCancelled(true);
	}
	
	@EventHandler
	public void MountActivate(MountActivateEvent event)
	{
		if (!_gadgetsEnabled)
			event.setCancelled(true);
	}

	public void addGameMode(Player caller, Player target)
	{
		if (!_creativeAdmin.containsKey(caller.getName()))
			_creativeAdmin.put(caller.getName(), new ArrayList<String>());
		
		if (target.getGameMode() == GameMode.CREATIVE)
		{
			_creativeAdmin.get(caller.getName()).add(target.getName());
		}
		else
		{
			_creativeAdmin.get(caller.getName()).remove(target.getName());
		}
	}
	
	@EventHandler
	public void clearGameMode(PlayerQuitEvent event)
	{
		ArrayList<String> creative = _creativeAdmin.remove(event.getPlayer().getName());
		
		if (creative == null)
			return;
		
		for (String name : creative)
		{
			Player player = UtilPlayer.searchExact(name);
			if (player == null)
				continue;
			
			player.setGameMode(GameMode.SURVIVAL);
			
			UtilPlayer.message(player, F.main("Game Mode", event.getPlayer().getName() + " left the game. Creative Mode: " + F.tf(false)));
		}
	}
}
