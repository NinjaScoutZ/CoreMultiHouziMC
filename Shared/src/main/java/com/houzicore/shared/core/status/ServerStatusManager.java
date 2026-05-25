package com.houzicore.shared.core.status;

import java.io.File;
import java.util.Collection;

import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.server.ServerListPingEvent;
import org.bukkit.plugin.java.JavaPlugin;

import com.houzicore.shared.MiniPlugin;
import com.houzicore.shared.account.CoreClientManager;
import com.houzicore.shared.common.util.Callback;
import com.houzicore.shared.core.monitor.LagMeter;
import com.houzicore.shared.updater.UpdateType;
import com.houzicore.shared.updater.event.UpdateEvent;
import com.houzicore.shared.serverdata.Region;
import com.houzicore.shared.serverdata.Utility;
import com.houzicore.shared.serverdata.commands.ServerCommandManager;
import com.houzicore.shared.serverdata.commands.SuicideCommand;
import com.houzicore.shared.serverdata.data.MinecraftServer;
import com.houzicore.shared.serverdata.data.ServerGroup;
import com.houzicore.shared.serverdata.servers.ServerManager;
import com.houzicore.shared.serverdata.servers.ServerRepository;

public class ServerStatusManager extends MiniPlugin {
	// The default timeout (in seconds) before the ServerStatus expires.
	public final int DEFAULT_SERVER_TIMEOUT = 30;

	private final ServerRepository _repository;
	private final CoreClientManager _clientManager;
	private final LagMeter _lagMeter;

	private final String _name;
	private final Region _region;

	private boolean _enabled = true;
	private boolean _firstHeartbeatPublished = false;

	private final long _startUpDate;

	public ServerStatusManager(JavaPlugin plugin, CoreClientManager clientManager, LagMeter lagMeter) {
		super("Server Status Manager", plugin);

		_startUpDate = Utility.currentTimeSeconds();
		_clientManager = clientManager;
		_lagMeter = lagMeter;

		if (new File("IgnoreUpdates.dat").exists()) {
			_enabled = false;
		}

		setupConfigValues();

		String sysName = System.getProperty("serverstatus.name");
		_name = (sysName != null && !sysName.isEmpty()) ? sysName : plugin.getConfig().getString("serverstatus.name");

		_region = plugin.getConfig().getBoolean("serverstatus.asia") ? Region.ASIA : Region.TH;

		ServerCommandManager.getInstance().initializeServer(_name);
		ServerCommandManager.getInstance().registerCommandType("SuicideCommand", SuicideCommand.class,
				new SuicideHandler(this, _name, _region));

		_repository = ServerManager.getServerRepository(_region);
		saveServerStatus();
	}

	public void disableStatus() {
		_enabled = false;
		String message = String.format("{\"type\":\"SERVER_STOPPING\", \"server\":\"%s\"}", _name);
		_repository.publishNetworkUpdate(message);
		saveServerStatus();
	}

	/**
	 * @return a newly instanced {@link MinecraftServer} snapshot that represents
	 *         the current internal state of this minecraft server.
	 */
	private MinecraftServer generateServerSnapshot() {
		final ServerListPingEvent event = new ServerListPingEvent("",
				java.net.InetAddress.getLoopbackAddress(), getPlugin().getServer().getMotd(),
				getPlugin().getServer().getOnlinePlayers().size(), getPlugin().getServer().getMaxPlayers());
		
		try {
			getPluginManager().callEvent(event);
		} catch (IllegalStateException e) {
			// Paper 1.21.1+ requires ServerListPingEvent to be async.
			// Ignore the event call if fired synchronously to prevent crashes.
		}

		final String motd = _enabled ? event.getMotd() : "Restarting";
		final int playerCount = _clientManager.getPlayerCountIncludingConnecting();
		final int maxPlayerCount = event.getMaxPlayers();
		final int tps = (int) _lagMeter.getTicksPerSecond();
		final String address = Bukkit.getServer().getIp().isEmpty() ? "127.0.0.1" : Bukkit.getServer().getIp();
		final int port = _plugin.getServer().getPort();
		final String group = _plugin.getConfig().getString("serverstatus.group") + "";
		final int ram = (int) ((Runtime.getRuntime().maxMemory() - Runtime.getRuntime().freeMemory()) / 1048576);
		final int maxRam = (int) (Runtime.getRuntime().maxMemory() / 1048576);

		return new MinecraftServer(_name, group, motd, address, port, playerCount, maxPlayerCount, tps, ram, maxRam,
				_startUpDate);
	}

