package com.houzicore.shared.core.thank;

import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import com.houzicore.shared.MiniPlugin;
import com.houzicore.shared.account.CoreClientManager;
import com.houzicore.shared.common.util.C;
import com.houzicore.shared.common.util.F;
import com.houzicore.shared.common.util.UtilPlayer;
import com.houzicore.shared.common.util.UtilServer;
import com.houzicore.shared.core.booster.BoosterManager;
import com.houzicore.shared.core.donation.DonationManager;
import com.houzicore.shared.core.thank.command.ThankCommand;

public class ThankManager extends MiniPlugin {

    private final CoreClientManager _clientManager;
    private final DonationManager _donationManager;
    private final BoosterManager _boosterManager;
    private final ThankRepository _repository;

    private static final int SENDER_REWARD = 20;
    private static final int RECEIVER_REWARD = 50;

    public ThankManager(JavaPlugin plugin, CoreClientManager clientManager, DonationManager donationManager, BoosterManager boosterManager) {
        super("Thank", plugin);
        _clientManager = clientManager;
        _donationManager = donationManager;
        _boosterManager = boosterManager;
        _repository = new ThankRepository(plugin);
    }

    @Override
    public void addCommands() {
        addCommand(new ThankCommand(this));
    }

    public void attemptThank(Player caller, String targetName) {
        if (!_boosterManager.isBoosterActive()) {
            UtilPlayer.message(caller, F.main(getName(), "There are no active Global Boosters to thank for."));
            return;
        }

        String activeBoosterPlayer = _boosterManager.getActiveBoosterPlayer();
        if (activeBoosterPlayer == null || !activeBoosterPlayer.equalsIgnoreCase(targetName)) {
            UtilPlayer.message(caller, F.main(getName(), "You can only thank " + C.cYellow + activeBoosterPlayer + C.mBody + " right now for their active Booster."));
            return;
        }

        if (caller.getName().equalsIgnoreCase(targetName)) {
            UtilPlayer.message(caller, F.main(getName(), "You cannot thank yourself!"));
            return;
        }

        final int senderAccountId = _clientManager.Get(caller).getAccountId();
        
        // We need to look up the receiver's account ID. Since it could be offline on this server, we might need a db query.
        // But DonationManager / ClientManager should have a way. We can query the account ID async.
        _plugin.getServer().getScheduler().runTaskAsynchronously(_plugin, () -> {
            int receiverAccountId = _clientManager.getRepository().getAccountId(targetName);
            if (receiverAccountId == -1) {
                Bukkit.getScheduler().runTask(_plugin, () -> UtilPlayer.message(caller, F.main(getName(), "Could not find account for that player.")));
                return;
            }

            // A unique reason so players can thank the same person for a new booster if they wait long enough.
            // But since booster is 1 hour, using the end time as part of the reason makes it unique per session.
            String reason = "Booster_" + _boosterManager.getBoosterEndTime();

            boolean success = _repository.addThank(senderAccountId, receiverAccountId, reason);
            
            Bukkit.getScheduler().runTask(_plugin, () -> {
                if (!success) {
                    UtilPlayer.message(caller, F.main(getName(), "You have already thanked " + C.cYellow + targetName + C.mBody + " for this booster!"));
                    return;
                }

                // Give rewards
                _donationManager.RewardEssence(null, "Thanked Booster", caller.getName(), caller.getUniqueId(), SENDER_REWARD, true);
                
                // For receiver, we need their UUID. DonationManager's RewardEssence supports offline if UUID is provided.
                // We'll just fetch UUID if needed, but RewardEssence can take name and look it up or we can just run a query.
                // Wait, DonationManager.RewardEssence in HouziCore:
                // RewardEssence(Callback<Boolean> callback, String reason, String name, UUID uuid, int amount, boolean display)
                // If receiver is offline on this instance, DonationManager handles it.
                // We need the receiver's UUID. Let's just use empty UUID, DonationManager will look it up if needed? 
                // Actually, _clientManager.getRepository() might have a method. Let's just pass null for UUID.
                _donationManager.RewardEssence(null, "Thanked by " + caller.getName(), targetName, null, RECEIVER_REWARD, false);

                UtilPlayer.message(caller, F.main(getName(), "You thanked " + C.cYellow + targetName + C.mBody + " and received " + C.cGreen + SENDER_REWARD + " Essence" + C.mBody + "!"));
                caller.playSound(caller.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1.0f, 1.0f);

                // If receiver is online on this server, message them
                Player receiver = Bukkit.getPlayerExact(targetName);
                if (receiver != null) {
                    UtilPlayer.message(receiver, F.main(getName(), C.cYellow + caller.getName() + C.mBody + " thanked you! You received " + C.cGreen + RECEIVER_REWARD + " Essence" + C.mBody + "!"));
                    receiver.playSound(receiver.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1.0f, 1.0f);
                }
            });
        });
    }
}
