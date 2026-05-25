package com.houzicore.shared.core.benefit.benefits;

import com.houzicore.shared.core.benefit.BenefitManager;
import com.houzicore.shared.core.benefit.BenefitManagerRepository;
import com.houzicore.shared.common.util.Callback;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

public abstract class BenefitBase {
	private final BenefitManager _plugin;
	private final String _name;
	private final BenefitManagerRepository _repository;

	protected BenefitBase(BenefitManager plugin, String name, BenefitManagerRepository repository) {
		_plugin = plugin;
		_name = name;
		_repository = repository;
	}

	public String getName() {
		return _name;
	}

	public JavaPlugin getPlugin() {
		return _plugin.getPlugin();
	}

	public BenefitManagerRepository getRepository() {
		return _repository;
	}

	public void recordBenefit(final Player player, final Callback<Boolean> callback) {
		Bukkit.getServer().getScheduler().runTaskAsynchronously(_plugin.getPlugin(), new Runnable() {
			@Override
			public void run() {
				final boolean success = _repository.addBenefit(_plugin.getClientManager().Get(player).getAccountId(),
						_name);

				callback.run(success);
			}
		});
	}

	public abstract void rewardPlayer(Player player);
}
