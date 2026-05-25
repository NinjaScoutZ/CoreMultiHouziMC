package com.houzicore.arcade;

import java.io.File;

import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import org.bukkit.plugin.java.JavaPlugin;
import com.houzicore.shared.CustomTagFix;
import com.houzicore.shared.TablistFix;
import com.houzicore.shared.account.CoreClientManager;
import com.houzicore.shared.core.achievement.AchievementManager;
import com.houzicore.shared.core.antihack.AntiHack;
import com.houzicore.shared.core.blockrestore.BlockRestore;
import com.houzicore.shared.core.blood.Blood;
import com.houzicore.shared.core.chat.Chat;
import com.houzicore.shared.core.command.CommandCenter;
import com.houzicore.shared.common.util.FileUtil;
import com.houzicore.shared.common.util.UtilServer;
import com.houzicore.shared.core.cosmetic.CosmeticManager;
import com.houzicore.shared.core.creature.Creature;
import com.houzicore.shared.core.disguise.DisguiseManager;
import com.houzicore.shared.core.donation.DonationManager;
import com.houzicore.shared.core.friend.FriendManager;
import com.houzicore.shared.core.gadget.GadgetManager;
import com.houzicore.shared.core.give.Give;
import com.houzicore.shared.core.hologram.HologramManager;
import com.houzicore.shared.core.ignore.IgnoreManager;
import com.houzicore.shared.core.inventory.InventoryManager;
import com.houzicore.shared.core.itemstack.ItemStackFactory;
import com.houzicore.shared.core.leaderboard.LeaderboardManager;
import com.houzicore.shared.core.memory.MemoryFix;
import com.houzicore.shared.core.message.MessageManager;
import com.houzicore.shared.core.monitor.LagMeter;
import com.houzicore.shared.core.mount.MountManager;
import com.houzicore.shared.core.npc.NpcManager;
import com.houzicore.shared.core.packethandler.PacketHandler;
import com.houzicore.shared.core.pet.PetManager;
import com.houzicore.shared.core.portal.Portal;
import com.houzicore.shared.core.preferences.PreferencesManager;
import com.houzicore.shared.core.projectile.ProjectileManager;
import com.houzicore.shared.core.punish.Punish;
import com.houzicore.shared.recharge.Recharge;
import com.houzicore.shared.core.serverConfig.ServerConfiguration;
import com.houzicore.shared.core.stats.StatsManager;
import com.houzicore.shared.core.status.ServerStatusManager;
import com.houzicore.shared.core.teleport.Teleport;
//
import com.houzicore.shared.updater.Updater;
import com.houzicore.shared.core.visibility.VisibilityManager;
import com.houzicore.arcade.nautilus.game.arcade.game.GameServerConfig;

public class Arcade extends JavaPlugin
{      
	private String WEB_CONFIG = "webServer";

	//Modules   
	private CoreClientManager _clientManager;
	private DonationManager _donationManager; 
	private ArcadeManager _gameManager;
	private NpcManager _npcManager;
	 
	private ServerConfiguration _serverConfiguration;

