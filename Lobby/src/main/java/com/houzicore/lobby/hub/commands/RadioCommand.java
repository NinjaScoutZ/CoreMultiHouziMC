package com.houzicore.lobby.hub.commands;

import org.bukkit.entity.Player;

import com.houzicore.shared.core.command.CommandBase;
import com.houzicore.shared.common.Rank;
import com.houzicore.shared.common.util.F;
import com.houzicore.shared.common.util.UtilPlayer;
import com.houzicore.lobby.hub.HubManager;
import com.houzicore.shared.core.music.RadioManager;
import com.houzicore.lobby.hub.ui.radio.RadioShop;

public class RadioCommand extends CommandBase<HubManager> {

	private final RadioManager _radioManager;
	private final RadioShop _radioShop;

	public RadioCommand(HubManager plugin, RadioManager radioManager, RadioShop radioShop) {
		super(plugin, Rank.ADMIN, "radio", "music", "nbs");
		_radioManager = radioManager;
		_radioShop = radioShop;
	}

	@Override
	public void Execute(Player caller, String[] args) {
		if (args == null || args.length == 0) {
			_radioShop.attemptShopOpen(caller);
			return;
		}

		String action = args[0].toLowerCase();
		if (action.equals("next")) {
			_radioManager.nextSong();
			UtilPlayer.message(caller, F.main("Radio", "Skipped to next song."));
		} else if (action.equals("prev")) {
			_radioManager.previousSong();
			UtilPlayer.message(caller, F.main("Radio", "Skipped to previous song."));
		} else if (action.equals("shuffle")) {
			_radioManager.toggleShuffle();
			UtilPlayer.message(caller, F.main("Radio", "Toggled shuffle mode."));
		} else if (action.equals("stop")) {
			_radioManager.stop();
			UtilPlayer.message(caller, F.main("Radio", "Stopped radio."));
		} else {
			UtilPlayer.message(caller, F.main("Radio", "Commands: next, prev, shuffle, stop"));
		}
	}
}
