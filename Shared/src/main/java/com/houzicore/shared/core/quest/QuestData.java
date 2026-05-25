package com.houzicore.shared.core.quest;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class QuestData {
	private Map<Integer, PlayerQuest> _quests = new HashMap<>();

	public QuestData() {}

	public void addQuest(PlayerQuest quest) {
		_quests.put(quest.getQuestId(), quest);
	}

	public PlayerQuest getQuest(int questId) {
		return _quests.get(questId);
	}
	
	public PlayerQuest getOrCreateQuest(int questId) {
		PlayerQuest pQuest = _quests.get(questId);
		if (pQuest == null) {
			Quest q = Quest.getById(questId);
			if (q != null) {
				pQuest = new PlayerQuest(questId, q.getType().getCurrentPeriodId(), 0, false);
				_quests.put(questId, pQuest);
			}
		} else {
			// Check expiration
			Quest q = pQuest.getQuest();
			if (q != null) {
				long currentPeriod = q.getType().getCurrentPeriodId();
				if (pQuest.getPeriodId() != currentPeriod) {
					pQuest.setPeriodId(currentPeriod);
					pQuest.setProgress(0);
					pQuest.setCompleted(false);
				}
			}
		}
		return pQuest;
	}

	public Collection<PlayerQuest> getAllQuests() {
		return _quests.values();
	}
}
