package com.houzicore.arcade.nautilus.game.arcade.scoreboard;

import java.util.ArrayList;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import com.houzicore.shared.core.scoreboard.ScoreboardSidebar;
import com.houzicore.shared.core.scoreboard.ScoreboardDataProvider;
import com.houzicore.shared.core.scoreboard.PlayerScoreboard;
import com.houzicore.shared.core.scoreboard.ScoreboardManager;
import com.houzicore.shared.core.scoreboard.DefaultScoreboardDataProvider;
import java.util.List;

import com.houzicore.shared.common.util.C;
import com.houzicore.arcade.GameType;
import com.houzicore.arcade.nautilus.game.arcade.game.Game;
import com.houzicore.arcade.nautilus.game.arcade.game.GameTeam;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.Scoreboard;

import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import com.houzicore.shared.updater.UpdateType;
import com.houzicore.shared.updater.event.UpdateEvent;

/**
 * In-game sidebar scoreboard renderer backed by scoreboard-library packet
 * sidebars.
 */
public class GameScoreboard implements Listener, ScoreboardDataProvider {
	private Game Game;

	private Component _cachedTitle = Component.empty();
	private List<Component> _cachedLines = new ArrayList<>();
	private Scoreboard _legacyObjectiveBoard;

	private static final LegacyComponentSerializer LEGACY_SERIALIZER = LegacyComponentSerializer.legacySection();

	private ArrayList<ScoreboardElement> _elements = new ArrayList<ScoreboardElement>();

	private String _title;
	private int _lastLineCount = 0;
	private int _animTick = 0;

	public GameScoreboard(Game game) {
		Game = game;
		_title = Game.GetName();

		com.houzicore.shared.core.scoreboard.ScoreboardManager scoreboards = com.houzicore.shared.core.scoreboard.ScoreboardManager
				.getInstance();
		if (scoreboards == null) {
			throw new IllegalStateException("ScoreboardManager must be initialized before GameScoreboard");
		}

		_cachedTitle = Component.text(Game.GetName());
		Game.Manager.getPluginManager().registerEvents(this, Game.Manager.getPlugin());
	}

	@Override
	public Component getTitle(Player player) {
		return _cachedTitle;
	}

	@Override
	public List<Component> getLines(Player player) {
		if (_cachedLines == null) return java.util.Collections.emptyList();
		List<Component> playerLines = new ArrayList<>();
		int kills = 0;
		int assists = 0;
		int beds = 0;
		if (Game.GetStats().containsKey(player)) {
			java.util.HashMap<String, Integer> pStats = Game.GetStats().get(player);
			String gameName = Game.GetName();
			if (pStats.containsKey(gameName + ".Kills")) kills = pStats.get(gameName + ".Kills");
			else if (pStats.containsKey("Global.Kills")) kills = pStats.get("Global.Kills");
			else if (pStats.containsKey("Kills")) kills = pStats.get("Kills");

			if (pStats.containsKey(gameName + ".Assists")) assists = pStats.get(gameName + ".Assists");
			else if (pStats.containsKey("Global.Assists")) assists = pStats.get("Global.Assists");
			else if (pStats.containsKey("Assists")) assists = pStats.get("Assists");

			if (pStats.containsKey(gameName + ".BrokeBeds")) beds = pStats.get(gameName + ".BrokeBeds");
			else if (pStats.containsKey("Global.BrokeBeds")) beds = pStats.get("Global.BrokeBeds");
			else if (pStats.containsKey("BrokeBeds")) beds = pStats.get("BrokeBeds");
		}
		
		for (Component line : _cachedLines) {
			String legacy = LEGACY_SERIALIZER.serialize(line);
			if (legacy.contains("%KILLS%") || legacy.contains("%ASSISTS%") || legacy.contains("%BEDS%")) {
				legacy = legacy.replace("%KILLS%", String.valueOf(kills))
							   .replace("%ASSISTS%", String.valueOf(assists))
							   .replace("%BEDS%", String.valueOf(beds));
				playerLines.add(LEGACY_SERIALIZER.deserialize(legacy));
			} else {
				playerLines.add(line);
			}
		}
		return playerLines;
	}

	@Deprecated
	public Scoreboard GetScoreboard() {
		if (_legacyObjectiveBoard == null) {
			_legacyObjectiveBoard = Bukkit.getScoreboardManager().getNewScoreboard();
		}
		return _legacyObjectiveBoard;
	}

	public void applyBoard(Player player) {
		applyBoard(player, false);
	}

