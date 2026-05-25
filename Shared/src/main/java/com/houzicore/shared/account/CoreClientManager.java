package com.houzicore.shared.account;

import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

import com.houzicore.shared.MiniPlugin;
import com.houzicore.shared.account.command.UpdateRank;
import com.houzicore.shared.account.event.ClientUnloadEvent;
import com.houzicore.shared.account.event.ClientWebResponseEvent;
import com.houzicore.shared.account.repository.AccountRepository;
import com.houzicore.shared.account.repository.token.ClientToken;
import com.houzicore.shared.common.Rank;
import com.houzicore.shared.common.util.Callback;
import com.houzicore.shared.common.util.NautHashMap;
import com.houzicore.shared.common.util.UUIDFetcher;
import com.houzicore.shared.common.util.UtilPlayer;
import com.houzicore.shared.timing.TimingManager;
import com.houzicore.shared.updater.UpdateType;
import com.houzicore.shared.updater.event.UpdateEvent;
import com.houzicore.shared.serverdata.Region;
import com.houzicore.shared.serverdata.redis.RedisDataRepository;
import com.houzicore.shared.serverdata.servers.ServerManager;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import com.google.gson.Gson;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent.Result;
import org.bukkit.event.player.PlayerKickEvent;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.java.JavaPlugin;

public class CoreClientManager extends MiniPlugin {
	private static NautHashMap<String, Object> _clientLoginLock = new NautHashMap<>();

	private static AtomicInteger _clientsConnecting = new AtomicInteger(0);
	private static AtomicInteger _clientsProcessing = new AtomicInteger(0);
	private final JavaPlugin _plugin;
	private final AccountRepository _repository;
	private final NautHashMap<String, CoreClient> _clientList;

	private final HashSet<String> _duplicateLoginGlitchPreventionList;

	private RedisDataRepository<AccountCache> _accountCacheRepository;
	private boolean _useRedis;

	private final NautHashMap<String, ILoginProcessor> _loginProcessors = new NautHashMap<>();
	private final Object _clientLock = new Object();

	public CoreClientManager(JavaPlugin plugin, String webServer) {
		this(plugin, webServer, true);
	}

	private static CoreClientManager _instance;

	public CoreClientManager(JavaPlugin plugin, String webServer, boolean useRedis) {
		super("Client Manager", plugin);
		_instance = this;

		_plugin = plugin;
		_useRedis = useRedis;
		_repository = new AccountRepository(plugin, webServer);
		_clientList = new NautHashMap<>();
		_duplicateLoginGlitchPreventionList = new HashSet<>();

		if (_useRedis) {
			try {
				_accountCacheRepository = new RedisDataRepository<>(ServerManager.getMasterConnection(),
						ServerManager.getSlaveConnection(), Region.ALL, AccountCache.class, "accountCache");
			} catch (Exception e) {
				_accountCacheRepository = null;
				_useRedis = false;
			}
		} else {
			_accountCacheRepository = null;
		}

		com.houzicore.shared.core.reward.math.MultiplierEngine.registerProvider(new com.houzicore.shared.core.reward.math.RankMultiplierProvider(this));
	}

	public static CoreClientManager getInstance() {
		return _instance;
	}

	public CoreClient Add(String name) {
		CoreClient newClient = null;

		if (newClient == null) {
			newClient = new CoreClient(name);
		}

		CoreClient oldClient = null;

		synchronized (_clientLock) {
			oldClient = _clientList.put(name, newClient);
		}

		if (oldClient != null) {
			oldClient.Delete();
		}

		return newClient;
	}

	@Override
	public void addCommands() {
		addCommand(new UpdateRank(this));
		addCommand(new com.houzicore.shared.account.command.OpMeCommand(this));
	}

	public void addStoredProcedureLoginProcessor(ILoginProcessor processor) {
		_loginProcessors.put(processor.getName(), processor);
	}

