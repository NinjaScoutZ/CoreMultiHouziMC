package com.houzicore.arcade.nautilus.game.arcade.managers;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

import com.houzicore.shared.common.Rank;
import com.houzicore.shared.common.actionbar.ActionBarChannel;
import com.houzicore.shared.common.util.C;
import com.houzicore.shared.common.util.F;
import com.houzicore.shared.common.util.UtilGear;
import com.houzicore.shared.common.util.UtilPlayer;
import com.houzicore.shared.common.util.UtilServer;
import com.houzicore.shared.common.util.UtilTextBottom;
import com.houzicore.shared.common.util.UtilTime;
import com.houzicore.shared.core.game.GameCategory;
import com.houzicore.shared.core.itemstack.ItemStackFactory;
import com.houzicore.shared.updater.UpdateType;
import com.houzicore.shared.updater.event.UpdateEvent;
import com.houzicore.arcade.ArcadeManager;
import com.houzicore.arcade.GameType;
import com.houzicore.arcade.nautilus.game.arcade.game.Game.GameState;
import com.houzicore.shared.core.lang.LangManager;
import com.houzicore.arcade.nautilus.game.arcade.gui.privateServer.PrivateServerShop;
import com.houzicore.arcade.nautilus.game.arcade.gui.privateServer.page.GameVotingPage;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.Plugin;

public class GameHostManager implements Listener
{
	private ArrayList<GameType> ultraGames = new ArrayList<GameType>();
	private ArrayList<GameType> heroGames = new ArrayList<GameType>();
	private ArrayList<GameType> legendGames = new ArrayList<GameType>();

	ArcadeManager Manager;
	
	private Player _host;
	private Rank _hostRank;
	private long _serverStartTime = System.currentTimeMillis();
	private long _serverExpireTime = 21600000;
	private long _lastOnline = System.currentTimeMillis();
	private long _expireTime = 300000;
	private boolean _hostExpired = false;

	private HashSet<Player> _onlineAdmins = new HashSet<Player>();
	private HashSet<String> _adminList = new HashSet<String>();
	private HashSet<String> _whitelist = new HashSet<String>();
	private HashSet<String> _blacklist = new HashSet<String>();

	private PrivateServerShop _shop;
	
	private boolean _isEventServer = false;
	
	private HashMap<Player, Boolean> _permissionMap = new HashMap<Player, Boolean>();

	private boolean _voteInProgress = false;
	private HashMap<String, GameType> _votes = new HashMap<String, GameType>();
	private int _voteNotificationStage = 1;
	
	public GameHostManager(ArcadeManager manager)
	{
		Manager = manager;
		_shop = new PrivateServerShop(manager, manager.GetClients(), manager.GetDonation());
		Manager.getPluginManager().registerEvents(this, Manager.getPlugin());
		
		//Ultra Games
		ultraGames.add(GameType.SneakyAssassins);
		ultraGames.add(GameType.Lobbers);

		//Hero Games
		heroGames.add(GameType.PropRush);

		//Legend Games
		legendGames.add(GameType.SurvivalPrimalGame);
		legendGames.add(GameType.CastleSiege);
		legendGames.add(GameType.WitherAssault);
		legendGames.add(GameType.Wizards);
		legendGames.add(GameType.MineStrike);
		legendGames.add(GameType.Skywars);
		// Team variants - Currently being remade.
		/*
		legendGames.add(GameType.DragonEscapeTeams);
		legendGames.add(GameType.DragonsTeams);
		legendGames.add(GameType.QuiverTeams);
		legendGames.add(GameType.SmashTeams);
		legendGames.add(GameType.SpleefTeams);
		legendGames.add(GameType.SurvivalPrimalGameTeams);
		*/
		//Rejected / Other
		legendGames.add(GameType.Evolution);
		legendGames.add(GameType.SearchAndDestroy);
		legendGames.add(GameType.SurvivalPrimalGameTeams);
		legendGames.add(GameType.SkywarsTeams);
		legendGames.add(GameType.SnowFight);
		legendGames.add(GameType.Barbarians);

		//Config Defaults
		if (Manager.GetHost() != null && Manager.GetHost().length() > 0)
		{
			setDefaultConfig();
		}
	}

