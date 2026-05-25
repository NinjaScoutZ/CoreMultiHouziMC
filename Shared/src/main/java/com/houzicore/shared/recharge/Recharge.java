package com.houzicore.shared.recharge;

import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.plugin.java.JavaPlugin;

import com.houzicore.shared.MiniPlugin;
import com.houzicore.shared.updater.event.UpdateEvent;
import com.houzicore.shared.updater.UpdateType;
import com.houzicore.shared.account.event.ClientUnloadEvent;
import com.houzicore.shared.common.util.F;
import com.houzicore.shared.common.util.NautHashMap;
import com.houzicore.shared.common.util.UtilPlayer;
import com.houzicore.shared.common.util.UtilServer;
import com.houzicore.shared.common.util.UtilTime;
import com.houzicore.shared.common.util.UtilTime.TimeUnit;

public class Recharge extends MiniPlugin {
	public static Recharge Instance;

	public static void Initialize(JavaPlugin plugin) {
		Instance = new Recharge(plugin);
	}
	public HashSet<String> informSet = new HashSet<>();

	public NautHashMap<String, NautHashMap<String, RechargeData>> _recharge = new NautHashMap<>();

	private HashSet<String> _ignoreCooldowns = new HashSet<>();

	protected Recharge(JavaPlugin plugin) {
		super("Recharge", plugin);
	}

	@Override
	public void addCommands() {
		addCommand(new com.houzicore.shared.recharge.command.CooldownCommand(this));
	}

	public boolean isIgnoreCooldowns(Player player) {
		return _ignoreCooldowns.contains(player.getName());
	}

	public void setIgnoreCooldowns(Player player, boolean ignore) {
		if (ignore) {
			_ignoreCooldowns.add(player.getName());
		} else {
			_ignoreCooldowns.remove(player.getName());
		}
	}

	@EventHandler
	public void clearPlayer(ClientUnloadEvent event) {
		_recharge.remove(event.GetName());
	}

	public void debug(Player player, String ability) {
		if (!_recharge.containsKey(player.getName())) {
			player.sendMessage("No Recharge Map.");
			return;
		}

		if (!_recharge.get(player.getName()).containsKey(ability)) {
			player.sendMessage("Ability Not Found.");
			return;
		}

		_recharge.get(player.getName()).get(ability).debug(player);
	}

	public NautHashMap<String, RechargeData> Get(Player player) {
		return Get(player.getName());
	}

	public NautHashMap<String, RechargeData> Get(String name) {
		if (!_recharge.containsKey(name)) {
			_recharge.put(name, new NautHashMap<String, RechargeData>());
		}

		return _recharge.get(name);
	}

	@EventHandler
	public void PlayerDeath(PlayerDeathEvent event) {
		Get(event.getEntity().getName()).clear();
	}

	public void recharge() {
		for (final Player cur : UtilServer.getPlayers()) {
			final LinkedList<String> rechargeList = new LinkedList<>();

			// Check Recharged
			for (final String ability : Get(cur).keySet()) {
				if (Get(cur).get(ability).Update()) {
					rechargeList.add(ability);
				}
			}

			// Inform Recharge
			for (final String ability : rechargeList) {
				Get(cur).remove(ability);

				// Event
				final RechargedEvent rechargedEvent = new RechargedEvent(cur, ability);
				UtilServer.getServer().getPluginManager().callEvent(rechargedEvent);

				if (informSet.contains(ability)) {
					UtilPlayer.message(cur, F.main("Recharge", "You can use " + F.skill(ability) + "."));
				}
			}
		}
	}

	public void recharge(Player player, String ability) {
		Get(player).remove(ability);
	}

	public void Reset(Player player) {
		_recharge.put(player.getName(), new NautHashMap<String, RechargeData>());
	}

	public void Reset(Player player, String stringContains) {
		final NautHashMap<String, RechargeData> data = _recharge.get(player.getName());

		if (data == null)
			return;

		final Iterator<String> rechargeIter = data.keySet().iterator();

		while (rechargeIter.hasNext()) {
			final String key = rechargeIter.next();

			if (key.toLowerCase().contains(stringContains.toLowerCase())) {
				rechargeIter.remove();
			}
		}
	}

	public void setCountdown(Player player, String ability, boolean countdown) {
		if (!_recharge.containsKey(player.getName()))
			return;

		if (!_recharge.get(player.getName()).containsKey(ability))
			return;

		_recharge.get(player.getName()).get(ability).Countdown = countdown;
	}

	public void setDisplayForce(Player player, String ability, boolean displayForce) {
		if (!_recharge.containsKey(player.getName()))
			return;

		if (!_recharge.get(player.getName()).containsKey(ability))
			return;

		_recharge.get(player.getName()).get(ability).DisplayForce = displayForce;
	}

	@EventHandler
	public void update(UpdateEvent event) {
		if (event.getType() != UpdateType.TICK)
			return;

		recharge();
	}

	public boolean usable(Player player, String ability) {
		return usable(player, ability, false);
	}

	public boolean usable(Player player, String ability, boolean inform) {
		if (isIgnoreCooldowns(player)) return true;
		if (!Get(player).containsKey(ability))
			return true;

		if (Get(player).get(ability).GetRemaining() <= 0)
			return true;
		else {
			if (inform) {
				UtilPlayer.message(player, F.main("Recharge", "You cannot use " + F.skill(ability) + " for "
						+ F.time(UtilTime.convertString(Get(player).get(ability).GetRemaining(), 1, TimeUnit.FIT))
						+ "."));
			}

			return false;
		}
	}

	public boolean use(Player player, String ability, long recharge, boolean inform, boolean attachItem) {
		return use(player, ability, ability, recharge, inform, attachItem);
	}

	public boolean use(Player player, String ability, long recharge, boolean inform, boolean attachItem,
			boolean attachDurability) {
		return use(player, ability, ability, recharge, inform, attachItem, attachDurability);
	}

	public boolean use(Player player, String ability, String abilityFull, long recharge, boolean inform,
			boolean attachItem) {
		return use(player, ability, abilityFull, recharge, inform, attachItem, false);
	}

	public boolean use(Player player, String ability, String abilityFull, long recharge, boolean inform,
			boolean attachItem, boolean attachDurability) {
		if (recharge == 0 || isIgnoreCooldowns(player))
			return true;

		// Ensure Expirey
		recharge();

		// Lodge Recharge Msg
		if (inform && recharge > 1000) {
			informSet.add(ability);
		}

		// Recharging
		if (Get(player).containsKey(ability)) {
			if (inform) {
				UtilPlayer.message(player, F.main("Recharge", "You cannot use " + F.skill(abilityFull) + " for "
						+ F.time(UtilTime.convertString(Get(player).get(ability).GetRemaining(), 1, TimeUnit.FIT))
						+ "."));
			}

			return false;
		}

		// Insert
		UseRecharge(player, ability, recharge, attachItem, attachDurability);

		return true;
	}

	public void useForce(Player player, String ability, long recharge) {
		useForce(player, ability, recharge, false);
	}

	public void useForce(Player player, String ability, long recharge, boolean attachItem) {
		UseRecharge(player, ability, recharge, attachItem, false);
	}

	public void UseRecharge(Player player, String ability, long recharge, boolean attachItem,
			boolean attachDurability) {
		// Event
		final RechargeEvent rechargeEvent = new RechargeEvent(player, ability, recharge);
		UtilServer.getServer().getPluginManager().callEvent(rechargeEvent);

		Get(player).put(ability, new RechargeData(this, player, ability, player.getInventory().getItemInMainHand(),
				rechargeEvent.GetRecharge(), attachItem, attachDurability));
	}
}
