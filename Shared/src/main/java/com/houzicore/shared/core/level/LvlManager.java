package com.houzicore.shared.core.level;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;

import com.houzicore.shared.account.CoreClientManager;
import com.houzicore.shared.common.util.C;
import com.houzicore.shared.common.util.F;
import com.houzicore.shared.common.util.UtilText;
import com.houzicore.shared.common.actionbar.ActionBarChannel;
import com.houzicore.shared.common.actionbar.ActionBarService;
import com.houzicore.shared.core.donation.DonationManager;
import com.houzicore.shared.core.inventory.InventoryManager;
import com.houzicore.shared.core.lang.LangManager;
import com.houzicore.shared.core.stats.StatsManager;
import com.houzicore.shared.core.stats.event.StatChangeEvent;
import com.houzicore.shared.core.treasure.TreasureType;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.title.Title;
import java.time.Duration;

/**
 * Manages Global Player Level/XP system.
 * XP is stored in StatsManager under "Global.XP".
 * Level is calculated dynamically from XP and can be projected onto
 * lobby-only vanilla level/exp visuals via applyExperienceBar(...).
 */
public class LvlManager implements Listener {

    public static final String XP_STAT = "Global.XP";
    private static final int MAX_LEVEL = 100;

    private final JavaPlugin _plugin;
    private final StatsManager _stats;
    private final CoreClientManager _clients;
    private final DonationManager _donationManager;

    // XP per level follows a slightly exponential formula:
    // Level N requires XP_BASE * N^1.5 cumulative XP
    private static final long XP_BASE = 500;

    public LvlManager(JavaPlugin plugin, StatsManager stats, CoreClientManager clients, DonationManager donationManager) {
        _plugin = plugin;
        _stats = stats;
        _clients = clients;
        _donationManager = donationManager;
        _plugin.getServer().getPluginManager().registerEvents(this, _plugin);
    }

    /**
     * Returns the cumulative XP required to reach a given level.
     */
    public static long xpForLevel(int level) {
        if (level <= 1) return 0;
        long total = 0;
        for (int i = 1; i < level; i++) {
            total += (long) (XP_BASE * Math.pow(i, 1.5));
        }
        return total;
    }

    /**
     * Returns the level corresponding to a given total XP amount.
     */
    public static int levelFromXp(long xp) {
        int level = 1;
        while (xpForLevel(level + 1) <= xp) {
            level++;
        }
        return Math.min(level, MAX_LEVEL);
    }

    /**
     * Returns the level for a given player based on their "Global.XP" stat.
     */
    public int getLevel(Player player) {
        long xp = getXp(player);
        return levelFromXp(xp);
    }

    /**
     * Returns the raw XP for a given player.
     */
    public long getXp(Player player) {
        if (_stats.Get(player) == null) return 0;
        return _stats.Get(player).getStat(XP_STAT);
    }

    public long getXpAtCurrentLevel(Player player) {
        return xpForLevel(getLevel(player));
    }

    public long getXpAtNextLevel(Player player) {
        int level = getLevel(player);
        if (level >= MAX_LEVEL) {
            return xpForLevel(MAX_LEVEL);
        }

        return xpForLevel(level + 1);
    }

    public long getXpIntoCurrentLevel(Player player) {
        return Math.max(0, getXp(player) - getXpAtCurrentLevel(player));
    }

    public long getXpNeededForNextLevel(Player player) {
        int level = getLevel(player);
        if (level >= MAX_LEVEL) {
            return 0;
        }

        return Math.max(0, getXpAtNextLevel(player) - getXp(player));
    }

    public float getProgress(Player player) {
        int level = getLevel(player);
        if (level >= MAX_LEVEL) {
            return 0.999f;
        }

        long currentLevelXp = getXpAtCurrentLevel(player);
        long nextLevelXp = getXpAtNextLevel(player);
        long span = Math.max(1, nextLevelXp - currentLevelXp);
        float progress = (float) (getXp(player) - currentLevelXp) / (float) span;
        return Math.max(0f, Math.min(0.999f, progress));
    }

    public void applyExperienceBar(Player player) {
        if (player == null || !player.isOnline()) {
            return;
        }

        player.setLevel(getLevel(player));
        player.setExp(getProgress(player));
    }

