package com.houzicore.shared.core.energy;

import java.util.HashMap;

public class ClientEnergy {
	public double Energy;
	public long LastEnergy;

	public HashMap<String, Integer> MaxEnergyMods = new HashMap<>();
	public HashMap<String, Integer> SwingEnergyMods = new HashMap<>();

	public int EnergyBonus() {
		int bonus = 0;

		for (final int i : MaxEnergyMods.values()) {
			bonus += i;
		}

		return bonus;
	}

	public int SwingEnergy() {
		int mod = 0;

		for (final int i : SwingEnergyMods.values()) {
			mod += i;
		}

		return Math.max(0, 4 + mod);
	}
}