	public void applyBoard(Player player, boolean forceReset) {
		PlayerScoreboard ps = ScoreboardManager.getInstance().getPlayerScoreboard(player);
		if (ps != null) {
			ps.setProvider(this);
			ps.draw(ScoreboardManager.getInstance(), player);
		}

		org.bukkit.scoreboard.Scoreboard mainBoard = Bukkit.getScoreboardManager().getMainScoreboard();
		if (player.getScoreboard() != mainBoard) {
			player.setScoreboard(mainBoard);
		}

		if (forceReset) {
			com.houzicore.shared.core.scoreboard.ScoreboardManager scoreboards = com.houzicore.shared.core.scoreboard.ScoreboardManager
					.getInstance();
			if (scoreboards != null) {
				scoreboards.unassignTeam(player);
			}
		}

		GameTeam team = Game.GetTeam(player);
		if (team != null) {
			SetPlayerTeam(player, team.GetName().toUpperCase());
		} else if (Game.Manager != null && Game.Manager.isSpectator(player)) {
			SetPlayerTeam(player, "SPEC");
		} else {
			com.houzicore.shared.core.scoreboard.ScoreboardManager scoreboards = com.houzicore.shared.core.scoreboard.ScoreboardManager
					.getInstance();
			if (scoreboards != null) {
				scoreboards.assignRankTeam(player);
			}
		}
	}

	public void removeBoard(Player player) {
		PlayerScoreboard ps = ScoreboardManager.getInstance().getPlayerScoreboard(player);
		if (ps != null && ps.getProvider() == this) {
			ps.setProvider(new DefaultScoreboardDataProvider(ScoreboardManager.getInstance(), ps));
			ps.draw(ScoreboardManager.getInstance(), player);
		}
	}

	public void close() {
		HandlerList.unregisterAll(this);
		for (Player player : Bukkit.getOnlinePlayers()) {
			PlayerScoreboard ps = ScoreboardManager.getInstance().getPlayerScoreboard(player);
			if (ps != null && ps.getProvider() == this) {
				ps.setProvider(new DefaultScoreboardDataProvider(ScoreboardManager.getInstance(), ps));
				ps.draw(ScoreboardManager.getInstance(), player);
			}
		}
		_lastLineCount = 0;
		_elements.clear();
	}

	public void assignRankTeamOnJoin(Player player) {
		com.houzicore.shared.core.scoreboard.ScoreboardManager scoreboards = com.houzicore.shared.core.scoreboard.ScoreboardManager
				.getInstance();
		if (scoreboards != null) {
			scoreboards.assignRankTeam(player);
		}
	}

	@EventHandler
	public void onUpdate(UpdateEvent event) {
		if (event.getType() == UpdateType.TICK) {
			_animTick++;
			UpdateTitle();
		}
	}

	public void setTitle(String title) {
		_title = title;
	}

	public void UpdateTitle() {
		UpdateTitle(true);
	}

	public void UpdateTitle(boolean triggerDraw) {
		if (_title == null) {
			return;
		}

		_cachedTitle = getAnimatedTitle(_title, Game.GetType(), _animTick);

		if (triggerDraw) {
			for (Player player : Bukkit.getOnlinePlayers()) {
				PlayerScoreboard ps = ScoreboardManager.getInstance().getPlayerScoreboard(player);
				if (ps != null && ps.getProvider() == this) {
					ps.draw(ScoreboardManager.getInstance(), player);
				}
			}
		}
	}

	public static class ScoreboardColors {
		public final java.awt.Color baseColor;
		public final java.awt.Color glowColor;
		public final java.awt.Color highlightColor;

		public ScoreboardColors(java.awt.Color base, java.awt.Color glow, java.awt.Color highlight) {
			this.baseColor = base;
			this.glowColor = glow;
			this.highlightColor = highlight;
		}
	}

