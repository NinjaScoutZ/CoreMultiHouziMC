package com.houzicore.shared.core.bonuses;

import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

import com.houzicore.shared.account.CoreClientManager;
import com.houzicore.shared.common.util.C;
import com.houzicore.shared.common.util.F;
import com.houzicore.shared.common.util.UtilTime;
import com.houzicore.shared.core.donation.DonationManager;
import com.houzicore.shared.core.shop.item.ShopItem;
import com.houzicore.shared.core.shop.page.ShopPageBase;
import com.houzicore.shared.core.shop.ShopBase;

public class BonusPage extends ShopPageBase<BonusManager, BonusMenu> {

    public BonusPage(BonusManager plugin, BonusMenu shop, CoreClientManager clientManager, DonationManager donationManager, Player player) {
        super(plugin, shop, clientManager, donationManager, "Keeper of Essence", player);
        buildPage();
    }

    @Override
    protected void buildPage() {
        if (!getPlugin().Get(getPlayer()).Loaded) {
            getPlayer().sendMessage(F.main("Bonus", "Your bonus data is still loading..."));
            return;
        }

        BonusClientData data = getPlugin().Get(getPlayer());

        // Daily Reward
        long dailyTime = data.DailyTime;
        long timeSinceDaily = System.currentTimeMillis() - dailyTime;
        boolean dailyAvailable = timeSinceDaily > (1000L * 60 * 60 * 24);

        int dailySlot = 11;
        if (dailyAvailable) {
            addButton(dailySlot, new ShopItem(Material.ENDER_CHEST, C.cGreen + "Daily Reward",
                    new String[]{C.cGray + "Claim your daily reward!", "", C.cYellow + "Click to claim!"},
                    1, false), (player, clickType) -> {
                claimDaily(data);
            });
        } else {
            long remaining = (1000L * 60 * 60 * 24) - timeSinceDaily;
            addButton(dailySlot, new ShopItem(Material.MINECART, C.cRed + "Daily Reward",
                    new String[]{C.cGray + "You have already claimed this today.", "", C.cRed + "Available in: " + UtilTime.MakeStr(remaining)},
                    1, false), (player, clickType) -> {
                getPlayer().playSound(getPlayer().getLocation(), Sound.BLOCK_NOTE_BLOCK_BASS, 1.0f, 1.0f);
            });
        }

        // Vote Reward
        long voteTime = data.VoteTime;
        long timeSinceVote = System.currentTimeMillis() - voteTime;
        boolean voteAvailable = timeSinceVote > (1000L * 60 * 60 * 24);

        int voteSlot = 15;
        if (voteAvailable) {
            addButton(voteSlot, new ShopItem(Material.DIAMOND, C.cGreen + "Vote Reward",
                    new String[]{C.cGray + "Claim your reward for voting!", "", C.cYellow + "Click to claim!"},
                    1, false), (player, clickType) -> {
                claimVote(data);
            });
        } else {
            long remaining = (1000L * 60 * 60 * 24) - timeSinceVote;
            addButton(voteSlot, new ShopItem(Material.COAL, C.cRed + "Vote Reward",
                    new String[]{C.cGray + "You have already voted today.", "", C.cRed + "Available in: " + UtilTime.MakeStr(remaining)},
                    1, false), (player, clickType) -> {
                getPlayer().playSound(getPlayer().getLocation(), Sound.BLOCK_NOTE_BLOCK_BASS, 1.0f, 1.0f);
            });
        }

        // Spin the Wheel (Tickets)
        int tickets = data.Tickets;
        int spinSlot = 22; // Bottom center
        if (tickets > 0) {
            addButton(spinSlot, new ShopItem(Material.PLAYER_HEAD, (byte) 4, C.cGreen + "Spin the Wheel",
                    new String[]{C.cGray + "Use a Ticket to spin the wheel!", "", C.cYellow + "Tickets: " + C.cWhite + tickets, "", C.cYellow + "Click to spin!"},
                    1, false, false), (player, clickType) -> {
                getShop().openPageForPlayer(getPlayer(), new SpinWheelPage(getPlugin(), getShop(), getClientManager(), getDonationManager(), player, data));
            });
        } else {
            addButton(spinSlot, new ShopItem(Material.PLAYER_HEAD, (byte) 4, C.cRed + "Spin the Wheel",
                    new String[]{C.cGray + "You don't have any Spin Tickets.", "", C.cRed + "Tickets: 0"},
                    1, false, false), (player, clickType) -> {
                getPlayer().playSound(getPlayer().getLocation(), Sound.BLOCK_NOTE_BLOCK_BASS, 1.0f, 1.0f);
            });
        }
    }

    private void claimDaily(BonusClientData data) {
        long timeSinceDaily = System.currentTimeMillis() - data.DailyTime;
        
        // If more than 48 hours, reset streak
        if (timeSinceDaily > (1000L * 60 * 60 * 48)) {
            data.DailyStreak = 0;
        }
        
        data.DailyStreak++;
        data.DailyTime = System.currentTimeMillis();

        getPlugin().getPlugin().getServer().getScheduler().runTaskAsynchronously(getPlugin().getPlugin(), () -> {
            int accountId = getClientManager().Get(getPlayer()).getAccountId();
            getPlugin().getRepository().updateDailyBonus(accountId, data.DailyTime, data.DailyStreak);
        });

        getPlayer().playSound(getPlayer().getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1.0f, 1.0f);
        getDonationManager().RewardCoinsLater("Daily Bonus", getPlayer(), 100 * data.DailyStreak);
        
        getPlayer().sendMessage(F.main("Bonus", "You claimed your " + C.cGreen + "Daily Reward" + C.mBody + "! Streak: " + C.cYellow + data.DailyStreak));
        refresh();
    }

    private void claimVote(BonusClientData data) {
        data.VoteStreak++;
        data.VoteTime = System.currentTimeMillis();
        data.Tickets++; // Give 1 spin ticket

        getPlugin().getPlugin().getServer().getScheduler().runTaskAsynchronously(getPlugin().getPlugin(), () -> {
            int accountId = getClientManager().Get(getPlayer()).getAccountId();
            getPlugin().getRepository().updateVoteBonus(accountId, data.VoteTime, data.VoteStreak);
            getPlugin().getRepository().updateTickets(accountId, data.Tickets);
        });

        getPlayer().playSound(getPlayer().getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1.0f, 1.0f);
        getDonationManager().RewardCoinsLater("Vote Bonus", getPlayer(), 500);

        getPlayer().sendMessage(F.main("Bonus", "You claimed your " + C.cGreen + "Vote Reward" + C.mBody + " and received 1 " + C.cYellow + "Spin Ticket" + C.mBody + "!"));
        refresh();
    }
}
