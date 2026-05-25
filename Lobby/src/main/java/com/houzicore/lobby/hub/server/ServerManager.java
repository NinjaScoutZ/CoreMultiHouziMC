package com.houzicore.lobby.hub.server;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.bukkit.configuration.file.YamlConfiguration;

import com.houzicore.shared.core.lang.LangManager;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityPortalEnterEvent;
import org.bukkit.event.entity.EntityPortalEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerPortalEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.util.Vector;

import com.houzicore.shared.MiniPlugin;
import com.houzicore.shared.account.CoreClientManager;
import com.houzicore.shared.common.Rank;
import com.houzicore.shared.common.util.C;
import com.houzicore.shared.common.util.Callback;
import com.houzicore.shared.common.util.F;
import com.houzicore.shared.common.util.NautHashMap;
import com.houzicore.shared.common.util.UtilAction;
import com.houzicore.shared.common.util.UtilAlg;
import com.houzicore.shared.common.util.UtilPlayer;
import com.houzicore.shared.common.util.UtilTime;
import com.houzicore.shared.common.util.UtilTime.TimeUnit;
import com.houzicore.shared.core.donation.DonationManager;
import com.houzicore.shared.core.itemstack.ItemStackFactory;
import com.houzicore.shared.core.party.Party;
import com.houzicore.shared.core.party.PartyManager;
import com.houzicore.shared.core.portal.Portal;
import com.houzicore.shared.recharge.Recharge;
import com.houzicore.shared.core.shop.ShopBase;
import com.houzicore.shared.core.status.ServerStatusManager;
import com.houzicore.shared.updater.UpdateType;
import com.houzicore.shared.updater.event.UpdateEvent;
import com.houzicore.lobby.hub.HubManager;
import com.houzicore.lobby.hub.modules.StackerManager;
import com.houzicore.lobby.hub.queue.QueueManager;
import com.houzicore.lobby.hub.queue.ui.QueueShop;
import com.houzicore.lobby.hub.server.ui.LobbyShop;
import com.houzicore.lobby.hub.server.ui.QuickShop;
import com.houzicore.lobby.hub.server.ui.ServerNpcShop;
import com.houzicore.shared.api.feature.FeatureKey;
import com.houzicore.shared.serverdata.data.MinecraftServer;
import com.houzicore.shared.serverdata.data.ServerGroup;
import com.houzicore.shared.core.npc.NpcManager;
import com.houzicore.shared.core.npc.Npc;
import com.houzicore.shared.core.npc.event.NpcInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;

public class ServerManager extends MiniPlugin
{
	private static final Long FREE_PORTAL_TIMER = 20000L;
	private static final Long BETA_PORTAL_TIMER = 120000L;

	private CoreClientManager _clientManager;
	private DonationManager _donationManager;
	private Portal _portal;
	private PartyManager _partyManager;
	private ServerStatusManager _statusManager;
	private HubManager _hubManager;
	private StackerManager _stackerManager;
	private QueueManager _queueManager;
	
	private NautHashMap<String, HashSet<ServerInfo>> _serverKeyInfoMap = new NautHashMap<String, HashSet<ServerInfo>>();
	private NautHashMap<String, String> _serverKeyTagMap = new NautHashMap<String, String>();
	private NautHashMap<String, Integer> _serverPlayerCounts = new NautHashMap<String, Integer>();
	private NautHashMap<String, ServerNpcShop> _serverNpcShopMap = new NautHashMap<String, ServerNpcShop>();
	private NautHashMap<String, ServerInfo> _serverInfoMap = new NautHashMap<String, ServerInfo>();
	private NautHashMap<String, Long> _serverUpdate = new NautHashMap<String, Long>();
	private NautHashMap<Vector, String> _serverPortalLocations = new NautHashMap<Vector, String>();
	private NautHashMap<String, Long> _quickMatchLogThrottle = new NautHashMap<String, Long>();

	public static ServerManager Instance;
	private NautHashMap<Player, String> _playerRequests = new NautHashMap<Player, String>();

	public NautHashMap<Player, String> getPlayerRequests() {
		return _playerRequests;
	}

	public NautHashMap<String, HashSet<ServerInfo>> getServerKeyInfoMap() {
		return _serverKeyInfoMap;
	}

	// Join Time for Free Players Timer
	private NautHashMap<String, Long> _joinTime = new NautHashMap<String, Long>();

	private QueueShop _domShop;
	private QuickShop _quickShop;
	private LobbyShop _lobbyShop;
	private NpcManager _npcManager;
	
	private boolean _alternateUpdateFire = false;
	private boolean _retrieving = false;
	private long _lastRetrieve = 0;
	
	public boolean isPortalOpenStatus(ServerInfo serverInfo)
	{
		if (serverInfo == null || serverInfo.MOTD == null) return false;
		String motd = serverInfo.MOTD.toLowerCase();
		return motd.contains("starting") || motd.contains("recruiting") || 
			   motd.contains("waiting") || motd.contains("cup") || 
			   motd.contains("voting") || motd.contains("generating") || 
			   motd.contains("open");
	}
	