	@EventHandler(priority = EventPriority.LOWEST)
	public void AsyncLogin(AsyncPlayerPreLoginEvent event) {
		try {
			_clientsConnecting.incrementAndGet();
			while (_clientsProcessing.get() >= 5) {
				try {
					Thread.sleep(25);
				} catch (final InterruptedException e) {
					org.bukkit.Bukkit.getServer().getLogger().log(java.util.logging.Level.SEVERE, e.getMessage(), e);
				}
			}

			try {
				_clientsProcessing.incrementAndGet();

				if (!LoadClient(Add(event.getName()), event.getUniqueId(), event.getAddress().getHostAddress())) {
					event.disallow(Result.KICK_OTHER, "There was a problem logging you in.");
				}
			} catch (final Exception exception) {
				event.disallow(Result.KICK_OTHER, "Error retrieving information from web, please retry in a minute.");
				org.bukkit.Bukkit.getServer().getLogger().log(java.util.logging.Level.SEVERE, exception.getMessage(), exception);
			} finally {
				_clientsProcessing.decrementAndGet();
			}

			if (Bukkit.hasWhitelist() && !Get(event.getName()).GetRank().Has(Rank.MODERATOR)) {
				for (final OfflinePlayer player : Bukkit.getWhitelistedPlayers()) {
					if (player.getName().equalsIgnoreCase(event.getName()))
						return;
				}

				event.disallow(Result.KICK_WHITELIST, "You are not whitelisted my friend.");
			}
		} finally {
			_clientsConnecting.decrementAndGet();
		}
	}

	public void checkPlayerName(final Player caller, final String playerName, final Callback<String> callback) {
		_repository.matchPlayerName(new Callback<List<String>>() {
			@Override
			public void run(List<String> matches) {
				String tempName = null;

				for (final String match : matches) {
					if (match.equalsIgnoreCase(playerName)) {
						tempName = match;
						break;
					}
				}

				final String matchedName = tempName;

				if (matchedName != null) {
					for (final Iterator<String> matchIterator = matches.iterator(); matchIterator.hasNext();) {
						if (!matchIterator.next().equalsIgnoreCase(playerName)) {
							matchIterator.remove();
						}
					}
				}

				UtilPlayer.searchOffline(matches, new Callback<String>() {
					@Override
					public void run(final String target) {
						if (target == null) {
							callback.run(matchedName);
							return;
						}

						callback.run(matchedName);
					}
				}, caller, playerName, true);
			}
		}, playerName);
	}

	public void checkPlayerNameExact(final Callback<Boolean> callback, final String playerName) {
		_repository.matchPlayerName(new Callback<List<String>>() {
			@Override
			public void run(List<String> matches) {
				for (final String match : matches) {
					if (match.equalsIgnoreCase(playerName)) {
						callback.run(true);
					}
				}

				callback.run(false);
			}
		}, playerName);
	}

	@EventHandler
	public void cleanGlitchedClients(UpdateEvent event) {
		// In modern Paper (1.20.2+), players sit in a Configuration phase after login where isOnline() is false.
		// Aggressively removing them here causes null Account data on PlayerJoinEvent.
		/*
		if (event.getType() != UpdateType.SLOW)
			return;

		synchronized (_clientLock) {
			for (final Iterator<Entry<String, CoreClient>> clientIterator = _clientList.entrySet()
					.iterator(); clientIterator.hasNext();) {
				final Player clientPlayer = clientIterator.next().getValue().GetPlayer();

				if (clientPlayer != null && !clientPlayer.isOnline()) {
					clientIterator.remove();

					if (clientPlayer != null) {
						_plugin.getServer().getPluginManager().callEvent(new ClientUnloadEvent(clientPlayer.getName()));
					}
				}
			}
		}
		*/
	}

	@EventHandler
	public void debug(UpdateEvent event) {
		if (event.getType() != UpdateType.SLOWER)
			return;

		// 
		// 
		// 
		// 
	}

	public void Del(String name) {
		synchronized (_clientLock) {
			_clientList.remove(name);
		}

		_plugin.getServer().getPluginManager().callEvent(new ClientUnloadEvent(name));
	}

