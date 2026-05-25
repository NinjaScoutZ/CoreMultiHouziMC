package com.houzicore.arcade;

import java.awt.Event;
import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.Sound;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.block.BlockBurnEvent;
import org.bukkit.event.block.BlockFadeEvent;
import org.bukkit.event.block.BlockSpreadEvent;
import org.bukkit.event.block.LeavesDecayEvent;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.server.ServerListPingEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.util.Vector;

import com.google.common.base.Objects;

import com.houzicore.shared.MiniPlugin;
import com.houzicore.shared.account.CoreClientManager;
import com.houzicore.shared.core.achievement.AchievementManager;
import com.houzicore.shared.core.blockrestore.BlockRestore;
import com.houzicore.shared.core.blood.Blood;
import com.houzicore.shared.core.chat.Chat;
import com.houzicore.shared.common.Rank;
import com.houzicore.shared.common.jsonchat.ClickEvent;
import com.houzicore.shared.common.jsonchat.JsonMessage;
import com.houzicore.shared.common.util.C;
import com.houzicore.shared.common.util.F;
import com.houzicore.shared.common.util.NautHashMap;
import com.houzicore.shared.common.util.UtilGear;
import com.houzicore.shared.common.util.UtilInv;
import com.houzicore.shared.common.util.UtilPlayer;
import com.houzicore.shared.common.util.UtilServer;
import com.houzicore.shared.common.util.UtilTime;
import com.houzicore.shared.core.cosmetic.CosmeticManager;
import com.houzicore.shared.core.creature.Creature;
import com.houzicore.shared.core.disguise.DisguiseManager;
import com.houzicore.shared.core.donation.DonationManager;
import com.houzicore.shared.core.elo.EloManager;
import com.houzicore.shared.core.energy.Energy;
import com.houzicore.shared.core.explosion.Explosion;
import com.houzicore.shared.core.hologram.HologramManager;
import com.houzicore.shared.core.inventory.InventoryManager;
import com.houzicore.shared.core.itemstack.ItemStackFactory;
import com.houzicore.shared.core.movement.Movement;
import com.houzicore.shared.core.notifier.NotificationManager;
import com.houzicore.shared.core.packethandler.IPacketHandler;
import com.houzicore.shared.core.packethandler.PacketHandler;
import com.houzicore.shared.core.packethandler.PacketInfo;
//import com.houzicore.shared.core.packethandler.PacketPlayResourcePackStatus;
//import com.houzicore.shared.core.packethandler.PacketPlayResourcePackStatus.EnumResourcePackStatus;
import com.houzicore.shared.core.party.PartyManager;
import com.houzicore.shared.core.pet.PetManager;
import com.houzicore.shared.core.portal.Portal;
import com.houzicore.shared.core.preferences.PreferencesManager;
import com.houzicore.shared.core.projectile.ProjectileManager;
import com.houzicore.shared.core.resourcepack.ResUnloadCheck;
import com.houzicore.shared.core.resourcepack.ResPackManager;
import com.houzicore.shared.core.resourcepack.redis.RedisUnloadResPack;
import com.houzicore.shared.core.reward.RewardRarity;
import com.houzicore.shared.core.reward.rewards.PetReward;
import com.houzicore.shared.core.stats.StatsManager;
import com.houzicore.shared.core.status.ServerStatusManager;
import com.houzicore.shared.core.task.TaskManager;
import com.houzicore.shared.core.teleport.Teleport;
import com.houzicore.shared.timing.TimingManager;
import com.houzicore.shared.updater.event.UpdateEvent;
import com.houzicore.shared.common.IRelation;
import com.houzicore.arcade.nautilus.game.arcade.addons.CompassAddon;
import com.houzicore.arcade.nautilus.game.arcade.addons.SoupAddon;
import com.houzicore.arcade.nautilus.game.arcade.addons.TeamArmorAddon;
import com.houzicore.shared.core.condition.ConditionManager;
import com.houzicore.shared.core.damage.DamageManager;
import com.houzicore.shared.core.fire.Fire;
import com.houzicore.arcade.nautilus.game.arcade.command.DisguiseCommand;
import com.houzicore.arcade.nautilus.game.arcade.command.GameCommand;
import com.houzicore.arcade.nautilus.game.arcade.command.MapBuilderBridgeCommand;
import com.houzicore.arcade.nautilus.game.arcade.command.WriteCommand;
import com.houzicore.arcade.nautilus.game.arcade.command.KitUnlockCommand;
import com.houzicore.arcade.nautilus.game.arcade.events.GameStateChangeEvent;
import com.houzicore.arcade.nautilus.game.arcade.game.Game;
import com.houzicore.arcade.nautilus.game.arcade.game.Game.GameState;
import com.houzicore.arcade.nautilus.game.arcade.game.GameServerConfig;
import com.houzicore.arcade.nautilus.game.arcade.game.GameTeam;
import com.houzicore.arcade.nautilus.game.arcade.game.games.event.EventModule;

import com.houzicore.arcade.nautilus.game.arcade.managers.ArcadeAchievementChatRenderer;
import com.houzicore.arcade.nautilus.game.arcade.managers.GameChatManager;
import com.houzicore.arcade.nautilus.game.arcade.managers.GameCreationManager;
import com.houzicore.arcade.nautilus.game.arcade.managers.GameFlagManager;
import com.houzicore.arcade.nautilus.game.arcade.managers.GameEssenceManager;
import com.houzicore.arcade.nautilus.game.arcade.managers.GameHostManager;
import com.houzicore.arcade.nautilus.game.arcade.managers.GameLobbyHologramManager;
import com.houzicore.arcade.nautilus.game.arcade.managers.GameLobbyManager;
import com.houzicore.arcade.nautilus.game.arcade.managers.GameLootManager;
import com.houzicore.arcade.nautilus.game.arcade.managers.GameManager;
import com.houzicore.arcade.nautilus.game.arcade.managers.GamePlayerManager;
import com.houzicore.arcade.nautilus.game.arcade.managers.GameSpectatorManager;
import com.houzicore.arcade.nautilus.game.arcade.managers.GameStatManager;
import com.houzicore.arcade.nautilus.game.arcade.managers.GameTournamentManager;
import com.houzicore.arcade.nautilus.game.arcade.managers.GameWorldManager;
import com.houzicore.arcade.nautilus.game.arcade.managers.IdleManager;
import com.houzicore.arcade.nautilus.game.arcade.managers.MapVotingManager;
import com.houzicore.arcade.nautilus.game.arcade.managers.MiscManager;
import com.houzicore.arcade.nautilus.game.arcade.managers.GameLifecycleGuard;
import com.houzicore.arcade.nautilus.game.arcade.shop.ArcadeShop;
import com.houzicore.shared.core.nametag.SubnameManager;
 