	public ServerManager(JavaPlugin plugin, CoreClientManager clientManager, DonationManager donationManager, Portal portal, PartyManager partyManager, ServerStatusManager statusManager, HubManager hubManager, StackerManager stackerManager, QueueManager queueManager, NpcManager npcManager)
	{
		super("Server Manager", plugin);
		Instance = this;
		
		_clientManager = clientManager;
		_donationManager = donationManager;
		_portal = portal;
		_partyManager = partyManager;
		_statusManager = statusManager;
		_hubManager = hubManager;
		_stackerManager = stackerManager;
		_queueManager = queueManager;
		_npcManager = npcManager;
		
		plugin.getServer().getMessenger().registerOutgoingPluginChannel(plugin, "BungeeCord");
		
		LoadServers();
		
		_quickShop = new QuickShop(this, clientManager, donationManager, "Quick Menu");
		_lobbyShop = new LobbyShop(this, clientManager, donationManager, "Lobby Menu");
		
		// Schedule async server status polling — replaces legacy UpdateEvent.SEC loop
		plugin.getServer().getScheduler().runTaskTimerAsynchronously(plugin, () -> {
			if (_retrieving) return;
			if (!UtilTime.elapsed(_lastRetrieve, 4000)) return;
			_retrieving = true;
			_lastRetrieve = System.currentTimeMillis();
			_statusManager.retrieveServerStatuses(data -> {
				if (data == null) { _retrieving = false; return; }
				plugin.getServer().getScheduler().runTask(plugin, () -> {
					applyServerStatuses(data);
					_retrieving = false;
				});
			});
		}, 100L, 20L); // start after 5s, repeat every second
	}
	
	@EventHandler(priority = EventPriority.LOW)
	public void playerPortalEvent(PlayerPortalEvent event)
	{
		event.setCancelled(true);
	}
	
	@EventHandler(priority = EventPriority.LOW)
	public void entityPortalEvent(EntityPortalEvent event)
	{
		event.setCancelled(true);
	}
	
	@EventHandler(priority = EventPriority.LOW)
	public void playerCheckPortalEvent(EntityPortalEnterEvent event)
	{
		if (!(event.getEntity() instanceof Player))
		{
			if (event.getEntity() instanceof LivingEntity)
				UtilAction.velocity(event.getEntity(), UtilAlg.getTrajectory(event.getEntity().getLocation(), _hubManager.GetSpawn()), 1, true, 0.8, 0, 1, true);
			
			return;
		}
		
		final Player player = (Player)event.getEntity();

		if (!Recharge.Instance.use(player, "Portal Server", 1000, false, false))
			return;

		long timeUntilPortal = getMillisecondsUntilPortal(player, false);
		if (!_hubManager.CanPortal(player) || timeUntilPortal > 0)
		{
			player.closeInventory();
			
			if (timeUntilPortal > 0)
			{
				player.playSound(player.getEyeLocation(), Sound.ENTITY_CHICKEN_EGG, 2, 2);
				UtilPlayer.message(player, F.main("Server Portal", LangManager.get().get(player, "server.portal.cooldown", UtilTime.convertString(timeUntilPortal, 0, TimeUnit.SECONDS))));
			}

			UtilAction.velocity(player, UtilAlg.getTrajectory(player.getLocation(), _hubManager.GetSpawn()), 1.5, true, 0.8, 0, 1.0, true);

			// Need to set their velocity again a tick later
			// Setting Y-Velocity while in a portal doesn't seem to do anything... Science!
			_plugin.getServer().getScheduler().runTask(_plugin, new Runnable()
			{

				@Override
				public void run()
				{
					if (player != null && player.isOnline())
					{
						UtilAction.velocity(player, UtilAlg.getTrajectory(player.getLocation(), _hubManager.GetSpawn()), 1, true, 0.5, 0, 1.0, true);
					}
				}
			});

			return;
		}
		
		String serverName = _serverPortalLocations.get(player.getLocation().getBlock().getLocation().toVector());

		if (serverName != null)
		{
			performQuickMatch(player, serverName);
		}
	}
	
	public void performQuickMatch(Player player, String serverKey)
	{
		Collection<ServerInfo> rawList = GetServerList(serverKey);
		if (rawList == null || rawList.isEmpty())
		{
			player.playSound(player.getLocation(), Sound.ENTITY_ITEM_BREAK, 1f, 0.6f);
			if (!isLogThrottled(serverKey)) {
				getPlugin().getLogger().warning("[QuickMatch] UUID=" + player.getUniqueId() + " Target=" + serverKey + " Result=FAIL Reason=Empty List");
			}
			UtilPlayer.message(player, F.main("Server Portal", LangManager.get().get(player, "server.portal.no_servers")));
			return;
		}
		ServerInfo recommendation = getQuickMatchRecommendation(player, serverKey);
		if (recommendation != null)
		{
			getPlugin().getLogger().info("[QuickMatch] UUID=" + player.getUniqueId() + " Target=" + serverKey + " Result=SUCCESS Server=" + recommendation.Name);
			player.playSound(player.getLocation(), Sound.BLOCK_AMETHYST_BLOCK_CHIME, 0.75f, 1.45f);
			SelectServer(player, recommendation);
			return;
		}
		
		if (!isLogThrottled(serverKey)) {
			getPlugin().getLogger().warning("[QuickMatch] UUID=" + player.getUniqueId() + " Target=" + serverKey + " Result=FAIL Reason=All Full Or Offline");
		}
		player.playSound(player.getLocation(), Sound.ENTITY_ITEM_BREAK, 1f, 0.6f);
		UtilPlayer.message(player, F.main("Server Portal", LangManager.get().get(player, "server.portal.no_servers")));
	}

	private List<ServerInfo> getServerListSnapshot(String serverKey)
	{
		Collection<ServerInfo> rawList = GetServerList(serverKey);
		List<ServerInfo> serverList = new ArrayList<ServerInfo>();

		if (rawList == null)
		{
			return serverList;
		}

		for (ServerInfo info : rawList)
		{
			if (info != null)
			{
				serverList.add(info);
			}
		}

		return serverList;
	}

	private int getRequiredSlotsForQuickMatch(Player player, List<ServerInfo> serverList)
	{
		if (player == null || serverList.isEmpty() || serverList.get(0) == null)
		{
			return 1;
		}

		return GetRequiredSlots(player, serverList.get(0).ServerType);
	}

	private boolean isQuickMatchJoinable(ServerInfo serverInfo, int slots)
	{
		return serverInfo != null
				&& serverInfo.Status == ServerStatusType.STARTING
				&& (serverInfo.MaxPlayers - serverInfo.CurrentPlayers) >= slots;
	}

