package com.houzicore.arcade.nautilus.game.arcade.game.games.arena;

public enum RoundState {
	WAITING("Waiting"),
	STARTING_5("Starting in 5s"),
	STARTING_4("Starting in 4s"),
	STARTING_3("Starting in 3s"),
	STARTING_2("Starting in 2s"),
	STARTING_1("Starting in 1s"),
	STARTED("FIGHT!"),
	FIGHTING("Fighting");

	private final String scoreboardText;

	RoundState(String scoreboardText) {
		this.scoreboardText = scoreboardText;
	}

	public String getScoreboardText() {
		return scoreboardText;
	}
}