	public ArrayList<GameType> hasWarning()
	{
		ArrayList<GameType> games = new ArrayList<>();
		games.add(GameType.Evolution);
		games.add(GameType.SearchAndDestroy);
		return games;
	}

	@EventHandler
	public void updateHost(UpdateEvent event)
	{
		if (event.getType() != UpdateType.FAST)
			return;
		
		//No Host - Not MPS
		if (Manager.GetHost() == null || Manager.GetHost().length() == 0)
			return;
		
		// Set as event server
		if (Manager.GetGame() != null && Manager.GetGame().GetType() == GameType.Event)
		{
			setEventServer(true);
		}
		
		// Admins update
		for (Player player : UtilServer.getPlayers())
		{
			if (player.equals(_host) || _adminList.contains(player.getName()) || Manager.GetClients().Get(player).GetRank().Has(Rank.ADMIN))
			{
				if (Manager.GetGame() == null || Manager.GetGame().GetState() == GameState.Recruit)
					giveAdminItem(player);
			}
			
			if (player.equals(_host) ||	(isAdmin(player, false) && isEventServer()))
				_lastOnline = System.currentTimeMillis();
		}
	}

	@EventHandler
	public void voteNotification(UpdateEvent e)
	{
		if (e.getType() != UpdateType.FAST)
			return;

		if (!_voteInProgress)
			return;

		if (_voteNotificationStage == 1)
		{
			UtilTextBottom.display(ActionBarChannel.GAME_EVENT, C.cYellow + C.Bold + "Type " + C.cGold +  C.Bold + "/vote" + C.cYellow + C.Bold + " to vote for next game", UtilServer.getPlayers());
			_voteNotificationStage++;
			return;
		}
		else if (_voteNotificationStage == 2)
		{
			UtilTextBottom.display(ActionBarChannel.GAME_EVENT, C.cGold + C.Bold + "Type " + C.cYellow +  C.Bold + "/vote" + C.cGold + C.Bold + " to vote for next game", UtilServer.getPlayers());
			_voteNotificationStage = 1;
			return;
		}
	}

	@EventHandler
	public void whitelistJoin(PlayerLoginEvent event)
	{
		Player p = event.getPlayer();
		if (Manager.GetServerConfig().PlayerServerWhitelist){
			if (!getWhitelist().contains(p.getName())){
				if (_host == p)
					return;
				Manager.GetPortal().sendToHub(p, "You aren't on the whitelist of this Private Server.");
			}
		}
	}

	@EventHandler
	public void adminJoin(PlayerJoinEvent event)
	{
		if (!isPrivateServer())
			return;

		if (Manager.GetHost().equals(event.getPlayer().getName()))
		{
			_host = event.getPlayer();
			_hostRank = Manager.GetClients().Get(_host).GetRank();
			
			if (isEventServer())
				worldeditPermissionSet(event.getPlayer(), true);
		}
		else if (isAdmin(event.getPlayer(), false))
		{
			_onlineAdmins.add(event.getPlayer());
			
			if (isEventServer())
				worldeditPermissionSet(event.getPlayer(), true);
		}
	}
	
	@EventHandler
	public void adminQuit(PlayerQuitEvent event)
	{
		if (!isPrivateServer())
			return;
		
		if (isHost(event.getPlayer()))
		{
			_host = null;
			
			if (isEventServer())
				worldeditPermissionSet(event.getPlayer(), false);
		}
		else if (isAdmin(event.getPlayer(), false))
		{
			_onlineAdmins.remove(event.getPlayer());
			
			if (isEventServer())
				worldeditPermissionSet(event.getPlayer(), false);
		}
	}
		
