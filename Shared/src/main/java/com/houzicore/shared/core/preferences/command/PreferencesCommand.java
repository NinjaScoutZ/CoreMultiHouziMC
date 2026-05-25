package com.houzicore.shared.core.preferences.command;

import org.bukkit.entity.Player;

import com.houzicore.shared.core.command.CommandBase;
import com.houzicore.shared.common.Rank;
import com.houzicore.shared.core.preferences.PreferencesManager;

public class PreferencesCommand extends CommandBase<PreferencesManager> {
	public PreferencesCommand(PreferencesManager plugin) {
		super(plugin, Rank.ALL, "prefs");
	}

	@Override
	public void Execute(Player caller, String[] args) {
		Plugin.openShop(caller);
	}
}