public class ArcadeManager extends MiniPlugin implements IRelation
{ 
	// Modules       
	private BlockRestore _blockRestore;
	private Blood _blood;
	private Chat _chat;
	private CoreClientManager _clientManager;
	private DisguiseManager _disguiseManager;
	private DonationManager _donationManager;
	private ConditionManager _conditionManager;
	private PetManager _petManager;
	private Creature _creature;
	private DamageManager _damageManager;
	private Explosion _explosionManager;
	private EventModule _eventManager;
	private com.houzicore.arcade.nautilus.game.arcade.kit.traits.TraitManager _traitManager;
	private com.houzicore.shared.core.ignore.IgnoreManager _ignoreManager;
	
	private Fire _fire;
	private ProjectileManager _projectileManager;
	
	private Portal _portal;  
	private ArcadeShop _arcadeShop;
	private com.houzicore.arcade.nautilus.game.arcade.kit.traits.ui.TraitShop _traitShop;
	
	
	// Managers 
	private GameCreationManager _gameCreationManager;
	private GameEssenceManager _GameEssenceManager;
	private GameManager _gameManager;
	private GameLobbyManager _gameLobbyManager;
	private GamePlayerManager _gamePlayerManager;
	private GameTournamentManager _gameTournamentManager;
	private GameWorldManager _gameWorldManager;
	private GameHostManager _gameHostManager;
	private GameStatManager _gameStatManager;
    private com.houzicore.arcade.bootstrap.ArcadeTransitionCoordinator _transitionCoordinator;
	private MapVotingManager _mapVotingManager;
	private ServerStatusManager _serverStatusManager;
	private InventoryManager _inventoryManager;
	private CosmeticManager _cosmeticManager;
	private final IdleManager _idleManager;
	private GameLifecycleGuard _lifecycleGuard;
    private HologramManager _hologramManager;
	private com.houzicore.shared.core.displayentity.DisplayEntityManager _displayEntityManager;
	private AchievementManager _achievementManager;
	private com.houzicore.shared.core.battlepass.BattlePassManager _battlePassManager;
	private StatsManager _statsManager;
	private PartyManager _partyManager;
	private PreferencesManager _preferencesManager;
	private com.houzicore.shared.core.level.LvlManager _lvlManager;
	private com.houzicore.arcade.nautilus.game.arcade.managers.GameCoinManager _gameCoinManager;
	private EloManager _eloManager;

	private TaskManager _taskManager;
    private PacketHandler _packetHandler;
	
	
	private IPacketHandler _resourcePacketHandler;
	private String _resourcePackUrl;
	private boolean _resourcePackRequired;
	private NautHashMap<String, Boolean> _resourcePackUsers = new NautHashMap<String, Boolean>();
	private NautHashMap<String, Long> _resourcePackNoResponse = new NautHashMap<String, Long>();

	// Observers
	private HashSet<Player> _specList = new HashSet<Player>();

	// Server Games
	private GameServerConfig _serverConfig;

	// Games
	private Game _game;
	
	//Youtuber Kits
	private HashSet<Player> _youtube = new HashSet<Player>();

	@Override
	public void addCommands()
	{
		addCommand(new GameCommand(this));
		addCommand(new WriteCommand(this));
		addCommand(new DisguiseCommand(this));
		addCommand(new KitUnlockCommand(this));
		addCommand(new MapBuilderBridgeCommand(this));
		addCommand(new com.houzicore.arcade.nautilus.game.arcade.command.KitCommand(this));
		
		try {
			com.houzicore.arcade.nautilus.game.arcade.command.DiagCommand.register(getPlugin().getLifecycleManager(), this);
		} catch (Exception e) {
			getPlugin().getLogger().warning("Failed to register Brigadier command /diag: " + e.getMessage());
		}
	}

