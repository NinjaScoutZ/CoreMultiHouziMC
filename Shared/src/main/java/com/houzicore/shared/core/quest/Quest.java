package com.houzicore.shared.core.quest;

public enum Quest {
	// Daily Quests
	DAILY_KILLS(1, QuestType.DAILY, "Daily Kills", "Get 15 Kills in any mode", 15, 200),
	DAILY_WINS(2, QuestType.DAILY, "Daily Victor", "Win 3 Games", 3, 300),
	DAILY_PLAYTIME(3, QuestType.DAILY, "Daily Dedication", "Play games for 30 minutes", 30, 150),
	DAILY_FIRST_BLOOD(4, QuestType.DAILY, "First Blood", "Get First Blood in 1 game", 1, 500),
	DAILY_GAME_MASTER(5, QuestType.DAILY, "Game Master", "Win 1 game", 1, 300),
	DAILY_SOCIAL(6, QuestType.DAILY, "Social Butterfly", "Send 5 friend requests", 5, 200),
	DAILY_EXPLORER(7, QuestType.DAILY, "Explorer", "Join 3 different game modes", 3, 400),
	DAILY_SURVIVOR(8, QuestType.DAILY, "Survivor", "Survive to top 5 in SG", 1, 350),

	// Weekly Quests
	WEEKLY_KILLS(11, QuestType.WEEKLY, "Weekly Bloodthirst", "Get 100 Kills", 100, 1500),
	WEEKLY_WINS(12, QuestType.WEEKLY, "Weekly Champion", "Win 20 Games", 20, 3000),
	WEEKLY_PLAYTIME(13, QuestType.WEEKLY, "Weekly Devotion", "Play games for 180 minutes", 180, 2000),
	WEEKLY_CHAMPION(14, QuestType.WEEKLY, "Champion", "Win 10 games", 10, 2000),
	WEEKLY_WARRIOR(15, QuestType.WEEKLY, "Warrior", "Get 50 kills", 50, 1500),
	WEEKLY_COLLECTOR(16, QuestType.WEEKLY, "Collector", "Open 10 treasure chests", 10, 1000);

	private final int _id;
	private final QuestType _type;
	private final String _name;
	private final String _description;
	private final int _maxProgress;
	private final int _rewardEssence;

	private Quest(int id, QuestType type, String name, String description, int maxProgress, int rewardEssence) {
		_id = id;
		_type = type;
		_name = name;
		_description = description;
		_maxProgress = maxProgress;
		_rewardEssence = rewardEssence;
	}

	public int getId() { return _id; }
	public QuestType getType() { return _type; }
	public String getName() { return _name; }
	public String getDescription() { return _description; }
	public int getMaxProgress() { return _maxProgress; }
	public int getRewardEssence() { return _rewardEssence; }

	public static Quest getById(int id) {
		for (Quest q : values()) {
			if (q.getId() == id) return q;
		}
		return null;
	}
}
