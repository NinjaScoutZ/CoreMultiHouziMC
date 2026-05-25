package com.houzicore.lobby.hub.modules;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;

import com.houzicore.lobby.hub.HubManager;
import com.houzicore.shared.account.CoreClientManager;
import com.houzicore.shared.core.donation.DonationManager;
import com.houzicore.shared.core.itemstack.ItemBuilder;
import com.houzicore.shared.core.itemstack.ItemStackFactory;
import com.houzicore.shared.core.shop.page.ShopPageBase;

public class DailyLoginPage extends ShopPageBase<HubManager, DailyLoginShop> {

    private final DailyLoginManager _manager;

    // Day reward items — visually distinct per tier
    private static final Material[] DAY_MATERIALS = {
        Material.IRON_NUGGET,    // Day 1
        Material.GOLD_NUGGET,    // Day 2
        Material.IRON_INGOT,     // Day 3
        Material.GOLD_INGOT,     // Day 4
        Material.EMERALD,        // Day 5
        Material.DIAMOND,        // Day 6
        Material.NETHER_STAR     // Day 7
    };

    // Slots: centered row with spacing (Row 3, slots 19-25)
    private static final int[] DAY_SLOTS = { 19, 20, 21, 22, 23, 24, 25 };

    public DailyLoginPage(HubManager plugin, DailyLoginShop shop, CoreClientManager clientManager,
                          DonationManager donationManager, Player player, DailyLoginManager manager) {
        super(plugin, shop, clientManager, donationManager, "§6§l✦ §f§lDaily Reward §6§l✦", player, 54);
        _manager = manager;
        buildPage();
    }