	public ArcadeManager(Arcade plugin, ServerStatusManager serverStatusManager, GameServerConfig serverConfig,
						 CoreClientManager clientManager, DonationManager donationManager, 
						 StatsManager statsManager, AchievementManager achievementManager, com.houzicore.shared.core.battlepass.BattlePassManager battlePassManager, DisguiseManager disguiseManager, Creature creature, Teleport teleport, Blood blood, Chat chat,
						 Portal portal, PreferencesManager preferences, InventoryManager inventoryManager, PacketHandler packetHandler,
						 CosmeticManager cosmeticManager, ProjectileManager projectileManager, PetManager petManager, HologramManager hologramManager, com.houzicore.shared.core.displayentity.DisplayEntityManager displayEntityManager, com.houzicore.shared.core.ignore.IgnoreManager ignoreManager, String webAddress)
	{
		super("Game Manager", plugin);

		_serverConfig = serverConfig;
		_ignoreManager = ignoreManager;

		// Modules
		_blockRestore = new BlockRestore(plugin);
		
		_blood = blood;
		_preferencesManager = preferences;

		_explosionManager = new Explosion(plugin, _blockRestore);
		_explosionManager.SetDebris(false);

		_conditionManager = new ConditionManager(plugin);
		
		_clientManager = clientManager;
		_serverStatusManager = serverStatusManager;
		_chat = chat;
		_creature = creature;

		com.houzicore.shared.core.combat.CombatManager combatManager = new com.houzicore.shared.core.combat.CombatManager(plugin);
		new com.houzicore.shared.core.combat.legacy.LegacyCombatManager(plugin);
		_damageManager = new DamageManager(plugin, combatManager, null, disguiseManager, _conditionManager);
		_damageManager.UseSimpleWeaponDamage = true;
		
		_disguiseManager = disguiseManager;

		_donationManager = donationManager;

		_fire = new Fire(plugin, _conditionManager, _damageManager);

		_projectileManager = projectileManager;
		
		_packetHandler = packetHandler;
		
		_partyManager = new PartyManager(plugin, portal, _clientManager, preferences, _ignoreManager);
		_statsManager = statsManager;
		_eloManager = new EloManager(plugin, clientManager);
		_taskManager = new TaskManager(plugin, clientManager, webAddress);
		_achievementManager = achievementManager;
		_battlePassManager = battlePassManager;
		_inventoryManager = inventoryManager;
		_cosmeticManager = cosmeticManager;
		_portal = portal;
		_petManager = petManager;
		_eventManager = new EventModule(this, getPlugin());
		_traitManager = new com.houzicore.arcade.nautilus.game.arcade.kit.traits.TraitManager(this);

		// Shop
		_arcadeShop = new ArcadeShop(this, clientManager, donationManager);
		_traitShop = new com.houzicore.arcade.nautilus.game.arcade.kit.traits.ui.TraitShop(this, clientManager, donationManager);

		// Managers
		new GameChatManager(this);
		_gameCreationManager = new GameCreationManager(this);
		_GameEssenceManager = new GameEssenceManager(this);
		_gameManager = new GameManager(this);
		_gameLobbyManager = new GameLobbyManager(this);
		new GameLobbyHologramManager(this);
		_gameHostManager = new GameHostManager(this);
		_mapVotingManager = new MapVotingManager(this);
		new GameFlagManager(this);
        _transitionCoordinator = new com.houzicore.arcade.bootstrap.ArcadeTransitionCoordinator(this);
		_gamePlayerManager = new GamePlayerManager(this);
		new ArcadeAchievementChatRenderer(this);
		_gameTournamentManager = new GameTournamentManager(this);
		_gameStatManager = new GameStatManager(this);
		new GameLootManager(this, petManager);
		new GameSpectatorManager(this);
		new com.houzicore.arcade.nautilus.game.arcade.managers.GameDeathManager(this);
		new com.houzicore.arcade.nautilus.game.arcade.managers.KillStreakManager(this);
		new com.houzicore.arcade.nautilus.game.arcade.managers.WinStreakManager(this);
		_gameCoinManager = new com.houzicore.arcade.nautilus.game.arcade.managers.GameCoinManager(this);
		new com.houzicore.arcade.nautilus.game.arcade.managers.KillEffectManager(this);
		new com.houzicore.arcade.nautilus.game.arcade.managers.WinCelebrationManager(this);
		new com.houzicore.arcade.nautilus.game.arcade.managers.ArrowTrailManager(this);
		new com.houzicore.arcade.nautilus.game.arcade.managers.SpawnAnimationManager(this);
		new com.houzicore.arcade.nautilus.game.arcade.managers.CustomDeathMessageManager(this);
		_gameWorldManager = new GameWorldManager(this);
		_lifecycleGuard = new GameLifecycleGuard(this);
		new MiscManager(this);
		_hologramManager = hologramManager;
		_displayEntityManager = displayEntityManager;
		_idleManager = new IdleManager(this);
		_lvlManager = new com.houzicore.shared.core.level.LvlManager(plugin, statsManager, clientManager, donationManager);
		
		com.houzicore.shared.core.nametag.SubnameManager subnameManager = new com.houzicore.shared.core.nametag.SubnameManager(plugin, clientManager);
		subnameManager.setSubnameProvider(target -> {
			com.houzicore.shared.core.party.Party party = _partyManager != null ? _partyManager.getPartyByPlayer(target) : null;
			if (party != null) {
				return net.kyori.adventure.text.Component.text("§dParty ของ " + party.getLeaderName());
			}
			
			if (_cosmeticManager != null) {
				com.houzicore.shared.core.gadget.types.Gadget gadget = _cosmeticManager.getGadgetManager().getActive(target, com.houzicore.shared.core.gadget.types.GadgetType.Banner);
				if (gadget != null) {
					return net.kyori.adventure.text.Component.text("§e" + gadget.GetName());
				}
			}
			
			try {
				com.houzicore.shared.core.clan.ClanManager clanMgr = com.houzicore.shared.core.clan.ClanManager.getInstance();
				if (clanMgr != null) {
					com.houzicore.shared.core.clan.Clan clan = clanMgr.getClan(target);
					if (clan != null) {
						return net.kyori.adventure.text.Component.text("§b[" + clan.getName() + "]");
					}
				}
			} catch (Exception ignored) {}
			
			return null;
		});
		//new HolidayManager(this);
	}

	public MapVotingManager getMapVotingManager()
	{
		return _mapVotingManager;
	}

	public GameEssenceManager GetGameEssenceManager()
	{
		return _GameEssenceManager;
	}
	
	public com.houzicore.arcade.nautilus.game.arcade.kit.traits.TraitManager getTraitManager()
	{
		return _traitManager;
	}

	public com.houzicore.arcade.nautilus.game.arcade.kit.traits.ui.TraitShop getTraitShop()
	{
		return _traitShop;
	}
	
	public GamePlayerManager GetGamePlayerManager()
	{
		return _gamePlayerManager;
	}
	
	public GameStatManager getGameStatManager()
	{
		return _gameStatManager;
	}
	