	public static ScoreboardColors getColorsForGame(GameType type) {
		if (type == null) {
			return new ScoreboardColors(
				new java.awt.Color(255, 170, 0),   // Orange/Gold (FFAA00)
				new java.awt.Color(255, 255, 85),  // Yellow (FFFF55)
				new java.awt.Color(255, 255, 255)  // White (FFFFFF)
			);
		}
		switch (type) {
			case SurvivalPrimalGame:
			case SurvivalPrimalGameTeams:
				return new ScoreboardColors(
					new java.awt.Color(17, 153, 142),  // 11998E (Teal)
					new java.awt.Color(56, 239, 125),  // 38EF7D (Light Green)
					new java.awt.Color(255, 255, 255)
				);
			case Skywars:
			case SkywarsTeams:
				return new ScoreboardColors(
					new java.awt.Color(0, 114, 255),   // 0072FF (Blue)
					new java.awt.Color(0, 198, 255),   // 00C6FF (Cyan)
					new java.awt.Color(255, 255, 255)
				);
			case PropRush:
				return new ScoreboardColors(
					new java.awt.Color(0, 119, 182),    // Rich Cyan/Ocean Blue
					new java.awt.Color(0, 204, 255),    // Vibrant Sky Blue/Cyan
					new java.awt.Color(255, 255, 255)   // White
				);
			case Bedwars:
				return new ScoreboardColors(
					new java.awt.Color(255, 65, 108),  // FF416C (Pinkish Red)
					new java.awt.Color(255, 75, 43),   // FF4B2B (Orange Red)
					new java.awt.Color(253, 216, 53)   // FDD835 (Vibrant Gold)
				);
			case SpeedBuilders:
				return new ScoreboardColors(
					new java.awt.Color(138, 43, 226),   // BlueViolet (Deep Purple)
					new java.awt.Color(218, 112, 214),   // Orchid (Bright Purple-Pink)
					new java.awt.Color(255, 255, 255)   // White
				);
			default:
				return new ScoreboardColors(
					new java.awt.Color(255, 170, 0),   // Orange/Gold
					new java.awt.Color(255, 255, 85),  // Yellow
					new java.awt.Color(255, 255, 255)
				);
		}
	}

	public static Component getAnimatedTitle(String rawTitle, GameType gameType, int animTick) {
		if (rawTitle == null || rawTitle.isEmpty()) {
			return Component.empty();
		}
		
		String text = "   " + rawTitle.toUpperCase(java.util.Locale.ROOT) + "   ";
		int N = text.length();
		
		ScoreboardColors colors = getColorsForGame(gameType);
		
		int sweepTicks = 35;
		int pauseTicks = 30;
		int cycleTicks = sweepTicks + pauseTicks;
		int t = animTick % cycleTicks;
		
		double center;
		if (t < sweepTicks) {
			double progress = (double) t / sweepTicks;
			center = progress * (N + 8) - 4;
		} else {
			center = -100;
		}
		
		net.kyori.adventure.text.TextComponent.Builder builder = Component.text();
		
		for (int j = 0; j < N; j++) {
			char c = text.charAt(j);
			double dist = Math.abs(j - center);
			
			java.awt.Color interpolatedColor;
			if (dist <= 1.5) {
				double ratio = dist / 1.5;
				interpolatedColor = lerpColor(colors.highlightColor, colors.glowColor, ratio);
			} else if (dist <= 4.0) {
				double ratio = (dist - 1.5) / 2.5;
				interpolatedColor = lerpColor(colors.glowColor, colors.baseColor, ratio);
			} else {
				interpolatedColor = colors.baseColor;
			}
			
			builder.append(Component.text(String.valueOf(c))
				.color(TextColor.color(interpolatedColor.getRGB()))
				.decorate(TextDecoration.BOLD));
		}
		
		return builder.build();
	}
	
	private static java.awt.Color lerpColor(java.awt.Color c1, java.awt.Color c2, double ratio) {
		ratio = Math.max(0.0, Math.min(1.0, ratio));
		int r = (int) (c1.getRed() + (c2.getRed() - c1.getRed()) * ratio);
		int g = (int) (c1.getGreen() + (c2.getGreen() - c1.getGreen()) * ratio);
		int b = (int) (c1.getBlue() + (c2.getBlue() - c1.getBlue()) * ratio);
		return new java.awt.Color(r, g, b);
	}

	public String ParseTeamName(String name) {
		return name.substring(0, Math.min(16, name.length()));
	}

	public void CreateTeams() {
		for (Player player : com.houzicore.shared.common.util.UtilServer.getPlayers()) {
			GameTeam team = Game.GetTeam(player);
			if (team != null) {
				SetPlayerTeam(player, team.GetName().toUpperCase());
			} else {
				assignRankTeamOnJoin(player);
			}
		}
	}

	private GameTeam findGameTeam(String teamName) {
		if (teamName == null || teamName.isEmpty())
			return null;

		for (GameTeam team : Game.GetTeamList()) {
			if (team.GetName().equalsIgnoreCase(teamName))
				return team;
		}

		return null;
	}

