package com.houzicore.shared.core.notifier;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.plugin.java.JavaPlugin;

import com.houzicore.shared.MiniPlugin;
import com.houzicore.shared.account.CoreClientManager;
import com.houzicore.shared.common.Rank;
import com.houzicore.shared.common.util.C;
import com.houzicore.shared.common.util.F;
import com.houzicore.shared.common.util.UtilPlayer;
import com.houzicore.shared.common.util.UtilServer;
import com.houzicore.shared.updater.event.UpdateEvent;

public class NotificationManager extends MiniPlugin {
	private final boolean _enabled = true;

	private final CoreClientManager _clientManager;

	private final String _summerLine = C.cYellow + "█" + C.cGold + "█" + C.cYellow + "█" + C.cGold + "█" + C.cYellow
			+ "█" + C.cGold + "█" + C.cYellow + "█" + C.cGold + "█" + C.cYellow + "█" + C.cGold + "█" + C.cYellow + "█"
			+ C.cGold + "█" + C.cYellow + "█" + C.cGold + "█" + C.cYellow + "█" + C.cGold + "█" + C.cYellow + "█"
			+ C.cGold + "█" + C.cYellow + "█" + C.cGold + "█" + C.cYellow + "█" + C.cGold + "█" + C.cYellow + "█"
			+ C.cGold + "█" + C.cYellow + "█" + C.cGold + "█" + C.cYellow + "█" + C.cGold + "█" + C.cYellow + "█"
			+ C.cGold + "█" + C.cYellow + "█" + C.cGold + "█" + C.cYellow + "█" + C.cGold + "█" + C.cYellow + "█";

	public NotificationManager(JavaPlugin plugin, CoreClientManager client) {
		super("Notification Manager", plugin);

		_clientManager = client;
	}

	@EventHandler
	public void notify(UpdateEvent event) {
		if (!_enabled)
			return;

		// if (event.getType() == UpdateType.MIN_08)
		// hugeSale();

		// if (event.getType() == UpdateType.MIN_16)
		// sale();
	}
}