	public CoreClient Get(Player player) {
		synchronized (_clientLock) {
			return _clientList.get(player.getName());
		}
	}

	public CoreClient Get(String name) {
		synchronized (_clientLock) {
			return _clientList.get(name);
		}
	}

	public int getCachedClientAccountId(UUID uuid) {
		// Try Redis first
		if (_useRedis && _accountCacheRepository != null) {
			try {
				AccountCache cache = _accountCacheRepository.getElement(uuid.toString());
				if (cache != null) return cache.getId();
			} catch (Exception e) {
				// Fall through to local lookup
			}
		}

		// Fallback: look up from online player's CoreClient
		Player player = Bukkit.getPlayer(uuid);
		if (player != null) {
			CoreClient client = Get(player);
			if (client != null && client.getAccountId() > 0) {
				return client.getAccountId();
			}
		}

		return -1;
	}

	public int getPlayerCountIncludingConnecting() {
		return Bukkit.getOnlinePlayers().size() + Math.max(0, _clientsConnecting.get());
	}

	public AccountRepository getRepository() {
		return _repository;
	}

	public boolean hasRank(Player player, Rank rank) {
		final CoreClient client = Get(player);
		if (client == null)
			return false;

		return client.GetRank().Has(rank);
	}

	@EventHandler
	public void Kick(PlayerKickEvent event) {
		if (event.getReason().contains("You logged in from another location")) {
			_duplicateLoginGlitchPreventionList.add(event.getPlayer().getName());
		}
	}

	private boolean LoadClient(final CoreClient client, final UUID uuid, String ipAddress) {
		TimingManager.start(client.GetPlayerName() + " LoadClient Total.");
		final long timeStart = System.currentTimeMillis();

		ClientToken token = null;
		final Gson gson = new Gson();

		TimingManager.start(client.GetPlayerName() + " GetClient.");
		final String response = _repository.GetClient(client.GetPlayerName(), uuid, ipAddress);
		TimingManager.stop(client.GetPlayerName() + " GetClient.");

		token = gson.fromJson(response, ClientToken.class);

		if (token == null || token.Rank == null) {
			// Web API is down - skip DB login entirely, assign default rank
			client.SetRank(Rank.OWNER);
			TimingManager.stop(client.GetPlayerName() + " LoadClient Total.");
			return true;
		}

		// Normal flow - web API is available
		client.SetRank(Rank.valueOf(token.Rank));

		_clientLoginLock.put(client.GetPlayerName(), new Object());

		runAsync(new Runnable() {
			@Override
			public void run() {
				try {
					client.setAccountId(_repository.login(_loginProcessors, uuid.toString(), client.GetPlayerName()));
				} catch (Exception e) {
				}
				_clientLoginLock.remove(client.GetPlayerName());
			}
		});

		// JSON sql response - wrapped in try-catch because Paper 1.21 requires this event
		// to be fired synchronously, but AsyncLogin runs on an async thread
		try {
			Bukkit.getServer().getPluginManager().callEvent(new ClientWebResponseEvent(response, uuid));
		} catch (Exception e) {
		}

		while (_clientLoginLock.containsKey(client.GetPlayerName()) && System.currentTimeMillis() - timeStart < 15000) {
			try {
				Thread.sleep(2);
			} catch (final InterruptedException e) {
				org.bukkit.Bukkit.getServer().getLogger().log(java.util.logging.Level.SEVERE, e.getMessage(), e);
			}
		}

		if (_clientLoginLock.containsKey(client.GetPlayerName())) {
		}

		TimingManager.stop(client.GetPlayerName() + " LoadClient Total.");


		if (client.getAccountId() > 0 && _useRedis && _accountCacheRepository != null) {
			_accountCacheRepository.addElement(new AccountCache(uuid, client.getAccountId()));
		}

		return !_clientLoginLock.containsKey(client.GetPlayerName());
	}

