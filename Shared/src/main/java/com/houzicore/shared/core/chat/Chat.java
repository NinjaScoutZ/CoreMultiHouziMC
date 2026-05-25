package com.houzicore.shared.core.chat;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.charset.Charset;
import java.security.cert.X509Certificate;
import java.util.HashMap;
import java.util.Iterator;
import java.util.UUID;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import com.houzicore.shared.MiniPlugin;
import com.houzicore.shared.core.preferences.PreferencesManager;
import com.houzicore.shared.recharge.Recharge;
import com.houzicore.shared.updater.event.UpdateEvent;
import com.houzicore.shared.updater.UpdateType;
import com.houzicore.shared.account.CoreClientManager;
import com.houzicore.shared.core.achievement.AchievementManager;
import com.houzicore.shared.core.chat.command.BroadcastCommand;
import com.houzicore.shared.core.chat.command.ChatSlowCommand;
import com.houzicore.shared.core.chat.command.ChatTestCommand;
import com.houzicore.shared.core.chat.command.SilenceCommand;
import com.houzicore.shared.core.donation.DonationManager;
import com.houzicore.shared.core.level.LvlManager;
import com.houzicore.shared.core.stats.StatsManager;
import com.houzicore.shared.common.Rank;
import com.houzicore.shared.common.util.C;
import com.houzicore.shared.common.util.F;
import com.houzicore.shared.common.util.UtilPlayer;
import com.houzicore.shared.common.util.UtilServer;
import com.houzicore.shared.common.util.UtilText;
import com.houzicore.shared.common.util.UtilTime;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import io.papermc.paper.event.player.AsyncChatEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;

public class Chat extends MiniPlugin {
	public static void trustCert() throws Exception {
		final TrustManager[] trustAllCerts = new TrustManager[] { new X509TrustManager() {
			@Override
			public void checkClientTrusted(X509Certificate[] certs, String authType) {
			}

			@Override
			public void checkServerTrusted(X509Certificate[] certs, String authType) {
			}

			@Override
			public java.security.cert.X509Certificate[] getAcceptedIssuers() {
				return null;
			}

		} };

		final SSLContext sc = SSLContext.getInstance("SSL");
		sc.init(null, trustAllCerts, new java.security.SecureRandom());
		HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());

		// Create all-trusting host name verifier
		final HostnameVerifier allHostsValid = new HostnameVerifier() {
			@Override
			public boolean verify(String hostname, SSLSession session) {
				return true;
			}
		};