	public GameTournamentManager GetGameTournamentManager()
	{
		return _gameTournamentManager;
	}

	public GameWorldManager GetGameWorldManager()
	{
		return _gameWorldManager;
	}

	public GameLifecycleGuard getLifecycleGuard()
	{
		return _lifecycleGuard;
	}

	public com.houzicore.arcade.bootstrap.ArcadeTransitionCoordinator getTransitionCoordinator()
	{
		return _transitionCoordinator;
	}
	
	public EventModule GetEventModule()
	{
		return _eventManager;
	}
	
	public PreferencesManager getPreferences()
	{
		return _preferencesManager;
	}

	public StatsManager GetStatsManager()
	{
		return _statsManager;
	}

	public com.houzicore.shared.core.level.LvlManager getLvlManager()
	{
		return _lvlManager;
	}

	public com.houzicore.arcade.nautilus.game.arcade.managers.GameCoinManager getGameCoinManager()
	{
		return _gameCoinManager;
	}

	public EloManager getEloManager()
	{
		return _eloManager;
	}

	public ServerStatusManager GetServerStatusManager()
	{
		return _serverStatusManager;
	}

	public CosmeticManager GetCosmeticManager()
	{
		return _cosmeticManager;
	}

	public ChatColor GetColor(Player player)
	{
		if (_game == null)
			return ChatColor.GRAY;

		GameTeam team = _game.GetTeam(player);
		if (team == null)
			return ChatColor.GRAY;

		return team.GetColor();
	}

	@Override
	public boolean canHurt(String a, String b)
	{
		return canHurt(UtilPlayer.searchExact(a), UtilPlayer.searchExact(b));
	}

	public boolean canHurt(Player pA, Player pB)
	{
		if (pA == null || pB == null)
			return false;

		if (_game.isContextRuntime())
		{
			if (!_game.getRules().isDamage())
				return false;

			if (!_game.getRules().isDamagePvP())
				return false;
		}
		else
		{
			if (!_game.Damage)
				return false;

			if (!_game.DamagePvP)
				return false;
		}

		// Self Damage
		if (pA.equals(pB))
			return _game.isContextRuntime() ? _game.getRules().isDamageSelf() : _game.DamageSelf;

		GameTeam tA = _game.GetTeam(pA);
		if (tA == null)
			return false;

		GameTeam tB = _game.GetTeam(pB);
		if (tB == null)
			return false;

		if (tA.equals(tB) && !(_game.isContextRuntime() ? _game.getRules().isDamageTeamSelf() : _game.DamageTeamSelf))
			return false;

		if (!tA.equals(tB) && !(_game.isContextRuntime() ? _game.getRules().isDamageTeamOther() : _game.DamageTeamOther))
			return false;

		return true;
	}

	@Override
	public boolean isSafe(Player player)
	{
		if (_game == null)
			return true;

		if (_game.IsPlaying(player))
			return false;

		return true;
	}

	@EventHandler
	public void onGameStateChange(GameStateChangeEvent event)
	{
		if (event.GetState() == GameState.Prepare || event.GetState() == GameState.Live)
		{
			if (_cosmeticManager != null)
			{
				_cosmeticManager.disableItemsForGame();
				_cosmeticManager.setActive(false);
			}
		}
		else if (event.GetState() == GameState.Recruit || event.GetState() == GameState.Vote)
		{
			if (_cosmeticManager != null)
			{
				_cosmeticManager.setActive(true);
			}
		}
	}

	@EventHandler
	public void MessageMOTD(ServerListPingEvent event)
	{
		event.setMaxPlayers(_serverConfig.MaxPlayers);

		//MPS
		if (_gameHostManager != null && _gameHostManager.isPrivateServer())
		{
			if (_gameHostManager.isHostExpired())
			{
				event.setMotd(ChatColor.RED + "Finished");
				return;
			}
			
			if (!GetServerConfig().PublicServer || GetServerConfig().PlayerServerWhitelist)
			{
				event.setMotd(ChatColor.GRAY + "Private");
				return;
			}
		}

		String extrainformation = "|" + _serverConfig.ServerType + "|" + (_game == null ? "Unknown" : _game.GetName())
				+ "|" + ((_game == null || _game.WorldData == null) ? "Unknown" : _game.WorldData.MapName);

		if (_gameHostManager.isPrivateServer() && _gameHostManager.hasRank(Rank.MODERATOR))
			extrainformation += "|StaffHosted";

		//Always Joinable
//		if (_game != null && _game.JoinInProgress)
//		{
//			event.setMotd(ChatColor.GREEN + "Recruiting" + extrainformation);
//		}
		//Voting
		if (_game != null && _game.GetState() == GameState.Vote)
		{
			event.setMotd(ChatColor.GREEN + "Voting" + extrainformation);
		}
		//Recruiting
		else if (_game == null || _game.GetState() == GameState.Recruit)
		{
			if (_game != null && _game.GetCountdown() != -1)
			{
				event.setMotd(ChatColor.GREEN + "Starting in " + _game.GetCountdown() + " Seconds" + extrainformation);
			}
			else
			{
				event.setMotd(ChatColor.GREEN + "Recruiting" + extrainformation);
			}
		}
		//In Progress
		else
		{
			event.setMotd(ChatColor.YELLOW + "In Progress" + extrainformation);
		}
	}

	@EventHandler
	public void MessageJoin(PlayerJoinEvent event)
	{
		String name = event.getPlayer().getName();
		
		if (_game != null && _game.AnnounceJoinQuit)
			event.setJoinMessage(F.sys("Join", GetColor(event.getPlayer()) + name));

		else
			event.setJoinMessage(null);
	}

	@EventHandler
	public void MessageQuit(PlayerQuitEvent event)
	{
		String name = event.getPlayer().getName();
		
		if (_game == null || _game.AnnounceJoinQuit)
			event.setQuitMessage(F.sys("Quit", GetColor(event.getPlayer()) + name));
		else
			event.setQuitMessage(null);
	}