	private int getQuickMatchScore(ServerInfo serverInfo, int slots)
	{
		if (!isQuickMatchJoinable(serverInfo, slots))
		{
			return Integer.MIN_VALUE;
		}

		int score = 0;
		String motd = serverInfo.MOTD == null ? "" : ChatColor.stripColor(serverInfo.MOTD).toLowerCase();
		int freeSlots = Math.max(0, serverInfo.MaxPlayers - serverInfo.CurrentPlayers);

		if (motd.contains("recruiting"))
			score += 80;
		else if (motd.contains("waiting"))
			score += 72;
		else if (motd.contains("starting"))
			score += 64;
		else if (motd.contains("open"))
			score += 58;
		else if (motd.contains("voting"))
			score += 50;
		else if (motd.contains("generating"))
			score += 34;
		else if (motd.contains("cup"))
			score += 20;

		score += Math.min(serverInfo.CurrentPlayers, 24) * 4;
		score += Math.min(freeSlots, 8) * 2;

		if (serverInfo.CurrentPlayers == 0)
			score -= 10;

		if (freeSlots <= slots + 2)
			score += 8;

		return score;
	}

	public ServerInfo getQuickMatchRecommendation(Player player, String serverKey)
	{
		List<ServerInfo> serverList = getServerListSnapshot(serverKey);
		int slots = getRequiredSlotsForQuickMatch(player, serverList);
		ServerInfo best = null;
		int bestScore = Integer.MIN_VALUE;

		try
		{
			Collections.sort(serverList, new ServerSorter(slots));
		}
		catch (Exception exception)
		{
			exception.printStackTrace();
		}

		for (ServerInfo serverInfo : serverList)
		{
			int score = getQuickMatchScore(serverInfo, slots);

			if (score > bestScore)
			{
				best = serverInfo;
				bestScore = score;
				continue;
			}

			if (score == bestScore && best != null && serverInfo != null && serverInfo.CurrentPlayers > best.CurrentPlayers)
			{
				best = serverInfo;
			}
		}

		return bestScore == Integer.MIN_VALUE ? null : best;
	}

	public int getJoinableServerCount(Player player, String serverKey)
	{
		List<ServerInfo> serverList = getServerListSnapshot(serverKey);
		int slots = getRequiredSlotsForQuickMatch(player, serverList);
		int count = 0;

		for (ServerInfo serverInfo : serverList)
		{
			if (isQuickMatchJoinable(serverInfo, slots))
			{
				count++;
			}
		}

		return count;
	}

	public int getOnlineShardCount(String serverKey)
	{
		int count = 0;

		for (ServerInfo serverInfo : getServerListSnapshot(serverKey))
		{
			if (serverInfo.Status != ServerStatusType.OFFLINE)
			{
				count++;
			}
		}

		return count;
	}

	public int getTotalPlayers(String serverKey)
	{
		int total = 0;

		for (ServerInfo serverInfo : getServerListSnapshot(serverKey))
		{
			total += Math.max(0, serverInfo.CurrentPlayers);
		}

		return total;
	}

	private String getQuickMatchStatusKey(ServerInfo serverInfo)
	{
		if (serverInfo == null || serverInfo.MOTD == null)
		{
			return "server.quickmatch.status.unknown";
		}

		String motd = ChatColor.stripColor(serverInfo.MOTD).toLowerCase();

		if (motd.contains("recruiting"))
			return "server.quickmatch.status.recruiting";
		if (motd.contains("waiting"))
			return "server.quickmatch.status.waiting";
		if (motd.contains("starting"))
			return "server.quickmatch.status.starting";
		if (motd.contains("open"))
			return "server.quickmatch.status.open";
		if (motd.contains("voting"))
			return "server.quickmatch.status.voting";
		if (motd.contains("generating"))
			return "server.quickmatch.status.generating";
		if (motd.contains("offline"))
			return "server.quickmatch.status.offline";
		if (motd.contains("progress") || motd.contains("restarting"))
			return "server.quickmatch.status.live";

		return "server.quickmatch.status.unknown";
	}

	public String getQuickMatchRecommendationText(Player player, String serverKey)
	{
		ServerInfo recommendation = getQuickMatchRecommendation(player, serverKey);

		if (recommendation == null)
		{
			return LangManager.get().get(player, "server.quickmatch.none");
		}

		String status = LangManager.get().get(player, getQuickMatchStatusKey(recommendation));
		return recommendation.Name + " • " + status + " • " + recommendation.CurrentPlayers + "/" + recommendation.MaxPlayers;
	}

	public String getQuickMatchAvailabilityText(Player player, String serverKey)
	{
		int openCount = getJoinableServerCount(player, serverKey);
		int totalPlayers = getTotalPlayers(serverKey);

		if (openCount > 0)
		{
			return LangManager.get().get(player, "server.quickmatch.open_now", openCount) + " • "
					+ LangManager.get().get(player, "server.quickmatch.playing", totalPlayers);
		}

		return LangManager.get().get(player, "server.quickmatch.none") + " • "
				+ LangManager.get().get(player, "server.quickmatch.playing", totalPlayers);
	}

	public String getServerBrowserStatusText(Player player, String serverKey)
	{
		return LangManager.get().get(player, "server.quickmatch.shards_online", getOnlineShardCount(serverKey))
				+ " • " + LangManager.get().get(player, "server.quickmatch.playing", getTotalPlayers(serverKey));
	}

	public String getNpcStatusLine(String serverKey)
	{
		int totalPlayers = getTotalPlayers(serverKey);
		int onlineShards = getOnlineShardCount(serverKey);

		if (onlineShards <= 0 && totalPlayers <= 0)
		{
			return "§c§lOFFLINE";
		}

		return "§f§l" + totalPlayers + " Players";
	}
	