    public static String formatLevelPrefix(int level) {
        return getLevelColor(level) + "[" + UtilText.toSmallCaps("lvl " + level) + "]" + org.bukkit.ChatColor.RESET + " ";
    }

    /**
     * Awards XP to a player and handles levelling up.
     */
    public void awardXp(Player player, long amount, String reason) {
        if (player == null || amount <= 0) return;

        _stats.incrementStat(player, XP_STAT, amount);

        // Notify in action bar
        if (player.isOnline()) {
            Component message = Component.text("+" + amount + " XP", NamedTextColor.YELLOW);

            if (reason != null && !reason.isEmpty()) {
                message = message.appendSpace()
                        .append(Component.text("(" + reason + ")", NamedTextColor.GRAY));
            }

            ActionBarService.display(player, ActionBarChannel.REWARD, message);
        }
    }

    /**
     * Returns a formatted "[LVL X]" prefix for a player.
     */
    public String getLevelPrefix(Player player) {
        return formatLevelPrefix(getLevel(player));
    }

    private static String getLevelColor(int lvl) {
        if (lvl >= 80) return C.cPurple;
        if (lvl >= 60) return C.cGold;
        if (lvl >= 40) return C.cAqua;
        if (lvl >= 20) return C.cGreen;
        return C.cGray;
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onXpStatChange(StatChangeEvent event) {
        if (!XP_STAT.equalsIgnoreCase(event.getStatName())) {
            return;
        }

        Player player = org.bukkit.Bukkit.getPlayerExact(event.getPlayerName());
        if (player == null || !player.isOnline()) {
            return;
        }

        int oldLevel = levelFromXp(event.getValueBefore());
        int newLevel = levelFromXp(event.getValueAfter());

        if (newLevel <= oldLevel) {
            return;
        }

        player.playSound(player.getLocation(), org.bukkit.Sound.UI_TOAST_CHALLENGE_COMPLETE, 1f, 1f);

        Title.Times times = Title.Times.times(Duration.ofMillis(500), Duration.ofMillis(3000), Duration.ofMillis(800));
        Component titleComp = Component.text(UtilText.toSmallCaps("level up!"), NamedTextColor.GOLD, TextDecoration.BOLD);
        Component subtitleComp = Component.text("Level " + oldLevel + " -> " + newLevel, NamedTextColor.WHITE);
        player.showTitle(Title.title(titleComp, subtitleComp, times));

        player.sendMessage(F.main("Level Up", C.cYellow + "You are now " + C.cGreen + C.Bold + "Level " + newLevel + C.cYellow + "!"));
        rewardMilestones(player, oldLevel, newLevel);
        applyExperienceBar(player);
    }

    private void rewardMilestones(Player player, int oldLevel, int newLevel) {
        if (_donationManager == null) {
            return;
        }

        for (int level = oldLevel + 1; level <= newLevel; level++) {
            MilestoneReward reward = MilestoneReward.forLevel(level);
            if (reward == null) {
                continue;
            }

            if (reward.essence > 0) {
                _donationManager.RewardEssenceLater("Level Milestone " + level, player, reward.essence);
            }
            if (reward.coins > 0) {
                _donationManager.RewardCoinsLater("Level Milestone " + level, player, reward.coins);
            }
            if (reward.gold > 0) {
                _donationManager.RewardGoldLater("Level Milestone " + level, player, reward.gold);
            }
            if (reward.treasureType != null && reward.treasureCount > 0 && InventoryManager.Instance != null) {
                InventoryManager.Instance.addItemToInventory(player, "Item", reward.treasureType.getItemName(), reward.treasureCount);
            }

            boolean thai = LangManager.get() != null && LangManager.get().isThai(player);
            String tierText = thai ? reward.thaiTierName : reward.englishTierName;
            player.sendMessage(F.main(
                    thai ? "เลเวลสำคัญ" : "Level Milestone",
                    (thai ? "ถึง " : "Reached ") + C.cAqua + C.Bold + "Level " + level + C.cGray + " • " + tierText));

            if (reward.essence > 0) {
                player.sendMessage(C.cDGray + "  • " + C.cGreen + reward.essence + C.cGray + (thai ? " Essence" : " Essence"));
            }
            if (reward.coins > 0) {
                player.sendMessage(C.cDGray + "  • " + C.cGold + reward.coins + C.cGray + (thai ? " Coins" : " Coins"));
            }
            if (reward.gold > 0) {
                player.sendMessage(C.cDGray + "  • " + C.cYellow + reward.gold + C.cGray + (thai ? " Gold" : " Gold"));
            }
            if (reward.treasureType != null && reward.treasureCount > 0) {
                player.sendMessage(C.cDGray + "  • " + C.cPurple + reward.treasureCount + "x " + C.cWhite + reward.treasureType.getPlainName(player));
            }

            ActionBarService.display(
                    player,
                    ActionBarChannel.REWARD,
                    Component.text((thai ? "Milestone " : "Milestone ") + "Lv." + level, NamedTextColor.GOLD, TextDecoration.BOLD)
                            .appendSpace()
                            .append(Component.text("• " + reward.rewardSummary(player, thai), NamedTextColor.YELLOW)),
                    2200L);
        }
    }

    private static final class MilestoneReward {
        private final String englishTierName;
        private final String thaiTierName;
        private final int essence;
        private final int coins;
        private final int gold;
        private final TreasureType treasureType;
        private final int treasureCount;

        private MilestoneReward(String englishTierName, String thaiTierName, int essence, int coins, int gold,
                TreasureType treasureType, int treasureCount) {
            this.englishTierName = englishTierName;
            this.thaiTierName = thaiTierName;
            this.essence = essence;
            this.coins = coins;
            this.gold = gold;
            this.treasureType = treasureType;
            this.treasureCount = treasureCount;
        }

        private static MilestoneReward forLevel(int level) {
            switch (level) {
                case 5:
                    return new MilestoneReward("Early Climb", "ช่วงตั้งตัว", 150, 0, 0, TreasureType.OLD, 1);
                case 10:
                    return new MilestoneReward("Early Climb", "ช่วงตั้งตัว", 250, 100, 0, TreasureType.OLD, 2);
                case 15:
                    return new MilestoneReward("Early Climb", "ช่วงตั้งตัว", 400, 150, 0, TreasureType.ANCIENT, 1);
                case 20:
                    return new MilestoneReward("Mid Circuit", "ช่วงเร่งเครื่อง", 350, 225, 10, TreasureType.ANCIENT, 1);
                case 30:
                    return new MilestoneReward("Mid Circuit", "ช่วงเร่งเครื่อง", 500, 325, 20, TreasureType.LEGENDARY, 1);
                case 40:
                    return new MilestoneReward("High Signal", "ช่วงโดดเด่น", 700, 450, 35, TreasureType.ANCIENT, 2);
                case 50:
                    return new MilestoneReward("High Signal", "ช่วงโดดเด่น", 900, 600, 50, TreasureType.LEGENDARY, 1);
                case 60:
                    return new MilestoneReward("Elite Run", "ช่วงชั้นแนวหน้า", 1100, 775, 70, TreasureType.LEGENDARY, 2);
                case 70:
                    return new MilestoneReward("Elite Run", "ช่วงชั้นแนวหน้า", 1350, 950, 90, TreasureType.MYTHICAL, 1);
                case 80:
                    return new MilestoneReward("Prestige Edge", "ช่วงศักดิ์ศรี", 1650, 1150, 120, TreasureType.LEGENDARY, 2);
                case 90:
                    return new MilestoneReward("Prestige Edge", "ช่วงศักดิ์ศรี", 2000, 1400, 160, TreasureType.MYTHICAL, 1);
                case 100:
                    return new MilestoneReward("Legend Mark", "ช่วงตำนาน", 2600, 1800, 240, TreasureType.MYTHICAL, 2);
                default:
                    return null;
            }
        }

        private String rewardSummary(Player player, boolean thai) {
            StringBuilder out = new StringBuilder();
            if (essence > 0) {
                out.append(essence).append(" Essence");
            }
            if (coins > 0) {
                if (out.length() > 0) out.append(", ");
                out.append(coins).append(" Coins");
            }
            if (gold > 0) {
                if (out.length() > 0) out.append(", ");
                out.append(gold).append(" Gold");
            }
            if (treasureType != null && treasureCount > 0) {
                if (out.length() > 0) out.append(", ");
                out.append(treasureCount).append("x ").append(thai ? treasureType.getPlainName(player) : treasureType.getItemName());
            }
            return out.toString();
        }
    }
}
