package com.houzicore.shared.core.achievement;

import java.util.List;

import org.bukkit.Material;
import org.bukkit.entity.Player;

import com.houzicore.shared.account.CoreClientManager;
import com.houzicore.shared.common.Rank;
import com.houzicore.shared.common.util.C;
import com.houzicore.shared.common.util.UtilTime;
import com.houzicore.shared.core.stats.PlayerStats;
import com.houzicore.shared.core.stats.StatsManager;

public enum AchievementCategory {
	GLOBAL("Global", null, new StatDisplay[] { StatDisplay.GEMS_EARNED, null,
			new StatDisplay("Games Played", "GamesPlayed"), StatDisplay.TIME_IN_GAME }, Material.EMERALD, 0,
			GameCategory.GLOBAL, "None"),

	BRIDGES("The Bridges", null, new StatDisplay[] { StatDisplay.WINS, StatDisplay.GAMES_PLAYED, StatDisplay.KILLS,
			StatDisplay.DEATHS, StatDisplay.GEMS_EARNED }, Material.IRON_PICKAXE, 0, GameCategory.SURVIVAL,
			"Destructor Kit"),

	SURVIVAL_GAMES(
			"Survival Primal Game", new String[] { "Survival Primal Game" }, new StatDisplay[] { StatDisplay.WINS, StatDisplay.GAMES_PLAYED, StatDisplay.KILLS,
					StatDisplay.DEATHS, StatDisplay.GEMS_EARNED },
			Material.DIAMOND_SWORD, 0, GameCategory.SURVIVAL, "Horseman Kit"),

	SKYWARS("Skywars", null, new StatDisplay[] { StatDisplay.WINS, StatDisplay.GAMES_PLAYED, StatDisplay.KILLS,
			StatDisplay.DEATHS, StatDisplay.GEMS_EARNED }, Material.FEATHER, 5, GameCategory.SURVIVAL,
			"Destructor Kit"),

	UHC("Ultra Hardcore", null, new StatDisplay[] { StatDisplay.WINS, StatDisplay.GAMES_PLAYED, StatDisplay.KILLS,
			StatDisplay.DEATHS, StatDisplay.GEMS_EARNED }, Material.GOLDEN_APPLE, 0, GameCategory.SURVIVAL, "None"),

	WIZARDS("Wizards", null, new StatDisplay[] { StatDisplay.WINS, StatDisplay.GAMES_PLAYED, StatDisplay.KILLS,
			StatDisplay.DEATHS, StatDisplay.GEMS_EARNED }, Material.BLAZE_ROD, 0, GameCategory.SURVIVAL,
			"Witch Doctor Kit"),

	CASTLE_SIEGE("Castle Siege", null,
			new StatDisplay[] { StatDisplay.WINS, StatDisplay.GAMES_PLAYED, new StatDisplay("Kills as Defenders"),
					new StatDisplay("Deaths as Defenders"), new StatDisplay("Kills as Undead"),
					new StatDisplay("Deaths as Undead"), StatDisplay.GEMS_EARNED },
			Material.DIAMOND_CHESTPLATE, 0, GameCategory.CLASSICS, null),

	BLOCK_HUNT(
			"Prop Rush", new String[] { "Prop Rush" }, new StatDisplay[] { StatDisplay.WINS, StatDisplay.GAMES_PLAYED, StatDisplay.KILLS,
					StatDisplay.DEATHS, StatDisplay.GEMS_EARNED },
			Material.GRASS_BLOCK, 0, GameCategory.CLASSICS, "Infestor Kit"),

	SMASH_MOBS(
			"Super Smash Mobs", null, new StatDisplay[] { StatDisplay.WINS, StatDisplay.GAMES_PLAYED, StatDisplay.KILLS,
					StatDisplay.DEATHS, StatDisplay.GEMS_EARNED },
			Material.PLAYER_HEAD, 4, GameCategory.CLASSICS, "Sheep Kit"),

	MINE_STRIKE("MineStrike", null, new StatDisplay[] { StatDisplay.WINS, StatDisplay.GAMES_PLAYED, StatDisplay.KILLS,
			StatDisplay.DEATHS, StatDisplay.GEMS_EARNED }, Material.TNT, 0, GameCategory.CLASSICS, "None"),