	private boolean isLogThrottled(String serverKey) {
		if (_quickMatchLogThrottle.containsKey(serverKey) && System.currentTimeMillis() - _quickMatchLogThrottle.get(serverKey) < 10000) {
			return true;
		}
		// Cleanup map while we are here
		_quickMatchLogThrottle.entrySet().removeIf(entry -> System.currentTimeMillis() - entry.getValue() >= 10000);
		_quickMatchLogThrottle.put(serverKey, System.currentTimeMillis());
		return false;
	}
	
	@EventHandler
	public void checkQueuePrompts(UpdateEvent event)
	{
		if (event.getType() != UpdateType.SEC)
			return;
		
		/*
		for (final Player player : _queueManager.findPlayersNeedingPrompt())
		{
			player.playSound(player.getLocation(), Sound.ENDERDRAGON_GROWL, 5f, 1f);
			
			Bukkit.getScheduler().runTaskLater(getPlugin(), new Runnable()
			{
				public void run()
				{
					if (player.isOnline())
					{
						_domShop.attemptShopOpen(player);						
					}
				}
			}, 20);
		}
		*/
	}
	
	@EventHandler(priority = EventPriority.LOW)
	public void playerJoin(PlayerJoinEvent event)
	{
		if (_clientManager.Get(event.getPlayer()).GetRank() == Rank.ALL)
		{
			_joinTime.put(event.getPlayer().getName(), System.currentTimeMillis());
		}
	}

	@EventHandler
	public void playerQuit(PlayerQuitEvent event)
	{
		_joinTime.remove(event.getPlayer().getName());
		_playerRequests.remove(event.getPlayer());
	}
	
	@EventHandler
	public void checkPlayerRequests(UpdateEvent event)
	{
		if (event.getType() != UpdateType.SEC) return;
		
		java.util.Iterator<java.util.Map.Entry<Player, String>> it = _playerRequests.entrySet().iterator();
		while (it.hasNext()) {
			java.util.Map.Entry<Player, String> entry = it.next();
			Player player = entry.getKey();
			String group = entry.getValue();
			
			if (player == null || !player.isOnline()) {
				it.remove();
				continue;
			}
			
			ServerInfo recommendation = getQuickMatchRecommendation(player, group);
			if (recommendation != null) {
				com.houzicore.shared.common.util.UtilPlayer.message(player, com.houzicore.shared.common.util.F.main("Server", "§a" + group + " is ready! Routing..."));
				SelectServer(player, recommendation);
				it.remove();
			}
		}
	}
	
	@EventHandler(priority = EventPriority.LOWEST)
	public void playerInteract(PlayerInteractEvent event)
	{
		if (event.getHand() != EquipmentSlot.HAND)
		{
			return;
		}

		com.houzicore.shared.api.feature.FeatureGate gate = com.houzicore.lobby.hub.bootstrap.LobbyBootstrap.getInstance().getFeatureGate();
		if (gate != null && !gate.isAllowed(event.getPlayer(), FeatureKey.LOBBY_ITEM_USE))
		{
			return;
		}

		if (event.getItem() != null && event.getItem().getType() == Material.COMPASS)
		{
			event.setCancelled(true);
			event.getPlayer().playSound(event.getPlayer().getLocation(), Sound.UI_BUTTON_CLICK, 0.7f, 1.2f);
			event.getPlayer().playSound(event.getPlayer().getLocation(), Sound.BLOCK_AMETHYST_BLOCK_CHIME, 0.45f, 1.8f);
			_quickShop.attemptShopOpen(event.getPlayer());
		}
		else if (event.getItem() != null && event.getItem().getType() == Material.CLOCK)
		{
			event.setCancelled(true);
			event.getPlayer().playSound(event.getPlayer().getLocation(), Sound.UI_BUTTON_CLICK, 0.7f, 1.05f);
			event.getPlayer().playSound(event.getPlayer().getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 0.55f, 1.45f);
			_lobbyShop.attemptShopOpen(event.getPlayer());
		}
	}

	public Long getMillisecondsUntilPortal(Player player, boolean beta)
	{
//		Party party = _partyManager.getPartyByPlayer(player);
		long timeLeft = 0;

		if (_joinTime.containsKey(player.getName()))
		{
			timeLeft = (_joinTime.get(player.getName()) - System.currentTimeMillis()) + (beta ? BETA_PORTAL_TIMER : FREE_PORTAL_TIMER);
			
			if (timeLeft <= 0)
				timeLeft = 0;
		}

//		if (party != null)
//		{
//			if (player.getName().equals(party.getLeaderName()))
//			{
//				for (Player partyPlayer : party.GetPlayersOnline())
//				{
//					if (!partyPlayer.equals(player))
//						timeLeft = Math.max(timeLeft, getMillisecondsUntilPortal(partyPlayer));
//				}
//			}
//		}

		return timeLeft;
	}
	
	public void RemoveServer(String serverName)
	{
		for (String key : _serverKeyInfoMap.keySet())
		{
			_serverKeyInfoMap.get(key).removeIf(info -> info.Name.equalsIgnoreCase(serverName));
		}
		
		_serverInfoMap.remove(serverName);
	}
	
	public void addServerGroup(String serverKey, String...serverTag)
	{
		_serverKeyInfoMap.put(serverKey, new HashSet<ServerInfo>());
		
		for (String tag : serverTag)
			_serverKeyTagMap.put(tag, serverKey);
	}
	
