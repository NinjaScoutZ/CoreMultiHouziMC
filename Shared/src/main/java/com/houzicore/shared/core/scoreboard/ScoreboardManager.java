package com.houzicore.shared.core.scoreboard;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Locale;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.java.JavaPlugin;

import com.houzicore.shared.MiniPlugin;
import com.houzicore.shared.account.CoreClientManager;
import com.houzicore.shared.common.Rank;
import com.houzicore.shared.core.donation.DonationManager;
import com.houzicore.shared.updater.UpdateType;
import com.houzicore.shared.updater.event.UpdateEvent;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.megavex.scoreboardlibrary.api.ScoreboardLibrary;
import net.megavex.scoreboardlibrary.api.exception.NoPacketAdapterAvailableException;
import net.megavex.scoreboardlibrary.api.noop.NoopScoreboardLibrary;
import net.megavex.scoreboardlibrary.api.team.ScoreboardTeam;
import net.megavex.scoreboardlibrary.api.team.TeamDisplay;
import net.megavex.scoreboardlibrary.api.team.TeamManager;
import net.megavex.scoreboardlibrary.api.team.enums.NameTagVisibility;

public class ScoreboardManager extends MiniPlugin {
	private static final LegacyComponentSerializer LEGACY_SERIALIZER =
			LegacyComponentSerializer.legacySection();

	private final CoreClientManager _clientManager;
	private final DonationManager _donationManager;

	private static ScoreboardManager _instance;

	private ScoreboardLibrary _scoreboardLibrary;
	private TeamManager _teamManager;

	// This stores current scoreboard for the player
	private final HashMap<Player, PlayerScoreboard> _playerScoreboards = new HashMap<>();

	// Scoreboards (can be shared between players)
	private final HashMap<String, ScoreboardData> _scoreboards = new HashMap<>();

	public ScoreboardManager(JavaPlugin plugin, CoreClientManager clientManager, DonationManager donationManager) {
		super("Scoreboard Manager", plugin);
		_instance = this;
		_clientManager = clientManager;
		_donationManager = donationManager;

		for (Player player : Bukkit.getOnlinePlayers()) {
			_playerScoreboards.put(player, new PlayerScoreboard(this, player));
			assignRankTeam(player);
		}
	}

	@Override
	public void enable() {
		try {
			_scoreboardLibrary = ScoreboardLibrary.loadScoreboardLibrary(getPlugin());
		} catch (NoPacketAdapterAvailableException e) {
			_scoreboardLibrary = new NoopScoreboardLibrary();
			getPlugin().getLogger().warning("No scoreboard-library packet adapter available; scoreboards will be hidden.");
		}

		_teamManager = _scoreboardLibrary.createTeamManager();
	}

	@Override
	public void disable() {
		for (PlayerScoreboard playerScoreboard : _playerScoreboards.values()) {
			playerScoreboard.clear();
		}
		_playerScoreboards.clear();

		if (_teamManager != null && !_teamManager.closed()) {
			_teamManager.close();
		}
		_teamManager = null;

		if (_scoreboardLibrary != null && !_scoreboardLibrary.closed()) {
			_scoreboardLibrary.close();
		}
		_scoreboardLibrary = null;
	}

	public static ScoreboardManager getInstance() {
		return _instance;
	}

	public ScoreboardSidebar createSidebar() {
		if (_scoreboardLibrary == null || _scoreboardLibrary.closed()) {
			throw new IllegalStateException("scoreboard-library is not available");
		}

		return new ScoreboardSidebar(_scoreboardLibrary.createSidebar());
	}

	public TeamManager getTeamManager() {
		return _teamManager;
	}

	public void draw() {
		try {
			final Iterator<Player> playerIterator = _playerScoreboards.keySet().iterator();

			while (playerIterator.hasNext()) {
				final Player player = playerIterator.next();

				// Offline
				if (!player.isOnline()) {
					playerIterator.remove();
					continue;
				}

				_playerScoreboards.get(player).draw(this, player);
			}
		} catch (Exception e) {
			Bukkit.getLogger().severe("[SB-DEBUG] Exception in draw(): " + e.getMessage());
			e.printStackTrace();
		}
	}

