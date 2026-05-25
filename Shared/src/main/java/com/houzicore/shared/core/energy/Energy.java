package com.houzicore.shared.core.energy;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerExpChangeEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.plugin.java.JavaPlugin;

import com.houzicore.shared.MiniClientPlugin;
import com.houzicore.shared.updater.event.UpdateEvent;
import com.houzicore.shared.updater.UpdateType;
import com.houzicore.shared.common.util.F;
import com.houzicore.shared.common.util.UtilPlayer;
import com.houzicore.shared.common.util.UtilServer;
import com.houzicore.shared.core.energy.event.EnergyEvent;
import com.houzicore.shared.core.energy.event.EnergyEvent.EnergyChangeReason;

public class Energy extends MiniClientPlugin<ClientEnergy> {
	private final double _baseEnergy = 180;
	private boolean _enabled = true;

	public Energy(JavaPlugin plugin) {
		super("Energy", plugin);
	}

	public void AddEnergyMaxMod(Player player, String reason, int amount) {
		Get(player).MaxEnergyMods.put(reason, amount);
	}

	@Override
	protected ClientEnergy AddPlayer(String player) {
		return new ClientEnergy();
	}

	public double GetCurrent(Player player) {
		return Get(player).Energy;
	}

	public double GetMax(Player player) {
		return _baseEnergy + Get(player).EnergyBonus();
	}

	@EventHandler
	public void handleExp(PlayerExpChangeEvent event) {
		if (!_enabled)
			return;

		event.setAmount(0);
	}

	@EventHandler
	public void HandleJoin(PlayerJoinEvent event) {
		Get(event.getPlayer()).Energy = 0;
	}

	@EventHandler
	public void HandleRespawn(PlayerRespawnEvent event) {
		Get(event.getPlayer()).Energy = 0;
	}

	public void ModifyEnergy(Player player, double energy) {
		if (!_enabled)
			return;

		final ClientEnergy client = Get(player);

		if (energy > 0) {
			client.Energy = Math.min(GetMax(player), client.Energy + energy);
		} else {
			client.Energy = Math.max(0, client.Energy + energy);
		}

		// Record Drain
		if (energy < 0) {
			client.LastEnergy = System.currentTimeMillis();
		}

		player.setExp(Math.min(0.999f, (float) client.Energy / (float) GetMax(player)));
	}

	public void RemoveEnergyMaxMod(Player player, String reason) {
		Get(player).MaxEnergyMods.remove(reason);
	}

	public void setEnabled(boolean b) {
		_enabled = b;
	}

	@EventHandler
	public void Update(UpdateEvent event) {
		if (!_enabled)
			return;

		if (event.getType() != UpdateType.TICK)
			return;

		for (final Player cur : UtilServer.getPlayers()) {
			UpdateEnergy(cur);
		}
	}

	private void UpdateEnergy(Player cur) {
		if (cur.isDead())
			return;

		// Get Exp Attribs
		final double energy = 0.4;

		// Modify Energy
		final EnergyEvent energyEvent = new EnergyEvent(cur, energy, EnergyChangeReason.Recharge);
		_plugin.getServer().getPluginManager().callEvent(energyEvent);

		if (energyEvent.isCancelled())
			return;

		// Update Players Exp
		ModifyEnergy(cur, energyEvent.GetTotalAmount());
	}

	public boolean Use(Player player, String ability, double amount, boolean use, boolean inform) {
		final ClientEnergy client = Get(player);

		if (client.Energy < amount) {
			if (inform) {
				UtilPlayer.message(player,
						F.main(_moduleName, "You are too exhausted to use " + F.skill(ability) + "."));
			}

			return false;
		} else {
			if (!use)
				return true;

			ModifyEnergy(player, -amount);

			return true;
		}
	}
}
