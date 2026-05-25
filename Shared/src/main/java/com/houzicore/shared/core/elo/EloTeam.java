package com.houzicore.shared.core.elo;

import java.util.ArrayList;
import java.util.List;

public class EloTeam {
	private final List<EloPlayer> _players = new ArrayList<>();

	public int TotalElo = 0;

	public void addPlayer(EloPlayer player) {
		TotalElo += player.Rating;

		_players.add(player);
	}

	public List<EloPlayer> getPlayers() {
		return _players;
	}
}
