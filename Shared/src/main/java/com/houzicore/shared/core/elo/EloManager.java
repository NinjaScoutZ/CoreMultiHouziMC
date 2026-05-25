package com.houzicore.shared.core.elo;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;

import com.houzicore.shared.MiniDbClientPlugin;
import com.houzicore.shared.account.CoreClientManager;
import com.houzicore.shared.common.util.NautHashMap;

import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

public class EloManager extends MiniDbClientPlugin<EloClientData> {
	private static Object _playerEloLock = new Object();

	private final EloRepository _repository;
	private final EloRatingSystem _ratingSystem;
	private final NautHashMap<String, NautHashMap<String, Integer>> _playerElos;

	public EloManager(JavaPlugin plugin, CoreClientManager clientManager) {
		super("Elo Rating", plugin, clientManager);

		_repository = new EloRepository(plugin);
		_ratingSystem = new EloRatingSystem(new KFactor(0, 1200, 25), new KFactor(1201, 1600, 20),
				new KFactor(1601, 2000, 15), new KFactor(2001, 2500, 10));
		_playerElos = new NautHashMap<>();
	}

	@Override
	protected EloClientData AddPlayer(String player) {
		return new EloClientData();
	}

	public int getElo(UUID uuid, String gameType) {
		int elo = 1000;

		synchronized (_playerEloLock) {
			if (_playerElos.containsKey(uuid.toString())) {
				if (_playerElos.get(uuid.toString()).containsKey(gameType)) {
					elo = _playerElos.get(uuid.toString()).get(gameType);
				}
			}
		}

		return elo;
	}

	public EloTeam getNewRatings(EloTeam teamA, EloTeam teamB, GameResult result) {
		final EloTeam newTeam = new EloTeam();


		final int newTotal = _ratingSystem.getNewRating(teamA.TotalElo / teamA.getPlayers().size(),
				teamB.TotalElo / teamB.getPlayers().size(), result) * teamA.getPlayers().size();


		for (final EloPlayer player : teamA.getPlayers()) {
			final EloPlayer newPlayer = new EloPlayer();
			newPlayer.UniqueId = player.UniqueId;
			newPlayer.Rating = (int) (player.Rating
					+ (double) player.Rating / (double) teamA.TotalElo * (newTotal - teamA.TotalElo));

			player.printInfo();

			newPlayer.printInfo();

			newTeam.addPlayer(newPlayer);
		}

		return newTeam;
	}

	@Override
	public String getQuery(int accountId, String uuid, String name) {
		return "SELECT gameType, elo FROM eloRating WHERE uuid = '" + uuid + "';";
	}

	@Override
	public void processLoginResultSet(String playerName, int accountId, ResultSet resultSet) throws SQLException {
		Set(playerName, _repository.loadClientInformation(resultSet));
	}

	public void saveElo(final String uuid, final String gameType, final int elo) {
		Bukkit.getServer().getScheduler().runTaskAsynchronously(getPlugin(), new Runnable() {
			@Override
			public void run() {
				_repository.saveElo(uuid, gameType, elo);

				synchronized (_playerEloLock) {
					if (_playerElos.containsKey(uuid)) {
						if (_playerElos.get(uuid).containsKey(gameType)) {
							_playerElos.get(uuid).put(gameType, elo);
						}
					}
				}
			}
		});
	}

	public void saveElo(UUID uuid, String gameType, int elo) {
		saveElo(uuid.toString(), gameType, elo);
	}
}