	@Override     
	public void onEnable() 
	{
		// ── MapParser Bypass ──
		if (getServer().getPluginManager().getPlugin("MapParser") != null) {
			getLogger().info("=============================================");
			getLogger().info("MapParser detected! Arcade entering BYPASS mode.");
			getLogger().info("Game systems, scoreboards, and events disabled.");
			getLogger().info("=============================================");
			return;
		}

		//Delete Old Games Folders
		DeleteFolders();
 
		//Configs
		getConfig().addDefault(WEB_CONFIG, "http://localhost/");
		getConfig().set(WEB_CONFIG, getConfig().getString(WEB_CONFIG));
		saveConfig();
		
		String webServerAddress = getConfig().getString(WEB_CONFIG);

		//Logger.initialize(this);

		//Static Modules
		CommandCenter.Initialize(this);		
		_clientManager = new CoreClientManager(this, webServerAddress);
		CommandCenter.Instance.setClientManager(_clientManager);

		ItemStackFactory.Initialize(this, false);  
		Recharge.Initialize(this);   
		VisibilityManager.Initialize(this);
		Give.Initialize(this);

		_donationManager = new DonationManager(this, _clientManager, webServerAddress);

		com.houzicore.shared.core.booster.BoosterManager boosterManager = new com.houzicore.shared.core.booster.BoosterManager(this, _clientManager, _donationManager);
		new com.houzicore.shared.core.thank.ThankManager(this, _clientManager, _donationManager, boosterManager);
		
		new com.houzicore.shared.core.bonuses.BonusManager(this, _clientManager, _donationManager);

		_serverConfiguration = new ServerConfiguration(this, _clientManager);
		
		// new com.houzicore.shared.announce.AnnounceManager(this, _clientManager); // TODO: announce package not migrated
		
		PreferencesManager preferenceManager = new PreferencesManager(this, _clientManager, _donationManager);
		com.houzicore.shared.core.clan.ClanManager clanManager = new com.houzicore.shared.core.clan.ClanManager(this, _clientManager);

		Creature creature = new Creature(this);
		_npcManager = new NpcManager(this, creature);
		ServerStatusManager serverStatusManager = new ServerStatusManager(this, _clientManager, new LagMeter(this, _clientManager));
		LeaderboardManager leaderboardManager = new LeaderboardManager(this, _clientManager);
		Teleport teleport = new Teleport(this);		
		Portal portal = new Portal(this, _clientManager, serverStatusManager.getCurrentServerName());
		// FileUpdater removed
		PacketHandler packetHandler = new PacketHandler(this);
		
		DisguiseManager disguiseManager = new DisguiseManager(this, packetHandler);


		Punish punish = new Punish(this, webServerAddress, _clientManager);
		AntiHack.Initialize(this, punish, portal, preferenceManager, _clientManager);
		// AntiHack.Instance.setKick(false);
		
		IgnoreManager ignoreManager = new IgnoreManager(this, _clientManager, preferenceManager, portal);
		StatsManager statsManager = new StatsManager(this, _clientManager);
		AchievementManager achievementManager = new AchievementManager(statsManager, _clientManager, _donationManager);
		com.houzicore.shared.core.battlepass.BattlePassManager battlePassManager = new com.houzicore.shared.core.battlepass.BattlePassManager(this, _clientManager, _donationManager);
		com.houzicore.shared.core.quest.QuestManager questManager = new com.houzicore.shared.core.quest.QuestManager(this, _clientManager, _donationManager);
        FriendManager friendManager = new FriendManager(this, _clientManager, preferenceManager, portal);
        Chat chat = new Chat(this, _clientManager, preferenceManager, achievementManager, statsManager, _donationManager, serverStatusManager.getCurrentServerName());
        com.houzicore.shared.core.lang.LangManager langManager = new com.houzicore.shared.core.lang.LangManager(this, preferenceManager);
        new com.houzicore.shared.core.scoreboard.ScoreboardManager(this, _clientManager, _donationManager);
        new MessageManager(this, _clientManager, preferenceManager, ignoreManager, punish, friendManager, chat);
		
		BlockRestore blockRestore = new BlockRestore(this);
		
		ProjectileManager projectileManager = new ProjectileManager(this);
		HologramManager hologramManager = new HologramManager(this);
		
		// Bootstrap
		com.houzicore.arcade.bootstrap.ArcadeBootstrap.init(this);

		//Inventory
		InventoryManager inventoryManager = new InventoryManager(this, _clientManager);
		PetManager petManager = new PetManager(this, _clientManager, _donationManager, disguiseManager, creature, blockRestore, webServerAddress, com.houzicore.arcade.bootstrap.ArcadeBootstrap.getInstance().getFeatureGate());
		MountManager mountManager = new MountManager(this, _clientManager, _donationManager, blockRestore, disguiseManager, com.houzicore.arcade.bootstrap.ArcadeBootstrap.getInstance().getFeatureGate());
		GadgetManager gadgetManager = new GadgetManager(this, _clientManager, _donationManager, inventoryManager, mountManager, petManager, preferenceManager, disguiseManager, blockRestore, projectileManager, com.houzicore.arcade.bootstrap.ArcadeBootstrap.getInstance().getFeatureGate());
		CosmeticManager cosmeticManager = new CosmeticManager(this, _clientManager, _donationManager, inventoryManager, gadgetManager, mountManager, petManager, null);
		cosmeticManager.setInterfaceSlot(6);
		
		//Arcade Manager  
		com.houzicore.shared.core.displayentity.DisplayEntityManager displayEntityManager = new com.houzicore.shared.core.displayentity.DisplayEntityManager(this);
		_gameManager = new ArcadeManager(this, serverStatusManager, ReadServerConfig(), _clientManager, _donationManager, statsManager, achievementManager, battlePassManager, disguiseManager, creature, teleport, new Blood(this), chat, portal, preferenceManager, inventoryManager, packetHandler, cosmeticManager, projectileManager, petManager, hologramManager, displayEntityManager, ignoreManager, webServerAddress);
		
		if (org.bukkit.Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null) {
			new ArcadePlaceholderExpansion(_gameManager).register();
		}
		
		new MemoryFix(this);
		new CustomTagFix(this);
		new TablistFix(this, _clientManager);
		
		//Updates
		getServer().getScheduler().scheduleSyncRepeatingTask(this, new Updater(this), 1, 1);
		
		// Remove nasty biomes logic removed for 1.21.1


	}

