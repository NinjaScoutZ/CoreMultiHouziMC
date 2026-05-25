package com.houzicore.shared.core.portal;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.HashSet;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.java.JavaPlugin;

import com.houzicore.shared.MiniPlugin;
import com.houzicore.shared.account.CoreClientManager;
import com.houzicore.shared.common.Rank;
import com.houzicore.shared.common.util.C;
import com.houzicore.shared.common.util.Callback;
import com.houzicore.shared.common.util.F;
import com.houzicore.shared.common.util.UtilPlayer;
import com.houzicore.shared.common.util.UtilTabTitle;
import com.houzicore.shared.core.portal.Commands.SendCommand;
import com.houzicore.shared.core.portal.Commands.ServerCommand;
import com.houzicore.shared.serverdata.Region;
import com.houzicore.shared.serverdata.commands.ServerCommandManager;
import com.houzicore.shared.serverdata.commands.ServerTransfer;
import com.houzicore.shared.serverdata.commands.TransferCommand;
import com.houzicore.shared.serverdata.data.MinecraftServer;
import com.houzicore.shared.serverdata.servers.ServerManager;
import com.houzicore.shared.serverdata.servers.ServerRepository;

public class Portal extends MiniPlugin {
	// The singleton instance of Portal
	private static Portal instance;

	public static Portal getInstance() {
		return instance;
	}

	public static void transferPlayer(String playerName, String serverName) {
		final ServerTransfer serverTransfer = new ServerTransfer(playerName, serverName);
		final TransferCommand transferCommand = new TransferCommand(serverTransfer);
		transferCommand.publish();
	}
	private final ServerRepository _repository;
	private final CoreClientManager _clientManager;

	private final HashSet<String> _connectingPlayers = new HashSet<>();
	private final Region _region;

	private final String _serverName;

	public Portal(JavaPlugin plugin, CoreClientManager clientManager, String serverName) {
		super("Portal", plugin);

		instance = this;
		_clientManager = clientManager;

		_region = plugin.getConfig().getBoolean("serverstatus.asia") ? Region.ASIA : Region.TH;
		_serverName = serverName;
		_repository = ServerManager.getServerRepository(_region);

		Bukkit.getMessenger().registerOutgoingPluginChannel(getPlugin(), "BungeeCord");

		// Register the server command type for future use
		ServerCommandManager.getInstance().registerCommandType("TransferCommand", TransferCommand.class,
				new TransferHandler());
	}

	@Override
	public void addCommands() {
		addCommand(new ServerCommand(this));
		addCommand(new SendCommand(this));
	}

	public void doesServerExist(final String serverName, final Callback<Boolean> callback) {
		if (callback == null)
			return;

		Bukkit.getScheduler().runTaskAsynchronously(getPlugin(), new Runnable() {
			@Override
			public void run() {
				final boolean serverExists = ServerManager.getServerRepository(_region).serverExists(serverName);

				Bukkit.getScheduler().runTask(getPlugin(), new Runnable() {
					@Override
					public void run() {
						callback.run(serverExists);
					}
				});
			}
		});
	}

	@EventHandler
	public void join(PlayerJoinEvent event) {
		// Used to inject BrandConfig Tablist here. Now handled securely per-server module.
	}

	public void sendAllPlayers(String serverName) {
		for (final Player player : Bukkit.getOnlinePlayers()) {
			sendPlayerToServer(player, serverName);
		}
	}

	private void sendPlayer(final Player player, String serverName) {
		final ByteArrayOutputStream b = new ByteArrayOutputStream();
		final DataOutputStream out = new DataOutputStream(b);

		try {
			out.writeUTF("Connect");
			out.writeUTF(serverName);
		} catch (final IOException e) {
			// Can never happen
		} finally {
			try {
				out.close();
			} catch (final IOException e) {
				org.bukkit.Bukkit.getServer().getLogger().log(java.util.logging.Level.SEVERE, e.getMessage(), e);
			}
		}

		player.sendPluginMessage(getPlugin(), "BungeeCord", b.toByteArray());
		_connectingPlayers.add(player.getName());

		getScheduler().scheduleSyncDelayedTask(getPlugin(), new Runnable() {
			@Override
			public void run() {
				_connectingPlayers.remove(player.getName());
			}
		}, 20L);

		UtilPlayer.message(player, F.main(getName(),
				com.houzicore.shared.core.lang.LangManager.get().get(player, "portal.sent").replace("{0}", _serverName).replace("{1}", serverName)));
	}

	public void sendPlayerToServer(Player player, String serverName) {
		sendPlayerToServer(player, serverName, true);
	}

	public void sendPlayerToServer(final Player player, final String serverName, boolean callEvent) {
		if (_connectingPlayers.contains(player.getName()))
			return;

		if (callEvent) {
			final ServerTransferEvent event = new ServerTransferEvent(player, serverName);
			Bukkit.getPluginManager().callEvent(event);
		}

		final boolean override = serverName.equalsIgnoreCase("Lobby");
		final Rank playerRank = _clientManager.Get(player).GetRank();

		if (override) {
			sendPlayer(player, serverName);
		} else {
			runAsync(new Runnable() {
				@Override
				public void run() {
					final MinecraftServer server = _repository.getServerStatus(serverName);

					if (server == null)
						return;

					Bukkit.getServer().getScheduler().runTask(_plugin, new Runnable() {
						@Override
						public void run() {
							if (server.getPlayerCount() < server.getMaxPlayerCount() || playerRank.Has(Rank.WARRIOR)) {
								sendPlayer(player, serverName);
							} else {
								UtilPlayer.message(player,
										F.main(getName(), com.houzicore.shared.core.lang.LangManager.get().get(player, "portal.is_full").replace("{0}", serverName)));
							}
						}
					});
				}
			});
		}
	}

	public void sendToHub(Player player, String message) {
		if (message != null) {
			UtilPlayer.message(player, "  ");
			UtilPlayer.message(player, C.cGold + C.Bold + message);
			UtilPlayer.message(player, "  ");
		}

		player.playSound(player.getLocation(), Sound.ENTITY_ENDER_DRAGON_GROWL, 10f, 1f);
		sendPlayerToServer(player, "Lobby");
	}
}
