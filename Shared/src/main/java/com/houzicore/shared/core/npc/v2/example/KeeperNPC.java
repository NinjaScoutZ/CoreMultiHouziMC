package com.houzicore.shared.core.npc.v2.example;

import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import com.houzicore.shared.common.util.UtilPlayer;
import com.houzicore.shared.core.donation.DonationManager;
import com.houzicore.shared.core.npc.v2.DialogueSet;
import com.houzicore.shared.core.npc.v2.HouziNPC;
import com.houzicore.shared.core.npc.v2.HouziNPCConfig;
import com.houzicore.shared.core.stats.StatsManager;

public class KeeperNPC extends HouziNPC {

    private final DonationManager donationManager;
    private final StatsManager statsManager;

    public KeeperNPC(Location spawnLocation, DonationManager donationManager, StatsManager statsManager) {
        super(new HouziNPCConfig() {
            @Override
            public String[] getHolograms() {
                return new String[]{ "§bKeeper", "§e§lCLICK" };
            }

            @Override
            public Location getLocation() {
                return spawnLocation;
            }

            @Override
            public EntityType getEntityType() {
                return EntityType.VILLAGER;
            }
        });
        
        this.donationManager = donationManager;
        this.statsManager = statsManager;
    }

    @Override
    public void onClick(Player player, Plugin plugin) {
        if (isInDialogue(player)) {
            UtilPlayer.message(player, "§cPlease wait until I finish speaking!");
            return;
        }

        long claimedCount = statsManager.Get(player).getStat("NPC.Keeper.Claimed");

        if (claimedCount > 0) {
            // Already claimed
            setDialogue(plugin, player, "already-claimed");
        } else {
            // First time
            setDialogue(plugin, player, "welcome").thenRun(() -> {
                // Give rewards
                donationManager.RewardEssenceLater("Keeper Reward", player, 5000);
                donationManager.RewardCoinsLater("Keeper Reward", player, 1000);
                
                // Mark as claimed
                statsManager.Get(player).addStat("NPC.Keeper.Claimed", 1);
                
                UtilPlayer.message(player, "§a§l+5000 Essence! §7(Keeper Reward)");
                UtilPlayer.message(player, "§e§l+1000 Coins! §7(Keeper Reward)");
                player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1f, 1f);
            });
        }
    }

    @Override
    public DialogueSet[] dialogues() {
        return new DialogueSet[]{
            new DialogueSet("welcome", 40L, // 2 seconds between lines
                "Greetings, traveler. I am the Keeper.",
                "It looks like this is your first time here.",
                "I have been instructed to give you this starter fund.",
                "Use it wisely. Good luck on your journey!"
            ),
            new DialogueSet("already-claimed", 30L,
                "Welcome back, traveler.",
                "I hope you've been putting that starter fund to good use."
            )
        };
    }
}