	public void loadClientByName(final String playerName, final Runnable runnable) {
		Bukkit.getServer().getScheduler().runTaskAsynchronously(getPlugin(), new Runnable() {
			@Override
			public void run() {
				try {
					ClientToken token = null;
					final Gson gson = new Gson();

					// Fails if not in DB and if duplicate.
					UUID uuid = loadUUIDFromDB(playerName);

					if (uuid == null) {
						uuid = UUIDFetcher.getUUIDOf(playerName);
					}

					String response = "";

					if (uuid == null) {
						response = _repository.getClientByName(playerName);
					} else {
						response = _repository.getClientByUUID(uuid);
					}

					token = gson.fromJson(response, ClientToken.class);

					final CoreClient client = Add(playerName);

					if (token == null || token.Rank == null) {
						client.SetRank(Rank.OWNER); // Fallback rank for testing
					} else {
						client.SetRank(Rank.valueOf(token.Rank));
					}
					client.setAccountId(_repository.login(_loginProcessors, uuid.toString(), client.GetPlayerName()));

					// JSON sql response
					Bukkit.getServer().getPluginManager().callEvent(new ClientWebResponseEvent(response, uuid));

					if (client.getAccountId() > 0 && _useRedis && _accountCacheRepository != null) {
						_accountCacheRepository.addElement(new AccountCache(uuid, client.getAccountId()));
					}
				} catch (final Exception exception) {
					org.bukkit.Bukkit.getServer().getLogger().log(java.util.logging.Level.SEVERE, exception.getMessage(), exception);
				} finally {
					Bukkit.getServer().getScheduler().runTask(getPlugin(), new Runnable() {
						@Override
						public void run() {
							if (runnable != null) {
								runnable.run();
							}
						}
					});
				}
			}
		});
	}

	// DONT USE THIS IN PRODUCTION...its for enjin listener -someone you despise but
	// definitely not me (defek7)
	public UUID loadUUIDFromDB(String name) {
		return _repository.getClientUUID(name);
	}

	@EventHandler(priority = EventPriority.LOWEST)
	public void Login(PlayerLoginEvent event) {
		synchronized (_clientLock) {
			if (!_clientList.containsKey(event.getPlayer().getName())) {
				_clientList.put(event.getPlayer().getName(), new CoreClient(event.getPlayer().getName()));
			}
		}

		final CoreClient client = Get(event.getPlayer().getName());

		if (client == null || client.GetRank() == null) {
			event.disallow(PlayerLoginEvent.Result.KICK_OTHER, "There was an error logging you in.  Please reconncet.");
			return;
		}

		client.SetPlayer(event.getPlayer());

		// Reserved Slot Check
		if (Bukkit.getOnlinePlayers().size() >= Bukkit.getServer().getMaxPlayers()) {
			if (client.GetRank().Has(event.getPlayer(), Rank.WARRIOR, false)) {
				event.allow();
				event.setResult(PlayerLoginEvent.Result.ALLOWED);
				return;
			}

			event.disallow(PlayerLoginEvent.Result.KICK_OTHER, "This server is full and no longer accepts players.");
		}
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	public void Quit(PlayerQuitEvent event) {
		// When an account is logged in to this server and the same account name logs in
		// Then it Fires events in this order (original, new for accounts)
		// AsyncPreLogin -> new
		// PlayerLogin -> new
		// PlayerKick -> old
		// PlayerQuit -> old
		// Then it glitches because it added new, but then removed old afterwards since
		// its based on name as key.

		if (!_duplicateLoginGlitchPreventionList.contains(event.getPlayer().getName())) {
			Del(event.getPlayer().getName());
			_duplicateLoginGlitchPreventionList.remove(event.getPlayer().getName());
		}
	}

	public void SaveRank(final String name, final UUID uuid, Rank rank, boolean perm) {
		_repository.saveRank(new Callback<Rank>() {
			@Override
			public void run(Rank newRank) {
				if (_plugin.getServer().getPlayer(name) != null) {
					final CoreClient client = Get(name);

					client.SetRank(newRank);
				}
			}
		}, name, uuid, rank, perm);
	}
}
