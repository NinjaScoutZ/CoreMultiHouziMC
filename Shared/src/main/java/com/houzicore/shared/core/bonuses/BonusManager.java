package com.houzicore.shared.core.bonuses;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.java.JavaPlugin;

import com.houzicore.shared.MiniClientPlugin;
import com.houzicore.shared.account.CoreClientManager;
import com.houzicore.shared.core.donation.DonationManager;
import com.houzicore.shared.updater.UpdateType;
import com.houzicore.shared.updater.event.UpdateEvent;
import java.util.HashSet;
import java.util.Set;

public class BonusManager extends MiniClientPlugin<BonusClientData> {

    private final CoreClientManager _clientManager;
    private final DonationManager _donationManager;
    private final BonusRepository _repository;
    private final Set<SpinWheelPage> _activeSpins;

    public BonusManager(JavaPlugin plugin, CoreClientManager clientManager, DonationManager donationManager) {
        super("Bonus", plugin);
        _clientManager = clientManager;
        _donationManager = donationManager;
        _repository = new BonusRepository(plugin);
        _activeSpins = new HashSet<>();
    }

    @Override
    public void addCommands() {
        addCommand(new com.houzicore.shared.core.bonuses.command.BonusCommand(this));
    }

    @Override
    protected BonusClientData AddPlayer(String player) {
        return new BonusClientData();
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        BonusClientData data = Get(player);
        
        _plugin.getServer().getScheduler().runTaskAsynchronously(_plugin, () -> {
            int accountId = _clientManager.Get(player).getAccountId();
            _repository.loadBonusData(accountId, data);
            data.Loaded = true;
        });
    }

    public void addSpin(SpinWheelPage spin) {
        _activeSpins.add(spin);
    }

    public void removeSpin(SpinWheelPage spin) {
        _activeSpins.remove(spin);
    }

    @EventHandler
    public void onUpdate(UpdateEvent event) {
        if (event.getType() != UpdateType.TICK) return;
        
        Set<SpinWheelPage> toRemove = new HashSet<>();
        for (SpinWheelPage spin : _activeSpins) {
            if (!spin.tick()) {
                toRemove.add(spin);
            }
        }
        _activeSpins.removeAll(toRemove);
    }

    public BonusRepository getRepository() {
        return _repository;
    }

    public CoreClientManager getClientManager() {
        return _clientManager;
    }

    public DonationManager getDonationManager() {
        return _donationManager;
    }
}