	public Game GetGame()
	{
		return _game;
	}

	public void SetGame(Game game)
	{
		_game = game;
	}

	public int GetPlayerMin()
	{
		return GetServerConfig().MinPlayers;
	}

	public int GetPlayerFull()
	{
		return GetServerConfig().MaxPlayers;
	}

	public void HubClock(Player player)
	{
		if (!IsHotbarHubClock())
			return;

		if (_game != null && !_game.GiveClock)
			return;

		if (player.getOpenInventory().getType() != InventoryType.CRAFTING &&
				player.getOpenInventory().getType() != InventoryType.CREATIVE)
			return;

		if (!UtilGear.isMat(player.getInventory().getItem(8), Material.CLOCK) && !UtilGear.isMat(player.getInventory().getItem(8), Material.GLISTERING_MELON_SLICE))
		{
			player.getInventory().setItem(
					8,
					ItemStackFactory.Instance.CreateStack(Material.CLOCK, (byte) 0, 1, (short) 0, C.cGreen
							+ "Return to Hub", new String[]{"", ChatColor.RESET + "Click while holding this",
							ChatColor.RESET + "to return to the Hub."}));

			UtilInv.Update(player);
		}
	}

	@EventHandler
	public void Login(PlayerLoginEvent event)
	{
		if (Bukkit.getServer().hasWhitelist())
		{
			if (_clientManager.Get(event.getPlayer().getName()).GetRank().Has(event.getPlayer(), Rank.MODERATOR, false))
			{
				event.allow();
				event.setResult(PlayerLoginEvent.Result.ALLOWED);

				if (_serverConfig.Tournament)
				{
					event.getPlayer().setOp(true);
				}
			}
			else
			{
				for (OfflinePlayer player : Bukkit.getWhitelistedPlayers())
				{
					if (player.getName().equalsIgnoreCase(event.getPlayer().getName()))
					{
						event.allow();
						event.setResult(PlayerLoginEvent.Result.ALLOWED);
						return;
					}
				}

				event.disallow(PlayerLoginEvent.Result.KICK_OTHER, "Server Whitelisted!");
			}

			return;
		}

		// Reserved Slot Check
		if (Bukkit.getOnlinePlayers().size() >= Bukkit.getServer().getMaxPlayers())
		{
			if (_clientManager.Get(event.getPlayer().getName()).GetRank().Has(event.getPlayer(), Rank.HELPER, false))
			{
				event.allow();
				event.setResult(PlayerLoginEvent.Result.ALLOWED);
				return;
			}
			else if (_clientManager.Get(event.getPlayer().getName()).GetRank().Has(event.getPlayer(), Rank.WARRIOR, false)
					|| _donationManager.Get(event.getPlayer().getName()).OwnsUnknownPackage(_serverConfig.ServerType + " ULTRA"))
			{
				
				if (GetGame() != null && GetGame().DontAllowOverfill)
				{
					event.disallow(PlayerLoginEvent.Result.KICK_OTHER, C.Bold + "Server has reached max capacity for gameplay purposes.");
					return;
				}
				else if (Bukkit.getServer().getOnlinePlayers().size() / Bukkit.getMaxPlayers() > 1.5)
				{
					event.disallow(PlayerLoginEvent.Result.KICK_OTHER, C.Bold + "Server has reached max capacity for gameplay purposes.");
					return;
				}
				else if (_gameHostManager.isEventServer() && Bukkit.getServer().getOnlinePlayers().size() >= 128) 
				{
					event.disallow(PlayerLoginEvent.Result.KICK_OTHER, C.Bold + "Server has reached max capacity for gameplay purposes.");
					return;
				}
				
				event.allow();
				event.setResult(PlayerLoginEvent.Result.ALLOWED);

				return;
			}

			event.disallow(PlayerLoginEvent.Result.KICK_OTHER, "Server Full! This server has reached its maximum player capacity.");
		}
	}

	@EventHandler(priority = EventPriority.LOW)
	public void AdminOP(PlayerJoinEvent event)
	{
		// Give developers operator on their servers
		String groupStr = _plugin.getConfig().getString("serverstatus.group");
		boolean testServer = groupStr != null && groupStr.equalsIgnoreCase("Testing");

		if (_clientManager.Get(event.getPlayer()).GetRank().Has(Rank.OWNER) || (testServer && (_clientManager.Get(event.getPlayer()).GetRank().Has(Rank.DEVELOPER) || _clientManager.Get(event.getPlayer()).GetRank() == Rank.JNR_DEV)))
			event.getPlayer().setOp(true);
		else
			event.getPlayer().setOp(false);
	}

	public boolean IsAlive(Player player)
	{
		if (_game == null)
			return false;

		return _game.IsAlive(player);
	}

	public void Clear(Player player)
	{
		// DESYNC FIX (Bug 4): Never change GameMode/health on a dead player
		if (player.isDead())
		{
			return;
		}
		// Delegate base state wipe to the runtime spine
		com.houzicore.arcade.bootstrap.ArcadeBootstrap.getInstance().getPlayerStateApplier().cleanState(player);

		//Remove all conditions
		GetCondition().EndCondition(player, null, null);
			

		HubClock(player);

		GetDisguise().undisguise(player);
	}

	public void clearSpectators()
	{
		_specList.clear();
	}

	public ArrayList<String> LoadFiles(String gameName)
	{
		TimingManager.start("ArcadeManager LoadFiles");
		
		File folder = new File("Maps" + File.separatorChar + gameName);
		if (!folder.exists())
			folder.mkdirs();

		ArrayList<String> maps = new ArrayList<String>();


		if (folder.listFiles() == null)
		{
			TimingManager.stop("ArcadeManager LoadFiles");
			return maps;
		}

		for (File file : folder.listFiles())
		{
			if (!file.isFile())
				continue;

			String name = file.getName();

			if (name.length() < 5)
				continue;

			if (!name.toLowerCase().endsWith(".zip"))
				continue;

			maps.add(name.substring(0, name.length() - 4));
		}

		for (String map : maps)

		if (maps.isEmpty())

		TimingManager.stop("ArcadeManager LoadFiles");

		return maps;
	}

//	public ClassManager getClassManager()
//	{
//		return _classManager;
//	}

//	public ClassCombatShop getClassShop()
//	{
//		return _classShop;
//	}