	DRAW_MY_THING("Draw My Thing", null,
			new StatDisplay[] { StatDisplay.WINS, StatDisplay.GAMES_PLAYED, StatDisplay.GEMS_EARNED },
			Material.WRITABLE_BOOK, 0, GameCategory.CLASSICS, "Extra Tools Kit"),

	CHAMPIONS(
			"Champions", new String[] { "Champions Domination", "Champions TDM" }, new StatDisplay[] { StatDisplay.WINS,
					StatDisplay.GAMES_PLAYED, StatDisplay.KILLS, StatDisplay.DEATHS, StatDisplay.GEMS_EARNED },
			Material.BEACON, 0, GameCategory.CHAMPIONS, "Extra Class Skills"),

	MASTER_BUILDERS("Master Builders", null,
			new StatDisplay[] { StatDisplay.WINS, StatDisplay.GAMES_PLAYED, StatDisplay.GEMS_EARNED }, Material.OAK_PLANKS, 0,
			GameCategory.CLASSICS, "None"),

	// Arcade
	DRAGONS("Dragons", null, new StatDisplay[] { StatDisplay.WINS, StatDisplay.GAMES_PLAYED, StatDisplay.GEMS_EARNED },
			Material.END_STONE, 0, GameCategory.ARCADE, null),

	DRAGON_ESCAPE("Dragon Escape", null,
			new StatDisplay[] { StatDisplay.WINS, StatDisplay.GAMES_PLAYED, StatDisplay.GEMS_EARNED },
			Material.DRAGON_EGG, 0, GameCategory.ARCADE, "Digger Kit"),

	SHEEP_QUEST("Sheep Quest", null, new StatDisplay[] { StatDisplay.WINS, StatDisplay.GAMES_PLAYED, StatDisplay.KILLS,
			StatDisplay.DEATHS, StatDisplay.GEMS_EARNED }, Material.WHITE_WOOL, 0, GameCategory.ARCADE, null),

	SNEAKY_ASSASSINS(
			"Sneaky Assassins", null, new StatDisplay[] { StatDisplay.WINS, StatDisplay.GAMES_PLAYED, StatDisplay.KILLS,
					StatDisplay.DEATHS, StatDisplay.GEMS_EARNED },
			Material.INK_SAC, 0, GameCategory.ARCADE, "Briber Kit"),

	ONE_IN_THE_QUIVER(
			"One in the Quiver", null, new StatDisplay[] { StatDisplay.WINS, StatDisplay.GAMES_PLAYED,
					StatDisplay.KILLS, StatDisplay.DEATHS, StatDisplay.GEMS_EARNED },
			Material.BOW, 0, GameCategory.ARCADE, "Slam Shooter Kit"),

	SUPER_PAINTBALL("Super Paintball", null, new StatDisplay[] { StatDisplay.WINS, StatDisplay.GAMES_PLAYED,
			StatDisplay.KILLS, StatDisplay.DEATHS, StatDisplay.GEMS_EARNED }, Material.ENDER_PEARL, 0,
			GameCategory.ARCADE, null),

	TURF_WARS("Turf Wars", null, new StatDisplay[] { StatDisplay.WINS, StatDisplay.GAMES_PLAYED, StatDisplay.KILLS,
			StatDisplay.DEATHS, StatDisplay.GEMS_EARNED }, Material.TERRACOTTA, 14, GameCategory.ARCADE, null),

	RUNNER("Runner", null, new StatDisplay[] { StatDisplay.WINS, StatDisplay.GAMES_PLAYED, StatDisplay.DEATHS,
			StatDisplay.GEMS_EARNED }, Material.LEATHER_BOOTS, 0, GameCategory.ARCADE, null),

	SPLEEF("Super Spleef", null, new StatDisplay[] { StatDisplay.WINS, StatDisplay.GAMES_PLAYED, StatDisplay.DEATHS,
			StatDisplay.GEMS_EARNED }, Material.IRON_SHOVEL, 0, GameCategory.ARCADE, null),

	DEATH_TAG("Death Tag", null, new StatDisplay[] { StatDisplay.WINS, StatDisplay.GAMES_PLAYED, StatDisplay.KILLS,
			StatDisplay.DEATHS, StatDisplay.GEMS_EARNED }, Material.PLAYER_HEAD, 0, GameCategory.ARCADE, null),

