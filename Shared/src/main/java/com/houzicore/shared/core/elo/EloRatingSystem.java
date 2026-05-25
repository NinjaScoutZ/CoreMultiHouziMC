package com.houzicore.shared.core.elo;

public class EloRatingSystem {
	private final static int DEFAULT_KFACTOR = 25;

	public final static double WIN = 1.0;
	public final static double DRAW = 0.5;
	public final static double LOSS = 0.0;

	private KFactor[] _kFactors = {};

	public EloRatingSystem(KFactor... kFactors) {
		_kFactors = kFactors;
	}

	private int calculateNewRating(int oldRating, double score, double expectedScore, double kFactor) {
		return oldRating + (int) (kFactor * (score - expectedScore));
	}

	private double getExpectedScore(int rating, int opponentRating) {
		return 1.0 / (1.0 + Math.pow(10.0, (opponentRating - rating) / 400.0));
	}

	private double getKFactor(int rating) {
		for (final KFactor _kFactor : _kFactors) {
			if (rating >= _kFactor.getStartIndex() && rating <= _kFactor.getEndIndex())
				return _kFactor.value;
		}

		return DEFAULT_KFACTOR;
	}

	/**
	 * Get new rating.
	 *
	 * @param rating
	 *            Rating of either the current player or the average of the current
	 *            team.
	 * @param opponentRating
	 *            Rating of either the opponent player or the average of the
	 *            opponent team or teams.
	 * @param score
	 *            Score: 0=Loss 0.5=Draw 1.0=Win
	 * @return the new rating
	 */
	public int getNewRating(int rating, int opponentRating, double score) {
		final double kFactor = getKFactor(rating);
		final double expectedScore = getExpectedScore(rating, opponentRating);
		final int newRating = calculateNewRating(rating, score, expectedScore, kFactor);

		return newRating;
	}

	public int getNewRating(int rating, int opponentRating, GameResult result) {
		switch (result) {
		case Win:
			return getNewRating(rating, opponentRating, WIN);
		case Loss:
			return getNewRating(rating, opponentRating, LOSS);
		case Draw:
			return getNewRating(rating, opponentRating, DRAW);
		}

		return -1;
	}
}
