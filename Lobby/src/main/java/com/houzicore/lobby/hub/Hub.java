package com.houzicore.lobby.hub;

import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import com.houzicore.shared.CustomTagFix;
import com.houzicore.shared.TablistFix;
import com.houzicore.shared.account.CoreClientManager;
import com.houzicore.shared.core.achievement.AchievementManager;
import com.houzicore.shared.core.antihack.AntiHack;
import com.houzicore.shared.core.aprilfools.AprilFoolsManager;
import com.houzicore.shared.core.blockrestore.BlockRestore;
import com.houzicore.shared.core.chat.Chat;
import com.houzicore.shared.core.command.CommandCenter;
import com.houzicore.shared.core.creature.Creature;
import com.houzicore.shared.core.disguise.DisguiseManager;
import com.houzicore.shared.core.donation.DonationManager;
import com.houzicore.shared.core.elo.EloManager;
import com.houzicore.shared.core.energy.Energy;
import com.houzicore.shared.core.friend.FriendManager;
import com.houzicore.shared.core.give.Give;
import com.houzicore.shared.core.displayentity.DisplayEntityManager;
import com.houzicore.shared.core.hologram.HologramManager;
import com.houzicore.shared.core.ignore.IgnoreManager;
import com.houzicore.shared.core.itemstack.ItemStackFactory;
import com.houzicore.shared.core.memory.MemoryFix;
import com.houzicore.shared.core.message.MessageManager;
import com.houzicore.shared.core.monitor.LagMeter;
import com.houzicore.shared.core.movement.Movement;
import com.houzicore.shared.core.npc.NpcManager;
import com.houzicore.shared.core.packethandler.PacketHandler;
import com.houzicore.shared.core.party.PartyManager;
import com.houzicore.shared.core.personalServer.PersonalServerManager;
import com.houzicore.shared.core.pet.PetManager;
import com.houzicore.shared.core.portal.Portal;
import com.houzicore.shared.core.preferences.PreferencesManager;
import com.houzicore.shared.core.projectile.ProjectileManager;
import com.houzicore.shared.core.punish.Punish;
import com.houzicore.shared.recharge.Recharge;
import com.houzicore.shared.core.resourcepack.ResUnloadCheck;
import com.houzicore.shared.core.resourcepack.ResPackManager;
import com.houzicore.shared.core.serverConfig.ServerConfiguration;
import com.houzicore.shared.core.spawn.Spawn;
import com.houzicore.shared.core.stats.StatsManager;
import com.houzicore.shared.core.status.ServerStatusManager;
import com.houzicore.shared.core.task.TaskManager;
import com.houzicore.shared.core.teleport.Teleport;
// import com.houzicore.shared.updater.FileUpdater; // TODO: Not in Shared JAR
import com.houzicore.shared.updater.Updater;
import com.houzicore.shared.core.visibility.VisibilityManager;
import com.houzicore.lobby.hub.modules.StackerManager;
import com.houzicore.lobby.hub.poll.PollManager;
import com.houzicore.lobby.hub.queue.QueueManager;
import com.houzicore.lobby.hub.server.ServerManager;
import com.houzicore.shared.core.lang.LangManager;
// TODO: classcombat/combat/condition/damage/fire modules not in Shared JAR
// import com.houzicore.shared.core.classcombat.Class.ClassManager;
// import com.houzicore.shared.core.classcombat.Condition.SkillConditionManager;
// import com.houzicore.shared.core.classcombat.Skill.SkillFactory;
// import com.houzicore.shared.core.classcombat.item.ItemFactory;
// import com.houzicore.shared.core.classcombat.shop.ClassCombatShop;
// import com.houzicore.shared.core.classcombat.shop.ClassShopManager;
import com.houzicore.shared.common.IRelation;
import com.houzicore.shared.core.combat.CombatManager;
import com.houzicore.shared.core.combat.legacy.LegacyCombatManager;
import com.houzicore.shared.core.condition.ConditionManager;
import com.houzicore.shared.core.damage.DamageManager;
import com.houzicore.shared.core.fire.Fire;

public class Hub extends JavaPlugin implements IRelation
{
		private static final String WEB_CONFIG = "webServer";
	private static final String REDIS_CONFIG = "use-redis";