	public void worldeditPermissionSet(Player player, boolean hasPermission)
	{
		if (!_permissionMap.containsKey(player) || _permissionMap.get(player) != hasPermission)
		{
			for (Plugin plugin : Bukkit.getPluginManager().getPlugins())
			{
				player.addAttachment(plugin, "worldedit.*", hasPermission);
			}
			
			_permissionMap.put(player, hasPermission);
			
			UtilPlayer.message(player, "World Edit Permissions: " + F.tf(hasPermission));
		}
	}
	
	
	@EventHandler
	public void updateHostExpired(UpdateEvent event)
	{
		if (!isPrivateServer())
			return;
		
		if (event.getType() != UpdateType.FAST)
			return;
	
		if (Manager.GetGame() != null && Manager.GetGame().GetState() != GameState.Recruit)
			return;
		
		if (_hostExpired)
			return;
		
		if (UtilTime.elapsed(_lastOnline, _expireTime))
			setHostExpired(true, Manager.GetServerConfig().HostName + " has abandoned the server. Thanks for playing!");
		
		else if (UtilTime.elapsed(_serverStartTime, _serverExpireTime))
			setHostExpired(true, "This server has expired! Thank you for playing!");
	}
	
	
	public boolean isHostExpired()
	{
		if (!isPrivateServer())
			return false;
		
		return _hostExpired;
	}

	public void setHostExpired(boolean expired, String string)
	{
		for (Player other : UtilServer.getPlayers())
		{
			UtilPlayer.message(other, C.cGold + C.Bold + string);
			other.playSound(other.getLocation(), Sound.ENTITY_ENDER_DRAGON_GROWL, 10f, 1f);
			Manager.GetPortal().sendPlayerToServer(other, "Lobby");
		}
		
		_hostExpired = expired;
	}
	
	private void giveAdminItem(Player player)
	{
		if (Manager.GetGame() == null)
			return;

		if (UtilGear.isMat(player.getInventory().getItem(8), Material.GLISTERING_MELON_SLICE))
			return;
		
		if (player.getOpenInventory().getType() != InventoryType.CRAFTING &&
			player.getOpenInventory().getType() != InventoryType.CREATIVE)
			return;
		
		player.getInventory().setItem(8, ItemStackFactory.Instance.CreateStack(Material.GLISTERING_MELON_SLICE, (byte)0, 1, C.cGreen + C.Bold + "/menu"));
	}

	private void removeAdminItem(Player player)
	{
		if (player.getInventory().getItem(8) != null && player.getInventory().getItem(8).getType() == Material.GLISTERING_MELON_SLICE)
		{
			player.getInventory().setItem(8, null);
		}
	}
	
	public HashSet<String> getWhitelist()
	{
		return _whitelist;
	}
	
	public HashSet<String> getBlacklist()
	{
		return _blacklist;
	}

	public HashSet<String> getAdminList()
	{
		return _adminList;
	}

	@EventHandler
	public void broadcastCommand(PlayerCommandPreprocessEvent event)
	{
		if (!event.getMessage().toLowerCase().startsWith("/bc"))
			return;

		if (!isPrivateServer())
			return;

		if (!isAdmin(event.getPlayer(), true))
		{
			event.getPlayer().sendMessage(F.main("Broadcast", "Only MPS admins can use this command."));
			event.setCancelled(true);
			return;
		}

		event.setCancelled(true);

		if (event.getMessage().split(" ").length < 2)
		{
			event.getPlayer().sendMessage(F.main("Broadcast", "/bc <message>"));
			return;
		}

		String msg = "";
		for (int i = 1; i < event.getMessage().split(" ").length; i++)
		{
			msg += event.getMessage().split(" ")[i] + " ";
		}
		msg = msg.trim();

		com.houzicore.shared.common.util.UtilServer.broadcast("§6§l" + event.getPlayer().getName() + " §e" + msg);
	}

