package com.houzicore.shared.core.booster;

import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.plugin.java.JavaPlugin;

import com.houzicore.shared.MiniPlugin;
import com.houzicore.shared.account.CoreClientManager;
import com.houzicore.shared.common.util.C;
import com.houzicore.shared.common.util.F;
import com.houzicore.shared.common.util.UtilServer;
import com.houzicore.shared.common.util.UtilTime;
import com.houzicore.shared.core.donation.DonationManager;
import com.houzicore.shared.core.reward.math.MultiplierEngine;
import com.houzicore.shared.serverdata.commands.CommandCallback;
import com.houzicore.shared.serverdata.commands.ServerCommand;
import com.houzicore.shared.serverdata.commands.ServerCommandManager;
import com.houzicore.shared.updater.UpdateType;
import com.houzicore.shared.updater.event.UpdateEvent;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.title.Title;
import java.time.Duration;

public class BoosterManager extends MiniPlugin implements CommandCallback {

    private final CoreClientManager _clientManager;
    private final DonationManager _donationManager;
    
    private String _activeBoosterPlayer = null;
    private long _boosterEndTime = 0;

    private static final int BOOSTER_COST_ESSENCE = 1000;
    private static final long BOOSTER_DURATION_MS = 60 * 60 * 1000L; // 1 Hour

    public BoosterManager(JavaPlugin plugin, CoreClientManager clientManager, DonationManager donationManager) {
        super("Booster", plugin);
        _clientManager = clientManager;
        _donationManager = donationManager;

        MultiplierEngine.registerProvider(new GlobalBoosterProvider(this));
        
        ServerCommandManager.getInstance().registerCommandType("BoosterActivateCommand", BoosterActivateCommand.class, this);
    }

    @Override
    public void addCommands() {
        addCommand(new BoosterCommand(this));
    }

    public String getActiveBoosterPlayer() {
        return _activeBoosterPlayer;
    }

    public long getBoosterEndTime() {
        return _boosterEndTime;
    }

    public boolean isBoosterActive() {
        return _activeBoosterPlayer != null && System.currentTimeMillis() < _boosterEndTime;
    }

    public void attemptActivateBooster(Player player) {
        if (isBoosterActive()) {
            player.sendMessage(F.main(getName(), "A Global Booster is already active, thanks to " + C.cYellow + _activeBoosterPlayer + C.mBody + "!"));
            return;
        }

        int essence = _donationManager.Get(player.getName()).GetEssence();
        if (essence < BOOSTER_COST_ESSENCE) {
            player.sendMessage(F.main(getName(), "You need " + C.cAqua + BOOSTER_COST_ESSENCE + " Essence" + C.mBody + " to activate a Global Booster."));
            return;
        }

        // Deduct Essence
        _donationManager.Get(player.getName()).DeductCost(BOOSTER_COST_ESSENCE, com.houzicore.shared.common.CurrencyType.Essence);
        _donationManager.RewardEssence(null, "Global Booster Purchase", player.getName(), player.getUniqueId(), -BOOSTER_COST_ESSENCE, true);

        // Broadcast to all servers
        BoosterActivateCommand command = new BoosterActivateCommand(player.getName(), BOOSTER_DURATION_MS);
        ServerCommandManager.getInstance().publishCommand(command);
        
        // Apply locally immediately just in case
        activateLocally(player.getName(), BOOSTER_DURATION_MS);
    }

    private void activateLocally(String playerName, long durationMs) {
        if (isBoosterActive()) return;

        _activeBoosterPlayer = playerName;
        _boosterEndTime = System.currentTimeMillis() + durationMs;

        Title.Times times = Title.Times.times(Duration.ofMillis(500), Duration.ofMillis(4000), Duration.ofMillis(1000));
        Component title = Component.text("GLOBAL BOOSTER", NamedTextColor.GOLD, TextDecoration.BOLD);
        Component subtitle = Component.text(playerName, NamedTextColor.YELLOW).append(Component.text(" activated a +100% Coin Booster!", NamedTextColor.WHITE));

        for (Player p : UtilServer.getPlayers()) {
            p.showTitle(Title.title(title, subtitle, times));
            p.playSound(p.getLocation(), Sound.ENTITY_ENDER_DRAGON_GROWL, 1.0f, 1.0f);
            p.sendMessage(F.main(getName(), C.cYellow + playerName + C.mBody + " has activated a " + C.cGold + "Global Coin Booster" + C.mBody + " for 1 Hour!"));
        }
    }

    @Override
    public void run(ServerCommand command) {
        if (command instanceof BoosterActivateCommand) {
            BoosterActivateCommand boosterCmd = (BoosterActivateCommand) command;
            activateLocally(boosterCmd.getPlayerName(), boosterCmd.getDurationMs());
        }
    }

    @EventHandler
    public void onUpdate(UpdateEvent event) {
        if (event.getType() != UpdateType.SEC) return;

        if (_activeBoosterPlayer != null && System.currentTimeMillis() > _boosterEndTime) {
            for (Player p : UtilServer.getPlayers()) {
                p.sendMessage(F.main(getName(), "The " + C.cGold + "Global Coin Booster" + C.mBody + " has expired."));
                p.playSound(p.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASS, 1.0f, 0.5f);
            }
            _activeBoosterPlayer = null;
            _boosterEndTime = 0;
        }
    }
}
