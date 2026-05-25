package com.houzicore.shared.core.cosmetic.collection;

import com.houzicore.shared.MiniPlugin;
import com.houzicore.shared.account.CoreClientManager;
import com.houzicore.shared.common.util.C;
import com.houzicore.shared.common.util.F;
import com.houzicore.shared.common.util.UtilParticle;
import com.houzicore.shared.common.util.UtilParticle.ParticleType;
import com.houzicore.shared.common.util.UtilParticle.ViewDist;
import com.houzicore.shared.core.donation.DonationManager;
import com.houzicore.shared.updater.UpdateType;
import com.houzicore.shared.updater.event.UpdateEvent;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/**
 * Manages players' cosmetic collections and grants bonuses when collections are complete.
 */
public class CollectionManager extends MiniPlugin {

    private final CoreClientManager clientManager;
    private final DonationManager donationManager;
    
    // Tracks players who have completed specific collections to avoid repeatedly sending completion messages
    private final Set<String> completedCollectionsTracker = new HashSet<>();

    public CollectionManager(JavaPlugin plugin, CoreClientManager clientManager, DonationManager donationManager) {
        super("Cosmetic Collections", plugin);
        this.clientManager = clientManager;
        this.donationManager = donationManager;
    }

    public DonationManager getDonationManager() {
        return donationManager;
    }

    public CoreClientManager getClientManager() {
        return clientManager;
    }

    /**
     * Checks if a player owns all items in a specified collection.
     */
    public boolean hasCompletedCollection(Player player, CosmeticCollection collection) {
        if (donationManager.Get(player.getName()) == null) return false;
        
        for (String itemName : collection.getRequiredItems()) {
            if (!donationManager.Get(player.getName()).OwnsUnknownPackage(itemName)) {
                return false;
            }
        }
        return true;
    }

    /**
     * Returns the number of items a player owns in a collection.
     */
    public int getCompletionCount(Player player, CosmeticCollection collection) {
        if (donationManager.Get(player.getName()) == null) return 0;
        
        int count = 0;
        for (String itemName : collection.getRequiredItems()) {
            if (donationManager.Get(player.getName()).OwnsUnknownPackage(itemName)) {
                count++;
            }
        }
        return count;
    }

    /**
     * Update loop to check if players naturally complete a collection during play.
     */
    @EventHandler
    public void onUpdate(UpdateEvent event) {
        if (event.getType() != UpdateType.SEC) return;

        for (Player player : Bukkit.getOnlinePlayers()) {
            for (CosmeticCollection collection : CosmeticCollection.values()) {
                String trackerKey = player.getUniqueId().toString() + "_" + collection.name();
                
                if (hasCompletedCollection(player, collection)) {
                    if (!completedCollectionsTracker.contains(trackerKey)) {
                        completedCollectionsTracker.add(trackerKey);
                        playCompletionEffect(player, collection);
                    }
                }
            }
        }
    }
    
    /**
     * Plays a special effect and sends a message when a player first completes a collection.
     */
    private void playCompletionEffect(Player player, CosmeticCollection collection) {
        player.sendMessage(F.main(getName(), "You completed the " + collection.getColor() + C.Bold + collection.getDisplayName() + C.cGray + " collection!"));
        player.sendMessage(F.main(getName(), "Bonus unlocked: " + C.cYellow + collection.getBonusDescription()));
        
        player.playSound(player.getLocation(), Sound.UI_TOAST_CHALLENGE_COMPLETE, 1.0f, 1.0f);
        
        UtilParticle.PlayParticle(ParticleType.FIREWORKS_SPARK,
                player.getLocation().add(0, 1, 0),
                0.5f, 0.5f, 0.5f,
                0.1f, 50,
                ViewDist.NORMAL, Bukkit.getOnlinePlayers().toArray(new Player[0]));
    }
}