	public void SetPlayerTeam(Player player, String teamName) {
		com.houzicore.shared.core.scoreboard.ScoreboardManager scoreboards = com.houzicore.shared.core.scoreboard.ScoreboardManager
				.getInstance();
		if (scoreboards == null)
			return;

		if (teamName == null)
			teamName = "";

		if (teamName.equalsIgnoreCase("SPEC")) {
			scoreboards.assignSpectatorTeam(player);
			return;
		}

		GameTeam gameTeam = findGameTeam(teamName);
		if (gameTeam == null) {
			if (teamName.isEmpty()) {
				scoreboards.assignRankTeam(player);
			} else {
				scoreboards.assignGameTeam(player, teamName, Component.empty(), null);
			}
			return;
		}

		Component prefix = Component.empty();
		if (gameTeam.GetDisplaytag()) {
			prefix = LEGACY_SERIALIZER
					.deserialize(gameTeam.GetColor() + C.Bold + gameTeam.GetName() + gameTeam.GetColor() + " ");
		}

		scoreboards.assignGameTeam(
				player,
				gameTeam.GetName().toUpperCase(),
				prefix,
				com.houzicore.shared.TablistFix.chatColorToAdventure(gameTeam.GetColor()));
	}

	public void ResetScore(String line) {
		if (_legacyObjectiveBoard != null) {
			_legacyObjectiveBoard.resetScores(line);
		}
	}

	public String Clean(String line) {
		if (line.length() > 64)
			line = line.substring(0, 64);

		return line;
	}

	public void Write(String line) {
		line = Clean(line);

		_elements.add(new ScoreboardElementText(line));
	}

	public void Write(Component component) {
		_elements.add(new ScoreboardElementComponent(component));
	}

	public void WriteOrdered(String key, String line, int value, boolean prependScore) {
		if (prependScore)
			line = value + " " + line;

		line = Clean(line);

		for (ScoreboardElement elem : _elements) {
			if (elem instanceof ScoreboardElementScores) {
				ScoreboardElementScores scores = (ScoreboardElementScores) elem;

				if (scores.IsKey(key)) {
					scores.AddScore(line, value);
					return;
				}
			}
		}

		_elements.add(new ScoreboardElementScores(key, line, value, true));
	}

	public void WriteBlank() {
		_elements.add(new ScoreboardElementText(" "));
	}

	private static final String SEPARATOR = ChatColor.DARK_GRAY + "" + ChatColor.STRIKETHROUGH
			+ "                                ";

	private boolean isPropRush() {
		return Game.GetType() == GameType.PropRush;
	}

	private Component buildPropRushIcon(GameTeam team) {
		if (team.GetColor() == ChatColor.AQUA) {
			return Component.text("■ ", NamedTextColor.AQUA);
		}

		if (team.GetColor() == ChatColor.RED) {
			return Component.text("☻ ", NamedTextColor.RED);
		}

		return Component.text("● ", NamedTextColor.GRAY);
	}

	private ScoreboardLine buildPropRushTeamHeaderLine(String line, GameTeam team) {
		return ScoreboardLine.component(Component.text()
				.append(Component.text(" ", NamedTextColor.DARK_GRAY))
				.append(buildPropRushIcon(team))
				.append(LEGACY_SERIALIZER.deserialize(line.trim()))
				.build());
	}

	private ScoreboardLine buildPropRushPlayerLine(String line, GameTeam team) {
		return ScoreboardLine.component(Component.text()
				.append(Component.text(" ", NamedTextColor.DARK_GRAY))
				.append(buildPropRushIcon(team))
				.append(LEGACY_SERIALIZER.deserialize(line.trim()))
				.build());
	}

