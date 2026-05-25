package com.houzicore.shared.core.benefit;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import com.houzicore.shared.MiniDbClientPlugin;
import com.houzicore.shared.account.CoreClientManager;
import com.houzicore.shared.core.benefit.benefits.*;
import com.houzicore.shared.common.util.Callback;
import com.houzicore.shared.core.inventory.InventoryManager;

import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.java.JavaPlugin;

public class BenefitManager extends MiniDbClientPlugin<BenefitData> {
	private final BenefitManagerRepository _repository;

	private final List<BenefitBase> _benefits = new ArrayList<>();

	public BenefitManager(JavaPlugin plugin, CoreClientManager clientManager, InventoryManager inventoryManager) {
		super("Benefit Manager", plugin, clientManager);

		_repository = new BenefitManagerRepository(plugin);

		// _benefits.add(new Christmas2014(plugin, _repository, inventoryManager));
		// _benefits.add(new Thanksgiving2014(plugin, _repository, inventoryManager));
		// _benefits.add(new Players40k(this, _repository, inventoryManager));
	}

	@Override
	protected BenefitData AddPlayer(String player) {
		return new BenefitData();
	}

	@Override
	public String getQuery(int accountId, String uuid, String name) {
		return "SELECT benefit FROM rankBenefits WHERE rankBenefits.accountId = '" + accountId + "';";
	}

	@EventHandler(priority = EventPriority.LOWEST)
	public void giveBenefit(final PlayerJoinEvent event) {
		Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(getPlugin(), new Runnable() {
			@Override
			public void run() {
				if (Get(event.getPlayer()).Loaded) {
					for (final BenefitBase benefit : _benefits) {
						if (!Get(event.getPlayer()).Benefits.contains(benefit.getName())) {
							benefit.recordBenefit(event.getPlayer(), new Callback<Boolean>() {
								@Override
								public void run(Boolean success) {
									if (success) {
										benefit.rewardPlayer(event.getPlayer());
									} else {
									}
								}
							});
						}
					}
				}
			}
		}, 100L);
	}

	@Override
	public void processLoginResultSet(String playerName, int accountId, ResultSet resultSet) throws SQLException {
		Set(playerName, _repository.retrievePlayerBenefitData(resultSet));
	}
}
