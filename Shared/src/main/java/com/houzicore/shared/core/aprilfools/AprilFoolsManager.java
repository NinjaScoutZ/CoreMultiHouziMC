package com.houzicore.shared.core.aprilfools;

import java.util.Calendar;

import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.plugin.java.JavaPlugin;

import com.houzicore.shared.MiniPlugin;
import com.houzicore.shared.account.CoreClientManager;
import com.houzicore.shared.common.util.UtilMath;
import com.houzicore.shared.common.util.UtilServer;
import com.houzicore.shared.common.util.UtilTextMiddle;
import com.houzicore.shared.core.disguise.DisguiseManager;
import com.houzicore.shared.core.disguise.disguises.*;
import com.houzicore.shared.updater.UpdateType;
import com.houzicore.shared.updater.event.UpdateEvent;

public class AprilFoolsManager extends MiniPlugin {
	public static AprilFoolsManager Instance;

	public static void Initialize(JavaPlugin plugin, CoreClientManager clientManager, DisguiseManager disguiseManager) {
		Instance = new AprilFoolsManager(plugin, clientManager, disguiseManager);
	}
	private boolean _enabled;
	private final DisguiseManager _disguiseManager;

	private final CoreClientManager _clientManager;

	protected AprilFoolsManager(JavaPlugin plugin, CoreClientManager clientManager, DisguiseManager disguiseManager) {
		super("April Fools", plugin);

		_disguiseManager = disguiseManager;
		_clientManager = clientManager;

		final Calendar c = Calendar.getInstance();
		_enabled = c.get(Calendar.MONTH) == Calendar.APRIL && c.get(Calendar.DAY_OF_MONTH) == 1;
	}

	@EventHandler(priority = EventPriority.LOW)
	public void chatAdd(AsyncPlayerChatEvent event) {
		if (!_enabled)
			return;

		final String[] words = event.getMessage().split(" ");

		String out = "";
		for (final String word : words) {
			// Prefix
			if (Math.random() > 0.85) {
				out += "moo";

				for (int i = 0; i < UtilMath.r(2); i++) {
					out += "o";
				}

				out += " " + word + " ";

			}

			// Suffix
			else if (Math.random() > 0.85) {
				out += word + " ";

				out += "moo";

				for (int i = 0; i < UtilMath.r(2); i++) {
					out += "o";
				}

				out += " ";
			}

			// Swap
			else if (Math.random() > 0.99) {
				out += "moo";

				for (int i = 3; i < word.length(); i++) {
					out += "o";
				}

				out += " ";
			} else {
				out += word + " ";
			}
		}

		event.setMessage(out);
	}

	public String getName(Player player) {
		// Name
		int index = 0;
		boolean hitVowel = false;
		for (int i = 0; i < player.getName().length() - 2 && i < 5; i++) {
			// Detect vowel ;o
			if (player.getName().toLowerCase().charAt(i) == 'a' || player.getName().toLowerCase().charAt(i) == 'e'
					|| player.getName().toLowerCase().charAt(i) == 'i'
					|| player.getName().toLowerCase().charAt(i) == 'o'
					|| player.getName().toLowerCase().charAt(i) == 'u') {
				hitVowel = true;
			}
			// Post vowel consonant - stop here
			else if (hitVowel) {
				break;
			}

			index = i + 1;
		}

		String name = "Moo" + player.getName().substring(index, player.getName().length());

		if (name.length() > 16) {
			name = name.substring(0, 16);
		}

		return name;
	}

	public boolean isActive() {
		return _enabled;
	}

	public void setEnabled(boolean b) {
		final Calendar c = Calendar.getInstance();
		_enabled = b && c.get(Calendar.MONTH) == Calendar.APRIL && c.get(Calendar.DAY_OF_MONTH) == 1;
	}

	@EventHandler
	public void updateCow(UpdateEvent event) {
		if (!_enabled)
			return;

		if (event.getType() != UpdateType.FAST)
			return;

		// Disguise
		for (final Player player : UtilServer.getPlayers()) {
			if (_disguiseManager.getDisguise(player) != null) {
				// Moo
				if (Math.random() > 0.8) {
					if (_disguiseManager.getDisguise(player) instanceof DisguiseCow) {
						player.getWorld().playSound(player.getLocation(), Sound.ENTITY_COW_AMBIENT, (float) Math.random() + 0.5f,
								(float) Math.random() + 0.5f);
					}
				}

				continue;
			}

			// Disguise
			com.houzicore.shared.api.disguise.DisguiseRequest request = new com.houzicore.shared.api.disguise.DisguiseRequest(
				player.getUniqueId(),
				com.houzicore.shared.api.disguise.DisguiseArchetype.MOB,
				"COW",
				true,
				false,
				false
			);
			//disguise.setName(getName(player), _clientManager.Get(player).GetRank());
			//disguise.setCustomNameVisible(true);
			_disguiseManager.getService().apply(player, request);
		}
	}

	@EventHandler
	public void updateEnabled(UpdateEvent event) {
		if (event.getType() != UpdateType.SLOW)
			return;

		final Calendar c = Calendar.getInstance();
		_enabled = c.get(Calendar.MONTH) == Calendar.APRIL && c.get(Calendar.DAY_OF_MONTH) == 1;
	}

	@EventHandler
	public void updateText(UpdateEvent event) {
		if (!_enabled)
			return;

		if (event.getType() != UpdateType.SLOW)
			return;

		if (Math.random() <= 0.99)
			return;

		UtilTextMiddle.display("Moo", null, 5, 20, 5);
	}
}
