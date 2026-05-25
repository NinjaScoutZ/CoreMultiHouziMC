package com.houzicore.shared.core.battlepass;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.plugin.java.JavaPlugin;

import com.houzicore.shared.MiniDbClientPlugin;
import com.houzicore.shared.account.CoreClient;
import com.houzicore.shared.account.CoreClientManager;
import com.houzicore.shared.common.util.F;
import com.houzicore.shared.common.util.UtilPlayer;
import com.houzicore.shared.common.util.UtilServer;
import com.houzicore.shared.core.donation.DonationManager;
import com.houzicore.shared.core.stats.event.StatChangeEvent;

public class BattlePassManager extends MiniDbClientPlugin<BattlePassData> {

	private final BattlePassRepository _repository;
	private final DonationManager _donationManager;
	private String _currentSeason = "Season_1";

	public BattlePassManager(JavaPlugin plugin, CoreClientManager clientManager, DonationManager donationManager) {
		super("Battle Pass", plugin, clientManager);
		_repository = new BattlePassRepository(plugin);
		_donationManager = donationManager;
	}

	@Override
	public void addCommands() {
		addCommand(new BattlePassCommand(this));
	}

	@Override
	protected BattlePassData AddPlayer(String player) {
		return new BattlePassData();
	}

	@Override
	public void processLoginResultSet(String playerName, int accountId, ResultSet resultSet) throws SQLException {
		Set(playerName, _repository.loadClientInformation(resultSet));
	}

	@Override
	public String getQuery(int accountId, String uuid, String name) {
		return "SELECT xp, claimedTiers FROM battlepass_progress WHERE accountId = " + accountId + " AND season = '" + _currentSeason + "';";
	}
	
	public String getCurrentSeason() {
		return _currentSeason;
	}

	public void setCurrentSeason(String season) {
		_currentSeason = season;
	}
	
	public DonationManager getDonationManager() {
		return _donationManager;
	}

	@EventHandler
	public void onStatChange(StatChangeEvent event) {
		int diff = (int) (event.getValueAfter() - event.getValueBefore());
		if (diff <= 0) return;

		String stat = event.getStatName();
		int xpGained = 0;

		if (stat.endsWith(".Kills")) {
			xpGained = diff * 15;
		} else if (stat.endsWith(".Wins")) {
			xpGained = diff * 100;
		} else if (stat.startsWith("Achievement.")) {
			xpGained = diff * 50;
		}

		if (xpGained > 0) {
			addXp(event.getPlayerName(), xpGained);
		}
	}

	public void addXp(final String playerName, final int amount) {
		final Player player = UtilPlayer.searchExact(playerName);
		final BattlePassData data = Get(playerName);
		
		if (data != null) {
			data.addXp(amount);
			
			// Show action bar progress occasionally or save async
			saveData(playerName, data);
		}
	}

	public void saveData(final String playerName, final BattlePassData data) {
		final CoreClient client = getClientManager().Get(playerName);
		if (client == null || client.getAccountId() <= 0) return;

		Bukkit.getScheduler().runTaskAsynchronously(getPlugin(), new Runnable() {
			@Override
			public void run() {
				_repository.saveClientInformation(client.getAccountId(), _currentSeason, data);
			}
		});
	}

	public void openShop(Player player) {
		new BattlePassShop(this, getClientManager(), _donationManager, "Battle Pass").attemptShopOpen(player);
	}
}
