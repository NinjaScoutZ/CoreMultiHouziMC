package com.houzicore.shared.core.preferences;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map.Entry;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.java.JavaPlugin;

import com.houzicore.shared.MiniDbClientPlugin;
import com.houzicore.shared.account.CoreClientManager;
import com.houzicore.shared.common.util.NautHashMap;
import com.houzicore.shared.core.donation.DonationManager;
import com.houzicore.shared.core.itemstack.ItemStackFactory;
import com.houzicore.shared.core.preferences.command.PreferencesCommand;
import com.houzicore.shared.core.preferences.ui.PreferencesShop;
import com.houzicore.shared.updater.UpdateType;
import com.houzicore.shared.updater.event.UpdateEvent;

public class PreferencesManager extends MiniDbClientPlugin<UserPreferences> {
	private final PreferencesRepository _repository;
	private final PreferencesShop _shop;

	private final NautHashMap<String, UserPreferences> _saveBuffer = new NautHashMap<>();

	public PreferencesManager(JavaPlugin plugin, CoreClientManager clientManager, DonationManager donationManager) {
		super("Preferences", plugin, clientManager);

		_repository = new PreferencesRepository(plugin);
		_shop = new PreferencesShop(this, clientManager, donationManager);

		addCommand(new PreferencesCommand(this));
	}

	@Override
	protected UserPreferences AddPlayer(String player) {
		return new UserPreferences();
	}

	@Override
	public String getQuery(int accountId, String uuid, String name) {
		return _repository.buildLoadQuery(uuid);
	}

	public void openShop(Player caller) {
		_shop.attemptShopOpen(caller);
	}

	@EventHandler(priority = EventPriority.LOWEST)
	public void playerInteract(PlayerInteractEvent event) {
		if (event.getItem() != null && event.getItem().getType() == Material.COMPARATOR) {
			_shop.attemptShopOpen(event.getPlayer());
			event.setCancelled(true);
		}
	}

	@Override
	public void processLoginResultSet(String playerName, int accountId, ResultSet resultSet) throws SQLException {
		Set(playerName, _repository.loadClientInformation(resultSet));
	}

	public void savePreferences(Player caller) {
		_saveBuffer.put(caller.getUniqueId().toString(), Get(caller));
	}

	@EventHandler
	public void storeBuffer(UpdateEvent event) {
		if (event.getType() != UpdateType.SLOW)
			return;

		final NautHashMap<String, UserPreferences> bufferCopy = new NautHashMap<>();

		for (final Entry<String, UserPreferences> entry : _saveBuffer.entrySet()) {
			bufferCopy.put(entry.getKey(), entry.getValue());
		}

		_saveBuffer.clear();

		getPlugin().getServer().getScheduler().runTaskAsynchronously(getPlugin(), new Runnable() {
			@Override
			public void run() {
				_repository.saveUserPreferences(bufferCopy);
			}
		});
	}
}