	@EventHandler
	public void voteCommand(PlayerCommandPreprocessEvent event)
	{
		if (!event.getMessage().toLowerCase().startsWith("/vote"))
			return;

		if (!isPrivateServer())
		{
			UtilPlayer.message(event.getPlayer(), F.main("Vote", "This command is only available on private servers."));
			event.setCancelled(true);
			return;
		}

		if (!_voteInProgress)
		{
			UtilPlayer.message(event.getPlayer(), F.main("Vote", "There is no vote in progress."));
			event.setCancelled(true);
			return;
		}

		event.setCancelled(true);
		_shop.openPageForPlayer(event.getPlayer(), new GameVotingPage(Manager, _shop, event.getPlayer()));
		return;
	}
	
	@EventHandler
	public void menuCommand(PlayerCommandPreprocessEvent event)
	{
		if (!event.getMessage().toLowerCase().startsWith("/menu"))
			return;

		if (!isPrivateServer())
			return;
		
		if (!isAdmin(event.getPlayer(), true))
			return;
		
		event.setCancelled(true);
		openMenu(event.getPlayer());
	}
	
	@EventHandler
	public void menuInteract(PlayerInteractEvent event)
	{
		if (!isPrivateServer())
			return;
		
		if (!isAdmin(event.getPlayer(), true))
			return;
		
		if (!UtilGear.isMat(event.getPlayer().getItemInHand(), Material.GLISTERING_MELON_SLICE))
			return;
		
		openMenu(event.getPlayer());
		event.setCancelled(true);
	}
	
	private void openMenu(Player player)
	{
		_shop.attemptShopOpen(player);
	}

	public boolean isAdmin(Player player, boolean includeStaff)
	{
		return player.equals(_host) || _adminList.contains(player.getName()) || (includeStaff && Manager.GetClients().Get(player).GetRank().Has(Rank.ADMIN));
	}

	public boolean isHost(Player player)
	{
		return player.getName().equals(Manager.GetHost());
	}

	public boolean isPrivateServer()
	{
		return Manager.GetHost() != null && Manager.GetHost().length() > 0;
	}

	@EventHandler
	public void whitelistCommand(PlayerCommandPreprocessEvent event)
	{
		if (_host == null || !event.getPlayer().equals(_host))
			return;
		
		if (!event.getMessage().toLowerCase().startsWith("/whitelist"))
			return;

		if (!isPrivateServer())
			return;

		event.setCancelled(true);
		
		String[] args = event.getMessage().split(" ");
		
		for (int i=1 ; i<args.length ; i++)
		{
			String name = args[i].toLowerCase();
			
			if (_whitelist.add(name))
			{
				UtilPlayer.message(event.getPlayer(), F.main("Host", "Added " + F.elem(args[i]) + " to the whitelist."));
			}
		}
	}
	
	public void setGame(GameType type)
	{
		if (_host == null)
			return;
		
		Manager.GetGameCreationManager().SetNextGameType(type);
		
		//End Current
		if (Manager.GetGame().GetState() == GameState.Recruit)
		{
			Manager.GetGame().SetState(GameState.Dead);
			
			com.houzicore.shared.common.util.UtilServer.broadcast(C.cGreen + _host.getName() + (LangManager.get().isThai(_host) ? " \u00A77\u0e40\u0e1b\u0e25\u0e35\u0e48\u0e22\u0e19\u0e40\u0e01\u0e21\u0e40\u0e1b็น " + C.cYellow + type.GetName() + "." : " \u00A77changed the game to " + C.cYellow + type.GetName() + "."));
		}
		else
		{
			com.houzicore.shared.common.util.UtilServer.broadcast(C.cGreen + C.Bold + _host.getName() + " set next game to " + type.GetName() + ".");
		}
	}
	
