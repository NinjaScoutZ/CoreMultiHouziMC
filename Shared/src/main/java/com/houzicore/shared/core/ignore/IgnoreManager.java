package com.houzicore.shared.core.ignore;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.houzicore.shared.MiniDbClientPlugin;
import com.houzicore.shared.account.CoreClientManager;
import com.houzicore.shared.common.Rank;
import com.houzicore.shared.common.jsonchat.ChildJsonMessage;
import com.houzicore.shared.common.jsonchat.JsonMessage;
import com.houzicore.shared.common.util.C;
import com.houzicore.shared.common.util.F;
import com.houzicore.shared.core.ignore.command.Ignore;
import com.houzicore.shared.core.ignore.command.Unignore;
import com.houzicore.shared.core.ignore.data.IgnoreData;
import com.houzicore.shared.core.ignore.data.IgnoreRepository;
import com.houzicore.shared.core.portal.Portal;
import com.houzicore.shared.core.preferences.PreferencesManager;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.plugin.java.JavaPlugin;

public class IgnoreManager extends MiniDbClientPlugin<IgnoreData> {
	private final PreferencesManager _preferenceManager;
	private final IgnoreRepository _repository;
	private final Portal _portal;
	private final com.houzicore.shared.core.ignore.ui.IgnoreShop _ignoreShop;

	public IgnoreManager(JavaPlugin plugin, CoreClientManager clientManager, PreferencesManager preferences,
			Portal portal) {
		super("Ignore", plugin, clientManager);

		_preferenceManager = preferences;
		_repository = new IgnoreRepository(plugin);
		_portal = portal;
		_ignoreShop = new com.houzicore.shared.core.ignore.ui.IgnoreShop(this, clientManager);
	}

	public com.houzicore.shared.core.ignore.ui.IgnoreShop getShop() {
		return _ignoreShop;
	}

	@Override
	public void addCommands() {
		addCommand(new Ignore(this));
		addCommand(new Unignore(this));
	}

	public void addIgnore(final Player caller, final String name) {
		if (caller.getName().equalsIgnoreCase(name)) {
			caller.sendMessage(F.main(getName(), com.houzicore.shared.core.lang.LangManager.get().get(caller, "ignore.cannot_ignore_self")));
			return;
		}

		for (final String status : Get(caller).getIgnored()) {
			if (status.equalsIgnoreCase(name)) {
				caller.sendMessage(
						F.main(getName(), com.houzicore.shared.core.lang.LangManager.get().get(caller, "ignore.already_ignored").replace("{0}", name)));
				return;

			}
		}

		final IgnoreData ignoreData = Get(caller);

		if (ignoreData != null) {
			ignoreData.getIgnored().add(name);
		}

		Bukkit.getServer().getScheduler().runTaskAsynchronously(getPlugin(), new Runnable() {
			@Override
			public void run() {
				_repository.addIgnore(caller, name);

				Bukkit.getServer().getScheduler().runTask(_plugin, new Runnable() {
					@Override
					public void run() {
						caller.sendMessage(F.main(getName(), com.houzicore.shared.core.lang.LangManager.get().get(caller, "ignore.now_ignoring").replace("{0}", name)));
					}
				});
			}
		});
	}

	@Override
	protected IgnoreData AddPlayer(String player) {
		return new IgnoreData();
	}

	public Portal getPortal() {
		return _portal;
	}

	public PreferencesManager getPreferenceManager() {
		return _preferenceManager;
	}

	@Override
	public String getQuery(int accountId, String uuid, String name) {
		return "SELECT tA.Name FROM accountIgnore INNER Join accounts AS fA ON fA.uuid = uuidIgnorer INNER JOIN accounts AS tA ON tA.uuid = uuidIgnored WHERE uuidIgnorer = '"
				+ uuid + "';";
	}

	public boolean isIgnoring(Player caller, Player target) {
		return isIgnoring(caller, target.getName());
	}

	public boolean isIgnoring(Player caller, String target) {
		final IgnoreData data = Get(caller);

		for (final String ignored : data.getIgnored()) {
			if (ignored.equalsIgnoreCase(target))
				return true;
		}

		return false;
	}

	@EventHandler
	public void onChat(AsyncPlayerChatEvent event) {
		if (ClientManager.Get(event.getPlayer()).GetRank().Has(Rank.HELPER))
			return;

		final Iterator<Player> itel = event.getRecipients().iterator();

		while (itel.hasNext()) {
			final Player player = itel.next();

			final IgnoreData info = Get(player);

			for (final String ignored : info.getIgnored()) {
				if (ignored.equalsIgnoreCase(event.getPlayer().getName())) {
					itel.remove();

					break;
				}
			}
		}
	}

	@Override
	public void processLoginResultSet(String playerName, int accountId, ResultSet resultSet) throws SQLException {
		Set(playerName, _repository.loadClientInformation(resultSet));
	}

	public void removeIgnore(final Player caller, final String name) {
		final IgnoreData ignoreData = Get(caller);

		if (ignoreData != null) {
			final Iterator<String> itel = ignoreData.getIgnored().iterator();

			while (itel.hasNext()) {
				final String ignored = itel.next();

				if (ignored.equalsIgnoreCase(name)) {
					itel.remove();
					break;
				}
			}
		}

		caller.sendMessage(F.main(getName(), com.houzicore.shared.core.lang.LangManager.get().get(caller, "ignore.no_longer_ignoring").replace("{0}", name)));

		Bukkit.getServer().getScheduler().runTaskAsynchronously(getPlugin(), new Runnable() {
			@Override
			public void run() {
				_repository.removeIgnore(caller.getName(), name);
			}
		});
	}

	public void showIgnores(Player caller) {
		final List<String> ignoredPlayers = Get(caller).getIgnored();

		caller.sendMessage(C.cAqua + C.Strike + "=====================[" + ChatColor.RESET + C.cWhite + C.Bold
				+ "Ignoring" + ChatColor.RESET + C.cAqua + C.Strike + "]======================");

		final ArrayList<ChildJsonMessage> sentLines = new ArrayList<>();

		for (final String ignored : ignoredPlayers) {

			final ChildJsonMessage message = new JsonMessage("").color("white").extra("").color("white");

			message.add("Ignoring " + ignored).color("gray");

			message.add(" - ").color("white");

			message.add("Unignore").color("red").bold().click("run_command", "/unignore " + ignored).hover("show_text",
					"Stop ignoring " + ignored);

			sentLines.add(message);
		}

		// Send In Order
		for (final JsonMessage msg : sentLines) {
			msg.sendToPlayer(caller);
		}

		if (sentLines.isEmpty()) {
			caller.sendMessage(" ");
			caller.sendMessage("Welcome to your Ignore List!");
			caller.sendMessage(" ");
			caller.sendMessage("To ignore people, type " + C.cGreen + "/ignore <Player Name>");
			caller.sendMessage(" ");
			caller.sendMessage("Type " + C.cGreen + "/ignore" + ChatColor.RESET + " at any time to view the ignored!");
			caller.sendMessage(" ");
		}

		final ChildJsonMessage message = new JsonMessage("")
				.extra(C.cAqua + C.Strike + "=====================================================");

		message.sendToPlayer(caller);
	}
}
