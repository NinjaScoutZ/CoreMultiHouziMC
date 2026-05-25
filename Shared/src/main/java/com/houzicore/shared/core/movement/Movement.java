package com.houzicore.shared.core.movement;

import com.houzicore.shared.MiniClientPlugin;
import com.houzicore.shared.common.util.UtilMath;
import com.houzicore.shared.updater.UpdateType;
import com.houzicore.shared.updater.event.UpdateEvent;

////
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.plugin.java.JavaPlugin;

public class Movement extends MiniClientPlugin<ClientMovement> {
	public Movement(JavaPlugin plugin) {
		super("Movement", plugin);
	}

	@Override
	protected ClientMovement AddPlayer(String player) {
		return new ClientMovement();
	}

	@EventHandler
	public void Update(UpdateEvent event) {
		if (event.getType() == UpdateType.TICK) {
			for (final Player cur : getPlugin().getServer().getOnlinePlayers()) {
				final ClientMovement player = Get(cur);

				if (player.LastLocation != null)
					if (UtilMath.offset(player.LastLocation, cur.getLocation()) > 0) {
						player.LastMovement = System.currentTimeMillis();
					}

				player.LastLocation = cur.getLocation();

				// Save Grounded
				if (cur.isOnGround()) {
					player.LastGrounded = System.currentTimeMillis();
				}
			}
		}
	}
}