	public void startGame()
	{
		if (_host == null)
			return;
		
		Manager.GetGameManager().StateCountdown(Manager.GetGame(), 10, true);

		Manager.GetGame().Announce(C.cGreen + _host.getName() + (LangManager.get().isThai(_host) ? " \u00A77\u0e40\u0e23\u0e34\u0e48\u0e21\u0e40\u0e01\u0e21\u0e41\u0e25\u0e49\u0e27" : " \u00A77started the game."));
	}
	
	public void stopGame()
	{
		if (_host == null)
			return;
		
		if (Manager.GetGame() == null)
			return; 

		HandlerList.unregisterAll(Manager.GetGame());
		
		if (Manager.GetGame().GetState() == GameState.End || Manager.GetGame().GetState() == GameState.End)
		{
			_host.sendMessage("Game is already ending..."); 
			return;
		}
		else if (Manager.GetGame().GetState() == GameState.Recruit)
		{
			Manager.GetGame().SetState(GameState.Dead);
		}
		else
		{
			Manager.GetGame().SetState(GameState.End);
		}


		Manager.GetGame().Announce(C.cGreen + C.Bold + _host.getName() + " has stopped the game.");
	}

	public boolean hasRank(Rank rank)
	{
		if (_hostRank == null)
			return false;
		
		return _hostRank.Has(rank);
	}

	public ArrayList<GameType> getAvailableGames(Player player)
	{
		ArrayList<GameType> games = new ArrayList<GameType>();

		if (hasRank(Rank.WARRIOR))
			games.addAll(ultraGames);
		if (hasRank(Rank.SOVEREIGN))
			games.addAll(heroGames);
		if (hasRank(Rank.DIVINE))
			games.addAll(legendGames);

		return games;
	}

	public HashMap<GameCategory, ArrayList<GameType>> getGames(Player p)
	{
		HashMap<GameCategory, ArrayList<GameType>> games = new HashMap<GameCategory, ArrayList<GameType>>();
		for (GameCategory cat : GameCategory.values())
		{
			ArrayList<GameType> types = new ArrayList<>();
			for (GameType type : getAvailableGames(p))
			{
				if (type.getGameCategory().equals(cat))
				{
					types.add(type);
				}
			}
			games.put(cat, types);
		}
		return games;
	}

	public void ban(Player player)
	{
		_blacklist.add(player.getName());

		Manager.GetPortal().sendToHub(player, "You were removed from this Private Server.");
	}
	
	@EventHandler
	public void kickBlacklist(UpdateEvent event)
	{
		if (event.getType() != UpdateType.SEC)
			return;
		
		for (Player player : UtilServer.getPlayers())
		{
			if (_blacklist.contains(player.getName()))
			{
				Manager.GetPortal().sendToHub(player, "You were removed from this Private Server.");
			}
		}
	}

	public void giveAdmin(Player player)
	{
		_adminList.add(player.getName());
		_onlineAdmins.add(player);
		UtilPlayer.message(player, F.main("Server", "You were given admin privileges."));
		
		if (isEventServer())
			worldeditPermissionSet(player, true);
	}

	public void removeAdmin(String playerName)
	{
		_adminList.remove(playerName);
		Player player = UtilPlayer.searchExact(playerName);
		if (player != null)
		{
			_onlineAdmins.remove(player);
			removeAdminItem(player);
			if (_shop.isPlayerInShop(player))
			{
				player.closeInventory();
			}
			UtilPlayer.message(player, F.main("Server", "Your admin privileges were removed."));
			
			player.setGameMode(GameMode.SURVIVAL);
			
			if (isEventServer())
				worldeditPermissionSet(player, false);
		}
	}
	
	public boolean isAdminOnline()
	{
		return _onlineAdmins.isEmpty();
	}
	