	public String getCurrentServerName() {
		return _name;
	}

	public Region getRegion() {
		return _region;
	}

	public void retrieveServerGroups(final Callback<Collection<ServerGroup>> callback) {
		if (!_enabled)
			return;

		getPlugin().getServer().getScheduler().runTaskAsynchronously(getPlugin(), new Runnable() {
			@Override
			public void run() {
				if (callback != null) {
					callback.run(_repository.getServerGroups(null));
				}
			}
		});
	}

	public void retrieveServerStatuses(final Callback<Collection<MinecraftServer>> callback) {
		if (!_enabled)
			return;

		getPlugin().getServer().getScheduler().runTaskAsynchronously(getPlugin(), new Runnable() {
			@Override
			public void run() {
				if (callback != null) {
					callback.run(_repository.getServerStatuses());
				}
			}
		});
	}

	/**
	 * Save the current {@link MinecraftServer} snapshot of this server to the
	 * {@link ServerRepository}.
	 */
	private void saveServerStatus() {
		getPlugin().getServer().getScheduler().runTaskAsynchronously(getPlugin(), new Runnable() {
			@Override
			public void run() {
				final MinecraftServer serverSnapshot = generateServerSnapshot();
				final MinecraftServer server = _repository.getServerStatus(serverSnapshot.getName());
				int timeout = DEFAULT_SERVER_TIMEOUT;

				if (!_firstHeartbeatPublished && serverSnapshot.getPublicAddress() != null && !serverSnapshot.getPublicAddress().isEmpty()) {
					_firstHeartbeatPublished = true;
					String message = String.format("{\"type\":\"SERVER_READY\", \"server\":\"%s\", \"ip\":\"%s\", \"port\":%d}", 
						serverSnapshot.getName(), serverSnapshot.getPublicAddress(), serverSnapshot.getPort());
					_repository.publishNetworkUpdate(message);
				}

				if (server != null && !server.getPublicAddress().equalsIgnoreCase(serverSnapshot.getPublicAddress())) {
					timeout = -DEFAULT_SERVER_TIMEOUT;
					/*
					 * ProcessRunner pr = new ProcessRunner(new String[] {"/bin/sh",
					 * "/home/houzicore/config/killServer.sh", serverSnapshot.getName()});
					 * pr.start(new GenericRunnable<Boolean>() { public void run(Boolean error) { if
					 * (error) log("Error Killing myself."); else log("It worked."); } });
					 */
				}

				_repository.updataServerStatus(serverSnapshot, timeout);
			}
		});
	}

	@EventHandler
	public void saveServerStatus(UpdateEvent event) {
		if (event.getType() != UpdateType.FASTER)
			return;

		if (!_enabled)
			return;

		saveServerStatus();
	}

	private void setupConfigValues() {
		try {
			getPlugin().getConfig().addDefault("serverstatus.connectionurl", "127.0.0.1:3307");
			getPlugin().getConfig().set("serverstatus.connectionurl",
					getPlugin().getConfig().getString("serverstatus.connectionurl"));

			getPlugin().getConfig().addDefault("serverstatus.username", "MilitaryPolice");
			getPlugin().getConfig().set("serverstatus.username",
					getPlugin().getConfig().getString("serverstatus.username"));

			getPlugin().getConfig().addDefault("serverstatus.password", "CUPr6Wuw2Rus$qap");
			getPlugin().getConfig().set("serverstatus.password",
					getPlugin().getConfig().getString("serverstatus.password"));

			getPlugin().getConfig().addDefault("serverstatus.asia", true);
			getPlugin().getConfig().set("serverstatus.asia", getPlugin().getConfig().getBoolean("serverstatus.asia"));

			getPlugin().getConfig().addDefault("serverstatus.name", "TEST-1");
			getPlugin().getConfig().set("serverstatus.name", getPlugin().getConfig().getString("serverstatus.name"));

			getPlugin().getConfig().addDefault("serverstatus.group", "Testing");
			getPlugin().getConfig().set("serverstatus.group", getPlugin().getConfig().getString("serverstatus.group"));

			getPlugin().saveConfig();
		} catch (final Exception e) {
			org.bukkit.Bukkit.getServer().getLogger().log(java.util.logging.Level.SEVERE, e.getMessage(), e);
		}
	}
}