	public CoreClientManager getClients() {
		return _clientManager;
	}

	public PlayerScoreboard getPlayerScoreboard(Player player) {
		return _playerScoreboards.get(player);
	}

	public ScoreboardData getData(String scoreboardName, boolean create) {
		if (!create)
			return _scoreboards.get(scoreboardName);

		if (!_scoreboards.containsKey(scoreboardName)) {
			_scoreboards.put(scoreboardName, new ScoreboardData());
		}

		return _scoreboards.get(scoreboardName);
	}

	public DonationManager getDonation() {
		return _donationManager;
	}

	@EventHandler
	public void playerJoin(PlayerJoinEvent event) {
		_playerScoreboards.put(event.getPlayer(), new PlayerScoreboard(this, event.getPlayer()));
		assignRankTeam(event.getPlayer());
	}

	@EventHandler
	public void playerQuit(PlayerQuitEvent event) {
		PlayerScoreboard ps = _playerScoreboards.remove(event.getPlayer());
		if (ps != null) {
			ps.clear();
		}
		unassignTeam(event.getPlayer());
		_currentPlayerTeams.remove(event.getPlayer());
		if (_teamManager != null && !_teamManager.closed()) {
			_teamManager.removePlayer(event.getPlayer());
		}
	}

	public void assignRankTeam(Player player) {
		assignPlayerTeam(player, null, Component.empty(), null, false);
	}

	public void assignGameTeam(Player player, String gameTeamName, Component gamePrefix, NamedTextColor playerColor) {
		assignPlayerTeam(player, gameTeamName, gamePrefix, playerColor, false);
	}

	public void assignSpectatorTeam(Player player) {
		assignPlayerTeam(player, "SPEC", Component.empty(), NamedTextColor.GRAY, true);
	}

	// Map to track the current team assigned to a player to prevent redundant remove/add packets
	private final HashMap<Player, String> _currentPlayerTeams = new HashMap<>();

	public void assignPlayerTeam(Player player, String gameTeamName, Component gamePrefix, NamedTextColor playerColor, boolean spectator) {
		if (player == null || _teamManager == null || _teamManager.closed()) {
			return;
		}

		Rank rank = resolveRank(player);
		String teamName = teamNameFor(player, rank, gameTeamName);

		// FIX: Prevent redundant team assignments that cause Remove/Add packet spam and crash 1.21 clients
		String currentTeam = _currentPlayerTeams.get(player);
		if (currentTeam != null && currentTeam.equals(teamName)) {
			// Player is already on this team, ensure they are tracked as a viewer
			_teamManager.addPlayer(player);
			return; 
		}

		// Remove from old team to prevent duplicate assignments
		unassignTeam(player);

		// Update tracking map
		_currentPlayerTeams.put(player, teamName);

		_teamManager.addPlayer(player);

		ScoreboardTeam team = _teamManager.createIfAbsent(teamName);
		TeamDisplay display = team.defaultDisplay();

		// Try playerHead texture tag first; fall back to plain text if unavailable
		Component rankPrefix;
		if (rank != Rank.ALL) {
			Component wideTag = com.houzicore.shared.core.chat.Chat.buildWideTagComponent(rank.name());
			if (wideTag != null) {
				rankPrefix = wideTag.color(net.kyori.adventure.text.format.NamedTextColor.WHITE).append(Component.space());
			} else {
				rankPrefix = com.houzicore.shared.core.chat.Chat.buildPlainRankPrefix(rank.name());
			}
		} else {
			rankPrefix = Component.empty();
		}
		Component prefix = rankPrefix.append(gamePrefix == null ? Component.empty() : gamePrefix);

		display.displayName(Component.text(teamName));
		display.prefix(prefix);
		display.suffix(clanSuffix(player));
		display.playerColor(playerColor != null ? playerColor : com.houzicore.shared.TablistFix.chatColorToAdventure(rank.GetColor()));
		display.nameTagVisibility(NameTagVisibility.ALWAYS);
		display.canSeeFriendlyInvisibles(spectator);
		display.addEntry(player.getName());
	}