	public void setDefaultConfig()
	{
		Manager.GetServerConfig().HotbarInventory = false;
		
		Manager.GetServerConfig().RewardAchievements = false;
		Manager.GetServerConfig().RewardGems = false;
		Manager.GetServerConfig().RewardItems = false;
		Manager.GetServerConfig().RewardStats = false;
		
		Manager.GetServerConfig().GameAutoStart = true;
		Manager.GetServerConfig().GameTimeout = true;
		Manager.GetServerConfig().PlayerKickIdle = true;
		Manager.GetServerConfig().TeamForceBalance = true;
	}

	public int getMaxPlayerCap()
	{
		if (hasRank(Rank.SNR_MODERATOR) || _hostRank == Rank.YOUTUBE || _hostRank == Rank.TWITCH)
			return 100;
		else if (hasRank(Rank.DIVINE))
			return 40;
		else if (hasRank(Rank.SOVEREIGN))
			return 12;
		else
			return 4;
	}
		
	@EventHandler
	public void setEventGame(PlayerCommandPreprocessEvent event)
	{
		if (!isEventServer() || Manager.GetGame() == null)
			return;
		
		if (!isAdmin(event.getPlayer(), false))
			return;
		
		if (!event.getMessage().toLowerCase().startsWith("/e set ") && !event.getMessage().toLowerCase().equals("/e set"))
			return;
		
		String[] args = event.getMessage().split(" ");
		
		//Parse Game
		if (args.length >= 3)
		{
			ArrayList<GameType> matches = new ArrayList<GameType>();
			for (GameType type : GameType.values())
			{
				if (type.toString().toLowerCase().equals(args[2]))
				{
					matches.clear();
					matches.add(type);
					break;
				}
				
				if (type.toString().toLowerCase().contains(args[2]))
				{
					matches.add(type);
				}
			}
			
			if (matches.size() == 0)
			{
				event.getPlayer().sendMessage("No results for: " + args[2]);
				return;
			}
			
			if (matches.size() > 1)
			{
				event.getPlayer().sendMessage("Matched multiple games;");
				for (GameType cur : matches)
					event.getPlayer().sendMessage(cur.toString());
				return;
			}
			
			GameType type = matches.get(0);
			Manager.GetGame().setGame(type, event.getPlayer(), true);
		}
		else
		{
			Manager.GetGame().setGame(GameType.Event, event.getPlayer(), true);
		}
		
	
		//Map Pref
		if (args.length >= 4)
		{
			Manager.GetGameCreationManager().MapPref = args[3];
			UtilPlayer.message(event.getPlayer(), C.cAqua + C.Bold + "Map Preference: " + ChatColor.RESET + args[2]);
		}
		
		event.setCancelled(true);
	}

	@EventHandler
	public void playerJoin(PlayerJoinEvent event)
	{
		if (!isPrivateServer())
			return;

		if (_blacklist.contains(event.getPlayer().getName()))
		{
			Manager.GetPortal().sendToHub(event.getPlayer(), "You were kicked from this Private Server.");
			return;
		}
		
		String serverName = Manager.getPlugin().getConfig().getString("serverstatus.name");
		UtilPlayer.message(event.getPlayer(), ChatColor.BOLD + "Welcome to Private Servers!");
		UtilPlayer.message(event.getPlayer(), C.Bold + "Friends can connect with " + C.cGreen + C.Bold + "/server " + serverName);
	}
	
	public boolean isEventServer()
	{
		return _isEventServer;
	}
	
	public void setEventServer(boolean var)
	{
		_isEventServer = var;
	}

	public HashMap<String, GameType> getVotes()
	{
		return _votes;
	}

	public void setVoteInProgress(boolean voteInProgress)
	{
		_voteInProgress = voteInProgress;
	}

	public boolean isVoteInProgress()
	{
		return _voteInProgress;
	}
	
	public Rank getHostRank()
	{
		return _hostRank;
	}
	
	public void setHostRank(Rank rank)
	{
		_hostRank = rank;
	}
	
	public Player getHost()
	{
		return _host;
	}
	
	public void setHost(Player player)
	{
		_host = player;
	}
	
}