	public void openClassShop(Player player)
	{
		// _classShop.attemptShopOpen(player); // TODO: Champions class shop not migrated
	}

	@EventHandler
	public void BlockBurn(BlockBurnEvent event)
	{
		if (_game == null)
			event.setCancelled(true);
	}

	@EventHandler
	public void BlockSpread(BlockSpreadEvent event)
	{
		if (_game == null)
			event.setCancelled(true);
	}

	@EventHandler
	public void BlockFade(BlockFadeEvent event)
	{
		if (_game == null)
			event.setCancelled(true);
	}

	@EventHandler
	public void BlockDecay(LeavesDecayEvent event)
	{
		if (_game == null)
			event.setCancelled(true);
	}

	@EventHandler
	public void MobSpawn(CreatureSpawnEvent event)
	{
		// Allow TextDisplay entities (used by Holograms) to spawn regardless of game state
		if (event.getEntity() instanceof org.bukkit.entity.TextDisplay)
			return;

		if (_game == null)
		{
			event.setCancelled(true);
			return;
		}

		// Disable natural monster spawning, only allow plugin-spawned monsters
		if (event.getSpawnReason() == CreatureSpawnEvent.SpawnReason.NATURAL
				|| event.getSpawnReason() == CreatureSpawnEvent.SpawnReason.DEFAULT
				|| event.getSpawnReason() == CreatureSpawnEvent.SpawnReason.CHUNK_GEN
				|| event.getSpawnReason() == CreatureSpawnEvent.SpawnReason.JOCKEY
				|| event.getSpawnReason() == CreatureSpawnEvent.SpawnReason.PATROL
				|| event.getSpawnReason() == CreatureSpawnEvent.SpawnReason.RAID
				|| event.getSpawnReason() == CreatureSpawnEvent.SpawnReason.REINFORCEMENTS
				|| event.getSpawnReason() == CreatureSpawnEvent.SpawnReason.NETHER_PORTAL)
		{
			if (!(event.getEntity() instanceof Player))
			{
				event.setCancelled(true);
			}
		}
	}

//	@EventHandler
//	public void SkillTrigger(SkillTriggerEvent event)
//	{
//		if (_game == null || !_game.IsLive())
//		{
//			event.setCancelled(true);
//		}
//	}
//
//	@EventHandler
//	public void ItemTrigger(ItemTriggerEvent event)
//	{
//		if (_game == null || !_game.IsLive())
//		{
//			event.setCancelled(true);
//		}
//	}

	@EventHandler
	public void Observer(PlayerCommandPreprocessEvent event)
	{
		if (event.getMessage().equalsIgnoreCase("/spec"))
		{
			event.setCancelled(true);

			if (_game != null && _game.InProgress())
			{
				UtilPlayer.message(event.getPlayer(), F.main("Game", "You cannot toggle Spectator during games."));
				return;
			}

			if (!_specList.remove(event.getPlayer()))
			{
				_specList.add(event.getPlayer());

				UtilPlayer.message(event.getPlayer(), F.main("Game", "You are now a Spectator!"));
			}
			else
			{
				UtilPlayer.message(event.getPlayer(), F.main("Game", "You are no longer a Spectator!"));
			}

			// Clean
			if (_game != null)
			{
				// Remove Data
				_game.RemoveTeamPreference(event.getPlayer());
				_game.GetPlayerKits().remove(event.getPlayer());
				_game.GetEssence().remove(event.getPlayer());

				// Leave Team
				GameTeam team = _game.GetTeam(event.getPlayer());

				if (team != null)
				{
					team.RemovePlayer(event.getPlayer());
				}
			}
		}
	}

	@EventHandler
	public void ObserverQuit(PlayerQuitEvent event)
	{
		_specList.remove(event.getPlayer());
	}

	public boolean IsObserver(Player player)
	{
		return _specList.contains(player);
	}

	public boolean IsTournamentServer()
	{
		return _serverConfig.Tournament;
	}
	
	public boolean IsTournamentPoints()
	{
		return _serverConfig.TournamentPoints;
	}

	public boolean IsTeamRejoin()
	{
		return _serverConfig.TeamRejoin;
	}

	public boolean IsTeamAutoJoin()
	{
		return _serverConfig.TeamAutoJoin;
	}

	public boolean IsGameAutoStart()
	{
		return _serverConfig.GameAutoStart;
	}

	public boolean IsGameTimeout()
	{
		return _serverConfig.GameTimeout;
	}

	public boolean IsTeamBalance()
	{
		return _serverConfig.TeamForceBalance;
	}

	public boolean IsRewardEssence()
	{
		return _serverConfig.RewardGems;
	}

	public boolean IsRewardItems()
	{
		return _serverConfig.RewardItems;
	}

	public boolean IsRewardStats()
	{
		return _serverConfig.RewardStats;
	}

	public boolean IsRewardAchievements()
	{
		return _serverConfig.RewardAchievements;
	}

	public boolean IsHotbarInventory()
	{
		return _serverConfig.HotbarInventory;
	}

	public boolean IsHotbarHubClock()
	{
		return _serverConfig.HotbarHubClock;
	}

	public boolean IsPlayerKickIdle()
	{
		return _serverConfig.PlayerKickIdle;
	}

	public int GetDesiredPlayerAmount()
	{
		return _serverConfig.MaxPlayers;
	}
	
	public String GetHost()
	{
		return _serverConfig.HostName;
	}

