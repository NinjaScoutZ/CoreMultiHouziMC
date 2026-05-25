package com.houzicore.shared.core.punish;

import java.util.ArrayList;
import java.util.List;

import com.houzicore.shared.common.util.NautHashMap;

public class PunishClient {
	private final NautHashMap<Category, List<Punishment>> _punishments;

	public PunishClient() {
		_punishments = new NautHashMap<>();
	}

	public void AddPunishment(Category category, Punishment punishment) {
		if (!_punishments.containsKey(category)) {
			_punishments.put(category, new ArrayList<Punishment>());
		}

		_punishments.get(category).add(punishment);
	}

	public Punishment GetPunishment(PunishmentSentence sentence) {
		for (final List<Punishment> punishments : _punishments.values()) {
			for (final Punishment punishment : punishments) {
				if (sentence == PunishmentSentence.Ban && punishment.IsBanned())
					return punishment;
				else if (sentence == PunishmentSentence.Mute && punishment.IsMuted())
					return punishment;
			}
		}

		return null;
	}

	public NautHashMap<Category, List<Punishment>> GetPunishments() {
		return _punishments;
	}

	public boolean IsBanned() {
		for (final List<Punishment> punishments : _punishments.values()) {
			for (final Punishment punishment : punishments) {
				if (punishment.IsBanned())
					return true;
			}
		}

		return false;
	}

	public boolean IsMuted() {
		for (final List<Punishment> punishments : _punishments.values()) {
			for (final Punishment punishment : punishments) {
				if (punishment.IsMuted())
					return true;
			}
		}

		return false;
	}
}