	@org.bukkit.event.EventHandler
	public void onNpcInteract(NpcInteractEntityEvent event) {
		if (_npcManager == null) return;
		Npc npc = _npcManager.getNpcByEntity(event.getNpc());
		if (npc != null) {
			String name = npc.getDatabaseRecord().getName();
			if (name != null && _serverNpcShopMap.containsKey(name)) {
				_serverNpcShopMap.get(name).attemptShopOpen(event.getPlayer());
			} else if (name != null) {
				getPlugin().getLogger().warning("[ServerManager] NPC clicked: '" + name + "' but no shop found. Available: " + _serverNpcShopMap.keySet());
			}
		}
	}
	
	public void openServerShop(Player player, String serverNpcName) {
		if (_serverNpcShopMap.containsKey(serverNpcName)) {
			_serverNpcShopMap.get(serverNpcName).attemptShopOpen(player);
		} else {
			getPlugin().getLogger().warning("[ServerManager] No shop found for manual open: " + serverNpcName);
		}
	}

	public void quickJoin(Player player, String serverKey) {
		java.util.Set<ServerInfo> servers = _serverKeyInfoMap.get(serverKey);
		if (servers == null || servers.isEmpty()) {
			UtilPlayer.message(player, F.main(getName(), "ไม่มีห้องให้เข้าร่วมในขณะนี้ (No servers available)"));
			return;
		}

		ServerInfo bestServer = null;
		int highestPlayers = -1;

		for (ServerInfo server : servers) {
			if (server.CurrentPlayers >= server.MaxPlayers) continue;
			if (server.CurrentPlayers > highestPlayers) {
				highestPlayers = server.CurrentPlayers;
				bestServer = server;
			}
		}

		if (bestServer != null) {
			player.sendTitle("§a§lJOINING...", "§f" + bestServer.Name, 5, 30, 5);
			SelectServer(player, bestServer);
		} else {
			UtilPlayer.message(player, F.main(getName(), "ทุกห้องเต็มแล้ว! (All servers are full!)"));
		}
	}

	
	public void AddServerNpc(String serverNpcName, String...serverTag)
	{
		addServerGroup(serverNpcName, serverTag);
		_serverNpcShopMap.put(serverNpcName, new ServerNpcShop(this, _clientManager, _donationManager, serverNpcName));
	}
	
	public void RemoveServerNpc(String serverNpcName)
	{
		Set<ServerInfo> mappedServers = _serverKeyInfoMap.remove(serverNpcName);
		_serverNpcShopMap.remove(serverNpcName);
		
		if (mappedServers != null)
		{
			for (ServerInfo mappedServer : mappedServers)
			{
				boolean isMappedElseWhere = false;
				
				for (String key : _serverKeyInfoMap.keySet())
				{
					for (ServerInfo value : _serverKeyInfoMap.get(key))
					{
						if (value.Name.equalsIgnoreCase(mappedServer.Name))
						{
							isMappedElseWhere = true;
							break;
						}
					}
					
					if (isMappedElseWhere)
						break;
				}
				
				if (!isMappedElseWhere)
					_serverInfoMap.remove(mappedServer.Name);
			}
		}
	}
	
	public Collection<ServerInfo> GetServerList(String serverNpcName)
	{
		return _serverKeyInfoMap.get(serverNpcName);
	}
	
	public Set<String> GetAllServers()
	{
		return _serverInfoMap.keySet();
	}
	
	public ServerInfo GetServerInfo(String serverName)
	{
		return _serverInfoMap.get(serverName);
	}
	
	public boolean HasServerNpc(String serverNpcName) 
	{
		return _serverKeyInfoMap.containsKey(serverNpcName);
	}
	
	@Override
	public void addCommands()
	{
		addCommand(new com.houzicore.lobby.hub.commands.LobbyReloadCommand(this));
		addCommand(new com.houzicore.lobby.hub.commands.GameMenuCommand(this));
		addCommand(new com.houzicore.lobby.hub.commands.LobbyMenuCommand(this));
	}

	@EventHandler
	public void updatePages(UpdateEvent event)
	{
		if (event.getType() != UpdateType.SEC)
			return;
		
		_quickShop.UpdatePages();
		
		for (ServerNpcShop shop : _serverNpcShopMap.values())
		{
			shop.UpdatePages();
		}
	}
	
	// Called on main thread with fresh data from async fetch
	private void applyServerStatuses(java.util.Collection<com.houzicore.shared.serverdata.data.MinecraftServer> data)
	{
		for (MinecraftServer server : data)
		{
			String name = server.getName();
			String tag = name.contains("-") ? name.substring(0, name.indexOf("-")) : name;

			ServerInfo info = _serverInfoMap.get(name);
			if (info == null)
			{
				info = new ServerInfo();
				info.Name = name;
				info.Game = server.getGroup();
				info.ServerType = tag;
				info.Map = "Random";
				info.HostedByStaff = false;
				_serverInfoMap.put(name, info);
				if (_serverKeyTagMap.containsKey(tag))
					_serverKeyInfoMap.get(_serverKeyTagMap.get(tag)).add(info);
			}

			info.MOTD = server.getMotd();
			info.Status = ServerStatusType.parse(info.MOTD);
			info.CurrentPlayers = server.getPlayerCount();
			info.MaxPlayers = server.getMaxPlayerCount();
			_serverUpdate.put(name, System.currentTimeMillis());
		}

		_serverPlayerCounts.clear();
		for (ServerInfo info : _serverInfoMap.values())
		{
			if (!_serverUpdate.containsKey(info.Name) || System.currentTimeMillis() - _serverUpdate.get(info.Name) > 40000)
			{
				info.MOTD = ChatColor.DARK_RED + "OFFLINE";
				info.Status = ServerStatusType.OFFLINE;
				info.CurrentPlayers = 0;
			}
			String tag = info.Name.contains("-") ? info.Name.substring(0, info.Name.indexOf("-")) : info.Name;
			if (_serverKeyTagMap.containsKey(tag))
			{
				int oldPlayerCount = _serverPlayerCounts.containsKey(tag) ? _serverPlayerCounts.get(tag) : 0;
				_serverPlayerCounts.put(tag, oldPlayerCount + info.CurrentPlayers);
			}
		}

		if (_npcManager != null)
		{
			for (Npc npc : _npcManager.getNpcs())
			{
				String serverNpcName = npc.getDatabaseRecord().getName();
				if (serverNpcName != null && _serverKeyInfoMap.containsKey(serverNpcName))
				{
					npc.setExtraHologramLine(getNpcStatusLine(serverNpcName));
					if (npc.getEntity() != null) com.houzicore.shared.common.util.UtilEnt.Vegetate(npc.getEntity());
				}
			}
		}
	}