		// Install the all-trusting host verifier
		HttpsURLConnection.setDefaultHostnameVerifier(allHostsValid);
	}
	private final CoreClientManager _clientManager;
	private final PreferencesManager _preferences;

	private final AchievementManager _achievements;
	private final StatsManager _statsManager;
	private final DonationManager _donationManager;
	private final String[] _hackusations = { "hack", "hax", "hacker", "hacking", "cheat", "cheater", "cheating",
			"forcefield", "flyhack", "flyhacking", "autoclick", "aimbot" };
	private final String _filterUrl = "https://10.33.53.5:8003/content/item/moderate";
	private final String _appId = "34018d65-466d-4a91-8e92-29ca49f022c4";
	private final String _apiKey = "oUywMpwZcIzZO5AWnfDx";

	private final String _serverName;
	private int _chatSlow = 0;
	private long _silenced = 0;

	private boolean _threeSecondDelay = true;

	private final HashMap<UUID, MessageData> _playerLastMessage = new HashMap<>();

	private static Chat instance;

	public Chat(JavaPlugin plugin, CoreClientManager clientManager, PreferencesManager preferences,
			AchievementManager achievements, String serverName) {
		this(plugin, clientManager, preferences, achievements, null, null, serverName);
	}

	public Chat(JavaPlugin plugin, CoreClientManager clientManager, PreferencesManager preferences,
			AchievementManager achievements, StatsManager statsManager, String serverName) {
		this(plugin, clientManager, preferences, achievements, statsManager, null, serverName);
	}

	public Chat(JavaPlugin plugin, CoreClientManager clientManager, PreferencesManager preferences,
			AchievementManager achievements, StatsManager statsManager, DonationManager donationManager, String serverName) {
		super("Chat", plugin);

		instance = this;

		_clientManager = clientManager;
		_serverName = serverName;
		_preferences = preferences;
		_achievements = achievements;
		_statsManager = statsManager;
		_donationManager = donationManager;

		try {
			trustCert();
		} catch (final Exception e) {
			org.bukkit.Bukkit.getServer().getLogger().log(java.util.logging.Level.SEVERE, e.getMessage(), e);
		}
	}

	@Override
	public void addCommands() {
		addCommand(new SilenceCommand(this));
		addCommand(new BroadcastCommand(this));
		addCommand(new ChatSlowCommand(this));
		addCommand(new ChatTestCommand(this));
		addCommand(new com.houzicore.shared.core.chat.command.UpdateVCommand(this));
		addCommand(new com.houzicore.shared.core.chat.command.SymbolCommand(this));
		addCommand(new com.houzicore.shared.core.chat.command.SpriteCommand(this));
	}

	private int getChatLevel(Player player, Rank rank) {
		if (_statsManager != null && _statsManager.Get(player) != null) {
			return LvlManager.levelFromXp(_statsManager.Get(player).getStat(LvlManager.XP_STAT));
		}

		return 1;
	}

	@SuppressWarnings("unchecked")
	private JSONObject buildJsonChatObject(String filtertype, String name, String player, String msg, String server,
			int rule) {
		final JSONObject message = new JSONObject();
		switch (filtertype) {
		case "chat":
			/*
			 * message.put("player_display_name", name); message.put("player", player);
			 * message.put("text", msg); message.put("server", "gamma"); message.put("room",
			 * server); message.put("language", "en"); message.put("rule", rule);
			 */
			message.put("content", msg);
			break;
		case "moderate":
			final JSONObject content = new JSONObject();
			content.put("content", msg);
			content.put("type", "text");

			final JSONArray parts = new JSONArray();
			parts.add(content);

			final JSONObject mainContent = new JSONObject();
			mainContent.put("applicationId", _appId);
			mainContent.put("createInstant", System.currentTimeMillis());
			mainContent.put("parts", parts);
			mainContent.put("senderDisplayName", name);
			mainContent.put("senderId", player);

			message.put("content", mainContent);
			break;
		case "username":
			message.put("player_id", name);
			message.put("username", name);
			message.put("language", "en");
			message.put("rule", rule);
			break;
		}
		return message;
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	public void filterChat(AsyncPlayerChatEvent event) {
		if (event.isCancelled())
			return;

		if (event.isAsynchronous()) {
			final Player sender = event.getPlayer();
			final String filteredMessage = getFilteredMessage(sender, event.getMessage());
			event.setMessage(filteredMessage);
		}
	}

	/**
	 * Paper 1.21+ authoritative chat renderer.
	 * Applies the unified prefix (clan tag + level badge + rank badge) via
	 * {@link AsyncChatEvent#renderer()} so the format survives Paper's modern
	 * chat pipeline. The legacy {@code AsyncPlayerChatEvent.setFormat()} path
	 * is NOT reliably honoured by Paper, so this handler is the true owner.
	 */
	/**
	 * Build a wide-tag Component by stitching icon chunks named KEY_1, KEY_2, ... KEY_N.
	 * Returns null if no chunks were found for the given key.
	 */
	public static Component buildWideTagComponent(String key) {
		com.houzicore.shared.core.icon.CustomIconManager mgr = com.houzicore.shared.core.icon.CustomIconManager.getInstance();
		if (mgr == null) return null;

		Component result = Component.empty();
		boolean found = false;
		for (int i = 1; i <= 30; i++) {
			com.houzicore.shared.core.icon.IconData part = mgr.getIcon(key + "_" + i);
			if (part != null) {
				found = true;
				result = result.append(Component.object().contents(
						net.kyori.adventure.text.object.ObjectContents.playerHead()
								.profileProperty(net.kyori.adventure.text.object.PlayerHeadObjectContents.property(
										"textures",
										part.getValue(),
										part.getSignature()
								))
								.build()
				).build());
			} else {
				break;
			}
		}
		if (!found) {
			com.houzicore.shared.core.icon.IconData part = mgr.getIcon(key);
			if (part == null) part = mgr.getIcon(key.toLowerCase());
			if (part == null) part = mgr.getIcon("rank_" + key.toLowerCase());
			if (part == null) part = mgr.getIcon("rank_" + key);
			
			if (part != null) {
				return Component.object().contents(
						net.kyori.adventure.text.object.ObjectContents.playerHead()
								.profileProperty(net.kyori.adventure.text.object.PlayerHeadObjectContents.property(
										"textures",
										part.getValue(),
										part.getSignature()
								))
								.build()
				).build();
			}
		}
		return found ? result : null;
	}

	/**
	 * Build a plain-text rank prefix for scoreboard/nametag/below-name contexts.
	 * <p><b>NEVER</b> uses ObjectContents.playerHead() — safe for all non-chat contexts.</p>
	 * <p>Use {@link #buildWideTagComponent(String)} for chat/tablist only.</p>
	 */
	public static Component buildPlainRankPrefix(String key) {
		try {
			Rank rank = Rank.valueOf(key);
			if (rank == null || rank == Rank.ALL) return Component.empty();
			NamedTextColor color = com.houzicore.shared.TablistFix.chatColorToAdventure(rank.GetColor());
			return Component.text(rank.Name + " ", color, net.kyori.adventure.text.format.TextDecoration.BOLD);
		} catch (Exception e) {
			return Component.empty();
		}
	}

	public static Component getChatPrefixComponent(Player player) {
		if (instance == null) return Component.empty();

		Rank playerRank = instance._clientManager.Get(player).GetRank();
		Component wideTag = buildWideTagComponent(playerRank.name());
		
		int level = instance.getChatLevel(player, playerRank);
		String levelBadge = com.houzicore.shared.core.chat.ChatBadgeFormatter.buildLevelBadge(level);
		
		Component levelComp = LegacyComponentSerializer.legacySection().deserialize(levelBadge);
		
		if (wideTag != null) {
			return levelComp.append(wideTag).append(Component.space());
		} else {
			boolean ownsUltra = instance._donationManager != null
					&& instance._donationManager.Get(player.getName()) != null
					&& instance._donationManager.Get(player.getName()).OwnsUltraPackage();
			String rankBadge = ChatBadgeFormatter.buildRankBadge(playerRank, ownsUltra);
			Component rankComp = LegacyComponentSerializer.legacySection().deserialize(rankBadge);
			return levelComp.append(rankComp);
		}
	}

	public static Component getClanTagComponent(Player player) {
		try {
			com.houzicore.shared.core.clan.ClanManager clanMgr = com.houzicore.shared.core.clan.ClanManager.getInstance();
			if (clanMgr != null) {
				com.houzicore.shared.core.clan.Clan clan = clanMgr.getClan(player);
				if (clan != null) {
					return LegacyComponentSerializer.legacySection().deserialize(" §8[§b" + clan.getName() + "§8]");
				}
			}
		} catch (Exception ignored) {}
		return Component.empty();
	}

	public static Component formatChat(Player sender, String displayName, Component message) {
		if (instance == null) return message;
		Component prefixComp = getChatPrefixComponent(sender);
		
		Rank rank = instance._clientManager.Get(sender).GetRank();
		NamedTextColor nameColor;
		if (rank == Rank.ALL) {
			nameColor = NamedTextColor.GRAY;
		} else {
			nameColor = com.houzicore.shared.TablistFix.chatColorToAdventure(rank.GetColor());
		}
		Component nameComp = Component.text(displayName, nameColor);
		Component clanComp = getClanTagComponent(sender);
		
		Component separator = Component.text("§8: ");

		Component finalMessage = replaceObjectTags(message);
		Component styledMessage = finalMessage.color(rank == Rank.ALL ? NamedTextColor.GRAY : NamedTextColor.WHITE);

		return prefixComp.append(nameComp).append(clanComp).append(separator).append(styledMessage);
	}

	public CoreClientManager getClientManager() {
		return _clientManager;
	}

	@EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
	public void renderChat(AsyncChatEvent event) {
		event.renderer((source, sourceDisplayName, message, viewer) -> {
			return formatChat(source, source.getName(), message);
		});
	}

	/**
	 * Scans a Component for {@code <texture:name>} and {@code <sprite:name>}
	 * tags and replaces them with the appropriate object components.
	 * Uses plain-text serialization to reliably match regardless of component nesting.
	 */
	public static Component replaceObjectTags(Component component) {
		return component.replaceText(net.kyori.adventure.text.TextReplacementConfig.builder()
			.match("<(texture|sprite):([a-zA-Z0-9_:./]+)>")
			.replacement((matchResult, builder) -> {
				String tagType = matchResult.group(1);
				String tagValue = matchResult.group(2);

				Component replacement = null;
				if ("texture".equals(tagType)) {
					replacement = buildWideTagComponent(tagValue.toUpperCase());
				} else if ("sprite".equals(tagType)) {
					try {
						if (tagValue.contains(":")) {
							String[] parts = tagValue.split(":", 2);
							net.kyori.adventure.key.Key atlas = net.kyori.adventure.key.Key.key(parts[0]);
							net.kyori.adventure.key.Key sprite = net.kyori.adventure.key.Key.key(parts[1]);
							replacement = Component.object().contents(
									net.kyori.adventure.text.object.ObjectContents.sprite(sprite, atlas)
							).build();
						} else {
							net.kyori.adventure.key.Key sprite = net.kyori.adventure.key.Key.key(tagValue);
							replacement = Component.object().contents(
									net.kyori.adventure.text.object.ObjectContents.sprite(sprite)
							).build();
						}
					} catch (Exception ignored) {}
				}

				if (replacement != null) {
					return replacement;
				} else {
					return builder; // Keep original if no replacement found
				}
			})
			.build());
	}

	public static String getExtChatPrefix(Player player) {
		if (instance == null) return "";
		try {
			final Rank rank = instance._clientManager.Get(player).GetRank();
			final int level = instance.getChatLevel(player, rank);
			boolean ownsUltra = instance._donationManager != null
					&& instance._donationManager.Get(player.getName()) != null
					&& instance._donationManager.Get(player.getName()).OwnsUltraPackage();

			String basePrefix = ChatBadgeFormatter.buildPrefix(level, rank, ownsUltra);

			String clanTag = "";
			try {
				com.houzicore.shared.core.clan.ClanManager clanMgr = com.houzicore.shared.core.clan.ClanManager.getInstance();
				if (clanMgr != null) {
					com.houzicore.shared.core.clan.Clan clan = clanMgr.getClan(player);
					if (clan != null) {
						clanTag = "§8[§b" + clan.getName() + "§8] ";
					}
				}
			} catch (Exception ignored) {}

			return clanTag + basePrefix;
		} catch (Exception e) {
			return "";
		}
	}


	public String getFilteredMessage(Player player, String originalMessage) {
		com.houzicore.shared.common.Rank rank = _clientManager.Get(player).GetRank();

		if (rank.Has(com.houzicore.shared.common.Rank.MODERATOR)) {
			// Staff: allow all & codes including &k obfuscation
			return org.bukkit.ChatColor.translateAlternateColorCodes('&', originalMessage);
		}

		// All players: translate & codes + hex/gradient via HouziColorParser
		// Strip &k obfuscation for non-staff to prevent chat abuse
		return com.houzicore.shared.common.util.HouziColorParser.parse(originalMessage).replace("\u00a7k", "");
	}

	private String getResponseFromCleanSpeak(JSONObject message, String filtertype) {
		/*
		 * String authString = _authName + ":" + _apiKey; byte[] authEncBytes =
		 * Base64.encodeBase64(authString.getBytes()); String authStringEnc = new
		 * String(authEncBytes); String url = _filterUrl + filtertype;
		 */
		final String url = _filterUrl;

		StringBuffer response = null;

		HttpsURLConnection connection = null;
		DataOutputStream outputStream = null;
		BufferedReader bufferedReader = null;
		InputStreamReader inputStream = null;

		try {
			final URL obj = new URL(url);

			connection = (HttpsURLConnection) obj.openConnection();

			// add request header con.setRequestMethod("POST");
			connection.setRequestProperty("Content-Type", "application/json");
			// connection.setRequestProperty("Authorization", "Basic " + authStringEnc);
			connection.setRequestProperty("Connection", "Keep-Alive");
			connection.addRequestProperty("Authentication", _apiKey);

			final String urlParameters = message.toString();

			// Send post request
			connection.setDoOutput(true);
			outputStream = new DataOutputStream(connection.getOutputStream());
			outputStream.writeBytes(urlParameters);
			outputStream.flush();
			outputStream.close();

			inputStream = new InputStreamReader(connection.getInputStream(), Charset.forName("UTF-8"));
			bufferedReader = new BufferedReader(inputStream);
			String inputLine;
			response = new StringBuffer();

			while ((inputLine = bufferedReader.readLine()) != null) {
				response.append(inputLine);
			}

			bufferedReader.close();
		} catch (final Exception exception) {
		} finally {
			if (connection != null) {
				connection.disconnect();
			}

			if (outputStream != null) {
				try {
					outputStream.flush();
				} catch (final IOException e) {
					org.bukkit.Bukkit.getServer().getLogger().log(java.util.logging.Level.SEVERE, e.getMessage(), e);
				}

				try {
					outputStream.close();
				} catch (final IOException e) {
					org.bukkit.Bukkit.getServer().getLogger().log(java.util.logging.Level.SEVERE, e.getMessage(), e);
				}
			}

			if (bufferedReader != null) {
				try {
					bufferedReader.close();
				} catch (final IOException e) {
					org.bukkit.Bukkit.getServer().getLogger().log(java.util.logging.Level.SEVERE, e.getMessage(), e);
				}
			}

			if (inputStream != null) {
				try {
					inputStream.close();
				} catch (final IOException e) {
					org.bukkit.Bukkit.getServer().getLogger().log(java.util.logging.Level.SEVERE, e.getMessage(), e);
				}
			}
		}

		String pmresponse = null;

		if (response != null) {
			pmresponse = response.toString();
		}

		return pmresponse;
	}

	@EventHandler(priority = EventPriority.LOWEST)
	public void HandleChat(AsyncPlayerChatEvent event) {
		if (event.isCancelled())
			return;

		final Player sender = event.getPlayer();

		if (SilenceCheck(sender)) {
			event.setCancelled(true);
			return;
		} else if (_threeSecondDelay && _clientManager.Get(sender).GetRank() == Rank.ALL
				&& getChatLevel(sender, Rank.ALL) < 25
				&& !Recharge.Instance.use(sender, "All Chat Message", 3000, false, false)) {
			UtilPlayer.message(sender, C.cYellow + "You can only chat once every 3 seconds to prevent spam.");
			UtilPlayer.message(sender, C.cYellow + "Buy a Rank at " + C.cGreen + "an administrator" + C.cYellow
					+ " to remove this limit!");
			event.setCancelled(true);
		} else if (!_clientManager.Get(sender).GetRank().Has(Rank.MODERATOR)
				&& !Recharge.Instance.use(sender, "Chat Message", 400, false, false)) {
			UtilPlayer.message(sender, F.main("Chat", "You are sending messages too fast."));
			event.setCancelled(true);
		} else if (!_clientManager.Get(sender).GetRank().Has(Rank.HELPER) && msgContainsHack(event.getMessage())) {
			UtilPlayer.message(sender,
					F.main("Chat",
							"Accusing players of cheating in-game is against the rules."
									+ "If you think someone is cheating, please gather evidence and report it at "
									+ F.link("an administrator")));
			event.setCancelled(true);
		} else if (_playerLastMessage.containsKey(sender.getUniqueId())) {
			final MessageData lastMessage = _playerLastMessage.get(sender.getUniqueId());
			final long chatSlowTime = 1000L * _chatSlow;
			final long timeDiff = System.currentTimeMillis() - lastMessage.getTimeSent();
			if (timeDiff < chatSlowTime && !_clientManager.Get(sender).GetRank().Has(Rank.HELPER)) {
				UtilPlayer.message(sender, F.main("Chat", "Chat slow enabled. Please wait "
						+ F.time(UtilTime.convertString(chatSlowTime - timeDiff, 1, UtilTime.TimeUnit.FIT))));
				event.setCancelled(true);
			} else if (!_clientManager.Get(sender).GetRank().Has(Rank.MODERATOR)
					&& UtilText.isStringSimilar(event.getMessage(), lastMessage.getMessage(), 0.8f)) {
				UtilPlayer.message(sender, F.main("Chat", "This message is too similar to your previous message."));
				event.setCancelled(true);
			}
		}

		if (!event.isCancelled()) {
			_playerLastMessage.put(sender.getUniqueId(), new MessageData(event.getMessage()));
		}
	}

	public String hasher(JSONArray hasharray, String message) {
		final StringBuilder newmsg = new StringBuilder(message);

		for (int i = 0; i < hasharray.size(); i++) {
			final Long charindex = (Long) hasharray.get(i);
			final int charidx = charindex.intValue();
			newmsg.setCharAt(charidx, '*');
		}

		return newmsg.toString();
	}

	@EventHandler
	public void lagTest(PlayerCommandPreprocessEvent event) {
		if (event.getMessage().equals("lag") || event.getMessage().equals("ping")) {
			event.getPlayer().sendMessage(F.main(getName(), "PONG!"));
			event.setCancelled(true);
		}
	}

	private boolean msgContainsHack(String msg) {
		msg = " " + msg.toLowerCase().replaceAll("[^a-z ]", "") + " ";
		for (final String s : _hackusations) {
			if (msg.contains(" " + s + " "))
				return true;
		}
		return false;
	}

	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
	public void onSignChange(SignChangeEvent event) {
		if (_clientManager.Get(event.getPlayer()).GetRank().Has(Rank.ADMIN))
			return;

		// Prevent silenced players from using signs
		if (SilenceCheck(event.getPlayer())) {
			event.setCancelled(true);
			return;
		}

		for (int i = 0; i < event.getLines().length; i++) {
			final String line = event.getLine(i);
			if (line != null && line.length() > 0) {
				final String filteredLine = getFilteredMessage(event.getPlayer(), line);
				if (filteredLine != null) {
					event.setLine(i, filteredLine);
				}
			}
		}
	}

	public JSONArray parseHashes(String response) {
		final JSONObject checkhash = (JSONObject) JSONValue.parse(response);
		JSONArray hasharray;
		hasharray = (JSONArray) checkhash.get("hashes");

		return hasharray;
	}

	@EventHandler
	public void playerQuit(PlayerQuitEvent event) {
		_playerLastMessage.remove(event.getPlayer().getUniqueId());
	}

	@EventHandler
	public void preventMe(PlayerCommandPreprocessEvent event) {
		if (event.getMessage().toLowerCase().startsWith("/me ")
				|| event.getMessage().toLowerCase().startsWith("/bukkit")) {
			event.getPlayer().sendMessage(F.main(getName(), "No, you!"));
			event.setCancelled(true);
		}
	}

	@EventHandler
	public void removeChat(AsyncPlayerChatEvent event) {
		if (event.isCancelled())
			return;

		if (event.isAsynchronous()) {
			for (final Iterator<Player> playerIterator = event.getRecipients().iterator(); playerIterator.hasNext();) {
				if (!_preferences.Get(playerIterator.next()).ShowChat) {
					playerIterator.remove();
				}
			}
		}
	}

	public void setChatSlow(int seconds, boolean inform) {
		if (seconds < 0) {
			seconds = 0;
		}

		_chatSlow = seconds;

		if (inform) {
			if (seconds == 0) {
				UtilServer.broadcast(F.main("Chat", "Chat Slow is now disabled"));
			} else {
				UtilServer.broadcast(
						F.main("Chat", "Chat slow is now enabled with a cooldown of " + F.time(seconds + " seconds")));
			}
		}
	}

	public void setThreeSecondDelay(boolean b) {
		_threeSecondDelay = b;
	}

	public void Silence(long duration, boolean inform) {
		// Set Silenced
		if (duration > 0) {
			_silenced = System.currentTimeMillis() + duration;
		} else {
			_silenced = duration;
		}

		if (!inform)
			return;

		// Announce
		if (duration == -1) {
			UtilServer.broadcast(F.main("Chat", "Chat has been silenced for " + F.time("Permanent") + "."));
		} else if (duration == 0) {
			UtilServer.broadcast(F.main("Chat", "Chat is no longer silenced."));
		} else {
			UtilServer.broadcast(
					F.main("Chat", "Chat has been silenced for " + F.time(UtilTime.MakeStr(duration, 1)) + "."));
		}
	}

	public boolean SilenceCheck(Player player) {
		SilenceEnd();

		if (_silenced == 0)
			return false;

		if (_clientManager.Get(player).GetRank().Has(player, Rank.MODERATOR, false))
			return false;

		if (_silenced == -1) {
			UtilPlayer.message(player, F.main(getName(), "Chat is silenced permanently."));
		} else {
			UtilPlayer.message(player, F.main(getName(), "Chat is silenced for "
					+ F.time(UtilTime.MakeStr(_silenced - System.currentTimeMillis(), 1)) + "."));
		}

		return true;
	}

	public long Silenced() {
		return _silenced;
	}

	public void SilenceEnd() {
		if (_silenced <= 0)
			return;

		if (System.currentTimeMillis() > _silenced) {
			Silence(0, true);
		}
	}

	@EventHandler
	public void SilenceUpdate(UpdateEvent event) {
		if (event.getType() != UpdateType.FAST)
			return;

		SilenceEnd();
	}
}
