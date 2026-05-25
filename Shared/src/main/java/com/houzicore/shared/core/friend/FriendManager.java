package com.houzicore.shared.core.friend;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.plugin.java.JavaPlugin;

import com.houzicore.shared.MiniDbClientPlugin;
import com.houzicore.shared.account.CoreClientManager;
import com.houzicore.shared.common.Rank;
import com.houzicore.shared.common.jsonchat.ChildJsonMessage;
import com.houzicore.shared.common.jsonchat.JsonMessage;
import com.houzicore.shared.common.util.C;
import com.houzicore.shared.common.util.F;
import com.houzicore.shared.common.util.NautHashMap;
import com.houzicore.shared.common.util.UtilServer;
import com.houzicore.shared.common.util.UtilTime;
import com.houzicore.shared.core.friend.command.AddFriend;
import com.houzicore.shared.core.friend.command.DeleteFriend;
import com.houzicore.shared.core.friend.command.FriendsDisplay;
import com.houzicore.shared.core.friend.data.FriendData;
import com.houzicore.shared.core.friend.data.FriendRepository;
import com.houzicore.shared.core.friend.data.FriendStatus;
import com.houzicore.shared.core.friend.ui.FriendShop;
import com.houzicore.shared.core.portal.Portal;
import com.houzicore.shared.core.preferences.PreferencesManager;
import com.houzicore.shared.updater.UpdateType;
import com.houzicore.shared.updater.event.UpdateEvent;

public class FriendManager extends MiniDbClientPlugin<FriendData> {
	private static FriendSorter _friendSorter = new FriendSorter();

	private final PreferencesManager _preferenceManager;
	private final FriendRepository _repository;
	private final Portal _portal;
	private final FriendShop _friendShop;

	public FriendManager(JavaPlugin plugin, CoreClientManager clientManager, PreferencesManager preferences,
			Portal portal) {
		super("Friends", plugin, clientManager);

		_preferenceManager = preferences;
		_repository = new FriendRepository(plugin);
		_portal = portal;
		_friendShop = new FriendShop(this, clientManager);
	}

	public FriendShop getShop() {
		return _friendShop;
	}

	@Override
	public void addCommands() {
		addCommand(new AddFriend(this));
		addCommand(new DeleteFriend(this));
		addCommand(new FriendsDisplay(this));
		addCommand(new com.houzicore.shared.core.friend.command.FavoriteFriend(this));
	}

	public void addFriend(final Player caller, final String name) {
		if (caller.getName().equalsIgnoreCase(name)) {
			caller.sendMessage(F.main(getName(), com.houzicore.shared.core.lang.LangManager.get().get(caller, "friend.cannot_add_self")));
			return;
		}

		boolean update = false;
		for (final FriendStatus status : Get(caller).getFriends()) {
			if (status.Name.equalsIgnoreCase(name)) {
				if (status.Status == FriendStatusType.Pending || status.Status == FriendStatusType.Blocked) {
					update = true;
					break;
				} else if (status.Status == FriendStatusType.Denied) {
					caller.sendMessage(F.main(getName(),
							com.houzicore.shared.core.lang.LangManager.get().get(caller, "friend.denied").replace("{0}", name)));
					return;
				} else if (status.Status == FriendStatusType.Accepted) {
					caller.sendMessage(F.main(getName(), com.houzicore.shared.core.lang.LangManager.get().get(caller, "friend.already_friends").replace("{0}", name)));
					return;
				} else if (status.Status == FriendStatusType.Sent) {
					caller.sendMessage(F.main(getName(),
							com.houzicore.shared.core.lang.LangManager.get().get(caller, "friend.pending").replace("{0}", name)));
					return;
				}
			}
		}

		final boolean updateFinal = update;

		Bukkit.getServer().getScheduler().runTaskAsynchronously(getPlugin(), new Runnable() {
			@Override
			public void run() {
				if (updateFinal) {
					_repository.updateFriend(caller.getName(), name, "Accepted");
					_repository.updateFriend(name, caller.getName(), "Accepted");

					Bukkit.getServer().getScheduler().runTask(_plugin, new Runnable() {
						@Override
						public void run() {
							for (final FriendStatus status : Get(caller).getFriends()) {
								if (status.Name.equalsIgnoreCase(name)) {
									status.Status = FriendStatusType.Accepted;
									break;
								}
							}
						}
					});
				} else {
					_repository.addFriend(caller, name);

					Bukkit.getServer().getScheduler().runTask(_plugin, new Runnable() {
						@Override
						public void run() {
							for (final FriendStatus status : Get(caller).getFriends()) {
								if (status.Name.equalsIgnoreCase(name)) {
									status.Status = FriendStatusType.Sent;
									break;
								}
							}
						}
					});
				}

				Bukkit.getServer().getScheduler().runTask(_plugin, new Runnable() {
					@Override
					public void run() {
						if (updateFinal) {
							caller.sendMessage(F.main(getName(),
									com.houzicore.shared.core.lang.LangManager.get().get(caller, "friend.now_friends").replace("{0}", name)));
						} else {
							caller.sendMessage(F.main(getName(),
									com.houzicore.shared.core.lang.LangManager.get().get(caller, "friend.added").replace("{0}", name)));
						}
					}
				});
			}
		});
	}