	@Override 
	public void onDisable() 
	{
		for (Player player : UtilServer.getPlayers())
			player.kickPlayer("Server Shutdown");

		if (_gameManager != null && _gameManager.GetLobby() != null)
			_gameManager.GetLobby().Cleanup();

		if (_gameManager != null && _gameManager.GetGame() != null)
			if (_gameManager.GetGame().WorldData != null)
				_gameManager.GetGame().WorldData.Uninitialize();
	}

	public GameServerConfig ReadServerConfig() 
	{
		GameServerConfig config = new GameServerConfig();

		try
		{
			config.HostName = _serverConfiguration.getServerGroup().getHost();
			config.ServerType = _serverConfiguration.getServerGroup().getServerType();
			config.MinPlayers = _serverConfiguration.getServerGroup().getMinPlayers();
			config.MaxPlayers = _serverConfiguration.getServerGroup().getMaxPlayers();
			config.Tournament = _serverConfiguration.getServerGroup().getTournament();
			config.TournamentPoints = _serverConfiguration.getServerGroup().getTournamentPoints();
			config.TeamRejoin = _serverConfiguration.getServerGroup().getTeamRejoin();
			config.TeamAutoJoin = _serverConfiguration.getServerGroup().getTeamAutoJoin();
			config.TeamForceBalance = _serverConfiguration.getServerGroup().getTeamForceBalance();
			config.GameAutoStart = _serverConfiguration.getServerGroup().getGameAutoStart();
			config.GameTimeout = _serverConfiguration.getServerGroup().getGameTimeout();
			config.RewardGems = _serverConfiguration.getServerGroup().getRewardEssence();
			config.RewardItems = _serverConfiguration.getServerGroup().getRewardItems();
			config.RewardStats = _serverConfiguration.getServerGroup().getRewardStats();
			config.RewardAchievements = _serverConfiguration.getServerGroup().getRewardAchievements();
			config.HotbarInventory = _serverConfiguration.getServerGroup().getHotbarInventory();
			config.HotbarHubClock = _serverConfiguration.getServerGroup().getHotbarHubClock();
			config.PlayerKickIdle = _serverConfiguration.getServerGroup().getPlayerKickIdle();
			
			for (String gameName : _serverConfiguration.getServerGroup().getGames().split(","))
			{
				try
				{
					GameType type = GameType.valueOf(gameName);
					config.GameList.add(type);
				}
				catch (Exception e)
				{
	
				}
			}
		}
		catch (Exception ex)
		{
		}

		if (!config.IsValid())
			config = GetDefaultConfig();

		return config;
	}

	public GameServerConfig GetDefaultConfig()
	{
		GameServerConfig config = new GameServerConfig();

		config.ServerType = "Minigames";
		config.MinPlayers = 2;
		config.MaxPlayers = 16;
		config.Tournament = false;
		config.GameAutoStart = true;
		config.GameTimeout = true;
		config.TeamAutoJoin = true;
		config.RewardGems = true;
		config.RewardItems = true;
		config.RewardStats = true;
		config.RewardAchievements = true;
		config.HotbarInventory = true;
		config.HotbarHubClock = true;
		config.PlayerKickIdle = true;

		// Default game list for standalone mode (no Redis)
		String gamesStr = getConfig().getString("serverstatus.games");
		if (gamesStr != null && !gamesStr.isEmpty()) {
			for (String gameName : gamesStr.split(",")) {
				try {
					config.GameList.add(GameType.valueOf(gameName.trim()));
				} catch (Exception e) {}
			}
		} else {
			String group = System.getProperty("serverstatus.group");
			if (group == null) {
				String name = System.getProperty("serverstatus.name");
				if (name != null && name.contains("-")) {
					group = name.substring(0, name.lastIndexOf("-"));
				}
			}
			
			if (group != null && !group.isEmpty() && !group.equals("MIN")) {
				boolean found = false;
				for (GameType type : GameType.values()) {
					if (type.name().equalsIgnoreCase(group) || type.GetName().equalsIgnoreCase(group)) {
						config.GameList.add(type);
						found = true;
						break;
					}
				}
				if (!found) {
					config.GameList.add(GameType.PropRush);
				}
			} else {
				// Fallback if not defined in config.yml
				config.GameList.add(GameType.PropRush);
			}
		}

		return config;
	}

	public NpcManager getNpcManager()
	{
		return _npcManager;
	}

	private void DeleteFolders() 
	{
		File curDir = new File(".");

		File[] filesList = curDir.listFiles();
		for(File file : filesList)
		{
			if (!file.isDirectory())
				continue;

			if (file.getName().length() < 4)
				continue;

			if (!file.getName().substring(0, 4).equalsIgnoreCase("Game"))
				continue;

			FileUtil.DeleteFolder(file);

		}
	}
}
