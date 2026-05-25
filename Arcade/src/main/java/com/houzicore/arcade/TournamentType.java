package com.houzicore.arcade;

import java.util.EnumSet;
import java.util.Set;

public enum TournamentType
{
	SUPER_SMASH_MOBS(GameType.Event),
	SURVIVAL_GAMES(GameType.SurvivalPrimalGame),
	MIXED_ARCADE(
			GameType.SneakyAssassins);

	private final Set<GameType> _gameTypes;

	TournamentType(GameType firstGameType, GameType... rest)
	{
		_gameTypes = EnumSet.of(firstGameType, rest);
	}

	public Set<GameType> getGameTypes()
	{
		return _gameTypes;
	}

	public static TournamentType getTournamentType(GameType gameType)
	{
		for (TournamentType type : values())
		{
			if (type.getGameTypes().contains(gameType))
				return type;
		}

		return null;
	}
}