	@Override
	protected FriendData AddPlayer(String player) {
		return new FriendData();
	}

	public Portal getPortal() {
		return _portal;
	}

	public PreferencesManager getPreferenceManager() {
		return _preferenceManager;
	}

	@Override
	public String getQuery(int accountId, String uuid, String name) {
		return "SELECT tA.Name, status, tA.lastLogin, now(), accountFriend.favorite, tA.uuid FROM accountFriend INNER Join accounts AS fA ON fA.uuid = uuidSource INNER JOIN accounts AS tA ON tA.uuid = uuidTarget WHERE uuidSource = '"
				+ uuid + "';";
	}

	public boolean isFriends(Player player, String friend) {
		final FriendData friendData = Get(player);

		for (final FriendStatus friendStatus : friendData.getFriends()) {
			if (friendStatus.Name.equalsIgnoreCase(friend))
				return true;
		}

		return false;
	}

	@Override
	public void processLoginResultSet(String playerName, int accountId, ResultSet resultSet) throws SQLException {
		Set(playerName, _repository.loadClientInformation(resultSet));
	}

	public void removeFriend(final Player caller, final String name) {
		Bukkit.getServer().getScheduler().runTaskAsynchronously(getPlugin(), new Runnable() {
			@Override
			public void run() {
				_repository.removeFriend(caller.getName(), name);
				_repository.removeFriend(name, caller.getName());

				Bukkit.getServer().getScheduler().runTask(_plugin, new Runnable() {
					@Override
					public void run() {
						for (final FriendStatus status : Get(caller).getFriends()) {
							if (status.Name.equalsIgnoreCase(name)) {
								status.Status = FriendStatusType.Blocked;
								break;
							}
						}

						caller.sendMessage(F.main(getName(),
								com.houzicore.shared.core.lang.LangManager.get().get(caller, "friend.deleted").replace("{0}", name)));
					}
				});
			}
		});
	}

	public void updateFavorite(final Player caller, final String name, final boolean favorite) {
		Bukkit.getServer().getScheduler().runTaskAsynchronously(getPlugin(), new Runnable() {
			@Override
			public void run() {
				_repository.updateFavorite(caller.getName(), name, favorite);

				Bukkit.getServer().getScheduler().runTask(_plugin, new Runnable() {
					@Override
					public void run() {
						for (final FriendStatus status : Get(caller).getFriends()) {
							if (status.Name.equalsIgnoreCase(name)) {
								status.Favorite = favorite;
								break;
							}
						}
					}
				});
			}
		});
	}