	@EventHandler
	public void ObserverQuit(GameStateChangeEvent event)
	{
		// Champions modules not migrated
		// if (_skillFactory != null)
		// {
		// 	_skillFactory.ResetAll();
		// }
	}

	public InventoryManager getInventoryManager()
	{
		return _inventoryManager;
	}

	@EventHandler(priority = EventPriority.LOWEST)
	public void cosmeticState(GameStateChangeEvent event)
	{
		//Disable
		if (event.GetState() == GameState.Recruit || event.GetState() == GameState.Vote)
		{
			getCosmeticManager().setActive(true);
		}
		else if (event.GetState() == GameState.Prepare || event.GetState() == GameState.Loading || event.GetState() == GameState.Dead)
		{
			if (event.GetGame().GadgetsDisabled)
			{
				getCosmeticManager().setHideParticles(true);

				if (getCosmeticManager().isShowingInterface())
				{
					getCosmeticManager().setActive(false);
					getCosmeticManager().disableItemsForGame();
				}
			}
		}
	}

	/*public void saveBasicStats(final Game game)
	{
		if (!IsTournamentServer())
			return;
		
		final Map<UUID, Boolean> data = new HashMap<>();

		for (Player loser : game.getLosers())
			data.put(loser.getUniqueId(), false);

		for (Player winner : game.getWinners())
			data.put(winner.getUniqueId(), true);

		Bukkit.getScheduler().runTaskAsynchronously(getPlugin(), new Runnable()
		{
			@Override
			public void run()
			{
				getArcadeRepository().saveBasicStats(game.GetType(), IsTournamentServer(), (int) (System.currentTimeMillis() - game.getGameLiveTime()), data);
			}
		});
	}*/

	/*public void saveLeaderboardStats(Game game)
	{
		final TournamentType type = TournamentType.getTournamentType(game.GetType());

		if (type != null)
		{
			final Map<UUID, Boolean> data = new HashMap<>();

			for (Player loser : game.getLosers())
				data.put(loser.getUniqueId(), false);

			for (Player winner : game.getWinners())
				data.put(winner.getUniqueId(), true);

			Bukkit.getScheduler().runTaskAsynchronously(getPlugin(), new Runnable()
			{
				@Override
				public void run()
				{
					getArcadeRepository().saveLeaderboardStats(0, type.ordinal(), data);
				}
			});
		}
	}*/

	public boolean isGameInProgress()
	{
		return _game != null && _game.InProgress();
	}

	public boolean hasKitsUnlocked(Player player)
	{
		return _youtube.contains(player);
	}

	public void toggleUnlockKits(Player caller)
	{
		if (_youtube.remove(caller))
		{
			UtilPlayer.message(caller, C.cRed + C.Bold + "Celebrity Mode Disabled: " + ChatColor.RESET + "Kits Locked");
		}
		else
		{
			_youtube.add(caller);
			UtilPlayer.message(caller, C.cGreen + C.Bold + "Celebrity Mode Enabled: " + ChatColor.RESET + "All Kits Unlocked");
		}
	}
	
	@EventHandler
	public void quitYoutuber(PlayerQuitEvent event)
	{
		_youtube.remove(event.getPlayer());
	}

	public IdleManager getIdleManager()
	{
		return _idleManager;
	}

	public void rewardPet(Player player, String pet, EntityType type)
	{
		if (!player.isOnline())
			return;
		
		PetReward reward = new PetReward(_petManager, _inventoryManager, _donationManager, pet, pet, type, RewardRarity.OTHER, 0);
		
		if (reward.canGiveReward(player))
			reward.giveReward(null, player);
	}
	
	public void toggleChampionsModules(GameType gameType)
	{
		boolean isChamps = false;

		// Champions modules not migrated — entire toggle disabled
		// TODO: re-implement Champions class system
	}

	public PartyManager getPartyManager()
	{
		return _partyManager;
	}

	/**
	 * Called from PlayerRespawnEvent (death path) — always force-teleports to the cached
	 * spectator location to prevent Paper 1.21 world dimension desync.
	 * 
	 * GameDeathManager guarantees this is only called on alive, non-dead players.
	 */
	public void addSpectator(Player player, org.bukkit.Location specLoc) 
	{
		if (GetGame() == null)
			return;

		// Safety: never set GameMode on a dead player
		if (player.isDead())
			return;

		if (_transitionCoordinator != null)
		{
			_transitionCoordinator.transitionToSpectator(player, com.houzicore.shared.api.context.TransitionReason.SPECTATE);
		}
		else
		{
			com.houzicore.arcade.bootstrap.ArcadeBootstrap.getInstance().getPlayerStateApplier().applyContextState(player, com.houzicore.shared.api.context.PlayerContextId.ARCADE_SPECTATOR);
		}

		// Teleport to force position sync after gamemode packet
		if (specLoc != null)
			player.teleport(specLoc);

		player.setFireTicks(0);
		player.eject();
		player.leaveVehicle();
		player.setArrowsInBody(0);

		GetGame().GetScoreboard().SetPlayerTeam(player, "SPEC");
	}

	/**
	 * Legacy overload used by other callers (GameFlagManager, GameManager, etc.)
	 */
	public void addSpectator(Player player, boolean teleport) 
	{
		addSpectator(player, teleport && GetGame() != null ? GetGame().GetSpectatorLocation() : null);
	}

	public boolean isSpectator(org.bukkit.entity.Entity player) 
	{
		if (player instanceof Player)
		{
			Player p = (Player)player;
			if (IsObserver(p)) return true;
			if (GetGame() != null && GetGame().InProgress() && !GetGame().IsAlive(p)) return true;
			return false;
		}
		return false;
	}

	public List<Player> getValidPlayersForGameStart()
	{
		List<Player> validPlayers = new ArrayList<>();
		for (Player player : Bukkit.getOnlinePlayers())
		{
			if (!IsObserver(player))
			{
				validPlayers.add(player);
			}
		}
		return validPlayers;
	}