    @Override
    protected void buildPage() {
        // 1. Alternating glass border & background fill (Modern High-Contrast)
        ItemStack blueGlass = ItemStackFactory.Instance.CreateStack(
            Material.CYAN_STAINED_GLASS_PANE, (byte) 0, 1, "§r"
        );
        ItemStack grayGlass = ItemStackFactory.Instance.CreateStack(
            Material.BLACK_STAINED_GLASS_PANE, (byte) 0, 1, "§r"
        );

        for (int i = 0; i < getSize(); i++) {
            // Border slots definition (Row 0, Row 5, Col 0, Col 8)
            int row = i / 9;
            int col = i % 9;
            if (row == 0 || row == 5 || col == 0 || col == 8) {
                getInventory().setItem(i, blueGlass);
            } else {
                getInventory().setItem(i, grayGlass);
            }
        }

        // Calculate today's status
        int streak = _manager.getStreak(getPlayer());
        boolean canClaim = _manager.canClaim(getPlayer());
        int todayDay = canClaim ? (streak + 1) : streak;
        if (todayDay > 7) todayDay = 7;
        if (todayDay < 1) todayDay = 1;

        // 2. Stats / Profile Card (Slot 10)
        ItemStack head = new ItemStack(Material.PLAYER_HEAD);
        SkullMeta skullMeta = (SkullMeta) head.getItemMeta();
        if (skullMeta != null) {
            skullMeta.setOwningPlayer(getPlayer());
            skullMeta.setDisplayName("§b§lProfile Stats");
            java.util.List<String> headerLore = new java.util.ArrayList<>();
            headerLore.add("§8───────────────");
            headerLore.add("§7Player: §f" + getPlayer().getName());
            headerLore.add("§7Rank: " + getClient().GetRank().GetColor() + getClient().GetRank().Name);
            headerLore.add("§8───────────────");
            headerLore.add("§eCoins: §a" + getDonationManager().Get(getPlayer().getName()).getCoins());
            headerLore.add("§dEssence: §a" + getDonationManager().Get(getPlayer().getName()).GetEssence());
            headerLore.add("§8───────────────");
            skullMeta.setLore(headerLore);
            head.setItemMeta(skullMeta);
        }
        addButton(10, head, (player, clickType) -> {
            playAcceptSound(player);
        });

        // 3. Streak Info Card (Slot 13)
        ItemStack streakCard = new ItemBuilder(Material.WRITABLE_BOOK)
                .setTitle("§d§lSTREAK PROGRESSION")
                .addLore("§8───────────────")
                .addLore("§7Current Streak: §e" + Math.max(0, streak) + " / 7 Days")
                .addLore(canClaim ? "§a✔ Status: Can claim today!" : "§7Status: Come back tomorrow!")
                .addLore(canClaim ? "§a✔ สถานะ: รับรางวัลได้วันนี้!" : "§7สถานะ: กลับมาพรุ่งนี้!")
                .addLore("§8───────────────")
                .addLore("§7Login daily to keep your streak alive!")
                .addLore("§7เข้าสู่ระบบทุกวันเพื่อคงสถานะ streak!")
                .setGlow(canClaim)
                .build();
        addButton(13, streakCard, (player, clickType) -> {
            playAcceptSound(player);
        });

        // 4. Jackpot Info Card (Slot 16)
        ItemStack jackpotCard = new ItemBuilder(Material.NETHER_STAR)
                .setTitle("§6§l✦ Day 7 Jackpot ✦")
                .addLore("§8───────────────")
                .addLore("§7Day 7 Reward: §b500 Coins + Ancient Chest")
                .addLore("§8───────────────")
                .addLore("§eSovereign Bonus:")
                .addLore("§7Sovereign rank members receive")
                .addLore("§7a bonus multiplier on daily rewards!")
                .setGlow(true)
                .build();
        addButton(16, jackpotCard, (player, clickType) -> {
            playAcceptSound(player);
        });

        // 5. Day reward items timeline (slots 19-25)
        for (int i = 0; i < 7; i++) {
            int day = i + 1;
            int slot = DAY_SLOTS[i];

            boolean isClaimed = (day < todayDay) || (day == todayDay && !canClaim);
            boolean isToday = (day == todayDay) && canClaim;
            boolean isFuture = day > todayDay;

            String rewardDesc = getRewardDescription(day);

            if (isClaimed) {
                // Claimed: green, lime dye
                ItemStack item = new ItemBuilder(Material.LIME_DYE)
                        .setTitle("§a§m✔ Day " + day)
                        .addLore("§8───────────────")
                        .addLore("§7Reward: §a" + rewardDesc)
                        .addLore("§8───────────────")
                        .addLore("§a✔ Already Claimed!")
                        .addLore("§a✔ รับแล้ว!")
                        .build();
                addButton(slot, item, (player, clickType) -> {
                    playDenySound(player);
                });
            } else if (isToday) {
                // Today: glowing chest, claimable!
                ItemStack item = new ItemBuilder(Material.CHEST)
                        .setTitle("§e§l★ Day " + day + " §e§l★")
                        .addLore("§8───────────────")
                        .addLore("§7Reward: §e" + rewardDesc)
                        .addLore("§8───────────────")
                        .addLore("§a§l▶ Click to claim!")
                        .addLore("§a§l▶ คลิกเพื่อรับ!")
                        .setGlow(true)
                        .build();

                final int finalDay = day;
                addButton(slot, item, (player, clickType) -> {
                    if (_manager.canClaim(player)) {
                        _manager.claimReward(player, finalDay);
                        playAcceptSound(player);
                        buildPage();
                    } else {
                        playDenySound(player);
                    }
                });
            } else {
                // Future: locked, gray
                ItemStack item = new ItemBuilder(DAY_MATERIALS[i])
                        .setTitle("§7Day " + day)
                        .addLore("§8───────────────")
                        .addLore("§7Reward: §f" + rewardDesc)
                        .addLore("§8───────────────")
                        .addLore("§c✖ Locked")
                        .addLore("§c✖ ยังไม่ปลดล็อค")
                        .build();
                addButton(slot, item, (player, clickType) -> {
                    playDenySound(player);
                });
            }
        }

        // 6. Big CTA Claim Button (Slot 40)
        if (canClaim) {
            ItemStack claimButton = new ItemBuilder(Material.EMERALD_BLOCK)
                    .setTitle("§a§l▶ CLAIM TODAY'S REWARD ◀")
                    .addLore("§8───────────────")
                    .addLore("§7Reward for Day " + todayDay + ":")
                    .addLore("§e" + getRewardDescription(todayDay))
                    .addLore("§8───────────────")
                    .addLore("§aClick here to claim your reward!")
                    .addLore("§aคลิกที่นี่เพื่อรับรางวัลวันนี้!")
                    .setGlow(true)
                    .build();

            final int finalTodayDay = todayDay;
            addButton(40, claimButton, (player, clickType) -> {
                if (_manager.canClaim(player)) {
                    _manager.claimReward(player, finalTodayDay);
                    playAcceptSound(player);
                    buildPage();
                } else {
                    playDenySound(player);
                }
            });
        } else {
            int nextDay = todayDay + 1;
            if (nextDay > 7) nextDay = 1;
            ItemStack claimedButton = new ItemBuilder(Material.REDSTONE_BLOCK)
                    .setTitle("§c§lREWARD ALREADY CLAIMED")
                    .addLore("§8───────────────")
                    .addLore("§7Next reward Day " + nextDay + " unlocks:")
                    .addLore("§eTomorrow's reset")
                    .addLore("§8───────────────")
                    .addLore("§7Check back tomorrow for the next reward!")
                    .addLore("§7กลับมาวันพรุ่งนี้เพื่อรับรางวัลถัดไป!")
                    .build();

            addButton(40, claimedButton, (player, clickType) -> {
                playDenySound(player);
            });
        }
    }

    private String getRewardDescription(int day) {
        boolean isPremium = getClient().GetRank().Has(com.houzicore.shared.common.Rank.SOVEREIGN);
        String chestName = isPremium ? "Ancient Chest" : "Old Chest";
        String coins = switch (day) {
            case 1 -> "50";
            case 2 -> "75";
            case 3 -> "100";
            case 4 -> "150";
            case 5 -> "200";
            case 6 -> "300";
            default -> "500";
        };
        return coins + " Coins + " + chestName;
    }
}