	public String getCurrentTeamName(Player player) {
		return _currentPlayerTeams.get(player);
	}

	public void unassignTeam(Player player) {
		if (_teamManager == null || _teamManager.closed() || player == null) {
			return;
		}

		String currentTeamName = _currentPlayerTeams.get(player);
		if (currentTeamName != null) {
			ScoreboardTeam team = _teamManager.team(currentTeamName);
			if (team != null) {
				team.defaultDisplay().removeEntry(player.getName());
			}
		}
		_currentPlayerTeams.remove(player);
	}

	private Rank resolveRank(Player player) {
		Rank rank = Rank.ALL;
		if (_clientManager != null && _clientManager.Get(player) != null && _clientManager.Get(player).GetRank() != null) {
			rank = _clientManager.Get(player).GetRank();
		}

		if (!rank.Has(Rank.WARRIOR)
				&& _donationManager != null
				&& _donationManager.Get(player.getName()) != null
				&& _donationManager.Get(player.getName()).OwnsUltraPackage()) {
			rank = Rank.WARRIOR;
		}

		return rank;
	}

	private String teamNameFor(Player player, Rank rank, String gameTeamName) {
		String teamPart = gameTeamName == null || gameTeamName.isEmpty()
				? "R"
				: gameTeamName.toUpperCase(Locale.ROOT).replaceAll("[^A-Z0-9]", "");
		if (teamPart.length() > 4) {
			teamPart = teamPart.substring(0, 4);
		}

		String value = String.format("%02d%s%08x", rank.ordinal(), teamPart, Math.abs(player.getUniqueId().hashCode()));
		return value.substring(0, Math.min(16, value.length()));
	}

	private Component clanSuffix(Player player) {
		try {
			com.houzicore.shared.core.clan.ClanManager clanManager = com.houzicore.shared.core.clan.ClanManager.getInstance();
			if (clanManager == null) {
				return Component.empty();
			}

			com.houzicore.shared.core.clan.Clan clan = clanManager.getClan(player);
			if (clan == null) {
				return Component.empty();
			}

			return LEGACY_SERIALIZER.deserialize(" " + ChatColor.AQUA + "[" + clan.getName() + "]");
		} catch (Exception ignored) {
			return Component.empty();
		}
	}

	private String[] _animatedTitles = null;
	private int _titleTick = 0;

	@EventHandler
	public void updateTitle(UpdateEvent event) {
		if (event.getType() != UpdateType.FASTEST)
			return;

		if (Bukkit.getPluginManager().isPluginEnabled("Arcade") || Bukkit.getPluginManager().isPluginEnabled("HouziCoreArcade"))
			return;

		if (_animatedTitles == null) {
			_animatedTitles = new String[20];
			String text = "   " + com.houzicore.shared.core.common.BrandConfig.mainServerName().toUpperCase(java.util.Locale.ROOT) + "   ";
			for (int i = 0; i < _animatedTitles.length; i++) {
				StringBuilder sb = new StringBuilder("<bold>");
				for (int j = 0; j < text.length(); j++) {
					double center = (i / (double)_animatedTitles.length) * (text.length() + 8) - 4;
					double dist = Math.abs(j - center);
					if (dist < 1.5) sb.append("&#FFFFFF").append(text.charAt(j)); // white highlight
					else if (dist < 3.5) sb.append("&#FFFF55").append(text.charAt(j)); // yellow layer
					else sb.append("&#FFAA00").append(text.charAt(j)); // gold base
				}
				sb.append("</bold>");
				_animatedTitles[i] = com.houzicore.shared.common.util.HouziColorParser.parse(sb.toString());
			}
		}

		_titleTick = (_titleTick + 1) % _animatedTitles.length;
		String currentTitle = _animatedTitles[_titleTick];

		for (final PlayerScoreboard ps : _playerScoreboards.values()) {
			if ("default".equals(ps.getScoreboardDataName())) {
				ps.setTitle(currentTitle);
			}
		}
	}
}