	SNAKE("Snake", null, new StatDisplay[] { StatDisplay.WINS, StatDisplay.GAMES_PLAYED, StatDisplay.KILLS,
			StatDisplay.DEATHS, StatDisplay.GEMS_EARNED }, Material.WHITE_WOOL, 4, GameCategory.ARCADE, "Reversal Snake Kit"),

	BACON_BRAWL("Bacon Brawl", null, new StatDisplay[] { StatDisplay.WINS, StatDisplay.GAMES_PLAYED, StatDisplay.KILLS,
			StatDisplay.DEATHS, StatDisplay.GEMS_EARNED }, Material.PORKCHOP, 0, GameCategory.ARCADE, null),

	MICRO_BATTLE("Micro Battle", null, new StatDisplay[] { StatDisplay.WINS, StatDisplay.GAMES_PLAYED,
			StatDisplay.KILLS, StatDisplay.DEATHS, StatDisplay.GEMS_EARNED }, Material.LAVA, 0, GameCategory.ARCADE,
			null),

	BOMB_LOBBERS(
			"Bomb Lobbers", null, new StatDisplay[] { StatDisplay.WINS, StatDisplay.GAMES_PLAYED, StatDisplay.KILLS,
					StatDisplay.DEATHS, StatDisplay.GEMS_EARNED },
			Material.FIRE_CHARGE, 0, GameCategory.ARCADE, "Waller Kit");

	public static enum GameCategory {
		GLOBAL, SURVIVAL, CLASSICS, CHAMPIONS, ARCADE;
	}
	private String _name;
	private String[] _statsToPull;
	private StatDisplay[] _statDisplays;
	private Material _icon;
	private GameCategory _gameCategory;
	private byte _iconData;

	private String _kitReward;

	AchievementCategory(String name, String[] statsToPull, StatDisplay[] statDisplays, Material icon, int iconData,
			GameCategory gameCategory, String kitReward) {
		_name = name;

		if (statsToPull != null) {
			_statsToPull = statsToPull;
		} else {
			_statsToPull = new String[] { name };
		}
		_statDisplays = statDisplays;
		_icon = icon;
		_iconData = (byte) iconData;
		_gameCategory = gameCategory;
		_kitReward = kitReward;
	}

	public void addStats(CoreClientManager clientManager, StatsManager statsManager, List<String> lore, int max,
			Player player, Player target) {
		final PlayerStats stats = statsManager.Get(target);
		for (int i = 0; i < _statDisplays.length && i < max; i++) {
			// If the stat is null then just display a blank line instead
			if (_statDisplays[i] == null) {
				lore.add(" ");
				continue;
			}

			final String displayName = _statDisplays[i].getDisplayName();

			// Skip showing Losses, Kills, Deaths for other players
			if (!clientManager.Get(player).GetRank().Has(Rank.MODERATOR) && !player.equals(target)
					&& (displayName.contains("Losses") || displayName.contains("Kills")
							|| displayName.contains("Deaths") || displayName.equals("Time In Game")
							|| displayName.equals("Games Played"))) {
				continue;
			}

			int statNumber = 0;
			for (final String statToPull : _statsToPull) {
				for (final String statName : _statDisplays[i].getStats()) {
					statNumber += stats.getStat(statToPull + "." + statName);
				}
			}

			String statString = C.cWhite + statNumber;
			// Need to display special for time
			if (displayName.equalsIgnoreCase("Time In Game")) {
				statString = C.cWhite + UtilTime.convertString(statNumber * 1000L, 0, UtilTime.TimeUnit.FIT);
			}

			lore.add(C.cYellow + displayName + ": " + statString);
		}
	}

	public void addStats(CoreClientManager clientManager, StatsManager statsManager, List<String> lore, Player player,
			Player target) {
		addStats(clientManager, statsManager, lore, Integer.MAX_VALUE, player, target);
	}

	public String getFriendlyName() {
		return _name;
	}

	public GameCategory getGameCategory() {
		return _gameCategory;
	}

	public Material getIcon() {
		return _icon;
	}

	public byte getIconData() {
		return _iconData;
	}

	public String getReward() {
		return _kitReward;
	}

	public StatDisplay[] getStatsToDisplay() {
		return _statDisplays;
	}

	public String[] getStatsToPull() {
		return _statsToPull;
	}
}