	@Override
	public void onEnable()
	{
		// Initialize NpcApi (shaded) for player-skin NPCs
		
		getConfig().addDefault(WEB_CONFIG, "http://localhost/");
		getConfig().addDefault(REDIS_CONFIG, false);
		getConfig().set(WEB_CONFIG, getConfig().getString(WEB_CONFIG));
		if (!getConfig().isSet(REDIS_CONFIG)) getConfig().set(REDIS_CONFIG, false);
		saveConfig();
		
		String webServerAddress = getConfig().getString(WEB_CONFIG);
		boolean useRedis = getConfig().getBoolean(REDIS_CONFIG, false);

		// Propagate Redis setting globally BEFORE any module that uses ServerCommandManager
		com.houzicore.shared.serverdata.commands.ServerCommandManager.setDisabled(!useRedis);

		//Logger.initialize(this);

		//Static Modules
		CommandCenter.Initialize(this);
		CoreClientManager clientManager = new CoreClientManager(this, webServerAddress, useRedis);
		CommandCenter.Instance.setClientManager(clientManager);
		
		// Context API Bootstrap (Track B - Sprint 2)
		com.houzicore.lobby.hub.bootstrap.LobbyBootstrap.init(this);
		com.houzicore.shared.api.feature.FeatureGate featureGate = com.houzicore.lobby.hub.bootstrap.LobbyBootstrap.getInstance().getFeatureGate();
		
		ItemStackFactory.Initialize(this, false);
		Recharge.Initialize(this);
		VisibilityManager.Initialize(this);
		Give.Initialize(this);
		Punish punish = new Punish(this, webServerAddress, clientManager);
		BlockRestore blockRestore = new BlockRestore(this);
		DonationManager donationManager = new DonationManager(this, clientManager, webServerAddress);

		com.houzicore.shared.core.booster.BoosterManager boosterManager = new com.houzicore.shared.core.booster.BoosterManager(this, clientManager, donationManager);
		new com.houzicore.shared.core.thank.ThankManager(this, clientManager, donationManager, boosterManager);
		new ServerConfiguration(this, clientManager);
		
		new com.houzicore.shared.core.announce.AnnounceManager(this, clientManager);
		
		new com.houzicore.shared.core.bonuses.BonusManager(this, clientManager, donationManager);

		//Other Modules
		PacketHandler packetHandler = new PacketHandler(this);
		DisguiseManager disguiseManager = new DisguiseManager(this, packetHandler);
		PreferencesManager preferenceManager = new PreferencesManager(this, clientManager, donationManager);
		Creature creature = new Creature(this);
		NpcManager npcManager = new NpcManager(this, creature);
		PetManager petManager = new PetManager(this, clientManager, donationManager, disguiseManager, creature, blockRestore, webServerAddress, featureGate);
		PollManager pollManager = new PollManager(this, clientManager, donationManager);
		
		// Language manager — must be created before HubManager
		new LangManager(this, preferenceManager);
		
		//Main Modules
		ServerStatusManager serverStatusManager = new ServerStatusManager(this, clientManager, new LagMeter(this, clientManager));
		
		Portal portal = new Portal(this, clientManager, serverStatusManager.getCurrentServerName());

        IgnoreManager ignoreManager = new IgnoreManager(this, clientManager, preferenceManager, portal);

        FriendManager friendManager = new FriendManager(this, clientManager, preferenceManager, portal);        
		
		StatsManager statsManager = new StatsManager(this, clientManager);
		AchievementManager achievementManager = new AchievementManager(statsManager, clientManager, donationManager);
		com.houzicore.shared.core.level.LvlManager lvlManager = new com.houzicore.shared.core.level.LvlManager(this, statsManager, clientManager, donationManager);
		new com.houzicore.shared.core.title.TitleManager(this, clientManager, lvlManager, preferenceManager, donationManager);
		com.houzicore.shared.core.battlepass.BattlePassManager battlePassManager = new com.houzicore.shared.core.battlepass.BattlePassManager(this, clientManager, donationManager);
		com.houzicore.shared.core.quest.QuestManager questManager = new com.houzicore.shared.core.quest.QuestManager(this, clientManager, donationManager);
 
		PartyManager partyManager = new PartyManager(this, portal, clientManager, preferenceManager, ignoreManager);
		com.houzicore.shared.core.clan.ClanManager clanManager = new com.houzicore.shared.core.clan.ClanManager(this, clientManager);
		
		ConditionManager conditionManager = new ConditionManager(this);
		HologramManager hologramManager = new HologramManager(this);
		DisplayEntityManager displayEntityManager = new DisplayEntityManager(this);
		HubManager hubManager = new HubManager(this, blockRestore, clientManager, donationManager, conditionManager, disguiseManager, new TaskManager(this, clientManager, webServerAddress), portal, partyManager, preferenceManager, petManager, pollManager, statsManager, achievementManager, lvlManager, hologramManager, displayEntityManager);
		
		// Prototype NPC V2 Framework Test
		com.houzicore.shared.core.npc.v2.NpcManagerV2 npcManagerV2 = new com.houzicore.shared.core.npc.v2.NpcManagerV2(this, hologramManager);
		org.bukkit.Location spawnLoc = new org.bukkit.Location(org.bukkit.Bukkit.getWorlds().get(0), 3.5, 133, 4.5, 135f, 0f);
		npcManagerV2.registerNpc(new com.houzicore.shared.core.npc.v2.example.KeeperNPC(spawnLoc, donationManager, statsManager));

		com.houzicore.lobby.hub.modules.HubScoreboardManager scoreboardManager = new com.houzicore.lobby.hub.modules.HubScoreboardManager(hubManager, clientManager, donationManager, friendManager, achievementManager);
		new com.houzicore.lobby.hub.modules.HubBossBarManager(this);

		QueueManager queueManager = new QueueManager(this, clientManager, donationManager, new EloManager(this, clientManager), partyManager);
		hubManager.setQueueManager(queueManager);

		new ServerManager(this, clientManager, donationManager, portal, partyManager, serverStatusManager, hubManager, new StackerManager(hubManager), queueManager, npcManager);
		Chat chat = new Chat(this, clientManager, preferenceManager, achievementManager, statsManager, donationManager, serverStatusManager.getCurrentServerName());
		new MessageManager(this, clientManager, preferenceManager, ignoreManager, punish, friendManager, chat);
		
		// new ResPackManager(new ResUnloadCheck() ... );

		new PersonalServerManager(this, clientManager);
		
		// CosmeticManager cosmeticManager = new CosmeticManager(this, clientManager, donationManager, inventoryManager, disguiseManager, petManager, mountManager, gadgetManager, morphManager, particleManager, wardrobeManager, webServerAddress);
		AprilFoolsManager.Initialize(this, clientManager, disguiseManager);
		
		// new HubTeleport(this);

		CombatManager combatManager = new CombatManager(this);
		new LegacyCombatManager(this);
		DamageManager damage = new DamageManager(this, combatManager, npcManager, disguiseManager, conditionManager);
		Fire fire = new Fire(this, conditionManager, damage);
		com.houzicore.shared.core.teleport.Teleport teleport = new com.houzicore.shared.core.teleport.Teleport(this); 
		Energy energy = new Energy(this);
		energy.setEnabled(false);
		
		// ItemFactory itemFactory = new ItemFactory(this, blockRestore, conditionManager, damage, energy, fire, throwManager, webServerAddress);
		// SkillFactory skillManager = new SkillFactory(this, damage, this, combatManager, conditionManager, throwManager, disguiseManager, blockRestore, fire, new Movement(this), teleport, energy, webServerAddress);
		// ClassManager classManager = new ClassManager(this, clientManager, donationManager, skillManager, itemFactory, webServerAddress);
		
        // ClassShopManager shopManager = new ClassShopManager(this, classManager, skillManager, itemFactory, achievementManager, clientManager);
        
        // new ClassCombatShop(shopManager, clientManager, donationManager, false, "Brute", classManager.GetClass("Brute"));
        // new ClassCombatShop(shopManager, clientManager, donationManager, false, "Mage", classManager.GetClass("Mage"));
        // new ClassCombatShop(shopManager, clientManager, donationManager, false, "Ranger", classManager.GetClass("Ranger"));
        // new ClassCombatShop(shopManager, clientManager, donationManager, false, "Knight", classManager.GetClass("Knight"));
        // new ClassCombatShop(shopManager, clientManager, donationManager, false, "Assassin", classManager.GetClass("Assassin"));
        
		//Updates
		getServer().getScheduler().scheduleSyncRepeatingTask(this, new Updater(this), 1, 1);
		
		if (getServer().getPluginManager().getPlugin("PlaceholderAPI") != null) {
			new LobbyPlaceholderExpansion().register();
		} else {
		}
	}

	@Override
	public void onDisable()
	{
	}
	
	@Override
	public boolean canHurt(Player a, Player b)
	{
		return false;
	}

	@Override
	public boolean canHurt(String a, String b)
	{
		return false;
	}

	@Override
	public boolean isSafe(Player a)
	{
		return true;
	}
}
