package com.houzicore.lobby.hub.modules.profile;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import com.houzicore.shared.account.CoreClientManager;
import com.houzicore.shared.common.util.C;
import com.houzicore.shared.common.util.UtilText;
import com.houzicore.shared.core.itemstack.ItemBuilder;
import com.houzicore.shared.core.shop.page.ShopPageBase;
import com.houzicore.shared.core.stats.StatsManager;
import com.houzicore.lobby.hub.HubManager;

public class PlayerProfilePage extends ShopPageBase<HubManager, PlayerProfileShop> {

    private final Player _target;
    private final StatsManager _stats;
    private final com.houzicore.shared.core.level.LvlManager _levels;

    public PlayerProfilePage(HubManager plugin, PlayerProfileShop shop, CoreClientManager clientManager, String name, Player viewer, Player target, StatsManager stats, com.houzicore.shared.core.level.LvlManager levels) {
        super(plugin, shop, clientManager, null, name, viewer, 54);
        _target = target;
        _stats = stats;
        _levels = levels;
        buildPage();
    }

    @Override
    protected void buildPage() {
        org.bukkit.inventory.Inventory inventory = getInventory();
        if (inventory == null) return;
        boolean thai = com.houzicore.shared.core.lang.LangManager.get().isThai(getPlayer());
        
        inventory.clear();
        getButtonMap().clear();

        if (_target == null || !_target.isOnline()) return;

        // Read stats
        long xp       = _stats.Get(_target) != null ? _stats.Get(_target).getStat("Global.XP") : 0;
        long wins     = _stats.Get(_target) != null ? _stats.Get(_target).getStat("Global.Wins") : 0;
        long kills    = _stats.Get(_target) != null ? _stats.Get(_target).getStat("Global.Kills") : 0;
        long streak   = _stats.Get(_target) != null ? _stats.Get(_target).getStat("Global.WinStreak") : 0;
        long login    = _stats.Get(_target) != null ? _stats.Get(_target).getStat("Global.LoginStreak") : 0;

        int lvl = _levels != null ? _levels.getLevel(_target) : 1;
        long xpIntoLevel = _levels != null ? _levels.getXpIntoCurrentLevel(_target) : xp;
        long xpNeeded = _levels != null ? _levels.getXpNeededForNextLevel(_target) : 0;
        int percent = _levels != null ? Math.round(_levels.getProgress(_target) * 100f) : 0;

        long essence = getPlugin().GetDonation().Get(getPlayer().getName()).GetEssence();
        ItemStack lightBlue = new ItemBuilder(Material.LIGHT_BLUE_STAINED_GLASS_PANE)
            .setTitle("§bEssence: §a" + essence).build();

        for (int i = 0; i < 54; i++) {
            setItem(i, lightBlue);
        }

        ItemStack nameItem = new ItemBuilder(Material.PLAYER_HEAD)
            .setTitle(C.cGold + C.Bold + _target.getName())
            .addLore("§8────────────────────────")
            .addLore(C.cGray + (thai ? "สรุปโปรไฟล์ผู้เล่น" : "Player snapshot"))
            .addLore(C.cGray + (thai ? "เลเวล: " : "Level: ") + C.cYellow + lvl)
            .addLore(C.cGray + (thai ? "ความคืบหน้า: " : "Progress: ") + C.cGreen + percent + "%")
            .setPlayerHead(_target.getName())
            .build();
        addButton(13, nameItem, (player, clickType) -> {});

        ItemStack lvlCard = new ItemBuilder(Material.EXPERIENCE_BOTTLE)
            .setTitle(C.cGreen + UtilText.toSmallCaps(thai ? "ระดับและ xp" : "level & xp"))
            .addLore("§8────────────────────────")
            .addLore(C.cGray + (thai ? "เลเวล: " : "Level: ") + C.cYellow + lvl)
            .addLore(C.cGray + (thai ? "XP รวม: " : "Total XP: ") + C.cAqua + xp)
            .addLore(C.cGray + (thai ? "ความคืบหน้า: " : "Progress: ") + C.cGreen + percent + "%" + C.cGray + " (" + C.cAqua + xpIntoLevel + C.cGray + (thai ? " ในเลเวล" : " in level") + ")")
            .addLore(C.cGray + (thai ? "อีก: " : "Next Level In: ") + C.cYellow + xpNeeded + " XP")
            .build();
        addButton(29, lvlCard, (player, clickType) -> {});

        ItemStack combatCard = hideInfo(new ItemBuilder(Material.IRON_SWORD)
            .setTitle(C.cRed + UtilText.toSmallCaps(thai ? "สถิติการเล่น" : "combat stats"))
            .addLore("§8────────────────────────")
            .addLore(C.cGray + (thai ? "ชนะ: " : "Wins: ") + C.cGreen + wins)
            .addLore(C.cGray + (thai ? "คิล: " : "Kills: ") + C.cYellow + kills)
            .addLore(C.cGray + (thai ? "วินสตรีค: " : "Win Streak: ") + C.cGold + streak)
            .build());
        addButton(31, combatCard, (player, clickType) -> {});

        ItemStack loginCard = new ItemBuilder(Material.CLOCK)
            .setTitle(C.cAqua + UtilText.toSmallCaps(thai ? "กิจกรรม" : "activity"))
            .addLore("§8────────────────────────")
            .addLore(C.cGray + (thai ? "ล็อกอินต่อเนื่อง: " : "Login Streak: ") + C.cGold + login + (thai ? " วัน" : " day(s)"))
            .build();
        addButton(33, loginCard, (player, clickType) -> {});


    }
}