	public void reloadNpcMappings()
	{
		_serverKeyInfoMap.clear();
		_serverKeyTagMap.clear();
		_serverNpcShopMap.clear();
		LoadNpcMappingsYaml();
		getPlugin().getLogger().info("[ServerManager] NPC mappings hot-reloaded ("
			+ _serverNpcShopMap.size() + " NPCs registered)");
	}

	public void Help(Player caller, String message)
	{
		
		UtilPlayer.message(caller, F.main(_moduleName, com.houzicore.shared.core.lang.LangManager.get().get(caller, "hub.servernpc.command_list")));
		UtilPlayer.message(caller, F.help("/servernpc create <name>", com.houzicore.shared.core.lang.LangManager.get().get(caller, "hub.servernpc.help_create"), Rank.OWNER));
		UtilPlayer.message(caller, F.help("/servernpc delete <name>", com.houzicore.shared.core.lang.LangManager.get().get(caller, "hub.servernpc.help_create"), Rank.OWNER));
		UtilPlayer.message(caller, F.help("/servernpc addserver <servernpc> | <name>", com.houzicore.shared.core.lang.LangManager.get().get(caller, "hub.servernpc.help_add"), Rank.OWNER));
		UtilPlayer.message(caller, F.help("/servernpc removeserver <name>", com.houzicore.shared.core.lang.LangManager.get().get(caller, "hub.servernpc.help_remove"), Rank.OWNER));
		UtilPlayer.message(caller, F.help("/servernpc listnpcs", com.houzicore.shared.core.lang.LangManager.get().get(caller, "hub.servernpc.help_list"), Rank.OWNER));
		UtilPlayer.message(caller, F.help("/servernpc listservers <servernpc>", com.houzicore.shared.core.lang.LangManager.get().get(caller, "hub.servernpc.help_listservers"), Rank.OWNER));
		UtilPlayer.message(caller, F.help("/servernpc listoffline", com.houzicore.shared.core.lang.LangManager.get().get(caller, "hub.servernpc.help_listoffline"), Rank.OWNER));
		
		if (message != null)
			UtilPlayer.message(caller, F.main(_moduleName, ChatColor.RED + message));
	}
	
	public void Help(Player caller)
	{
		Help(caller, null);
	}

	public PartyManager getPartyManager()
	{
		return _partyManager;
	}
	