	public void Draw() {
		UpdateTitle(false);

		String dateStr = java.time.LocalDate.now().format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yy"));
		String serverName = "Unknown";
		if (Game.Manager != null && Game.Manager.getPlugin() != null)
			serverName = Game.Manager.getPlugin().getConfig().getString("serverstatus.name", "MIN-1");

		ArrayList<ScoreboardLine> rawGameLines = new ArrayList<ScoreboardLine>();
		for (ScoreboardElement elem : _elements) {
			for (ScoreboardLine line : elem.GetLines()) {
				rawGameLines.add(line);
			}
		}

		ArrayList<ScoreboardLine> newLines = new ArrayList<ScoreboardLine>();

		newLines.add(ScoreboardLine.legacy(SEPARATOR));

		for (ScoreboardLine rawLine : rawGameLines) {
			if (rawLine.isComponent()) {
				newLines.add(rawLine);
				continue;
			}

			String line = rawLine.getLegacyText();
			String stripped = line.replaceAll("(?i)\\u00A7[0-9A-FK-ORX]", "").trim();
			boolean isBlank = stripped.isEmpty();

			if (line.contains(C.cYellow + C.Bold + "Time") || line.contains(C.cGreen + C.Bold + "Time")) {
				newLines.add(ScoreboardLine.legacy(" " + ChatColor.WHITE + "\u23f1 " + C.cYellow + C.Bold + "ᴛɪᴍᴇ"));
			} else if (line.contains("Time Left")) {
				newLines.add(
						ScoreboardLine.legacy(" " + ChatColor.WHITE + "\u23f1 " + C.cYellow + C.Bold + "ᴛɪᴍᴇ ʟᴇғᴛ"));
			} else if (line.contains("Bridges In")) {
				newLines.add(
						ScoreboardLine.legacy(" " + ChatColor.GOLD + "\u2693 " + C.cYellow + C.Bold + "ʙʀɪᴅɢᴇs ɪɴ"));
			} else if (line.contains("Borders")) {
				newLines.add(ScoreboardLine.legacy(" " + ChatColor.RED + "\u26a0 " + C.cYellow + C.Bold + "ʙᴏʀᴅᴇʀs"));
			} else if (line.contains("Players Alive")) {
				newLines.add(ScoreboardLine.legacy(" " + ChatColor.GREEN + "\u2764 " + C.cGreen + C.Bold + "ᴀʟɪᴠᴇ"));
			} else if (line.contains("Players Dead")) {
				newLines.add(ScoreboardLine.legacy(" " + ChatColor.GRAY + "\u2620 " + C.cGray + C.Bold + "ᴅᴇᴀᴅ"));
			} else if (isBlank) {
				newLines.add(ScoreboardLine.legacy(line));
			} else {
				boolean isTeamHeader = false;
				for (GameTeam gt : Game.GetTeamList()) {
					if (line.contains(gt.GetColor() + C.Bold + gt.GetName())) {
						if (isPropRush() && (gt.GetColor() == ChatColor.AQUA || gt.GetColor() == ChatColor.RED)) {
							newLines.add(buildPropRushTeamHeaderLine(line, gt));
						} else {
							newLines.add(ScoreboardLine.legacy(" " + gt.GetColor() + "\u258c " + line.trim()));
						}
						isTeamHeader = true;
						break;
					}
				}
				if (!isTeamHeader) {
					boolean isPlayerLine = false;
					for (GameTeam gt : Game.GetTeamList()) {
						String colorPrefix = gt.GetColor() + "";
						if (line.startsWith(colorPrefix) && !line.contains(C.Bold) && stripped.length() <= 16) {
							if (gt.GetColor() == ChatColor.GRAY || gt.GetColor() == ChatColor.DARK_GRAY) {
								newLines.add(
										ScoreboardLine.legacy(" " + ChatColor.DARK_GRAY + "\u2717 " + line.trim()));
							} else {
								if (isPropRush()
										&& (gt.GetColor() == ChatColor.AQUA || gt.GetColor() == ChatColor.RED)) {
									newLines.add(buildPropRushPlayerLine(line, gt));
								} else {
									newLines.add(ScoreboardLine.legacy(" " + gt.GetColor() + "\u25cf " + line.trim()));
								}
							}
							isPlayerLine = true;
							break;
						}
					}
					if (!isPlayerLine) {
						if (line.startsWith(C.cGreen) && !line.contains(C.Bold) && stripped.length() <= 16) {
							newLines.add(ScoreboardLine.legacy(" " + ChatColor.GREEN + "\u25cf " + line.trim()));
						} else if (line.startsWith(C.cGray) && !line.contains(C.Bold) && stripped.length() <= 16) {
							newLines.add(ScoreboardLine.legacy(" " + ChatColor.DARK_GRAY + "\u2717 " + line.trim()));
						} else {
							newLines.add(ScoreboardLine.legacy(" " + line));
						}
					}
				}
			}
		}

		newLines.add(ScoreboardLine.legacy(SEPARATOR));
		newLines.add(ScoreboardLine.legacy(" " + ChatColor.DARK_GRAY + dateStr + " \u2022 " + serverName));

		int lineCount = Math.min(newLines.size(), ScoreboardSidebar.MAX_LINES);

		List<Component> nextLines = new ArrayList<>();
		for (int i = 0; i < lineCount; i++) {
			ScoreboardLine line = newLines.get(i);
			Component component = line.isComponent()
					? line.getComponent()
					: LEGACY_SERIALIZER.deserialize(line.getLegacyText());
			nextLines.add(component);
		}
		_cachedLines = nextLines;
		_lastLineCount = lineCount;

		for (Player player : Bukkit.getOnlinePlayers()) {
			PlayerScoreboard ps = ScoreboardManager.getInstance().getPlayerScoreboard(player);
			if (ps != null && ps.getProvider() == this) {
				ps.draw(ScoreboardManager.getInstance(), player);
			}
		}
	}

	public void Reset() {
		_elements.clear();
	}
}
