package com.houzicore.lobby.hub.modules;

import java.util.UUID;

import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

import com.houzicore.shared.common.util.C;
import com.houzicore.shared.common.util.F;
import com.houzicore.shared.common.util.UtilText;
import com.houzicore.shared.core.itemstack.ItemBuilder;
import com.houzicore.shared.core.stats.StatsManager;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.title.Title;
import java.time.Duration;

import com.houzicore.lobby.hub.HubManager;
import com.houzicore.shared.common.util.UtilEvent.ActionType;

public class DailyLoginManager implements Listener {

    private static final String LAST_LOGIN_STAT = "Global.LastLoginTime";
    private static final String STREAK_STAT = "Global.LoginStreak";
    private static final long MILLIS_24H = 24L * 60L * 60L * 1000L;
    private static final long MILLIS_48H = 48L * 60L * 60L * 1000L;

    private final HubManager _hub;
    private final StatsManager _stats;
    private final DailyLoginShop _shop;

    private final java.util.Set<UUID> _checkedThisSession = new java.util.HashSet<>();

    public DailyLoginManager(HubManager hub, StatsManager stats) {
        _hub = hub;
        _stats = stats;
        _shop = new DailyLoginShop(hub, this);
        hub.getPlugin().getServer().getPluginManager().registerEvents(this, hub.getPlugin());
    }

    // Auto-open disabled - player must click the Daily Login NPC to claim

    @EventHandler
    public void onInteract(PlayerInteractEvent event) {
        if (!com.houzicore.shared.common.util.UtilEvent.isAction(event, ActionType.R)) return;
        
        Player player = event.getPlayer();
        ItemStack item = player.getInventory().getItemInMainHand();
        if (item == null || item.getType() == Material.AIR) return;

        if (item.getType() == Material.CLOCK && item.getItemMeta() != null && item.getItemMeta().hasDisplayName()) {
            if (item.getItemMeta().getDisplayName().contains("Reward") || item.getItemMeta().getDisplayName().contains("รางวัล")) {
                _shop.attemptShopOpen(player);
                event.setCancelled(true);
            }
        }
    }

    private org.bukkit.plugin.java.JavaPlugin hub() {
        return _hub.getPlugin();
    }

    public void openRewards(Player player) {
        _shop.attemptShopOpen(player);
    }

    public boolean canClaim(Player player) {
        if (_stats.Get(player) == null) return false;
        long now = System.currentTimeMillis();
        long lastLogin = _stats.Get(player).getStat(LAST_LOGIN_STAT) * 1000L;
        return (now - lastLogin) >= MILLIS_24H;
    }

    public int getStreak(Player player) {
        if (_stats.Get(player) == null) return 0;
        long now = System.currentTimeMillis();
        long lastLogin = _stats.Get(player).getStat(LAST_LOGIN_STAT) * 1000L;
        long streak = _stats.Get(player).getStat(STREAK_STAT);
        
        boolean streakBroken = (now - lastLogin) >= MILLIS_48H;
        if (streakBroken && (now - lastLogin) >= MILLIS_24H) {
            return 0; 
        }
        return (int) streak;
    }

    public void claimReward(Player player, int dayExpected) {
        if (!canClaim(player)) return;

        long now = System.currentTimeMillis();
        long lastLogin = _stats.Get(player).getStat(LAST_LOGIN_STAT) * 1000L;
        long streak = _stats.Get(player).getStat(STREAK_STAT);
        boolean streakBroken = (now - lastLogin) >= MILLIS_48H;

        _stats.incrementStat(player, LAST_LOGIN_STAT, (now / 1000L) - (lastLogin / 1000L));

        if (streakBroken) {
            long oldStreak = streak;
            if (oldStreak > 0) _stats.incrementStat(player, STREAK_STAT, -oldStreak);
            streak = 0;
        }
        streak++;
        _stats.incrementStat(player, STREAK_STAT, 1);

        giveReward(player, (int) streak);
    }

    private void giveReward(Player player, int day) {
        int finalDay = Math.min(day, 7);
        String rewardDesc;
        int coinsReward;

        switch (finalDay) {
            case 1 -> { coinsReward = 50; rewardDesc = "50 Coins"; }
            case 2 -> { coinsReward = 75; rewardDesc = "75 Coins"; }
            case 3 -> { coinsReward = 100; rewardDesc = "100 Coins"; }
            case 4 -> { coinsReward = 150; rewardDesc = "150 Coins"; }
            case 5 -> { coinsReward = 200; rewardDesc = "200 Coins"; }
            case 6 -> { coinsReward = 300; rewardDesc = "300 Coins"; }
            default -> { coinsReward = 500; rewardDesc = "500 Coins"; }
        }

        boolean isPremium = _hub.GetClients().Get(player).GetRank().Has(com.houzicore.shared.common.Rank.SOVEREIGN);
        String chestName = isPremium ? "Ancient Chest" : "Old Chest";
        rewardDesc += " & 1 " + chestName;

        _hub.GetDonation().RewardCoinsLater("Daily Reward", player, coinsReward);
        _hub.getInventoryManager().addItemToInventory(player, "Treasure", chestName, 1);

        Title.Times times = Title.Times.times(Duration.ofMillis(400), Duration.ofMillis(2500), Duration.ofMillis(800));
        player.showTitle(Title.title(
            Component.text(UtilText.toSmallCaps("daily reward!"), NamedTextColor.GOLD, TextDecoration.BOLD),
            Component.text("Day " + finalDay + " — " + rewardDesc, NamedTextColor.GRAY),
            times
        ));

        player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 0.8f, 1.2f);
        hub().getServer().getScheduler().runTaskLater(hub(), () -> {
            player.playSound(player.getLocation(), Sound.ENTITY_FIREWORK_ROCKET_LAUNCH, 1f, 1.4f);
        }, 5L);

        player.sendMessage(F.main("Daily Reward", C.cYellow + "Day " + C.Bold + finalDay + C.cYellow + " streak — Claimed " + C.cGreen + rewardDesc + C.cYellow + "!"));
    }
}