	@EventHandler
	public void onSecond(UpdateEvent event)
	{
		Iterator<Entry<String, Long>> itel = _resourcePackNoResponse.entrySet().iterator();

		while (itel.hasNext())
		{
			Entry<String, Long> entry = itel.next();

			if (UtilTime.elapsed(entry.getValue(), 20000))
			{
				Player player = Bukkit.getPlayerExact(entry.getKey());

				if (player != null)
				{
					// Send it again, enforce it!
					_resourcePackNoResponse.put(player.getName(), System.currentTimeMillis());
					player.setResourcePack(_resourcePackUrl);
				}
				else
				{
					itel.remove();
				}
			}
		}
	}

	@EventHandler
	public void ResourcePackQuit(PlayerQuitEvent event)
	{
		Player player = event.getPlayer();

		if (_resourcePackUsers.containsKey(player.getName()) && _resourcePackUsers.get(player.getName()))
		{
			new RedisUnloadResPack(player.getName()).publish();

			_resourcePackUsers.remove(player.getName());
		}
	}

	@EventHandler
	public void outdatedVersion(GameStateChangeEvent event)
	{
		if (!_resourcePackRequired)
			return;

		for (Player player : UtilServer.getPlayers())
		{
			if (!UtilPlayer.is1_8(player))
				returnHubNoResPack(player, "You need to be using 1.8 to play " + GetGame().GetName() + "!");
		}
	}

	private void returnHubNoResPack(Player player)
	{
		player.playSound(player.getLocation(), Sound.ENTITY_ENDER_DRAGON_GROWL, 10f, 1f);
		GetPortal().sendPlayerToServer(player, "Lobby");
	}

	private void returnHubNoResPack(Player player, String message)
	{
		UtilPlayer.message(player, "  ");
		UtilPlayer.message(player, C.cGold + C.Bold + message);
		UtilPlayer.message(player, "  ");
		
		returnHubNoResPack(player);
	}

	@EventHandler
	public void ResourcePackJoin(PlayerJoinEvent event)
	{
		Player player = event.getPlayer();

		if (!UtilPlayer.is1_8(player) && _resourcePackRequired)
		{
			returnHubNoResPack(player, "You need to be using 1.8 to play " + GetGame().GetName() + "!");

			return;
		}

		if (_resourcePackUrl != null)
		{
			if (_resourcePackRequired)
			{
				_resourcePackNoResponse.put(player.getName(), System.currentTimeMillis());
			}

			_resourcePackUsers.put(player.getName(), false);
			player.setResourcePack(_resourcePackUrl);
		}
	}

	public void setResourcePack(String resourcePack, boolean forceResourcePack)
	{
		if (!Objects.equal(resourcePack, _resourcePackUrl) || forceResourcePack != _resourcePackRequired)
		{
			_resourcePackNoResponse.clear();
			_resourcePackUsers.clear();
			_resourcePackUrl = resourcePack == null || resourcePack.isEmpty() ? null : resourcePack;
			_resourcePackRequired = forceResourcePack;

			if (_resourcePackUrl == null || _resourcePackUrl.isEmpty())
			{
				_resourcePackRequired = false;

				for (Player player : Bukkit.getOnlinePlayers())
				{
					player.setResourcePack("http://www.chivebox.com/file/c/empty.zip");
				}
			}
			else
			{
				for (Player player : Bukkit.getOnlinePlayers())
				{
					if (_resourcePackRequired)
					{
						_resourcePackNoResponse.put(player.getName(), System.currentTimeMillis());
					}
					
					_resourcePackUsers.put(player.getName(), false);
					player.setResourcePack(_resourcePackUrl);
				}
			}
		}
	}
	public GameServerConfig GetServerConfig()
	{
		return _serverConfig;
	}

	public ArrayList<GameType> GetGameList()
	{
		return GetServerConfig().GameList;
	}

	public AchievementManager GetAchievement()
	{
		return _achievementManager;
	}

	public com.houzicore.shared.core.battlepass.BattlePassManager getBattlePassManager()
	{
		return _battlePassManager;
	}

	public Blood GetBlood()
	{
		return _blood;
	}

	public Chat GetChat()
	{
		return _chat;
	}

	public BlockRestore GetBlockRestore()
	{
		return _blockRestore;
	}

	public CoreClientManager GetClients()
	{
		return _clientManager;
	}

	public ConditionManager GetCondition() { return _conditionManager; }

	public Creature GetCreature()
	{
		return _creature;
	}
	
	public PacketHandler getPacketHandler()
	{
	    return _packetHandler;
	}

	public CosmeticManager getCosmeticManager()
	{
		return _cosmeticManager;
	}

	public DisguiseManager GetDisguise()
	{
		return _disguiseManager;
	}
	
	public HologramManager getHologramManager()
	{
	    return _hologramManager;
	}

	public com.houzicore.shared.core.displayentity.DisplayEntityManager getDisplayEntityManager()
	{
		return _displayEntityManager;
	}

	public DamageManager GetDamage() { return _damageManager; }

	public DonationManager GetDonation()
	{
		return _donationManager;
	}



	public Explosion GetExplosion()
	{
		return _explosionManager;
	}

	public Fire GetFire() { return _fire; }
	
	public ProjectileManager GetProjectile()
	{
		return _projectileManager;
	}

	public Portal GetPortal()
	{
		return _portal;
	}

	public GameLobbyManager GetLobby()
	{
		return _gameLobbyManager;
	}

	public TaskManager GetTaskManager()
	{
		return _taskManager;
	}

	public ArcadeShop GetShop()
	{
		return _arcadeShop;
	}

	public GameCreationManager GetGameCreationManager()
	{
		return _gameCreationManager;
	}

	public GameHostManager GetGameHostManager()
	{
		return _gameHostManager;
	}

	public GameManager GetGameManager()
	{
		return _gameManager;
	}

}
