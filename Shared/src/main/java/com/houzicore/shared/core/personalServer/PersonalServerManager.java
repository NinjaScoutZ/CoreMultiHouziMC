package com.houzicore.shared.core.personalServer;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

import com.houzicore.shared.MiniPlugin;
import com.houzicore.shared.account.CoreClientManager;
import com.houzicore.shared.common.Rank;
import com.houzicore.shared.common.jsonchat.ClickEvent;
import com.houzicore.shared.common.jsonchat.Color;
import com.houzicore.shared.common.jsonchat.HoverEvent;
import com.houzicore.shared.common.jsonchat.JsonMessage;
import com.houzicore.shared.common.util.C;
import com.houzicore.shared.common.util.F;
import com.houzicore.shared.common.util.UtilMath;
import com.houzicore.shared.common.util.UtilPlayer;
import com.houzicore.shared.core.itemstack.ItemStackFactory;
import com.houzicore.shared.recharge.Recharge;
import com.houzicore.shared.serverdata.Region;
import com.houzicore.shared.serverdata.data.ServerGroup;
import com.houzicore.shared.serverdata.servers.ServerManager;
import com.houzicore.shared.serverdata.servers.ServerRepository;

public class PersonalServerManager extends MiniPlugin {
	private final ServerRepository _repository;
	private final CoreClientManager _clientManager;

	private final boolean _us;

	private final int _interfaceSlot = 6;
	private final ItemStack _interfaceItem;
	private boolean _giveInterfaceItem = false;

	public PersonalServerManager(JavaPlugin plugin, CoreClientManager clientManager) {
		super("Personal Server Manager", plugin);

		_clientManager = clientManager;

		setupConfigValues();

		_us = plugin.getConfig().getBoolean("serverstatus.asia");

		final Region region = _us ? Region.ASIA : Region.TH;
		_repository = ServerManager.getServerRepository(region);

		_interfaceItem = ItemStackFactory.Instance.CreateStack(Material.GLISTERING_MELON_SLICE, (byte) 0, 1,
				C.cGreen + "/hostserver");
	}

	@Override
	public void addCommands() {
		addCommand(new HostServerCommand(this));
		addCommand(new HostEventServerCommand(this));
	}

	private void createGroup(final Player host, final String serverName, final int ram, final int cpu,
			final int minPlayers, final int maxPlayers, final String games, final boolean event) {
		getPlugin().getServer().getScheduler().runTaskAsynchronously(getPlugin(), new Runnable() {
			@Override
			public void run() {
				for (final ServerGroup existingServerGroup : _repository.getServerGroups(null)) {
					if (existingServerGroup.getPrefix().equalsIgnoreCase(serverName)
							|| existingServerGroup.getName().equalsIgnoreCase(serverName)) {
						if (host.getName().equalsIgnoreCase(existingServerGroup.getHost())) {
							host.sendMessage(F.main(getName(),
									"Your server is still being created or already exists.  If you haven't been connected in 20 seconds, type /server "
											+ serverName + "-1."));
						} else {
							host.sendMessage(C.cRed
									+ "Sorry, but you're not allowed to create a MPS server because you have chosen a name to glitch the system :)");
						}

						return;
					}
				}

				final ServerGroup serverGroup = new ServerGroup(serverName, serverName, host.getName(), ram, cpu, 1, 0,
						UtilMath.random.nextInt(250) + 19999, true, "arcade.zip", "Arcade.jar", "plugins/Arcade/",
						minPlayers, maxPlayers, true, false, false, games, "Player", true, event, false, true, false,
						true, true, false, false, false, false, true, true, true, false, false, "",
						_us ? Region.ASIA : Region.TH);

				getPlugin().getServer().getScheduler().runTaskAsynchronously(getPlugin(), new Runnable() {
					@Override
					public void run() {
						_repository.updateServerGroup(serverGroup);
						Bukkit.getScheduler().runTask(getPlugin(), new Runnable() {
							@Override
							public void run() {
								host.sendMessage(F.main(getName(),
										serverName + "-1 successfully created.  You will be sent to it shortly."));
								host.sendMessage(
										F.main(getName(), "If you haven't been connected in 20 seconds, type /server "
												+ serverName + "-1."));
							}
						});
					}
				});
			}
		});
	}

	public void hostServer(Player player, String serverName, boolean eventServer) {
		int ram = 1024;
		int cpu = 1;

		final Rank rank = _clientManager.Get(player).GetRank();

		if (eventServer || rank.Has(Rank.SNR_MODERATOR) || rank == Rank.YOUTUBE || rank == Rank.TWITCH) {
			ram = 2048;
			cpu = 4;
		}

		if (eventServer) {
			createGroup(player, "EVENT", ram, cpu, 40, 80, "Event", eventServer);
		} else {
			createGroup(player, serverName, ram, cpu, 40, 80, "Smash", eventServer);
		}
	}

	@EventHandler
	public void onJoin(PlayerJoinEvent event) {
		if (_giveInterfaceItem) {
			event.getPlayer().getInventory().setItem(_interfaceSlot, _interfaceItem);
		}
	}

	@EventHandler
	public void openServer(PlayerInteractEvent event) {
		if (_interfaceItem.equals(event.getPlayer().getItemInHand())) {
			if (!Recharge.Instance.use(event.getPlayer(), "Host Server Melon", 30000, false, false))
				return;

			if (_clientManager.Get(event.getPlayer()).GetRank().Has(Rank.DIVINE)) {
				showHostMessage(event.getPlayer());
			} else {
				UtilPlayer.message(event.getPlayer(), F.main("Server",
						com.houzicore.shared.core.lang.LangManager.get().get(event.getPlayer(), "server.rank_required").replace("{0}", F.rank(Rank.DIVINE))));
			}
		}
	}

	private void setupConfigValues() {
		try {
			if (!getPlugin().getConfig().contains("serverstatus.asia")) {
				getPlugin().getConfig().addDefault("serverstatus.asia", true);
				getPlugin().getConfig().set("serverstatus.asia", true);
				getPlugin().saveConfig();
			}
		} catch (final Exception e) {
			org.bukkit.Bukkit.getServer().getLogger().log(java.util.logging.Level.SEVERE, e.getMessage(), e);
		}
	}

	public void showHostMessage(Player player) {
		UtilPlayer.message(player, C.cRed + "────────────────────────────────────────────────");
		UtilPlayer.message(player, com.houzicore.shared.core.lang.LangManager.get().get(player, "server.create_info"));
		UtilPlayer.message(player, com.houzicore.shared.core.lang.LangManager.get().get(player, "server.play_friends"));

		new JsonMessage("Please ").click(ClickEvent.RUN_COMMAND, "/hostserver")
				.hover(HoverEvent.SHOW_TEXT, com.houzicore.shared.core.lang.LangManager.get().get(player, "server.click_create_hover")).extra(com.houzicore.shared.core.lang.LangManager.get().get(player, "server.click_here")).color(Color.GREEN)
				.extra(com.houzicore.shared.core.lang.LangManager.get().get(player, "server.confirm")).color(Color.WHITE)
				.send(JsonMessage.MessageType.CHAT_BOX, player);

		UtilPlayer.message(player, C.cRed + "────────────────────────────────────────────────");
	}
}