	public void SelectServer(org.bukkit.entity.Player player, ServerInfo serverInfo)
	{
		Party party = _partyManager.getPartyByPlayer(player);
		
		if (party == null || player.getName().equals(party.getLeaderName()))
		{
			player.leaveVehicle();
			player.eject();
			player.playSound(player.getLocation(), Sound.BLOCK_AMETHYST_BLOCK_CHIME, 0.8f, 1.35f);
			player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 0.75f, 1.8f);
			
			_portal.sendPlayerToServer(player, serverInfo.Name);
		}
	}

	public void ListServerNpcs(Player caller)
	{
		
		UtilPlayer.message(caller, F.main(getName(), com.houzicore.shared.core.lang.LangManager.get().get(caller, "hub.servernpc.npc_list")));
		
		for (String serverNpc : _serverKeyInfoMap.keySet())
		{
			UtilPlayer.message(caller, F.main(getName(), C.cYellow + serverNpc));
		}
	}
	
	public void ListServers(Player caller, String serverNpcName)
	{
		
		UtilPlayer.message(caller, F.main(getName(), com.houzicore.shared.core.lang.LangManager.get().get(caller, "hub.servernpc.server_list", serverNpcName)));
		
		for (ServerInfo serverNpc : _serverKeyInfoMap.get(serverNpcName))
		{
			UtilPlayer.message(caller, F.main(getName(), C.cYellow + serverNpc.Name +  C.cWhite + " - " + serverNpc.MOTD + " " + serverNpc.CurrentPlayers + "/" + serverNpc.MaxPlayers));
		}
	}
	
	public void ListOfflineServers(Player caller)
	{
		
		UtilPlayer.message(caller, F.main(getName(), com.houzicore.shared.core.lang.LangManager.get().get(caller, "hub.servernpc.offline_list")));
		
		for (ServerInfo serverNpc : _serverInfoMap.values())
		{
			if (serverNpc.MOTD.equalsIgnoreCase(ChatColor.DARK_RED + "OFFLINE"))
			{
				UtilPlayer.message(caller, F.main(getName(), C.cYellow + serverNpc.Name +  C.cWhite + " - " + F.time(UtilTime.convertString(System.currentTimeMillis() - _serverUpdate.get(serverNpc.Name), 0, TimeUnit.FIT))));
			}
		}
	}
	
	public void LoadServers()
	{
		_serverInfoMap.clear();
		_serverUpdate.clear();
		
		for (String npcName : _serverKeyInfoMap.keySet())
		{
			_serverKeyInfoMap.get(npcName).clear();
		}
		
		_serverKeyTagMap.clear();

		LoadNpcMappingsYaml();

		FileInputStream fstream = null;
		BufferedReader br = null;
		
		HashSet<String> npcNames = new HashSet<String>();
		
		try
		{
			File npcFile = new File("ServerManager.dat");

			if (npcFile.exists())
			{
				fstream = new FileInputStream(npcFile);
				br = new BufferedReader(new InputStreamReader(fstream));
				
				String line = br.readLine();
				
				while (line != null)
				{
					String serverNpcName = line.substring(0, line.indexOf('|')).trim();
					String[] serverTags = line.substring(line.indexOf('|') + 1, line.indexOf('|', line.indexOf('|') + 1)).trim().split(",");
					String[] locations = line.substring(line.indexOf('|', line.indexOf('|') + 1) + 1).trim().split(",");

					for (String location : locations)
					{
						_serverPortalLocations.put(ParseVector(location), serverNpcName);
					}
					
					if (!HasServerNpc(serverNpcName))
					{
						AddServerNpc(serverNpcName, serverTags);
					}
					
					npcNames.add(serverNpcName);
					
					line = br.readLine();
				}
			}
		}
		catch (Exception e)
		{
			getPlugin().getLogger().severe("Failed to map ServerManager.dat locations: " + e.getMessage());
			e.printStackTrace();
		}
		finally
		{
			if (br != null)
			{
				try
				{
					br.close();
				} 
				catch (IOException e)
				{
					e.printStackTrace();
				}
			}
			
			if (fstream != null)
			{
				try
				{
					fstream.close();
				} 
				catch (IOException e)
				{
					e.printStackTrace();
				}
			}
		}
		
		for (String npcName : npcNames)
		{
			if (!_serverNpcShopMap.containsKey(npcName))
				_serverNpcShopMap.remove(npcName);
			
			if (!_serverKeyInfoMap.containsKey(npcName))
				_serverKeyInfoMap.remove(npcName);
		}
		
		LoadGameServersYaml();
	}

	private void LoadNpcMappingsYaml()
	{
		File file = new File(_plugin.getDataFolder(), "npc-mappings.yml");
		YamlConfiguration config = YamlConfiguration.loadConfiguration(file);
		
		if (!file.exists())
		{
			// Default mappings if file doesn't exist
			config.set("npc_mappings.The Bridges", java.util.Arrays.asList("BR"));
			config.set("npc_mappings.Survival Primal Games", java.util.Arrays.asList("HG", "Primal"));
			config.set("npc_mappings.SkyWars", java.util.Arrays.asList("SKY"));
			config.set("npc_mappings.Wizards", java.util.Arrays.asList("WIZ"));
			config.set("npc_mappings.Castle Siege", java.util.Arrays.asList("CS"));
			config.set("npc_mappings.Prop Rush", java.util.Arrays.asList("BH", "HideSeek", "HS"));
			config.set("npc_mappings.Super Smash Mobs", java.util.Arrays.asList("SSM"));
			config.set("npc_mappings.MineStrike", java.util.Arrays.asList("MS"));
			config.set("npc_mappings.Draw My Thing", java.util.Arrays.asList("DMT"));
			config.set("npc_mappings.Dominate", java.util.Arrays.asList("DOM"));
			config.set("npc_mappings.Team Deathmatch", java.util.Arrays.asList("TDM"));
			config.set("npc_mappings.Master Builders", java.util.Arrays.asList("BLD"));
			config.set("npc_mappings.Mixed Arcade", java.util.Arrays.asList("MIN", "DR", "DE", "PB", "TF", "RUN", "SN", "DT", "SQ", "SA", "SS", "OITQ"));
			config.set("npc_mappings.Beta Games", java.util.Arrays.asList("BETA"));
			config.set("npc_mappings.HouziCore Player Servers", java.util.Arrays.asList("PLAYER"));
			
			try {
				config.save(file);
			} catch (IOException e) {
				getPlugin().getLogger().warning("Could not save npc-mappings.yml: " + e.getMessage());
			}
		}

		if (config.contains("npc_mappings"))
		{
			for (String key : config.getConfigurationSection("npc_mappings").getKeys(false))
			{
				List<String> tags = config.getStringList("npc_mappings." + key);
				if (tags != null && !tags.isEmpty())
				{
					AddServerNpc(key, tags.toArray(new String[0]));
				}
			}
		}
	}

	private void LoadGameServersYaml()
	{
		File file = new File(_plugin.getDataFolder(), "game-servers.yml");
		if (!file.exists()) return;

		YamlConfiguration config = YamlConfiguration.loadConfiguration(file);
		List<java.util.Map<?, ?>> list = config.getMapList("servers");
		for (java.util.Map<?, ?> map : list)
		{
			String name = (String) map.get("name");
			String tag = (String) map.get("tag");
			int slots = (Integer) map.get("slots");

			String serverName = tag + "-1";
			ServerInfo newServerInfo = new ServerInfo();
			newServerInfo.Name = serverName;
			newServerInfo.MOTD = "Waiting...";
			newServerInfo.CurrentPlayers = 0;
			newServerInfo.MaxPlayers = slots;
			newServerInfo.HostedByStaff = false;
			newServerInfo.ServerType = name;
			newServerInfo.Game = name;
			newServerInfo.Map = "Random";

			_serverInfoMap.put(serverName, newServerInfo);
			_serverUpdate.put(serverName, System.currentTimeMillis());

			if (_serverKeyTagMap.containsKey(tag))
			{
				_serverKeyInfoMap.get(_serverKeyTagMap.get(tag)).add(newServerInfo);
				if (!_serverPlayerCounts.containsKey(tag))
					_serverPlayerCounts.put(tag, 0);
				_serverPlayerCounts.put(tag, _serverPlayerCounts.get(tag) + newServerInfo.CurrentPlayers);
			}
		}
	}

	public int GetRequiredSlots(Player player, String serverType)
	{
		int slots = 0;
		
		Party party = _partyManager.getPartyByPlayer(player);
		
		if (party != null)
		{
			if (player.getName().equals(party.getLeaderName()))
			{
				for (String name : party.GetPlayers())
				{
					Player partyPlayer = UtilPlayer.searchExact(name);
					
					if (partyPlayer == null)
						continue;
					
					if (_clientManager.Get(partyPlayer).GetRank().Has(Rank.WARRIOR) || _donationManager.Get(partyPlayer.getName()).OwnsUnknownPackage(serverType + " ULTRA"))
						continue;
					
					slots++;
				}
			}
		}
		else
		{
			if (!_clientManager.Get(player).GetRank().Has(Rank.WARRIOR) && !_donationManager.Get(player.getName()).OwnsUnknownPackage(serverType + " ULTRA"))
				slots++;
		}
		
		return slots;
	}

	public ServerNpcShop getMixedArcadeShop()
	{
		return _serverNpcShopMap.get("Mixed Arcade");
	}

	public ServerNpcShop getSuperSmashMobsShop()
	{
		return _serverNpcShopMap.get("Super Smash Mobs");
	}

	@SuppressWarnings("rawtypes")
	public ShopBase getDominateShop()
	{
		return _serverNpcShopMap.get("Dominate");
	}

	public ServerNpcShop getBridgesShop()
	{
		return _serverNpcShopMap.get("The Bridges");
	}
	
	public ServerNpcShop getSurvivalGamesShop()
	{
		return _serverNpcShopMap.get("Survival Primal Game");
	}

	public ServerNpcShop getBlockHuntShop()
	{
		return _serverNpcShopMap.get("Prop Rush");
	}

	public ServerNpcShop getBetaShop()
	{
		return _serverNpcShopMap.get("Beta Games");
	}

	public ServerNpcShop getUHCShop()
	{
		return _serverNpcShopMap.get("Ultra Hardcore");
	}

	public ServerNpcShop getSKYShop()
	{
		return _serverNpcShopMap.get("SkyWars");
	}

	public ServerNpcShop getPlayerGamesShop()
	{
		return _serverNpcShopMap.get("HouziCore Player Servers");
	}
	
	private Vector ParseVector(String vectorString)
	{
		Vector vector = new Vector();
		
		String [] parts = vectorString.trim().split(" ");
		
		vector.setX(Double.parseDouble(parts[0]));
		vector.setY(Double.parseDouble(parts[1]));
		vector.setZ(Double.parseDouble(parts[2]));
		
		return vector;
	}

	public ServerStatusManager getStatusManager()
	{
		return _statusManager;
	}

	public ShopBase<ServerManager> getCastleSiegeShop()
	{
		return _serverNpcShopMap.get("Castle Siege");
	}

	public HubManager getHubManager()
	{
		return _hubManager;
	}

	public ShopBase<ServerManager> getDrawMyThingShop()
	{
		return _serverNpcShopMap.get("Draw My Thing");
	}

	public ShopBase<ServerManager> getTeamDeathmatchShop()
	{
		return _serverNpcShopMap.get("Team Deathmatch");
	}

	public ShopBase<ServerManager> getMinestrikeShop()
	{
		return _serverNpcShopMap.get("Mine-Strike");
	}

	public ShopBase<ServerManager> getWizardShop()
	{
		return _serverNpcShopMap.get("Wizards");
	}

	public int getGroupTagPlayerCount(String tag)
	{
		if (_serverPlayerCounts.containsKey(tag))
			return _serverPlayerCounts.get(tag);
		else
			return 0;
	}

	public ShopBase<ServerManager> getBuildShop()
	{
		return _serverNpcShopMap.get("Master Builders");
	}

	public QuickShop getQuickShop()
	{
		return _quickShop;
	}

	public LobbyShop getLobbyShop()
	{
		return _lobbyShop;
	}
	public boolean isGroupOnline(String... tags)
	{
		for (String tag : tags)
		{
			if (!_serverKeyTagMap.containsKey(tag)) continue;
			String key = _serverKeyTagMap.get(tag);
			if (!_serverKeyInfoMap.containsKey(key)) continue;
			for (ServerInfo info : _serverKeyInfoMap.get(key))
			{
				if (info.Status != ServerStatusType.OFFLINE) return true;
			}
		}
		return false;
	}

	public void requestServer(Player player, String serverGroup) {
		org.bukkit.Bukkit.getScheduler().runTaskAsynchronously(getPlugin(), () -> {
			try {
				java.net.URL url = new java.net.URL("http://127.0.0.1:23333/api/templates/MIN/start");
				java.net.HttpURLConnection conn = (java.net.HttpURLConnection) url.openConnection();
				conn.setRequestMethod("POST");
				conn.setRequestProperty("x-admin-token", "houzi");
				conn.setRequestProperty("Content-Type", "application/json");
				conn.setDoOutput(true);
				
				String jsonPayload = "{\"prefix\": \"" + serverGroup + "\"}";
				try (java.io.OutputStream os = conn.getOutputStream()) {
					byte[] input = jsonPayload.getBytes("utf-8");
					os.write(input, 0, input.length);
				}
				
				int code = conn.getResponseCode();
				if (code == 200) {
					org.bukkit.Bukkit.getScheduler().runTask(getPlugin(), () -> {
						_playerRequests.put(player, serverGroup);
					});
					com.houzicore.shared.common.util.UtilPlayer.message(player, com.houzicore.shared.common.util.F.main("Server", "§a" + serverGroup + " server request successful! Please wait a moment for it to start."));
				} else {
					com.houzicore.shared.common.util.UtilPlayer.message(player, com.houzicore.shared.common.util.F.main("Server", "§cFailed to request server. Server might be at capacity. Code: " + code));
				}
			} catch (Exception e) {
				com.houzicore.shared.common.util.UtilPlayer.message(player, com.houzicore.shared.common.util.F.main("Server", "§cAn error occurred while requesting the server."));
				e.printStackTrace();
			}
		});
	}
}