	public void showFriends(Player caller) {
		final boolean isStaff = ClientManager.Get(caller).GetRank().Has(Rank.HELPER);
		boolean gotAFriend = false;
		final List<FriendStatus> friendStatuses = Get(caller).getFriends();
		Collections.sort(friendStatuses, _friendSorter);

		caller.sendMessage(C.cAqua + C.Strike + "======================[" + ChatColor.RESET + C.cWhite + C.Bold
				+ "Friends" + ChatColor.RESET + C.cAqua + C.Strike + "]======================");

		final ArrayList<ChildJsonMessage> sentLines = new ArrayList<>();
		final ArrayList<ChildJsonMessage> pendingLines = new ArrayList<>();
		final ArrayList<ChildJsonMessage> onlineLines = new ArrayList<>();
		final ArrayList<ChildJsonMessage> offlineLines = new ArrayList<>();

		for (final FriendStatus friend : friendStatuses) {
			if (friend.Status == FriendStatusType.Blocked || friend.Status == FriendStatusType.Denied) {
				continue;
			}

			if (!_preferenceManager.Get(caller).PendingFriendRequests && friend.Status == FriendStatusType.Pending) {
				continue;
			}

			gotAFriend = true;

			final ChildJsonMessage message = new JsonMessage("").color("white").extra("").color("white");

			if (friend.Status == FriendStatusType.Accepted) {
				// Online Friend
				if (friend.Online) {
					if (friend.ServerName.contains("Staff") || friend.ServerName.contains("CUST")) {
						if (isStaff && friend.ServerName.contains("Staff")) {
							message.add("Teleport").color("green").bold()
									.click("run_command", "/server " + friend.ServerName)
									.hover("show_text", "Teleport to " + friend.Name + "'s server.");
						} else {
							message.add("No Teleport").color("yellow").bold();
						}
					} else {
						message.add("Teleport").color("green").bold()
								.click("run_command", "/server " + friend.ServerName)
								.hover("show_text", "Teleport to " + friend.Name + "'s server.");
					}

					message.add(" - ").color("white");
					message.add(friend.Favorite ? "★" : "☆").color(friend.Favorite ? "yellow" : "gray").bold().click("run_command", "/friendfavorite " + friend.Name)
							.hover("show_text", "Toggle Favorite for " + friend.Name);
					message.add(" - ").color("white");
					message.add("Delete").color("red").bold().click("run_command", "/unfriend " + friend.Name)
							.hover("show_text", "Remove " + friend.Name + " from your friends list.");
					message.add(" - ").color("white");
					message.add(friend.Name).color(friend.Online ? "green" : "gray");
					message.add(" - ").color("white");

					if (friend.ServerName.contains("Staff") || friend.ServerName.contains("CUST")) {
						if (isStaff && friend.ServerName.contains("Staff")) {
							message.add(friend.ServerName).color("dark_green");
						} else {
							message.add("Private Staff Server").color("dark_green");
						}
					} else {
						message.add(friend.ServerName).color("dark_green");
					}

					onlineLines.add(message);
				}
				// Offline Friend
				else {
					message.add(friend.Favorite ? "★" : "☆").color(friend.Favorite ? "yellow" : "gray").bold().click("run_command", "/friendfavorite " + friend.Name)
							.hover("show_text", "Toggle Favorite for " + friend.Name);
					message.add(" - ").color("white");
					message.add("Delete").color("red").bold().click("run_command", "/unfriend " + friend.Name)
							.hover("show_text", "Remove " + friend.Name + " from your friends list.");
					message.add(" - ").color("white");
					message.add(friend.Name).color(friend.Online ? "green" : "gray");
					message.add(" - ").color("white");
					message.add("Offline for ").color("gray").add(UtilTime.MakeStr(friend.LastSeenOnline))
							.color("gray");

					offlineLines.add(message);
				}
			}
			// Pending
			else if (friend.Status == FriendStatusType.Pending) {
				message.add("Accept").color("green").bold().click("run_command", "/friend " + friend.Name)
						.hover("show_text", "Accept " + friend.Name + "'s friend request.");
				message.add(" - ").color("white");
				message.add("Deny").color("red").bold().click("run_command", "/unfriend " + friend.Name)
						.hover("show_text", "Deny " + friend.Name + "'s friend request.");
				message.add(" - ").color("white");
				message.add(friend.Name + " Requested Friendship").color("gray");

				pendingLines.add(message);
			}
			// Sent
			else if (friend.Status == FriendStatusType.Sent) {
				message.add("Cancel").color("red").bold().click("run_command", "/unfriend " + friend.Name)
						.hover("show_text", "Cancel friend request to " + friend.Name);
				message.add(" - ").color("white");
				message.add(friend.Name + " Friendship Request").color("gray");

				sentLines.add(message);
			}
		}

		// Send In Order
		for (final JsonMessage msg : sentLines) {
			msg.sendToPlayer(caller);
		}

		for (final JsonMessage msg : offlineLines) {
			msg.sendToPlayer(caller);
		}

		for (final JsonMessage msg : pendingLines) {
			msg.sendToPlayer(caller);
		}

		for (final JsonMessage msg : onlineLines) {
			msg.sendToPlayer(caller);
		}

		if (!gotAFriend) {
			caller.sendMessage(" ");
			caller.sendMessage("Welcome to your Friends List!");
			caller.sendMessage(" ");
			caller.sendMessage("To add friends, type " + C.cGreen + "/friend <Player Name>");
			caller.sendMessage(" ");
			caller.sendMessage(
					"Type " + C.cGreen + "/friend" + ChatColor.RESET + " at any time to interact with your friends!");
			caller.sendMessage(" ");
		}

		final ChildJsonMessage message = new JsonMessage("").extra(C.cAqua + C.Strike + "======================");

		message.add(C.cDAqua + "Toggle GUI").click("run_command", "/friendsdisplay");

		message.hover("show_text", C.cAqua + "Toggle friends to display in a inventory");

		message.add(C.cAqua + C.Strike + "======================");

		message.sendToPlayer(caller);
	}

	@EventHandler
	public void updateFriends(UpdateEvent event) {
		if (event.getType() != UpdateType.SLOW || Bukkit.getOnlinePlayers().size() == 0)
			return;

		final Player[] onlinePlayers = UtilServer.getPlayers();

		Bukkit.getServer().getScheduler().runTaskAsynchronously(_plugin, new Runnable() {
			@Override
			public void run() {
				final NautHashMap<String, FriendData> newData = _repository.getFriendsForAll(onlinePlayers);

				Bukkit.getServer().getScheduler().runTask(_plugin, new Runnable() {
					@Override
					public void run() {
						for (final Player player : Bukkit.getOnlinePlayers()) {
							if (newData.containsKey(player.getUniqueId().toString())) {
								Get(player).setFriends(newData.get(player.getUniqueId().toString()).getFriends());
							} else {
								Get(player).getFriends().clear();
							}
						}
					}
				});
			}
		});
	}
}
