package com.houzicore.arcade.legacy;

import com.houzicore.arcade.GameType;

import java.util.Collections;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

public final class LegacyGameTypeResolver
{
	private static final Map<String, GameType> ALIASES;

	static
	{
		Map<String, GameType> aliases = new HashMap<>();

		alias(aliases, GameType.PropRush,
				"HideSeek",
				"Hide Seek",
				"Block Hunt",
				"BlockHunt",
				"Prop Rush");

		alias(aliases, GameType.SurvivalPrimalGame,
				"Survival Games",
				"SurvivalGames",
				"Primal Games",
				"Survival Primal Games",
				"SG");

		alias(aliases, GameType.SurvivalPrimalGameTeams,
				"Survival Games Teams",
				"Team Survival Games",
				"Survival Primal Games Teams",
				"SG Teams");

		alias(aliases, GameType.MineWare,
				"Old MineWare",
				"Micro Games");

		alias(aliases, GameType.Lobbers,
				"Bomb Lobbers");

		alias(aliases, GameType.SearchAndDestroy,
				"Search and Destroy",
				"SearchAndDestroy");

		alias(aliases, GameType.HoleInTheWall,
				"Hole in the Wall",
				"HoleInTheWall");

		ALIASES = Collections.unmodifiableMap(aliases);
	}

	private LegacyGameTypeResolver()
	{
	}

	public static Optional<GameType> resolve(String rawName)
	{
		if (rawName == null)
			return Optional.empty();

		String trimmed = rawName.trim();
		if (trimmed.isEmpty())
			return Optional.empty();

		for (GameType type : GameType.values())
		{
			if (type.name().equalsIgnoreCase(trimmed) || type.GetName().equalsIgnoreCase(trimmed))
				return Optional.of(type);
		}

		return Optional.ofNullable(ALIASES.get(normalize(trimmed)));
	}

	public static Optional<GameType> resolvePlayable(String rawName)
	{
		return resolve(rawName).filter(type -> type.getGameClass() != null);
	}

	public static boolean isPlayable(GameType type)
	{
		return type != null && type.getGameClass() != null;
	}

	private static void alias(Map<String, GameType> aliases, GameType type, String... names)
	{
		for (String name : names)
			aliases.put(normalize(name), type);
	}

	private static String normalize(String name)
	{
		return name.toLowerCase(Locale.ROOT).replaceAll("[^a-z0-9]", "");
	}
}
