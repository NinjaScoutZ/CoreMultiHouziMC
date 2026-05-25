package com.houzicore.shared.core.quest;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.plugin.java.JavaPlugin;

import com.houzicore.shared.MiniDbClientPlugin;
import com.houzicore.shared.account.CoreClient;
import com.houzicore.shared.account.CoreClientManager;
import com.houzicore.shared.common.util.C;
import com.houzicore.shared.common.util.F;
import com.houzicore.shared.common.util.UtilPlayer;
import com.houzicore.shared.core.donation.DonationManager;
import com.houzicore.shared.core.stats.event.StatChangeEvent;

public class QuestManager extends MiniDbClientPlugin<QuestData> {

	private final QuestRepository _repository;
	private final DonationManager _donationManager;

	public QuestManager(JavaPlugin plugin, CoreClientManager clientManager, DonationManager donationManager) {
		super("Quest Manager", plugin, clientManager);
		_repository = new QuestRepository(plugin);
		_donationManager = donationManager;
	}

	@Override
	public void addCommands() {
		addCommand(new QuestCommand(this));
	}

	@Override
	protected QuestData AddPlayer(String player) {
		return new QuestData();
	}

	@Override
	public void processLoginResultSet(String playerName, int accountId, ResultSet resultSet) throws SQLException {
		Set(playerName, _repository.loadClientInformation(resultSet));
	}

	@Override
	public String getQuery(int accountId, String uuid, String name) {
		return "SELECT questId, periodId, progress, completed FROM player_quests WHERE accountId = " + accountId + ";";
	}

	public DonationManager getDonationManager() {
		return _donationManager;
	}

	@EventHandler
	public void onStatChange(StatChangeEvent event) {
		int diff = (int) (event.getValueAfter() - event.getValueBefore());
		if (diff <= 0) return;

		String stat = event.getStatName();

		if (stat.endsWith(".Kills")) {
			addProgress(event.getPlayerName(), Quest.DAILY_KILLS, diff);
			addProgress(event.getPlayerName(), Quest.WEEKLY_KILLS, diff);
			addProgress(event.getPlayerName(), Quest.WEEKLY_WARRIOR, diff);
		} else if (stat.endsWith(".Wins")) {
			addProgress(event.getPlayerName(), Quest.DAILY_WINS, diff);
			addProgress(event.getPlayerName(), Quest.WEEKLY_WINS, diff);
			addProgress(event.getPlayerName(), Quest.DAILY_GAME_MASTER, diff);
			addProgress(event.getPlayerName(), Quest.WEEKLY_CHAMPION, diff);
		} else if (stat.equals("Global.Time In Game")) {
			addProgress(event.getPlayerName(), Quest.DAILY_PLAYTIME, diff);
			addProgress(event.getPlayerName(), Quest.WEEKLY_PLAYTIME, diff);
		} else if (stat.endsWith(".Games Played") || stat.equals("Global.Games Played")) {
			addProgress(event.getPlayerName(), Quest.DAILY_EXPLORER, diff);
		} else if (stat.endsWith(".First Blood") || stat.contains("First Blood")) {
			addProgress(event.getPlayerName(), Quest.DAILY_FIRST_BLOOD, diff);
		} else if (stat.contains("Treasure") && stat.contains("Open")) {
			addProgress(event.getPlayerName(), Quest.WEEKLY_COLLECTOR, diff);
		} else if (stat.contains("Friend") || stat.contains("Social")) {
			addProgress(event.getPlayerName(), Quest.DAILY_SOCIAL, diff);
		} else if (stat.contains("Survival Games") && stat.contains("Top 5")) {
			addProgress(event.getPlayerName(), Quest.DAILY_SURVIVOR, diff);
		}
	}

	public void addProgress(String playerName, Quest quest, int amount) {
		QuestData data = Get(playerName);
		if (data == null) return;
		
		PlayerQuest pq = data.getOrCreateQuest(quest.getId());
		if (pq.isCompleted()) return;

		pq.addProgress(amount);
		
		if (pq.getProgress() >= quest.getMaxProgress()) {
			pq.setProgress(quest.getMaxProgress());
			pq.setCompleted(true);
			
			// Give Reward
			Player player = UtilPlayer.searchExact(playerName);
			if (player != null) {
				_donationManager.RewardEssenceLater("Quest: " + quest.getName(), player, quest.getRewardEssence());
				player.playSound(player.getLocation(), Sound.UI_TOAST_CHALLENGE_COMPLETE, 1f, 1f);
				UtilPlayer.message(player, F.main("Quest", com.houzicore.shared.core.lang.LangManager.get().get(player, "quest.completed").replace("{0}", quest.getName()).replace("{1}", String.valueOf(quest.getRewardEssence()))));
			}
		}
		
		saveQuest(playerName, pq);
	}

	public void saveQuest(final String playerName, final PlayerQuest quest) {
		final CoreClient client = getClientManager().Get(playerName);
		if (client == null || client.getAccountId() <= 0) return;

		Bukkit.getScheduler().runTaskAsynchronously(getPlugin(), new Runnable() {
			@Override
			public void run() {
				_repository.saveQuest(client.getAccountId(), quest);
			}
		});
	}

	public void openShop(Player player) {
		new QuestShop(this, getClientManager(), _donationManager, "Quests").attemptShopOpen(player);
	}
}
